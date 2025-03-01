package flat.widget.text;

import flat.events.ActionEvent;
import flat.events.TextEvent;
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
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.enums.ImageFilter;
import flat.widget.stages.Menu;
import flat.widget.stages.MenuItem;
import flat.widget.stages.MenuItemTest;
import flat.window.Activity;
import flat.window.ActivitySupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class, Font.class, DrawableReader.class})
public class DropDownTest {

    Controller controller;
    UXBuilder builder;
    Font defaultFont;

    ResourceStream resIcon;
    Drawable icon;

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
        DropDown textField = new DropDown();

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", ActionEvent.class)).thenReturn(action);

        var selected = (UXListener<TextEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onOptionSelected", TextEvent.class)).thenReturn(selected);

        assertNull(textField.getActionIcon());
        assertEquals(0, textField.getActionIconSpacing(), 0.001f);
        assertEquals(0x00000000, textField.getActionIconBgColor());
        assertEquals(0xFFFFFFFF, textField.getActionIconColor());
        assertEquals(Cursor.UNSET, textField.getActionIconCursor());
        assertEquals(ImageFilter.LINEAR, textField.getActionIconImageFilter());
        assertEquals(0, textField.getActionIconWidth(), 0.001f);
        assertEquals(0, textField.getActionIconHeight(), 0.001f);

        textField.setAttributes(createNonDefaultValues(), "text-field");
        textField.applyAttributes(controller);

        assertNull(textField.getActionIcon());
        assertEquals(0, textField.getActionIconSpacing(), 0.001f);
        assertEquals(0x00000000, textField.getActionIconBgColor());
        assertEquals(0xFFFFFFFF, textField.getActionIconColor());
        assertEquals(Cursor.UNSET, textField.getActionIconCursor());
        assertEquals(ImageFilter.LINEAR, textField.getActionIconImageFilter());
        assertEquals(0, textField.getActionIconWidth(), 0.001f);
        assertEquals(0, textField.getActionIconHeight(), 0.001f);

        textField.applyStyle();

        assertEquals(icon, textField.getActionIcon());
        assertEquals(16, textField.getActionIconSpacing(), 0.001f);
        assertEquals(0xFF00F0FF, textField.getActionIconBgColor());
        assertEquals(0xFF0000FF, textField.getActionIconColor());
        assertEquals(Cursor.HAND, textField.getActionIconCursor());
        assertEquals(ImageFilter.NEAREST, textField.getActionIconImageFilter());
        assertEquals(20, textField.getActionIconWidth(), 0.001f);
        assertEquals(22, textField.getActionIconHeight(), 0.001f);
    }

