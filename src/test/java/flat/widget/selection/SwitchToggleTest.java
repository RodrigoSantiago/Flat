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
    ResourceStream resInactive;

    Drawable iconActive;
    Drawable iconInactive;

    @Before
    public void before() {
        theme = mock(UXTheme.class);

        mockStatic(DrawableReader.class);

        iconActive = mock(Drawable.class);
        when(iconActive.getWidth()).thenReturn(16f);
        when(iconActive.getHeight()).thenReturn(20f);
        iconInactive = mock(Drawable.class);
        when(iconInactive.getWidth()).thenReturn(20f);
        when(iconInactive.getHeight()).thenReturn(16f);

        resActive = mock(ResourceStream.class);
        resInactive = mock(ResourceStream.class);

        when(DrawableReader.parse(resActive)).thenReturn(iconActive);
        when(DrawableReader.parse(resInactive)).thenReturn(iconInactive);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", ActionEvent.class)).thenReturn(action);

        var listener = (UXValueListener<Boolean>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onActiveWork", Boolean.class)).thenReturn(listener);

        SwitchToggle switchToggle = new SwitchToggle();

        assertNull(switchToggle.getIconActive());
        assertNull(switchToggle.getIconInactive());
        assertEquals(0, switchToggle.getIconTransitionDuration(), 0.0001f);
        assertEquals(0, switchToggle.getSlideTransitionDuration(), 0.0001f);
        assertEquals(0xFFFFFFFF, switchToggle.getIconColor());
        assertEquals(Direction.HORIZONTAL, switchToggle.getDirection());
        assertEquals(ImageFilter.LINEAR, switchToggle.getIconImageFilter());
        assertFalse(switchToggle.isActive());
        assertNull(switchToggle.getToggleListener());
        assertNull(switchToggle.getActiveListener());

        switchToggle.setAttributes(createNonDefaultValues(), null);
        switchToggle.applyAttributes(controller);

        assertNull(switchToggle.getIconActive());
        assertNull(switchToggle.getIconInactive());
        assertEquals(0, switchToggle.getIconTransitionDuration(), 0.0001f);
        assertEquals(0, switchToggle.getSlideTransitionDuration(), 0.0001f);
        assertEquals(0xFFFFFFFF, switchToggle.getIconColor());
        assertEquals(Direction.HORIZONTAL, switchToggle.getDirection());
        assertEquals(ImageFilter.LINEAR, switchToggle.getIconImageFilter());
        assertTrue(switchToggle.isActive());
        assertEquals(action, switchToggle.getToggleListener());
        assertEquals(listener, switchToggle.getActiveListener());

        switchToggle.applyStyle();

        assertEquals(iconActive, switchToggle.getIconActive());
        assertEquals(iconInactive, switchToggle.getIconInactive());
        assertEquals(1.0f, switchToggle.getIconTransitionDuration(), 0.0001f);
        assertEquals(2.0f, switchToggle.getSlideTransitionDuration(), 0.0001f);
        assertEquals(0xFF0000FF, switchToggle.getIconColor());
        assertEquals(Direction.VERTICAL, switchToggle.getDirection());
        assertEquals(ImageFilter.NEAREST, switchToggle.getIconImageFilter());
        assertTrue(switchToggle.isActive());
        assertEquals(action, switchToggle.getToggleListener());
        assertEquals(listener, switchToggle.getActiveListener());
    }

    @Test
    public void measureHorizontal() {
        SwitchToggle switchToggle = new SwitchToggle();
        switchToggle.setIconActive(iconActive);
        switchToggle.setIconInactive(iconInactive);
        switchToggle.setDirection(Direction.HORIZONTAL);
        switchToggle.setActive(true);
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
        switchToggle.setIconActive(iconActive);
        switchToggle.setIconInactive(iconInactive);
        switchToggle.setDirection(Direction.VERTICAL);
        switchToggle.setActive(true);
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
        switchToggle.setIconActive(iconActive);
        switchToggle.setIconInactive(iconInactive);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        switchToggle.setToggleListener(action);

        var listener = (UXValueListener<Boolean>) mock(UXValueListener.class);
        switchToggle.setActiveListener(listener);

        switchToggle.setActive(false);
        assertFalse(switchToggle.isActive());

        switchToggle.toggle();
        assertTrue(switchToggle.isActive());

        switchToggle.toggle();
        assertFalse(switchToggle.isActive());

        switchToggle.setActive(true);

        verify(action, times(2)).handle(any());
        verify(listener, times(3)).handle(any());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxIconActive = mock(UXValue.class);
        when(uxIconActive.asResource(any())).thenReturn(resActive);
        UXValue uxIconInactive = mock(UXValue.class);
        when(uxIconInactive.asResource(any())).thenReturn(resInactive);

        hash.put(UXHash.getHash("on-toggle"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("on-active-change"), new UXValueText("onActiveWork"));
        hash.put(UXHash.getHash("icon-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("direction"), new UXValueText(Direction.VERTICAL.toString()));
        hash.put(UXHash.getHash("icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));
        hash.put(UXHash.getHash("icon-active"), uxIconActive);
        hash.put(UXHash.getHash("icon-inactive"), uxIconInactive);
        hash.put(UXHash.getHash("icon-transition-duration"), new UXValueNumber(1.0f));
        hash.put(UXHash.getHash("slide-transition-duration"), new UXValueNumber(2.0f));
        hash.put(UXHash.getHash("active"), new UXValueBool(true));
        return hash;
    }
}