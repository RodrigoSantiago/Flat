package flat.widget.selection;

import flat.events.ActionEvent;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.uxml.value.*;
import flat.widget.Widget;
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
public class RadioButtonTest {
    ResourceStream resActive;
    ResourceStream resInactive;

    Drawable iconActive;
    Drawable iconInactive;

    @Before
    public void before() {
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

        RadioButton radioButton = new RadioButton();

        assertNull(radioButton.getIconActive());
        assertNull(radioButton.getIconInactive());
        assertEquals(0, radioButton.getIconTransitionDuration(), 0.0001f);
        assertEquals(0xFFFFFFFF, radioButton.getIconColor());
        assertEquals(ImageFilter.LINEAR, radioButton.getIconImageFilter());
        assertFalse(radioButton.isActive());
        assertNull(radioButton.getToggleListener());
        assertNull(radioButton.getActiveListener());

        radioButton.setAttributes(createNonDefaultValues(), "radio-button");
        radioButton.applyAttributes(controller);

        assertNull(radioButton.getIconActive());
        assertNull(radioButton.getIconInactive());
        assertEquals(0, radioButton.getIconTransitionDuration(), 0.0001f);
        assertEquals(0xFFFFFFFF, radioButton.getIconColor());
        assertEquals(ImageFilter.LINEAR, radioButton.getIconImageFilter());
        assertTrue(radioButton.isActive());
        assertEquals(action, radioButton.getToggleListener());
        assertEquals(listener, radioButton.getActiveListener());

        radioButton.applyStyle();

        assertEquals(iconActive, radioButton.getIconActive());
        assertEquals(iconInactive, radioButton.getIconInactive());
        assertEquals(1.0f, radioButton.getIconTransitionDuration(), 0.0001f);
        assertEquals(0xFF0000FF, radioButton.getIconColor());
        assertEquals(ImageFilter.NEAREST, radioButton.getIconImageFilter());
        assertTrue(radioButton.isActive());
        assertEquals(action, radioButton.getToggleListener());
        assertEquals(listener, radioButton.getActiveListener());
    }

    @Test
    public void measure() {
        RadioButton radioButton = new RadioButton();
        radioButton.setIconActive(iconActive);
        radioButton.setIconInactive(iconInactive);
        radioButton.setActive(true);
        radioButton.onMeasure();

        assertEquals(20, radioButton.getMeasureWidth(), 0.1f);
        assertEquals(20, radioButton.getMeasureHeight(), 0.1f);

        radioButton.setMargins(1, 2, 3, 4);
        radioButton.setPadding(5, 4, 2, 3);
        radioButton.onMeasure();

        assertEquals(20 + 13, radioButton.getMeasureWidth(), 0.1f);
        assertEquals(20 + 11, radioButton.getMeasureHeight(), 0.1f);

        radioButton.setPrefSize(100, 200);
        radioButton.onMeasure();

        assertEquals(106, radioButton.getMeasureWidth(), 0.1f);
        assertEquals(204, radioButton.getMeasureHeight(), 0.1f);

        radioButton.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        radioButton.onMeasure();

        assertEquals(Widget.MATCH_PARENT, radioButton.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, radioButton.getMeasureHeight(), 0.1f);
    }

    @Test
    public void fireAction() {
        RadioButton radioButton = new RadioButton();
        radioButton.setIconActive(iconActive);
        radioButton.setIconInactive(iconInactive);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        radioButton.setToggleListener(action);

        var listener = (UXValueListener<Boolean>) mock(UXValueListener.class);
        radioButton.setActiveListener(listener);

        assertFalse(radioButton.isActive());

        radioButton.toggle();
        assertTrue(radioButton.isActive());

        radioButton.toggle();
        assertTrue(radioButton.isActive());

        radioButton.setActive(false);

        verify(action, times(1)).handle(any());
        verify(listener, times(2)).handle(any());
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
        hash.put(UXHash.getHash("icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));
        hash.put(UXHash.getHash("icon-active"), uxIconActive);
        hash.put(UXHash.getHash("icon-inactive"), uxIconInactive);
        hash.put(UXHash.getHash("icon-transition-duration"), new UXValueNumber(1.0f));
        hash.put(UXHash.getHash("active"), new UXValueBool(true));
        return hash;
    }
}