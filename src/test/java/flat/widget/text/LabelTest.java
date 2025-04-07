package flat.widget.text;

import flat.graphics.symbols.Font;
import flat.uxml.Controller;
import flat.uxml.UXBuilder;
import flat.uxml.UXHash;
import flat.uxml.value.*;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Font.class})
public class LabelTest {

    Controller controller;
    UXBuilder builder;
    Font defaultFont;
    Font boldFont;

    @Before
    public void before() {
        controller = mock(Controller.class);
        builder = mock(UXBuilder.class);

        mockStatic(Font.class);

        defaultFont = mock(Font.class);
        boldFont = mock(Font.class);
        when(Font.getDefault()).thenReturn(defaultFont);

        when(Font.findFont(any(), any(), any(), any())).thenReturn(defaultFont);
        when(defaultFont.getHeight(anyFloat())).thenReturn(16f);
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(64f);
    }

    @Test
    public void properties() {
        Label label = new Label();

        assertEquals(HorizontalAlign.LEFT, label.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, label.getVerticalAlign());
        assertFalse(label.isTextAllCaps());
        assertEquals(defaultFont, label.getTextFont());
        assertEquals(16f, label.getTextSize(), 0.1f);
        assertEquals(0x000000FF, label.getTextColor());
        assertNull(label.getText());

        label.setAttributes(createNonDefaultValues(), null);
        label.applyAttributes(null);

        assertEquals(HorizontalAlign.LEFT, label.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, label.getVerticalAlign());
        assertFalse(label.isTextAllCaps());
        assertEquals(defaultFont, label.getTextFont());
        assertEquals(16f, label.getTextSize(), 0.1f);
        assertEquals(0x000000FF, label.getTextColor());
        assertEquals("Hello World", label.getText());

        label.applyStyle();

        assertEquals(HorizontalAlign.RIGHT, label.getHorizontalAlign());
        assertEquals(VerticalAlign.BOTTOM, label.getVerticalAlign());
        assertTrue(label.isTextAllCaps());
        assertEquals(boldFont, label.getTextFont());
        assertEquals(24f, label.getTextSize(), 0.1f);
        assertEquals(0xFF0000FF, label.getTextColor());
        assertEquals("Hello World", label.getText());
    }

    @Test
    public void showText() {
        Label label = new Label();
        label.setText("Hello World");
        assertEquals("Hello World", label.getText());
        assertEquals("Hello World", label.getShowText());
        label.setTextAllCaps(true);
        assertEquals("Hello World", label.getText());
        assertEquals("HELLO WORLD", label.getShowText());
    }

    @Test
    public void measure() {
        when(defaultFont.getWidth(any(), anyInt(), anyInt(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);

        Label label = new Label();
        label.setText("Hello World");
        label.onMeasure();

        assertEquals(165, label.getMeasureWidth(), 0.1f);
        assertEquals(32, label.getMeasureHeight(), 0.1f);

        label.setMargins(1, 2, 3, 4);
        label.setPadding(5, 4, 2, 3);
        label.onMeasure();

        assertEquals(178, label.getMeasureWidth(), 0.1f);
        assertEquals(43, label.getMeasureHeight(), 0.1f);

        label.setPrefSize(100, 200);
        label.onMeasure();

        assertEquals(106, label.getMeasureWidth(), 0.1f);
        assertEquals(204, label.getMeasureHeight(), 0.1f);

        label.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        label.onMeasure();

        assertEquals(Widget.MATCH_PARENT, label.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, label.getMeasureHeight(), 0.1f);
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();
        UXValue uxBoldFont = mock(UXValue.class);
        when(uxBoldFont.asFont(any())).thenReturn(boldFont);

        hash.put(UXHash.getHash("horizontal-align"), new UXValueText(HorizontalAlign.RIGHT.toString()));
        hash.put(UXHash.getHash("vertical-align"), new UXValueText(VerticalAlign.BOTTOM.toString()));
        hash.put(UXHash.getHash("text"), new UXValueText("Hello World"));
        hash.put(UXHash.getHash("text-all-caps"), new UXValueBool(true));
        hash.put(UXHash.getHash("text-font"), uxBoldFont);
        hash.put(UXHash.getHash("text-size"), new UXValueSizeSp(24));
        hash.put(UXHash.getHash("text-color"), new UXValueColor(0xFF0000FF));
        return hash;
    }
}