package flat.widget.structure;

import flat.events.ActionEvent;
import flat.graphics.symbols.Font;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.uxml.value.*;
import flat.widget.Widget;
import flat.widget.layout.Panel;
import flat.widget.stages.Menu;
import flat.widget.stages.MenuItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class, Font.class, DrawableReader.class})
public class ToolBarTest {

    ResourceStream resIcon;
    Drawable icon;
    Font defaultFont;
    Font boldFont;
    Font italicFont;

    @Before
    public void before() {
        mockStatic(Font.class);

        defaultFont = mock(Font.class);
        boldFont = mock(Font.class);
        italicFont = mock(Font.class);
        when(Font.getDefault()).thenReturn(defaultFont);

        when(Font.findFont(any(), any(), any(), any())).thenReturn(defaultFont);
        when(defaultFont.getHeight(eq(16f))).thenReturn(16f);
        when(defaultFont.getHeight(eq(8f))).thenReturn(8f);
        when(defaultFont.getWidth(any(), anyInt(), eq(0), eq(16f), anyFloat())).thenReturn(0f);
        when(defaultFont.getWidth(any(), anyInt(), anyInt(), eq(16f), anyFloat())).thenReturn(128f);
        when(defaultFont.getWidth(any(), anyInt(), eq(0), eq(8f), anyFloat())).thenReturn(0f);
        when(defaultFont.getWidth(any(), anyInt(), anyInt(), eq(8f), anyFloat())).thenReturn(64f);

        mockStatic(DrawableReader.class);

        icon = mock(Drawable.class);
        when(icon.getWidth()).thenReturn(16f);
        when(icon.getHeight()).thenReturn(20f);

        resIcon = mock(ResourceStream.class);

        when(DrawableReader.parse(resIcon)).thenReturn(icon);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        var action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", ActionEvent.class)).thenReturn(action);

        ToolBar toolBar = new ToolBar();

        assertNull(toolBar.getTitle());
        assertNull(toolBar.getSubtitle());
        assertEquals(16, toolBar.getTitleSize(), 0.001f);
        assertEquals(8, toolBar.getSubtitleSize(), 0.001f);
        assertEquals(defaultFont, toolBar.getTitleFont());
        assertEquals(defaultFont, toolBar.getSubtitleFont());
        assertEquals(0x000000FF, toolBar.getTitleColor());
        assertEquals(0x000000FF, toolBar.getSubtitleColor());
        assertNull(toolBar.getNavigationAction());
        assertNotNull(toolBar.getNavigationItem());
        assertNotNull(toolBar.getOverflowItem());

        toolBar.setAttributes(createNonDefaultValues(), null);
        toolBar.applyAttributes(controller);

        assertEquals("Title", toolBar.getTitle());
        assertEquals("Subtitle", toolBar.getSubtitle());
        assertEquals(16, toolBar.getTitleSize(), 0.001f);
        assertEquals(8, toolBar.getSubtitleSize(), 0.001f);
        assertEquals(defaultFont, toolBar.getTitleFont());
        assertEquals(defaultFont, toolBar.getSubtitleFont());
        assertEquals(0x000000FF, toolBar.getTitleColor());
        assertEquals(0x000000FF, toolBar.getSubtitleColor());
        assertEquals(action, toolBar.getNavigationAction());
        assertNotNull(toolBar.getNavigationItem());
        assertNotNull(toolBar.getOverflowItem());

        toolBar.applyStyle();

        assertEquals("Title", toolBar.getTitle());
        assertEquals("Subtitle", toolBar.getSubtitle());
        assertEquals(32f, toolBar.getTitleSize(), 0.001f);
        assertEquals(24f, toolBar.getSubtitleSize(), 0.001f);
        assertEquals(boldFont, toolBar.getTitleFont());
        assertEquals(italicFont, toolBar.getSubtitleFont());
        assertEquals(0xFF0000FF, toolBar.getTitleColor());
        assertEquals(0xFFFF00FF, toolBar.getSubtitleColor());
        assertEquals(action, toolBar.getNavigationAction());
        assertNotNull(toolBar.getNavigationItem());
        assertNotNull(toolBar.getOverflowItem());
    }

