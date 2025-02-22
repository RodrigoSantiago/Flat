package flat.widget.structure;

import flat.events.ActionEvent;
import flat.graphics.context.Font;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXListener;
import flat.uxml.UXNode;
import flat.uxml.value.*;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.ImageFilter;
import flat.widget.enums.VerticalAlign;
import flat.widget.layout.Frame;
import flat.widget.text.Button;
import flat.window.Activity;
import flat.window.ActivitySupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class, Font.class, DrawableReader.class})
public class PageTest {

    Font boldFont;
    Font defaultFont;

    ResourceStream resIcon;
    Drawable icon;
    ResourceStream resCloseIcon;
    Drawable closeIcon;

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

        closeIcon = mock(Drawable.class);
        when(closeIcon.getWidth()).thenReturn(20f);
        when(closeIcon.getHeight()).thenReturn(18f);

        resCloseIcon = mock(ResourceStream.class);
        when(DrawableReader.parse(resCloseIcon)).thenReturn(closeIcon);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        Page page = new Page();

        assertEquals(VerticalAlign.MIDDLE, page.getVerticalAlign());
        assertFalse(page.isTextAllCaps());
        assertEquals(defaultFont, page.getFont());
        assertEquals(16f, page.getTextSize(), 0.1f);
        assertEquals(0x000000FF, page.getTextColor());
        assertNull(page.getText());

        assertEquals(0, page.getIconWidth(), 0.001f);
        assertEquals(0, page.getIconHeight(), 0.001f);
        assertEquals(0, page.getIconSpacing(), 0.1f);
        assertEquals(ImageFilter.LINEAR, page.getIconImageFilter());
        assertNull(page.getIcon());
        assertEquals(0xFFFFFFFF, page.getIconColor());
        assertEquals(0, page.getCloseIconWidth(), 0.001f);
        assertEquals(0, page.getCloseIconHeight(), 0.001f);
        assertEquals(0, page.getCloseIconSpacing(), 0.1f);
        assertEquals(ImageFilter.LINEAR, page.getCloseIconImageFilter());
        assertNull(page.getCloseIcon());
        assertEquals(0xFFFFFFFF, page.getCloseIconColor());
        assertNull(page.getCloseIcon());
        assertNull(page.getCloseIcon());

        page.setAttributes(createNonDefaultValues(), "page");
        page.applyAttributes(controller);

        assertEquals(VerticalAlign.MIDDLE, page.getVerticalAlign());
        assertFalse(page.isTextAllCaps());
        assertEquals(defaultFont, page.getFont());
        assertEquals(16f, page.getTextSize(), 0.1f);
        assertEquals(0x000000FF, page.getTextColor());
        assertEquals("Hello World", page.getText());

        assertEquals(0, page.getIconWidth(), 0.001f);
        assertEquals(0, page.getIconHeight(), 0.001f);
        assertEquals(0, page.getCloseIconSpacing(), 0.1f);
        assertEquals(ImageFilter.LINEAR, page.getIconImageFilter());
        assertNull(page.getIcon());
        assertEquals(0xFFFFFFFF, page.getCloseIconColor());
        assertEquals(0, page.getCloseIconWidth(), 0.001f);
        assertEquals(0, page.getCloseIconHeight(), 0.001f);
        assertEquals(0, page.getIconSpacing(), 0.1f);
        assertEquals(ImageFilter.LINEAR, page.getCloseIconImageFilter());
        assertNull(page.getCloseIcon());
        assertEquals(0xFFFFFFFF, page.getCloseIconColor());

        page.applyStyle();

        assertEquals(VerticalAlign.BOTTOM, page.getVerticalAlign());
        assertTrue(page.isTextAllCaps());
        assertEquals(boldFont, page.getFont());
        assertEquals(24f, page.getTextSize(), 0.1f);
        assertEquals(0xFF0000FF, page.getTextColor());
        assertEquals("Hello World", page.getText());

