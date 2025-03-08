package flat.widget.value;

import flat.events.SlideEvent;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.uxml.value.*;
import flat.widget.Widget;
import flat.widget.enums.Direction;
import flat.widget.enums.ImageFilter;
import flat.widget.enums.LineCap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DrawableReader.class})
public class SliderTest {

    Controller controller;
    UXBuilder builder;

    ResourceStream resIcon;
    Drawable icon;

    @Before
    public void before() {
        controller = mock(Controller.class);
        builder = mock(UXBuilder.class);

        mockStatic(DrawableReader.class);
        icon = mock(Drawable.class);
        when(icon.getWidth()).thenReturn(24f);
        when(icon.getHeight()).thenReturn(16f);

        resIcon = mock(ResourceStream.class);
        when(DrawableReader.parse(resIcon)).thenReturn(icon);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        var change = (UXValueListener<Float>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onValueChange", Float.class)).thenReturn(change);

        var listener = (UXListener<SlideEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onSlideWork", SlideEvent.class)).thenReturn(listener);

        var filter = (UXListener<SlideEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onSlideFilter", SlideEvent.class)).thenReturn(filter);

        Slider slider = new Slider();

        assertEquals(1, slider.getMaxValue(), 0.001f);
        assertEquals(0, slider.getMinValue(), 0.001f);
        assertEquals(0, slider.getValue(), 0.001f);
        assertEquals(0, slider.getSteps(), 0.001f);
        assertNull(slider.getIcon());
        assertEquals(ImageFilter.LINEAR, slider.getIconImageFilter());
        assertEquals(0, slider.getIconWidth(), 0.001f);
        assertEquals(0, slider.getIconHeight(), 0.001f);
        assertEquals(0x00000000, slider.getIconBgColor());
        assertEquals(0xFFFFFFFF, slider.getIconColor());
        assertEquals(1, slider.getLineWidth(), 0.001f);
        assertEquals(LineCap.BUTT, slider.getLineCap());
        assertEquals(0xFFFFFFFF, slider.getLineColor());
        assertEquals(0x000000FF, slider.getLineFilledColor());
        assertEquals(Direction.HORIZONTAL, slider.getDirection());
        assertNull(slider.getSlideListener());
        assertNull(slider.getSlideFilter());
        assertNull(slider.getValueListener());

        slider.setAttributes(createNonDefaultValues(), null);
        slider.applyAttributes(controller);

        assertEquals(20, slider.getMaxValue(), 0.001f);
        assertEquals(10, slider.getMinValue(), 0.001f);
        assertEquals(15, slider.getValue(), 0.001f);
        assertEquals(5, slider.getSteps(), 0.001f);
        assertNull(slider.getIcon());
        assertEquals(ImageFilter.LINEAR, slider.getIconImageFilter());
        assertEquals(0, slider.getIconWidth(), 0.001f);
        assertEquals(0, slider.getIconHeight(), 0.001f);
        assertEquals(0x00000000, slider.getIconBgColor());
        assertEquals(0xFFFFFFFF, slider.getIconColor());
        assertEquals(1, slider.getLineWidth(), 0.001f);
        assertEquals(LineCap.BUTT, slider.getLineCap());
        assertEquals(0xFFFFFFFF, slider.getLineColor());
        assertEquals(0x000000FF, slider.getLineFilledColor());
        assertEquals(Direction.HORIZONTAL, slider.getDirection());
        assertEquals(listener, slider.getSlideListener());
        assertEquals(filter, slider.getSlideFilter());
        assertEquals(change, slider.getValueListener());

        slider.applyStyle();

        assertEquals(20, slider.getMaxValue(), 0.001f);
        assertEquals(10, slider.getMinValue(), 0.001f);
        assertEquals(15, slider.getValue(), 0.001f);
        assertEquals(5, slider.getSteps(), 0.001f);
        assertEquals(icon, slider.getIcon());
        assertEquals(ImageFilter.NEAREST, slider.getIconImageFilter());
        assertEquals(18, slider.getIconWidth(), 0.001f);
        assertEquals(20, slider.getIconHeight(), 0.001f);
        assertEquals(0xFF00FFFF, slider.getIconBgColor());
        assertEquals(0xFF0000FF, slider.getIconColor());
        assertEquals(3, slider.getLineWidth(), 0.001f);
        assertEquals(LineCap.ROUND, slider.getLineCap());
        assertEquals(0x00FFFFFF, slider.getLineColor());
        assertEquals(0x00FF00FF, slider.getLineFilledColor());
        assertEquals(Direction.VERTICAL, slider.getDirection());
        assertEquals(listener, slider.getSlideListener());
        assertEquals(filter, slider.getSlideFilter());
        assertEquals(change, slider.getValueListener());
    }

    @Test
    public void measureHorizontal() {
        Slider slider = new Slider();
        slider.setIconWidth(16);
        slider.setIconHeight(18);
        slider.setIcon(icon);
        slider.setDirection(Direction.HORIZONTAL);
        slider.onMeasure();

        assertEquals(32, slider.getMeasureWidth(), 0.1f);
        assertEquals(18, slider.getMeasureHeight(), 0.1f);

        slider.setMargins(1, 2, 3, 4);
        slider.setPadding(5, 4, 2, 3);
        slider.onMeasure();

        assertEquals(32 + 13, slider.getMeasureWidth(), 0.1f);
        assertEquals(18 + 11, slider.getMeasureHeight(), 0.1f);

        slider.setPrefSize(100, 200);
        slider.onMeasure();

        assertEquals(100 + 6, slider.getMeasureWidth(), 0.1f);
        assertEquals(200 + 4, slider.getMeasureHeight(), 0.1f);

        slider.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        slider.onMeasure();

        assertEquals(Widget.MATCH_PARENT, slider.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, slider.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureVertical() {
        Slider slider = new Slider();
        slider.setIconWidth(16);
        slider.setIconHeight(18);
        slider.setIcon(icon);
        slider.setDirection(Direction.VERTICAL);
        slider.onMeasure();

        assertEquals(16, slider.getMeasureWidth(), 0.1f);
        assertEquals(36, slider.getMeasureHeight(), 0.1f);

        slider.setMargins(1, 2, 3, 4);
        slider.setPadding(5, 4, 2, 3);
        slider.onMeasure();

        assertEquals(16 + 13, slider.getMeasureWidth(), 0.1f);
        assertEquals(36 + 11, slider.getMeasureHeight(), 0.1f);

        slider.setPrefSize(100, 200);
        slider.onMeasure();

        assertEquals(100 + 6, slider.getMeasureWidth(), 0.1f);
        assertEquals(200 + 4, slider.getMeasureHeight(), 0.1f);

        slider.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        slider.onMeasure();

        assertEquals(Widget.MATCH_PARENT, slider.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, slider.getMeasureHeight(), 0.1f);
    }

    @Test
    public void fireSlide() {
        Slider slider = new Slider();
        slider.setIconWidth(16);
        slider.setIconHeight(18);
        slider.setIcon(icon);
        slider.setRangeLimits(-5, 5);
        slider.setValue(0);
        slider.setDirection(Direction.HORIZONTAL);

        var slideListener = (UXListener<SlideEvent>) mock(UXListener.class);
        var slideFilter = (UXListener<SlideEvent>) mock(UXListener.class);
        var valueListener = (UXValueListener<Float>) mock(UXValueListener.class);
        slider.setSlideListener(slideListener);
        slider.setSlideFilter(slideFilter);
        slider.setValueListener(valueListener);

        assertEquals(0, slider.getValue(), 0.001f);
        slider.slideTo(3);
        assertEquals(3, slider.getValue(), 0.001f);
        slider.slideTo(-10);
        assertEquals(-5, slider.getValue(), 0.001f);
        slider.slideTo(10);
        assertEquals(5, slider.getValue(), 0.001f);
        slider.slideTo(10);
        assertEquals(5, slider.getValue(), 0.001f);

        doAnswer(invocation -> {
            SlideEvent event = invocation.getArgument(0);
            event.consume();
            return null;
        }).when(slideFilter).handle(any(SlideEvent.class));

        slider.slideTo(0);
        assertEquals(5, slider.getValue(), 0.001f);
        slider.setValue(1);
        slider.setValue(2);

        verify(slideListener, times(3)).handle(any());
        verify(slideFilter, times(4)).handle(any());
        verify(valueListener, times(5)).handle(any());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxIconActive = mock(UXValue.class);
        when(uxIconActive.asResource(any())).thenReturn(resIcon);

        hash.put(UXHash.getHash("icon"), uxIconActive);

        hash.put(UXHash.getHash("on-slide"), new UXValueText("onSlideWork"));
        hash.put(UXHash.getHash("on-slide-filter"), new UXValueText("onSlideFilter"));
        hash.put(UXHash.getHash("on-value-change"), new UXValueText("onValueChange"));

        hash.put(UXHash.getHash("max-value"), new UXValueNumber(20));
        hash.put(UXHash.getHash("min-value"), new UXValueNumber(10));
        hash.put(UXHash.getHash("value"), new UXValueNumber(15));
        hash.put(UXHash.getHash("steps"), new UXValueNumber(5));

        hash.put(UXHash.getHash("color"), new UXValueColor(0xFF0000FF));

        hash.put(UXHash.getHash("direction"), new UXValueText(Direction.VERTICAL.toString()));
        hash.put(UXHash.getHash("icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));
        hash.put(UXHash.getHash("icon-width"), new UXValueSizeDp(18));
        hash.put(UXHash.getHash("icon-height"), new UXValueSizeDp(20));
        hash.put(UXHash.getHash("icon-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("icon-bg-color"), new UXValueColor(0xFF00FFFF));
        hash.put(UXHash.getHash("line-color"), new UXValueColor(0x00FFFFFF));
        hash.put(UXHash.getHash("line-filled-color"), new UXValueColor(0x00FF00FF));
        hash.put(UXHash.getHash("line-width"), new UXValueSizeDp(3));
        hash.put(UXHash.getHash("line-cap"), new UXValueText(LineCap.ROUND.toString()));
        return hash;
    }
}