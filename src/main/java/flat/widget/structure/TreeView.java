package flat.widget.structure;

import flat.events.ActionEvent;
import flat.events.DragEvent;
import flat.events.PointerEvent;
import flat.exception.FlatException;
import flat.math.Vector2;
import flat.uxml.*;
import flat.widget.Widget;
import flat.widget.enums.Direction;
import flat.widget.enums.Visibility;
import flat.widget.layout.LinearBox;
import flat.widget.stages.Dialog;
import flat.window.Application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TreeView extends RecycleView {

    private final HashMap<Object, TreeItemCell> cellById = new HashMap<>();
    private TreeItemCell root;
    private ArrayList<TreeItemCell> visibleCells = new ArrayList<>();
    private ArrayList<TreeItemCell> selection = new ArrayList<>();
    private TreeItemData[] selectionData = new TreeItemData[0];
    private boolean invalidSelection;
    
    private boolean multiSelectionEnabled = true;
    private boolean dragItemEnabled = true;
    private boolean selfDropItemEnabled = true;

    private Dialog dragPopup;
    private ListItem dragItem1;
    private ListItem dragItem2;
    private Widget dropBox;
    private Vector2 dropBoxPos = new Vector2();

    private UXListener<TreeViewStyle> stylizeListener;
    private UXListener<TreeViewCellAction> cellActionListener;
    private UXValueListener<TreeItemData[]> selectionChangeListener;
    private final UXListener<ActionEvent> itemStateListener = (e) -> {
        ListItem item = e.getSource();
        if (item.getIndex() >= 0 && item.getIndex() < visibleCells.size()) {
            var cell = visibleCells.get(item.getIndex());
            if (cell.isOpen()) {
                cell.close();
            } else {
                cell.open();
            }
            refreshItems();
        }
    };

    private final UXListener<PointerEvent> itemPointer = (e) -> {
        ListItem item = e.getSource();
        if (item.getIndex() >= 0 && item.getIndex() < visibleCells.size() && !item.isUndefined()) {
            var cell = visibleCells.get(item.getIndex());
            if (cell != null && e.getType() == PointerEvent.PRESSED && e.getClickCount() == 2 && e.getPointerID() == 1) {
                if (cell.getData().isFolder()) {
                    if (cell.isOpen()) {
                        cell.close();
                    } else {
                        cell.open();
                    }
                    refreshItems();
                } else {
                    UXListener.safeHandle(cellActionListener, new TreeViewCellAction(this, cell, cell.getData(), item));
                }
            }
        }
    };

    private final ListViewAdapter<TreeItemCell> adapter = new ListViewAdapter<>() {
        @Override
        public int size() {
            return visibleCells.size();
        }

        @Override
        public Widget createListItem() {
            ListItem item = new ListItem();
            item.setChangeStateListener(itemStateListener);
            item.setPointerListener(itemPointer);
            return item;
        }

        @Override
        public void buildListItem(int index, Widget item) {
            TreeItemCell cell = visibleCells.get(index);
            if (stylizeListener != null) {
                ListItem listItem = (ListItem) item;
                listItem.setIndex(index);
                UXListener.safeHandle(stylizeListener,
                        new TreeViewStyle(TreeView.this, cell, cell.getData(), listItem, false, false));
            }
        }

        @Override
        public RecycleView getListView() {
            return TreeView.this;
        }
    };

    public TreeView() {
        root = new TreeItemCell(visibleCells);
        setAdapter(adapter);

        dragItem1 = new ListItem();
        dragItem2 = new ListItem();
        LinearBox lbox = new LinearBox();
        lbox.setDirection(Direction.VERTICAL);
        lbox.add(dragItem1, dragItem2);

        dragPopup = new Dialog();
        dragPopup.addStyle("tree-view-drag-box");
        dragPopup.build(lbox);
        dragPopup.setHandleEventsEnabled(false);

        dropBox = new LinearBox();
        dropBox.addStyle("tree-view-drop-box");
        dropBox.setHandleEventsEnabled(false);
        dropBox.setVisibility(Visibility.GONE);
        add(dropBox);
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setMultiSelectionEnabled(attrs.getAttributeBool("multi-selection-enabled", isMultiSelectionEnabled()));
        setDragItemEnabled(attrs.getAttributeBool("drag-item-enabled", isDragItemEnabled()));
        setSelfDropItemEnabled(attrs.getAttributeBool("self-drop-item-enabled", isSelfDropItemEnabled()));
        setSelectionChangeListener(attrs.getAttributeValueListener("on-selection-change", TreeItemData[].class, controller, getSelectionChangeListener()));
        setStylizeListener(attrs.getAttributeListener("on-stylize", TreeViewStyle.class, controller, getStylizeListener()));
        setCellActionListener(attrs.getAttributeListener("on-cell-action", TreeViewCellAction.class, controller, getCellActionListener()));
    }

    @Override
    public void onMeasure() {
        dropBox.onMeasure();
        super.onMeasure();
    }

    @Override
    public void onLayout(float width, float height) {
        super.onLayout(width, height);
        if (dropBox.getVisibility() != Visibility.GONE) {
            performSingleLayoutFree(getInWidth(), getInHeight(), dropBox);
            dropBox.setLayoutPosition(dropBoxPos.x, dropBoxPos.y);
        }
    }

    @Override
    protected boolean detachChild(Widget child) {
        if (child == dropBox) {
            return false;
        }
        return super.detachChild(child);
    }

    private void localAddItem(TreeItemData load, boolean select) {
        Object parentId = load.getParentId();
        TreeItemCell folderCell = cellById.get(parentId);
        if (folderCell == null) {
            folderCell = root;
        }

        TreeItemCell cell = new TreeItemCell(visibleCells, load);
        cellById.put(load.getId(), cell);
        folderCell.add(cell);

        if (select && visibleCells.contains(cell)) {
            selection.add(cell);
            invalidateSelection();
            cell.setSelected(true);
        }
    }

    private void localRemoveItem(TreeItemData item) {
        TreeItemCell cell = cellById.remove(item.getId());
        if (cell != null && cell.getTopParent() == root) {
            cell.getParent().remove(cell);
        }
        if (cell != null) {
            for (var child : cell.getChildren()) {
                localRemoveItem(child.getData());
            }
        }
    }

    public void addTreeItem(TreeItemData item, boolean select) {
        checkNewChild(item);
        if (select) {
            resetSelection();
        }
        localAddItem(item, select);
        refreshItems();
        fireValueListener();
    }

    public void addTreeItems(List<? extends TreeItemData> items, boolean select) {
        for (var item : items) {
            checkNewChild(item);
        }
        if (select) {
            resetSelection();
        }
        for (var item : items) {
            localAddItem(item, select);
        }
        refreshItems();
        fireValueListener();
    }

    public void removeTreeItem(TreeItemData item) {
        resetSelection();
        localRemoveItem(item);
        refreshItems();
        fireValueListener();
    }

    public void removeTreeItems(List<? extends TreeItemData> items) {
        resetSelection();
        for (var item : items) {
            localRemoveItem(item);
        }
        refreshItems();
        fireValueListener();
    }
    
    public void removeAllTreeItems() {
        removeTreeItems(root.getChildren().stream().map(TreeItemCell::getData).toList());
    }

    private void checkNewChild(TreeItemData child) {
        if (cellById.containsKey(child.getId())) {
            throw new FlatException("A TreeView cannot have two items with the same Id");
        }
    }

    private void select(TreeItemCell cell, boolean reverse) {
        if (!selection.contains(cell)) {
            invalidateSelection();
            selection.add(cell);
            cell.setSelected(true);
        } else if (reverse) {
            invalidateSelection();
            selection.remove(cell);
            cell.setSelected(false);
        }
    }

    private void selectNewCell(TreeItemCell cell, boolean add) {
        if (!add) {
            resetSelection();
        }
        select(cell, false);
        refreshItems();
    }

    private void selectRangeCell(TreeItemCell cell) {
        TreeItemCell last = selection.get(selection.size() - 1);
        int indexA = visibleCells.indexOf(last);
        int indexB = visibleCells.indexOf(cell);
        if (indexA > -1 && indexB > -1) {
            for (int i = Math.min(indexA, indexB); i <= Math.max(indexA, indexB); i++) {
                select(visibleCells.get(i), false);
            }
            refreshItems();
        } else {
            selectNewCell(cell, true);
        }
    }
    
    public void setSelection(TreeItemCell... cells) {
        resetSelection();
        for (var cell : cells) {
            var itemCell = cellById.get(cell.getData().getId());
            if (itemCell != null) {
                select(itemCell, false);
                select(itemCell, false);
            }
        }
        refreshItems();
    }
    
    public void setSelection(TreeItemData... cells) {
        resetSelection();
        for (var cell : cells) {
            var itemCell = cellById.get(cell.getId());
            if (itemCell != null) {
                select(itemCell, false);
            }
        }
        refreshItems();
        fireValueListener();
    }
    
    public void slideTo(TreeItemData data) {
        var cell = getCellByData(data);
        if (cell == null) return;
        
        var parent = cell.getParent();
        int id = cell.visibleIndex();
        while (parent != null && !parent.isRoot()) {
            parent.open();
            id += parent.visibleIndex();
            parent = parent.getParent();
        }
        refreshItems();
        float target = getItemHeight() * id;
        if (getViewOffsetY() + getViewDimensionY() < target || getViewOffsetY() > target) {
            slideVerticalTo(target - getViewDimensionY() * 0.5f);
        }
    }

    public void clearSelection() {
        resetSelection();
        refreshItems();
        fireValueListener();
    }

    private void invalidateSelection() {
        invalidSelection = true;
    }

    private void resetSelection() {
        for (var cell : selection) {
            cell.setSelected(false);
            cell.setDragged(false);
        }
        if (!selection.isEmpty()) {
            invalidateSelection();
        }
        selection.clear();
        dragClear();
    }

    public UXListener<TreeViewStyle> getStylizeListener() {
        return stylizeListener;
    }

    public void setStylizeListener(UXListener<TreeViewStyle> stylizeListener) {
        this.stylizeListener = stylizeListener;
        refreshItems();
    }

    public UXValueListener<TreeItemData[]> getSelectionChangeListener() {
        return selectionChangeListener;
    }

    public void setSelectionChangeListener(UXValueListener<TreeItemData[]> selectionChangeListener) {
        this.selectionChangeListener = selectionChangeListener;
    }

    public List<TreeItemData> getSelection() {
        return List.of(selectionData);
    }

    private void fireValueListener() {
        if (!invalidSelection) return;
        invalidSelection = false;

        TreeItemData[] pOldSelection = selectionData;
        selectionData = selection.stream().map(TreeItemCell::getData).toList().toArray(new TreeItemData[0]);
        if (selectionChangeListener != null) {
            UXValueListener.safeHandle(selectionChangeListener, new ValueChange<>(this, pOldSelection, selectionData));
        }
    }

    public boolean isDragItemEnabled() {
        return dragItemEnabled;
    }

    public void setDragItemEnabled(boolean dragItemEnabled) {
        this.dragItemEnabled = dragItemEnabled;
    }

    public boolean isSelfDropItemEnabled() {
        return selfDropItemEnabled;
    }

    public void setSelfDropItemEnabled(boolean selfDropItemEnabled) {
        if (this.selfDropItemEnabled != selfDropItemEnabled) {
            this.selfDropItemEnabled = selfDropItemEnabled;
        }
    }

    public UXListener<TreeViewCellAction> getCellActionListener() {
        return cellActionListener;
    }

    public void setCellActionListener(UXListener<TreeViewCellAction> cellActionListener) {
        this.cellActionListener = cellActionListener;
    }

    @Override
    public void pointer(PointerEvent ev) {
        super.pointer(ev);

        if (ev.getSource() == this && ev.getType() == PointerEvent.RELEASED) {
            // clearSelection();
        }
        if (ev.getSource() instanceof ListItem item && ev.getType() == PointerEvent.PRESSED) {
            if (item.getIndex() >= 0 && item.getIndex() < visibleCells.size()) {
                var pointerCell = visibleCells.get(item.getIndex());
                if (!ev.getSource().isUndefined()) {
                    if (ev.getPointerID() == 1) {
                        onCellPointerPressed(pointerCell, ev);
                    } else if (ev.getPointerID() == 2) {
                        onCellPointerRequestContext(pointerCell, ev);
                    }
                }
            }
        }
        if (ev.getSource() instanceof ListItem item && ev.getType() == PointerEvent.RELEASED) {
            if (item.getIndex() >= 0 && item.getIndex() < visibleCells.size()) {
                var pointerCell = visibleCells.get(item.getIndex());
                if (!ev.getSource().isUndefined()) {
                    if (ev.getPointerID() == 1) {
                        onCellPointerReleased(pointerCell, ev);
                    }
                }
            }
        }
    }

    TreeViewDragData dragData;

    @Override
    public void drag(DragEvent event) {
        if (event.getType() == DragEvent.STARTED) {
            if (isDragItemEnabled() &&
                    !selection.isEmpty() &&
                    event.getSource() instanceof ListItem item &&
                    event.getDistance() > getItemHeight() * 0.3f &&
                    item.getIndex() >= 0 && item.getIndex() < visibleCells.size()) {
                dragData = new TreeViewDragData(this, selection.stream().map(TreeItemCell::getData).toList());
                cellToRemoveOnRelease = null;
                event.accept(this);
                event.setData(dragData);
                dragStart(visibleCells.get(item.getIndex()), new Vector2(event.getX(), event.getY()));
            }
        }
        if (event.getType() == DragEvent.OVER && event.getData() == dragData) {
            if (dragData.isCancelled()) {
                event.cancel();
                dragClear();
            } else {
                dragOver(new Vector2(event.getX(), event.getY()));
                if (isSelfDropItemEnabled()) {
                    dragHover(new Vector2(event.getX(), event.getY()));
                } else {
                    dragExit();
                }
            }
        }
        super.drag(event);
        if (event.getType() == DragEvent.HOVER && event.isAccepted()) {
            dragHover(new Vector2(event.getX(), event.getY()));
        }
        if (event.getType() == DragEvent.EXITED) {
            dragExit();
        }
        if (event.getType() == DragEvent.DONE) {
            dragClear();
        }
    }

    public TreeItemCell getCellByData(TreeItemData data) {
        if (data == null) return null;
        return cellById.get(data.getId());
    }

    public ListItem getItemByCell(TreeItemCell cell) {
        if (cell == null) return null;
        int index = visibleCells.indexOf(cell);
        if (index > -1) {
            for (var item : items) {
                if (item instanceof ListItem listItem && listItem.getIndex() == index) {
                    return listItem;
                }
            }
        }
        return null;
    }
    
    public boolean isMultiSelectionEnabled() {
        return multiSelectionEnabled;
    }
    
    public void setMultiSelectionEnabled(boolean multiSelectionEnabled) {
        if (this.multiSelectionEnabled != multiSelectionEnabled) {
            this.multiSelectionEnabled = multiSelectionEnabled;
            if (!multiSelectionEnabled) {
                clearSelection();
            }
        }
    }
    
    // ------------------------------
    private void dragOver(Vector2 point) {
        dragPopup.setPrefWidth(getWidth());
        dragPopup.moveTo(point.x, point.y);
    }

    private void dragHover(Vector2 point) {
        TreeDropPos treeDropPos = getDropPos(point.x, point.y);

        Vector2 zpos = new Vector2(0, 0);
        localToScreen(zpos);

        Vector2 rpos = new Vector2(point);
        screenToLocal(rpos);

        if (rpos.y < getOutY()) {
            slideVertical(-getScrollSensibility() * Application.getLoopTime() * 10f);
        } else if (rpos.y > getOutY() + getOutHeight()) {
            slideVertical(getScrollSensibility() * Application.getLoopTime() * 10f);
        }

        if (treeDropPos.getCell() == null) {
            dropBox.setVisibility(Visibility.GONE);
        } else {
            float ih = getItemHeight();
            dropBoxPos = new Vector2(0, treeDropPos.getPos() * ih - dropBox.getHeight() / 2f);
            if (treeDropPos.getItem() == null) {
                localToScreen(dropBoxPos);
            } else {
                treeDropPos.getItem().localToScreen(dropBoxPos);
            }
            dropBox.getParent().screenToLocal(dropBoxPos);
            dropBox.setVisibility(Visibility.VISIBLE);
            invalidate(true);
        }
    }

    private void dragExit() {
        dropBox.setVisibility(Visibility.GONE);
    }

    private void dragClear() {
        for (var cell : visibleCells) {
            cell.setDragged(false);
        }
        if (dragData != null) {
            dragData.setCancelled();
            dragData = null;
        }
        dragPopup.hide();
        dropBox.setVisibility(Visibility.GONE);
        refreshItems();
    }

    private void dragStart(TreeItemCell cell, Vector2 pos) {
        for (var selectedCell : selection) {
            selectedCell.setDragged(true);
        }
        var cell1 = selection.get(0);
        dragItem1.setIndex(visibleCells.indexOf(cell1));
        UXListener.safeHandle(stylizeListener,
                new TreeViewStyle(this, cell1, cell1.getData(), dragItem1, true, false));
        if (selection.size() == 1) {
            dragItem2.setVisibility(Visibility.GONE);
        } else {
            var cell2 = selection.get(1);
            dragItem2.setVisibility(Visibility.VISIBLE);
            dragItem2.setIndex(visibleCells.indexOf(cell2));
            UXListener.safeHandle(stylizeListener,
                    new TreeViewStyle(this, cell2, cell2.getData(), dragItem2, true, true));
        }
        dragPopup.setPrefWidth(getWidth());
        dragPopup.show(getActivity(), pos.x, pos.y);
        refreshItems();
    }

    public TreeDropPos getDropPos(float x, float y) {
        Vector2 rpos = new Vector2(x, y);
        screenToLocal(rpos);

        if (rpos.x < getOutX() || rpos.x > getOutX() + getOutWidth()) {
            dropBox.setVisibility(Visibility.GONE);
            return new TreeDropPos(null, null, 0, 0);
        }

        var item = findByPosition(0, y, false);
        TreeItemCell cell;
        if (item instanceof ListItem listItem) {
            cell = visibleCells.get(listItem.getIndex());
        } else {
            cell = visibleCells.get(visibleCells.size() - 1);
            Vector2 pos = new Vector2(0, 0);
            for (var itemChild : getChildrenIterable()) {
                if (itemChild instanceof ListItem litem) {
                    item = itemChild;
                    if (litem.getIndex() >= 0 && litem.getIndex() < visibleCells.size()) {
                        cell = visibleCells.get(litem.getIndex());
                    }
                    itemChild.screenToLocal(pos.set(x, y));
                    if (item.getHeight() > pos.y) {
                        break;
                    }
                }
            }
        }
        Vector2 pos = new Vector2(x, y);
        if (item == null) {
            screenToLocal(pos);
        } else {
            item.screenToLocal(pos);
        }
        float ih = getItemHeight();
        float dropPos;
        if (cell.isFolder()) {
            dropPos = pos.y < ih / 3 ? 0 : pos.y < ih / 3 * 2 ? 0.5f : 1;
        } else {
            dropPos = pos.y < ih / 2 ? 0 : 1;
        }
        float realPos = pos.y < ih / 3 ? 0 : pos.y < ih / 3 * 2 ? 0.5f : 1;
        return new TreeDropPos(item instanceof ListItem listItem ? listItem : null, cell, dropPos, realPos);
    }
    
    TreeItemCell cellToRemoveOnRelease;

    private void onCellPointerPressed(TreeItemCell cell, PointerEvent event) {
        if (isHoldShift(event) && !selection.isEmpty()) {
            selectRangeCell(cell);
        } else {
            if (isHoldCtrl(event) && selection.contains(cell)) {
                cellToRemoveOnRelease = cell;
            } else {
                selectNewCell(cell, isHoldCtrl(event));
            }
        }
        fireValueListener();
    }
    
    private void onCellPointerReleased(TreeItemCell cell, PointerEvent event) {
        if (cellToRemoveOnRelease != null) {
            select(cellToRemoveOnRelease, true);
            refreshItems();
            cellToRemoveOnRelease = null;
            fireValueListener();
        }
    }

    private void onCellPointerRequestContext(TreeItemCell cell, PointerEvent event) {
        if (!cell.isSelected()) {
            if (isHoldShift(event) && !selection.isEmpty()) {
                selectRangeCell(cell);
            } else {
                selectNewCell(cell, isHoldCtrl(event));
            }
        } else {
            // Move to TOP
            selection.remove(cell);
            selection.add(cell);
        }
        fireValueListener();
    }
    
    private boolean isHoldCtrl(PointerEvent event) {
        return multiSelectionEnabled && event.isCtrlDown();
    }
    
    private boolean isHoldShift(PointerEvent event) {
        return multiSelectionEnabled && event.isShiftDown();
    }
}
