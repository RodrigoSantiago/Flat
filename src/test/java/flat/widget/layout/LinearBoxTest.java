package flat.widget.layout;

import flat.uxml.UXChildren;
import flat.uxml.UXHash;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueText;
import flat.widget.Widget;
import flat.widget.enums.Direction;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class LinearBoxTest {

    @Test
    public void childrenFromUx() {
        LinearBox box = new LinearBox();
        Widget child1 = new Widget();
        Widget child2 = new Widget();

        UXChildren uxChild = mock(UXChildren.class);
        when(uxChild.next()).thenReturn(child1).thenReturn(child2).thenReturn(null);

        assertNull(child1.getParent());
        assertNull(child2.getParent());
        box.applyChildren(uxChild);
        assertEquals(child1, box.getChildrenIterable().get(0));
        assertEquals(child2, box.getChildrenIterable().get(1));
        assertEquals(box, child1.getParent());
        assertEquals(box, child2.getParent());
    }

    @Test
    public void alignDirectionStyles() {
        LinearBox box = new LinearBox();
        box.setAttributes(createNonDefaultValues(), "stack-box");
        box.applyStyle();

        assertEquals(HorizontalAlign.LEFT, box.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, box.getVerticalAlign());
        assertEquals(Direction.VERTICAL, box.getDirection());
    }

    private void testDefaultPosSize(LinearBox parent, LinearBox child) {
        parent.setPrefSize(200, 350);
        child.setPrefSize(100, 120);
        parent.add(child);

        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.BOTTOM);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(0, child.getLayoutX(), 0.001f);
        assertEquals(230, child.getLayoutY(), 0.001f);
        assertEquals(100, child.getLayoutWidth(), 0.001f);
        assertEquals(120, child.getLayoutHeight(), 0.001f);

        parent.setHorizontalAlign(HorizontalAlign.RIGHT);
        parent.setVerticalAlign(VerticalAlign.TOP);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(100, child.getLayoutX(), 0.001f);
        assertEquals(0, child.getLayoutY(), 0.001f);
        assertEquals(100, child.getLayoutWidth(), 0.001f);
        assertEquals(120, child.getLayoutHeight(), 0.001f);

        parent.setHorizontalAlign(HorizontalAlign.CENTER);
        parent.setVerticalAlign(VerticalAlign.MIDDLE);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(50, child.getLayoutX(), 0.001f);
        assertEquals(115, child.getLayoutY(), 0.001f);
        assertEquals(100, child.getLayoutWidth(), 0.001f);
        assertEquals(120, child.getLayoutHeight(), 0.001f);

        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);
        parent.setMargins(2, 3, 4, 5);
        parent.setPadding(3, 4, 5, 6);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(11, child.getLayoutX(), 0.001f);
        assertEquals(5, child.getLayoutY(), 0.001f);
        assertEquals(100, child.getLayoutWidth(), 0.001f);
        assertEquals(120, child.getLayoutHeight(), 0.001f);

        parent.setHorizontalAlign(HorizontalAlign.RIGHT);
        parent.setVerticalAlign(VerticalAlign.BOTTOM);
        parent.setMargins(2, 3, 4, 5);
        parent.setPadding(3, 4, 5, 6);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(93, child.getLayoutX(), 0.001f);
        assertEquals(221, child.getLayoutY(), 0.001f);
        assertEquals(100, child.getLayoutWidth(), 0.001f);
        assertEquals(120, child.getLayoutHeight(), 0.001f);
    }

    @Test
    public void alignOneChildHorizontal() {
        LinearBox parent = new LinearBox();
        LinearBox child = new LinearBox();

        parent.setDirection(Direction.HORIZONTAL);
        testDefaultPosSize(parent, child);
    }

    @Test
    public void alignOneChildIverseHorizontal() {
        LinearBox parent = new LinearBox();
        LinearBox child = new LinearBox();

        parent.setDirection(Direction.IHORIZONTAL);
        testDefaultPosSize(parent, child);
    }

    @Test
    public void alignOneChildVertical() {
        LinearBox parent = new LinearBox();
        LinearBox child = new LinearBox();

        parent.setDirection(Direction.VERTICAL);
        testDefaultPosSize(parent, child);
    }

    @Test
    public void alignOneChildIverseVertical() {
        LinearBox parent = new LinearBox();
        LinearBox child = new LinearBox();

        parent.setDirection(Direction.IVERTICAL);
        testDefaultPosSize(parent, child);
    }

    @Test
    public void alignMultipleChildIverseHorizontal() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();

        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);
        parent.add(child1, child2);
        parent.setDirection(Direction.IHORIZONTAL);

        parent.setPrefSize(200, 350);
        child1.setPrefSize(100, 120);
        child2.setPrefSize(80, 110);

        parent.onMeasure();
        assertMeasure(parent, 200, 350);
        parent.onLayout(200, 350);

        assertLayout(child1, 80, 0, 100, 120);
        assertLayout(child2, 0, 0, 80, 110);
    }

    @Test
    public void alignMultipleChildIverseVertical() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();

        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);
        parent.add(child1, child2);
        parent.setDirection(Direction.IVERTICAL);

        parent.setPrefSize(350, 200);
        child1.setPrefSize(120, 100);
        child2.setPrefSize(110, 80);

        parent.onMeasure();
        assertMeasure(parent, 350, 200);
        parent.onLayout(350, 200);

        assertLayout(child1, 0, 80, 120, 100);
        assertLayout(child2, 0, 0, 110, 80);
    }

    @Test
    public void alignWithMarginPaddingHorizontal() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();

        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);
        parent.add(child1, child2);
        parent.setDirection(Direction.HORIZONTAL);

        parent.setPrefSize(214, 362);
        parent.setMargins(1, 2, 3, 4);
        parent.setPadding(5, 6, 7, 8);
        child1.setPrefSize(60, 120);
        child2.setPrefSize(80, 110);

        parent.onMeasure();
        assertMeasure(parent, 220, 366);
        parent.onLayout(220, 366);

        assertLayout(child1, 12, 6, 60, 120);
        assertLayout(child2, 72, 6, 80, 110);

        parent.setHorizontalAlign(HorizontalAlign.RIGHT);
        parent.setVerticalAlign(VerticalAlign.BOTTOM);
        parent.onMeasure();
        assertMeasure(parent, 220, 366);
        parent.onLayout(220, 366);

        assertLayout(child1, 72, 236, 60, 120);
        assertLayout(child2, 132, 246, 80, 110);

        parent.setHorizontalAlign(HorizontalAlign.CENTER);
        parent.setVerticalAlign(VerticalAlign.MIDDLE);
        parent.onMeasure();
        assertMeasure(parent, 220, 366);
        parent.onLayout(220, 366);

        assertLayout(child1, 42, 121, 60, 120);
        assertLayout(child2, 102, 126, 80, 110);
    }

    @Test
    public void alignWithMarginPaddingVertical() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();

        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);
        parent.add(child1, child2);
        parent.setDirection(Direction.VERTICAL);

        parent.setPrefSize(214, 362);
        parent.setMargins(1, 2, 3, 4);
        parent.setPadding(5, 6, 7, 8);
        child1.setPrefSize(60, 120);
        child2.setPrefSize(80, 110);

        parent.onMeasure();
        assertMeasure(parent, 220, 366);
        parent.onLayout(220, 366);

        assertLayout(child1, 12, 6, 60, 120);
        assertLayout(child2, 12, 126, 80, 110);

        parent.setHorizontalAlign(HorizontalAlign.RIGHT);
        parent.setVerticalAlign(VerticalAlign.BOTTOM);
        parent.onMeasure();
        assertMeasure(parent, 220, 366);
        parent.onLayout(220, 366);

        assertLayout(child1, 152, 126, 60, 120);
        assertLayout(child2, 132, 246, 80, 110);

        parent.setHorizontalAlign(HorizontalAlign.CENTER);
        parent.setVerticalAlign(VerticalAlign.MIDDLE);
        parent.onMeasure();
        assertMeasure(parent, 220, 366);
        parent.onLayout(220, 366);

        assertLayout(child1, 82, 66, 60, 120);
        assertLayout(child2, 72, 186, 80, 110);
    }

    @Test
    public void measureWithMaxSizeHorizontal() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();
        parent.add(child1, child2);

        parent.setDirection(Direction.HORIZONTAL);
        parent.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        child1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child1.setMaxSize(100, 50);
        child2.setMaxSize(200, 75);
        parent.onMeasure();
        assertMeasure(parent, 300, 75);
    }

    @Test
    public void measureWithMaxSizeVertical() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();
        parent.add(child1, child2);

        parent.setDirection(Direction.VERTICAL);
        parent.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        child1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child1.setMaxSize(50, 100);
        child2.setMaxSize(75, 200);
        parent.onMeasure();
        assertMeasure(parent, 75, 300);
    }

    @Test
    public void measureOverflowHorizontal() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();
        LinearBox child3 = new LinearBox();
        parent.add(child1, child2, child3);

        parent.setDirection(Direction.HORIZONTAL);
        child1.setPrefSize(100, 50);
        child2.setPrefSize(200, 75);
        child3.setPrefSize(300, 95);
        parent.onMeasure();
        assertMeasure(parent, 600, 95);
    }

    @Test
    public void measureOverflowVertical() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();
        LinearBox child3 = new LinearBox();
        parent.add(child1, child2, child3);

        parent.setDirection(Direction.VERTICAL);
        child1.setPrefSize(50, 100);
        child2.setPrefSize(75, 200);
        child3.setPrefSize(95, 300);
        parent.onMeasure();
        assertMeasure(parent, 95, 600);
    }

    @Test
    public void addRemove() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();
        LinearBox child3 = new LinearBox();
        parent.add(child1, child2, child3);

        parent.setDirection(Direction.HORIZONTAL);
        child1.setPrefSize(100, 50);
        child2.setPrefSize(200, 75);
        child3.setPrefSize(300, 95);
        parent.onMeasure();
        assertMeasure(parent, 600, 95);

        parent.remove(child2);
        parent.add(child2);
        parent.onMeasure();
        assertMeasure(parent, 600, 95);
    }

    @Test
    public void layoutTwoChildrenHorizontal() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();
        parent.add(child1, child2);

        parent.setDirection(Direction.HORIZONTAL);
        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);
        // Both defined size
        parent.setPrefSize(400, 200);
        child1.setPrefSize(100, 120);
        child2.setPrefSize(140, 160);
        parent.onMeasure();
        assertMeasure(parent, 400, 200);
        parent.onLayout(400, 200);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 100, 0, 140, 160);

        // Both defined with minimun size (bigger parent)
        parent.setPrefSize(400, 200);
        child1.setPrefSize(100, 120);
        child1.setMinSize(50, 60);
        child2.setPrefSize(140, 160);
        child2.setMinSize(70, 80);
        parent.onMeasure();
        assertMeasure(parent, 400, 200);
        parent.onLayout(400, 200);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 100, 0, 140, 160);

        // Both defined with minimun size (small parent)
        parent.setPrefSize(100, 50);
        child1.setPrefSize(100, 120);
        child1.setMinSize(50, 60);
        child2.setPrefSize(140, 160);
        child2.setMinSize(70, 80);
        parent.onMeasure();
        assertMeasure(parent, 100, 50);
        parent.onLayout(100, 50);

        assertLayout(child1, 0, 0, 41.6f, 50);
        assertLayout(child2, 41.6f, 0, 58.3f, 50);
        child1.setMinSize(0, 0);
        child2.setMinSize(0, 0);

        // Both defined with maximum size smaller than defined
        parent.setPrefSize(400, 200);
        child1.setPrefSize(100, 120);
        child1.setMaxSize(50, 60);
        child2.setPrefSize(140, 160);
        child2.setMaxSize(70, 80);
        parent.onMeasure();
        assertMeasure(parent, 400, 200);
        parent.onLayout(400, 200);

        assertLayout(child1, 0, 0, 50, 60);
        assertLayout(child2, 50, 0, 70, 80);
        child1.setMaxSize(0, 0);
        child2.setMaxSize(0, 0);

        // One defined, One Match Parent (bigger parent)
        parent.setPrefSize(400, 200);
        child1.setPrefSize(100, 120);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.onMeasure();
        assertMeasure(parent, 400, 200);
        parent.onLayout(400, 200);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 100, 0, 300, 200);

        // One defined, One Match Parent (parent smaller than defined)
        parent.setPrefSize(90, 110);
        child1.setPrefSize(100, 120);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.onMeasure();
        assertMeasure(parent, 90, 110);
        parent.onLayout(90, 110);

        assertLayout(child1, 0, 0, 90, 110);
        assertLayout(child2, 90, 0, 0, 110);

        // One defined + min, One Match Parent (bigger parent)
        parent.setPrefSize(400, 200);
        child1.setPrefSize(100, 120);
        child1.setMinSize(50, 60);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.onMeasure();
        assertMeasure(parent, 400, 200);
        parent.onLayout(400, 200);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 100, 0, 300, 200);

        // One defined + min, One Match Parent (Parent size bigger than min, smaller than defined)
        parent.setPrefSize(75, 110);
        child1.setPrefSize(100, 120);
        child1.setMinSize(50, 60);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.onMeasure();
        assertMeasure(parent, 75, 110);
        parent.onLayout(75, 110);

        assertLayout(child1, 0, 0, 75, 110);
        assertLayout(child2, 75, 0, 0, 110);

        // One defined + min, One Match Parent (Parent size smalelr than min)
        parent.setPrefSize(45, 55);
        child1.setPrefSize(100, 120);
        child1.setMinSize(50, 60);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.onMeasure();
        assertMeasure(parent, 45, 55);
        parent.onLayout(45, 55);

        assertLayout(child1, 0, 0, 45, 55);
        assertLayout(child2, 45, 0, 0, 55);

        // One defined + min, One Match Parent + min (bigger parent)
        parent.setPrefSize(400, 200);
        child1.setPrefSize(100, 120);
        child1.setMinSize(50, 60);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setMinSize(70, 80);
        parent.onMeasure();
        assertMeasure(parent, 400, 200);
        parent.onLayout(400, 200);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 100, 0, 300, 200);

        // One defined + min, One Match Parent + min (Parent size bigger than total min, smaller than defined)
        parent.setPrefSize(150, 160);
        child1.setPrefSize(100, 120);
        child1.setMinSize(50, 60);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setMinSize(70, 80);
        parent.onMeasure();
        assertMeasure(parent, 150, 160);
        parent.onLayout(150, 160);

        assertLayout(child1, 0, 0, 80, 120);
        assertLayout(child2, 80, 0, 70, 160);
        child1.setMinSize(0, 0);
        child2.setMinSize(0, 0);

        // One defined, One Match Parent + max (bigger parent)
        parent.setPrefSize(400, 200);
        child1.setPrefSize(100, 120);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setMaxSize(200, 180);
        parent.onMeasure();
        assertMeasure(parent, 400, 200);
        parent.onLayout(400, 200);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 100, 0, 200, 180);

        // One defined, One Match Parent + max (parent smaller than defined)
        parent.setPrefSize(80, 110);
        child1.setPrefSize(100, 120);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setMaxSize(200, 180);
        parent.onMeasure();
        assertMeasure(parent, 80, 110);
        parent.onLayout(80, 110);

        assertLayout(child1, 0, 0, 80, 110);
        assertLayout(child2, 80, 0, 0, 110);
    }

    @Test
    public void layoutTwoChildrenVertical() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();
        parent.add(child1, child2);

        parent.setDirection(Direction.VERTICAL);
        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);
        // Both defined size
        parent.setPrefSize(400, 300);
        child1.setPrefSize(100, 120);
        child2.setPrefSize(140, 160);
        parent.onMeasure();
        assertMeasure(parent, 400, 300);
        parent.onLayout(400, 300);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 0, 120, 140, 160);

        // Both defined with minimun size (bigger parent)
        parent.setPrefSize(400, 300);
        child1.setPrefSize(100, 120);
        child1.setMinSize(50, 60);
        child2.setPrefSize(140, 160);
        child2.setMinSize(70, 80);
        parent.onMeasure();
        assertMeasure(parent, 400, 300);
        parent.onLayout(400, 300);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 0, 120, 140, 160);

        // Both defined with minimun size (small parent)
        parent.setPrefSize(100, 110);
        child1.setPrefSize(100, 120);
        child1.setMinSize(50, 60);
        child2.setPrefSize(140, 160);
        child2.setMinSize(70, 80);
        parent.onMeasure();
        assertMeasure(parent, 100, 110);
        parent.onLayout(100, 110);

        assertLayout(child1, 0, 0, 100, 47.1f);
        assertLayout(child2, 0, 47.1f, 100, 62.85f);
        child1.setMinSize(0, 0);
        child2.setMinSize(0, 0);

        // Both defined with maximum size smaller than defined
        parent.setPrefSize(400, 300);
        child1.setPrefSize(100, 120);
        child1.setMaxSize(50, 60);
        child2.setPrefSize(140, 160);
        child2.setMaxSize(70, 80);
        parent.onMeasure();
        assertMeasure(parent, 400, 300);
        parent.onLayout(400, 300);

        assertLayout(child1, 0, 0, 50, 60);
        assertLayout(child2, 0, 60, 70, 80);
        child1.setMaxSize(0, 0);
        child2.setMaxSize(0, 0);

        // One defined, One Match Parent (bigger parent)
        parent.setPrefSize(400, 300);
        child1.setPrefSize(100, 120);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.onMeasure();
        assertMeasure(parent, 400, 300);
        parent.onLayout(400, 300);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 0, 120, 400, 180);

        // One defined, One Match Parent (parent smaller than defined)
        parent.setPrefSize(90, 110);
        child1.setPrefSize(100, 120);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.onMeasure();
        assertMeasure(parent, 90, 110);
        parent.onLayout(90, 110);

        assertLayout(child1, 0, 0, 90, 110);
        assertLayout(child2, 0, 110, 90, 0);

        // One defined + min, One Match Parent (bigger parent)
        parent.setPrefSize(400, 300);
        child1.setPrefSize(100, 120);
        child1.setMinSize(50, 60);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.onMeasure();
        assertMeasure(parent, 400, 300);
        parent.onLayout(400, 300);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 0, 120, 400, 180);

        // One defined + min, One Match Parent (Parent size bigger than min, smaller than defined)
        parent.setPrefSize(75, 110);
        child1.setPrefSize(100, 120);
        child1.setMinSize(50, 60);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.onMeasure();
        assertMeasure(parent, 75, 110);
        parent.onLayout(75, 110);

        assertLayout(child1, 0, 0, 75, 110);
        assertLayout(child2, 0, 110, 75, 0);

        // One defined + min, One Match Parent (Parent size smaller than min)
        parent.setPrefSize(45, 55);
        child1.setPrefSize(100, 120);
        child1.setMinSize(50, 60);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.onMeasure();
        assertMeasure(parent, 45, 55);
        parent.onLayout(45, 55);

        assertLayout(child1, 0, 0, 45, 55);
        assertLayout(child2, 0, 55, 45, 0);

        // One defined + min, One Match Parent + min (bigger parent)
        parent.setPrefSize(400, 300);
        child1.setPrefSize(100, 120);
        child1.setMinSize(50, 60);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setMinSize(70, 80);
        parent.onMeasure();
        assertMeasure(parent, 400, 300);
        parent.onLayout(400, 300);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 0, 120, 400, 180);

        // One defined + min, One Match Parent + min (Parent size bigger than total min, smaller than defined)
        parent.setPrefSize(150, 170);
        child1.setPrefSize(100, 200);
        child1.setMinSize(50, 60);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setMinSize(70, 80);
        parent.onMeasure();
        assertMeasure(parent, 150, 170);
        parent.onLayout(150, 170);

        assertLayout(child1, 0, 0, 100, 90);
        assertLayout(child2, 0, 90, 150, 80);
        child1.setMinSize(0, 0);
        child2.setMinSize(0, 0);

        // One defined, One Match Parent + max (bigger parent)
        parent.setPrefSize(400, 300);
        child1.setPrefSize(100, 120);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setMaxSize(200, 80);
        parent.onMeasure();
        assertMeasure(parent, 400, 300);
        parent.onLayout(400, 300);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 0, 120, 200, 80);

        // One defined, One Match Parent + max (parent smaller than defined)
        parent.setPrefSize(80, 110);
        child1.setPrefSize(100, 120);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setMaxSize(200, 80);
        parent.onMeasure();
        assertMeasure(parent, 80, 110);
        parent.onLayout(80, 110);

        assertLayout(child1, 0, 0, 80, 110);
        assertLayout(child2, 0, 110, 80, 0);
    }

    @Test
    public void layoutMultipleChildrenHorizontal() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox(); // defined
        LinearBox child2 = new LinearBox(); // MP + min (W = 1)
        LinearBox child3 = new LinearBox(); // defined
        LinearBox child4 = new LinearBox(); // MP + max (W = 1)
        parent.add(child1, child2, child3, child4);

        parent.setDirection(Direction.HORIZONTAL);
        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);

        child1.setPrefSize(100, 120);
        child1.setMinSize(45, 60);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setMinSize(60, 70);
        child3.setPrefSize(140, 160);
        child3.setMinSize(70, 80);
        child4.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child4.setMaxSize(180, 250);

        // Parent Bigger tham everthing
        parent.setPrefSize(700, 400);
        parent.onMeasure();
        assertMeasure(parent, 700, 400);
        parent.onLayout(700, 400);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 100, 0, 280, 400);
        assertLayout(child3, 380, 0, 140, 160);
        assertLayout(child4, 520, 0, 180, 250);

        // Parent bigger than all, Smaller than child4 Max
        parent.setPrefSize(500, 400);
        parent.onMeasure();
        assertMeasure(parent, 500, 400);
        parent.onLayout(500, 400);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 100, 0, 160, 400);
        assertLayout(child3, 260, 0, 140, 160);
        assertLayout(child4, 400, 0, 100, 250);

        // Parent Smaller than defined
        parent.setPrefSize(280, 400);
        parent.onMeasure();
        assertMeasure(parent, 280, 400);
        parent.onLayout(280, 400);

        assertLayout(child1, 0, 0, 91.2f, 120);      // 45 /  55 > 45 / 46.2 = 91.2
        assertLayout(child2, 91.2f, 0, 60, 400);     // 60 /   0 > 60 /  0   = 60
        assertLayout(child3, 151.2f, 0, 128.8f, 160);// 70 /  70 > 70 / 58.8 = 128.8
        assertLayout(child4, 280, 0, 0, 250);        // [min = 175] [total def = 125 over 105(280 - 175)]

        // Parent Smaller than minimum
        parent.setPrefSize(150, 400);
        parent.onMeasure();
        assertMeasure(parent, 150, 400);
        parent.onLayout(150, 400);

        assertLayout(child1, 0, 0, 38.6f, 120);      // 45 > 38.57
        assertLayout(child2, 38.6f, 0, 51.4f, 400);  // 60 > 51.42
        assertLayout(child3, 90, 0, 60, 160);        // 70 > 60
        assertLayout(child4, 150, 0, 0, 250);        // [min = 175] [total def = 125 over 105(280 - 175)]

        // Parent Bigger tham everthing, (all MP have max size)
        child2.setMaxSize(200, 100);
        parent.setPrefSize(700, 400);
        parent.onMeasure();
        assertMeasure(parent, 700, 400);
        parent.onLayout(700, 400);

        assertLayout(child1, 0, 0, 100, 120);
        assertLayout(child2, 100, 0, 200, 100);
        assertLayout(child3, 300, 0, 140, 160);
        assertLayout(child4, 440, 0, 180, 250);
    }

    @Test
    public void layoutMultipleChildrenVertical() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox(); // defined
        LinearBox child2 = new LinearBox(); // MP + min (W = 1)
        LinearBox child3 = new LinearBox(); // defined
        LinearBox child4 = new LinearBox(); // MP + max (W = 1)
        parent.add(child1, child2, child3, child4);

        parent.setDirection(Direction.VERTICAL);
        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);

        child1.setPrefSize(120, 100);
        child1.setMinSize(60, 45);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setMinSize(70, 60);
        child3.setPrefSize(160, 140);
        child3.setMinSize(80, 70);
        child4.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child4.setMaxSize(250, 180);

        // Parent Bigger tham everthing
        parent.setPrefSize(400, 700);
        parent.onMeasure();
        assertMeasure(parent, 400, 700);
        parent.onLayout(400, 700);

        assertLayout(child1, 0,   0, 120, 100);
        assertLayout(child2, 0, 100, 400, 280);
        assertLayout(child3, 0, 380, 160, 140);
        assertLayout(child4, 0, 520, 250, 180);

        // Parent bigger than all, Smaller than child4 Max
        parent.setPrefSize(400, 500);
        parent.onMeasure();
        assertMeasure(parent, 400, 500);
        parent.onLayout(400, 500);

        assertLayout(child1, 0,   0, 120, 100);
        assertLayout(child2, 0, 100, 400, 160);
        assertLayout(child3, 0, 260, 160, 140);
        assertLayout(child4, 0, 400, 250, 100);

        // Parent Smaller than defined
        parent.setPrefSize(400, 280);
        parent.onMeasure();
        assertMeasure(parent, 400, 280);
        parent.onLayout(400, 280);

        assertLayout(child1, 0,      0, 120,  91.2f);
        assertLayout(child2, 0,  91.2f, 400,     60);
        assertLayout(child3, 0, 151.2f, 160, 128.8f);
        assertLayout(child4, 0,    280, 250,      0);

        // Parent Smaller than minimum
        parent.setPrefSize(400, 150);
        parent.onMeasure();
        assertMeasure(parent, 400, 150);
        parent.onLayout(400, 150);

        assertLayout(child1, 0,     0, 120, 38.6f);
        assertLayout(child2, 0, 38.6f, 400, 51.4f);
        assertLayout(child3, 0,    90, 160,    60);
        assertLayout(child4, 0,   150, 250,     0);

        // Parent Bigger tham everthing, (all MP have max size)
        child2.setMaxSize(100, 200);
        parent.setPrefSize(400, 700);
        parent.onMeasure();
        assertMeasure(parent, 400, 700);
        parent.onLayout(400, 700);

        assertLayout(child1, 0,   0, 120, 100);
        assertLayout(child2, 0, 100, 100, 200);
        assertLayout(child3, 0, 300, 160, 140);
        assertLayout(child4, 0, 440, 250, 180);
    }

    @Test
    public void layoutMultipleWeightHorizontal() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();
        parent.add(child1, child2);

        parent.setDirection(Direction.HORIZONTAL);
        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);

        parent.setPrefSize(400, 700);
        child1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child1.setWeight(1);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setWeight(2);

        parent.onMeasure();
        assertMeasure(parent, 400, 700);
        parent.onLayout(400, 700);

        assertLayout(child1, 0, 0, 133.33f, 700);
        assertLayout(child2, 133.33f, 0, 266.66f, 700);

        parent.setPrefSize(400, 700);
        child1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child1.setWeight(0);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setWeight(0);

        parent.onMeasure();
        assertMeasure(parent, 400, 700);
        parent.onLayout(400, 700);

        assertLayout(child1, 0, 0, 200, 700);
        assertLayout(child2, 200, 0, 200, 700);
    }

    @Test
    public void layoutMultipleWeighVertical() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();
        parent.add(child1, child2);

        parent.setDirection(Direction.VERTICAL);
        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);

        parent.setPrefSize(700, 400);
        child1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child1.setWeight(1);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setWeight(2);

        parent.onMeasure();
        assertMeasure(parent, 700, 400);
        parent.onLayout(700, 400);

        assertLayout(child1, 0, 0, 700, 133.33f);
        assertLayout(child2, 0, 133.33f, 700, 266.66f);

        parent.setPrefSize(700, 400);
        child1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child1.setWeight(0);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setWeight(0);

        parent.onMeasure();
        assertMeasure(parent, 700, 400);
        parent.onLayout(700, 400);

        assertLayout(child1, 0, 0, 700, 200);
        assertLayout(child2, 0, 200, 700, 200);
    }

    @Test
    public void layoutIterationMaxSizeHorizontal() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();
        LinearBox child3 = new LinearBox();
        parent.add(child1, child2, child3);

        parent.setDirection(Direction.HORIZONTAL);
        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);

        child1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child1.setMaxSize(60, 70);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setMaxSize(100, 110);
        child3.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child3.setMaxSize(80, 90);

        // Parent Bigger than all
        parent.setPrefSize(260, 300);
        parent.onMeasure();
        assertMeasure(parent, 260, 300);
        parent.onLayout(260, 300);

        assertLayout(child1,   0, 0,  60, 70);
        assertLayout(child2,  60, 0, 100, 110);
        assertLayout(child3, 160, 0,  80, 90);

        // Parent Bigger than M1 max, less than M2 + M3
        parent.setPrefSize(200, 300);
        parent.onMeasure();
        assertMeasure(parent, 200, 300);
        parent.onLayout(200, 300);

        assertLayout(child1,   0, 0, 60, 70);
        assertLayout(child2,  60, 0, 70, 110);
        assertLayout(child3, 130, 0, 70, 90);

        // Parent Bigger than M1, M3 max, less than M2
        parent.setPrefSize(230, 300);
        parent.onMeasure();
        assertMeasure(parent, 230, 300);
        parent.onLayout(230, 300);

        assertLayout(child1,   0, 0, 60, 70);
        assertLayout(child2,  60, 0, 90, 110);
        assertLayout(child3, 150, 0, 80, 90);
    }

    @Test
    public void layoutIterationMaxSizeVertical() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        LinearBox child2 = new LinearBox();
        LinearBox child3 = new LinearBox();
        parent.add(child1, child2, child3);

        parent.setDirection(Direction.VERTICAL);
        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);

        child1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child1.setMaxSize(70, 60);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setMaxSize(110, 100);
        child3.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child3.setMaxSize(90, 80);

        // Parent Bigger than all
        parent.setPrefSize(300, 260);
        parent.onMeasure();
        assertMeasure(parent, 300, 260);
        parent.onLayout(300, 260);

        assertLayout(child1, 0,   0, 70 ,  60);
        assertLayout(child2, 0,  60, 110, 100);
        assertLayout(child3, 0, 160, 90 ,  80);

        // Parent Bigger than M1 max, less than M2 + M3
        parent.setPrefSize(300, 200);
        parent.onMeasure();
        assertMeasure(parent, 300, 200);
        parent.onLayout(300, 200);

        assertLayout(child1, 0,   0, 70 , 60);
        assertLayout(child2, 0,  60, 110, 70);
        assertLayout(child3, 0, 130, 90 , 70);

        // Parent Bigger than M1, M3 max, less than M2
        parent.setPrefSize(300, 230);
        parent.onMeasure();
        assertMeasure(parent, 300, 230);
        parent.onLayout(300, 230);

        assertLayout(child1, 0,   0, 70 , 60);
        assertLayout(child2, 0,  60, 110, 90);
        assertLayout(child3, 0, 150, 90 , 80);
    }

    @Test
    public void layoutChildrenMaxAndMarginsHorizontal() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        parent.add(child1);

        parent.setDirection(Direction.HORIZONTAL);
        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);
        // Both defined size
        parent.setPrefSize(400, 200);
        child1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child1.setMaxSize(100, 120);
        child1.setMargins(1, 2, 3, 4);
        parent.onMeasure();
        assertMeasure(parent, 400, 200);
        parent.onLayout(400, 200);

        assertLayout(child1, 0, 0, 106, 124);
    }

    @Test
    public void layoutChildrenMaxAndMarginsVertical() {
        LinearBox parent = new LinearBox();
        LinearBox child1 = new LinearBox();
        parent.add(child1);

        parent.setDirection(Direction.VERTICAL);
        parent.setHorizontalAlign(HorizontalAlign.LEFT);
        parent.setVerticalAlign(VerticalAlign.TOP);
        // Both defined size
        parent.setPrefSize(400, 200);
        child1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child1.setMaxSize(100, 120);
        child1.setMargins(1, 2, 3, 4);
        parent.onMeasure();
        assertMeasure(parent, 400, 200);
        parent.onLayout(400, 200);

        assertLayout(child1, 0, 0, 106, 124);
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
        hash.put(UXHash.getHash("direction"), new UXValueText(Direction.VERTICAL.toString()));
        return hash;
    }
}
