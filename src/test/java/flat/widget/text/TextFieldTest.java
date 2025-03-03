package flat.widget.text;

import flat.events.TextEvent;
import flat.graphics.context.Font;
import flat.uxml.*;
import flat.uxml.value.*;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.widget.structure.ListView;
import flat.widget.value.HorizontalScrollBar;
import flat.widget.value.VerticalScrollBar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
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

        when(defaultFont.getHeight(eq(8f))).thenReturn(8f);
        when(defaultFont.getWidth(any(), eq(8f), anyFloat())).thenReturn(8f);
        when(defaultFont.getWidth(any(), eq(0), eq(0), eq(8f), anyFloat())).thenReturn(0f);
        when(defaultFont.getWidth(any(), eq(0), eq(1), eq(8f), anyFloat())).thenReturn(8f);
        when(defaultFont.getCaretOffset(any(), eq(0), eq(0), eq(8f), anyFloat(), anyFloat(), anyBoolean())).thenReturn(new Font.CaretData(0, 0));
        when(defaultFont.getCaretOffset(any(), eq(0), eq(1), eq(8f), anyFloat(), anyFloat(), anyBoolean())).thenReturn(new Font.CaretData(1, 8f));
    }

    @Test
    public void properties() {
        TextField textField = new TextField();

        var action = (UXValueListener<String>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onTextChangeWork", String.class)).thenReturn(action);

        var filter = (UXListener<TextEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onTextChangeFilter", TextEvent.class)).thenReturn(filter);

        var inputFilter = (UXListener<TextEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onTextInputFilter", TextEvent.class)).thenReturn(inputFilter);

        assertEquals(HorizontalAlign.LEFT, textField.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, textField.getVerticalAlign());
        assertEquals(defaultFont, textField.getTextFont());
        assertEquals(16f, textField.getTextSize(), 0.1f);
        assertEquals(8f, textField.getTitleSize(), 0.1f);
        assertEquals(0f, textField.getTitleSpacing(), 0.1f);
        assertEquals(1f, textField.getTitleTransitionDuration(), 0.1f);
        assertEquals(0.5f, textField.getCaretBlinkDuration(), 0.1f);
        assertEquals(0x000000FF, textField.getTextColor());
        assertEquals(0x000000FF, textField.getTitleColor());
        assertEquals(0x00000080, textField.getTextSelectedColor());
        assertEquals(0x000000FF, textField.getCaretColor());
        assertEquals(0x000000FF, textField.getTextDividerColor());
        assertEquals(0f, textField.getTextDividerSize(), 0.001f);
        assertFalse(textField.isTitleLocked());
        assertNull(textField.getText());
        assertNull(textField.getTitle());
        assertNull(textField.getTextChangeFilter());
        assertNull(textField.getTextChangeListener());
        assertNull(textField.getTextInputFilter());

        textField.setAttributes(createNonDefaultValues(), "text-field");
        textField.applyAttributes(controller);

        assertEquals(HorizontalAlign.LEFT, textField.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, textField.getVerticalAlign());
        assertEquals(defaultFont, textField.getTextFont());
        assertEquals(16f, textField.getTextSize(), 0.1f);
        assertEquals(8f, textField.getTitleSize(), 0.1f);
        assertEquals(0f, textField.getTitleSpacing(), 0.1f);
        assertEquals(1f, textField.getTitleTransitionDuration(), 0.1f);
        assertEquals(0.5f, textField.getCaretBlinkDuration(), 0.1f);
        assertEquals(0x000000FF, textField.getTextColor());
        assertEquals(0x000000FF, textField.getTitleColor());
        assertEquals(0x00000080, textField.getTextSelectedColor());
        assertEquals(0x000000FF, textField.getCaretColor());
        assertEquals(0x000000FF, textField.getTextDividerColor());
        assertEquals(0f, textField.getTextDividerSize(), 0.001f);
        assertFalse(textField.isTitleLocked());
        assertEquals("A", textField.getText());
        assertEquals("B", textField.getTitle());
        assertEquals(filter, textField.getTextChangeFilter());
        assertEquals(action, textField.getTextChangeListener());
        assertEquals(inputFilter, textField.getTextInputFilter());

        textField.applyStyle();

        assertEquals(HorizontalAlign.RIGHT, textField.getHorizontalAlign());
        assertEquals(VerticalAlign.BOTTOM, textField.getVerticalAlign());
        assertEquals(boldFont, textField.getTextFont());
        assertEquals(24f, textField.getTextSize(), 0.1f);
        assertEquals(12f, textField.getTitleSize(), 0.1f);
        assertEquals(2f, textField.getTitleSpacing(), 0.1f);
        assertEquals(0.5f, textField.getTitleTransitionDuration(), 0.1f);
        assertEquals(0.25f, textField.getCaretBlinkDuration(), 0.1f);
        assertEquals(0xFF0000FF, textField.getTextColor());
        assertEquals(0x00FF00FF, textField.getTitleColor());
        assertEquals(0xFFFF00FF, textField.getTextSelectedColor());
        assertEquals(0x0000FFFF, textField.getCaretColor());
        assertEquals(0x00FFFFFF, textField.getTextDividerColor());
        assertEquals(1f, textField.getTextDividerSize(), 0.001f);
        assertTrue(textField.isTitleLocked());
        assertEquals("A", textField.getText());
        assertEquals("B", textField.getTitle());
        assertEquals(filter, textField.getTextChangeFilter());
        assertEquals(action, textField.getTextChangeListener());
        assertEquals(inputFilter, textField.getTextInputFilter());
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

        assertEquals(16 + 13, textField.getMeasureWidth(), 0.1f);
        assertEquals(16 + 11, textField.getMeasureHeight(), 0.1f);

        textField.setPrefSize(100, 200);
        textField.onMeasure();

        assertEquals(106, textField.getMeasureWidth(), 0.1f);
        assertEquals(204, textField.getMeasureHeight(), 0.1f);

        textField.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        textField.onMeasure();

        assertEquals(Widget.MATCH_PARENT, textField.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, textField.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureTitle() {
        TextField textField = new TextField();
        textField.setText("A");
        textField.setTitle("B");
        textField.onMeasure();

        assertEquals(16, textField.getMeasureWidth(), 0.1f);
        assertEquals(16 + 8, textField.getMeasureHeight(), 0.1f);

        textField.setMargins(1, 2, 3, 4);
        textField.setPadding(5, 4, 2, 3);
        textField.onMeasure();

        assertEquals(16 + 13, textField.getMeasureWidth(), 0.1f);
        assertEquals(16 + 8 + 11, textField.getMeasureHeight(), 0.1f);

        textField.setPrefSize(100, 200);
        textField.onMeasure();

        assertEquals(106, textField.getMeasureWidth(), 0.1f);
        assertEquals(204, textField.getMeasureHeight(), 0.1f);

        textField.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        textField.onMeasure();

        assertEquals(Widget.MATCH_PARENT, textField.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, textField.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureTitleSpacing() {
        TextField textField = new TextField();
        textField.setText("A");
        textField.setTitleSpacing(2f);
        textField.onMeasure();

        assertEquals(16, textField.getMeasureWidth(), 0.1f);
        assertEquals(16, textField.getMeasureHeight(), 0.1f);

        textField.setTitle("B");
        textField.onMeasure();

        assertEquals(16, textField.getMeasureWidth(), 0.1f);
        assertEquals(16 + 8 + 2, textField.getMeasureHeight(), 0.1f);

        textField.setMargins(1, 2, 3, 4);
        textField.setPadding(5, 4, 2, 3);
        textField.onMeasure();

        assertEquals(16 + 13, textField.getMeasureWidth(), 0.1f);
        assertEquals(16 + 8 + 2 + 11, textField.getMeasureHeight(), 0.1f);

        textField.setPrefSize(100, 200);
        textField.onMeasure();

        assertEquals(106, textField.getMeasureWidth(), 0.1f);
        assertEquals(204, textField.getMeasureHeight(), 0.1f);

        textField.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        textField.onMeasure();

        assertEquals(Widget.MATCH_PARENT, textField.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, textField.getMeasureHeight(), 0.1f);
    }

    @Test
    public void childrenFromUx() {
        TextField textField = new TextField();
        HorizontalScrollBar horBar = new HorizontalScrollBar();
        VerticalScrollBar verBar = new VerticalScrollBar();

        UXChildren uxChild = mockChildren(
                new Widget[] {horBar, verBar},
                new String[] {"horizontal-bar", "vertical-bar"});

        assertNull(horBar.getParent());
        assertNull(verBar.getParent());

        textField.applyChildren(uxChild);

        assertEquals(textField, horBar.getParent());
        assertEquals(textField, verBar.getParent());

        assertEquals(horBar, textField.getHorizontalBar());
        assertEquals(verBar, textField.getVerticalBar());
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
        hash.put(UXHash.getHash("title"), new UXValueText("B"));
        hash.put(UXHash.getHash("text-font"), uxBoldFont);
        hash.put(UXHash.getHash("text-size"), new UXValueSizeSp(24f));
        hash.put(UXHash.getHash("title-size"), new UXValueSizeSp(12f));
        hash.put(UXHash.getHash("title-spacing"), new UXValueSizeSp(2f));
        hash.put(UXHash.getHash("title-transition-duration"), new UXValueSizeSp(0.5f));
        hash.put(UXHash.getHash("title-locked"), new UXValueBool(true));
        hash.put(UXHash.getHash("text-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("title-color"), new UXValueColor(0x00FF00FF));
        hash.put(UXHash.getHash("text-selected-color"), new UXValueColor(0xFFFF00FF));
        hash.put(UXHash.getHash("caret-color"), new UXValueColor(0x0000FFFF));
        hash.put(UXHash.getHash("caret-blink-duration"), new UXValueNumber(0.25f));
        hash.put(UXHash.getHash("text-divider-color"), new UXValueColor(0x00FFFFFF));
        hash.put(UXHash.getHash("text-divider-size"), new UXValueSizeSp(1f));
        hash.put(UXHash.getHash("on-text-change"), new UXValueText("onTextChangeWork"));
        hash.put(UXHash.getHash("on-text-change-filter"), new UXValueText("onTextChangeFilter"));
        hash.put(UXHash.getHash("on-text-input-filter"), new UXValueText("onTextInputFilter"));
        return hash;
    }

    private UXChildren mockChildren(Widget[] widgets, String[] booleans) {
        UXChildren uxChild = mock(UXChildren.class);
        ArrayList<UXChild> children = new ArrayList<>();
        for (int i = 0; i < widgets.length; i++) {
            var widget = widgets[i];
            HashMap<Integer, UXValue> attributes = null;
            if (booleans != null && i < booleans.length) {
                attributes = new HashMap<>();
                attributes.put(UXHash.getHash(booleans[i]), new UXValueBool(true));
            }
            children.add(new UXChild(widget, attributes));
        }
        PowerMockito.when(uxChild.iterator()).thenReturn(children.iterator());
        return uxChild;
    }
}