package flat.widget.text;

import flat.events.ActionEvent;
import flat.graphics.context.Font;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXListener;
import flat.uxml.UXValueListener;
import flat.uxml.value.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DrawableReader.class, Font.class})
public class ToggleButtonTest {

    Font boldFont;
    Font defaultFont;

    ResourceStream resIcon;
    Drawable icon;

    @Before
    public void before() {
        mockStatic(Font.class);

        defaultFont = mock(Font.class);
        boldFont = mock(Font.class);
        when(Font.getDefault()).thenReturn(defaultFont);

        when(Font.findFont(any(), any(), any(), any())).thenReturn(defaultFont);
        when(defaultFont.getHeight(anyFloat())).thenReturn(16f);
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(64f);

        mockStatic(DrawableReader.class);
        icon = mock(Drawable.class);
        when(icon.getWidth()).thenReturn(24f);
        when(icon.getHeight()).thenReturn(16f);

        resIcon = mock(ResourceStream.class);
        when(DrawableReader.parse(resIcon)).thenReturn(icon);

        when(defaultFont.getWidth(any(), anyInt(), anyInt(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", ActionEvent.class)).thenReturn(action);

        var active = (UXValueListener<Boolean>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onActiveWork", Boolean.class)).thenReturn(active);

        ToggleButton button = new ToggleButton();

        assertFalse(button.isActive());
        assertNull(button.getToggleListener());
        assertNull(button.getActiveListener());

        button.setAttributes(createNonDefaultValues(), "toggle-button");
        button.applyAttributes(controller);

        assertTrue(button.isActive());
        assertEquals(action, button.getToggleListener());
        assertEquals(active, button.getActiveListener());

        button.applyStyle();

        assertTrue(button.isActive());
        assertEquals(action, button.getToggleListener());
        assertEquals(active, button.getActiveListener());
    }

    @Test
    public void fireAction() {
        ToggleButton button = new ToggleButton();

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        var toggle = (UXListener<ActionEvent>) mock(UXListener.class);
        var active = (UXValueListener<Boolean>) mock(UXValueListener.class);
        button.setActionListener(action);
        button.setToggleListener(toggle);
        button.setActiveListener(active);

        button.action();
        button.toggle();
        button.toggle();
        button.setActive(false);
        verify(action, times(1)).handle(any());
        verify(toggle, times(3)).handle(any());
        verify(active, times(4)).handle(any());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("active"), new UXValueBool(true));
        hash.put(UXHash.getHash("on-toggle"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("on-active-change"), new UXValueText("onActiveWork"));

        return hash;
    }
}