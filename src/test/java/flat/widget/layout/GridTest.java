package flat.widget.layout;

import flat.uxml.*;
import flat.uxml.value.*;
import flat.widget.Widget;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class GridTest {

    @Test
    public void childrenFromUx() {
        Grid grid = new Grid();
        Panel child1 = new Panel();
        Panel child2 = new Panel();
        Panel child3 = new Panel();

        UXChildren uxChild = mockChildren(
                new TestCell(child1, 0, 0, 1, 1),
                new TestCell(child2, 0, 1, 1, 1),
                new TestCell(child3, 1, 0, 2, 1));

        assertNull(child1.getParent());
        assertNull(child2.getParent());
        assertNull(child3.getParent());
        grid.applyChildren(uxChild);
        assertEquals(3, grid.getChildrenIterable().size());
        assertEquals(child1, grid.getChildrenIterable().get(0));
        assertEquals(child2, grid.getChildrenIterable().get(1));
        assertEquals(child3, grid.getChildrenIterable().get(2));
        assertEquals(grid, child1.getParent());
        assertEquals(grid, child2.getParent());
        assertEquals(grid, child3.getParent());
        assertCell(grid, child1, 0, 0, 1, 1);
        assertCell(grid, child2, 0, 1, 1, 1);
        assertCell(grid, child3, 1, 0, 2, 1);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        Grid grid = new Grid();

        assertEquals(0, grid.getHorizontalSpacing(), 0.001f);
        assertEquals(0, grid.getVerticalSpacing(), 0.001f);
        assertEquals(1, grid.getColumnCount());
        assertEquals(1, grid.getRowCount());
        assertEquals(0, grid.getColumnWidth(0), 0.001f);
        assertEquals(0, grid.getRowHeight(0), 0.001f);

        grid.setAttributes(createNonDefaultValues(), null);
        grid.applyAttributes(controller);

        assertEquals(0, grid.getHorizontalSpacing(), 0.001f);
        assertEquals(0, grid.getVerticalSpacing(), 0.001f);
        assertEquals(2, grid.getColumnCount());
        assertEquals(2, grid.getRowCount());
        assertEquals(10, grid.getColumnWidth(0), 0.001f);
        assertEquals(20, grid.getColumnWidth(1), 0.001f);
        assertEquals(15, grid.getRowHeight(0), 0.001f);
        assertEquals(25, grid.getRowHeight(1), 0.001f);

        grid.applyStyle();

        assertEquals(16, grid.getHorizontalSpacing(), 0.001f);
        assertEquals(8, grid.getVerticalSpacing(), 0.001f);
        assertEquals(2, grid.getColumnCount());
        assertEquals(2, grid.getRowCount());
        assertEquals(10, grid.getColumnWidth(0), 0.001f);
        assertEquals(20, grid.getColumnWidth(1), 0.001f);
        assertEquals(15, grid.getRowHeight(0), 0.001f);
        assertEquals(25, grid.getRowHeight(1), 0.001f);
    }

    @Test
    public void measure() {
        Grid grid = new Grid();

        Panel child1 = new Panel();
        child1.setPrefSize(16, 8);
        Panel child2 = new Panel();
        child2.setPrefSize(8, 16);
        Panel child3 = new Panel();
        child3.setPrefSize(5, 7);

        grid.setColumns(new float[] {0, 0, 0});
        grid.setRows(new float[] {0, 0, 0});
        grid.onMeasure();
        assertMeasure(grid, 0, 0);

        grid.add(child1, 0, 0);
        grid.add(child2, 1, 0);
        grid.add(child3, 0, 1);

        // Wrap All
        grid.onMeasure();
        assertMeasure(grid, 24, 23);

        // Wrap + Match Parent
        child1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child3.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        grid.onMeasure();
        assertMeasure(grid, Widget.MATCH_PARENT, Widget.MATCH_PARENT);

        // Measure All
        grid.setColumns(new float[] {5, 7, 9});
        grid.setRows(new float[] {2, 11, 13});
        grid.onMeasure();
        assertMeasure(grid, 21, 26);

        // Match Parent All
        grid.setColumns(new float[] {Widget.MATCH_PARENT, Widget.MATCH_PARENT, Widget.MATCH_PARENT});
        grid.setRows(new float[] {Widget.MATCH_PARENT, Widget.MATCH_PARENT, Widget.MATCH_PARENT});
        grid.onMeasure();
        assertMeasure(grid, Widget.MATCH_PARENT, Widget.MATCH_PARENT);

        // Measure All + Spacing
        grid.setHorizontalSpacing(3);
        grid.setVerticalSpacing(5);
        grid.setColumns(new float[] {5, 7, 9});
        grid.setRows(new float[] {2, 11, 13});
        grid.onMeasure();
        assertMeasure(grid, 27, 36);

        // Measure All + Spacing + Single
        grid.setHorizontalSpacing(3);
        grid.setVerticalSpacing(5);
        grid.setColumns(new float[] {15});
        grid.setRows(new float[] {16});
        grid.onMeasure();
        assertMeasure(grid, 15, 16);
    }

    @Test
    public void layout() {
        Grid grid = new Grid();

        Panel child1 = new Panel();
        child1.setPrefSize(32, 20);
        Panel child2 = new Panel();
        child2.setPrefSize(24, 10);
        Panel child3 = new Panel();
        child3.setPrefSize(16, 30);

        grid.add(child1, 0, 0);
        grid.add(child2, 1, 1);
        grid.add(child3, 2, 2);

        // Size + Size + Size [FIT/BIG/SMALL]
        grid.setColumns(new float[] {30, 20, 15});
        grid.setRows(new float[] {16, 24, 32});
        grid.onMeasure();
        assertMeasure(grid, 65, 72);
        grid.onLayout(65, 72);
        assertLayoutColumns(grid, 30, 20, 15);
        assertLayoutRows(grid, 16, 24, 32);

        grid.setPrefSize(100, 150);
        grid.onMeasure();
        assertMeasure(grid, 100, 150);
        grid.onLayout(100, 150);
        assertLayoutColumns(grid, 30, 20, 15);
        assertLayoutRows(grid, 16, 24, 32);

        grid.setPrefSize(40, 60);
        grid.onMeasure();
        assertMeasure(grid, 40, 60);
        grid.onLayout(40, 60);
        assertLayoutColumns(grid, 18.4f, 12.3f, 9.15f);
        assertLayoutRows(grid, 13.3f, 20.0f, 26.6f);

        // Size + Match + Size [BIG/SMALL]
        grid.setColumns(new float[] {30, Widget.MATCH_PARENT, 15});
        grid.setRows(new float[] {16, Widget.MATCH_PARENT, 32});
        grid.setPrefSize(0, 0);
        grid.onMeasure();
        assertMeasure(grid, Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        grid.onLayout(65, 72);
        assertLayoutColumns(grid, 30, 20, 15);
        assertLayoutRows(grid, 16, 24, 32);

        grid.setPrefSize(100, 150);
        grid.onMeasure();
        assertMeasure(grid, 100, 150);
        grid.onLayout(100, 150);
        assertLayoutColumns(grid, 30, 55, 15);
        assertLayoutRows(grid, 16, 102, 32);

        grid.setPrefSize(40, 38);
        grid.onMeasure();
        assertMeasure(grid, 40, 38);
        grid.onLayout(40, 38);
        assertLayoutColumns(grid, 26.6f, 0, 13.3f);
        assertLayoutRows(grid, 12.6f, 0, 25.3f);

        // Match + Match + Match
        grid.setColumns(new float[] {Widget.MATCH_PARENT, Widget.MATCH_PARENT, Widget.MATCH_PARENT});
        grid.setRows(new float[] {Widget.MATCH_PARENT, Widget.MATCH_PARENT, Widget.MATCH_PARENT});
        grid.setPrefSize(0, 0);
        grid.onMeasure();
        assertMeasure(grid, Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        grid.onLayout(60, 90);
        assertLayoutColumns(grid, 20, 20, 20);
        assertLayoutRows(grid, 30, 30, 30);

        // Wrap + Wrap + Wrap
        grid.setColumns(new float[] {Widget.WRAP_CONTENT, Widget.WRAP_CONTENT, Widget.WRAP_CONTENT});
        grid.setRows(new float[] {Widget.WRAP_CONTENT, Widget.WRAP_CONTENT, Widget.WRAP_CONTENT});
        grid.setPrefSize(0, 0);
        grid.onMeasure();
        assertMeasure(grid, 72, 60);
        grid.onLayout(72, 60);
        assertLayoutColumns(grid, 32, 24, 16);
        assertLayoutRows(grid, 20, 10, 30);

        grid.setPrefSize(35, 30);
        grid.onMeasure();
        assertMeasure(grid, 35, 30);
        grid.onLayout(35, 30);
        assertLayoutColumns(grid, 15.5f, 11.6f, 7.7f);
        assertLayoutRows(grid, 10, 5, 15);

        // Wrap + (Wrap + Match) + Wrap [BiG/SMALL]
        Panel child4 = new Panel();
        child4.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        grid.add(child4, 1, 1, 1, 1);

        grid.setColumns(new float[] {Widget.WRAP_CONTENT, Widget.WRAP_CONTENT, Widget.WRAP_CONTENT});
        grid.setRows(new float[] {Widget.WRAP_CONTENT, Widget.WRAP_CONTENT, Widget.WRAP_CONTENT});
        grid.setPrefSize(0, 0);
        grid.onMeasure();
        assertMeasure(grid, Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        grid.onLayout(72, 60);
        assertLayoutColumns(grid, 32, 24, 16);
        assertLayoutRows(grid, 20, 10, 30);

        grid.setPrefSize(35, 30);
        grid.onMeasure();
        assertMeasure(grid, 35, 30);
        grid.onLayout(35, 30);
        assertLayoutColumns(grid, 15.5f, 11.6f, 7.7f);
        assertLayoutRows(grid, 10, 5, 15);

        grid.setPrefSize(100, 150);
        grid.onMeasure();
        assertMeasure(grid, 100, 150);
        grid.onLayout(100, 150);
        assertLayoutColumns(grid, 32, 52, 16);
        assertLayoutRows(grid, 20, 100, 30);

        // (Wrap + Match) + (Wrap + Match) + (Wrap + Match)
        Panel child5 = new Panel();
        child5.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        grid.add(child5, 0, 0, 1, 1);
        Panel child6 = new Panel();
        child6.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        grid.add(child6, 2, 2, 1, 1);

        grid.setColumns(new float[] {Widget.WRAP_CONTENT, Widget.WRAP_CONTENT, Widget.WRAP_CONTENT});
        grid.setRows(new float[] {Widget.WRAP_CONTENT, Widget.WRAP_CONTENT, Widget.WRAP_CONTENT});
        grid.setPrefSize(0, 0);
        grid.onMeasure();
        assertMeasure(grid, Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        grid.onLayout(72, 60);
        assertLayoutColumns(grid, 32, 24, 16);
        assertLayoutRows(grid, 20, 10, 30);

        grid.setPrefSize(35, 30);
        grid.onMeasure();
        assertMeasure(grid, 35, 30);
        grid.onLayout(35, 30);
        assertLayoutColumns(grid, 15.5f, 11.6f, 7.7f);
        assertLayoutRows(grid, 10, 5, 15);

        grid.setPrefSize(100, 150);
        grid.onMeasure();
        assertMeasure(grid, 100, 150);
        grid.onLayout(100, 150);
        assertLayoutColumns(grid, 41.3f, 33.33f, 25.3f);
        assertLayoutRows(grid, 50, 40, 60);

        assertLayoutColumnsPos(grid, 0, 41.3f, 74.6f);
        assertLayoutRowsPos(grid, 0, 50, 90);

        // Spacing
        grid.setHorizontalSpacing(4);
        grid.setVerticalSpacing(6);

        grid.setPrefSize(108, 162);
        grid.onMeasure();
        assertMeasure(grid, 108, 162);
        grid.onLayout(108, 162);
        assertLayoutColumns(grid, 41.3f, 33.33f, 25.3f);
        assertLayoutRows(grid, 50, 40, 60);

        assertLayoutColumnsPos(grid, 0, 4 + 41.3f, 8 + 74.6f);
        assertLayoutRowsPos(grid, 0, 6 + 50, 12 + 90);

    }

    private void assertCell(Grid grid, Widget widget, int x, int y, int w, int h) {
        var cell = grid.getCell(widget);
        assertEquals("Cell X", x, cell.x);
        assertEquals("Cell Y", y, cell.y);
        assertEquals("Cell W", w, cell.w);
        assertEquals("Cell H", h, cell.h);
    }

    private UXChildren mockChildren(TestCell... cells) {
        UXChildren uxChild = mock(UXChildren.class);
        ArrayList<UXChild> children = new ArrayList<>();
        for (TestCell cell : cells) {
            HashMap<Integer, UXValue> attributes = new HashMap<>();
            attributes.put(UXHash.getHash("cell-x"), new UXValueNumber(cell.x));
            attributes.put(UXHash.getHash("cell-y"), new UXValueNumber(cell.y));
            attributes.put(UXHash.getHash("cell-w"), new UXValueNumber(cell.w));
            attributes.put(UXHash.getHash("cell-h"), new UXValueNumber(cell.h));
            children.add(new UXChild(cell.widget, attributes));
        }
        when(uxChild.iterator()).thenReturn(children.iterator());
        return uxChild;
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("horizontal-spacing"), new UXValueSizeDp(16));
        hash.put(UXHash.getHash("vertical-spacing"), new UXValueSizeDp(8));
        hash.put(UXHash.getHash("columns"), new UXValueSizeList(new UXValue[]{new UXValueSizeDp(10), new UXValueSizeDp(20)}));
        hash.put(UXHash.getHash("rows"), new UXValueSizeList(new UXValue[]{new UXValueSizeDp(15), new UXValueSizeDp(25)}));
        return hash;
    }

    private void assertLayoutColumns(Grid grid, float... sizes) {
        assertEquals("Columns", sizes.length, grid.getColumnCount());
        for (int i = 0; i < sizes.length; i++) {
            assertEquals("Column W " + i, sizes[i], grid.getColumnLayoutWidth(i), 0.1f);
        }
    }

    private void assertLayoutRows(Grid grid, float... sizes) {
        assertEquals("Rows", sizes.length, grid.getRowCount());
        for (int i = 0; i < sizes.length; i++) {
            assertEquals("Row H " + i, sizes[i], grid.getRowLayoutHeight(i), 0.1f);
        }
    }

    private void assertLayoutColumnsPos(Grid grid, float... sizes) {
        assertEquals("Columns", sizes.length, grid.getColumnCount());
        for (int i = 0; i < sizes.length; i++) {
            assertEquals("Column X " + i, sizes[i], grid.getColumnLayoutX(i), 0.1f);
        }
    }

    private void assertLayoutRowsPos(Grid grid, float... sizes) {
        assertEquals("Rows", sizes.length, grid.getRowCount());
        for (int i = 0; i < sizes.length; i++) {
            assertEquals("Row Y " + i, sizes[i], grid.getRowLayoutY(i), 0.1f);
        }
    }

    private void assertMeasure(Widget widget, float width, float height) {
        assertEquals("Measure Width", width, widget.getMeasureWidth(), 0.1f);
        assertEquals("Measure Height", height, widget.getMeasureHeight(), 0.1f);
    }

    private void assertLayout(Widget widget, float x, float y, float width, float height) {
        assertEquals("X", x, widget.getLayoutX(), 0.1f);
        assertEquals("Y", y, widget.getLayoutY(), 0.1f);
        assertEquals("Width", width, widget.getLayoutWidth(), 0.1f);
        assertEquals("Height", height, widget.getLayoutHeight(), 0.1f);
    }

    private static class TestCell {
        Widget widget;
        int x, y, w, h;

        public TestCell(Widget widget, int x, int y, int w, int h) {
            this.widget = widget;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}