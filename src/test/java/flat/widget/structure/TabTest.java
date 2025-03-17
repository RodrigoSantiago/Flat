package flat.widget.structure;

import flat.graphics.context.Font;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXNode;
import flat.uxml.value.*;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.ImageFilter;
import flat.widget.enums.VerticalAlign;
import flat.widget.layout.Frame;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class, Font.class, DrawableReader.class})
public class TabTest {

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

        when(defaultFont.getWidth(any(), anyInt(), anyInt(), anyFloat(), anyFloat())).thenReturn(165f);
        when(defaultFont.getHeight(anyFloat())).thenReturn(32f);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        Tab tab = new Tab();

        assertEquals(VerticalAlign.TOP, tab.getVerticalAlign());
        assertFalse(tab.isTextAllCaps());
        assertEquals(defaultFont, tab.getTextFont());
        assertEquals(16f, tab.getTextSize(), 0.1f);
        assertEquals(0x000000FF, tab.getTextColor());
        assertNull(tab.getText());

        assertEquals(0, tab.getIconWidth(), 0.001f);
        assertEquals(0, tab.getIconHeight(), 0.001f);
        assertEquals(0, tab.getIconSpacing(), 0.1f);
        assertEquals(ImageFilter.LINEAR, tab.getIconImageFilter());
        assertNull(tab.getIcon());
        assertEquals(0xFFFFFFFF, tab.getIconColor());
        assertEquals(0, tab.getCloseIconWidth(), 0.001f);
        assertEquals(0, tab.getCloseIconHeight(), 0.001f);
        assertEquals(0, tab.getCloseIconSpacing(), 0.1f);
        assertEquals(ImageFilter.LINEAR, tab.getCloseIconImageFilter());
        assertNull(tab.getCloseIcon());
        assertEquals(0xFFFFFFFF, tab.getCloseIconColor());

        tab.setAttributes(createNonDefaultValues(), null);
        tab.applyAttributes(controller);

        assertEquals(VerticalAlign.TOP, tab.getVerticalAlign());
        assertFalse(tab.isTextAllCaps());
        assertEquals(defaultFont, tab.getTextFont());
        assertEquals(16f, tab.getTextSize(), 0.1f);
        assertEquals(0x000000FF, tab.getTextColor());
        assertEquals("Hello World", tab.getText());

        assertEquals(0, tab.getIconWidth(), 0.001f);
        assertEquals(0, tab.getIconHeight(), 0.001f);
        assertEquals(0, tab.getIconSpacing(), 0.1f);
        assertEquals(ImageFilter.LINEAR, tab.getIconImageFilter());
        assertNull(tab.getIcon());
        assertEquals(0xFFFFFFFF, tab.getIconColor());
        assertEquals(0, tab.getCloseIconWidth(), 0.001f);
        assertEquals(0, tab.getCloseIconHeight(), 0.001f);
        assertEquals(0, tab.getCloseIconSpacing(), 0.1f);
        assertEquals(ImageFilter.LINEAR, tab.getCloseIconImageFilter());
        assertNull(tab.getCloseIcon());
        assertEquals(0xFFFFFFFF, tab.getCloseIconColor());

        tab.applyStyle();

        assertEquals(VerticalAlign.BOTTOM, tab.getVerticalAlign());
        assertTrue(tab.isTextAllCaps());
        assertEquals(boldFont, tab.getTextFont());
        assertEquals(24f, tab.getTextSize(), 0.1f);
        assertEquals(0xFF0000FF, tab.getTextColor());
        assertEquals("Hello World", tab.getText());

