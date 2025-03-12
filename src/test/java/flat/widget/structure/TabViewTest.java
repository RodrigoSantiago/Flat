package flat.widget.structure;

import flat.graphics.context.Font;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXNode;
import flat.uxml.value.*;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.LineCap;
import flat.widget.enums.VerticalAlign;
import flat.widget.enums.VerticalPosition;
import flat.widget.layout.Frame;
import flat.window.Activity;
import flat.window.ActivitySupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class, Font.class})
public class TabViewTest {

    Font defaultFont;
    @Before
    public void before() {
        mockStatic(Font.class);
        defaultFont = mock(Font.class);
        when(Font.getDefault()).thenReturn(defaultFont);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);
        TabView tab = new TabView();

        assertEquals(HorizontalAlign.LEFT, tab.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, tab.getVerticalAlign());
        assertEquals(HorizontalAlign.LEFT, tab.getTabsHorizontalAlign());
        assertEquals(VerticalAlign.TOP, tab.getTabsVerticalAlign());
        assertEquals(VerticalPosition.TOP, tab.getTabsVerticalPosition());
        assertEquals(0, tab.getTabsPrefHeight(), 0.1f);
        assertEquals(10, tab.getScrollSensibility(), 0.1f);
        assertEquals(0x00000000, tab.getTabsBgColor());
        assertEquals(0, tab.getTabsElevation(), 0.1f);
        assertEquals(0, tab.getLineWidth(), 0.1f);
        assertEquals(0x00000000, tab.getLineColor());
        assertEquals(LineCap.BUTT, tab.getLineCap());
        assertEquals(0, tab.getLineAnimationDuration(), 0.1f);
        assertFalse(tab.isHiddenTabs());

        tab.setAttributes(createNonDefaultValues(), null);
        tab.applyAttributes(controller);

        assertEquals(HorizontalAlign.LEFT, tab.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, tab.getVerticalAlign());
        assertEquals(HorizontalAlign.LEFT, tab.getTabsHorizontalAlign());
        assertEquals(VerticalAlign.TOP, tab.getTabsVerticalAlign());
        assertEquals(VerticalPosition.TOP, tab.getTabsVerticalPosition());
        assertEquals(0, tab.getTabsPrefHeight(), 0.1f);
        assertEquals(10, tab.getScrollSensibility(), 0.1f);
        assertEquals(0x00000000, tab.getTabsBgColor());
        assertEquals(0, tab.getTabsElevation(), 0.1f);
        assertEquals(0, tab.getLineWidth(), 0.1f);
        assertEquals(0x00000000, tab.getLineColor());
        assertEquals(LineCap.BUTT, tab.getLineCap());
        assertEquals(0, tab.getLineAnimationDuration(), 0.1f);
        assertFalse(tab.isHiddenTabs());

        tab.applyStyle();