    @Test
    public void childrenFromUx() {
        ToolBar toolBar = new ToolBar();
        ToolItem itemA = new ToolItem();
        ToolItem itemB = new ToolItem();
        Panel panel = new Panel();

        UXChildren uxChild = mockChildren(itemA, itemB, panel);

        assertNull(itemA.getParent());
        assertNull(itemB.getParent());
        assertNull(panel.getParent());
        toolBar.applyChildren(uxChild);
        assertEquals(4, toolBar.getChildrenIterable().size());
        assertNotNull(toolBar.getChildrenIterable().get(0));
        assertNotNull(toolBar.getChildrenIterable().get(1));
        assertEquals(itemA, toolBar.getChildrenIterable().get(2));
        assertEquals(itemB, toolBar.getChildrenIterable().get(3));
        assertEquals(toolBar, itemA.getParent());
        assertEquals(toolBar, itemB.getParent());
        assertNull(panel.getParent());
    }

    @Test
    public void childrenOverflowNavigation() {
        ToolBar toolBar = new ToolBar();
        ToolItem itemA = new ToolItem();
        itemA.setId("itemA");
        ToolItem itemB = new ToolItem();
        itemB.setId("itemB");

        UXChildren uxChild = mockChildren(
                new Widget[]{itemA, itemB},
                new String[]{"overflow-item", "navigation-item"});

        assertNull(itemA.getParent());
        assertNull(itemB.getParent());

        toolBar.setAttributes(createOverflowNavigation(), null);
        toolBar.applyChildren(uxChild);

        assertEquals(2, toolBar.getChildrenIterable().size());
        assertEquals(itemA, toolBar.getChildrenIterable().get(0));
        assertEquals(itemB, toolBar.getChildrenIterable().get(1));
        assertEquals(toolBar, itemA.getParent());
        assertEquals(toolBar, itemB.getParent());
        assertEquals(itemA, toolBar.getOverflowItem());
        assertEquals(itemB, toolBar.getNavigationItem());
    }

    @Test
    public void measure() {
        ToolBar toolBar = new ToolBar();
        toolBar.setNavigationItem(null);
        toolBar.setOverflowItem(null);
        toolBar.onMeasure();
        assertMeasure(toolBar, 0, 0);

        toolBar.setNavigationItem(createItem(16, 18));
        toolBar.onMeasure();
        assertMeasure(toolBar, 16, 18);

        toolBar.setOverflowItem(createItem(16, 18));
        toolBar.onMeasure();
        assertMeasure(toolBar, 32, 18);

        toolBar.addToolItem(createItem(16, 18));
        toolBar.onMeasure();
        assertMeasure(toolBar, 48, 18);

        toolBar.addToolItem(createItem(16, 18));
        toolBar.onMeasure();
        assertMeasure(toolBar, 64, 18);

        toolBar.addToolItem(createItem(12, 21));
        toolBar.onMeasure();
        assertMeasure(toolBar, 76, 21);

        toolBar.setTitle("Title");
        toolBar.onMeasure();
        assertMeasure(toolBar, 76 + 128, 21);

        toolBar.setSubtitle("Subtitle");
        toolBar.onMeasure();
        assertMeasure(toolBar, 76 + 128, 16 + 8);

        toolBar.setMargins(1, 2, 3, 4);
        toolBar.setPadding(5, 4, 2, 3);
        toolBar.onMeasure();
        assertMeasure(toolBar, 76 + 128 + 13, 16 + 8 + 11);

        toolBar.setPrefSize(100, 200);
        toolBar.onMeasure();
        assertMeasure(toolBar, 106, 204);
    }

