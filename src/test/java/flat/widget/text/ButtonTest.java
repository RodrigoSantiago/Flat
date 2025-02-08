package flat.widget.text;

import flat.events.ActionEvent;
import flat.graphics.context.Font;
import flat.graphics.image.Drawable;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXListener;
import flat.uxml.value.*;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.ImageFilter;
import flat.widget.enums.VerticalAlign;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Font.class})
public class ButtonTest {

    Font boldFont;
    Font defaultFont;

    @Before
    public void before() {
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
        Controller controller = mock(Controller.class);
        UXListener<ActionEvent> action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", ActionEvent.class)).thenReturn(action);

        Button button = new Button();
        button.setAttributes(createNonDefaultValues(), "button");
        button.applyAttributes(controller);
        button.applyStyle();

        assertTrue(button.getIconScaleHeight());
        assertEquals(button.getIconAlign(), HorizontalAlign.RIGHT);
        assertEquals(button.getIconSpacing(), 24, 0.1f);
        assertEquals(button.getIconImageFilter(), ImageFilter.LINEAR);

        assertEquals(action, button.getActionListener());
    }

    @Test
    public void measure() {
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);

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
    public void measureIcon() {
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);

        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Button button = new Button();
        button.setText("Hello World");
        button.setIconImage(drawable);

        button.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        button.onMeasure();

        assertEquals(165 + 24, button.getMeasureWidth(), 0.1f);
        assertEquals(32, button.getMeasureHeight(), 0.1f);

        when(drawable.getHeight()).thenReturn(64f);
        button.onMeasure();

        assertEquals(165 + 24, button.getMeasureWidth(), 0.1f);
        assertEquals(64, button.getMeasureHeight(), 0.1f);

        when(drawable.getHeight()).thenReturn(16f);
        button.setMargins(1, 2, 3, 4);
        button.setPadding(5, 4, 2, 3);
        button.onMeasure();

        assertEquals(178 + 24, button.getMeasureWidth(), 0.1f);
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
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);

        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Button button = new Button();
        button.setText("Hello World");
        button.setIconImage(drawable);
        button.setIconSpacing(8f);

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
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);

        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Button button = new Button();
        button.setText("Hello World");
        button.setIconImage(drawable);
        button.setIconScaleHeight(true);

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


    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();
        UXValue uxBoldFont = mock(UXValue.class);
        when(uxBoldFont.asFont(any())).thenReturn(boldFont);

        hash.put(UXHash.getHash("icon-scale-height"), new UXValueBool(true));
        hash.put(UXHash.getHash("icon-align"), new UXValueText(HorizontalAlign.RIGHT.toString()));
        hash.put(UXHash.getHash("icon-spacing"), new UXValueSizeSp(24));
        hash.put(UXHash.getHash("icon-image-filter"), new UXValueText(ImageFilter.LINEAR.toString()));
        hash.put(UXHash.getHash("on-action"), new UXValueText("onActionWork"));

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