        assertEquals(16, page.getIconWidth(), 0.001f);
        assertEquals(18, page.getIconHeight(), 0.001f);
        assertEquals(24, page.getIconSpacing(), 0.1f);
        assertEquals(ImageFilter.NEAREST, page.getIconImageFilter());
        assertEquals(icon, page.getIcon());
        assertEquals(0xFFFF00FF, page.getIconColor());
        assertEquals(20, page.getCloseIconWidth(), 0.001f);
        assertEquals(22, page.getCloseIconHeight(), 0.001f);
        assertEquals(16, page.getCloseIconSpacing(), 0.1f);
        assertEquals(ImageFilter.NEAREST, page.getCloseIconImageFilter());
        assertEquals(closeIcon, page.getCloseIcon());
        assertEquals(0xFF0000FF, page.getCloseIconColor());
    }

    @Test
    public void measure() {
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);

        Page page = new Page();
        page.setText("Hello World");
        page.onMeasure();

        assertEquals(165, page.getMeasureWidth(), 0.1f);
        assertEquals(32, page.getMeasureHeight(), 0.1f);

        page.setMargins(1, 2, 3, 4);
        page.setPadding(5, 4, 2, 3);
        page.onMeasure();

        assertEquals(178, page.getMeasureWidth(), 0.1f);
        assertEquals(43, page.getMeasureHeight(), 0.1f);

        page.setPrefSize(100, 200);
        page.onMeasure();

        assertEquals(106, page.getMeasureWidth(), 0.1f);
        assertEquals(204, page.getMeasureHeight(), 0.1f);

        page.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        page.onMeasure();

        assertEquals(Widget.MATCH_PARENT, page.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, page.getMeasureHeight(), 0.1f);
    }

    @Test
    public void iconSize() {
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);

        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Page page = new Page();
        page.setText("Hello World");
        page.setIcon(drawable);
        page.setIconWidth(Widget.WRAP_CONTENT);
        page.setIconHeight(Widget.WRAP_CONTENT);

        page.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        page.onMeasure();

        assertEquals(165 + 32, page.getMeasureWidth(), 0.1f);
        assertEquals(32, page.getMeasureHeight(), 0.1f);

        page.setIconWidth(58f);
        page.setIconHeight(64f);
        page.onMeasure();

        assertEquals(165 + 58f, page.getMeasureWidth(), 0.1f);
        assertEquals(64, page.getMeasureHeight(), 0.1f);

        page.setIconWidth(32f);
        page.setIconHeight(16f);
        page.setMargins(1, 2, 3, 4);
        page.setPadding(5, 4, 2, 3);
        page.onMeasure();

        assertEquals(178 + 32, page.getMeasureWidth(), 0.1f);
        assertEquals(43, page.getMeasureHeight(), 0.1f);

        page.setPrefSize(100, 200);
        page.onMeasure();

        assertEquals(106, page.getMeasureWidth(), 0.1f);
        assertEquals(204, page.getMeasureHeight(), 0.1f);

        page.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        page.onMeasure();

        assertEquals(Widget.MATCH_PARENT, page.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, page.getMeasureHeight(), 0.1f);
    }

    @Test
    public void closeIconSize() {
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);

        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Page page = new Page();
        page.setText("Hello World");
        page.setCloseIcon(drawable);
        page.setCloseIconWidth(Widget.WRAP_CONTENT);
        page.setCloseIconHeight(Widget.WRAP_CONTENT);

        page.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        page.onMeasure();

        assertEquals(165 + 32, page.getMeasureWidth(), 0.1f);
        assertEquals(32, page.getMeasureHeight(), 0.1f);

        page.setCloseIconWidth(58f);
        page.setCloseIconHeight(64f);
        page.onMeasure();

        assertEquals(165 + 58f, page.getMeasureWidth(), 0.1f);
        assertEquals(64, page.getMeasureHeight(), 0.1f);

        page.setCloseIconWidth(32f);
        page.setCloseIconHeight(16f);
        page.setMargins(1, 2, 3, 4);
        page.setPadding(5, 4, 2, 3);
        page.onMeasure();

        assertEquals(178 + 32, page.getMeasureWidth(), 0.1f);
        assertEquals(43, page.getMeasureHeight(), 0.1f);

        page.setPrefSize(100, 200);
        page.onMeasure();

        assertEquals(106, page.getMeasureWidth(), 0.1f);
        assertEquals(204, page.getMeasureHeight(), 0.1f);

        page.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        page.onMeasure();

        assertEquals(Widget.MATCH_PARENT, page.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, page.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureIconSpacing() {
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);

        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Page page = new Page();
        page.setText("Hello World");
        page.setIcon(null);
        page.setIconSpacing(8f);
        page.setIconWidth(24);
        page.setIconHeight(16);

        page.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        page.onMeasure();

        assertEquals(165, page.getMeasureWidth(), 0.1f);
        assertEquals(32, page.getMeasureHeight(), 0.1f);

        page.setIcon(drawable);
        page.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        page.onMeasure();

        assertEquals(165 + 24 + 8f, page.getMeasureWidth(), 0.1f);
        assertEquals(32, page.getMeasureHeight(), 0.1f);

        page.setMargins(1, 2, 3, 4);
        page.setPadding(5, 4, 2, 3);
        page.onMeasure();

        assertEquals(178 + 24 + 8f, page.getMeasureWidth(), 0.1f);
        assertEquals(43, page.getMeasureHeight(), 0.1f);

        page.setPrefSize(100, 200);
        page.onMeasure();

        assertEquals(106, page.getMeasureWidth(), 0.1f);
        assertEquals(204, page.getMeasureHeight(), 0.1f);

        page.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        page.onMeasure();

        assertEquals(Widget.MATCH_PARENT, page.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, page.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureCloseIconSpacing() {
        when(defaultFont.getWidth(any(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);

        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Page page = new Page();
        page.setText("Hello World");
        page.setCloseIcon(null);
        page.setCloseIconSpacing(8f);
        page.setCloseIconWidth(24);
        page.setCloseIconHeight(16);

        page.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        page.onMeasure();

        assertEquals(165, page.getMeasureWidth(), 0.1f);
        assertEquals(32, page.getMeasureHeight(), 0.1f);

        page.setCloseIcon(drawable);
        page.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        page.onMeasure();

        assertEquals(165 + 24 + 8f, page.getMeasureWidth(), 0.1f);
        assertEquals(32, page.getMeasureHeight(), 0.1f);

        page.setMargins(1, 2, 3, 4);
        page.setPadding(5, 4, 2, 3);
        page.onMeasure();

        assertEquals(178 + 24 + 8f, page.getMeasureWidth(), 0.1f);
        assertEquals(43, page.getMeasureHeight(), 0.1f);

        page.setPrefSize(100, 200);
        page.onMeasure();

        assertEquals(106, page.getMeasureWidth(), 0.1f);
        assertEquals(204, page.getMeasureHeight(), 0.1f);

        page.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        page.onMeasure();

        assertEquals(Widget.MATCH_PARENT, page.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, page.getMeasureHeight(), 0.1f);
    }

    @Test
    public void selectPage() {
        Controller controller = mock(Controller.class);
        Tab tab = new Tab();
        Frame frame1 = new Frame();
        Page page1 = new Page();
        page1.setFrame(frame1);
        Frame frame2 = new Frame();
        Page page2 = new Page();
        page2.setFrame(frame2);

        assertNull(tab.getSelectedPage());

        tab.selectPage(page1);
        assertNull(tab.getSelectedPage());

        tab.addPage(page1);
        assertEquals(page1, tab.getSelectedPage());
        assertEquals(frame1, tab.getContent());
        assertTrue(page1.isSelected());
        assertFalse(page2.isSelected());

        tab.addPage(page2);
        assertEquals(page1, tab.getSelectedPage());
        assertEquals(frame1, tab.getContent());
        assertTrue(page1.isSelected());
        assertFalse(page2.isSelected());

        page2.requestSelect();
        assertEquals(page2, tab.getSelectedPage());
        assertEquals(frame2, tab.getContent());
        assertFalse(page1.isSelected());
        assertTrue(page2.isSelected());
    }

    @Test
    public void removePage() {
        Controller controller = mock(Controller.class);
        Tab tab = new Tab();
        Frame frame1 = new Frame();
        Page page1 = new Page();
        page1.setFrame(frame1);
        Frame frame2 = new Frame();
        Page page2 = new Page();
        page2.setFrame(frame2);

        assertNull(tab.getSelectedPage());
        assertNull(tab.getContent());

        tab.addPage(page1);
        assertEquals(page1, tab.getSelectedPage());
        assertEquals(frame1, tab.getContent());

        tab.addPage(page2);
        assertEquals(page1, tab.getSelectedPage());
        assertEquals(frame1, tab.getContent());

        tab.selectPage(page2);
        assertEquals(page2, tab.getSelectedPage());
        assertEquals(frame2, tab.getContent());

        tab.removePage(page1);
        assertEquals(page2, tab.getSelectedPage());
        assertEquals(frame2, tab.getContent());

        tab.removePage(page2);
        assertNull(tab.getSelectedPage());
        assertNull(tab.getContent());
    }

    @Test
    public void removePageBefore() {
        Controller controller = mock(Controller.class);
        Tab tab = new Tab();
        Frame frame1 = new Frame();
        Page page1 = new Page();
        page1.setFrame(frame1);
        Frame frame2 = new Frame();
        Page page2 = new Page();
        page2.setFrame(frame2);

        assertNull(tab.getSelectedPage());
        assertNull(tab.getContent());

        tab.addPage(page1);
        assertEquals(page1, tab.getSelectedPage());
        assertEquals(frame1, tab.getContent());

        tab.addPage(page2);
        assertEquals(page1, tab.getSelectedPage());
        assertEquals(frame1, tab.getContent());

        tab.selectPage(page2);
        assertEquals(page2, tab.getSelectedPage());
        assertEquals(frame2, tab.getContent());

        tab.removePage(page2);
        assertEquals(page1, tab.getSelectedPage());
        assertEquals(frame1, tab.getContent());
    }

    @Test
    public void removePageFirst() {
        Controller controller = mock(Controller.class);
        Tab tab = new Tab();
        Frame frame1 = new Frame();
        Page page1 = new Page();
        page1.setFrame(frame1);
        Frame frame2 = new Frame();
        Page page2 = new Page();
        page2.setFrame(frame2);

        assertNull(tab.getSelectedPage());
        assertNull(tab.getContent());

        tab.addPage(page1);
        assertEquals(page1, tab.getSelectedPage());
        assertEquals(frame1, tab.getContent());

        tab.addPage(page2);
        assertEquals(page1, tab.getSelectedPage());
        assertEquals(frame1, tab.getContent());

        tab.removePage(page1);
        assertEquals(page2, tab.getSelectedPage());
        assertEquals(frame2, tab.getContent());
    }

    @Test
    public void removeContentAndPageManually() {
        Controller controller = mock(Controller.class);
        Tab tab = new Tab();
        Frame frame1 = new Frame();
        Page page1 = new Page();
        page1.setFrame(frame1);

        assertNull(tab.getSelectedPage());
        assertNull(tab.getContent());

        tab.addPage(page1);
        assertEquals(page1, tab.getSelectedPage());
        assertEquals(frame1, tab.getContent());

        page1.getParent().remove(page1);
        assertEquals(page1, tab.getSelectedPage());
        assertEquals(frame1, tab.getContent());

        frame1.getParent().remove(frame1);
        assertEquals(page1, tab.getSelectedPage());
        assertEquals(frame1, tab.getContent());
    }

    @Test
    public void selectPageOnHideEvent() {
        Activity activity = mock(Activity.class);
        Scene scene = new Scene();
        when(activity.getScene()).thenReturn(scene);
        ActivitySupport.setActivity(scene, activity);

        Tab tab = new Tab();
        scene.add(tab);
        Frame frame1 = new Frame();
        Page page1 = new Page();
        page1.setFrame(frame1);
        Frame frame2 = new Frame();
        Page page2 = new Page();
        page2.setFrame(frame2);
        Frame frame3 = new Frame();
        Page page3 = new Page();
        page3.setFrame(frame3);

        Controller controller1 = new Controller() {
            @Override
            public void onHide() {
                tab.selectPage(page3);
            }
        };
        Controller controller2 = mock(Controller.class);
        Controller controller3 = mock(Controller.class);
        frame1.setController(controller1);
        frame2.setController(controller2);
        frame3.setController(controller3);

        assertNull(tab.getSelectedPage());
        assertNull(tab.getContent());

        tab.addPage(page1);
        tab.addPage(page2);
        tab.addPage(page3);

        tab.selectPage(page2);

        assertEquals(page3, tab.getSelectedPage());
        assertEquals(frame3, tab.getContent());
        verify(controller2, times(0)).onShow();
        verify(controller2, times(0)).onHide();
        verify(controller3, times(1)).onShow();
        verify(controller2, times(0)).onHide();
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();
        UXValue uxBoldFont = mock(UXValue.class);
        when(uxBoldFont.asFont(any())).thenReturn(boldFont);

        UXValue uxIcon = mock(UXValue.class);
        when(uxIcon.asResource(any())).thenReturn(resIcon);

        UXValue uxCloseIcon = mock(UXValue.class);
        when(uxCloseIcon.asResource(any())).thenReturn(resCloseIcon);

        hash.put(UXHash.getHash("icon"), uxIcon);
        hash.put(UXHash.getHash("icon-color"), new UXValueColor(0xFFFF00FF));
        hash.put(UXHash.getHash("icon-width"), new UXValueSizeSp(16));
        hash.put(UXHash.getHash("icon-height"), new UXValueSizeSp(18));
        hash.put(UXHash.getHash("icon-spacing"), new UXValueSizeSp(24));
        hash.put(UXHash.getHash("icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));

        hash.put(UXHash.getHash("close-icon"), uxCloseIcon);
        hash.put(UXHash.getHash("close-icon-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("close-icon-width"), new UXValueSizeSp(20));
        hash.put(UXHash.getHash("close-icon-height"), new UXValueSizeSp(22));
        hash.put(UXHash.getHash("close-icon-spacing"), new UXValueSizeSp(16));
        hash.put(UXHash.getHash("close-icon-image-filter"), new UXValueText(ImageFilter.NEAREST.toString()));

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