package flat.widget.text;

import flat.events.ActionEvent;
import flat.events.TextEvent;
import flat.graphics.context.Font;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueText;
import flat.widget.Scene;
import flat.widget.stages.Menu;
import flat.widget.stages.MenuItem;
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
public class TextDropDownTest {

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
    }

    @Test
    public void properties() {
        TextDropDown textField = new TextDropDown();

        var selected = (UXListener<TextEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onOptionSelected", TextEvent.class)).thenReturn(selected);

        assertNull(textField.getOptionSelectedListener());

        textField.setAttributes(createNonDefaultValues(), null);
        textField.applyAttributes(controller);

        assertEquals(selected, textField.getOptionSelectedListener());

        textField.applyStyle();

        assertEquals(selected, textField.getOptionSelectedListener());

        assertTrue(textField.getHorizontalBar().getStyles().contains("text-drop-down-horizontal-scroll-bar"));
        assertTrue(textField.getVerticalBar().getStyles().contains("text-drop-down-vertical-scroll-bar"));
    }

    @Test
    public void action() {
        List<String> options = List.of("A", "B", "C");
        TextDropDown dropDown = new TextDropDown();
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
        assertTrue(menu.getStyles().contains("drop-down-menu"));

        int i = 0;
        for (var child : menu.getUnmodifiableItemsList()) {
            assertEquals(child.getClass(), MenuItem.class);
            MenuItem item = (MenuItem) child;
            assertEquals(options.get(i), item.getText());
            i++;
        }

        for (var child : menu.getUnmodifiableItemsList()) {
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

        hash.put(UXHash.getHash("on-option-selected"), new UXValueText("onOptionSelected"));

        return hash;
    }
}