    @Test
    public void layout() {
        var nav = createItem(16, 18);
        var ove = createItem(16, 18);
        var item1 = createItem(16, 18);
        var item2 = createItem(16, 18);
        var item3 = createItem(16, 18);
        Menu menu = new Menu();
        MenuItem menuItem = new MenuItem();
        menu.addMenuItem(menuItem);

        ToolBar toolBar = new ToolBar();
        toolBar.setNavigationItem(nav);
        toolBar.setOverflowItem(ove);
        toolBar.setContextMenu(menu);
        toolBar.onMeasure();
        assertMeasure(toolBar, 32, 18);
        toolBar.onLayout(64, 18);
        assertLayout(nav, 0, 0, 16, 18);
        assertLayout(ove, 48, 0, 16, 18);

        toolBar.addToolItem(item1);
        toolBar.addToolItem(item2);
        toolBar.addToolItem(item3);

        // No text, Big Size, Overflow Always
        toolBar.onMeasure();
        assertMeasure(toolBar, 80, 18);
        toolBar.onLayout(100, 18);
        assertLayout(nav, 0, 0, 16, 18);
        assertLayout(item1, 36, 0, 16, 18);
        assertLayout(item2, 52, 0, 16, 18);
        assertLayout(item3, 68, 0, 16, 18);
        assertLayout(ove, 84, 0, 16, 18);

        // No text, Big Size
        toolBar.setContextMenu(null);
        toolBar.onMeasure();
        assertMeasure(toolBar, 80, 18);
        toolBar.onLayout(100, 18);
        assertLayout(nav, 0, 0, 16, 18);
        assertLayout(item1, 52, 0, 16, 18);
        assertLayout(item2, 68, 0, 16, 18);
        assertLayout(item3, 84, 0, 16, 18);

        // No text, Fit Size
        toolBar.onMeasure();
        assertMeasure(toolBar, 80, 18);
        toolBar.onLayout(64, 18);
        assertLayout(nav, 0, 0, 16, 18);
        assertLayout(item1, 16, 0, 16, 18);
        assertLayout(item2, 32, 0, 16, 18);
        assertLayout(item3, 48, 0, 16, 18);

        // No text, Hide 2
        toolBar.onMeasure();
        assertMeasure(toolBar, 80, 18);
        toolBar.onLayout(60, 18);
        assertLayout(nav, 0, 0, 16, 18);
        assertLayout(item1, 28, 0, 16, 18);
        assertLayout(item2, 44, 0, 16, 18); // Hidden
        assertLayout(item3, 60, 0, 16, 18); // Hidden
        assertLayout(ove, 44, 0, 16, 18);

        // No text, All
        toolBar.onMeasure();
        assertMeasure(toolBar, 80, 18);
        toolBar.onLayout(32, 18);
        assertLayout(nav, 0, 0, 16, 18);
        assertLayout(item1, 16, 0, 16, 18); // Hidden
        assertLayout(item2, 32, 0, 16, 18); // Hidden
        assertLayout(item3, 48, 0, 16, 18); // Hidden
        assertLayout(ove, 16, 0, 16, 18);
    }

    @Test
    public void layoutText() {
        var nav = createItem(16, 18);
        var ove = createItem(16, 18);
        var item1 = createItem(16, 18);
        var item2 = createItem(16, 18);
        var item3 = createItem(16, 18);
        Menu menu = new Menu();
        MenuItem menuItem = new MenuItem();
        menu.addMenuItem(menuItem);

        ToolBar toolBar = new ToolBar();
        toolBar.setNavigationItem(nav);
        toolBar.setOverflowItem(ove);
        toolBar.setContextMenu(menu);
        toolBar.setTitle("Title");
        toolBar.setSubtitle("Subtitle");
        toolBar.onMeasure();
        assertMeasure(toolBar, 160, 24);
        toolBar.onLayout(160, 24);
        assertLayout(nav, 0, 0, 16, 18);
        assertLayout(ove, 144, 0, 16, 18);

        toolBar.addToolItem(item1);
        toolBar.addToolItem(item2);
        toolBar.addToolItem(item3);

        // text, Big Size, Overflow Always
        toolBar.onMeasure();
        assertMeasure(toolBar, 208, 24);
        toolBar.onLayout(220, 24);
        assertLayout(nav, 0, 0, 16, 18);
        assertLayout(item1, 156, 0, 16, 18);
        assertLayout(item2, 172, 0, 16, 18);
        assertLayout(item3, 188, 0, 16, 18);
        assertLayout(ove, 204, 0, 16, 18);

        // text, Big Size
        toolBar.setContextMenu(null);
        toolBar.onMeasure();
        assertMeasure(toolBar, 208, 24);
        toolBar.onLayout(220, 24);
        assertLayout(nav, 0, 0, 16, 18);
        assertLayout(item1, 172, 0, 16, 18);
        assertLayout(item2, 188, 0, 16, 18);
        assertLayout(item3, 204, 0, 16, 18);

        // text, Fit Size
        toolBar.onMeasure();
        assertMeasure(toolBar, 208, 24);
        toolBar.onLayout(192, 24);
        assertLayout(nav, 0, 0, 16, 18);
        assertLayout(item1, 144, 0, 16, 18);
        assertLayout(item2, 160, 0, 16, 18);
        assertLayout(item3, 176, 0, 16, 18);

        // text, Hide 2
        toolBar.onMeasure();
        assertMeasure(toolBar, 208, 24);
        toolBar.onLayout(190, 24);
        assertLayout(nav, 0, 0, 16, 18);
        assertLayout(item1, 158, 0, 16, 18);
        assertLayout(item2, 174, 0, 16, 18); // Hidden
        assertLayout(item3, 190, 0, 16, 18); // Hidden
        assertLayout(ove, 174, 0, 16, 18);

        // text, All
        toolBar.onMeasure();
        assertMeasure(toolBar, 208, 24);
        toolBar.onLayout(164, 24);
        assertLayout(nav, 0, 0, 16, 18);
        assertLayout(item1, 148, 0, 16, 18); // Hidden
        assertLayout(item2, 164, 0, 16, 18); // Hidden
        assertLayout(item3, 180, 0, 16, 18); // Hidden
        assertLayout(ove, 148, 0, 16, 18);
    }

