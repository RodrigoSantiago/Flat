package flat.widget.text;

import flat.events.ActionEvent;
import flat.graphics.context.Font;
import flat.graphics.cursor.Cursor;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueColor;
import flat.uxml.value.UXValueSizeSp;
import flat.uxml.value.UXValueText;
import flat.widget.Widget;
import flat.widget.enums.ImageFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class, Font.class, DrawableReader.class})
public class TextInputFieldTest {

    Controller controller;
    UXBuilder builder;
    Font defaultFont;

    ResourceStream resIcon;
    Drawable icon;

    ResourceStream resIconAction;
    Drawable iconAction;

    @Before
    public void before() {
        mockStatic(Font.class);

        defaultFont = mock(Font.class);
        PowerMockito.when(Font.getDefault()).thenReturn(defaultFont);

        controller = mock(Controller.class);
        builder = mock(UXBuilder.class);

        mockStatic(DrawableReader.class);
        icon = mock(Drawable.class);
        PowerMockito.when(icon.getWidth()).thenReturn(24f);
        PowerMockito.when(icon.getHeight()).thenReturn(16f);

        resIcon = mock(ResourceStream.class);
        PowerMockito.when(DrawableReader.parse(resIcon)).thenReturn(icon);

        iconAction = mock(Drawable.class);
        PowerMockito.when(iconAction.getWidth()).thenReturn(24f);
        PowerMockito.when(iconAction.getHeight()).thenReturn(16f);

        resIconAction = mock(ResourceStream.class);
        PowerMockito.when(DrawableReader.parse(resIconAction)).thenReturn(iconAction);

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
        TextInputField textField = new TextInputField();

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", ActionEvent.class)).thenReturn(action);

        assertNull(textField.getIcon());
        assertEquals(0, textField.getIconSpacing(), 0.001f);
        assertEquals(0xFFFFFFFF, textField.getIconColor());
        assertEquals(ImageFilter.LINEAR, textField.getIconImageFilter());
        assertEquals(0, textField.getIconWidth(), 0.001f);
        assertEquals(0, textField.getIconHeight(), 0.001f);
        assertNull(textField.getActionIcon());
        assertEquals(0, textField.getActionIconSpacing(), 0.001f);
        assertEquals(0xFFFFFFFF, textField.getActionIconColor());
        assertEquals(ImageFilter.LINEAR, textField.getActionIconImageFilter());
        assertEquals(0, textField.getActionIconWidth(), 0.001f);
        assertEquals(0, textField.getActionIconHeight(), 0.001f);
        assertNull(textField.getActionListener());

        textField.setAttributes(createNonDefaultValues(), null);
        textField.applyAttributes(controller);

        assertNull(textField.getIcon());
        assertEquals(0, textField.getIconSpacing(), 0.001f);
        assertEquals(0xFFFFFFFF, textField.getIconColor());
        assertEquals(ImageFilter.LINEAR, textField.getIconImageFilter());
        assertEquals(0, textField.getIconWidth(), 0.001f);
        assertEquals(0, textField.getIconHeight(), 0.001f);
        assertNull(textField.getActionIcon());
        assertEquals(0, textField.getActionIconSpacing(), 0.001f);
        assertEquals(0xFFFFFFFF, textField.getActionIconColor());
        assertEquals(ImageFilter.LINEAR, textField.getActionIconImageFilter());
        assertEquals(0, textField.getActionIconWidth(), 0.001f);
        assertEquals(0, textField.getActionIconHeight(), 0.001f);
        assertEquals(action, textField.getActionListener());

        textField.applyStyle();

        assertEquals(icon, textField.getIcon());
        assertEquals(14, textField.getIconSpacing(), 0.001f);
        assertEquals(0xFF00FFFF, textField.getIconColor());
        assertEquals(ImageFilter.NEAREST, textField.getIconImageFilter());
        assertEquals(16, textField.getIconWidth(), 0.001f);
        assertEquals(18, textField.getIconHeight(), 0.001f);
        assertEquals(iconAction, textField.getActionIcon());
        assertEquals(16, textField.getActionIconSpacing(), 0.001f);
        assertEquals(0xFF0000FF, textField.getActionIconColor());
        assertEquals(ImageFilter.NEAREST, textField.getActionIconImageFilter());
        assertEquals(20, textField.getActionIconWidth(), 0.001f);
        assertEquals(22, textField.getActionIconHeight(), 0.001f);
        assertEquals(action, textField.getActionListener());

        assertTrue(textField.getHorizontalBar().getStyles().contains("text-input-field-horizontal-scroll-bar"));
        assertTrue(textField.getVerticalBar().getStyles().contains("text-input-field-vertical-scroll-bar"));
    }