        assertEquals(HorizontalAlign.CENTER, tab.getHorizontalAlign());
        assertEquals(VerticalAlign.BOTTOM, tab.getVerticalAlign());
        assertEquals(HorizontalAlign.RIGHT, tab.getTabsHorizontalAlign());
        assertEquals(VerticalAlign.MIDDLE, tab.getTabsVerticalAlign());
        assertEquals(VerticalPosition.BOTTOM, tab.getTabsVerticalPosition());
        assertEquals(16, tab.getTabsPrefHeight(), 0.1f);
        assertEquals(5, tab.getScrollSensibility(), 0.1f);
        assertEquals(0xFF0000FF, tab.getTabsBgColor());
        assertEquals(1, tab.getTabsElevation(), 0.1f);
        assertEquals(2, tab.getLineWidth(), 0.1f);
        assertEquals(0x0000FFFF, tab.getLineColor());
        assertEquals(LineCap.ROUND, tab.getLineCap());
        assertEquals(3, tab.getLineAnimationDuration(), 0.1f);
        assertTrue(tab.isHiddenTabs());
    }

    @Test
    public void addSelectTab() {
        Controller controller = mock(Controller.class);
        TabView tab = new TabView();
        Frame frame1 = new Frame();
        Tab tab1 = new Tab();
        tab1.setFrame(frame1);
        Frame frame2 = new Frame();
        Tab tab2 = new Tab();
        tab2.setFrame(frame2);

        assertNull(tab.getSelectedTab());

        tab.selectTab(tab1);
        assertNull(tab.getSelectedTab());

        tab.addTab(tab1);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab.addTab(tab2);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab.selectTab(tab2);
        assertEquals(tab2, tab.getSelectedTab());
        assertEquals(frame2, tab.getContent());
    }

    @Test
    public void removeTab() {
        Controller controller = mock(Controller.class);
        TabView tab = new TabView();
        Frame frame1 = new Frame();
        Tab tab1 = new Tab();
        tab1.setFrame(frame1);
        Frame frame2 = new Frame();
        Tab tab2 = new Tab();
        tab2.setFrame(frame2);

        assertNull(tab.getSelectedTab());
        assertNull(tab.getContent());

        tab.addTab(tab1);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab.addTab(tab2);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab.selectTab(tab2);
        assertEquals(tab2, tab.getSelectedTab());
        assertEquals(frame2, tab.getContent());

        tab.removeTab(tab1);
        assertEquals(tab2, tab.getSelectedTab());
        assertEquals(frame2, tab.getContent());

        tab.removeTab(tab2);
        assertNull(tab.getSelectedTab());
        assertNull(tab.getContent());
    }

    @Test
    public void removeTabBefore() {
        Controller controller = mock(Controller.class);
        TabView tab = new TabView();
        Frame frame1 = new Frame();
        Tab tab1 = new Tab();
        tab1.setFrame(frame1);
        Frame frame2 = new Frame();
        Tab tab2 = new Tab();
        tab2.setFrame(frame2);

        assertNull(tab.getSelectedTab());
        assertNull(tab.getContent());

        tab.addTab(tab1);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab.addTab(tab2);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab.selectTab(tab2);
        assertEquals(tab2, tab.getSelectedTab());
        assertEquals(frame2, tab.getContent());

        tab.removeTab(tab2);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());
    }

    @Test
    public void removeTabFirst() {
        Controller controller = mock(Controller.class);
        TabView tab = new TabView();
        Frame frame1 = new Frame();
        Tab tab1 = new Tab();
        tab1.setFrame(frame1);
        Frame frame2 = new Frame();
        Tab tab2 = new Tab();
        tab2.setFrame(frame2);

        assertNull(tab.getSelectedTab());
        assertNull(tab.getContent());

        tab.addTab(tab1);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab.addTab(tab2);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab.removeTab(tab1);
        assertEquals(tab2, tab.getSelectedTab());
        assertEquals(frame2, tab.getContent());
    }

    @Test
    public void removeContentAndTabManually() {
        Controller controller = mock(Controller.class);
        TabView tab = new TabView();
        Frame frame1 = new Frame();
        Tab tab1 = new Tab();
        tab1.setFrame(frame1);

        assertNull(tab.getSelectedTab());
        assertNull(tab.getContent());

        tab.addTab(tab1);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab1.getParent().remove(tab1);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        frame1.getParent().remove(frame1);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());
    }

    @Test
    public void selectTabOnHideEvent() {
        Activity activity = mock(Activity.class);
        Scene scene = new Scene();
        when(activity.getScene()).thenReturn(scene);
        ActivitySupport.setActivity(scene, activity);

        TabView tab = new TabView();
        scene.add(tab);
        Frame frame1 = new Frame();
        Tab tab1 = new Tab();
        tab1.setFrame(frame1);
        Frame frame2 = new Frame();
        Tab tab2 = new Tab();
        tab2.setFrame(frame2);
        Frame frame3 = new Frame();
        Tab tab3 = new Tab();
        tab3.setFrame(frame3);

        Controller controller1 = new Controller() {
            @Override
            public void onHide() {
                tab.selectTab(tab3);
            }
        };
        Controller controller2 = mock(Controller.class);
        Controller controller3 = mock(Controller.class);
        frame1.setController(controller1);
        frame2.setController(controller2);
        frame3.setController(controller3);

        assertNull(tab.getSelectedTab());
        assertNull(tab.getContent());

        tab.addTab(tab1);
        tab.addTab(tab2);
        tab.addTab(tab3);

        tab.selectTab(tab2);

        assertEquals(tab3, tab.getSelectedTab());
        assertEquals(frame3, tab.getContent());
        verify(controller2, times(0)).onShow();
        verify(controller2, times(0)).onHide();
        verify(controller3, times(1)).onShow();
        verify(controller2, times(0)).onHide();
    }

    @Test
    public void layoutTop() {
        TabView tab = new TabView();
        tab.setVerticalAlign(VerticalAlign.TOP);
        tab.setHorizontalAlign(HorizontalAlign.LEFT);
        tab.setTabsVerticalAlign(VerticalAlign.TOP);
        tab.setTabsHorizontalAlign(HorizontalAlign.LEFT);
        tab.setTabsVerticalPosition(VerticalPosition.TOP);

        Frame frame1 = new Frame();
        Tab tab1 = new Tab();
        tab1.setFrame(frame1);
        tab.addTab(tab1);

        tab.setPrefSize(500, 400);
        tab1.setPrefSize(100, 24);
        frame1.setPrefSize(200, 100);

        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(tab1, 0, 0, 100, 24);
        assertLayout(frame1, 0, 24, 200, 100);

        tab.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        tab.onMeasure();
        assertMeasure(tab, 200, 124);
        tab.onLayout(200, 124);
        assertLayout(tab1, 0, 0, 100, 24);
        assertLayout(frame1, 0, 24, 200, 100);
        tab.setPrefSize(500, 400);

        frame1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(tab1, 0, 0, 100, 24);
        assertLayout(frame1, 0, 24, 500, 376);
        frame1.setPrefSize(200, 100);

        tab1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(tab1, 0, 0, 500, 300);
        assertLayout(frame1, 0, 300, 200, 100);
        tab1.setPrefSize(100, 24);

        frame1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(tab1, 0, 0, 500, 200);
        assertLayout(frame1, 0, 200, 500, 200);

        tab1.setMinHeight(24);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(tab1, 0, 0, 500, 212);
        assertLayout(frame1, 0, 212, 500, 188);

    }

    @Test
    public void layoutBottom() {
        TabView tab = new TabView();
        tab.setVerticalAlign(VerticalAlign.TOP);
        tab.setHorizontalAlign(HorizontalAlign.LEFT);
        tab.setTabsVerticalAlign(VerticalAlign.TOP);
        tab.setTabsHorizontalAlign(HorizontalAlign.LEFT);
        tab.setTabsVerticalPosition(VerticalPosition.BOTTOM);

        Frame frame1 = new Frame();
        Tab tab1 = new Tab();
        tab1.setFrame(frame1);
        tab.addTab(tab1);

        tab.setPrefSize(500, 400);
        tab1.setPrefSize(100, 24);
        frame1.setPrefSize(200, 100);

        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(tab1, 0, 376, 100, 24);
        assertLayout(frame1, 0, 0, 200, 100);

        tab.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        tab.onMeasure();
        assertMeasure(tab, 200, 124);
        tab.onLayout(200, 124);
        assertLayout(tab1, 0, 100, 100, 24);
        assertLayout(frame1, 0, 0, 200, 100);
        tab.setPrefSize(500, 400);

        frame1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(tab1, 0, 376, 100, 24);
        assertLayout(frame1, 0, 0, 500, 376);
        frame1.setPrefSize(200, 100);

        tab1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(tab1, 0, 100, 500, 300);
        assertLayout(frame1, 0, 0, 200, 100);
        tab1.setPrefSize(100, 24);

        frame1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(tab1, 0, 200, 500, 200);
        assertLayout(frame1, 0, 0, 500, 200);

        tab1.setMinHeight(24);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(tab1, 0, 188, 500, 212);
        assertLayout(frame1, 0, 0, 500, 188);

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

        hash.put(UXHash.getHash("horizontal-align"), new UXValueText(HorizontalAlign.CENTER.toString()));
        hash.put(UXHash.getHash("vertical-align"), new UXValueText(VerticalAlign.BOTTOM.toString()));
        hash.put(UXHash.getHash("tabs-horizontal-align"), new UXValueText(HorizontalAlign.RIGHT.toString()));
        hash.put(UXHash.getHash("tabs-vertical-align"), new UXValueText(VerticalAlign.MIDDLE.toString()));
        hash.put(UXHash.getHash("tabs-vertical-position"), new UXValueText(VerticalPosition.BOTTOM.toString()));
        hash.put(UXHash.getHash("tabs-pref-height"), new UXValueSizeDp(16));
        hash.put(UXHash.getHash("tabs-elevation"), new UXValueSizeDp(1));
        hash.put(UXHash.getHash("tabs-bg-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("scroll-sensibility"), new UXValueNumber(5));
        hash.put(UXHash.getHash("hidden-tabs"), new UXValueBool(true));
        hash.put(UXHash.getHash("line-width"), new UXValueSizeDp(2));
        hash.put(UXHash.getHash("line-color"), new UXValueColor(0x0000FFFF));
        hash.put(UXHash.getHash("line-cap"),  new UXValueText(LineCap.ROUND.toString()));
        hash.put(UXHash.getHash("line-animation-duration"),  new UXValueNumber(3));
        return hash;
    }
}