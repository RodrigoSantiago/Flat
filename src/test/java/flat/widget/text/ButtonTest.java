package flat.widget.text;

import flat.events.ActionEvent;
import flat.graphics.symbols.Font;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.graphics.symbols.FontManager;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXListener;
import flat.uxml.UXValueListener;
import flat.uxml.value.*;
import flat.widget.Widget;
import flat.widget.enums.HorizontalPosition;
import flat.widget.enums.ImageFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DrawableReader.class, Font.class, FontManager.class})
public class ButtonTest {

    Font boldFont;
    Font defaultFont;

    ResourceStream resIcon;
    Drawable icon;

    @Before
    public void before() {
        mockStatic(Font.class);
        mockStatic(FontManager.class);

        defaultFont = mock(Font.class);
        boldFont = mock(Font.class);
        when(Font.getDefault()).thenReturn(defaultFont);

        when(FontManager.findFont(any(), any(), any(), any())).thenReturn(defaultFont);
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

        Button button = new Button();

        assertEquals(0, button.getIconWidth(), 0.001f);
        assertEquals(0, button.getIconHeight(), 0.001f);
        assertEquals(HorizontalPosition.LEFT, button.getIconPosition());
        assertEquals(0, button.getIconSpacing(), 0.1f);
        assertEquals(ImageFilter.LINEAR, button.getIconImageFilter());
        assertFalse(button.isIconClipCircle());
        assertNull(button.getIcon());
        assertEquals(0xFFFFFFFF, button.getIconColor());
        assertFalse(button.isActivated());
        assertNull(button.getActionListener());
        assertNull(button.getActivatedListener());

        button.setAttributes(createNonDefaultValues(), null);
        button.applyAttributes(controller);

        assertEquals(0, button.getIconWidth(), 0.001f);
        assertEquals(0, button.getIconHeight(), 0.001f);
        assertEquals(HorizontalPosition.LEFT, button.getIconPosition());
        assertEquals(0, button.getIconSpacing(), 0.1f);
        assertEquals(ImageFilter.LINEAR, button.getIconImageFilter());
        assertFalse(button.isIconClipCircle());
        assertNull(button.getIcon());
        assertEquals(0xFFFFFFFF, button.getIconColor());
        assertTrue(button.isActivated());
        assertEquals(action, button.getActionListener());
        assertEquals(active, button.getActivatedListener());

        button.applyStyle();

        assertEquals(16, button.getIconWidth(), 0.001f);
        assertEquals(18, button.getIconHeight(), 0.001f);
        assertEquals(HorizontalPosition.RIGHT, button.getIconPosition());
        assertEquals(24, button.getIconSpacing(), 0.1f);
        assertEquals(ImageFilter.NEAREST, button.getIconImageFilter());
        assertTrue(button.isIconClipCircle());
        assertEquals(icon, button.getIcon());
        assertEquals(0xFFFF00FF, button.getIconColor());
        assertTrue(button.isActivated());
        assertEquals(action, button.getActionListener());
        assertEquals(active, button.getActivatedListener());
    }

    @Test
    public void measure() {
        Button button = new Button();
        button.setText("Hello World");
        button.onMeasure();

        assertEquals(165, button.getMeasureWidth(), 0.1f);
        assertEquals(32, button.getMeasureHeight(), 0.1f);

        button.setMargins(1, 2, 3, 4);
        button.setPadding(5, 4, 2, 3);
        button.onMeasure();

        assertEquals(178, button.getMeasureWidth(), 0.1f);
        assertEquals(43, button.getMeasureHeight(), 0.1f);

        button.setPrefSize(100, 200);
        button.onMeasure();

        assertEquals(106, button.getMeasureWidth(), 0.1f);
        assertEquals(204, button.getMeasureHeight(), 0.1f);

        button.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        button.onMeasure();

        assertEquals(Widget.MATCH_PARENT, button.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, button.getMeasureHeight(), 0.1f);
    }

    @Test
    public void iconSize() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Button button = new Button();
        button.setText("Hello World");
        button.setIcon(drawable);
        button.setIconWidth(Widget.WRAP_CONTENT);
        button.setIconHeight(Widget.WRAP_CONTENT);

        button.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        button.onMeasure();

        assertEquals(165 + 32, button.getMeasureWidth(), 0.1f);
        assertEquals(32, button.getMeasureHeight(), 0.1f);

        button.setIconWidth(58f);
        button.setIconHeight(64f);
        button.onMeasure();

