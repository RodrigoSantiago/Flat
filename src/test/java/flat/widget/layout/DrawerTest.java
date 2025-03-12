package flat.widget.layout;

import flat.uxml.Controller;
import flat.uxml.UXChild;
import flat.uxml.UXChildren;
import flat.uxml.UXHash;
import flat.uxml.value.*;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.OverlayMode;
import flat.widget.enums.Position;
import flat.widget.enums.VerticalAlign;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class DrawerTest {


    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        Drawer drawer = new Drawer();

        assertNull(drawer.getFrontContent());
        assertNull(drawer.getBackContent());
        assertEquals(0, drawer.getSlideAnimationDuration(), 0.001f);
        assertEquals(Position.LEFT, drawer.getSlidePosition());
        assertEquals(HorizontalAlign.LEFT, drawer.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, drawer.getVerticalAlign());
        assertEquals(0x00000000, drawer.getBlockColor());
        assertEquals(OverlayMode.FLOATING, drawer.getOverlayMode());
        assertFalse(drawer.isBlockEvents());
        assertFalse(drawer.isAutoClose());

        drawer.setAttributes(createNonDefaultValues(), null);
        drawer.applyAttributes(controller);

        assertNull(drawer.getFrontContent());
        assertNull(drawer.getBackContent());
        assertEquals(0, drawer.getSlideAnimationDuration(), 0.001f);
        assertEquals(Position.LEFT, drawer.getSlidePosition());
        assertEquals(HorizontalAlign.LEFT, drawer.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, drawer.getVerticalAlign());
        assertEquals(0x00000000, drawer.getBlockColor());
        assertEquals(OverlayMode.FLOATING, drawer.getOverlayMode());
        assertFalse(drawer.isBlockEvents());
        assertFalse(drawer.isAutoClose());

        drawer.applyStyle();

        assertNull(drawer.getFrontContent());
        assertNull(drawer.getBackContent());
        assertEquals(1, drawer.getSlideAnimationDuration(), 0.001f);
        assertEquals(Position.BOTTOM, drawer.getSlidePosition());
        assertEquals(HorizontalAlign.RIGHT, drawer.getHorizontalAlign());
        assertEquals(VerticalAlign.MIDDLE, drawer.getVerticalAlign());
        assertEquals(0xFF0000FF, drawer.getBlockColor());
        assertEquals(OverlayMode.SPLIT, drawer.getOverlayMode());
        assertTrue(drawer.isBlockEvents());
        assertTrue(drawer.isAutoClose());
    }

    @Test
    public void children() {
        Drawer drawer = new Drawer();
        Panel childA = new Panel();
        Panel childB = new Panel();
        Panel content = new Panel();

        UXChildren uxChild = mockChildren(
                new Widget[] {childA, childB, content},
                new String[] {"back-content", "front-content", ""});

        assertNull(childA.getParent());
        assertNull(childB.getParent());
        assertNull(content.getParent());

        drawer.applyChildren(uxChild);

        assertEquals(drawer, childA.getParent());
        assertEquals(drawer, childB.getParent());
        assertNull(content.getParent());

        assertEquals(childA, drawer.getBackContent());
        assertEquals(childB, drawer.getFrontContent());
    }

    @Test
    public void measureFloatingHorizontal() {
        Panel childA = new Panel();
        Panel content = new Panel();

        Drawer drawer = new Drawer();
        drawer.setBackContent(childA);
        drawer.setFrontContent(content);
        drawer.setSlidePosition(Position.LEFT);
        drawer.setOverlayMode(OverlayMode.FLOATING);
        drawer.onMeasure();

        assertEquals(0, drawer.getMeasureWidth(), 0.1f);
        assertEquals(0, drawer.getMeasureHeight(), 0.1f);

        childA.setPrefSize(20, 30);
        drawer.onMeasure();

        assertEquals(20, drawer.getMeasureWidth(), 0.1f);
        assertEquals(30, drawer.getMeasureHeight(), 0.1f);

        content.setPrefSize(30, 40);
        drawer.onMeasure();

        assertEquals(30, drawer.getMeasureWidth(), 0.1f);
        assertEquals(40, drawer.getMeasureHeight(), 0.1f);

        content.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        drawer.onMeasure();

        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureHeight(), 0.1f);

        childA.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        content.setPrefSize(30, 40);
        drawer.onMeasure();

        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureHeight(), 0.1f);

        childA.setPrefSize(20, 30);
        content.setPrefSize(30, 40);
        drawer.setMargins(1, 2, 3, 4);
        drawer.setPadding(5, 4, 2, 3);
        drawer.onMeasure();

        assertEquals(30 + 13, drawer.getMeasureWidth(), 0.1f);
        assertEquals(40 + 11, drawer.getMeasureHeight(), 0.1f);

        drawer.setPrefSize(100, 200);
        drawer.onMeasure();

        assertEquals(106, drawer.getMeasureWidth(), 0.1f);
        assertEquals(204, drawer.getMeasureHeight(), 0.1f);

        drawer.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        drawer.onMeasure();

        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureNonFloatingHorizontal() {
        Panel childA = new Panel();
        Panel content = new Panel();

        Drawer drawer = new Drawer();
        drawer.setBackContent(childA);
        drawer.setFrontContent(content);
        drawer.setSlidePosition(Position.LEFT);
        drawer.setOverlayMode(OverlayMode.SPLIT);
        drawer.onMeasure();

        assertEquals(0, drawer.getMeasureWidth(), 0.1f);
        assertEquals(0, drawer.getMeasureHeight(), 0.1f);

        childA.setPrefSize(20, 30);
        drawer.onMeasure();

        assertEquals(20, drawer.getMeasureWidth(), 0.1f);
        assertEquals(30, drawer.getMeasureHeight(), 0.1f);

        content.setPrefSize(30, 40);
        drawer.onMeasure();

        assertEquals(20, drawer.getMeasureWidth(), 0.1f);
        assertEquals(40, drawer.getMeasureHeight(), 0.1f);

        drawer.show();
        drawer.onMeasure();

        assertEquals(50, drawer.getMeasureWidth(), 0.1f);
        assertEquals(40, drawer.getMeasureHeight(), 0.1f);

        content.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        drawer.onMeasure();

        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureHeight(), 0.1f);

        childA.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        content.setPrefSize(30, 40);
        drawer.onMeasure();

        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureHeight(), 0.1f);

        childA.setPrefSize(20, 30);
        content.setPrefSize(30, 40);
        drawer.setMargins(1, 2, 3, 4);
        drawer.setPadding(5, 4, 2, 3);
        drawer.onMeasure();

        assertEquals(50 + 13, drawer.getMeasureWidth(), 0.1f);
        assertEquals(40 + 11, drawer.getMeasureHeight(), 0.1f);

        drawer.setPrefSize(100, 200);
        drawer.onMeasure();

        assertEquals(106, drawer.getMeasureWidth(), 0.1f);
        assertEquals(204, drawer.getMeasureHeight(), 0.1f);

        drawer.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        drawer.onMeasure();

        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureFloatingVertical() {
        Panel childA = new Panel();
        Panel content = new Panel();

        Drawer drawer = new Drawer();
        drawer.setBackContent(childA);
        drawer.setFrontContent(content);
        drawer.setSlidePosition(Position.TOP);
        drawer.setOverlayMode(OverlayMode.FLOATING);
        drawer.onMeasure();

        assertEquals(0, drawer.getMeasureWidth(), 0.1f);
        assertEquals(0, drawer.getMeasureHeight(), 0.1f);

        childA.setPrefSize(20, 30);
        drawer.onMeasure();

        assertEquals(20, drawer.getMeasureWidth(), 0.1f);
        assertEquals(30, drawer.getMeasureHeight(), 0.1f);

        content.setPrefSize(30, 40);
        drawer.onMeasure();

        assertEquals(30, drawer.getMeasureWidth(), 0.1f);
        assertEquals(40, drawer.getMeasureHeight(), 0.1f);

        content.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        drawer.onMeasure();

        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureHeight(), 0.1f);

        childA.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        content.setPrefSize(30, 40);
        drawer.onMeasure();

        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureHeight(), 0.1f);

        childA.setPrefSize(20, 30);
        content.setPrefSize(30, 40);
        drawer.setMargins(1, 2, 3, 4);
        drawer.setPadding(5, 4, 2, 3);
        drawer.onMeasure();

        assertEquals(30 + 13, drawer.getMeasureWidth(), 0.1f);
        assertEquals(40 + 11, drawer.getMeasureHeight(), 0.1f);

        drawer.setPrefSize(100, 200);
        drawer.onMeasure();

        assertEquals(106, drawer.getMeasureWidth(), 0.1f);
        assertEquals(204, drawer.getMeasureHeight(), 0.1f);

        drawer.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        drawer.onMeasure();

        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureNonFloatingVertical() {
        Panel childA = new Panel();
        Panel content = new Panel();

        Drawer drawer = new Drawer();
        drawer.setBackContent(childA);
        drawer.setFrontContent(content);
        drawer.setSlidePosition(Position.TOP);
        drawer.setOverlayMode(OverlayMode.SPLIT);
        drawer.onMeasure();

        assertEquals(0, drawer.getMeasureWidth(), 0.1f);
        assertEquals(0, drawer.getMeasureHeight(), 0.1f);

        childA.setPrefSize(20, 30);
        drawer.onMeasure();

        assertEquals(20, drawer.getMeasureWidth(), 0.1f);
        assertEquals(30, drawer.getMeasureHeight(), 0.1f);

        content.setPrefSize(30, 40);
        drawer.onMeasure();

        assertEquals(30, drawer.getMeasureWidth(), 0.1f);
        assertEquals(30, drawer.getMeasureHeight(), 0.1f);

        drawer.show();
        drawer.onMeasure();

        assertEquals(30, drawer.getMeasureWidth(), 0.1f);
        assertEquals(70, drawer.getMeasureHeight(), 0.1f);

        content.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        drawer.onMeasure();

        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureHeight(), 0.1f);

        childA.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        content.setPrefSize(30, 40);
        drawer.onMeasure();

        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureHeight(), 0.1f);

        childA.setPrefSize(20, 30);
        content.setPrefSize(30, 40);
        drawer.setMargins(1, 2, 3, 4);
        drawer.setPadding(5, 4, 2, 3);
        drawer.onMeasure();

        assertEquals(30 + 13, drawer.getMeasureWidth(), 0.1f);
        assertEquals(70 + 11, drawer.getMeasureHeight(), 0.1f);

        drawer.setPrefSize(100, 200);
        drawer.onMeasure();

        assertEquals(106, drawer.getMeasureWidth(), 0.1f);
        assertEquals(204, drawer.getMeasureHeight(), 0.1f);

        drawer.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        drawer.onMeasure();

        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, drawer.getMeasureHeight(), 0.1f);
    }

    @Test
    public void layout() {
        Panel childA = new Panel();
        Panel content = new Panel();

        Drawer drawer = new Drawer();
        drawer.setBackContent(childA);
        drawer.setFrontContent(content);
        drawer.setSlidePosition(Position.LEFT);
        drawer.setOverlayMode(OverlayMode.FLOATING);

        childA.setPrefSize(300, 250);
        content.setPrefSize(150, 400);

        drawer.onMeasure();
        assertMeasure(drawer, 300, 400);
        drawer.onLayout(300, 400);
        assertLayout(childA, 0.0f, 0.0f, 300.0f, 250.0f);
        assertLayout(content, -150.0f, 0.0f, 150.0f, 400.0f);

        drawer.setSlidePosition(Position.RIGHT);
        drawer.onMeasure();
        assertMeasure(drawer, 300, 400);
        drawer.onLayout(300, 400);
        assertLayout(childA, 0.0f, 0.0f, 300.0f, 250.0f);
        assertLayout(content, 300.0f, 0.0f, 150.0f, 400.0f);

        drawer.setSlidePosition(Position.TOP);
        drawer.onMeasure();
        assertMeasure(drawer, 300, 400);
        drawer.onLayout(300, 400);
        assertLayout(childA, 0.0f, 0.0f, 300.0f, 250.0f);
        assertLayout(content, 0.0f, -400.0f, 150.0f, 400.0f);

        drawer.setSlidePosition(Position.BOTTOM);
        drawer.onMeasure();
        assertMeasure(drawer, 300, 400);
        drawer.onLayout(300, 400);
        assertLayout(childA, 0.0f, 0.0f, 300.0f, 250.0f);
        assertLayout(content, 0.0f, 400.0f, 150.0f, 400.0f);

        drawer.setSlidePosition(Position.LEFT);
        drawer.show();

        drawer.onMeasure();
        assertMeasure(drawer, 300, 400);
        drawer.onLayout(300, 400);
        assertLayout(childA, 0.0f, 0.0f, 300.0f, 250.0f);
        assertLayout(content, 0.0f, 0.0f, 150.0f, 400.0f);

        drawer.setSlidePosition(Position.RIGHT);
        drawer.onMeasure();
        assertMeasure(drawer, 300, 400);
        drawer.onLayout(300, 400);
        assertLayout(childA, 0.0f, 0.0f, 300.0f, 250.0f);
        assertLayout(content, 150.0f, 0.0f, 150.0f, 400.0f);

        drawer.setSlidePosition(Position.TOP);
        drawer.onMeasure();
        assertMeasure(drawer, 300, 400);
        drawer.onLayout(300, 400);
        assertLayout(childA, 0.0f, 0.0f, 300.0f, 250.0f);
        assertLayout(content, 0.0f, 0.0f, 150.0f, 400.0f);

        drawer.setSlidePosition(Position.BOTTOM);
        drawer.onMeasure();
        assertMeasure(drawer, 300, 400);
        drawer.onLayout(300, 400);
        assertLayout(childA, 0.0f, 0.0f, 300.0f, 250.0f);
        assertLayout(content, 0.0f, 0.0f, 150.0f, 400.0f);
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


    private UXChildren mockChildren(Widget[] widgets, String[] booleans) {
        UXChildren uxChild = mock(UXChildren.class);
        ArrayList<UXChild> children = new ArrayList<>();
        for (int i = 0; i < widgets.length; i++) {
            var widget = widgets[i];
            HashMap<Integer, UXValue> attributes = null;
            if (booleans != null && i < booleans.length) {
                attributes = new HashMap<>();
                attributes.put(UXHash.getHash(booleans[i]), new UXValueBool(true));
            }
            children.add(new UXChild(widget, attributes));
        }
        when(uxChild.iterator()).thenReturn(children.iterator());
        return uxChild;
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("slide-animation-duration"), new UXValueNumber(1));
        hash.put(UXHash.getHash("slide-position"), new UXValueText(Position.BOTTOM.toString()));
        hash.put(UXHash.getHash("horizontal-align"), new UXValueText(HorizontalAlign.RIGHT.toString()));
        hash.put(UXHash.getHash("vertical-align"), new UXValueText(VerticalAlign.MIDDLE.toString()));
        hash.put(UXHash.getHash("overlay-mode"), new UXValueText(OverlayMode.SPLIT.toString()));
        hash.put(UXHash.getHash("block-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("block-events"), new UXValueBool(true));
        hash.put(UXHash.getHash("auto-close"), new UXValueBool(true));
        return hash;
    }

}