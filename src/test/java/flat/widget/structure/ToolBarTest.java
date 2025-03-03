package flat.widget.structure;

import flat.events.ActionEvent;
import flat.graphics.context.Font;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueColor;
import flat.uxml.value.UXValueSizeDp;
import flat.uxml.value.UXValueText;
import flat.widget.Widget;
import flat.widget.layout.Panel;
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

        assertEquals(0, toolBar.getIconsWidth(), 0.001f);
        assertEquals(0, toolBar.getIconsHeight(), 0.001f);
        assertEquals(0, toolBar.getIconsSpacing(), 0.001f);
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

        toolBar.setAttributes(createNonDefaultValues(), "tool-bar");
        toolBar.applyAttributes(controller);

        assertEquals(0, toolBar.getIconsWidth(), 0.001f);
        assertEquals(0, toolBar.getIconsHeight(), 0.001f);
        assertEquals(0, toolBar.getIconsSpacing(), 0.001f);
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

        assertEquals(18f, toolBar.getIconsWidth(), 0.001f);
        assertEquals(22f, toolBar.getIconsHeight(), 0.001f);
        assertEquals(26f, toolBar.getIconsSpacing(), 0.001f);
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

        UXChildren uxChild = mock(UXChildren.class);
        when(uxChild.next()).thenReturn(itemA).thenReturn(itemB).thenReturn(panel).thenReturn(null);

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

        UXChildren uxChild = mock(UXChildren.class);
        when(uxChild.next()).thenReturn(itemA).thenReturn(itemB).thenReturn(null);

        assertNull(itemA.getParent());
        assertNull(itemB.getParent());

        toolBar.setAttributes(createOverflowNavigation(), "tool-bar");
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
        toolBar.setIconsWidth(16);
        toolBar.setIconsHeight(18);
        toolBar.setNavigationItem(null);
        toolBar.setOverflowItem(null);
        toolBar.onMeasure();
        assertMeasure(toolBar, 0, 18);

        toolBar.setNavigationItem(new ToolItem());
        toolBar.onMeasure();
        assertMeasure(toolBar, 16, 18);

        toolBar.setOverflowItem(new ToolItem());
        toolBar.onMeasure();
        assertMeasure(toolBar, 32, 18);

        toolBar.addToolItem(new ToolItem());
        toolBar.onMeasure();
        assertMeasure(toolBar, 48, 18);

        toolBar.addToolItem(new ToolItem());
        toolBar.onMeasure();
        assertMeasure(toolBar, 64, 18);

        toolBar.setIconsSpacing(3);
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
        ToolBar toolBar = new ToolBar();
        toolBar.setIconsWidth(16);
        toolBar.setIconsHeight(18);
        toolBar.setNavigationItem(null);
        toolBar.setOverflowItem(null);
        toolBar.onMeasure();
        assertMeasure(toolBar, 0, 18);

        toolBar.setNavigationItem(new ToolItem());
        toolBar.onMeasure();
        assertMeasure(toolBar, 16, 18);

        toolBar.setOverflowItem(new ToolItem());
        toolBar.onMeasure();
        assertMeasure(toolBar, 32, 18);

        toolBar.addToolItem(new ToolItem());
        toolBar.onMeasure();
        assertMeasure(toolBar, 48, 18);

        toolBar.addToolItem(new ToolItem());
        toolBar.onMeasure();
        assertMeasure(toolBar, 64, 18);

        toolBar.setIconsSpacing(3);
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

}