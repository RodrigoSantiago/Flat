package flat.widget.value;

import flat.events.ActionEvent;
import flat.events.SlideEvent;
import flat.uxml.*;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueColor;
import flat.uxml.value.UXValueNumber;
import flat.uxml.value.UXValueText;
import flat.widget.Widget;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class HorizontalScrollBarTest {

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

        var action = (UXListener<SlideEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", SlideEvent.class)).thenReturn(action);

        var listener = (UXValueListener<Float>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onViewOffsetWork", Float.class)).thenReturn(listener);

        HorizontalScrollBar scrollBar = new HorizontalScrollBar();

        assertEquals(0, scrollBar.getTotalDimension(), 0.1f);
        assertEquals(0, scrollBar.getViewDimension(), 0.1f);
        assertEquals(0, scrollBar.getViewOffset(), 0.1f);
        assertEquals(0f, scrollBar.getMinSize(), 0.0001f);
        assertEquals(0xFFFFFFFF, scrollBar.getColor());
        assertNull(scrollBar.getSlideListener());
        assertNull(scrollBar.getViewOffsetListener());

        scrollBar.setAttributes(createNonDefaultValues(), "scrollbar");
        scrollBar.applyAttributes(controller);

        assertEquals(200, scrollBar.getTotalDimension(), 0.1f);
        assertEquals(50, scrollBar.getViewDimension(), 0.1f);
        assertEquals(100, scrollBar.getViewOffset(), 0.1f);
        assertEquals(0f, scrollBar.getMinSize(), 0.0001f);
        assertEquals(0xFFFFFFFF, scrollBar.getColor());
        assertEquals(action, scrollBar.getSlideListener());
        assertEquals(listener, scrollBar.getViewOffsetListener());

        scrollBar.applyStyle();

        assertEquals(200, scrollBar.getTotalDimension(), 0.1f);
        assertEquals(50, scrollBar.getViewDimension(), 0.1f);
        assertEquals(100, scrollBar.getViewOffset(), 0.1f);
        assertEquals(0.1f, scrollBar.getMinSize(), 0.0001f);
        assertEquals(0xFF0000FF, scrollBar.getColor());
        assertEquals(action, scrollBar.getSlideListener());
        assertEquals(listener, scrollBar.getViewOffsetListener());
    }

    @Test
    public void measure() {
        HorizontalScrollBar scrollBar = new HorizontalScrollBar();
        scrollBar.setTotalDimension(200);
        scrollBar.setViewDimension(50);
        scrollBar.setViewOffset(100);
        scrollBar.onMeasure();

        assertEquals(0, scrollBar.getMeasureWidth(), 0.1f);
        assertEquals(0, scrollBar.getMeasureHeight(), 0.1f);

        scrollBar.setMargins(1, 2, 3, 4);
        scrollBar.setPadding(5, 4, 2, 3);
        scrollBar.onMeasure();

        assertEquals(13, scrollBar.getMeasureWidth(), 0.1f);
        assertEquals(11, scrollBar.getMeasureHeight(), 0.1f);

        scrollBar.setPrefSize(100, 200);
        scrollBar.onMeasure();

        assertEquals(100 + 6, scrollBar.getMeasureWidth(), 0.1f);
        assertEquals(200 + 4, scrollBar.getMeasureHeight(), 0.1f);

        scrollBar.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scrollBar.onMeasure();

        assertEquals(Widget.MATCH_PARENT, scrollBar.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, scrollBar.getMeasureHeight(), 0.1f);
    }

    @Test
    public void fireAction() {
        HorizontalScrollBar scrollBar = new HorizontalScrollBar();
        scrollBar.setTotalDimension(200);
        scrollBar.setViewDimension(50);
        scrollBar.setViewOffset(100);

        var action = (UXListener<SlideEvent>) mock(UXListener.class);
        scrollBar.setSlideListener(action);

        var listener = (UXValueListener<Float>) mock(UXValueListener.class);
        scrollBar.setViewOffsetListener(listener);

        scrollBar.slideTo(0);
        assertEquals(0, scrollBar.getViewOffset(), 0.1f);

        scrollBar.slideTo(200);
        assertEquals(150, scrollBar.getViewOffset(), 0.1f);

        scrollBar.slideTo(200);
        assertEquals(150, scrollBar.getViewOffset(), 0.1f);

        scrollBar.setViewDimension(60);
        assertEquals(140, scrollBar.getViewOffset(), 0.1f);

        scrollBar.setViewDimension(50);
        assertEquals(140, scrollBar.getViewOffset(), 0.1f);

        scrollBar.setTotalDimension(100);
        assertEquals(50, scrollBar.getViewOffset(), 0.1f);

        verify(action, times(2)).handle(any());
        verify(listener, times(4)).handle(any());

    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("on-slide"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("on-view-offset-change"), new UXValueText("onViewOffsetWork"));
        hash.put(UXHash.getHash("total-dimension"), new UXValueNumber(200));
        hash.put(UXHash.getHash("view-dimension"), new UXValueNumber(50));
        hash.put(UXHash.getHash("view-offset"), new UXValueNumber(100));
        hash.put(UXHash.getHash("min-size"), new UXValueNumber(0.1f));
        hash.put(UXHash.getHash("color"), new UXValueColor(0xFF0000FF));
        return hash;
    }
}