    @Test
    public void action() {
        var action = (UXListener<ActionEvent>) mock(UXListener.class);

        var nav = createItem(16, 18);
        ToolBar toolBar = new ToolBar();
        toolBar.setNavigationAction(action);
        toolBar.setNavigationItem(nav);

        toolBar.navigationAction();
        nav.action();

        verify(action, times(2)).handle(any());
    }

    private ToolItem createItem(float width, float height) {
        ToolItem toolItem = new ToolItem();
        toolItem.setPrefSize(width, height);
        return toolItem;
    }

    private void assertMeasure(Widget widget, float width, float height) {
        assertEquals("Measure Width", width, widget.getMeasureWidth(), 0.1f);
        assertEquals("Measure Height", height, widget.getMeasureHeight(), 0.1f);
    }

    private void assertLayout(Widget widget, float x, float y, float width, float height) {
        assertEquals("X", x, widget.getLayoutX(), 0.1f);
        assertEquals("Y", y, widget.getLayoutY(), 0.1f);
        assertEquals("Width", width, widget.getLayoutWidth(), 0.1f);
        assertEquals("Height", height, widget.getLayoutHeight(), 0.1f);
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxBoldFont = mock(UXValue.class);
        when(uxBoldFont.asFont(any())).thenReturn(boldFont);

        UXValue uxItalicFont = mock(UXValue.class);
        when(uxItalicFont.asFont(any())).thenReturn(italicFont);

        hash.put(UXHash.getHash("on-navigation"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("title"), new UXValueText("Title"));
        hash.put(UXHash.getHash("subtitle"),  new UXValueText("Subtitle"));
        hash.put(UXHash.getHash("title-size"), new UXValueSizeDp(32f));
        hash.put(UXHash.getHash("subtitle-size"), new UXValueSizeDp(24f));
        hash.put(UXHash.getHash("title-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("subtitle-color"), new UXValueColor(0xFFFF00FF));
        hash.put(UXHash.getHash("title-font"), uxBoldFont);
        hash.put(UXHash.getHash("subtitle-font"), uxItalicFont);
        hash.put(UXHash.getHash("icons-width"), new UXValueSizeDp(18f));
        hash.put(UXHash.getHash("icons-height"), new UXValueSizeDp(22f));
        hash.put(UXHash.getHash("icons-spacing"), new UXValueSizeDp(26f));
        return hash;
    }

    private HashMap<Integer, UXValue> createOverflowNavigation() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxBoldFont = mock(UXValue.class);
        when(uxBoldFont.asFont(any())).thenReturn(boldFont);

        UXValue uxItalicFont = mock(UXValue.class);
        when(uxItalicFont.asFont(any())).thenReturn(italicFont);

        hash.put(UXHash.getHash("overflow-item-id"), new UXValueText("itemA"));
        hash.put(UXHash.getHash("navigation-item-id"),  new UXValueText("itemB"));
        return hash;
    }

    private UXChildren mockChildren(Widget... widgets) {
        UXChildren uxChild = mock(UXChildren.class);
        ArrayList<UXChild> children = new ArrayList<>();
        for (var widget : widgets) {
            children.add(new UXChild(widget, null));
        }
        when(uxChild.iterator()).thenReturn(children.iterator());
        return uxChild;
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
        when(uxChild.iterator()).thenReturn(children.iterator());
        return uxChild;
    }

}