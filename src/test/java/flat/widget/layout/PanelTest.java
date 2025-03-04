package flat.widget.layout;

import flat.uxml.UXChild;
import flat.uxml.UXChildren;
import flat.widget.Widget;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class PanelTest {

    @Test
    public void childrenFromUx() {
        Panel panel = new Panel();
        Widget child = new Widget();

        UXChildren uxChild = mockChildren(child);

        assertNull(child.getParent());
        panel.applyChildren(uxChild);
        assertEquals(child, panel.getChildrenIterable().get(0));
        assertEquals(panel, child.getParent());
    }

    @Test
    public void siblingsFromUx() {
        Panel panel = new Panel();
        Widget child1 = new Widget();
        Widget child2 = new Widget();

        UXChildren uxChild = mockChildren(child1, child2);

        assertNull(child1.getParent());
        assertNull(child2.getParent());
        panel.applyChildren(uxChild);
        assertEquals(child1, panel.getChildrenIterable().get(0));
        assertEquals(child2, panel.getChildrenIterable().get(1));
        assertEquals(panel, child1.getParent());
        assertEquals(panel, child2.getParent());
    }

    @Test
    public void siblingsFromAdding() {
        Panel panel = new Panel();
        Widget child1 = new Widget();
        Widget child2 = new Widget();


        assertNull(child1.getParent());
        assertNull(child2.getParent());

        panel.add(child1, child2);

        assertEquals(child1, panel.getChildrenIterable().get(0));
        assertEquals(child2, panel.getChildrenIterable().get(1));
        assertEquals(panel, child1.getParent());
        assertEquals(panel, child2.getParent());
    }

    @Test
    public void swapParentsFromAdding() {
        Panel panel1 = new Panel();
        Panel panel2 = new Panel();
        Widget child = new Widget();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(child.getParent());
        panel1.add(child);

        assertEquals(child, panel1.getChildrenIterable().get(0));
        assertEquals(0, panel2.getChildrenIterable().size());
        assertEquals(panel1, child.getParent());

        panel2.add(child);
        assertEquals(child, panel2.getChildrenIterable().get(0));
        assertEquals(0, panel1.getChildrenIterable().size());
        assertEquals(panel2, child.getParent());
    }

    @Test
    public void addTwice() {
        Panel panel = new Panel();
        Widget child = new Widget();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(child.getParent());
        panel.add(child);

        assertEquals(child, panel.getChildrenIterable().get(0));
        assertEquals(1, panel.getChildrenIterable().size());
        assertEquals(panel, child.getParent());

        panel.add(child);
        assertEquals(child, panel.getChildrenIterable().get(0));
        assertEquals(1, panel.getChildrenIterable().size());
        assertEquals(panel, child.getParent());
    }

    @Test
    public void addAndRemove() {
        Panel panel = new Panel();
        Widget child = new Widget();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(child.getParent());
        panel.add(child);

        assertEquals(child, panel.getChildrenIterable().get(0));
        assertEquals(1, panel.getChildrenIterable().size());
        assertEquals(panel, child.getParent());

        panel.remove(child);
        assertEquals(0, panel.getChildrenIterable().size());
        assertNull(child.getParent());
    }

    @Test
    public void addAndRemoveSibling() {
        Panel panel = new Panel();
        Widget child1 = new Widget();
        Widget child2 = new Widget();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(child1.getParent());
        assertNull(child2.getParent());
        panel.add(child1);
        panel.add(child2);

        assertEquals(child1, panel.getChildrenIterable().get(0));
        assertEquals(child2, panel.getChildrenIterable().get(1));
        assertEquals(2, panel.getChildrenIterable().size());
        assertEquals(panel, child1.getParent());
        assertEquals(panel, child2.getParent());

        panel.remove(child1);
        assertEquals(child2, panel.getChildrenIterable().get(0));
        assertEquals(1, panel.getChildrenIterable().size());
        assertNull(child1.getParent());
        assertEquals(panel, child2.getParent());
    }

    @Test
    public void siblingsOrderFromElevation() {
        Panel panel = new Panel();
        Widget child1 = new Widget();
        Widget child2 = new Widget();

        assertNull(child1.getParent());
        assertNull(child2.getParent());

        panel.add(child1, child2);

        assertEquals(child1, panel.getChildrenIterable().get(0));
        assertEquals(child2, panel.getChildrenIterable().get(1));
        assertEquals(panel, child1.getParent());
        assertEquals(panel, child2.getParent());

        child1.setElevation(2);
        child2.setElevation(0);

        assertEquals(child2, panel.getChildrenIterable().get(0));
        assertEquals(child1, panel.getChildrenIterable().get(1));
        assertEquals(panel, child1.getParent());
        assertEquals(panel, child2.getParent());
    }

    @Test
    public void addItself() {
        Panel panel = new Panel();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(panel.getParent());

        panel.add(panel);

        assertNull(panel.getParent());
        assertEquals(0, panel.getChildrenIterable().size());
    }

    @Test
    public void addItsParent() {
        Panel panel = new Panel();
        Panel parent = new Panel();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(panel.getParent());
        assertNull(parent.getParent());

        parent.add(panel);

        assertEquals(parent, panel.getParent());
        assertNull(parent.getParent());

        assertEquals(0, panel.getChildrenIterable().size());
        assertEquals(1, parent.getChildrenIterable().size());

        panel.add(parent);

        assertEquals(parent, panel.getParent());
        assertNull(parent.getParent());

        assertEquals(0, panel.getChildrenIterable().size());
        assertEquals(1, parent.getChildrenIterable().size());
    }

    @Test
    public void measure() {
        Panel panel = new Panel();

        panel.setPrefSize(150, 100);
        panel.onMeasure();

        assertEquals(150f, panel.getMeasureWidth(), 0.0001f);
        assertEquals(100f, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        panel.onMeasure();

        assertEquals(Widget.WRAP_CONTENT, panel.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.WRAP_CONTENT, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        panel.onMeasure();

        assertEquals(Widget.MATCH_PARENT, panel.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, panel.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureWithMin() {
        // Min should affect measure values
        Panel panel = new Panel();

        panel.setPrefSize(150, 100);
        panel.setMinSize(200, 250);
        panel.onMeasure();

        assertEquals(200, panel.getMeasureWidth(), 0.0001f);
        assertEquals(250, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        panel.onMeasure();

        assertEquals(200, panel.getMeasureWidth(), 0.0001f);
        assertEquals(250, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        panel.onMeasure();

        assertEquals(Widget.MATCH_PARENT, panel.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, panel.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureWithMinAndPadding() {
        Panel panel = new Panel();

        panel.setPrefSize(150, 100);
        panel.setPadding(10, 10, 10, 10);
        panel.onMeasure();

        assertEquals(150, panel.getMeasureWidth(), 0.0001f);
        assertEquals(100, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(150, 100);
        panel.setMinSize(160, 110);
        panel.setPadding(10, 10, 10, 10);
        panel.onMeasure();

        assertEquals(160, panel.getMeasureWidth(), 0.0001f);
        assertEquals(110, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(150, 100);
        panel.setMinSize(180, 130);
        panel.setPadding(10, 10, 10, 10);
        panel.onMeasure();

        assertEquals(180, panel.getMeasureWidth(), 0.0001f);
        assertEquals(130, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        panel.setMinSize(160, 110);
        panel.setPadding(10, 10, 10, 10);
        panel.onMeasure();

        assertEquals(160, panel.getMeasureWidth(), 0.0001f);
        assertEquals(110, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        panel.setMinSize(160, 110);
        panel.setPadding(10, 10, 10, 10);
        panel.onMeasure();

        assertEquals(Widget.MATCH_PARENT, panel.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, panel.getMeasureHeight(), 0.0001f);

        Panel child = new Panel();
        panel.add(child);

        panel.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        panel.setMinSize(0, 0);
        panel.setPadding(1, 2, 3, 4);
        child.setPrefSize(150, 100);
        panel.onMeasure();

        assertEquals(156, panel.getMeasureWidth(), 0.0001f);
        assertEquals(104, panel.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureWithMinAndMargin() {
        Panel panel = new Panel();

        panel.setPrefSize(150, 100);
        panel.setMargins(10, 10, 10, 10);
        panel.onMeasure();

        assertEquals(170, panel.getMeasureWidth(), 0.0001f);
        assertEquals(120, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(150, 100);
        panel.setMinSize(160, 110);
        panel.setMargins(10, 10, 10, 10);
        panel.onMeasure();

        assertEquals(180, panel.getMeasureWidth(), 0.0001f);
        assertEquals(130, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(150, 100);
        panel.setMinSize(180, 130);
        panel.setMargins(10, 10, 10, 10);
        panel.onMeasure();

        assertEquals(200, panel.getMeasureWidth(), 0.0001f);
        assertEquals(150, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        panel.setMinSize(160, 110);
        panel.setMargins(10, 10, 10, 10);
        panel.onMeasure();

        assertEquals(180, panel.getMeasureWidth(), 0.0001f);
        assertEquals(130, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        panel.setMinSize(160, 110);
        panel.setMargins(10, 10, 10, 10);
        panel.onMeasure();

        assertEquals(Widget.MATCH_PARENT, panel.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, panel.getMeasureHeight(), 0.0001f);

        Panel child = new Panel();
        panel.add(child);

        panel.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        panel.setMinSize(0, 0);
        panel.setMargins(1, 2, 3, 4);
        child.setPrefSize(150, 100);
        panel.onMeasure();

        assertEquals(156, panel.getMeasureWidth(), 0.0001f);
        assertEquals(104, panel.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureWithMax() {
        // Max should not affect measure values
        Panel panel = new Panel();
        panel.setMaxSize(150, 100);
        panel.setPrefSize(200, 250);
        panel.onMeasure();

        assertEquals(200, panel.getMeasureWidth(), 0.0001f);
        assertEquals(250, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        panel.onMeasure();

        assertEquals(Widget.WRAP_CONTENT, panel.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.WRAP_CONTENT, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        panel.onMeasure();

        assertEquals(Widget.MATCH_PARENT, panel.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, panel.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureWithMaxMargin() {
        // Max should not affect measure values
        Panel panel = new Panel();
        Panel child = new Panel();
        child.setPrefSize(80, 80);
        panel.add(child);

        panel.setMaxSize(150, 100);
        panel.setPrefSize(200, 250);
        panel.setMargins(5, 4, 10, 8);
        panel.onMeasure();

        assertEquals(212, panel.getMeasureWidth(), 0.0001f);
        assertEquals(265, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        panel.onMeasure();

        assertEquals(92, panel.getMeasureWidth(), 0.0001f);
        assertEquals(95, panel.getMeasureHeight(), 0.0001f);

        panel.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        panel.onMeasure();

        assertEquals(Widget.MATCH_PARENT, panel.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, panel.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureMatchParent() {
        Panel parent = new Panel();
        parent.setPrefSize(150, 100);

        Panel child = new Panel();
        child.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.add(child);

        parent.onMeasure();

        assertEquals(150, parent.getMeasureWidth(), 0.0001f);
        assertEquals(100, parent.getMeasureHeight(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, child.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, child.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureMatchParentChild() {
        Panel parent = new Panel();
        parent.setPrefSize(150, 100);

        Panel child = new Panel();
        child.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.add(child);

        parent.onMeasure();

        assertEquals(150f, parent.getMeasureWidth(), 0.0001f);
        assertEquals(100f, parent.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureMatchParentChildAndWrapContent() {
        Panel parent = new Panel();
        parent.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);

        Panel child = new Panel();
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
        Panel parent = new Panel();
        parent.setPrefSize(150, 100);

        Panel child = new Panel();
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
        Panel parent = new Panel();
        Panel child = new Panel();
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
        Panel parent = new Panel();
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

    @Test
    public void layoutSingleChild() {
        Panel parent = new Panel();
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
