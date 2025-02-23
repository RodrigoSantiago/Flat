package flat.widget.text;

import flat.events.ActionEvent;
import flat.graphics.context.Font;
import flat.graphics.cursor.Cursor;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXListener;
import flat.uxml.UXNode;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueColor;
import flat.uxml.value.UXValueSizeSp;
import flat.uxml.value.UXValueText;
import flat.widget.Widget;
import flat.widget.enums.ImageFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class, Font.class, DrawableReader.class})
public class ChipTest {

    Font defaultFont;

    ResourceStream resIcon;
    Drawable icon;
    ResourceStream resCloseIcon;
    Drawable closeIcon;

    @Before
    public void before() {
        mockStatic(Font.class);

        defaultFont = mock(Font.class);
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

        closeIcon = mock(Drawable.class);
        when(closeIcon.getWidth()).thenReturn(20f);
        when(closeIcon.getHeight()).thenReturn(18f);

        resCloseIcon = mock(ResourceStream.class);
        when(DrawableReader.parse(resCloseIcon)).thenReturn(closeIcon);

        when(defaultFont.getWidth(any(), anyInt(), anyInt(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onCloseActionWork", ActionEvent.class)).thenReturn(action);

        Chip chip = new Chip();

        assertEquals(0, chip.getCloseIconWidth(), 0.001f);
        assertEquals(0, chip.getCloseIconHeight(), 0.001f);
        assertEquals(0, chip.getCloseIconSpacing(), 0.001f);
        assertEquals(ImageFilter.LINEAR, chip.getCloseIconImageFilter());
        assertNull(chip.getCloseIcon());
        assertEquals(0xFFFFFFFF, chip.getCloseIconColor());
        assertEquals(0x00000000, chip.getCloseIconBgColor());
        assertEquals(Cursor.UNSET, chip.getCloseIconCursor());
        assertNull(chip.getRequestCloseListener());

        chip.setAttributes(createNonDefaultValues(), "chip");
        chip.applyAttributes(controller);

        assertEquals(0, chip.getCloseIconWidth(), 0.001f);
        assertEquals(0, chip.getCloseIconHeight(), 0.001f);
        assertEquals(0, chip.getCloseIconSpacing(), 0.001f);
        assertEquals(ImageFilter.LINEAR, chip.getCloseIconImageFilter());
        assertNull(chip.getCloseIcon());
        assertEquals(0xFFFFFFFF, chip.getCloseIconColor());
        assertEquals(0x00000000, chip.getCloseIconBgColor());
        assertEquals(Cursor.UNSET, chip.getCloseIconCursor());
        assertEquals(action, chip.getRequestCloseListener());

        chip.applyStyle();

        assertEquals(20, chip.getCloseIconWidth(), 0.001f);
        assertEquals(22, chip.getCloseIconHeight(), 0.001f);
        assertEquals(16, chip.getCloseIconSpacing(), 0.001f);
        assertEquals(ImageFilter.NEAREST, chip.getCloseIconImageFilter());
        assertEquals(closeIcon, chip.getCloseIcon());
        assertEquals(0xFF0000FF, chip.getCloseIconColor());
        assertEquals(0xFF00F0FF, chip.getCloseIconBgColor());
        assertEquals(Cursor.HAND, chip.getCloseIconCursor());
        assertEquals(action, chip.getRequestCloseListener());
    }

    @Test
    public void measure() {
        Chip chip = new Chip();
        chip.setText("Hello World");
        chip.onMeasure();

        assertEquals(165, chip.getMeasureWidth(), 0.1f);
        assertEquals(32, chip.getMeasureHeight(), 0.1f);

        chip.setMargins(1, 2, 3, 4);
        chip.setPadding(5, 4, 2, 3);
        chip.onMeasure();

        assertEquals(178, chip.getMeasureWidth(), 0.1f);
        assertEquals(43, chip.getMeasureHeight(), 0.1f);

        chip.setPrefSize(100, 200);
        chip.onMeasure();

        assertEquals(106, chip.getMeasureWidth(), 0.1f);
        assertEquals(204, chip.getMeasureHeight(), 0.1f);

        chip.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        chip.onMeasure();

        assertEquals(Widget.MATCH_PARENT, chip.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, chip.getMeasureHeight(), 0.1f);
    }

    @Test
    public void iconSize() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Chip chip = new Chip();
        chip.setText("Hello World");
        chip.setIcon(drawable);
        chip.setIconWidth(Widget.WRAP_CONTENT);
        chip.setIconHeight(Widget.WRAP_CONTENT);

        chip.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        chip.onMeasure();

        assertEquals(165 + 32, chip.getMeasureWidth(), 0.1f);
        assertEquals(32, chip.getMeasureHeight(), 0.1f);

        chip.setIconWidth(58f);
        chip.setIconHeight(64f);
        chip.onMeasure();

        assertEquals(165 + 58f, chip.getMeasureWidth(), 0.1f);
        assertEquals(64, chip.getMeasureHeight(), 0.1f);

        chip.setIconWidth(32f);
        chip.setIconHeight(16f);
        chip.setMargins(1, 2, 3, 4);
        chip.setPadding(5, 4, 2, 3);
        chip.onMeasure();

        assertEquals(178 + 32, chip.getMeasureWidth(), 0.1f);
        assertEquals(43, chip.getMeasureHeight(), 0.1f);

        chip.setPrefSize(100, 200);
        chip.onMeasure();

        assertEquals(106, chip.getMeasureWidth(), 0.1f);
        assertEquals(204, chip.getMeasureHeight(), 0.1f);

        chip.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        chip.onMeasure();

        assertEquals(Widget.MATCH_PARENT, chip.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, chip.getMeasureHeight(), 0.1f);
    }

    @Test
    public void closeIconSize() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Chip chip = new Chip();
        chip.setText("Hello World");
        chip.setCloseIcon(drawable);
        chip.setCloseIconWidth(Widget.WRAP_CONTENT);
        chip.setCloseIconHeight(Widget.WRAP_CONTENT);

        chip.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        chip.onMeasure();

        assertEquals(165 + 32, chip.getMeasureWidth(), 0.1f);
        assertEquals(32, chip.getMeasureHeight(), 0.1f);

        chip.setCloseIconWidth(58f);
        chip.setCloseIconHeight(64f);
        chip.onMeasure();

        assertEquals(165 + 58f, chip.getMeasureWidth(), 0.1f);
        assertEquals(64, chip.getMeasureHeight(), 0.1f);

        chip.setCloseIconWidth(32f);
        chip.setCloseIconHeight(16f);
        chip.setMargins(1, 2, 3, 4);
        chip.setPadding(5, 4, 2, 3);
        chip.onMeasure();

        assertEquals(178 + 32, chip.getMeasureWidth(), 0.1f);
        assertEquals(43, chip.getMeasureHeight(), 0.1f);

        chip.setPrefSize(100, 200);
        chip.onMeasure();

        assertEquals(106, chip.getMeasureWidth(), 0.1f);
        assertEquals(204, chip.getMeasureHeight(), 0.1f);

        chip.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        chip.onMeasure();

        assertEquals(Widget.MATCH_PARENT, chip.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, chip.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureIconSpacing() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Chip chip = new Chip();
        chip.setText("Hello World");
        chip.setIcon(null);
        chip.setIconSpacing(8f);
        chip.setIconWidth(24);
        chip.setIconHeight(16);

        chip.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        chip.onMeasure();

        assertEquals(165, chip.getMeasureWidth(), 0.1f);
        assertEquals(32, chip.getMeasureHeight(), 0.1f);

        chip.setIcon(drawable);
        chip.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        chip.onMeasure();

        assertEquals(165 + 24 + 8f, chip.getMeasureWidth(), 0.1f);
        assertEquals(32, chip.getMeasureHeight(), 0.1f);

        chip.setMargins(1, 2, 3, 4);
        chip.setPadding(5, 4, 2, 3);
        chip.onMeasure();

        assertEquals(178 + 24 + 8f, chip.getMeasureWidth(), 0.1f);
        assertEquals(43, chip.getMeasureHeight(), 0.1f);

        chip.setPrefSize(100, 200);
        chip.onMeasure();

        assertEquals(106, chip.getMeasureWidth(), 0.1f);
        assertEquals(204, chip.getMeasureHeight(), 0.1f);

        chip.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        chip.onMeasure();

        assertEquals(Widget.MATCH_PARENT, chip.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, chip.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureCloseIconSpacing() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Chip chip = new Chip();
        chip.setText("Hello World");
        chip.setCloseIcon(null);
        chip.setCloseIconSpacing(8f);
        chip.setCloseIconWidth(24);
        chip.setCloseIconHeight(16);

        chip.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        chip.onMeasure();

        assertEquals(165, chip.getMeasureWidth(), 0.1f);
        assertEquals(32, chip.getMeasureHeight(), 0.1f);

        chip.setCloseIcon(drawable);
        chip.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        chip.onMeasure();

        assertEquals(165 + 24 + 8f, chip.getMeasureWidth(), 0.1f);
        assertEquals(32, chip.getMeasureHeight(), 0.1f);

        chip.setMargins(1, 2, 3, 4);
        chip.setPadding(5, 4, 2, 3);
        chip.onMeasure();

        assertEquals(178 + 24 + 8f, chip.getMeasureWidth(), 0.1f);
        assertEquals(43, chip.getMeasureHeight(), 0.1f);

        chip.setPrefSize(100, 200);
        chip.onMeasure();

        assertEquals(106, chip.getMeasureWidth(), 0.1f);
        assertEquals(204, chip.getMeasureHeight(), 0.1f);

        chip.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        chip.onMeasure();

        assertEquals(Widget.MATCH_PARENT, chip.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, chip.getMeasureHeight(), 0.1f);
    }

    @Test
    public void fireAction() {
        Chip chip = new Chip();
        chip.setText("Hello World");

        var closeAction = (UXListener<ActionEvent>) mock(UXListener.class);
        chip.setRequestCloseListener(closeAction);

        chip.requestClose();
        verify(closeAction, times(1)).handle(any());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxCloseIcon = mock(UXValue.class);
        when(uxCloseIcon.asResource(any())).thenReturn(resCloseIcon);

        hash.put(UXHash.getHash("close-icon"), uxCloseIcon);
        hash.put(UXHash.getHash("close-icon-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("close-icon-bg-color"), new UXValueColor(0xFF00F0FF));
        hash.put(UXHash.getHash("close-icon-width"), new UXValueSizeSp(20));
        hash.put(UXHash.getHash("close-icon-height"), new UXValueSizeSp(22));
        hash.put(UXHash.getHash("close-icon-spacing"), new UXValueSizeSp(16));
        hash.put(UXHash.getHash("close-icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));
        hash.put(UXHash.getHash("close-icon-cursor"), new UXValueText(Cursor.HAND.toString()));
        hash.put(UXHash.getHash("on-request-close"), new UXValueText("onCloseActionWork"));

        return hash;
    }
}