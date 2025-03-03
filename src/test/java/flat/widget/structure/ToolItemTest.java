package flat.widget.structure;

import flat.events.ActionEvent;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXListener;
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
public class ToolItemTest {

    ResourceStream resIcon;
    Drawable icon;

    @Before
    public void before() {
        mockStatic(DrawableReader.class);

        icon = mock(Drawable.class);
        when(icon.getWidth()).thenReturn(16f);
        when(icon.getHeight()).thenReturn(20f);

        resIcon = mock(ResourceStream.class);

        when(DrawableReader.parse(resIcon)).thenReturn(icon);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", ActionEvent.class)).thenReturn(action);

        ToolItem toolItem = new ToolItem();

        assertNull(toolItem.getIcon());
        assertNull(toolItem.getMenuText());
        assertNull(toolItem.getMenuShortcutText());
        assertEquals(0xFFFFFFFF, toolItem.getIconColor());
        assertEquals(ImageFilter.LINEAR, toolItem.getIconImageFilter());
        assertNull(toolItem.getActionListener());

        toolItem.setAttributes(createNonDefaultValues(), null);
        toolItem.applyAttributes(controller);

        assertNull(toolItem.getIcon());
        assertEquals("Hello World", toolItem.getMenuText());
        assertEquals("Ctrl+C", toolItem.getMenuShortcutText());
        assertEquals(0xFFFFFFFF, toolItem.getIconColor());
        assertEquals(ImageFilter.LINEAR, toolItem.getIconImageFilter());
        assertEquals(action, toolItem.getActionListener());

        toolItem.applyStyle();

        assertEquals(icon, toolItem.getIcon());
        assertEquals("Hello World", toolItem.getMenuText());
        assertEquals("Ctrl+C", toolItem.getMenuShortcutText());
        assertEquals(0xFF0000FF, toolItem.getIconColor());
        assertEquals(ImageFilter.NEAREST, toolItem.getIconImageFilter());
        assertEquals(action, toolItem.getActionListener());
    }

    @Test
    public void measure() {
        ToolItem toolItem = new ToolItem();
        toolItem.setIcon(icon);
        toolItem.onMeasure();

        assertEquals(0, toolItem.getMeasureWidth(), 0.1f);
        assertEquals(0, toolItem.getMeasureHeight(), 0.1f);

        toolItem.setPrefSize(20, 20);
        toolItem.onMeasure();

        assertEquals(20, toolItem.getMeasureWidth(), 0.1f);
        assertEquals(20, toolItem.getMeasureHeight(), 0.1f);

        toolItem.setMargins(1, 2, 3, 4);
        toolItem.setPadding(5, 4, 2, 3);
        toolItem.onMeasure();

        assertEquals(26, toolItem.getMeasureWidth(), 0.1f);
        assertEquals(24, toolItem.getMeasureHeight(), 0.1f);

        toolItem.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        toolItem.onMeasure();

        assertEquals(Widget.MATCH_PARENT, toolItem.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, toolItem.getMeasureHeight(), 0.1f);
    }

    @Test
    public void fireAction() {
        ToolItem toolItem = new ToolItem();

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        toolItem.setActionListener(action);

        toolItem.action();

        verify(action, times(1)).handle(any());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxIconActive = mock(UXValue.class);
        when(uxIconActive.asResource(any())).thenReturn(resIcon);

        hash.put(UXHash.getHash("on-action"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("icon-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));
        hash.put(UXHash.getHash("icon"), uxIconActive);
        hash.put(UXHash.getHash("menu-text"), new UXValueText("Hello World"));
        hash.put(UXHash.getHash("menu-shortcut-text"),  new UXValueText("Ctrl+C"));
        return hash;
    }
}