    @Test
    public void measure() {
        DropDown dropDown = new DropDown();
        dropDown.setText("A");
        dropDown.onMeasure();

        assertEquals(16, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(16, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setMargins(1, 2, 3, 4);
        dropDown.setPadding(5, 4, 2, 3);
        dropDown.onMeasure();

        assertEquals(16 + 13, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(16 + 11, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setPrefSize(100, 200);
        dropDown.onMeasure();

        assertEquals(106, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(204, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        dropDown.onMeasure();

        assertEquals(Widget.MATCH_PARENT, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, dropDown.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureTitle() {
        DropDown dropDown = new DropDown();
        dropDown.setText("A");
        dropDown.setTitle("B");
        dropDown.onMeasure();

        assertEquals(16, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(16 + 8, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setMargins(1, 2, 3, 4);
        dropDown.setPadding(5, 4, 2, 3);
        dropDown.onMeasure();

        assertEquals(16 + 13, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(16 + 8 + 11, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setPrefSize(100, 200);
        dropDown.onMeasure();

        assertEquals(106, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(204, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        dropDown.onMeasure();

        assertEquals(Widget.MATCH_PARENT, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, dropDown.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureActionIcon() {
        Drawable drawable = mock(Drawable.class);
        PowerMockito.when(drawable.getWidth()).thenReturn(24f);
        PowerMockito.when(drawable.getHeight()).thenReturn(16f);

        DropDown dropDown = new DropDown();
        dropDown.setText("A");
        dropDown.setActionIconWidth(24f);
        dropDown.setActionIconHeight(16f);
        dropDown.onMeasure();

        assertEquals(16, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(16, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setActionIcon(drawable);
        dropDown.onMeasure();

        assertEquals(16 + 24, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(16, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setActionIconHeight(24f);
        dropDown.onMeasure();

        assertEquals(16 + 24, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(24, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setActionIconHeight(16f);
        dropDown.setMargins(1, 2, 3, 4);
        dropDown.setPadding(5, 4, 2, 3);
        dropDown.onMeasure();

        assertEquals(16 + 24 + 13, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(16 + 11, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setPrefSize(100, 200);
        dropDown.onMeasure();

        assertEquals(106, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(204, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        dropDown.onMeasure();

        assertEquals(Widget.MATCH_PARENT, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, dropDown.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureActionIconSpacing() {
        Drawable drawable = mock(Drawable.class);
        PowerMockito.when(drawable.getWidth()).thenReturn(24f);
        PowerMockito.when(drawable.getHeight()).thenReturn(16f);

        DropDown dropDown = new DropDown();
        dropDown.setText("A");
        dropDown.setActionIconSpacing(8f);
        dropDown.setActionIconWidth(24f);
        dropDown.setActionIconHeight(16f);
        dropDown.onMeasure();

        assertEquals(16, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(16, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setActionIcon(drawable);
        dropDown.onMeasure();

        assertEquals(16 + 24 + 8, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(16, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setActionIconHeight(16f);
        dropDown.setMargins(1, 2, 3, 4);
        dropDown.setPadding(5, 4, 2, 3);
        dropDown.onMeasure();

        assertEquals(16 + 24 + 8 + 13, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(16 + 11, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setPrefSize(100, 200);
        dropDown.onMeasure();

        assertEquals(106, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(204, dropDown.getMeasureHeight(), 0.1f);

        dropDown.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        dropDown.onMeasure();

        assertEquals(Widget.MATCH_PARENT, dropDown.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, dropDown.getMeasureHeight(), 0.1f);
    }

    @Test
    public void action() {
        List<String> options = List.of("A", "B", "C");
        DropDown dropDown = new DropDown();
        dropDown.setOptions(options);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        dropDown.setActionListener(action);

        var optionSelected = (UXListener<TextEvent>) mock(UXListener.class);
        dropDown.setOptionSelectedListener(optionSelected);

        Activity activityA = mock(Activity.class);
        Scene sceneA = new Scene();
        ActivitySupport.setActivity(sceneA, activityA);
        PowerMockito.when(activityA.getScene()).thenReturn(sceneA);
        PowerMockito.when(activityA.getWidth()).thenReturn(800f);
        PowerMockito.when(activityA.getHeight()).thenReturn(600f);

        sceneA.add(dropDown);

        dropDown.action();

        Menu menu = dropDown.getSubMenu();
        assertTrue(menu.isShown());

        int i = 0;
        for (var child : menu.getChildrenIterable()) {
            assertEquals(child.getClass(), MenuItem.class);
            MenuItem item = (MenuItem) child;
            assertEquals(options.get(i), item.getText());
            i++;
        }

        for (var child : menu.getChildrenIterable()) {
            MenuItem item = (MenuItem) child;
            item.action();
            break;
        }
        assertFalse(menu.isShown());

        dropDown.selectOption(2);
        dropDown.selectOption(5);

        verify(action, times(1)).handle(any());
        verify(optionSelected, times(2)).handle(any());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxIcon = mock(UXValue.class);
        PowerMockito.when(uxIcon.asResource(any())).thenReturn(resIcon);

        hash.put(UXHash.getHash("on-action"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("on-option-selected"), new UXValueText("onoptionSelected"));
        hash.put(UXHash.getHash("action-icon"), uxIcon);
        hash.put(UXHash.getHash("action-icon-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("action-icon-bg-color"), new UXValueColor(0xFF00F0FF));
        hash.put(UXHash.getHash("action-icon-width"), new UXValueSizeSp(20));
        hash.put(UXHash.getHash("action-icon-height"), new UXValueSizeSp(22));
        hash.put(UXHash.getHash("action-icon-spacing"), new UXValueSizeSp(16));
        hash.put(UXHash.getHash("action-icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));
        hash.put(UXHash.getHash("action-icon-cursor"), new UXValueText(Cursor.HAND.toString()));
        hash.put(UXHash.getHash("on-request-close"), new UXValueText("onCloseActionWork"));

        return hash;
    }
}