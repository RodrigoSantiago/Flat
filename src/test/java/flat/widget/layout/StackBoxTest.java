package flat.widget.layout;

import flat.uxml.UXChild;
import flat.uxml.UXChildren;
import flat.uxml.UXHash;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueText;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class StackBoxTest {

    @Test
    public void childrenFromUx() {
        StackBox box = new StackBox();
        Widget child1 = new Widget();
        Widget child2 = new Widget();

        UXChildren uxChild = mockChildren(child1, child2);

        assertNull(child1.getParent());
        assertNull(child2.getParent());
        box.applyChildren(uxChild);
        assertEquals(child1, box.getChildrenIterable().get(0));
        assertEquals(child2, box.getChildrenIterable().get(1));
        assertEquals(box, child1.getParent());
        assertEquals(box, child2.getParent());
    }

    @Test
    public void alignStyles() {
        StackBox box = new StackBox();
        box.setAttributes(createNonDefaultValues(), "stack-box");
        box.applyStyle();

        assertEquals(HorizontalAlign.LEFT, box.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, box.getVerticalAlign());
    }

    @Test
    public void alignOneChild() {
        StackBox parent = new StackBox();
        StackBox child = new StackBox();

        parent.setPrefSize(200, 350);
        child.setPrefSize(100, 120);
        parent.add(child);

        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.BOTTOM);
        parent.onMeasure();
        assertMeasure(parent, 200, 350);
        parent.onLayout(200, 350);

        assertEquals(0, child.getLayoutX(), 0.001f);
        assertEquals(230, child.getLayoutY(), 0.001f);

        parent.setHorizontalAlign(HorizontalAlign.RIGHT);
        parent.setVerticalAlign(VerticalAlign.TOP);
        parent.onMeasure();
        assertMeasure(parent, 200, 350);
        parent.onLayout(200, 350);

        assertEquals(100, child.getLayoutX(), 0.001f);
        assertEquals(0, child.getLayoutY(), 0.001f);

        parent.setHorizontalAlign(HorizontalAlign.CENTER);
        parent.setVerticalAlign(VerticalAlign.MIDDLE);
        parent.onMeasure();
        assertMeasure(parent, 200, 350);
        parent.onLayout(200, 350);

        assertEquals(50, child.getLayoutX(), 0.001f);
        assertEquals(115, child.getLayoutY(), 0.001f);

        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);
        parent.setMargins(2, 3, 4, 5);
        parent.setPadding(3, 4, 5, 6);
        parent.onMeasure();
        assertMeasure(parent, 208, 356);
        parent.onLayout(208, 356);

        assertEquals(11, child.getLayoutX(), 0.001f);
        assertEquals(5, child.getLayoutY(), 0.001f);

        parent.setHorizontalAlign(HorizontalAlign.RIGHT);
        parent.setVerticalAlign(VerticalAlign.BOTTOM);
        parent.setMargins(2, 3, 4, 5);
        parent.setPadding(3, 4, 5, 6);
        parent.onMeasure();
        assertMeasure(parent, 208, 356);
        parent.onLayout(208, 356);

        assertEquals(101, child.getLayoutX(), 0.001f);
        assertEquals(227, child.getLayoutY(), 0.001f);
    }

    @Test
    public void alignSiblings() {
        StackBox parent = new StackBox();
        StackBox child1 = new StackBox();
        StackBox child2 = new StackBox();

        parent.setPrefSize(200, 350);
        child1.setPrefSize(100, 120);
        child2.setPrefSize(50, 60);
        parent.add(child1, child2);

        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.BOTTOM);
        parent.onMeasure();
        assertMeasure(parent, 200, 350);
        parent.onLayout(200, 350);

        assertEquals(0, child1.getLayoutX(), 0.001f);
        assertEquals(230, child1.getLayoutY(), 0.001f);

        assertEquals(0, child2.getLayoutX(), 0.001f);
        assertEquals(290, child2.getLayoutY(), 0.001f);

        parent.setHorizontalAlign(HorizontalAlign.RIGHT);
        parent.setVerticalAlign(VerticalAlign.TOP);
        parent.onMeasure();
        assertMeasure(parent, 200, 350);
        parent.onLayout(200, 350);

        assertEquals(100, child1.getLayoutX(), 0.001f);
        assertEquals(0, child1.getLayoutY(), 0.001f);
        assertEquals(150, child2.getLayoutX(), 0.001f);
        assertEquals(0, child2.getLayoutY(), 0.001f);

        parent.setHorizontalAlign(HorizontalAlign.CENTER);
        parent.setVerticalAlign(VerticalAlign.MIDDLE);
        parent.onMeasure();
        assertMeasure(parent, 200, 350);
        parent.onLayout(200, 350);

        assertEquals(50, child1.getLayoutX(), 0.001f);
        assertEquals(115, child1.getLayoutY(), 0.001f);
        assertEquals(75, child2.getLayoutX(), 0.001f);
        assertEquals(145, child2.getLayoutY(), 0.001f);

        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);
        parent.setMargins(2, 3, 4, 5);
        parent.setPadding(3, 4, 5, 6);
        parent.onMeasure();
        assertMeasure(parent, 208, 356);
        parent.onLayout(208, 356);

        assertEquals(11, child1.getLayoutX(), 0.001f);
        assertEquals(5, child1.getLayoutY(), 0.001f);
        assertEquals(11, child2.getLayoutX(), 0.001f);
        assertEquals(5, child2.getLayoutY(), 0.001f);

        parent.setHorizontalAlign(HorizontalAlign.RIGHT);
        parent.setVerticalAlign(VerticalAlign.BOTTOM);
        parent.setMargins(2, 3, 4, 5);
        parent.setPadding(3, 4, 5, 6);
        parent.onMeasure();
        assertMeasure(parent, 208, 356);
        parent.onLayout(208, 356);

        assertEquals(101, child1.getLayoutX(), 0.001f);
        assertEquals(227, child1.getLayoutY(), 0.001f);
        assertEquals(151, child2.getLayoutX(), 0.001f);
        assertEquals(287, child2.getLayoutY(), 0.001f);
    }

    @Test
    public void layoutSingleChild() {
        StackBox parent = new StackBox();
        Panel child1 = new Panel();
        Panel child2 = new Panel();
        parent.add(child1);
        parent.add(child2);

        // Child size does not affect Parent
        parent.setPrefSize(200, 400);
        child1.setPrefSize(150, 250);
        child2.setPrefSize(150, 250);
        parent.onMeasure();
        assertEquals(200, parent.getMeasureWidth(), 0.0001f);
        assertEquals(400, parent.getMeasureHeight(), 0.0001f);
        parent.onLayout(200, 400);

        assertEquals(200, parent.getLayoutWidth(), 0.0001f);
        assertEquals(400, parent.getLayoutHeight(), 0.0001f);
        assertEquals(150, child1.getLayoutWidth(), 0.0001f);
        assertEquals(250, child1.getLayoutHeight(), 0.0001f);
        assertEquals(150, child2.getLayoutWidth(), 0.0001f);
        assertEquals(250, child2.getLayoutHeight(), 0.0001f);

        child2.setPrefSize(155, 252);
        assertTrue(parent.onLayoutSingleChild(child2));

        assertEquals(200, parent.getLayoutWidth(), 0.0001f);
        assertEquals(400, parent.getLayoutHeight(), 0.0001f);
        assertEquals(150, child1.getLayoutWidth(), 0.0001f);
        assertEquals(250, child1.getLayoutHeight(), 0.0001f);
        assertEquals(155, child2.getLayoutWidth(), 0.0001f);
        assertEquals(252, child2.getLayoutHeight(), 0.0001f);
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

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();
        hash.put(UXHash.getHash("horizontal-align"), new UXValueText(HorizontalAlign.LEFT.toString()));
        hash.put(UXHash.getHash("vertical-align"), new UXValueText(VerticalAlign.TOP.toString()));
        return hash;
    }

    private UXChildren mockChildren(Widget... widgets) {
        UXChildren uxChild = mock(UXChildren.class);
        ArrayList<UXChild> children = new ArrayList<>();
        for (var widget : widgets) {
            children.add(new UXChild(widget, null));
        }
        when(uxChild.iterator()).thenReturn(children.iterator());
        return uxChild;
    }
}
