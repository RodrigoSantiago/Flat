package flat.widget.structure;

import flat.exception.FlatException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreeItemCell {
    private final List<TreeItemCell> list;
    private final ArrayList<TreeItemCell> children = new ArrayList<>();
    private boolean open;
    private TreeItemCell parent;
    private boolean selected;
    private boolean dragged;
    private final TreeItemData data;
    private final boolean folder;
    private int levels;

    public TreeItemCell(List<TreeItemCell> list) {
        this.list = list;
        data = null;
        folder = true;
        open = true;
        levels = -1;
    }

    public TreeItemCell(List<TreeItemCell> list, TreeItemData data) {
        this.list = list;
        this.data = data;
        this.folder = data.isFolder();
    }

    public boolean isRoot() {
        return data == null;
    }

    public TreeItemData getData() {
        return data;
    }

    public boolean isFolder() {
        return folder;
    }

    public int getLevels() {
        return levels;
    }

    public boolean isSelected() {
        return selected;
    }

    void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isDragged() {
        return dragged;
    }

    void setDragged(boolean dragged) {
        this.dragged = dragged;
    }

    public TreeItemCell getTopParent() {
        return parent == null ? this : parent.getTopParent();
    }

    public TreeItemCell getParent() {
        return parent;
    }

    public List<TreeItemCell> getChildren() {
        return Collections.unmodifiableList(children);
    }

    private void setParent(TreeItemCell parent) {
        if (this.parent != null && parent != null) {
            throw new FlatException("The parent cannot be replaced");
        }
        if (parent != null) {
            levels = parent.levels + 1;
        }
        this.parent = parent;
        this.open = false;
    }

    void add(TreeItemCell item) {
        int index = findInsertIndex(item);
        add(index, item);
    }

    private int findInsertIndex(TreeItemCell item) {
        var ip = item.getData();
        for (int i = 0; i < children.size(); i++) {
            var child = children.get(i);
            int compare = child.getData().compareTo(ip);
            if (compare > 0) {
                return i;
            }
        }
        return children.size();
    }

    private int visibleIndex() {
        if (getParent() == null) {
            return -1; // Root
        } else {
            int index = list.indexOf(this);
            if (index == -1) {
                return -2; // Invisible
            } else {
                return index; // Visible
            }
        }
    }

    private void add(int index, TreeItemCell item) {
        item.setParent(this);
        int vIndex = visibleIndex();
        if (vIndex == -2 || !isOpen()) {
            children.add(index, item);
        } else {
            if (index == children.size()) {
                int total = getTotalOpenItems();
                children.add(index, item);
                list.add(vIndex + 1 + total, item);
            } else {
                var child = children.get(index);
                children.add(index, item);
                list.add(list.indexOf(child), item);
            }
        }
    }

    void remove(TreeItemCell item) {
        item.close();
        item.setParent(null);
        children.remove(item);
        list.remove(item);
    }

    void clear() {
        if (isOpen()) {
            int s = list.indexOf(this) + 1;
            list.subList(s, s + getTotalOpenItems()).clear();
        }
        for (var item : children) {
            item.setParent(null);
        }
        children.clear();
    }

    public boolean isOpen() {
        return open;
    }

    public void open() {
        if (isFolder() && !isOpen()) {
            open = true;
            int index = list.indexOf(this);
            if (children.isEmpty()) {
                if (index > -1) list.set(index, this);
            } else {
                list.addAll(index + 1, children);
            }
        }
    }

    public void close() {
        if (isFolder() && isOpen()) {
            int vIndex = visibleIndex();
            if (children.isEmpty()) {
                setClosed();
                if (vIndex > -1) list.set(vIndex, this);
            } else {
                int total = getTotalOpenItems();
                setClosed();
                int s = vIndex + 1;
                list.subList(s, s + total).clear();
            }
        }
    }

    private void setClosed() {
        open = false;
        for (var child : children) {
            child.setClosed();
        }
    }

    private int getTotalOpenItems() {
        int count = 0;
        if (isOpen()) {
            for (TreeItemCell child : children) {
                count += child.getTotalOpenItems() + 1;
            }
        }
        return count;
    }
}
