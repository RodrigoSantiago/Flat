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
    Drawable iconActive;

    @Before
    public void before() {
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

        RadioButton radioButton = new RadioButton();

        assertNull(radioButton.getIcon());
        assertEquals(0, radioButton.getIconWidth(), 0.001f);
        assertEquals(0, radioButton.getIconHeight(), 0.001f);
        assertEquals(0xFFFFFFFF, radioButton.getIconColor());
        assertEquals(ImageFilter.LINEAR, radioButton.getIconImageFilter());
        assertFalse(radioButton.isActivated());
        assertNull(radioButton.getToggleListener());
        assertNull(radioButton.getActivatedListener());

        radioButton.setAttributes(createNonDefaultValues(), null);
        radioButton.applyAttributes(controller);

        assertNull(radioButton.getIcon());
        assertEquals(0, radioButton.getIconWidth(), 0.001f);
        assertEquals(0, radioButton.getIconHeight(), 0.001f);
        assertEquals(0xFFFFFFFF, radioButton.getIconColor());
        assertEquals(ImageFilter.LINEAR, radioButton.getIconImageFilter());
        assertTrue(radioButton.isActivated());
        assertEquals(action, radioButton.getToggleListener());
        assertEquals(listener, radioButton.getActivatedListener());

        radioButton.applyStyle();

        assertEquals(iconActive, radioButton.getIcon());
        assertEquals(16, radioButton.getIconWidth(), 0.001f);
        assertEquals(18, radioButton.getIconHeight(), 0.001f);
        assertEquals(0xFF0000FF, radioButton.getIconColor());
        assertEquals(ImageFilter.NEAREST, radioButton.getIconImageFilter());
        assertTrue(radioButton.isActivated());
        assertEquals(action, radioButton.getToggleListener());
        assertEquals(listener, radioButton.getActivatedListener());
    }

    @Test
    public void measure() {
        RadioButton radioButton = new RadioButton();
        radioButton.setIcon(iconActive);
        radioButton.setActivated(true);
        radioButton.onMeasure();

        assertEquals(16, radioButton.getMeasureWidth(), 0.1f);
        assertEquals(20, radioButton.getMeasureHeight(), 0.1f);

        radioButton.setIconWidth(20);
        radioButton.setIconHeight(20);
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
        radioButton.setIcon(iconActive);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        radioButton.setToggleListener(action);

        var listener = (UXValueListener<Boolean>) mock(UXValueListener.class);
        radioButton.setActivatedListener(listener);

        assertFalse(radioButton.isActivated());

        radioButton.toggle();
        assertTrue(radioButton.isActivated());

        radioButton.toggle();
        assertTrue(radioButton.isActivated());

        radioButton.setActivated(false);

        verify(action, times(1)).handle(any());
        verify(listener, times(2)).handle(any());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxIconActive = mock(UXValue.class);
        when(uxIconActive.asResource(any())).thenReturn(resActive);

        hash.put(UXHash.getHash("on-toggle"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("on-activated-change"), new UXValueText("onActiveWork"));
        hash.put(UXHash.getHash("icon-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));
        hash.put(UXHash.getHash("icon"), uxIconActive);
        hash.put(UXHash.getHash("icon-width"), new UXValueSizeDp(16));
        hash.put(UXHash.getHash("icon-height"), new UXValueSizeDp(18));
        hash.put(UXHash.getHash("activated"), new UXValueBool(true));
        return hash;
    }
}