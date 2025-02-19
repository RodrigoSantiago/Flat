package flat.widget.layout;

import flat.events.ActionEvent;
import flat.events.SlideEvent;
import flat.uxml.*;
import flat.uxml.value.*;
import flat.widget.Widget;
import flat.widget.enums.VerticalBarPosition;
import flat.widget.enums.Policy;
import flat.widget.enums.HorizontalBarPosition;
import flat.widget.value.HorizontalScrollBar;
import flat.widget.value.ScrollBar;
import flat.widget.value.VerticalScrollBar;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class ScrollBoxTest {

    Controller controller;
    UXBuilder builder;

    @Before
    public void before() {
        controller = mock(Controller.class);
        builder = mock(UXBuilder.class);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        var slideHorizontal = (UXListener<SlideEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onSlideHorizontalWork", SlideEvent.class)).thenReturn(slideHorizontal);

        var slideVertical = (UXListener<SlideEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onSlideVerticalWork", SlideEvent.class)).thenReturn(slideVertical);

        var filterHorizontal = (UXListener<SlideEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onFilterHorizontalWork", SlideEvent.class)).thenReturn(filterHorizontal);

        var filterVertical = (UXListener<SlideEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onFilterVerticalWork", SlideEvent.class)).thenReturn(filterVertical);

        var listenerx = (UXValueListener<Float>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onViewOffsetXWork", Float.class)).thenReturn(listenerx);

        var listenery = (UXValueListener<Float>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onViewOffsetYWork", Float.class)).thenReturn(listenery);

        ScrollBox scrollBox = new ScrollBox();

        assertEquals(Policy.AS_NEEDED, scrollBox.getHorizontalPolicy());
        assertEquals(Policy.AS_NEEDED, scrollBox.getVerticalPolicy());
        assertEquals(HorizontalBarPosition.BOTTOM, scrollBox.getHorizontalBarPosition());
        assertEquals(VerticalBarPosition.RIGHT, scrollBox.getVerticalBarPosition());
        assertEquals(10f, scrollBox.getScrollSensibility(), 0.0001f);
        assertFalse(scrollBox.isFloatingBars());
        assertNull(scrollBox.getSlideHorizontalFilter());
        assertNull(scrollBox.getSlideVerticalFilter());
        assertNull(scrollBox.getSlideHorizontalListener());
        assertNull(scrollBox.getSlideVerticalListener());
        assertNull(scrollBox.getViewOffsetXListener());
        assertNull(scrollBox.getViewOffsetYListener());

        scrollBox.setAttributes(createNonDefaultValues(), "scroll-box");
        scrollBox.applyAttributes(controller);

        assertEquals(Policy.AS_NEEDED, scrollBox.getHorizontalPolicy());
        assertEquals(Policy.AS_NEEDED, scrollBox.getVerticalPolicy());
        assertEquals(HorizontalBarPosition.BOTTOM, scrollBox.getHorizontalBarPosition());
        assertEquals(VerticalBarPosition.RIGHT, scrollBox.getVerticalBarPosition());
        assertEquals(10f, scrollBox.getScrollSensibility(), 0.0001f);
        assertFalse(scrollBox.isFloatingBars());
        assertEquals(filterHorizontal, scrollBox.getSlideHorizontalFilter());
        assertEquals(filterVertical, scrollBox.getSlideVerticalFilter());
        assertEquals(slideHorizontal, scrollBox.getSlideHorizontalListener());
        assertEquals(slideVertical, scrollBox.getSlideVerticalListener());
        assertEquals(listenerx, scrollBox.getViewOffsetXListener());
        assertEquals(listenery, scrollBox.getViewOffsetYListener());

        scrollBox.applyStyle();

        assertEquals(Policy.ALWAYS, scrollBox.getHorizontalPolicy());
        assertEquals(Policy.ALWAYS, scrollBox.getVerticalPolicy());
        assertEquals(HorizontalBarPosition.TOP, scrollBox.getHorizontalBarPosition());
        assertEquals(VerticalBarPosition.LEFT, scrollBox.getVerticalBarPosition());
        assertEquals(5f, scrollBox.getScrollSensibility(), 0.0001f);
        assertTrue(scrollBox.isFloatingBars());
        assertEquals(filterHorizontal, scrollBox.getSlideHorizontalFilter());
        assertEquals(filterVertical, scrollBox.getSlideVerticalFilter());
        assertEquals(slideHorizontal, scrollBox.getSlideHorizontalListener());
        assertEquals(slideVertical, scrollBox.getSlideVerticalListener());
        assertEquals(listenerx, scrollBox.getViewOffsetXListener());
        assertEquals(listenery, scrollBox.getViewOffsetYListener());
    }

    @Test
    public void children() {
        ScrollBox scrollBox = new ScrollBox();

        HorizontalScrollBar horBar = new HorizontalScrollBar();
        horBar.setId("hor-bar-id");
        VerticalScrollBar verBar = new VerticalScrollBar();
        verBar.setId("ver-bar-id");
        Panel content = new Panel();

        UXChildren uxChild = mock(UXChildren.class);
        when(uxChild.next()).thenReturn(horBar).thenReturn(verBar).thenReturn(content).thenReturn(null);

        assertNull(horBar.getParent());
        assertNull(verBar.getParent());
        assertNull(content.getParent());

        scrollBox.setAttributes(createBarValues(), "scrollBox");
        scrollBox.applyChildren(uxChild);
        scrollBox.applyAttributes(controller);

        assertEquals(scrollBox, horBar.getParent());
        assertEquals(scrollBox, verBar.getParent());
        assertEquals(scrollBox, content.getParent());

        assertEquals(horBar, scrollBox.getHorizontalBar());
        assertEquals(verBar, scrollBox.getVerticalBar());
    }

    @Test
    public void measure() {
        ScrollBox scrollBox = new ScrollBox();
        Panel content = new Panel();
        content.setPrefSize(500, 350);
        scrollBox.add(content);
        scrollBox.onMeasure();

        assertEquals(500, scrollBox.getMeasureWidth(), 0.1f);
        assertEquals(350, scrollBox.getMeasureHeight(), 0.1f);

        scrollBox.setMargins(1, 2, 3, 4);
        scrollBox.setPadding(5, 4, 2, 3);
        scrollBox.onMeasure();

        assertEquals(500 + 13, scrollBox.getMeasureWidth(), 0.1f);
        assertEquals(350 + 11, scrollBox.getMeasureHeight(), 0.1f);

        scrollBox.setPrefSize(100, 200);
        scrollBox.onMeasure();

        assertEquals(100 + 6, scrollBox.getMeasureWidth(), 0.1f);
        assertEquals(200 + 4, scrollBox.getMeasureHeight(), 0.1f);

        scrollBox.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scrollBox.onMeasure();

        assertEquals(Widget.MATCH_PARENT, scrollBox.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, scrollBox.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureBars() {
        ScrollBox scrollBox = new ScrollBox();
        HorizontalScrollBar horBar = new HorizontalScrollBar();
        VerticalScrollBar verBar = new VerticalScrollBar();
        Panel content = new Panel();
        horBar.setPrefSize(Widget.MATCH_PARENT, 16);
        verBar.setPrefSize(16, Widget.MATCH_PARENT);
        content.setPrefSize(500, 350);
        scrollBox.add(content);
        scrollBox.setHorizontalBar(horBar);
        scrollBox.setVerticalBar(verBar);

        assertEquals(horBar, scrollBox.getHorizontalBar());
        assertEquals(verBar, scrollBox.getVerticalBar());

        scrollBox.onMeasure();

        assertEquals(500, scrollBox.getMeasureWidth(), 0.1f);
        assertEquals(350, scrollBox.getMeasureHeight(), 0.1f);

        scrollBox.setMargins(1, 2, 3, 4);
        scrollBox.setPadding(5, 4, 2, 3);
        scrollBox.onMeasure();

        assertEquals(500 + 13, scrollBox.getMeasureWidth(), 0.1f);
        assertEquals(350 + 11, scrollBox.getMeasureHeight(), 0.1f);

        scrollBox.setPrefSize(100, 200);
        scrollBox.onMeasure();

        assertEquals(100 + 6, scrollBox.getMeasureWidth(), 0.1f);
        assertEquals(200 + 4, scrollBox.getMeasureHeight(), 0.1f);

        scrollBox.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scrollBox.onMeasure();

        assertEquals(Widget.MATCH_PARENT, scrollBox.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, scrollBox.getMeasureHeight(), 0.1f);
    }

    @Test
    public void positionBar() {
        ScrollBox scrollBox = new ScrollBox();
        HorizontalScrollBar horBar = new HorizontalScrollBar();
        horBar.setId("horBar");
        VerticalScrollBar verBar = new VerticalScrollBar();
        verBar.setId("verBar");
        Panel content = new Panel();
        horBar.setPrefSize(Widget.MATCH_PARENT, 16);
        verBar.setPrefSize(18, Widget.MATCH_PARENT);
        content.setPrefSize(500, 350);
        scrollBox.add(content);
        scrollBox.setHorizontalBar(horBar);
        scrollBox.setVerticalBar(verBar);

        assertEquals(horBar, scrollBox.getHorizontalBar());
        assertEquals(verBar, scrollBox.getVerticalBar());

        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(250, 150);
        assertLayout(scrollBox, 0, 0, 250, 150);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(250 - 18, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(150 - 16, scrollBox.getViewDimensionY(), 0.1f);

        assertLayout(horBar, 0, 150 - 16, 250 - 18, 16);
        assertLayout(verBar, 250 - 18, 0, 18, 150);

        scrollBox.setVerticalBarPosition(VerticalBarPosition.LEFT);
        scrollBox.setHorizontalBarPosition(HorizontalBarPosition.TOP);

        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(250, 150);
        assertLayout(scrollBox, 0, 0, 250, 150);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(250 - 18, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(150 - 16, scrollBox.getViewDimensionY(), 0.1f);

        assertLayout(horBar, 18, 0, 250 - 18, 16);
        assertLayout(verBar, 0, 0, 18, 150);
    }

    @Test
    public void layout() {
        ScrollBox scrollBox = new ScrollBox();
        HorizontalScrollBar horBar = new HorizontalScrollBar();
        VerticalScrollBar verBar = new VerticalScrollBar();
        Panel content = new Panel();
        horBar.setPrefSize(Widget.MATCH_PARENT, 16);
        verBar.setPrefSize(16, Widget.MATCH_PARENT);
        content.setPrefSize(500, 350);
        scrollBox.add(content);
        scrollBox.setHorizontalBar(horBar);
        scrollBox.setVerticalBar(verBar);

        assertEquals(horBar, scrollBox.getHorizontalBar());
        assertEquals(verBar, scrollBox.getVerticalBar());

        // Same Size
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(500, 350);
        assertLayout(scrollBox, 0, 0, 500, 350);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(500, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getViewDimensionY(), 0.1f);

        // Horizontal only Scrollable
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(250, 400);
        assertLayout(scrollBox, 0, 0, 250, 400);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(400 - 16, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(250, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(400 - 16, scrollBox.getViewDimensionY(), 0.1f);

        // Vertical only Scrollable
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(600, 150);
        assertLayout(scrollBox, 0, 0, 600, 150);
        assertEquals(600 - 16, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(600 - 16, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(150, scrollBox.getViewDimensionY(), 0.1f);

        // Horizontal only Scrollable. Hidden bar
        scrollBox.setHorizontalPolicy(Policy.NEVER);
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(250, 400);
        assertLayout(scrollBox, 0, 0, 250, 400);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(400, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(250, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(400, scrollBox.getViewDimensionY(), 0.1f);
        scrollBox.setHorizontalPolicy(Policy.AS_NEEDED);

        // Vertical only Scrollable. Hidden bar
        scrollBox.setVerticalPolicy(Policy.NEVER);
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(600, 150);
        assertLayout(scrollBox, 0, 0, 600, 150);
        assertEquals(600, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(600, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(150, scrollBox.getViewDimensionY(), 0.1f);
        scrollBox.setVerticalPolicy(Policy.AS_NEEDED);

        // Both Scrollable
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(250, 150);
        assertLayout(scrollBox, 0, 0, 250, 150);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(250 - 16, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(150 - 16, scrollBox.getViewDimensionY(), 0.1f);

        // Both Scrollable. Hidden Bars
        scrollBox.setVerticalPolicy(Policy.NEVER);
        scrollBox.setHorizontalPolicy(Policy.NEVER);
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(250, 150);
        assertLayout(scrollBox, 0, 0, 250, 150);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(250, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(150, scrollBox.getViewDimensionY(), 0.1f);
        scrollBox.setVerticalPolicy(Policy.AS_NEEDED);
        scrollBox.setHorizontalPolicy(Policy.AS_NEEDED);

        // Same Size. Hor size poke vertical bar
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(400, 350);
        assertLayout(scrollBox, 0, 0, 400, 350);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(400 - 16, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(350 - 16, scrollBox.getViewDimensionY(), 0.1f);

        // Same Size. Ver size poke horizontal bar
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(500, 300);
        assertLayout(scrollBox, 0, 0, 500, 300);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(500 - 16, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(300 - 16, scrollBox.getViewDimensionY(), 0.1f);
    }

    @Test
    public void layoutFloating() {
        ScrollBox scrollBox = new ScrollBox();
        HorizontalScrollBar horBar = new HorizontalScrollBar();
        VerticalScrollBar verBar = new VerticalScrollBar();
        Panel content = new Panel();
        horBar.setPrefSize(Widget.MATCH_PARENT, 16);
        verBar.setPrefSize(16, Widget.MATCH_PARENT);
        content.setPrefSize(500, 350);
        scrollBox.add(content);
        scrollBox.setHorizontalBar(horBar);
        scrollBox.setVerticalBar(verBar);
        scrollBox.setFloatingBars(true);

        assertEquals(horBar, scrollBox.getHorizontalBar());
        assertEquals(verBar, scrollBox.getVerticalBar());

        // Same Size
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(500, 350);
        assertLayout(scrollBox, 0, 0, 500, 350);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(500, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getViewDimensionY(), 0.1f);

        // Horizontal only Scrollable
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(250, 400);
        assertLayout(scrollBox, 0, 0, 250, 400);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(400, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(250, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(400, scrollBox.getViewDimensionY(), 0.1f);

        // Vertical only Scrollable
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(600, 150);
        assertLayout(scrollBox, 0, 0, 600, 150);
        assertEquals(600, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(600, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(150, scrollBox.getViewDimensionY(), 0.1f);

        // Horizontal only Scrollable. Hidden bar
        scrollBox.setHorizontalPolicy(Policy.NEVER);
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(250, 400);
        assertLayout(scrollBox, 0, 0, 250, 400);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(400, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(250, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(400, scrollBox.getViewDimensionY(), 0.1f);
        scrollBox.setHorizontalPolicy(Policy.AS_NEEDED);

        // Vertical only Scrollable. Hidden bar
        scrollBox.setVerticalPolicy(Policy.NEVER);
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(600, 150);
        assertLayout(scrollBox, 0, 0, 600, 150);
        assertEquals(600, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(600, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(150, scrollBox.getViewDimensionY(), 0.1f);
        scrollBox.setVerticalPolicy(Policy.AS_NEEDED);

        // Both Scrollable
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(250, 150);
        assertLayout(scrollBox, 0, 0, 250, 150);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(250, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(150, scrollBox.getViewDimensionY(), 0.1f);

        // Both Scrollable. Hidden Bars
        scrollBox.setVerticalPolicy(Policy.NEVER);
        scrollBox.setHorizontalPolicy(Policy.NEVER);
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(250, 150);
        assertLayout(scrollBox, 0, 0, 250, 150);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(250, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(150, scrollBox.getViewDimensionY(), 0.1f);
        scrollBox.setVerticalPolicy(Policy.AS_NEEDED);
        scrollBox.setHorizontalPolicy(Policy.AS_NEEDED);

        // Same Size. Hor size poke vertical bar
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(400, 350);
        assertLayout(scrollBox, 0, 0, 400, 350);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(400, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getViewDimensionY(), 0.1f);

        // Same Size. Ver size poke horizontal bar
        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(500, 300);
        assertLayout(scrollBox, 0, 0, 500, 300);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(500, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(300, scrollBox.getViewDimensionY(), 0.1f);
    }

    @Test
    public void fireAction() {
        ScrollBox scrollBox = new ScrollBox();
        HorizontalScrollBar horBar = new HorizontalScrollBar();
        VerticalScrollBar verBar = new VerticalScrollBar();
        Panel content = new Panel();
        horBar.setPrefSize(Widget.MATCH_PARENT, 16);
        verBar.setPrefSize(18, Widget.MATCH_PARENT);
        content.setPrefSize(500, 350);
        scrollBox.add(content);
        scrollBox.setHorizontalBar(horBar);
        scrollBox.setVerticalBar(verBar);

        assertEquals(horBar, scrollBox.getHorizontalBar());
        assertEquals(verBar, scrollBox.getVerticalBar());

        scrollBox.onMeasure();
        assertMeasure(scrollBox, 500, 350);
        scrollBox.onLayout(250, 150);
        assertLayout(scrollBox, 0, 0, 250, 150);
        assertEquals(500, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(350, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(232, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(134, scrollBox.getViewDimensionY(), 0.1f);

        assertLayout(horBar, 0, 150 - 16, 250 - 18, 16);
        assertLayout(verBar, 250 - 18, 0, 18, 150);

        var slideHorizontal = (UXListener<SlideEvent>) mock(UXListener.class);
        scrollBox.setSlideHorizontalListener(slideHorizontal);

        var slideVertical = (UXListener<SlideEvent>) mock(UXListener.class);
        scrollBox.setSlideVerticalListener(slideVertical);

        var filterHorizontal = (UXListener<SlideEvent>) mock(UXListener.class);
        scrollBox.setSlideHorizontalFilter(filterHorizontal);

        var filterVertical = (UXListener<SlideEvent>) mock(UXListener.class);
        scrollBox.setSlideVerticalFilter(filterVertical);

        var listenerx = (UXValueListener<Float>) mock(UXValueListener.class);
        scrollBox.setViewOffsetXListener(listenerx);

        var listenery = (UXValueListener<Float>) mock(UXValueListener.class);
        scrollBox.setViewOffsetYListener(listenery);

        scrollBox.slideTo(0, 0);
        assertEquals(0, scrollBox.getViewOffsetX(), 0.1f);
        assertEquals(0, scrollBox.getViewOffsetY(), 0.1f);

        scrollBox.slideTo(10, 32);
        assertEquals(10, scrollBox.getViewOffsetX(), 0.1f);
        assertEquals(32, scrollBox.getViewOffsetY(), 0.1f);

        horBar.slideTo(0);
        verBar.slideTo(0);

        horBar.setViewOffset(10);
        verBar.setViewOffset(32);

        scrollBox.slideTo(400, 500);
        assertEquals(268, scrollBox.getViewOffsetX(), 0.1f);
        assertEquals(216, scrollBox.getViewOffsetY(), 0.1f);

        scrollBox.slideTo(400, 500);
        assertEquals(268, scrollBox.getViewOffsetX(), 0.1f);
        assertEquals(216, scrollBox.getViewOffsetY(), 0.1f);

        content.setPrefSize(200, 120);
        scrollBox.onMeasure();
        scrollBox.onLayout(100, 80);
        assertEquals(200, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(120, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(82, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(64, scrollBox.getViewDimensionY(), 0.1f);

        assertEquals(118, scrollBox.getViewOffsetX(), 0.1f);
        assertEquals(56, scrollBox.getViewOffsetY(), 0.1f);
        verify(listenerx, times(3)).handle(any());
        verify(listenery, times(3)).handle(any());

        content.setPrefSize(200, 120);
        scrollBox.onMeasure();
        scrollBox.onLayout(200, 120);
        assertEquals(200, scrollBox.getTotalDimensionX(), 0.1f);
        assertEquals(120, scrollBox.getTotalDimensionY(), 0.1f);
        assertEquals(200, scrollBox.getViewDimensionX(), 0.1f);
        assertEquals(120, scrollBox.getViewDimensionY(), 0.1f);

        scrollBox.setViewOffsetX(scrollBox.getViewOffsetX());
        scrollBox.setViewOffsetY(scrollBox.getViewOffsetY());

        verify(slideHorizontal, times(3)).handle(any());
        verify(slideVertical, times(3)).handle(any());
        verify(filterHorizontal, times(3)).handle(any());
        verify(filterVertical, times(3)).handle(any());
        verify(listenerx, times(5)).handle(any());
        verify(listenery, times(5)).handle(any());
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

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("on-slide-horizontal"), new UXValueText("onSlideHorizontalWork"));
        hash.put(UXHash.getHash("on-slide-vertical"), new UXValueText("onSlideVerticalWork"));
        hash.put(UXHash.getHash("on-slide-horizontal-filter"), new UXValueText("onFilterHorizontalWork"));
        hash.put(UXHash.getHash("on-slide-vertical-filter"), new UXValueText("onFilterVerticalWork"));
        hash.put(UXHash.getHash("on-view-offset-x-change"), new UXValueText("onViewOffsetXWork"));
        hash.put(UXHash.getHash("on-view-offset-y-change"), new UXValueText("onViewOffsetYWork"));

        hash.put(UXHash.getHash("view-offset-x"), new UXValueNumber(100));
        hash.put(UXHash.getHash("view-offset-y"), new UXValueNumber(120));
        hash.put(UXHash.getHash("horizontal-policy"), new UXValueText(Policy.ALWAYS.toString()));
        hash.put(UXHash.getHash("vertical-policy"), new UXValueText(Policy.ALWAYS.toString()));
        hash.put(UXHash.getHash("horizontal-bar-position"), new UXValueText(HorizontalBarPosition.TOP.toString()));
        hash.put(UXHash.getHash("vertical-bar-position"), new UXValueText(VerticalBarPosition.LEFT.toString()));
        hash.put(UXHash.getHash("scroll-sensibility"), new UXValueNumber(5));
        hash.put(UXHash.getHash("floating-bars"), new UXValueBool(true));
        return hash;
    }

    private HashMap<Integer, UXValue> createBarValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("horizontal-bar-id"), new UXValueText("hor-bar-id"));
        hash.put(UXHash.getHash("vertical-bar-id"), new UXValueText("ver-bar-id"));
        return hash;
    }
}