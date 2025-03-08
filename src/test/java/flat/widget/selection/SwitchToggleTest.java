package flat.widget.selection;

import flat.events.ActionEvent;
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
public class SwitchToggleTest {
    UXTheme theme;

    ResourceStream resActive;

    Drawable iconActive;

    @Before
    public void before() {
        theme = mock(UXTheme.class);

        mockStatic(DrawableReader.class);

        iconActive = mock(Drawable.class);
        when(iconActive.getWidth()).thenReturn(16f);
        when(iconActive.getHeight()).thenReturn(20f);

        resActive = mock(ResourceStream.class);

        when(DrawableReader.parse(resActive)).thenReturn(iconActive);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", ActionEvent.class)).thenReturn(action);

        var listener = (UXValueListener<Boolean>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onActiveWork", Boolean.class)).thenReturn(listener);

        SwitchToggle switchToggle = new SwitchToggle();

        assertNull(switchToggle.getIcon());
        assertEquals(0, switchToggle.getIconWidth(), 0.0001f);
        assertEquals(0, switchToggle.getIconHeight(), 0.0001f);
        assertEquals(0, switchToggle.getSlideTransitionDuration(), 0.0001f);
        assertEquals(0xFFFFFFFF, switchToggle.getIconColor());
        assertEquals(0x00000000, switchToggle.getIconBgColor());
        assertEquals(0x00000000, switchToggle.getLineColor());
        assertEquals(1, switchToggle.getLineWidth(), 0.001f);
        assertEquals(LineCap.BUTT, switchToggle.getLineCap());
        assertEquals(Direction.HORIZONTAL, switchToggle.getDirection());
        assertEquals(ImageFilter.LINEAR, switchToggle.getIconImageFilter());
        assertFalse(switchToggle.isActivated());
        assertNull(switchToggle.getToggleListener());
        assertNull(switchToggle.getActivatedListener());

        switchToggle.setAttributes(createNonDefaultValues(), null);
        switchToggle.applyAttributes(controller);

        assertNull(switchToggle.getIcon());
        assertEquals(0, switchToggle.getIconWidth(), 0.0001f);
        assertEquals(0, switchToggle.getIconHeight(), 0.0001f);
        assertEquals(0, switchToggle.getSlideTransitionDuration(), 0.0001f);
        assertEquals(0xFFFFFFFF, switchToggle.getIconColor());
        assertEquals(0x00000000, switchToggle.getIconBgColor());
        assertEquals(0x00000000, switchToggle.getLineColor());
        assertEquals(1, switchToggle.getLineWidth(), 0.001f);
        assertEquals(LineCap.BUTT, switchToggle.getLineCap());
        assertEquals(Direction.HORIZONTAL, switchToggle.getDirection());
        assertEquals(ImageFilter.LINEAR, switchToggle.getIconImageFilter());
        assertTrue(switchToggle.isActivated());
        assertEquals(action, switchToggle.getToggleListener());
        assertEquals(listener, switchToggle.getActivatedListener());

        switchToggle.applyStyle();

        assertEquals(iconActive, switchToggle.getIcon());
        assertEquals(16, switchToggle.getIconWidth(), 0.0001f);
        assertEquals(18, switchToggle.getIconHeight(), 0.0001f);
        assertEquals(2.0f, switchToggle.getSlideTransitionDuration(), 0.0001f);
        assertEquals(0xFF0000FF, switchToggle.getIconColor());
        assertEquals(0xFFFF00FF, switchToggle.getIconBgColor());
        assertEquals(0x000000FF, switchToggle.getLineColor());
        assertEquals(2, switchToggle.getLineWidth(), 0.001f);
        assertEquals(LineCap.ROUND, switchToggle.getLineCap());
        assertEquals(Direction.VERTICAL, switchToggle.getDirection());
        assertEquals(ImageFilter.NEAREST, switchToggle.getIconImageFilter());
        assertTrue(switchToggle.isActivated());
        assertEquals(action, switchToggle.getToggleListener());
        assertEquals(listener, switchToggle.getActivatedListener());
    }

    @Test
    public void measureHorizontal() {
        SwitchToggle switchToggle = new SwitchToggle();
        switchToggle.setIcon(iconActive);
        switchToggle.setDirection(Direction.HORIZONTAL);
        switchToggle.setActivated(true);
        switchToggle.onMeasure();

        assertEquals(32, switchToggle.getMeasureWidth(), 0.1f);
        assertEquals(20, switchToggle.getMeasureHeight(), 0.1f);

        switchToggle.setIconWidth(20);
        switchToggle.setIconHeight(20);
        switchToggle.onMeasure();

        assertEquals(40, switchToggle.getMeasureWidth(), 0.1f);
        assertEquals(20, switchToggle.getMeasureHeight(), 0.1f);

        switchToggle.setMargins(1, 2, 3, 4);
        switchToggle.setPadding(5, 4, 2, 3);
        switchToggle.onMeasure();

        assertEquals(40 + 13, switchToggle.getMeasureWidth(), 0.1f);
        assertEquals(20 + 11, switchToggle.getMeasureHeight(), 0.1f);

        switchToggle.setPrefSize(100, 200);
        switchToggle.onMeasure();

        assertEquals(106, switchToggle.getMeasureWidth(), 0.1f);
        assertEquals(204, switchToggle.getMeasureHeight(), 0.1f);

        switchToggle.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        switchToggle.onMeasure();

        assertEquals(Widget.MATCH_PARENT, switchToggle.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, switchToggle.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureVertical() {
        SwitchToggle switchToggle = new SwitchToggle();
        switchToggle.setIcon(iconActive);
        switchToggle.setDirection(Direction.VERTICAL);
        switchToggle.setActivated(true);
        switchToggle.onMeasure();

        assertEquals(16, switchToggle.getMeasureWidth(), 0.1f);
        assertEquals(40, switchToggle.getMeasureHeight(), 0.1f);

        switchToggle.setIconWidth(20);
        switchToggle.setIconHeight(20);
        switchToggle.onMeasure();

        assertEquals(20, switchToggle.getMeasureWidth(), 0.1f);
        assertEquals(40, switchToggle.getMeasureHeight(), 0.1f);

        switchToggle.setMargins(1, 2, 3, 4);
        switchToggle.setPadding(5, 4, 2, 3);
        switchToggle.onMeasure();

        assertEquals(20 + 13, switchToggle.getMeasureWidth(), 0.1f);
        assertEquals(40 + 11, switchToggle.getMeasureHeight(), 0.1f);

        switchToggle.setPrefSize(100, 200);
        switchToggle.onMeasure();

        assertEquals(106, switchToggle.getMeasureWidth(), 0.1f);
        assertEquals(204, switchToggle.getMeasureHeight(), 0.1f);

        switchToggle.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        switchToggle.onMeasure();

        assertEquals(Widget.MATCH_PARENT, switchToggle.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, switchToggle.getMeasureHeight(), 0.1f);
    }

    @Test
    public void fireAction() {
        SwitchToggle switchToggle = new SwitchToggle();
        switchToggle.setIcon(iconActive);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        switchToggle.setToggleListener(action);

        var listener = (UXValueListener<Boolean>) mock(UXValueListener.class);
        switchToggle.setActivatedListener(listener);

        switchToggle.setActivated(false);
        assertFalse(switchToggle.isActivated());

        switchToggle.toggle();
        assertTrue(switchToggle.isActivated());

        switchToggle.toggle();
        assertFalse(switchToggle.isActivated());

        switchToggle.setActivated(true);

        verify(action, times(2)).handle(any());
        verify(listener, times(3)).handle(any());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxIconActive = mock(UXValue.class);
        when(uxIconActive.asResource(any())).thenReturn(resActive);

        hash.put(UXHash.getHash("on-toggle"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("on-activated-change"), new UXValueText("onActiveWork"));
        hash.put(UXHash.getHash("icon-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("icon-bg-color"), new UXValueColor(0xFFFF00FF));
        hash.put(UXHash.getHash("line-color"), new UXValueColor(0x000000FF));
        hash.put(UXHash.getHash("line-width"), new UXValueSizeDp(2));
        hash.put(UXHash.getHash("line-cap"), new UXValueText(LineCap.ROUND.toString()));
        hash.put(UXHash.getHash("direction"), new UXValueText(Direction.VERTICAL.toString()));
        hash.put(UXHash.getHash("icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));
        hash.put(UXHash.getHash("icon"), uxIconActive);
        hash.put(UXHash.getHash("icon-width"), new UXValueSizeDp(16));
        hash.put(UXHash.getHash("icon-height"), new UXValueSizeDp(18));
        hash.put(UXHash.getHash("slide-transition-duration"), new UXValueNumber(2.0f));
        hash.put(UXHash.getHash("activated"), new UXValueBool(true));
        return hash;
    }
}