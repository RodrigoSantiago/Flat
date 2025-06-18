package flat.widget.stages;

import flat.graphics.symbols.Font;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.graphics.symbols.FontManager;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXNode;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueColor;
import flat.uxml.value.UXValueSizeSp;
import flat.uxml.value.UXValueText;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.enums.DropdownAlign;
import flat.window.Activity;
import flat.window.ActivitySupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class, Font.class, FontManager.class, DrawableReader.class})
public class MenuItemTest {

    Font defaultFont;
    Font boldFont;
    Font scFont;

    ResourceStream resIcon;
    Drawable icon;

    @Before
    public void before() {
        mockStatic(Font.class);
        mockStatic(FontManager.class);

        defaultFont = mock(Font.class);
        when(Font.getDefault()).thenReturn(defaultFont);

        when(FontManager.findFont(any(), any(), any(), any())).thenReturn(defaultFont);
        when(defaultFont.getHeight(anyFloat())).thenReturn(16f);
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(64f);
        boldFont = mock(Font.class);
        scFont = mock(Font.class);

        mockStatic(DrawableReader.class);
        icon = mock(Drawable.class);
        when(icon.getWidth()).thenReturn(24f);
        when(icon.getHeight()).thenReturn(16f);

        resIcon = mock(ResourceStream.class);
        when(DrawableReader.parse(resIcon)).thenReturn(icon);

        when(defaultFont.getWidth(any(), anyInt(), anyInt(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        MenuItem menuitem = new MenuItem();

        assertEquals(defaultFont, menuitem.getShortcutTextFont());
        assertEquals(16f, menuitem.getShortcutTextSize(), 0.001f);
        assertEquals(0, menuitem.getShortcutSpacing(), 0.001f);
        assertEquals(0x000000FF, menuitem.getShortcutTextColor());
        assertNull(menuitem.getShortcutText());

        menuitem.setAttributes(createNonDefaultValues(), null);
        menuitem.applyAttributes(controller);

        assertEquals(defaultFont, menuitem.getShortcutTextFont());
        assertEquals(16f, menuitem.getShortcutTextSize(), 0.001f);
        assertEquals(0, menuitem.getShortcutSpacing(), 0.001f);
        assertEquals(0x000000FF, menuitem.getShortcutTextColor());
        assertEquals("Hello World", menuitem.getShortcutText());

        menuitem.applyStyle();

        assertEquals(boldFont, menuitem.getShortcutTextFont());
        assertEquals(22f, menuitem.getShortcutTextSize(), 0.001f);
        assertEquals(0, menuitem.getShortcutSpacing(), 0.001f);
        assertEquals(0xFF0000FF, menuitem.getShortcutTextColor());
        assertEquals("Hello World", menuitem.getShortcutText());
    }

    @Test
    public void measure() {
        MenuItem menuItem = new MenuItem();
        menuItem.setText("Hello World");
        menuItem.onMeasure();

        assertEquals(165, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(32, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setMargins(1, 2, 3, 4);
        menuItem.setPadding(5, 4, 2, 3);
        menuItem.onMeasure();

        assertEquals(178, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(43, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setPrefSize(100, 200);
        menuItem.onMeasure();

        assertEquals(106, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(204, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        menuItem.onMeasure();

        assertEquals(Widget.MATCH_PARENT, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, menuItem.getMeasureHeight(), 0.1f);
    }

    @Test
    public void iconSize() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        MenuItem menuItem = new MenuItem();
        menuItem.setText("Hello World");
        menuItem.setIcon(drawable);
        menuItem.setIconWidth(Widget.WRAP_CONTENT);
        menuItem.setIconHeight(Widget.WRAP_CONTENT);

        menuItem.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        menuItem.onMeasure();

        assertEquals(165 + 32, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(32, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setIconWidth(58f);
        menuItem.setIconHeight(64f);
        menuItem.onMeasure();

        assertEquals(165 + 58f, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(64, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setIconWidth(32f);
        menuItem.setIconHeight(16f);
        menuItem.setMargins(1, 2, 3, 4);
        menuItem.setPadding(5, 4, 2, 3);
        menuItem.onMeasure();

        assertEquals(178 + 32, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(43, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setPrefSize(100, 200);
        menuItem.onMeasure();

        assertEquals(106, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(204, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        menuItem.onMeasure();

        assertEquals(Widget.MATCH_PARENT, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, menuItem.getMeasureHeight(), 0.1f);
    }

    @Test
    public void shortcutTextSize() {
        when(scFont.getWidth(any(), anyInt(), anyInt(), eq(16f), anyFloat())).thenReturn(32f);
        when(scFont.getHeight(eq(16f))).thenReturn(32f);

        when(scFont.getWidth(any(), anyInt(), anyInt(), eq(64f), anyFloat())).thenReturn(58f);
        when(scFont.getHeight(eq(64f))).thenReturn(64f);

        when(scFont.getWidth(any(), anyInt(), anyInt(), eq(32f), anyFloat())).thenReturn(32f);
        when(scFont.getHeight(eq(32f))).thenReturn(16f);

        MenuItem menuItem = new MenuItem();
        menuItem.setShortcutTextFont(scFont);
        menuItem.setShortcutTextSize(16f);
        menuItem.setText("Hello World");
        menuItem.setShortcutText("Ctrl+C");

        menuItem.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        menuItem.onMeasure();

        assertEquals(165 + 32, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(32, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setShortcutTextSize(64f);
        menuItem.onMeasure();

        assertEquals(165 + 58f, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(64, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setShortcutTextSize(32f);
        menuItem.setMargins(1, 2, 3, 4);
        menuItem.setPadding(5, 4, 2, 3);
        menuItem.onMeasure();

        assertEquals(178 + 32, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(43, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setPrefSize(100, 200);
        menuItem.onMeasure();

        assertEquals(106, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(204, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        menuItem.onMeasure();

        assertEquals(Widget.MATCH_PARENT, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, menuItem.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureIconSpacing() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        MenuItem menuItem = new MenuItem();
        menuItem.setText("Hello World");
        menuItem.setIcon(null);
        menuItem.setIconSpacing(8f);
        menuItem.setIconWidth(24);
        menuItem.setIconHeight(16);

        menuItem.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        menuItem.onMeasure();

        // Menu Item has spacing even without icon
        assertEquals(165 + 24 + 8f, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(32, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setIcon(drawable);
        menuItem.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        menuItem.onMeasure();

        assertEquals(165 + 24 + 8f, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(32, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setMargins(1, 2, 3, 4);
        menuItem.setPadding(5, 4, 2, 3);
        menuItem.onMeasure();

        assertEquals(178 + 24 + 8f, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(43, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setPrefSize(100, 200);
        menuItem.onMeasure();

        assertEquals(106, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(204, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        menuItem.onMeasure();

        assertEquals(Widget.MATCH_PARENT, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, menuItem.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureShortcutTextSpacing() {
        when(scFont.getWidth(any(), anyInt(), anyInt(), eq(16f), anyFloat())).thenReturn(24f);
        when(scFont.getHeight(eq(16f))).thenReturn(16f);

        MenuItem menuItem = new MenuItem();
        menuItem.setText("Hello World");
        menuItem.setShortcutText(null);
        menuItem.setShortcutTextFont(scFont);
        menuItem.setShortcutSpacing(8f);
        menuItem.setShortcutTextSize(16f);

        menuItem.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        menuItem.onMeasure();

        assertEquals(165, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(32, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setShortcutText("Ctrl+C");
        menuItem.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        menuItem.onMeasure();

        assertEquals(165 + 24 + 8f, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(32, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setMargins(1, 2, 3, 4);
        menuItem.setPadding(5, 4, 2, 3);
        menuItem.onMeasure();

        assertEquals(178 + 24 + 8f, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(43, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setPrefSize(100, 200);
        menuItem.onMeasure();

        assertEquals(106, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(204, menuItem.getMeasureHeight(), 0.1f);

        menuItem.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        menuItem.onMeasure();

        assertEquals(Widget.MATCH_PARENT, menuItem.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, menuItem.getMeasureHeight(), 0.1f);
    }

    @Test
    public void subMenu() {
        Activity activityA = mock(Activity.class);
        Scene sceneA = new Scene();
        ActivitySupport.setActivity(sceneA, activityA);
        when(activityA.getScene()).thenReturn(sceneA);
        when(activityA.getWidth()).thenReturn(800);
        when(activityA.getHeight()).thenReturn(600);

        Menu parent = new Menu();
        MenuItem menuItem = new MenuItem();
        parent.addMenuItem(menuItem);

        Menu subMenu = new Menu();
        MenuItem subMenuItem = new MenuItem();
        subMenu.addMenuItem(subMenuItem);

        menuItem.setContextMenu(subMenu);

        parent.show(activityA, 150, 100, DropdownAlign.TOP_LEFT);

        assertTrue(parent.isShown());
        assertEquals(sceneA, parent.getParent());
        verify(activityA, times(1)).addPointerFilter(parent);
        verify(activityA, times(1)).addResizeFilter(parent);

        menuItem.showContextMenu();

        assertTrue(subMenu.isShown());
        assertEquals(sceneA, subMenu.getParent());
        verify(activityA, times(1)).addPointerFilter(subMenu);
        verify(activityA, times(1)).addResizeFilter(subMenu);

        menuItem.hideSiblingSubMenu();

        assertTrue(parent.isShown());
        assertEquals(sceneA, parent.getParent());
        assertFalse(subMenu.isShown());
        assertNull(subMenu.getParent());

        parent.hide();
        assertFalse(parent.isShown());
        assertNull(parent.getParent());
        assertFalse(subMenu.isShown());
        assertNull(subMenu.getParent());

    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxBoldFont = mock(UXValue.class);
        when(uxBoldFont.asFont(any())).thenReturn(boldFont);

        hash.put(UXHash.getHash("shortcut-text-font"), uxBoldFont);
        hash.put(UXHash.getHash("shortcut-text-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("shortcut-text-spacing"), new UXValueSizeSp(20));
        hash.put(UXHash.getHash("shortcut-text-size"), new UXValueSizeSp(22));
        hash.put(UXHash.getHash("shortcut-text"), new UXValueText("Hello World"));
        return hash;
    }
}