package flat.widget.stages;

import flat.events.ActionEvent;
import flat.uxml.*;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueNumber;
import flat.uxml.value.UXValueText;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.enums.DropdownAlign;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.Policy;
import flat.widget.enums.VerticalBarPosition;
import flat.widget.layout.Panel;
import flat.window.Activity;
import flat.window.ActivitySupport;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class MenuTest {


    Activity activityA;
    Scene sceneA;
    Activity activityB;
    Scene sceneB;

    @Before
    public void before() {
        activityA = mock(Activity.class);
        sceneA = new Scene();
        ActivitySupport.setActivity(sceneA, activityA);
        when(activityA.getScene()).thenReturn(sceneA);
        when(activityA.getWidth()).thenReturn(800);
        when(activityA.getHeight()).thenReturn(600);

        activityB = mock(Activity.class);
        sceneB = new Scene();
        ActivitySupport.setActivity(sceneB, activityB);
        when(activityB.getScene()).thenReturn(sceneB);
        when(activityB.getWidth()).thenReturn(800);
        when(activityB.getHeight()).thenReturn(600);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", ActionEvent.class)).thenReturn(action);

        var listener = (UXValueListener<Float>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onViewOffsetWork", Float.class)).thenReturn(listener);

        Menu menu = new Menu();

        assertEquals(HorizontalAlign.CENTER, menu.getHorizontalAlign());
        assertEquals(Policy.AS_NEEDED, menu.getVerticalBarPolicy());
        assertEquals(VerticalBarPosition.RIGHT, menu.getVerticalBarPosition());
        assertEquals(0, menu.getShowTransitionDuration(), 0.001f);
        assertEquals(20, menu.getScrollSensibility(), 0.001f);
        assertNull(menu.getSlideListener());
        assertNull(menu.getViewOffsetListener());

        menu.setAttributes(createNonDefaultValues(), null);
        menu.applyAttributes(controller);

        assertEquals(HorizontalAlign.CENTER, menu.getHorizontalAlign());
        assertEquals(Policy.AS_NEEDED, menu.getVerticalBarPolicy());
        assertEquals(VerticalBarPosition.RIGHT, menu.getVerticalBarPosition());
        assertEquals(0, menu.getShowTransitionDuration(), 0.001f);
        assertEquals(20, menu.getScrollSensibility(), 0.001f);
        assertEquals(action, menu.getSlideListener());
        assertEquals(listener, menu.getViewOffsetListener());

        menu.applyStyle();

        assertEquals(HorizontalAlign.RIGHT, menu.getHorizontalAlign());
        assertEquals(Policy.ALWAYS, menu.getVerticalBarPolicy());
        assertEquals(VerticalBarPosition.LEFT, menu.getVerticalBarPosition());
        assertEquals(0.25f, menu.getShowTransitionDuration(), 0.001f);
        assertEquals(5, menu.getScrollSensibility(), 0.001f);
        assertEquals(action, menu.getSlideListener());
        assertEquals(listener, menu.getViewOffsetListener());
    }

    @Test
    public void measure() {
        Menu menu = new Menu();
        Divider content = new Divider();
        content.setPrefSize(500, 350);
        menu.addDivider(content);
        menu.onMeasure();

        assertEquals(500, menu.getMeasureWidth(), 0.1f);
        assertEquals(350, menu.getMeasureHeight(), 0.1f);

        menu.setMargins(1, 2, 3, 4);
        menu.setPadding(5, 4, 2, 3);
        menu.onMeasure();

        assertEquals(500 + 13, menu.getMeasureWidth(), 0.1f);
        assertEquals(350 + 11, menu.getMeasureHeight(), 0.1f);

        menu.setPrefSize(100, 200);
        menu.onMeasure();

        assertEquals(100 + 6, menu.getMeasureWidth(), 0.1f);
        assertEquals(200 + 4, menu.getMeasureHeight(), 0.1f);

        menu.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        menu.onMeasure();

        assertEquals(Widget.MATCH_PARENT, menu.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, menu.getMeasureHeight(), 0.1f);
    }

    @Test
    public void layout() {
        Menu menu = new Menu();
        Divider content = new Divider();
        content.setPrefSize(500, 350);
        menu.addDivider(content);

        // Same Size
        menu.onMeasure();
        assertMeasure(menu, 500, 350);
        menu.onLayout(500, 350);
        assertLayout(menu, 0, 0, 500, 350);
        assertEquals(350, menu.getTotalDimension(), 0.1f);
        assertEquals(350, menu.getViewDimension(), 0.1f);

        // Horizontal only Scrollable
        menu.onMeasure();
        assertMeasure(menu, 500, 350);
        menu.onLayout(250, 400);
        assertLayout(menu, 0, 0, 250, 400);
        assertEquals(400, menu.getTotalDimension(), 0.1f);
        assertEquals(400, menu.getViewDimension(), 0.1f);

        // Vertical only Scrollable
        menu.onMeasure();
        assertMeasure(menu, 500, 350);
        menu.onLayout(600, 150);
        assertLayout(menu, 0, 0, 600, 150);
        assertEquals(350, menu.getTotalDimension(), 0.1f);
        assertEquals(150, menu.getViewDimension(), 0.1f);

        // Both Scrollable
        menu.onMeasure();
        assertMeasure(menu, 500, 350);
        menu.onLayout(250, 150);
        assertLayout(menu, 0, 0, 250, 150);
        assertEquals(350, menu.getTotalDimension(), 0.1f);
        assertEquals(150, menu.getViewDimension(), 0.1f);
    }

    @Test
    public void showHide() {
        Menu menu = new Menu();

        menu.show(activityA, 150, 100, DropdownAlign.TOP_LEFT);

        assertTrue(menu.isShown());
        assertEquals(sceneA, menu.getParent());
        verify(activityA, times(1)).addPointerFilter(menu);
        verify(activityA, times(1)).addResizeFilter(menu);

        menu.hide();
        assertFalse(menu.isShown());
        assertNull(menu.getParent());
    }

    @Test
    public void removeParentManually() {
        Menu menu = new Menu();
        menu.show(activityA, 150, 100, DropdownAlign.TOP_LEFT);

        assertTrue(menu.isShown());
        assertEquals(sceneA, menu.getParent());
        verify(activityA, times(1)).addPointerFilter(menu);
        verify(activityA, times(1)).addResizeFilter(menu);

        menu.getParent().remove(menu);

        assertEquals(sceneA, menu.getParent());
        assertTrue(menu.isShown());
    }

    @Test
    public void moveParentManually() {
        Panel child = new Panel();
        sceneA.add(child);

        Menu menu = new Menu();
        menu.show(activityA, 150, 100, DropdownAlign.TOP_LEFT);

        assertTrue(menu.isShown());
        assertEquals(sceneA, menu.getParent());
        verify(activityA, times(1)).addPointerFilter(menu);
        verify(activityA, times(1)).addResizeFilter(menu);

        child.add(menu);

        assertEquals(sceneA, menu.getParent());
        assertTrue(menu.isShown());
    }

    @Test
    public void showHideCascade() {
        Menu menu = new Menu();
        Menu childMenu = new Menu();

        menu.show(activityA, 150, 100, DropdownAlign.TOP_LEFT);
        childMenu.show(menu, 150, 100, DropdownAlign.TOP_LEFT);

        assertTrue(menu.isShown());
        assertTrue(childMenu.isShown());
        assertEquals(sceneA, menu.getParent());
        assertEquals(sceneA, childMenu.getParent());
        verify(activityA, times(1)).addPointerFilter(menu);
        verify(activityA, times(1)).addResizeFilter(menu);
        verify(activityA, times(1)).addPointerFilter(childMenu);
        verify(activityA, times(1)).addResizeFilter(childMenu);

        menu.hide();
        assertFalse(menu.isShown());
        assertFalse(childMenu.isShown());
        assertNull(menu.getParent());
        assertNull(childMenu.getParent());
    }

    @Test
    public void showHideChild() {
        Menu menu = new Menu();
        Menu childMenu = new Menu();

        menu.show(activityA, 150, 100, DropdownAlign.TOP_LEFT);
        childMenu.show(menu, 150, 100, DropdownAlign.TOP_LEFT);

        assertTrue(menu.isShown());
        assertTrue(childMenu.isShown());
        assertEquals(sceneA, menu.getParent());
        assertEquals(sceneA, childMenu.getParent());
        verify(activityA, times(1)).addPointerFilter(menu);
        verify(activityA, times(1)).addResizeFilter(menu);
        verify(activityA, times(1)).addPointerFilter(childMenu);
        verify(activityA, times(1)).addResizeFilter(childMenu);

        childMenu.hide();
        assertTrue(menu.isShown());
        assertFalse(childMenu.isShown());
        assertEquals(sceneA, menu.getParent());
        assertNull(childMenu.getParent());
    }

    @Test
    public void showCircular() {
        Menu menu = new Menu();

        menu.show(activityA, 150, 100, DropdownAlign.TOP_LEFT);
        menu.show(menu, 150, 100, DropdownAlign.TOP_LEFT);

        assertTrue(menu.isShown());
        assertEquals(sceneA, menu.getParent());
        verify(activityA, times(1)).addPointerFilter(menu);
        verify(activityA, times(1)).addResizeFilter(menu);

        menu.hide();
        assertFalse(menu.isShown());
        assertNull(menu.getParent());
    }

    @Test
    public void layoutPosition() {
        Menu menu = new Menu();
        menu.setPrefSize(100, 150);

        menu.show(activityA, 200, 300, DropdownAlign.TOP_LEFT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(200, menu.getLayoutX(), 0.1f);
        assertEquals(300, menu.getLayoutY(), 0.1f);
        menu.hide();

        menu.show(activityA, 200, 300, DropdownAlign.TOP_RIGHT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(100, menu.getLayoutX(), 0.1f);
        assertEquals(300, menu.getLayoutY(), 0.1f);
        menu.hide();

        menu.show(activityA, 200, 300, DropdownAlign.BOTTOM_LEFT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(200, menu.getLayoutX(), 0.1f);
        assertEquals(150, menu.getLayoutY(), 0.1f);
        menu.hide();

        menu.show(activityA, 200, 300, DropdownAlign.BOTTOM_RIGHT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(100, menu.getLayoutX(), 0.1f);
        assertEquals(150, menu.getLayoutY(), 0.1f);
        menu.hide();

        menu.show(activityA, 200, 200, DropdownAlign.SCREEN_SPACE);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(200, menu.getLayoutX(), 0.1f);
        assertEquals(200, menu.getLayoutY(), 0.1f);
        menu.hide();

        menu.show(activityA, 600, 400, DropdownAlign.SCREEN_SPACE);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(500, menu.getLayoutX(), 0.1f);
        assertEquals(250, menu.getLayoutY(), 0.1f);
        menu.hide();

        // Outsite activity
        menu.show(activityA, 800, 600, DropdownAlign.TOP_LEFT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(700, menu.getLayoutX(), 0.1f);
        assertEquals(450, menu.getLayoutY(), 0.1f);
        menu.hide();

        menu.show(activityA, 50, 50, DropdownAlign.BOTTOM_RIGHT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(0, menu.getLayoutX(), 0.1f);
        assertEquals(0, menu.getLayoutY(), 0.1f);
        menu.hide();

        // Margins
        menu.setMargins(1, 2, 3, 4);
        menu.show(activityA, 200, 300, DropdownAlign.TOP_LEFT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(196, menu.getLayoutX(), 0.1f);
        assertEquals(299, menu.getLayoutY(), 0.1f);
        menu.hide();

        menu.show(activityA, 200, 300, DropdownAlign.BOTTOM_RIGHT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(96, menu.getLayoutX(), 0.1f);
        assertEquals(149, menu.getLayoutY(), 0.1f);
        menu.hide();
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("horizontal-align"), new UXValueText(HorizontalAlign.RIGHT.toString()));
        hash.put(UXHash.getHash("show-transition-duration"), new UXValueNumber(0.25f));
        hash.put(UXHash.getHash("scroll-sensibility"), new UXValueNumber(5));
        hash.put(UXHash.getHash("on-slide"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("on-view-offset-change"), new UXValueText("onViewOffsetWork"));
        hash.put(UXHash.getHash("vertical-bar-policy"), new UXValueText(Policy.ALWAYS.toString()));
        hash.put(UXHash.getHash("vertical-bar-position"), new UXValueText(VerticalBarPosition.LEFT.toString()));
        return hash;
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
}