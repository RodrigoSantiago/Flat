package flat.widget.layout;

import flat.animations.StateInfo;
import flat.graphics.SmartContext;
import flat.uxml.Controller;
import flat.uxml.UXChildren;
import flat.uxml.UXStyleAttrs;
import flat.widget.Gadget;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enuns.Visibility;

import java.util.ArrayList;
import java.util.HashMap;

public class Grid extends Parent {

    private float[] rowSize = new float[1], colSize = new float[1];
    private Cell[] grid = new Cell[1];
    private int columns = 1, rows = 1;
    private float columnGap, rowGap;
    private HashMap<Widget, Cell> cells = new HashMap<>();

    private float[] rowSizeLayout, colSizeLayout;

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        int w = (int) style.asNumber("columns", getColumns());
        int h = (int) style.asNumber("rows", getRows());
        setDimension(w, h);

        super.applyAttributes(style, controller);
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        if (getStyle() == null) return;

        StateInfo info = getStateInfo();

        setColumnGap(getStyle().asNumber("column-gap", info, getColumnGap()));
        setRowGap(getStyle().asNumber("row-gap", info, getRowGap()));

        ArrayList<String> cSizes = getStyle().dynamicFinder("column-size-");
        for (String size : cSizes) {
            String number = size.substring(12);
            int n;
            try {
                n = Integer.parseInt(number);
            } catch (Exception e) {
                n = -1;
            }
            if (n >= 0 && n < columns) {
                setColumnSize(n, getStyle().asSize(size, info, getColumnSize(n)));
            }
        }
        ArrayList<String> rSizes = getStyle().dynamicFinder("row-size-");
        for (String size : rSizes) {
            String number = size.substring(9);
            int n;
            try {
                n = Integer.parseInt(number);
            } catch (Exception e) {
                n = -1;
            }
            if (n >= 0 && n < rows) {
                setRowSize(n, getStyle().asSize(size,  info, getRowSize(n)));
            }
        }
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        Gadget child;
        while ((child = children.next()) != null ) {
            if (child instanceof Cell) {
                Cell cell = (Cell) child;
                setCell(cell.getWidget(), cell.getColumn(), cell.getRow());
                setCellSpan(cell.getColumn(), cell.getRow(), cell.getColSpan(), cell.getRowSpan());
            }
        }
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));

        // Defined Width Math
        int match_parent_count = 0;
        float maxW = 0;
        for (int i = 0; i < columns; i++) {
            if (colSizeLayout[i] == MATCH_PARENT) {
                match_parent_count++;
            } else {
                maxW += colSizeLayout[i];
            }
        }
        // Undefined Width Math (divide)
        float reamingW = getInWidth() - maxW - (columnGap * columns);
        for (int i = 0; i < columns; i++) {
            if (colSizeLayout[i] == MATCH_PARENT) {
                colSizeLayout[i] = reamingW / match_parent_count;
            }
        }
        // Defined Height Math
        match_parent_count = 0;
        float maxH = 0;
        for (int i = 0; i < rows; i++) {
            if (rowSizeLayout[i] == MATCH_PARENT) {
                match_parent_count++;
            } else {
                maxH += rowSizeLayout[i];
            }
        }
        // Undefined Height Math (divide)
        float reamingH = getInHeight() - maxH - (rowGap * rows);
        for (int i = 0; i < rows; i++) {
            if (rowSizeLayout[i] == MATCH_PARENT) {
                rowSizeLayout[i] = reamingH / match_parent_count;
            }
        }
        for (Cell cell : cells.values()) {
            Widget child = cell.widget;
            if (child.getVisibility() == Visibility.Gone) continue;

            int col = cell.getColumn();
            int col2 = cell.getColumn() + cell.getColSpan();
            int row = cell.getRow();
            int row2 = cell.getRow() + cell.getRowSpan();
            // Position + outter padding
            float x = columnGap + getInX(), y = rowGap + getInY();
            for (int i = 0; i < col; i++) {
                x += colSizeLayout[i] + columnGap;
            }
            for (int i = 0; i < row; i++) {
                y += rowSizeLayout[i] + rowGap;
            }

            // Size + inner padding
            float w = columnGap * (col2 - col - 1), h = rowGap * (row2 - row - 1);
            for (int i = col; i < col2; i++) {
                w += colSizeLayout[i];
            }
            for (int i = row; i < row2; i++) {
                h += rowSizeLayout[i];
            }
            child.onLayout(w, h);
            child.setPosition(x, y);
        }
    }

    @Override
    public void onMeasure() {
        float mWidth = getPrefWidth(), mHeight = getPrefHeight();

        if (colSizeLayout == null || colSizeLayout.length != colSize.length) {
            colSizeLayout = colSize.clone();
        } else {
            System.arraycopy(colSize, 0, colSizeLayout, 0, colSize.length);
        }
        if (rowSizeLayout == null || rowSizeLayout.length != rowSize.length) {
            rowSizeLayout = rowSize.clone();
        } else {
            System.arraycopy(rowSize, 0, rowSizeLayout, 0, rowSize.length);
        }

        for (Cell cell : cells.values()) {
            Widget child = cell.widget;
            child.onMeasure();
            if (child.getVisibility() == Visibility.Gone) continue;

            int col = cell.getColumn();
            if (colSize[col] == WRAP_CONTENT) {
                if (child.getMeasureWidth() > colSizeLayout[col]) {
                    colSizeLayout[col] = child.getMeasureWidth();
                }
            }
            int row = cell.getRow();
            if (rowSize[row] == WRAP_CONTENT) {
                if (child.getMeasureHeight() > rowSizeLayout[row]) {
                    rowSizeLayout[row] = child.getMeasureHeight();
                }
            }
        }
        float maxW = columnGap;
        for (int i = 0; i < columns; i++) {
            maxW += colSizeLayout[i] + columnGap;
        }
        float maxH = rowGap;
        for (int i = 0; i < rows; i++) {
            maxH += rowSizeLayout[i] + rowGap;
        }
        if (mWidth == WRAP_CONTENT && maxW > mWidth) mWidth = maxW;
        if (mHeight == WRAP_CONTENT && maxH > mHeight) mHeight = maxH;

        mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
    }

    @Override
    public void remove(Widget widget) {
        super.remove(widget);
        Cell cell = cells.get(widget);
        if (cell != null) {
            removeCell(cell);
        }
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public float getColumnSize(int column) {
        return colSize[column];
    }

    public void setColumnSize(int column, float size) {
        if (column < this.columns && colSize[column] != size) {
            this.colSize[column] = size;
            invalidate(true);
        }
    }

    public float getRowSize(int row) {
        return rowSize[row];
    }

    public void setRowSize(int row, float size) {
        if (row < this.rows && rowSize[row] != size) {
            this.rowSize[row] = size;
            invalidate(true);
        }
    }

    public float getColumnGap() {
        return columnGap;
    }

    public void setColumnGap(float columnGap) {
        if (this.columnGap != columnGap) {
            this.columnGap = columnGap;
            invalidate(true);
        }
    }

    public float getRowGap() {
        return rowGap;
    }

    public void setRowGap(float rowGap) {
        if (this.rowGap != rowGap) {
            this.rowGap = rowGap;
            invalidate(false);
        }
    }

    void removeCell(Cell cell) {
        if (cell.widget.getParent() == this) {
            remove(cell.widget);
        } else if (cells.containsValue(cell)) {
            cells.remove(cell.widget);

            for (int x = cell.getColumn(); x < cell.getColumn() + cell.getColSpan(); x++) {
                for (int y = cell.getRow(); y < cell.getRow() + cell.getRowSpan(); y++) {
                    grid[x + y * columns] = null;
                }
            }
            invalidate(true);
        }
    }

    public void setCell(Widget widget, int col, int row) {
        int index = col + row * columns;
        Cell prev = grid[index];
        if ((prev == null && widget != null) || (prev != null && prev.widget != widget)) {
            if (prev != null) {
                removeCell(prev);
            }

            if (widget != null) {
                grid[index] = new Cell(widget, col, row, 1, 1);
                cells.put(widget, grid[index]);
                add(widget);
            }
            invalidate(true);
        }
    }

    public void setCellSpan(int col, int row, int colSpan, int rowSpan) {
        Cell cell = grid[col + row * columns];
        if (cell != null) {
            for (int x = cell.getColumn(); x < cell.getColumn() + cell.getColSpan(); x++) {
                for (int y = cell.getRow(); y < cell.getRow() + cell.getRowSpan(); y++) {
                    grid[x + y * columns] = null;
                }
            }
            cell.setColumnSpan(colSpan);
            cell.setRowSpan(rowSpan);
            for (int x = cell.getColumn(); x < cell.getColumn() + cell.getColSpan(); x++) {
                for (int y = cell.getRow(); y < cell.getRow() + cell.getRowSpan(); y++) {
                    Cell oldCell = grid[x + y * columns];
                    if (oldCell != null && oldCell != cell) {
                        removeCell(oldCell);
                    }
                    grid[x + y * columns] = cell;
                }
            }

            invalidate(true);
        }
    }

    public void setDimension(int columns, int rows) {
        if (this.columns != columns || this.rows != rows) {
            Cell[] grid = new Cell[columns * rows];
            for (int x = 0; x < columns || x < this.columns; x++) {
                for (int y = 0; y < rows || y < this.rows; y++) {
                    Cell cell = (x < this.columns && y < this.rows) ? this.grid[x + y * columns] : null;
                    if (x > columns || y > rows) {
                        if (cell != null) {
                            if (cell.getColumn() >= columns || cell.getRow() >= rows) {
                                removeCell(cell);
                            }
                        }
                    } else if (x > this.columns || y > this.columns) {
                        if (cell != null) {
                            removeCell(cell);
                        }
                    } else if (cell != null) {
                        int xSpan = cell.getColSpan();
                        if (cell.getColumn() + cell.getColSpan() > columns) {
                            xSpan = columns - cell.getColumn();
                        }
                        int ySpan = cell.getRowSpan();
                        if (cell.getRow() + cell.getRowSpan() > rows) {
                            ySpan = rows - cell.getRowSpan();
                        }
                        setCellSpan(x, y, xSpan, ySpan);
                        grid[x + y * columns] = cell;
                    }
                }
            }

            float[] colSize = new float[columns];
            for (int i = 0; i < columns && i < this.columns; i++) {
                colSize[i] = this.colSize[i];
            }

            float[] rowSize = new float[rows];
            for (int i = 0; i < rows && i < this.rows; i++) {
                rowSize[i] = this.rowSize[i];
            }

            this.grid = grid;
            this.rows = rows;
            this.columns = columns;
            this.rowSize = rowSize;
            this.colSize = colSize;

            invalidate(true);
        }
    }
}
