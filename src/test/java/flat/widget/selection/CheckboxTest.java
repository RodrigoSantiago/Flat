package flat.widget.selection;

import flat.events.ActionEvent;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXListener;
import flat.uxml.UXValueListener;
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
public class CheckboxTest {

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

        var activated = (UXValueListener<Boolean>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onActivatedWork", Boolean.class)).thenReturn(activated);

        var undefined = (UXValueListener<Boolean>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onUndefinedWork", Boolean.class)).thenReturn(undefined);

        Checkbox checkbox = new Checkbox();

        assertNull(checkbox.getIcon());
        assertEquals(0, checkbox.getIconWidth(), 0.001f);
        assertEquals(0, checkbox.getIconHeight(), 0.001f);
        assertEquals(0xFFFFFFFF, checkbox.getIconColor());
        assertEquals(ImageFilter.LINEAR, checkbox.getIconImageFilter());
        assertFalse(checkbox.isActivated());
        assertFalse(checkbox.isUndefined());
        assertNull(checkbox.getToggleListener());
        assertNull(checkbox.getActivatedListener());
        assertNull(checkbox.getUndefinedListener());

        checkbox.setAttributes(createNonDefaultValues(), null);
        checkbox.applyAttributes(controller);

        assertNull(checkbox.getIcon());
        assertEquals(0, checkbox.getIconWidth(), 0.001f);
        assertEquals(0, checkbox.getIconHeight(), 0.001f);
        assertEquals(0xFFFFFFFF, checkbox.getIconColor());
        assertEquals(ImageFilter.LINEAR, checkbox.getIconImageFilter());
        assertFalse(checkbox.isActivated()); // Undefined has priority
        assertTrue(checkbox.isUndefined());
        assertEquals(action, checkbox.getToggleListener());
        assertEquals(activated, checkbox.getActivatedListener());
        assertEquals(undefined, checkbox.getUndefinedListener());

        checkbox.applyStyle();

        assertEquals(iconActive, checkbox.getIcon());
        assertEquals(16, checkbox.getIconWidth(), 0.001f);
        assertEquals(18, checkbox.getIconHeight(), 0.001f);
        assertEquals(0xFF0000FF, checkbox.getIconColor());
        assertEquals(ImageFilter.NEAREST, checkbox.getIconImageFilter());
        assertFalse(checkbox.isActivated()); // Undefined has priority
        assertTrue(checkbox.isUndefined());
        assertEquals(action, checkbox.getToggleListener());
        assertEquals(activated, checkbox.getActivatedListener());
        assertEquals(undefined, checkbox.getUndefinedListener());
    }

    @Test
    public void measure() {
        Checkbox checkbox = new Checkbox();
        checkbox.setIcon(iconActive);
        checkbox.onMeasure();

        assertEquals(16, checkbox.getMeasureWidth(), 0.1f);
        assertEquals(20, checkbox.getMeasureHeight(), 0.1f);

        checkbox.setIconWidth(20);
        checkbox.setIconHeight(20);
        checkbox.onMeasure();

        assertEquals(20, checkbox.getMeasureWidth(), 0.1f);
        assertEquals(20, checkbox.getMeasureHeight(), 0.1f);

        checkbox.setMargins(1, 2, 3, 4);
        checkbox.setPadding(5, 4, 2, 3);
        checkbox.onMeasure();

        assertEquals(20 + 13, checkbox.getMeasureWidth(), 0.1f);
        assertEquals(20 + 11, checkbox.getMeasureHeight(), 0.1f);

        checkbox.setPrefSize(100, 200);
        checkbox.onMeasure();

        assertEquals(106, checkbox.getMeasureWidth(), 0.1f);
        assertEquals(204, checkbox.getMeasureHeight(), 0.1f);

        checkbox.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        checkbox.onMeasure();

        assertEquals(Widget.MATCH_PARENT, checkbox.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, checkbox.getMeasureHeight(), 0.1f);
    }

    @Test
    public void fireAction() {
        Checkbox checkbox = new Checkbox();
        checkbox.setIcon(iconActive);
        checkbox.setUndefined(true);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        checkbox.setToggleListener(action);

        var activated = (UXValueListener<Boolean>) mock(UXValueListener.class);
        checkbox.setActivatedListener(activated);

        var undefined = (UXValueListener<Boolean>) mock(UXValueListener.class);
        checkbox.setUndefinedListener(undefined);

        assertFalse(checkbox.isActivated());
        assertTrue(checkbox.isUndefined());
        checkbox.toggle();
        assertTrue(checkbox.isActivated());
        assertFalse(checkbox.isUndefined());
        checkbox.toggle();
        assertFalse(checkbox.isActivated());
        assertFalse(checkbox.isUndefined());
        checkbox.toggle();
        assertTrue(checkbox.isActivated());
        assertFalse(checkbox.isUndefined());

        checkbox.setUndefined(true);
        assertFalse(checkbox.isActivated());
        assertTrue(checkbox.isUndefined());

        verify(action, times(3)).handle(any());
        verify(activated, times(4)).handle(any());
        verify(undefined, times(2)).handle(any());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxIconActive = mock(UXValue.class);
        when(uxIconActive.asResource(any())).thenReturn(resActive);

        hash.put(UXHash.getHash("on-toggle"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("on-activated-change"), new UXValueText("onActivatedWork"));
        hash.put(UXHash.getHash("on-undefined-change"), new UXValueText("onUndefinedWork"));
        hash.put(UXHash.getHash("icon-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));
        hash.put(UXHash.getHash("icon"), uxIconActive);
        hash.put(UXHash.getHash("icon-width"), new UXValueSizeDp(16.0f));
        hash.put(UXHash.getHash("icon-height"), new UXValueSizeDp(18.0f));
        hash.put(UXHash.getHash("activated"), new UXValueBool(true));
        hash.put(UXHash.getHash("undefined"), new UXValueBool(true));
        return hash;
    }
}