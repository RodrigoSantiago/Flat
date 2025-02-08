package flat.widget.text;

import flat.graphics.context.Context;
import flat.graphics.context.Font;
import flat.uxml.*;
import flat.uxml.value.*;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.window.Activity;
import flat.window.Window;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Font.class})
public class LabelTest {

    Window window;
    Context context;
    Activity activity;
    UXTheme theme;
    Controller controller;
    UXBuilder builder;
    Font defaultFont;
    Font boldFont;

    @Before
    public void before() {
        window = mock(Window.class);
        context = mock(Context.class);

        when(context.getWindow()).thenReturn(window);
        when(context.getWidth()).thenReturn(200);
        when(context.getHeight()).thenReturn(100);

        activity = mock(Activity.class);
        when(activity.getContext()).thenReturn(context);
        when(activity.getWindow()).thenReturn(window);
        when(activity.getWidth()).thenReturn(200f);
        when(activity.getHeight()).thenReturn(100f);

        theme = mock(UXTheme.class);
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
        label.setAttributes(createNonDefaultValues(), "label");
        label.applyAttributes(null);
        label.applyStyle();

        assertEquals(HorizontalAlign.RIGHT, label.getHorizontalAlign());
        assertEquals(VerticalAlign.BOTTOM, label.getVerticalAlign());
        assertEquals("Hello World", label.getText());
        assertTrue(label.isTextAllCaps());
        assertEquals(boldFont, label.getFont());
        assertEquals(24f, label.getTextSize(), 0.1f);
        assertEquals(0xFF0000FF, label.getTextColor());
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
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(165f);
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
        hash.put(UXHash.getHash("font"), uxBoldFont);
        hash.put(UXHash.getHash("text-size"), new UXValueSizeSp(24));
        hash.put(UXHash.getHash("text-color"), new UXValueColor(0xFF0000FF));
        return hash;
    }
}