        assertEquals(165 + 58f, button.getMeasureWidth(), 0.1f);
        assertEquals(64, button.getMeasureHeight(), 0.1f);

        button.setIconWidth(32f);
        button.setIconHeight(16f);
        button.setMargins(1, 2, 3, 4);
        button.setPadding(5, 4, 2, 3);
        button.onMeasure();

        assertEquals(178 + 32, button.getMeasureWidth(), 0.1f);
        assertEquals(43, button.getMeasureHeight(), 0.1f);

        button.setPrefSize(100, 200);
        button.onMeasure();

        assertEquals(106, button.getMeasureWidth(), 0.1f);
        assertEquals(204, button.getMeasureHeight(), 0.1f);

        button.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        button.onMeasure();

        assertEquals(Widget.MATCH_PARENT, button.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, button.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureIconSpacing() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Button button = new Button();
        button.setText("Hello World");
        button.setIcon(null);
        button.setIconSpacing(8f);
        button.setIconWidth(24);
        button.setIconHeight(16);

        button.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        button.onMeasure();

        assertEquals(165, button.getMeasureWidth(), 0.1f);
        assertEquals(32, button.getMeasureHeight(), 0.1f);

        button.setIcon(drawable);
        button.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        button.onMeasure();

        assertEquals(165 + 24 + 8f, button.getMeasureWidth(), 0.1f);
        assertEquals(32, button.getMeasureHeight(), 0.1f);

        button.setMargins(1, 2, 3, 4);
        button.setPadding(5, 4, 2, 3);
        button.onMeasure();

        assertEquals(178 + 24 + 8f, button.getMeasureWidth(), 0.1f);
        assertEquals(43, button.getMeasureHeight(), 0.1f);

        button.setPrefSize(100, 200);
        button.onMeasure();

        assertEquals(106, button.getMeasureWidth(), 0.1f);
        assertEquals(204, button.getMeasureHeight(), 0.1f);

        button.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        button.onMeasure();

        assertEquals(Widget.MATCH_PARENT, button.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, button.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureIconScaleHeight() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Button button = new Button();
        button.setText("Hello World");
        button.setIcon(drawable);
        button.setIconWidth(48);
        button.setIconHeight(32);

        button.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        button.onMeasure();

        assertEquals(165 + 48, button.getMeasureWidth(), 0.1f);
        assertEquals(32, button.getMeasureHeight(), 0.1f);

        button.setMargins(1, 2, 3, 4);
        button.setPadding(5, 4, 2, 3);
        button.onMeasure();

        assertEquals(178 + 48, button.getMeasureWidth(), 0.1f);
        assertEquals(43, button.getMeasureHeight(), 0.1f);

        button.setPrefSize(100, 200);
        button.onMeasure();

        assertEquals(106, button.getMeasureWidth(), 0.1f);
        assertEquals(204, button.getMeasureHeight(), 0.1f);

        button.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        button.onMeasure();

        assertEquals(Widget.MATCH_PARENT, button.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, button.getMeasureHeight(), 0.1f);
    }

    @Test
    public void fireAction() {
        Button button = new Button();
        button.setText("Hello World");

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        button.setActionListener(action);

        button.action();
        verify(action, times(1)).handle(any());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();
        UXValue uxBoldFont = mock(UXValue.class);
        when(uxBoldFont.asFont(any())).thenReturn(boldFont);

        UXValue uxIconActive = mock(UXValue.class);
        when(uxIconActive.asResource(any())).thenReturn(resIcon);

        hash.put(UXHash.getHash("icon"), uxIconActive);
        hash.put(UXHash.getHash("icon-width"), new UXValueSizeSp(16));
        hash.put(UXHash.getHash("icon-height"), new UXValueSizeSp(18));
        hash.put(UXHash.getHash("icon-color"), new UXValueColor(0xFFFF00FF));
        hash.put(UXHash.getHash("icon-scale-height"), new UXValueBool(true));
        hash.put(UXHash.getHash("icon-position"), new UXValueText(HorizontalPosition.RIGHT.toString()));
        hash.put(UXHash.getHash("icon-spacing"), new UXValueSizeSp(24));
        hash.put(UXHash.getHash("icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));
        hash.put(UXHash.getHash("icon-clip-circle"), new UXValueBool(true));
        hash.put(UXHash.getHash("activated"), new UXValueBool(true));
        hash.put(UXHash.getHash("on-action"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("on-activated-change"), new UXValueText("onActiveWork"));
        return hash;
    }
}
