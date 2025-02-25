package flat.widget.text;

import flat.events.TextEvent;
import flat.graphics.context.Font;
import flat.uxml.*;
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
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Font.class})
public class TextFieldTest {

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
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(16f);
        when(defaultFont.getWidth(any(), eq(0), eq(0), anyFloat(), anyFloat())).thenReturn(0f);
        when(defaultFont.getWidth(any(), eq(0), eq(1), anyFloat(), anyFloat())).thenReturn(16f);
        when(defaultFont.getCaretOffset(any(), eq(0), eq(0), anyFloat(), anyFloat(), anyFloat(), anyBoolean())).thenReturn(new Font.CaretData(0, 0));
        when(defaultFont.getCaretOffset(any(), eq(0), eq(1), anyFloat(), anyFloat(), anyFloat(), anyBoolean())).thenReturn(new Font.CaretData(1, 16f));
    }

    @Test
    public void properties() {
        TextField textField = new TextField();

        var action = (UXValueListener<String>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onTextChangeWork", String.class)).thenReturn(action);

        var filter = (UXListener<TextEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onTextChangeFilter", TextEvent.class)).thenReturn(filter);

        assertEquals(HorizontalAlign.LEFT, textField.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, textField.getVerticalAlign());
        assertEquals(defaultFont, textField.getTextFont());
        assertEquals(16f, textField.getTextSize(), 0.1f);
        assertEquals(0x000000FF, textField.getTextColor());
        assertEquals(0x000000FF, textField.getTextHintColor());
        assertEquals(0x00000080, textField.getTextSelectedColor());
        assertNull(textField.getText());
        assertNull(textField.getTextHint());
        assertNull(textField.getTextChangeFilter());
        assertNull(textField.getTextChangeListener());

        textField.setAttributes(createNonDefaultValues(), "text-field");
        textField.applyAttributes(controller);

        assertEquals(HorizontalAlign.LEFT, textField.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, textField.getVerticalAlign());
        assertEquals(defaultFont, textField.getTextFont());
        assertEquals(16f, textField.getTextSize(), 0.1f);
        assertEquals(0x000000FF, textField.getTextColor());
        assertEquals(0x000000FF, textField.getTextHintColor());
        assertEquals(0x00000080, textField.getTextSelectedColor());
        assertEquals("A", textField.getText());
        assertEquals("B", textField.getTextHint());
        assertEquals(filter, textField.getTextChangeFilter());
        assertEquals(action, textField.getTextChangeListener());

        textField.applyStyle();

        assertEquals(HorizontalAlign.RIGHT, textField.getHorizontalAlign());
        assertEquals(VerticalAlign.BOTTOM, textField.getVerticalAlign());
        assertEquals(boldFont, textField.getTextFont());
        assertEquals(24f, textField.getTextSize(), 0.1f);
        assertEquals(0xFF0000FF, textField.getTextColor());
        assertEquals(0x00FF00FF, textField.getTextHintColor());
        assertEquals(0xFFFF00FF, textField.getTextSelectedColor());
        assertEquals("A", textField.getText());
        assertEquals("B", textField.getTextHint());
        assertEquals(filter, textField.getTextChangeFilter());
        assertEquals(action, textField.getTextChangeListener());
    }

    @Test
    public void setText() {
        TextField textField = new TextField();
        assertNull(textField.getText());
        textField.setText("A");
        assertEquals("A", textField.getText());
        textField.setText("B");
        assertEquals("B", textField.getText());
        textField.setText(null);
        assertNull(textField.getText());
    }

    @Test
    public void measure() {
        TextField textField = new TextField();
        textField.setText("A");
        textField.onMeasure();

        assertEquals(16, textField.getMeasureWidth(), 0.1f);
        assertEquals(16, textField.getMeasureHeight(), 0.1f);

        textField.setMargins(1, 2, 3, 4);
        textField.setPadding(5, 4, 2, 3);
        textField.onMeasure();

        assertEquals(29, textField.getMeasureWidth(), 0.1f);
        assertEquals(27, textField.getMeasureHeight(), 0.1f);

        textField.setPrefSize(100, 200);
        textField.onMeasure();

        assertEquals(106, textField.getMeasureWidth(), 0.1f);
        assertEquals(204, textField.getMeasureHeight(), 0.1f);

        textField.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        textField.onMeasure();

        assertEquals(Widget.MATCH_PARENT, textField.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, textField.getMeasureHeight(), 0.1f);
    }

    public void mockFont(int offset, int length, int charIndex, float width) {
        when(defaultFont.getCaretOffset(any(), eq(offset), eq(length), anyFloat(), anyFloat(), anyFloat(), anyBoolean()))
                .thenReturn(new Font.CaretData(charIndex, width));
    }

    public void mockFontPos(int offset, int length, float pos, int charIndex, float width) {
        when(defaultFont.getCaretOffset(any(), eq(offset), eq(length), anyFloat(), anyFloat(), eq(pos), anyBoolean()))
                .thenReturn(new Font.CaretData(charIndex, width));
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();
        UXValue uxBoldFont = mock(UXValue.class);
        when(uxBoldFont.asFont(any())).thenReturn(boldFont);

        hash.put(UXHash.getHash("horizontal-align"), new UXValueText(HorizontalAlign.RIGHT.toString()));
        hash.put(UXHash.getHash("vertical-align"), new UXValueText(VerticalAlign.BOTTOM.toString()));
        hash.put(UXHash.getHash("text"), new UXValueText("A"));
        hash.put(UXHash.getHash("text-hint"), new UXValueText("B"));
        hash.put(UXHash.getHash("text-font"), uxBoldFont);
        hash.put(UXHash.getHash("text-size"), new UXValueSizeSp(24));
        hash.put(UXHash.getHash("text-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("text-hint-color"), new UXValueColor(0x00FF00FF));
        hash.put(UXHash.getHash("text-selected-color"), new UXValueColor(0xFFFF00FF));
        hash.put(UXHash.getHash("on-text-change"), new UXValueText("onTextChangeWork"));
        hash.put(UXHash.getHash("on-text-change-filter"), new UXValueText("onTextChangeFilter"));
        return hash;
    }
}