    @Test
    public void measure() {
        TextInputField inputField = new TextInputField();
        inputField.setText("A");
        inputField.onMeasure();

        assertEquals(16, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16, inputField.getMeasureHeight(), 0.1f);

        inputField.setMargins(1, 2, 3, 4);
        inputField.setPadding(5, 4, 2, 3);
        inputField.onMeasure();

        assertEquals(16 + 13, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16 + 11, inputField.getMeasureHeight(), 0.1f);

        inputField.setPrefSize(100, 200);
        inputField.onMeasure();

        assertEquals(106, inputField.getMeasureWidth(), 0.1f);
        assertEquals(204, inputField.getMeasureHeight(), 0.1f);

        inputField.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        inputField.onMeasure();

        assertEquals(Widget.MATCH_PARENT, inputField.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, inputField.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureTitle() {
        TextInputField inputField = new TextInputField();
        inputField.setText("A");
        inputField.setTitle("B");
        inputField.onMeasure();

        assertEquals(16, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16 + 8, inputField.getMeasureHeight(), 0.1f);

        inputField.setMargins(1, 2, 3, 4);
        inputField.setPadding(5, 4, 2, 3);
        inputField.onMeasure();

        assertEquals(16 + 13, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16 + 8 + 11, inputField.getMeasureHeight(), 0.1f);

        inputField.setPrefSize(100, 200);
        inputField.onMeasure();

        assertEquals(106, inputField.getMeasureWidth(), 0.1f);
        assertEquals(204, inputField.getMeasureHeight(), 0.1f);

        inputField.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        inputField.onMeasure();

        assertEquals(Widget.MATCH_PARENT, inputField.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, inputField.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureIcon() {
        Drawable drawable = mock(Drawable.class);
        PowerMockito.when(drawable.getWidth()).thenReturn(24f);
        PowerMockito.when(drawable.getHeight()).thenReturn(16f);

        TextInputField inputField = new TextInputField();
        inputField.setText("A");
        inputField.setIconWidth(24f);
        inputField.setIconHeight(16f);
        inputField.onMeasure();

        assertEquals(16, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16, inputField.getMeasureHeight(), 0.1f);

        inputField.setIcon(drawable);
        inputField.onMeasure();

        assertEquals(16 + 24, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16, inputField.getMeasureHeight(), 0.1f);

        inputField.setIconHeight(24f);
        inputField.onMeasure();

        assertEquals(16 + 24, inputField.getMeasureWidth(), 0.1f);
        assertEquals(24, inputField.getMeasureHeight(), 0.1f);

        inputField.setIconHeight(16f);
        inputField.setMargins(1, 2, 3, 4);
        inputField.setPadding(5, 4, 2, 3);
        inputField.onMeasure();

        assertEquals(16 + 24 + 13, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16 + 11, inputField.getMeasureHeight(), 0.1f);

        inputField.setPrefSize(100, 200);
        inputField.onMeasure();

        assertEquals(106, inputField.getMeasureWidth(), 0.1f);
        assertEquals(204, inputField.getMeasureHeight(), 0.1f);

        inputField.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        inputField.onMeasure();

        assertEquals(Widget.MATCH_PARENT, inputField.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, inputField.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureIconSpacing() {
        Drawable drawable = mock(Drawable.class);
        PowerMockito.when(drawable.getWidth()).thenReturn(24f);
        PowerMockito.when(drawable.getHeight()).thenReturn(16f);

        TextInputField inputField = new TextInputField();
        inputField.setText("A");
        inputField.setIconSpacing(8f);
        inputField.setIconWidth(24f);
        inputField.setIconHeight(16f);
        inputField.onMeasure();

        assertEquals(16, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16, inputField.getMeasureHeight(), 0.1f);

        inputField.setIcon(drawable);
        inputField.onMeasure();

        assertEquals(16 + 24 + 8, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16, inputField.getMeasureHeight(), 0.1f);

        inputField.setActionIconHeight(16f);
        inputField.setMargins(1, 2, 3, 4);
        inputField.setPadding(5, 4, 2, 3);
        inputField.onMeasure();

        assertEquals(16 + 24 + 8 + 13, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16 + 11, inputField.getMeasureHeight(), 0.1f);

        inputField.setPrefSize(100, 200);
        inputField.onMeasure();

        assertEquals(106, inputField.getMeasureWidth(), 0.1f);
        assertEquals(204, inputField.getMeasureHeight(), 0.1f);

        inputField.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        inputField.onMeasure();

        assertEquals(Widget.MATCH_PARENT, inputField.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, inputField.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureActionIcon() {
        Drawable drawable = mock(Drawable.class);
        PowerMockito.when(drawable.getWidth()).thenReturn(24f);
        PowerMockito.when(drawable.getHeight()).thenReturn(16f);

        TextInputField inputField = new TextInputField();
        inputField.setText("A");
        inputField.setActionIconWidth(24f);
        inputField.setActionIconHeight(16f);
        inputField.onMeasure();

        assertEquals(16, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16, inputField.getMeasureHeight(), 0.1f);

        inputField.setActionIcon(drawable);
        inputField.onMeasure();

        assertEquals(16 + 24, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16, inputField.getMeasureHeight(), 0.1f);

        inputField.setActionIconHeight(24f);
        inputField.onMeasure();

        assertEquals(16 + 24, inputField.getMeasureWidth(), 0.1f);
        assertEquals(24, inputField.getMeasureHeight(), 0.1f);

        inputField.setActionIconHeight(16f);
        inputField.setMargins(1, 2, 3, 4);
        inputField.setPadding(5, 4, 2, 3);
        inputField.onMeasure();

        assertEquals(16 + 24 + 13, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16 + 11, inputField.getMeasureHeight(), 0.1f);

        inputField.setPrefSize(100, 200);
        inputField.onMeasure();

        assertEquals(106, inputField.getMeasureWidth(), 0.1f);
        assertEquals(204, inputField.getMeasureHeight(), 0.1f);

        inputField.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        inputField.onMeasure();

        assertEquals(Widget.MATCH_PARENT, inputField.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, inputField.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureActionIconSpacing() {
        Drawable drawable = mock(Drawable.class);
        PowerMockito.when(drawable.getWidth()).thenReturn(24f);
        PowerMockito.when(drawable.getHeight()).thenReturn(16f);

        TextInputField inputField = new TextInputField();
        inputField.setText("A");
        inputField.setActionIconSpacing(8f);
        inputField.setActionIconWidth(24f);
        inputField.setActionIconHeight(16f);
        inputField.onMeasure();

        assertEquals(16, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16, inputField.getMeasureHeight(), 0.1f);

        inputField.setActionIcon(drawable);
        inputField.onMeasure();

        assertEquals(16 + 24 + 8, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16, inputField.getMeasureHeight(), 0.1f);

        inputField.setActionIconHeight(16f);
        inputField.setMargins(1, 2, 3, 4);
        inputField.setPadding(5, 4, 2, 3);
        inputField.onMeasure();

        assertEquals(16 + 24 + 8 + 13, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16 + 11, inputField.getMeasureHeight(), 0.1f);

        inputField.setPrefSize(100, 200);
        inputField.onMeasure();

        assertEquals(106, inputField.getMeasureWidth(), 0.1f);
        assertEquals(204, inputField.getMeasureHeight(), 0.1f);

        inputField.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        inputField.onMeasure();

        assertEquals(Widget.MATCH_PARENT, inputField.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, inputField.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureComplete() {
        Drawable drawable = mock(Drawable.class);
        PowerMockito.when(drawable.getWidth()).thenReturn(24f);
        PowerMockito.when(drawable.getHeight()).thenReturn(16f);
        Drawable drawableB = mock(Drawable.class);
        PowerMockito.when(drawableB.getWidth()).thenReturn(22f);
        PowerMockito.when(drawableB.getHeight()).thenReturn(18f);

        TextInputField inputField = new TextInputField();
        inputField.setText("A");
        inputField.setIcon(drawable);
        inputField.setIconWidth(24f);
        inputField.setIconHeight(16f);
        inputField.setIconSpacing(2f);
        inputField.setActionIcon(drawableB);
        inputField.setActionIconWidth(22f);
        inputField.setActionIconHeight(18f);
        inputField.setActionIconSpacing(4f);
        inputField.onMeasure();

        assertEquals(16 + 24 + 22 + 2 + 4, inputField.getMeasureWidth(), 0.1f);
        assertEquals(18, inputField.getMeasureHeight(), 0.1f);

        inputField.setActionIconHeight(24f);
        inputField.onMeasure();

        assertEquals(16 + 24 + 22 + 2 + 4, inputField.getMeasureWidth(), 0.1f);
        assertEquals(24, inputField.getMeasureHeight(), 0.1f);

        inputField.setActionIconHeight(16f);
        inputField.setMargins(1, 2, 3, 4);
        inputField.setPadding(5, 4, 2, 3);
        inputField.onMeasure();

        assertEquals(16 + 24 + 22 + 2 + 4 + 13, inputField.getMeasureWidth(), 0.1f);
        assertEquals(16 + 11, inputField.getMeasureHeight(), 0.1f);

        inputField.setPrefSize(100, 200);
        inputField.onMeasure();

        assertEquals(106, inputField.getMeasureWidth(), 0.1f);
        assertEquals(204, inputField.getMeasureHeight(), 0.1f);

        inputField.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        inputField.onMeasure();

        assertEquals(Widget.MATCH_PARENT, inputField.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, inputField.getMeasureHeight(), 0.1f);
    }

    @Test
    public void action() {
        TextInputField inputField = new TextInputField();

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        inputField.setActionListener(action);

        inputField.action();

        verify(action, times(1)).handle(any());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxIcon = mock(UXValue.class);
        PowerMockito.when(uxIcon.asResource(any())).thenReturn(resIcon);

        UXValue uxActionIcon = mock(UXValue.class);
        PowerMockito.when(uxActionIcon.asResource(any())).thenReturn(resIconAction);

        hash.put(UXHash.getHash("icon"), uxIcon);
        hash.put(UXHash.getHash("icon-color"), new UXValueColor(0xFF00FFFF));
        hash.put(UXHash.getHash("icon-width"), new UXValueSizeSp(16));
        hash.put(UXHash.getHash("icon-height"), new UXValueSizeSp(18));
        hash.put(UXHash.getHash("icon-spacing"), new UXValueSizeSp(14));
        hash.put(UXHash.getHash("icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));

        hash.put(UXHash.getHash("on-action"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("action-icon"), uxActionIcon);
        hash.put(UXHash.getHash("action-icon-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("action-icon-bg-color"), new UXValueColor(0xFF00F0FF));
        hash.put(UXHash.getHash("action-icon-width"), new UXValueSizeSp(20));
        hash.put(UXHash.getHash("action-icon-height"), new UXValueSizeSp(22));
        hash.put(UXHash.getHash("action-icon-spacing"), new UXValueSizeSp(16));
        hash.put(UXHash.getHash("action-icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));
        hash.put(UXHash.getHash("action-icon-cursor"), new UXValueText(Cursor.HAND.toString()));

        return hash;
    }
}