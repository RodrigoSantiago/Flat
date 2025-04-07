package flat.widget.structure;

import flat.events.ActionEvent;
import flat.graphics.symbols.Font;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXListener;
import flat.uxml.UXNode;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class, Font.class, DrawableReader.class})
public class ListItemTest {

    Font boldFont;
    Font defaultFont;

    ResourceStream resIcon;
    Drawable icon;
    ResourceStream resStateIconOpen;
    Drawable stateIconOpen;

    @Before
    public void before() {
        mockStatic(Font.class);

        defaultFont = mock(Font.class);
        boldFont = mock(Font.class);
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

        stateIconOpen = mock(Drawable.class);
        when(stateIconOpen.getWidth()).thenReturn(20f);
        when(stateIconOpen.getHeight()).thenReturn(18f);

        resStateIconOpen = mock(ResourceStream.class);
        when(DrawableReader.parse(resStateIconOpen)).thenReturn(stateIconOpen);

        when(defaultFont.getWidth(any(), anyInt(), anyInt(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", ActionEvent.class)).thenReturn(action);

        var changeStateAction = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onChangeStateActionWork", ActionEvent.class)).thenReturn(changeStateAction);

        ListItem listItem = new ListItem();

        assertFalse(listItem.isTextAllCaps());
        assertEquals(defaultFont, listItem.getTextFont());
        assertEquals(16f, listItem.getTextSize(), 0.1f);
        assertEquals(0x000000FF, listItem.getTextColor());
        assertNull(listItem.getText());

        assertEquals(0, listItem.getIconWidth(), 0.001f);
        assertEquals(0, listItem.getIconHeight(), 0.001f);
        assertEquals(0, listItem.getIconSpacing(), 0.1f);
        assertEquals(ImageFilter.LINEAR, listItem.getIconImageFilter());
        assertNull(listItem.getIcon());
        assertEquals(0xFFFFFFFF, listItem.getIconColor());
        assertEquals(0, listItem.getStateIconWidth(), 0.001f);
        assertEquals(0, listItem.getStateIconHeight(), 0.001f);
        assertEquals(0, listItem.getStateIconSpacing(), 0.1f);
        assertEquals(ImageFilter.LINEAR, listItem.getStateIconImageFilter());
        assertNull(listItem.getStateIcon());
        assertEquals(0xFFFFFFFF, listItem.getStateIconColor());
        assertNull(listItem.getActionListener());
        assertNull(listItem.getChangeStateListener());

        listItem.setAttributes(createNonDefaultValues(), null);
        listItem.applyAttributes(controller);

        assertFalse(listItem.isTextAllCaps());
        assertEquals(defaultFont, listItem.getTextFont());
        assertEquals(16f, listItem.getTextSize(), 0.1f);
        assertEquals(0x000000FF, listItem.getTextColor());
        assertEquals("Hello World", listItem.getText());

        assertEquals(0, listItem.getIconWidth(), 0.001f);
        assertEquals(0, listItem.getIconHeight(), 0.001f);
        assertEquals(0, listItem.getStateIconSpacing(), 0.1f);
        assertEquals(ImageFilter.LINEAR, listItem.getIconImageFilter());
        assertNull(listItem.getIcon());
        assertEquals(0xFFFFFFFF, listItem.getStateIconColor());
        assertEquals(0, listItem.getStateIconWidth(), 0.001f);
        assertEquals(0, listItem.getStateIconHeight(), 0.001f);
        assertEquals(0, listItem.getIconSpacing(), 0.1f);
        assertEquals(ImageFilter.LINEAR, listItem.getStateIconImageFilter());
        assertNull(listItem.getStateIcon());
        assertEquals(0xFFFFFFFF, listItem.getStateIconColor());
        assertEquals(action, listItem.getActionListener());
        assertEquals(changeStateAction, listItem.getChangeStateListener());

        listItem.applyStyle();

        assertTrue(listItem.isTextAllCaps());
        assertEquals(boldFont, listItem.getTextFont());
        assertEquals(24f, listItem.getTextSize(), 0.1f);
        assertEquals(0xFF0000FF, listItem.getTextColor());
        assertEquals("Hello World", listItem.getText());

        assertEquals(16, listItem.getIconWidth(), 0.001f);
        assertEquals(18, listItem.getIconHeight(), 0.001f);
        assertEquals(24, listItem.getIconSpacing(), 0.1f);
        assertEquals(ImageFilter.NEAREST, listItem.getIconImageFilter());
        assertEquals(icon, listItem.getIcon());
        assertEquals(0xFFFF00FF, listItem.getIconColor());
        assertEquals(20, listItem.getStateIconWidth(), 0.001f);
        assertEquals(22, listItem.getStateIconHeight(), 0.001f);
        assertEquals(16, listItem.getStateIconSpacing(), 0.1f);
        assertEquals(ImageFilter.NEAREST, listItem.getStateIconImageFilter());
        assertEquals(stateIconOpen, listItem.getStateIcon());
        assertEquals(0xFF0000FF, listItem.getStateIconColor());
        assertEquals(action, listItem.getActionListener());
        assertEquals(changeStateAction, listItem.getChangeStateListener());
    }

    @Test
    public void layers() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        ListItem listItem = new ListItem();
        listItem.setText("Hello World");
        listItem.setIcon(drawable);
        listItem.setIconWidth(32);
        listItem.setIconHeight(24);

        listItem.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        listItem.onMeasure();

        assertEquals(165 + 32, listItem.getMeasureWidth(), 0.1f);
        assertEquals(32, listItem.getMeasureHeight(), 0.1f);
        assertEquals(165 + 32, listItem.getLayoutMinWidth(), 0.1f); // List Item have special min width
        assertEquals(0, listItem.getLayoutMinHeight(), 0.1f);

        listItem.setLayerWidth(16);
        listItem.setLayers(2);
        listItem.onMeasure();

        assertEquals(165 + 32 + (2 * 16), listItem.getMeasureWidth(), 0.1f);
        assertEquals(32, listItem.getMeasureHeight(), 0.1f);
        assertEquals(165 + 32 + 2 * 16, listItem.getLayoutMinWidth(), 0.1f);
        assertEquals(0, listItem.getLayoutMinHeight(), 0.1f);

        listItem.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        listItem.onMeasure();

        assertEquals(Widget.MATCH_PARENT, listItem.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, listItem.getMeasureHeight(), 0.1f);
        assertEquals(165 + 32 + 2 * 16, listItem.getLayoutMinWidth(), 0.1f);
        assertEquals(0, listItem.getLayoutMinHeight(), 0.1f);
    }

    @Test
    public void iconSize() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        ListItem listItem = new ListItem();
        listItem.setText("Hello World");
        listItem.setIcon(drawable);
        listItem.setIconWidth(Widget.WRAP_CONTENT);
        listItem.setIconHeight(Widget.WRAP_CONTENT);

        listItem.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        listItem.onMeasure();

        assertEquals(165 + 32, listItem.getMeasureWidth(), 0.1f);
        assertEquals(32, listItem.getMeasureHeight(), 0.1f);

        listItem.setIconWidth(58f);
        listItem.setIconHeight(64f);
        listItem.onMeasure();

        assertEquals(165 + 58f, listItem.getMeasureWidth(), 0.1f);
        assertEquals(64, listItem.getMeasureHeight(), 0.1f);

        listItem.setIconWidth(32f);
        listItem.setIconHeight(16f);
        listItem.setMargins(1, 2, 3, 4);
        listItem.setPadding(5, 4, 2, 3);
        listItem.onMeasure();

        assertEquals(178 + 32, listItem.getMeasureWidth(), 0.1f);
        assertEquals(43, listItem.getMeasureHeight(), 0.1f);

        listItem.setPrefSize(100, 200);
        listItem.onMeasure();

        assertEquals(165 + 32 + 13, listItem.getMeasureWidth(), 0.1f); // Special Min Width affects prefer Width
        assertEquals(204, listItem.getMeasureHeight(), 0.1f);

        listItem.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        listItem.onMeasure();

        assertEquals(Widget.MATCH_PARENT, listItem.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, listItem.getMeasureHeight(), 0.1f);
    }

    @Test
    public void stateIconSize() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        ListItem listItem = new ListItem();
        listItem.setText("Hello World");
        listItem.setStateIcon(drawable);
        listItem.setStateIconWidth(Widget.WRAP_CONTENT);
        listItem.setStateIconHeight(Widget.WRAP_CONTENT);

        listItem.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        listItem.onMeasure();

        assertEquals(165 + 32, listItem.getMeasureWidth(), 0.1f);
        assertEquals(32, listItem.getMeasureHeight(), 0.1f);

        listItem.setStateIconWidth(58f);
        listItem.setStateIconHeight(64f);
        listItem.onMeasure();

        assertEquals(165 + 58f, listItem.getMeasureWidth(), 0.1f);
        assertEquals(64, listItem.getMeasureHeight(), 0.1f);

        listItem.setStateIconWidth(32f);
        listItem.setStateIconHeight(16f);
        listItem.setMargins(1, 2, 3, 4);
        listItem.setPadding(5, 4, 2, 3);
        listItem.onMeasure();

        assertEquals(178 + 32, listItem.getMeasureWidth(), 0.1f);
        assertEquals(43, listItem.getMeasureHeight(), 0.1f);

        listItem.setPrefSize(100, 200);
        listItem.onMeasure();

        assertEquals(165 + 32 + 13, listItem.getMeasureWidth(), 0.1f); // Special Min Width affects prefer Width
        assertEquals(204, listItem.getMeasureHeight(), 0.1f);

        listItem.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        listItem.onMeasure();

        assertEquals(Widget.MATCH_PARENT, listItem.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, listItem.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureIconSpacing() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        ListItem listItem = new ListItem();
        listItem.setText("Hello World");
        listItem.setIcon(null);
        listItem.setIconSpacing(8f);
        listItem.setIconWidth(24);
        listItem.setIconHeight(16);

        listItem.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        listItem.onMeasure();

        assertEquals(165, listItem.getMeasureWidth(), 0.1f);
        assertEquals(32, listItem.getMeasureHeight(), 0.1f);

        listItem.setIcon(drawable);
        listItem.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        listItem.onMeasure();

        assertEquals(165 + 24 + 8f, listItem.getMeasureWidth(), 0.1f);
        assertEquals(32, listItem.getMeasureHeight(), 0.1f);

        listItem.setMargins(1, 2, 3, 4);
        listItem.setPadding(5, 4, 2, 3);
        listItem.onMeasure();

        assertEquals(178 + 24 + 8f, listItem.getMeasureWidth(), 0.1f);
        assertEquals(43, listItem.getMeasureHeight(), 0.1f);

        listItem.setPrefSize(100, 200);
        listItem.onMeasure();

        assertEquals(165 + 24 + 13 + 8, listItem.getMeasureWidth(), 0.1f); // Special Min Width affects prefer Width
        assertEquals(204, listItem.getMeasureHeight(), 0.1f);

        listItem.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        listItem.onMeasure();

        assertEquals(Widget.MATCH_PARENT, listItem.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, listItem.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureStateIconSpacing() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        ListItem listItem = new ListItem();
        listItem.setText("Hello World");
        listItem.setStateIcon(null);
        listItem.setStateIconSpacing(8f);
        listItem.setStateIconWidth(24);
        listItem.setStateIconHeight(16);

        listItem.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        listItem.onMeasure();

        assertEquals(165, listItem.getMeasureWidth(), 0.1f);
        assertEquals(32, listItem.getMeasureHeight(), 0.1f);

        listItem.setStateIcon(drawable);
        listItem.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        listItem.onMeasure();

        assertEquals(165 + 24 + 8f, listItem.getMeasureWidth(), 0.1f);
        assertEquals(32, listItem.getMeasureHeight(), 0.1f);

        listItem.setMargins(1, 2, 3, 4);
        listItem.setPadding(5, 4, 2, 3);
        listItem.onMeasure();

        assertEquals(178 + 24 + 8f, listItem.getMeasureWidth(), 0.1f);
        assertEquals(43, listItem.getMeasureHeight(), 0.1f);

        listItem.setPrefSize(100, 200);
        listItem.onMeasure();

        assertEquals(165 + 24 + 13 + 8, listItem.getMeasureWidth(), 0.1f); // Special Min Width affects prefer Width
        assertEquals(204, listItem.getMeasureHeight(), 0.1f);

        listItem.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        listItem.onMeasure();

        assertEquals(Widget.MATCH_PARENT, listItem.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, listItem.getMeasureHeight(), 0.1f);
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();
        UXValue uxBoldFont = mock(UXValue.class);
        when(uxBoldFont.asFont(any())).thenReturn(boldFont);

        UXValue uxIcon = mock(UXValue.class);
        when(uxIcon.asResource(any())).thenReturn(resIcon);

        UXValue uxOpenIcon = mock(UXValue.class);
        when(uxOpenIcon.asResource(any())).thenReturn(resStateIconOpen);

        hash.put(UXHash.getHash("icon"), uxIcon);
        hash.put(UXHash.getHash("icon-color"), new UXValueColor(0xFFFF00FF));
        hash.put(UXHash.getHash("icon-width"), new UXValueSizeSp(16));
        hash.put(UXHash.getHash("icon-height"), new UXValueSizeSp(18));
        hash.put(UXHash.getHash("icon-spacing"), new UXValueSizeSp(24));
        hash.put(UXHash.getHash("icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));

        hash.put(UXHash.getHash("state-icon"), uxOpenIcon);
        hash.put(UXHash.getHash("state-icon-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("state-icon-width"), new UXValueSizeSp(20));
        hash.put(UXHash.getHash("state-icon-height"), new UXValueSizeSp(22));
        hash.put(UXHash.getHash("state-icon-spacing"), new UXValueSizeSp(16));
        hash.put(UXHash.getHash("state-icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));

        hash.put(UXHash.getHash("text"), new UXValueText("Hello World"));
        hash.put(UXHash.getHash("text-all-caps"), new UXValueBool(true));
        hash.put(UXHash.getHash("text-font"), uxBoldFont);
        hash.put(UXHash.getHash("text-size"), new UXValueSizeSp(24));
        hash.put(UXHash.getHash("text-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("on-action"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("on-change-state"), new UXValueText("onChangeStateActionWork"));

        return hash;
    }
}