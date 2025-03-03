package flat.widget.layout;

import flat.animations.StateInfo;
import flat.graphics.SmartContext;
import flat.uxml.Controller;
import flat.uxml.TaskList;
import flat.uxml.UXAttrs;
import flat.uxml.UXChildren;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enums.Visibility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Grid extends Parent {

    private float horizontalSpacing;
    private float verticalSpacing;

    private float[] columns;
    private float[] rows;
    private int rowCount;
    private int columnCount;

    private final ArrayList<Cell> orderedCells = new ArrayList<>();
    private final HashMap<Widget, Integer> cells = new HashMap<>();

    private float[] columnsMeasureSize;
    private float[] rowsMeasureSize;
    private float[] columnsTempSize;
    private float[] rowsTempSize;
    private Column[] layoutColumns;
    private Row[] layoutRows;

    public Grid() {
        ensureRows(1);
        ensureColumns(1);
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        for (var child : children) {
            int x = (int) child.getAttributeNumber("cell-x", 0);
            int y = (int) child.getAttributeNumber("cell-y", 0);
            int w = (int) child.getAttributeNumber("cell-w", 0);
            int h = (int) child.getAttributeNumber("cell-h", 0);
            add(child.getWidget(), x, y, w, h);
        }
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();
        float[] columns = attrs.getAttributeSizeList("columns", this.columns);
        float[] rows = attrs.getAttributeSizeList("rows", this.rows);
        setDimensions(columns, rows);
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setHorizontalSpacing(attrs.getSize("horizontal-spacing", info, getHorizontalSpacing()));
        setVerticalSpacing(attrs.getSize("vertical-spacing", info, getVerticalSpacing()));
    }

    private void ensureColumns(int count) {
        columnCount = count;
        if (columns == null || columns.length < count) {
            columns = new float[count];
        }
        if (columnsMeasureSize == null || columnsMeasureSize.length < count) {
            columnsMeasureSize = new float[count];
        }
        if (columnsTempSize == null || columnsTempSize.length < count) {
            columnsTempSize = new float[count];
        }
        if (layoutColumns == null || layoutColumns.length < count) {
            layoutColumns = new Column[count];
            for (int i = 0; i < layoutColumns.length; i++) {
                layoutColumns[i] = new Column();
            }
        }
    }
    
    private void ensureRows(int count) {
        rowCount = count;
        if (rows == null || rows.length < count) {
            rows = new float[count];
        }
        if (rowsMeasureSize == null || rowsMeasureSize.length < count) {
            rowsMeasureSize = new float[count];
        }
        if (rowsTempSize == null || rowsTempSize.length < count) {
            rowsTempSize = new float[count];
        }
        if (layoutRows == null || layoutRows.length < count) {
            layoutRows = new Row[count];
            for (int i = 0; i < layoutRows.length; i++) {
                layoutRows[i] = new Row();
            }
        }
    }

    private boolean compare(float[] a, List<Float> b, int size) {
        if (size != b.size()) return false;
        for (int i = 0; i < size; i++) {
            if (a[i] != b.get(i)) return false;
        }
        return true;
    }

    private boolean compare(float[] a, float[] b, int size) {
        if (size != b.length) return false;
        for (int i = 0; i < size; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    public void setDimensions(float[] columns, float[] rows) {
        boolean c = compare(this.columns, columns, columnCount);
        boolean r = compare(this.rows, rows, rowCount);
        if (c && r) return;

        if (!c) {
            ensureColumns(columns.length);
            System.arraycopy(columns, 0, this.columns, 0, columns.length);
        }
        if (!r) {
            ensureRows(rows.length);
            System.arraycopy(rows, 0, this.rows, 0, rows.length);
        }
        reorderChildren();
    }

    public void setColumns(float[] columns) {
        if (compare(this.columns, columns, columnCount)) {
            return;
        }

        ensureColumns(columns.length);
        System.arraycopy(columns, 0, this.columns, 0, columns.length);
        reorderChildren();
    }

    public void setRows(float[] rows) {
        if (compare(this.rows, rows, rowCount)) {
            return;
        }

        ensureRows(rows.length);
        System.arraycopy(rows, 0, this.rows, 0, rows.length);
        reorderChildren();
    }

    public void setDimensions(List<Float> columns, List<Float> rows) {
        boolean c = compare(this.columns, columns, columnCount);
        boolean r = compare(this.rows, rows, rowCount);
        if (c && r) return;

        if (!c) {
            ensureColumns(columns.size());
            for (int i = 0; i < columns.size(); i++) {
                this.columns[i] = columns.get(i);
            }
        }
        if (!r) {
            ensureRows(rows.size());
            for (int i = 0; i < rows.size(); i++) {
                this.rows[i] = rows.get(i);
            }
        }
        reorderChildren();
    }

    public void setColumns(List<Float> columns) {
        if (compare(this.columns, columns, columnCount)) {
            return;
        }

        ensureColumns(columns.size());
        for (int i = 0; i < columns.size(); i++) {
            this.columns[i] = columns.get(i);
        }
        reorderChildren();
    }

    public void setRows(List<Float> rows) {
        if (compare(this.rows, rows, rowCount)) {
            return;
        }

        ensureRows(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            this.rows[i] = rows.get(i);
        }
        reorderChildren();
    }

    private void reorderChildren() {
        invalidate(true);
    }

    public void add(Widget widget, int x, int y, int w, int h) {
        w = Math.max(1, w);
        h = Math.max(1, h);

        TaskList tasks = new TaskList();
        if (attachAndAddChild(widget, tasks)) {
            orderedCells.add(new Cell(widget, x, y, w, h));
            cells.put(widget, orderedCells.size() - 1);
            tasks.run();
        }
    }

    @Override
    protected boolean detachChild(Widget child) {
        Integer index = cells.remove(child);
        if (index != null) {
            orderedCells.remove((int) index);
        }
        return super.detachChild(child);
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE) continue;
            child.onMeasure();
        }
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        float mWidth;
        float mHeight;
        if (wrapWidth) {
            Arrays.fill(columnsMeasureSize, 0);

            for (Cell cell : orderedCells) {
                if (cell.x < columnCount) {
                    columnsMeasureSize[cell.x] = Math.max(columnsMeasureSize[cell.x], cell.widget.getMeasureWidth());
                }
            }
            float definedWidth = 0;
            for (int i = 0; i < columnCount; i++) {
                float w = columnsMeasureSize[i];
                definedWidth += columns[i] == 0 ? w : columns[i];
            }

            float spacingX = getHorizontalSpacing() * Math.max(0, columnCount - 1);
            mWidth = definedWidth + spacingX;
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            Arrays.fill(rowsMeasureSize, 0);

            for (Cell cell : orderedCells) {
                if (cell.y < rowCount) {
                    rowsMeasureSize[cell.y] = Math.max(rowsMeasureSize[cell.y], cell.widget.getMeasureHeight());
                }
            }
            float definedHeight = 0;
            for (int i = 0; i < rowCount; i++) {
                float h = rowsMeasureSize[i];
                definedHeight += rows[i] == 0 ? h : rows[i];
            }
            float spacingY = getVerticalSpacing() * Math.max(0, rowCount - 1);
            mHeight = definedHeight + spacingY;
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }
        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);

        float spacingX = getHorizontalSpacing() * Math.max(0, columnCount - 1);
        float spacingY = getVerticalSpacing() * Math.max(0, rowCount - 1);
        float inWidth = Math.max(0, getInWidth() - spacingX);
        float inHeight = Math.max(0, getInHeight() - spacingY);

        for (int i = 0; i < columnCount; i++) {
            columnsMeasureSize[i] = columns[i] == MATCH_PARENT ? 0 : columns[i];
            columnsTempSize[i] = columns[i] == MATCH_PARENT ? 1 : 0;
        }
        for (int i = 0; i < rowCount; i++) {
            rowsMeasureSize[i] = rows[i] == MATCH_PARENT ? 0 : rows[i];
            rowsTempSize[i] = rows[i] == MATCH_PARENT ? 1 : 0;
        }

        for (Cell cell : orderedCells) {
            if (cell.x >= 0 && cell.x < columnCount && columns[cell.x] == WRAP_CONTENT) {
                if (cell.widget.getMeasureWidth() != MATCH_PARENT) {
                    columnsMeasureSize[cell.x] = Math.max(columnsMeasureSize[cell.x], cell.widget.getMeasureWidth());
                } else {
                    columnsTempSize[cell.x] = 1;
                }
            }
            if (cell.y >= 0 && cell.y < rowCount && rows[cell.y] == WRAP_CONTENT) {
                if (cell.widget.getMeasureHeight() != MATCH_PARENT) {
                    rowsMeasureSize[cell.y] = Math.max(rowsMeasureSize[cell.y], cell.widget.getMeasureHeight());
                } else {
                    rowsTempSize[cell.y] = 1;
                }
            }
        }

        float definedWidth = 0;
        for (int i = 0; i < columnCount; i++) {
            definedWidth += columnsMeasureSize[i];
        }

        float definedHeight = 0;
        for (int i = 0; i < rowCount; i++) {
            definedHeight += rowsMeasureSize[i];
        }

        int indefinedWidthCount = 0;
        for (int i = 0; i < columnCount; i++) {
            if (columnsTempSize[i] > 0) indefinedWidthCount++;
        }

        int indefinedHeightCount = 0;
        for (int i = 0; i < rowCount; i++) {
            if (rowsTempSize[i] > 0) indefinedHeightCount++;
        }

        if (definedWidth > inWidth) {
            float offWidth = inWidth / definedWidth;
            for (int i = 0; i < columnCount; i++) {
                layoutColumns[i].width = columnsMeasureSize[i] * offWidth;
            }
        } else {
            float leavWidth = Math.max(0, inWidth - definedWidth);
            for (int i = 0; i < columnCount; i++) {
                float ex = columnsTempSize[i] > 0 ? (leavWidth / indefinedWidthCount) : 0;
                layoutColumns[i].width = columnsMeasureSize[i] + ex;
            }
        }

        if (definedHeight > inHeight) {
            float offHeight = inHeight / definedHeight;
            for (int i = 0; i < rowCount; i++) {
                layoutRows[i].height = rowsMeasureSize[i] * offHeight;
            }
        } else {
            float leavHeight = Math.max(0, inHeight - definedHeight);
            for (int i = 0; i < rowCount; i++) {
                float ex = rowsTempSize[i] > 0 ? (leavHeight / indefinedHeightCount) : 0;
                layoutRows[i].height = rowsMeasureSize[i] + ex;
            }
        }

        float x = getInX();
        for (int i = 0; i < columnCount; i++) {
            Column layoutColumn = layoutColumns[i];
            layoutColumn.x = x;
            x += layoutColumn.width + getHorizontalSpacing();
        }

        float y = getInY();
        for (int i = 0; i < rowCount; i++) {
            Row layoutRow = layoutRows[i];
            layoutRow.y = y;
            y += layoutRow.height + getVerticalSpacing();
        }

        for (Cell cell : orderedCells) {
            if (cell.x >= 0 && cell.x < columnCount && cell.y >= 0 && cell.y < rowCount) {
                Column column = layoutColumns[cell.x];
                Row row = layoutRows[cell.y];
                int maxX = Math.min(columnCount, cell.x + cell.w);
                int maxY = Math.min(rowCount, cell.y + cell.h);

                float envWidth = 0;
                for (int i = cell.x; i < maxX; i++) {
                    envWidth += layoutColumns[i].width;
                    if (i > cell.x) {
                        envWidth += getHorizontalSpacing();
                    }
                }

                float envHeight = 0;
                for (int i = cell.y; i < maxY; i++) {
                    envHeight += layoutRows[i].height;
                    if (i > cell.y) {
                        envHeight += getVerticalSpacing();
                    }
                }

                float cW = Math.min(envWidth,
                        Math.min(cell.widget.getMeasureWidth(), cell.widget.getLayoutMaxWidth()));
                float cH = Math.min(envHeight,
                        Math.min(cell.widget.getMeasureHeight(), cell.widget.getLayoutMaxHeight()));
                
                cell.widget.onLayout(cW, cH);
                cell.widget.setLayoutPosition(column.x, row.y);
            } else {
                cell.widget.onLayout(0, 0);
                cell.widget.setLayoutPosition(0, 0);
            }
        }
    }

    @Override
    public boolean onLayoutSingleChild(Widget child) {
        Integer index = cells.get(child);
        if (index != null) {
            Cell cell = orderedCells.get((int) index);
            if (cell.x >= 0 && cell.x < columnCount && cell.y >= 0 && cell.y < rowCount) {
                if (columns[cell.x] == WRAP_CONTENT || columns[cell.y] == WRAP_CONTENT) {
                    return false;
                }
                
                Column column = layoutColumns[cell.x];
                Row row = layoutRows[cell.y];
                cell.widget.onMeasure();
                float cW = Math.min(column.width,
                        Math.min(cell.widget.getMeasureWidth(), cell.widget.getLayoutMaxWidth()));
                float cH = Math.min(row.height,
                        Math.min(cell.widget.getMeasureHeight(), cell.widget.getLayoutMaxHeight()));

                cell.widget.onLayout(cW, cH);
                cell.widget.setLayoutPosition(column.x, row.y);
            } else {
                cell.widget.onLayout(0, 0);
                cell.widget.setLayoutPosition(0, 0);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onDraw(SmartContext context) {
        drawBackground(context);
        drawRipple(context);
        drawChildren(context);
    }

    public float getHorizontalSpacing() {
        return horizontalSpacing;
    }

    public void setHorizontalSpacing(float horizontalSpacing) {
        if (this.horizontalSpacing != horizontalSpacing) {
            this.horizontalSpacing = horizontalSpacing;
            invalidate(true);
        }
    }

    public float getVerticalSpacing() {
        return verticalSpacing;
    }

    public void setVerticalSpacing(float verticalSpacing) {
        if (this.verticalSpacing != verticalSpacing) {
            this.verticalSpacing = verticalSpacing;
            invalidate(true);
        }
    }

    private static class Cell {
        private final Widget widget;
        private final int x, y, w, h;

        public Cell(Widget widget, int x, int y, int w, int h) {
            this.widget = widget;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public Widget getWidget() {
            return widget;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getW() {
            return w;
        }

        public int getH() {
            return h;
        }
    }

    private static class Column {
        float x;
        float width;
    }

    private static class Row {
        float y;
        float height;
    }
}
