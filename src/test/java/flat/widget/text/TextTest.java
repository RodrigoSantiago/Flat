package flat.widget.text;

import flat.graphics.symbols.Font;
import flat.graphics.symbols.FontManager;
import flat.uxml.Controller;
import flat.uxml.UXBuilder;
import flat.uxml.UXHash;
import flat.uxml.value.*;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.window.Activity;
import flat.window.ActivitySupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.nio.ByteBuffer;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Font.class, FontManager.class})
public class TextTest {

    Controller controller;
    UXBuilder builder;
    Font defaultFont;
    Font boldFont;

    @Before
    public void before() {
        controller = mock(Controller.class);
        builder = mock(UXBuilder.class);

        mockStatic(Font.class);
        mockStatic(FontManager.class);

        defaultFont = mock(Font.class);
        boldFont = mock(Font.class);
        when(Font.getDefault()).thenReturn(defaultFont);

        when(FontManager.findFont(any(), any(), any(), any())).thenReturn(defaultFont);
        when(defaultFont.getHeight(anyFloat())).thenAnswer((a) -> (Float) a.getArgument(0));
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenAnswer((a) -> {
            String text = a.getArgument(0);
            Float size = a.getArgument(1);
            return text.length() * size;
        });
        when(defaultFont.getWidth(any(), anyInt(), anyInt(), anyFloat(), anyFloat())).thenAnswer((a) -> {
            Integer length = a.getArgument(2);
            Float size = a.getArgument(3);
            return length * size;
        });
        when(defaultFont.getCaretOffset(any(), anyInt(), anyInt(), anyFloat(), anyFloat(), anyFloat(), anyBoolean())).thenAnswer((a) -> {
            Integer length = a.getArgument(2);
            Float size = a.getArgument(3);
            Float x = a.getArgument(5);
            Boolean half = a.getArgument(6);
            if (length * size == 0 || x <= 0) {
                return new Font.CaretData(0, 0);
            } else if (x >= size * length) {
                return new Font.CaretData(length, size * length);
            } else {
                int index = half ? Math.round(x / size) : (int) Math.floor(x / size);
                return new Font.CaretData(index, index * size);
            }
        });
        when(defaultFont.getCaretOffsetSpace(any(), anyInt(), anyInt(), anyFloat(), anyFloat(), anyFloat())).thenAnswer((a) -> {
            Integer length = a.getArgument(2);
            Float size = a.getArgument(3);
            Float x = a.getArgument(5);
            if (length * size == 0 || x <= 0) {
                return new Font.CaretData(0, 0);
            } else if (x >= size * length) {
                return new Font.CaretData(length, size * length);
            } else {
                int index = (int) Math.floor(x / size);
                return new Font.CaretData(index, index * size);
            }
        });
        when(defaultFont.getLineWrap(any(), anyInt(), anyInt(), anyFloat(), anyFloat(), anyFloat())).thenAnswer((a) -> {
            ByteBuffer text = a.getArgument(0);
            Integer offset = a.getArgument(1);
            Integer length = a.getArgument(2);
            Float size = a.getArgument(3);
            Float x = a.getArgument(5);
            if (length * size == 0 || x <= 0) {
                return 1;
            } else if (x >= size * length) {
                int lines = 1;
                for (int i = 0; i < length; i++) {
                    if (text.get(offset + i) == '\n') lines++;
                }
                return lines;
            } else {
                int maxChars = (int) (Math.floor(x / size) * size);
                int lines = 1;
                int chars = 0;
                for (int i = 0; i < length; i++) {
                    if (text.get(offset + i) == '\n' || chars >= maxChars) {
                        lines++;
                        chars = 0;
                    } else {
                        chars++;
                    }
                }
                return lines;
            }
        });
    }

    @Test
    public void properties() {
        Text text = new Text();

        assertEquals(HorizontalAlign.LEFT, text.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, text.getVerticalAlign());
        assertFalse(text.isTextAllCaps());
        assertEquals(defaultFont, text.getTextFont());
        assertEquals(16f, text.getTextSize(), 0.1f);
        assertEquals(0x000000FF, text.getTextColor());
        assertNull(text.getText());

        text.setAttributes(createNonDefaultValues(), null);
        text.applyAttributes(null);

        assertEquals(HorizontalAlign.LEFT, text.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, text.getVerticalAlign());
        assertFalse(text.isTextAllCaps());
        assertEquals(defaultFont, text.getTextFont());
        assertEquals(16f, text.getTextSize(), 0.1f);
        assertEquals(0x000000FF, text.getTextColor());
        assertEquals("Hello World", text.getText());

        text.applyStyle();

        assertEquals(HorizontalAlign.RIGHT, text.getHorizontalAlign());
        assertEquals(VerticalAlign.BOTTOM, text.getVerticalAlign());
        assertTrue(text.isTextAllCaps());
        assertEquals(boldFont, text.getTextFont());
        assertEquals(24f, text.getTextSize(), 0.1f);
        assertEquals(0xFF0000FF, text.getTextColor());
        assertEquals("Hello World", text.getText());
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
        Text text = new Text();
        text.setTextSize(32);
        text.setText("Hello World");
        text.onMeasure();

        assertEquals(11 * 32, text.getMeasureWidth(), 0.1f);
        assertEquals(32, text.getMeasureHeight(), 0.1f);

        text.setMargins(1, 2, 3, 4);
        text.setPadding(5, 4, 2, 3);
        text.onMeasure();

        assertEquals(11 * 32 + 13, text.getMeasureWidth(), 0.1f);
        assertEquals(43, text.getMeasureHeight(), 0.1f);

        text.setPrefSize(100, 200);
        text.onMeasure();

        assertEquals(106, text.getMeasureWidth(), 0.1f);
        assertEquals(204, text.getMeasureHeight(), 0.1f);

        text.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        text.onMeasure();

        assertEquals(Widget.MATCH_PARENT, text.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, text.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measurebreakLine() {
        Activity activity = mock(Activity.class);
        Runnable[] task = new Runnable[1];
        when(activity.runLater((Runnable) any())).thenAnswer((a) -> {
            task[0] = a.getArgument(0);
            return null;
        });
        Scene scene = new Scene();
        ActivitySupport.setActivity(scene, activity);
        when(activity.getScene()).thenReturn(scene);

        Text text = new Text();
        text.setTextSize(32);
        text.setText("Hello World");
        scene.add(text);

        text.onMeasure();
        assertEquals(11 * 32, text.getMeasureWidth(), 0.1f);
        assertEquals(32, text.getMeasureHeight(), 0.1f);

        text.onLayout(6 * 32, 32);
        assertEquals(6 * 32, text.getLayoutWidth(), 0.1f);
        assertEquals(32, text.getLayoutHeight(), 0.1f);

        task[0].run();
        text.onMeasure();
        assertEquals(11 * 32, text.getMeasureWidth(), 0.1f);
        assertEquals(2 * 32, text.getMeasureHeight(), 0.1f);

        text.onLayout(6 * 32, 2 * 32);
        assertEquals(6 * 32, text.getLayoutWidth(), 0.1f);
        assertEquals(2 * 32, text.getLayoutHeight(), 0.1f);
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