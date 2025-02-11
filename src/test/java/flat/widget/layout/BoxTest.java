package flat.widget.layout;

import flat.uxml.UXChildren;
import flat.widget.Widget;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class BoxTest {

    @Test
    public void childrenFromUx() {
        Box box = new Box();
        Widget child = new Widget();

        UXChildren uxChild = mock(UXChildren.class);
        when(uxChild.next()).thenReturn(child).thenReturn(null);

        assertNull(child.getParent());
        box.applyChildren(uxChild);
        assertEquals(child, box.getChildrenIterable().get(0));
        assertEquals(box, child.getParent());
    }

    @Test
    public void siblingsFromUx() {
        Box box = new Box();
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
    public void siblingsFromAdding() {
        Box box = new Box();
        Widget child1 = new Widget();
        Widget child2 = new Widget();


        assertNull(child1.getParent());
        assertNull(child2.getParent());

        box.add(child1, child2);

        assertEquals(child1, box.getChildrenIterable().get(0));
        assertEquals(child2, box.getChildrenIterable().get(1));
        assertEquals(box, child1.getParent());
        assertEquals(box, child2.getParent());
    }

    @Test
    public void swapParentsFromAdding() {
        Box box1 = new Box();
        Box box2 = new Box();
        Widget child = new Widget();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(child.getParent());
        box1.add(child);

        assertEquals(child, box1.getChildrenIterable().get(0));
        assertEquals(0, box2.getChildrenIterable().size());
        assertEquals(box1, child.getParent());

        box2.add(child);
        assertEquals(child, box2.getChildrenIterable().get(0));
        assertEquals(0, box1.getChildrenIterable().size());
        assertEquals(box2, child.getParent());
    }

    @Test
    public void addTwice() {
        Box box = new Box();
        Widget child = new Widget();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(child.getParent());
        box.add(child);

        assertEquals(child, box.getChildrenIterable().get(0));
        assertEquals(1, box.getChildrenIterable().size());
        assertEquals(box, child.getParent());

        box.add(child);
        assertEquals(child, box.getChildrenIterable().get(0));
        assertEquals(1, box.getChildrenIterable().size());
        assertEquals(box, child.getParent());
    }

    @Test
    public void addAndRemove() {
        Box box = new Box();
        Widget child = new Widget();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(child.getParent());
        box.add(child);

        assertEquals(child, box.getChildrenIterable().get(0));
        assertEquals(1, box.getChildrenIterable().size());
        assertEquals(box, child.getParent());

        box.remove(child);
        assertEquals(0, box.getChildrenIterable().size());
        assertNull(child.getParent());
    }

    @Test
    public void addAndRemoveSibling() {
        Box box = new Box();
        Widget child1 = new Widget();
        Widget child2 = new Widget();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(child1.getParent());
        assertNull(child2.getParent());
        box.add(child1);
        box.add(child2);

        assertEquals(child1, box.getChildrenIterable().get(0));
        assertEquals(child2, box.getChildrenIterable().get(1));
        assertEquals(2, box.getChildrenIterable().size());
        assertEquals(box, child1.getParent());
        assertEquals(box, child2.getParent());

        box.remove(child1);
        assertEquals(child2, box.getChildrenIterable().get(0));
        assertEquals(1, box.getChildrenIterable().size());
        assertNull(child1.getParent());
        assertEquals(box, child2.getParent());
    }

    @Test
    public void siblingsOrderFromElevation() {
        Box box = new Box();
        Widget child1 = new Widget();
        Widget child2 = new Widget();

        assertNull(child1.getParent());
        assertNull(child2.getParent());

        box.add(child1, child2);

        assertEquals(child1, box.getChildrenIterable().get(0));
        assertEquals(child2, box.getChildrenIterable().get(1));
        assertEquals(box, child1.getParent());
        assertEquals(box, child2.getParent());

        child1.setElevation(2);
        child2.setElevation(0);

        assertEquals(child2, box.getChildrenIterable().get(0));
        assertEquals(child1, box.getChildrenIterable().get(1));
        assertEquals(box, child1.getParent());
        assertEquals(box, child2.getParent());
    }

    @Test
    public void addItself() {
        Box box = new Box();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(box.getParent());

        box.add(box);

        assertNull(box.getParent());
        assertEquals(0, box.getChildrenIterable().size());
    }

    @Test
    public void addItsParent() {
        Box box = new Box();
        Box parent = new Box();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(box.getParent());
        assertNull(parent.getParent());

        parent.add(box);

        assertEquals(parent, box.getParent());
        assertNull(parent.getParent());

        assertEquals(0, box.getChildrenIterable().size());
        assertEquals(1, parent.getChildrenIterable().size());

        box.add(parent);

        assertEquals(parent, box.getParent());
        assertNull(parent.getParent());

        assertEquals(0, box.getChildrenIterable().size());
        assertEquals(1, parent.getChildrenIterable().size());
    }

    @Test
    public void measureBox() {
        Box box = new Box();

        box.setPrefSize(150, 100);
        box.onMeasure();

        assertEquals(150f, box.getMeasureWidth(), 0.0001f);
        assertEquals(100f, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        box.onMeasure();

        assertEquals(Widget.WRAP_CONTENT, box.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.WRAP_CONTENT, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        box.onMeasure();

        assertEquals(Widget.MATCH_PARENT, box.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, box.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureBoxWithMin() {
        // Min should affect measure values
        Box box = new Box();

        box.setPrefSize(150, 100);
        box.setMinSize(200, 250);
        box.onMeasure();

        assertEquals(200, box.getMeasureWidth(), 0.0001f);
        assertEquals(250, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        box.onMeasure();

        assertEquals(200, box.getMeasureWidth(), 0.0001f);
        assertEquals(250, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        box.onMeasure();

        assertEquals(Widget.MATCH_PARENT, box.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, box.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureBoxWithMinAndPadding() {
        Box box = new Box();

        box.setPrefSize(150, 100);
        box.setPadding(10, 10, 10, 10);
        box.onMeasure();

        assertEquals(150, box.getMeasureWidth(), 0.0001f);
        assertEquals(100, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(150, 100);
        box.setMinSize(160, 110);
        box.setPadding(10, 10, 10, 10);
        box.onMeasure();

        assertEquals(160, box.getMeasureWidth(), 0.0001f);
        assertEquals(110, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(150, 100);
        box.setMinSize(180, 130);
        box.setPadding(10, 10, 10, 10);
        box.onMeasure();

        assertEquals(180, box.getMeasureWidth(), 0.0001f);
        assertEquals(130, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        box.setMinSize(160, 110);
        box.setPadding(10, 10, 10, 10);
        box.onMeasure();

        assertEquals(160, box.getMeasureWidth(), 0.0001f);
        assertEquals(110, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        box.setMinSize(160, 110);
        box.setPadding(10, 10, 10, 10);
        box.onMeasure();

        assertEquals(Widget.MATCH_PARENT, box.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, box.getMeasureHeight(), 0.0001f);

        Box child = new Box();
        box.add(child);

        box.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        box.setMinSize(0, 0);
        box.setPadding(1, 2, 3, 4);
        child.setPrefSize(150, 100);
        box.onMeasure();

        assertEquals(156, box.getMeasureWidth(), 0.0001f);
        assertEquals(104, box.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureBoxWithMinAndMargin() {
        Box box = new Box();

        box.setPrefSize(150, 100);
        box.setMargins(10, 10, 10, 10);
        box.onMeasure();

        assertEquals(170, box.getMeasureWidth(), 0.0001f);
        assertEquals(120, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(150, 100);
        box.setMinSize(160, 110);
        box.setMargins(10, 10, 10, 10);
        box.onMeasure();

        assertEquals(180, box.getMeasureWidth(), 0.0001f);
        assertEquals(130, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(150, 100);
        box.setMinSize(180, 130);
        box.setMargins(10, 10, 10, 10);
        box.onMeasure();

        assertEquals(200, box.getMeasureWidth(), 0.0001f);
        assertEquals(150, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        box.setMinSize(160, 110);
        box.setMargins(10, 10, 10, 10);
        box.onMeasure();

        assertEquals(180, box.getMeasureWidth(), 0.0001f);
        assertEquals(130, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        box.setMinSize(160, 110);
        box.setMargins(10, 10, 10, 10);
        box.onMeasure();

        assertEquals(Widget.MATCH_PARENT, box.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, box.getMeasureHeight(), 0.0001f);

        Box child = new Box();
        box.add(child);

        box.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        box.setMinSize(0, 0);
        box.setMargins(1, 2, 3, 4);
        child.setPrefSize(150, 100);
        box.onMeasure();

        assertEquals(156, box.getMeasureWidth(), 0.0001f);
        assertEquals(104, box.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureBoxWithMax() {
        // Max should not affect measure values
        Box box = new Box();
        box.setMaxSize(150, 100);
        box.setPrefSize(200, 250);
        box.onMeasure();

        assertEquals(200, box.getMeasureWidth(), 0.0001f);
        assertEquals(250, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        box.onMeasure();

        assertEquals(Widget.WRAP_CONTENT, box.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.WRAP_CONTENT, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        box.onMeasure();

        assertEquals(Widget.MATCH_PARENT, box.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, box.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureBoxWithMaxMargin() {
        // Max should not affect measure values
        Box box = new Box();
        Box child = new Box();
        child.setPrefSize(80, 80);
        box.add(child);

        box.setMaxSize(150, 100);
        box.setPrefSize(200, 250);
        box.setMargins(5, 4, 10, 8);
        box.onMeasure();

        assertEquals(212, box.getMeasureWidth(), 0.0001f);
        assertEquals(265, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        box.onMeasure();

        assertEquals(92, box.getMeasureWidth(), 0.0001f);
        assertEquals(95, box.getMeasureHeight(), 0.0001f);

        box.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        box.onMeasure();

        assertEquals(Widget.MATCH_PARENT, box.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, box.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureBoxMatchParent() {
        Box parent = new Box();
        parent.setPrefSize(150, 100);

        Box child = new Box();
        child.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.add(child);

        parent.onMeasure();

        assertEquals(150, parent.getMeasureWidth(), 0.0001f);
        assertEquals(100, parent.getMeasureHeight(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, child.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, child.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureBoxMatchParentChild() {
        Box parent = new Box();
        parent.setPrefSize(150, 100);

        Box child = new Box();
        child.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.add(child);

        parent.onMeasure();

        assertEquals(150f, parent.getMeasureWidth(), 0.0001f);
        assertEquals(100f, parent.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureBoxMatchParentChildAndWrapContent() {
        Box parent = new Box();
        parent.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);

        Box child = new Box();
        child.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.add(child);

        parent.onMeasure();

        assertEquals(Widget.MATCH_PARENT, child.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, child.getMeasureHeight(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, parent.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, parent.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureChildBiggerThanParent() {
        Box parent = new Box();
        parent.setPrefSize(150, 100);

        Box child = new Box();
        child.setPrefSize(200, 250);
        parent.add(child);

        parent.onMeasure();

        assertEquals(150, parent.getMeasureWidth(), 0.0001f);
        assertEquals(100, parent.getMeasureHeight(), 0.0001f);
        assertEquals(200, child.getMeasureWidth(), 0.0001f);
        assertEquals(250, child.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void layoutOneChildren() {
        Box parent = new Box();
        Box child = new Box();
        parent.add(child);

        // Child size does not affect Parent
        parent.setPrefSize(200, 400);
        child.setPrefSize(150, 250);
        parent.onMeasure();
        assertEquals(200, parent.getMeasureWidth(), 0.0001f);
        assertEquals(400, parent.getMeasureHeight(), 0.0001f);
        parent.onLayout(200, 400);

        assertEquals(200, parent.getLayoutWidth(), 0.0001f);
        assertEquals(400, parent.getLayoutHeight(), 0.0001f);
        assertEquals(150, child.getLayoutWidth(), 0.0001f);
        assertEquals(250, child.getLayoutHeight(), 0.0001f);

        // Parent size does not affect Child
        parent.setPrefSize(100, 200);
        child.setPrefSize(150, 250);
        parent.onMeasure();
        assertEquals(100, parent.getMeasureWidth(), 0.0001f);
        assertEquals(200, parent.getMeasureHeight(), 0.0001f);
        parent.onLayout(100, 200);

        assertEquals(100, parent.getLayoutWidth(), 0.0001f);
        assertEquals(200, parent.getLayoutHeight(), 0.0001f);
        assertEquals(150, child.getLayoutWidth(), 0.0001f);
        assertEquals(250, child.getLayoutHeight(), 0.0001f);

        // Child MatchParent should obey Parent size
        parent.setPrefSize(100, 200);
        child.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.onMeasure();
        assertEquals(100, parent.getMeasureWidth(), 0.0001f);
        assertEquals(200, parent.getMeasureHeight(), 0.0001f);
        parent.onLayout(100, 200);

        assertEquals(100, parent.getLayoutWidth(), 0.0001f);
        assertEquals(200, parent.getLayoutHeight(), 0.0001f);
        assertEquals(100, child.getLayoutWidth(), 0.0001f);
        assertEquals(200, child.getLayoutHeight(), 0.0001f);

        // Parent WrapContent size should obey Child size
        parent.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        child.setPrefSize(150, 250);
        parent.onMeasure();
        assertEquals(150, parent.getMeasureWidth(), 0.0001f);
        assertEquals(250, parent.getMeasureHeight(), 0.0001f);
        parent.onLayout(150, 250);

        assertEquals(150, parent.getLayoutWidth(), 0.0001f);
        assertEquals(250, parent.getLayoutHeight(), 0.0001f);
        assertEquals(150, child.getLayoutWidth(), 0.0001f);
        assertEquals(250, child.getLayoutHeight(), 0.0001f);

        // Parent WrapContent with Child MatchParent should obey MaxSize
        parent.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        child.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child.setMaxSize(80, 75);
        parent.onMeasure();
        assertEquals(80, parent.getMeasureWidth(), 0.0001f);
        assertEquals(75, parent.getMeasureHeight(), 0.0001f);
        parent.onLayout(80, 75);

        assertEquals(80, parent.getLayoutWidth(), 0.0001f);
        assertEquals(75, parent.getLayoutHeight(), 0.0001f);
        assertEquals(80, child.getLayoutWidth(), 0.0001f);
        assertEquals(75, child.getLayoutHeight(), 0.0001f);

        // Parent WrapContent with Child MatchParent should obey MaxSize plus margins and paddings
        parent.setMargins(1, 2, 4, 8);
        parent.setPadding(16, 32, 64, 128);
        parent.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        child.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child.setMaxSize(80, 75);
        parent.onMeasure();
        assertEquals(80 + 170, parent.getMeasureWidth(), 0.0001f);
        assertEquals(75 + 85, parent.getMeasureHeight(), 0.0001f);
        parent.onLayout(250, 160);

        assertEquals(250, parent.getLayoutWidth(), 0.0001f);
        assertEquals(160, parent.getLayoutHeight(), 0.0001f);
        assertEquals(80, child.getLayoutWidth(), 0.0001f);
        assertEquals(75, child.getLayoutHeight(), 0.0001f);

        // Parent WrapContent with Child MatchParent should obey MaxSize plus margins and paddings, and its own margins
        parent.setMargins(1, 2, 4, 8);
        parent.setPadding(16, 32, 64, 128);
        parent.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        child.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child.setMaxSize(80, 75);
        child.setMargins(1, 2, 3, 4);
        parent.onMeasure();
        assertEquals(80 + 170 + 6, parent.getMeasureWidth(), 0.0001f);
        assertEquals(75 + 85 + 4, parent.getMeasureHeight(), 0.0001f);
        parent.onLayout(256, 164);

        assertEquals(256, parent.getLayoutWidth(), 0.0001f);
        assertEquals(164, parent.getLayoutHeight(), 0.0001f);
        assertEquals(86, child.getLayoutWidth(), 0.0001f);
        assertEquals(79, child.getLayoutHeight(), 0.0001f);
    }

    @Test
    public void layoutSiblings() {
        Box parent = new Box();
        Box child1 = new Box();
        Box child2 = new Box();
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

        // Parent size does not affect Child
        parent.setPrefSize(100, 200);
        child1.setPrefSize(150, 250);
        child2.setPrefSize(150, 250);
        parent.onMeasure();
        assertEquals(100, parent.getMeasureWidth(), 0.0001f);
        assertEquals(200, parent.getMeasureHeight(), 0.0001f);
        parent.onLayout(100, 200);

        assertEquals(100, parent.getLayoutWidth(), 0.0001f);
        assertEquals(200, parent.getLayoutHeight(), 0.0001f);
        assertEquals(150, child1.getLayoutWidth(), 0.0001f);
        assertEquals(250, child1.getLayoutHeight(), 0.0001f);
        assertEquals(150, child2.getLayoutWidth(), 0.0001f);
        assertEquals(250, child2.getLayoutHeight(), 0.0001f);

        // Child MatchParent should obey Parent size
        parent.setPrefSize(100, 200);
        child1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        child2.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.onMeasure();
        assertEquals(100, parent.getMeasureWidth(), 0.0001f);
        assertEquals(200, parent.getMeasureHeight(), 0.0001f);
        parent.onLayout(100, 200);

        assertEquals(100, parent.getLayoutWidth(), 0.0001f);
        assertEquals(200, parent.getLayoutHeight(), 0.0001f);
        assertEquals(100, child1.getLayoutWidth(), 0.0001f);
        assertEquals(200, child1.getLayoutHeight(), 0.0001f);
        assertEquals(100, child2.getLayoutWidth(), 0.0001f);
        assertEquals(200, child2.getLayoutHeight(), 0.0001f);

        // Parent WrapContent size should obey the biggest Child size
        parent.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        child1.setPrefSize(150, 250);
        child2.setPrefSize(130, 325);
        parent.onMeasure();
        assertEquals(150, parent.getMeasureWidth(), 0.0001f);
        assertEquals(325, parent.getMeasureHeight(), 0.0001f);
        parent.onLayout(150, 325);

        assertEquals(150, parent.getLayoutWidth(), 0.0001f);
        assertEquals(325, parent.getLayoutHeight(), 0.0001f);
        assertEquals(150, child1.getLayoutWidth(), 0.0001f);
        assertEquals(250, child1.getLayoutHeight(), 0.0001f);
        assertEquals(130, child2.getLayoutWidth(), 0.0001f);
        assertEquals(325, child2.getLayoutHeight(), 0.0001f);
    }
}