        assertEquals(16, tab.getIconWidth(), 0.001f);
        assertEquals(18, tab.getIconHeight(), 0.001f);
        assertEquals(24, tab.getIconSpacing(), 0.1f);
        assertEquals(ImageFilter.NEAREST, tab.getIconImageFilter());
        assertEquals(icon, tab.getIcon());
        assertEquals(0xFFFF00FF, tab.getIconColor());
        assertEquals(20, tab.getCloseIconWidth(), 0.001f);
        assertEquals(22, tab.getCloseIconHeight(), 0.001f);
        assertEquals(16, tab.getCloseIconSpacing(), 0.1f);
        assertEquals(ImageFilter.NEAREST, tab.getCloseIconImageFilter());
        assertEquals(closeIcon, tab.getCloseIcon());
        assertEquals(0xFF0000FF, tab.getCloseIconColor());
    }

    @Test
    public void measure() {
        Tab tab = new Tab();
        tab.setText("Hello World");
        tab.onMeasure();

        assertEquals(165, tab.getMeasureWidth(), 0.1f);
        assertEquals(32, tab.getMeasureHeight(), 0.1f);

        tab.setMargins(1, 2, 3, 4);
        tab.setPadding(5, 4, 2, 3);
        tab.onMeasure();

        assertEquals(178, tab.getMeasureWidth(), 0.1f);
        assertEquals(43, tab.getMeasureHeight(), 0.1f);

        tab.setPrefSize(100, 200);
        tab.onMeasure();

        assertEquals(106, tab.getMeasureWidth(), 0.1f);
        assertEquals(204, tab.getMeasureHeight(), 0.1f);

        tab.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();

        assertEquals(Widget.MATCH_PARENT, tab.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, tab.getMeasureHeight(), 0.1f);
    }

    @Test
    public void iconSize() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Tab tab = new Tab();
        tab.setText("Hello World");
        tab.setIcon(drawable);
        tab.setIconWidth(Widget.WRAP_CONTENT);
        tab.setIconHeight(Widget.WRAP_CONTENT);

        tab.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        tab.onMeasure();

        assertEquals(165 + 32, tab.getMeasureWidth(), 0.1f);
        assertEquals(32, tab.getMeasureHeight(), 0.1f);

        tab.setIconWidth(58f);
        tab.setIconHeight(64f);
        tab.onMeasure();

        assertEquals(165 + 58f, tab.getMeasureWidth(), 0.1f);
        assertEquals(64, tab.getMeasureHeight(), 0.1f);

        tab.setIconWidth(32f);
        tab.setIconHeight(16f);
        tab.setMargins(1, 2, 3, 4);
        tab.setPadding(5, 4, 2, 3);
        tab.onMeasure();

        assertEquals(178 + 32, tab.getMeasureWidth(), 0.1f);
        assertEquals(43, tab.getMeasureHeight(), 0.1f);

        tab.setPrefSize(100, 200);
        tab.onMeasure();

        assertEquals(106, tab.getMeasureWidth(), 0.1f);
        assertEquals(204, tab.getMeasureHeight(), 0.1f);

        tab.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();

        assertEquals(Widget.MATCH_PARENT, tab.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, tab.getMeasureHeight(), 0.1f);
    }

    @Test
    public void closeIconSize() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Tab tab = new Tab();
        tab.setText("Hello World");
        tab.setCloseIcon(drawable);
        tab.setCloseIconWidth(Widget.WRAP_CONTENT);
        tab.setCloseIconHeight(Widget.WRAP_CONTENT);

        tab.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        tab.onMeasure();

        assertEquals(165 + 32, tab.getMeasureWidth(), 0.1f);
        assertEquals(32, tab.getMeasureHeight(), 0.1f);

        tab.setCloseIconWidth(58f);
        tab.setCloseIconHeight(64f);
        tab.onMeasure();

        assertEquals(165 + 58f, tab.getMeasureWidth(), 0.1f);
        assertEquals(64, tab.getMeasureHeight(), 0.1f);

        tab.setCloseIconWidth(32f);
        tab.setCloseIconHeight(16f);
        tab.setMargins(1, 2, 3, 4);
        tab.setPadding(5, 4, 2, 3);
        tab.onMeasure();

        assertEquals(178 + 32, tab.getMeasureWidth(), 0.1f);
        assertEquals(43, tab.getMeasureHeight(), 0.1f);

        tab.setPrefSize(100, 200);
        tab.onMeasure();

        assertEquals(106, tab.getMeasureWidth(), 0.1f);
        assertEquals(204, tab.getMeasureHeight(), 0.1f);

        tab.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();

        assertEquals(Widget.MATCH_PARENT, tab.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, tab.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureIconSpacing() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Tab tab = new Tab();
        tab.setText("Hello World");
        tab.setIcon(null);
        tab.setIconSpacing(8f);
        tab.setIconWidth(24);
        tab.setIconHeight(16);

        tab.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        tab.onMeasure();

        assertEquals(165, tab.getMeasureWidth(), 0.1f);
        assertEquals(32, tab.getMeasureHeight(), 0.1f);

        tab.setIcon(drawable);
        tab.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        tab.onMeasure();

        assertEquals(165 + 24 + 8f, tab.getMeasureWidth(), 0.1f);
        assertEquals(32, tab.getMeasureHeight(), 0.1f);

        tab.setMargins(1, 2, 3, 4);
        tab.setPadding(5, 4, 2, 3);
        tab.onMeasure();

        assertEquals(178 + 24 + 8f, tab.getMeasureWidth(), 0.1f);
        assertEquals(43, tab.getMeasureHeight(), 0.1f);

        tab.setPrefSize(100, 200);
        tab.onMeasure();

        assertEquals(106, tab.getMeasureWidth(), 0.1f);
        assertEquals(204, tab.getMeasureHeight(), 0.1f);

        tab.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();

        assertEquals(Widget.MATCH_PARENT, tab.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, tab.getMeasureHeight(), 0.1f);
    }

    @Test
    public void measureCloseIconSpacing() {
        Drawable drawable = mock(Drawable.class);
        when(drawable.getWidth()).thenReturn(24f);
        when(drawable.getHeight()).thenReturn(16f);

        Tab tab = new Tab();
        tab.setText("Hello World");
        tab.setCloseIcon(null);
        tab.setCloseIconSpacing(8f);
        tab.setCloseIconWidth(24);
        tab.setCloseIconHeight(16);

        tab.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        tab.onMeasure();

        assertEquals(165, tab.getMeasureWidth(), 0.1f);
        assertEquals(32, tab.getMeasureHeight(), 0.1f);

        tab.setCloseIcon(drawable);
        tab.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        tab.onMeasure();

        assertEquals(165 + 24 + 8f, tab.getMeasureWidth(), 0.1f);
        assertEquals(32, tab.getMeasureHeight(), 0.1f);

        tab.setMargins(1, 2, 3, 4);
        tab.setPadding(5, 4, 2, 3);
        tab.onMeasure();

        assertEquals(178 + 24 + 8f, tab.getMeasureWidth(), 0.1f);
        assertEquals(43, tab.getMeasureHeight(), 0.1f);

        tab.setPrefSize(100, 200);
        tab.onMeasure();

        assertEquals(106, tab.getMeasureWidth(), 0.1f);
        assertEquals(204, tab.getMeasureHeight(), 0.1f);

        tab.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();

        assertEquals(Widget.MATCH_PARENT, tab.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, tab.getMeasureHeight(), 0.1f);
    }

    @Test
    public void selectTab() {
        Controller controller = mock(Controller.class);
        TabView tab = new TabView();
        Frame frame1 = new Frame();
        Tab tab1 = new Tab();
        tab1.setFrame(frame1);
        Frame frame2 = new Frame();
        Tab tab2 = new Tab();
        tab2.setFrame(frame2);

        assertNull(tab.getSelectedTab());

        tab.selectTab(tab1);
        assertNull(tab.getSelectedTab());

        tab.addTab(tab1);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());
        assertTrue(tab1.isSelected());
        assertFalse(tab2.isSelected());

        tab.addTab(tab2);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());
        assertTrue(tab1.isSelected());
        assertFalse(tab2.isSelected());

        tab2.requestSelect();
        assertEquals(tab2, tab.getSelectedTab());
        assertEquals(frame2, tab.getContent());
        assertFalse(tab1.isSelected());
        assertTrue(tab2.isSelected());
    }

    @Test
    public void removeTab() {
        Controller controller = mock(Controller.class);
        TabView tab = new TabView();
        Frame frame1 = new Frame();
        Tab tab1 = new Tab();
        tab1.setFrame(frame1);
        Frame frame2 = new Frame();
        Tab tab2 = new Tab();
        tab2.setFrame(frame2);

        assertNull(tab.getSelectedTab());
        assertNull(tab.getContent());

        tab.addTab(tab1);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab.addTab(tab2);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab.selectTab(tab2);
        assertEquals(tab2, tab.getSelectedTab());
        assertEquals(frame2, tab.getContent());

        tab.removeTab(tab1);
        assertEquals(tab2, tab.getSelectedTab());
        assertEquals(frame2, tab.getContent());

        tab.removeTab(tab2);
        assertNull(tab.getSelectedTab());
        assertNull(tab.getContent());
    }

    @Test
    public void removeTabBefore() {
        Controller controller = mock(Controller.class);
        TabView tab = new TabView();
        Frame frame1 = new Frame();
        Tab tab1 = new Tab();
        tab1.setFrame(frame1);
        Frame frame2 = new Frame();
        Tab tab2 = new Tab();
        tab2.setFrame(frame2);

        assertNull(tab.getSelectedTab());
        assertNull(tab.getContent());

        tab.addTab(tab1);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab.addTab(tab2);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab.selectTab(tab2);
        assertEquals(tab2, tab.getSelectedTab());
        assertEquals(frame2, tab.getContent());

        tab.removeTab(tab2);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());
    }

    @Test
    public void removeTabFirst() {
        Controller controller = mock(Controller.class);
        TabView tab = new TabView();
        Frame frame1 = new Frame();
        Tab tab1 = new Tab();
        tab1.setFrame(frame1);
        Frame frame2 = new Frame();
        Tab tab2 = new Tab();
        tab2.setFrame(frame2);

        assertNull(tab.getSelectedTab());
        assertNull(tab.getContent());

        tab.addTab(tab1);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab.addTab(tab2);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab.removeTab(tab1);
        assertEquals(tab2, tab.getSelectedTab());
        assertEquals(frame2, tab.getContent());
    }

    @Test
    public void removeContentAndTabManually() {
        Controller controller = mock(Controller.class);
        TabView tab = new TabView();
        Frame frame1 = new Frame();
        Tab tab1 = new Tab();
        tab1.setFrame(frame1);

        assertNull(tab.getSelectedTab());
        assertNull(tab.getContent());

        tab.addTab(tab1);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        tab1.getParent().remove(tab1);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());

        frame1.getParent().remove(frame1);
        assertEquals(tab1, tab.getSelectedTab());
        assertEquals(frame1, tab.getContent());
    }

    @Test
    public void selectTabOnHideEvent() {
        Activity activity = mock(Activity.class);
        Scene scene = new Scene();
        when(activity.getScene()).thenReturn(scene);
        ActivitySupport.setActivity(scene, activity);

        TabView tab = new TabView();
        scene.add(tab);
        Frame frame1 = new Frame();
        Tab tab1 = new Tab();
        tab1.setFrame(frame1);
        Frame frame2 = new Frame();
        Tab tab2 = new Tab();
        tab2.setFrame(frame2);
        Frame frame3 = new Frame();
        Tab tab3 = new Tab();
        tab3.setFrame(frame3);

        Controller controller1 = new Controller() {
            @Override
            public void onHide() {
                tab.selectTab(tab3);
            }
        };
        Controller controller2 = mock(Controller.class);
        Controller controller3 = mock(Controller.class);
        frame1.setController(controller1);
        frame2.setController(controller2);
        frame3.setController(controller3);

        assertNull(tab.getSelectedTab());
        assertNull(tab.getContent());

        tab.addTab(tab1);
        tab.addTab(tab2);
        tab.addTab(tab3);

        tab.selectTab(tab2);

        assertEquals(tab3, tab.getSelectedTab());
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
        hash.put(UXHash.getHash("text-font"), uxBoldFont);
        hash.put(UXHash.getHash("text-size"), new UXValueSizeSp(24));
        hash.put(UXHash.getHash("text-color"), new UXValueColor(0xFF0000FF));

        return hash;
    }
}