package flat.widget.stages;

import flat.events.ActionEvent;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXListener;
import flat.uxml.UXValueListener;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueNumber;
import flat.uxml.value.UXValueText;
import flat.widget.Group;
import flat.widget.Widget;
import flat.widget.WidgetSupport;
import flat.widget.enums.DropdownAlign;
import flat.widget.enums.HorizontalAlign;
import flat.widget.layout.Panel;
import flat.window.Activity;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class MenuTest {

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", ActionEvent.class)).thenReturn(action);

        var listener = (UXValueListener<Float>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onViewOffsetWork", Float.class)).thenReturn(listener);

        Menu menu = new Menu();

        assertEquals(HorizontalAlign.CENTER, menu.getHorizontalAlign());
        assertEquals(0, menu.getShowupTransitionDuration(), 0.001f);
        assertEquals(10, menu.getScrollSensibility(), 0.001f);
        assertNull(menu.getSlideListener());
        assertNull(menu.getViewOffsetListener());

        menu.setAttributes(createNonDefaultValues(), "menu");
        menu.applyAttributes(controller);

        assertEquals(HorizontalAlign.CENTER, menu.getHorizontalAlign());
        assertEquals(0, menu.getShowupTransitionDuration(), 0.001f);
        assertEquals(10, menu.getScrollSensibility(), 0.001f);
        assertEquals(action, menu.getSlideListener());
        assertEquals(listener, menu.getViewOffsetListener());

        menu.applyStyle();

        assertEquals(HorizontalAlign.RIGHT, menu.getHorizontalAlign());
        assertEquals(0.25f, menu.getShowupTransitionDuration(), 0.001f);
        assertEquals(5, menu.getScrollSensibility(), 0.001f);
        assertEquals(action, menu.getSlideListener());
        assertEquals(listener, menu.getViewOffsetListener());
    }

    @Test
    public void measure() {
        Menu menu = new Menu();
        Panel content = new Panel();
        content.setPrefSize(500, 350);
        menu.add(content);
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
        Panel content = new Panel();
        content.setPrefSize(500, 350);
        menu.add(content);

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
        Activity activity = mock(Activity.class);
        Group group = mock(Group.class);
        when(activity.getWidth()).thenReturn(800f);
        when(activity.getHeight()).thenReturn(600f);
        Menu menu = new Menu();

        doAnswer((any) -> {
            WidgetSupport.setGroup(menu, group);
            WidgetSupport.setActivity(menu, activity);
            return null;
        }).when(activity).addStage(menu);

        menu.show(activity, 150, 100, DropdownAlign.TOP_LEFT);

        assertTrue(menu.isShown());
        verify(activity, times(1)).addStage(menu);
        verify(activity, times(1)).addPointerFilter(menu);
        verify(activity, times(1)).addResizeFilter(menu);

        menu.hide();
        assertFalse(menu.isShown());
    }

    @Test
    public void showChangeActivity() {
        Activity activityA = mock(Activity.class);
        when(activityA.getWidth()).thenReturn(800f);
        when(activityA.getHeight()).thenReturn(600f);
        Activity activityB = mock(Activity.class);
        when(activityB.getWidth()).thenReturn(800f);
        when(activityB.getHeight()).thenReturn(600f);
        Group group = mock(Group.class);
        Menu menu = new Menu();

        doAnswer((any) -> {
            WidgetSupport.setGroup(menu, group);
            WidgetSupport.setActivity(menu, activityA);
            return null;
        }).when(activityA).addStage(menu);

        menu.show(activityA, 150, 100, DropdownAlign.TOP_LEFT);

        assertTrue(menu.isShown());
        verify(activityA, times(1)).addStage(menu);
        verify(activityA, times(1)).addPointerFilter(menu);
        verify(activityA, times(1)).addResizeFilter(menu);

        menu.onActivityChange(activityA, activityB);
        assertFalse(menu.isShown());
    }

    @Test
    public void showChangeGroup() {
        Activity activity = mock(Activity.class);
        Group groupA = mock(Group.class);
        Group groupB = mock(Group.class);
        when(activity.getWidth()).thenReturn(800f);
        when(activity.getHeight()).thenReturn(600f);
        Menu menu = new Menu();

        doAnswer((any) -> {
            WidgetSupport.setGroup(menu, groupA);
            WidgetSupport.setActivity(menu, activity);
            return null;
        }).when(activity).addStage(menu);

        menu.show(activity, 150, 100, DropdownAlign.TOP_LEFT);

        assertTrue(menu.isShown());
        verify(activity, times(1)).addStage(menu);
        verify(activity, times(1)).addPointerFilter(menu);
        verify(activity, times(1)).addResizeFilter(menu);

        menu.onGroupChange(groupA, groupB);
        assertFalse(menu.isShown());
    }

    @Test
    public void showHideCascade() {
        Activity activity = mock(Activity.class);
        Group group = mock(Group.class);
        when(activity.getWidth()).thenReturn(800f);
        when(activity.getHeight()).thenReturn(600f);

        Menu menu = new Menu();
        Menu childMenu = new Menu();

        doAnswer((any) -> {
            WidgetSupport.setGroup(menu, group);
            WidgetSupport.setActivity(menu, activity);
            return null;
        }).when(activity).addStage(menu);

        doAnswer((any) -> {
            WidgetSupport.setGroup(childMenu, group);
            WidgetSupport.setActivity(childMenu, activity);
            return null;
        }).when(activity).addStage(childMenu);

        menu.show(activity, 150, 100, DropdownAlign.TOP_LEFT);
        childMenu.show(menu, 150, 100, DropdownAlign.TOP_LEFT);

        assertTrue(menu.isShown());
        assertTrue(childMenu.isShown());
        verify(activity, times(1)).addStage(menu);
        verify(activity, times(1)).addPointerFilter(menu);
        verify(activity, times(1)).addResizeFilter(menu);

        menu.hide();
        assertFalse(menu.isShown());
        assertFalse(childMenu.isShown());
    }

    @Test
    public void showHideChild() {
        Activity activity = mock(Activity.class);
        Group group = mock(Group.class);
        when(activity.getWidth()).thenReturn(800f);
        when(activity.getHeight()).thenReturn(600f);

        Menu menu = new Menu();
        Menu childMenu = new Menu();

        doAnswer((any) -> {
            WidgetSupport.setGroup(menu, group);
            WidgetSupport.setActivity(menu, activity);
            return null;
        }).when(activity).addStage(menu);

        doAnswer((any) -> {
            WidgetSupport.setGroup(childMenu, group);
            WidgetSupport.setActivity(childMenu, activity);
            return null;
        }).when(activity).addStage(childMenu);

        menu.show(activity, 150, 100, DropdownAlign.TOP_LEFT);
        childMenu.show(menu, 150, 100, DropdownAlign.TOP_LEFT);

        assertTrue(menu.isShown());
        assertTrue(childMenu.isShown());
        verify(activity, times(1)).addStage(menu);
        verify(activity, times(1)).addPointerFilter(menu);
        verify(activity, times(1)).addResizeFilter(menu);

        childMenu.hide();
        assertTrue(menu.isShown());
        assertFalse(childMenu.isShown());
    }

    @Test
    public void showCircular() {
        Activity activity = mock(Activity.class);
        Group group = mock(Group.class);
        when(activity.getWidth()).thenReturn(800f);
        when(activity.getHeight()).thenReturn(600f);

        Menu menu = new Menu();

        doAnswer((any) -> {
            WidgetSupport.setGroup(menu, group);
            WidgetSupport.setActivity(menu, activity);
            return null;
        }).when(activity).addStage(menu);

        menu.show(activity, 150, 100, DropdownAlign.TOP_LEFT);
        menu.show(menu, 150, 100, DropdownAlign.TOP_LEFT);

        assertTrue(menu.isShown());
        verify(activity, times(1)).addStage(menu);
        verify(activity, times(1)).addPointerFilter(menu);
        verify(activity, times(1)).addResizeFilter(menu);

        menu.hide();
        assertFalse(menu.isShown());
    }

    @Test
    public void layoutPosition() {
        Activity activity = mock(Activity.class);
        when(activity.getWidth()).thenReturn(800f);
        when(activity.getHeight()).thenReturn(600f);

        Menu menu = new Menu();
        menu.setPrefSize(100, 150);

        menu.show(activity, 200, 300, DropdownAlign.TOP_LEFT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(200, menu.getX(), 0.1f);
        assertEquals(300, menu.getY(), 0.1f);
        menu.hide();

        menu.show(activity, 200, 300, DropdownAlign.TOP_RIGHT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(100, menu.getX(), 0.1f);
        assertEquals(300, menu.getY(), 0.1f);
        menu.hide();

        menu.show(activity, 200, 300, DropdownAlign.BOTTOM_LEFT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(200, menu.getX(), 0.1f);
        assertEquals(150, menu.getY(), 0.1f);
        menu.hide();

        menu.show(activity, 200, 300, DropdownAlign.BOTTOM_RIGHT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(100, menu.getX(), 0.1f);
        assertEquals(150, menu.getY(), 0.1f);
        menu.hide();

        menu.show(activity, 200, 200, DropdownAlign.SCREEN_SPACE);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(200, menu.getX(), 0.1f);
        assertEquals(200, menu.getY(), 0.1f);
        menu.hide();

        menu.show(activity, 600, 400, DropdownAlign.SCREEN_SPACE);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(500, menu.getX(), 0.1f);
        assertEquals(250, menu.getY(), 0.1f);
        menu.hide();

        // Outsite activity
        menu.show(activity, 800, 600, DropdownAlign.TOP_LEFT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(700, menu.getX(), 0.1f);
        assertEquals(450, menu.getY(), 0.1f);
        menu.hide();

        menu.show(activity, 50, 50, DropdownAlign.BOTTOM_RIGHT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(0, menu.getX(), 0.1f);
        assertEquals(0, menu.getY(), 0.1f);
        menu.hide();

        // Margins
        menu.setMargins(1, 2, 3, 4);
        menu.show(activity, 200, 300, DropdownAlign.TOP_LEFT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(196, menu.getX(), 0.1f);
        assertEquals(299, menu.getY(), 0.1f);
        menu.hide();

        menu.show(activity, 200, 300, DropdownAlign.BOTTOM_RIGHT);
        menu.onMeasure();
        menu.onLayout(100, 150);
        assertEquals(96, menu.getX(), 0.1f);
        assertEquals(149, menu.getY(), 0.1f);
        menu.hide();
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("horizontal-align"), new UXValueText(HorizontalAlign.RIGHT.toString()));
        hash.put(UXHash.getHash("showup-transition-duration"), new UXValueNumber(0.25f));
        hash.put(UXHash.getHash("scroll-sensibility"), new UXValueNumber(5));
        hash.put(UXHash.getHash("on-slide"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("on-view-offset-change"), new UXValueText("onViewOffsetWork"));
        return hash;
    }

    private void assertMeasure(Widget widget, float width, float height) {
        assertEquals("Measure Width", width, widget.getMeasureWidth(), 0.1f);
        assertEquals("Measure Height", height, widget.getMeasureHeight(), 0.1f);
    }

    private void assertLayout(Widget widget, float x, float y, float width, float height) {
        assertEquals("X", x, widget.getX(), 0.1f);
        assertEquals("Y", y, widget.getY(), 0.1f);
        assertEquals("Width", width, widget.getLayoutWidth(), 0.1f);
        assertEquals("Height", height, widget.getLayoutHeight(), 0.1f);
    }
}