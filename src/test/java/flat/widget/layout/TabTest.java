package flat.widget.layout;

import flat.graphics.context.Font;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXNode;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueNumber;
import flat.uxml.value.UXValueSizeDp;
import flat.uxml.value.UXValueText;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.widget.enums.VerticalPosition;
import flat.window.Activity;
import flat.window.ActivitySupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class, Font.class})
public class TabTest {

    Font defaultFont;
    @Before
    public void before() {
        mockStatic(Font.class);
        defaultFont = mock(Font.class);
        when(Font.getDefault()).thenReturn(defaultFont);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);
        Tab tab = new Tab();

        assertEquals(HorizontalAlign.LEFT, tab.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, tab.getVerticalAlign());
        assertEquals(HorizontalAlign.LEFT, tab.getPagesHorizontalAlign());
        assertEquals(VerticalAlign.TOP, tab.getPagesVerticalAlign());
        assertEquals(VerticalPosition.TOP, tab.getPagesVerticalPosition());
        assertEquals(0, tab.getPagesPrefHeight(), 0.1f);
        assertEquals(10, tab.getScrollSensibility(), 0.1f);

        tab.setAttributes(createNonDefaultValues(), "tab");
        tab.applyAttributes(controller);

        assertEquals(HorizontalAlign.LEFT, tab.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, tab.getVerticalAlign());
        assertEquals(HorizontalAlign.LEFT, tab.getPagesHorizontalAlign());
        assertEquals(VerticalAlign.TOP, tab.getPagesVerticalAlign());
        assertEquals(VerticalPosition.TOP, tab.getPagesVerticalPosition());
        assertEquals(0, tab.getPagesPrefHeight(), 0.1f);
        assertEquals(10, tab.getScrollSensibility(), 0.1f);

        tab.applyStyle();

        assertEquals(HorizontalAlign.CENTER, tab.getHorizontalAlign());
        assertEquals(VerticalAlign.BOTTOM, tab.getVerticalAlign());
        assertEquals(HorizontalAlign.RIGHT, tab.getPagesHorizontalAlign());
        assertEquals(VerticalAlign.MIDDLE, tab.getPagesVerticalAlign());
        assertEquals(VerticalPosition.BOTTOM, tab.getPagesVerticalPosition());
        assertEquals(16, tab.getPagesPrefHeight(), 0.1f);
        assertEquals(5, tab.getScrollSensibility(), 0.1f);
    }

    @Test
    public void addSelectPage() {
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

        tab.addPage(page2);
        assertEquals(page1, tab.getSelectedPage());
        assertEquals(frame1, tab.getContent());

        tab.selectPage(page2);
        assertEquals(page2, tab.getSelectedPage());
        assertEquals(frame2, tab.getContent());
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

    @Test
    public void layoutTop() {
        Tab tab = new Tab();
        tab.setVerticalAlign(VerticalAlign.TOP);
        tab.setHorizontalAlign(HorizontalAlign.LEFT);
        tab.setPagesVerticalAlign(VerticalAlign.TOP);
        tab.setPagesHorizontalAlign(HorizontalAlign.LEFT);
        tab.setPagesVerticalPosition(VerticalPosition.TOP);

        Frame frame1 = new Frame();
        Page page1 = new Page();
        page1.setFrame(frame1);
        tab.addPage(page1);

        tab.setPrefSize(500, 400);
        page1.setPrefSize(100, 24);
        frame1.setPrefSize(200, 100);

        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(page1, 0, 0, 100, 24);
        assertLayout(frame1, 0, 24, 200, 100);

        tab.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        tab.onMeasure();
        assertMeasure(tab, 200, 124);
        tab.onLayout(200, 124);
        assertLayout(page1, 0, 0, 100, 24);
        assertLayout(frame1, 0, 24, 200, 100);
        tab.setPrefSize(500, 400);

        frame1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(page1, 0, 0, 100, 24);
        assertLayout(frame1, 0, 24, 500, 376);
        frame1.setPrefSize(200, 100);

        page1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(page1, 0, 0, 500, 300);
        assertLayout(frame1, 0, 300, 200, 100);
        page1.setPrefSize(100, 24);

        frame1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        page1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(page1, 0, 0, 500, 200);
        assertLayout(frame1, 0, 200, 500, 200);

        page1.setMinHeight(24);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(page1, 0, 0, 500, 212);
        assertLayout(frame1, 0, 212, 500, 188);

    }

    @Test
    public void layoutBottom() {
        Tab tab = new Tab();
        tab.setVerticalAlign(VerticalAlign.TOP);
        tab.setHorizontalAlign(HorizontalAlign.LEFT);
        tab.setPagesVerticalAlign(VerticalAlign.TOP);
        tab.setPagesHorizontalAlign(HorizontalAlign.LEFT);
        tab.setPagesVerticalPosition(VerticalPosition.BOTTOM);

        Frame frame1 = new Frame();
        Page page1 = new Page();
        page1.setFrame(frame1);
        tab.addPage(page1);

        tab.setPrefSize(500, 400);
        page1.setPrefSize(100, 24);
        frame1.setPrefSize(200, 100);

        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(page1, 0, 100, 100, 24);
        assertLayout(frame1, 0, 0, 200, 100);

        tab.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        tab.onMeasure();
        assertMeasure(tab, 200, 124);
        tab.onLayout(200, 124);
        assertLayout(page1, 0, 100, 100, 24);
        assertLayout(frame1, 0, 0, 200, 100);
        tab.setPrefSize(500, 400);

        frame1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(page1, 0, 376, 100, 24);
        assertLayout(frame1, 0, 0, 500, 376);
        frame1.setPrefSize(200, 100);

        page1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(page1, 0, 100, 500, 300);
        assertLayout(frame1, 0, 0, 200, 100);
        page1.setPrefSize(100, 24);

        frame1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        page1.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(page1, 0, 200, 500, 200);
        assertLayout(frame1, 0, 0, 500, 200);

        page1.setMinHeight(24);
        tab.onMeasure();
        assertMeasure(tab, 500, 400);
        tab.onLayout(500, 400);
        assertLayout(page1, 0, 188, 500, 212);
        assertLayout(frame1, 0, 0, 500, 188);

    }

    private void assertMeasure(Widget widget, float width, float height) {
        assertEquals("Measure Width", width, widget.getMeasureWidth(), 0.1f);
        assertEquals("Measure Height", height, widget.getMeasureHeight(), 0.1f);
    }

    private void assertLayout(Widget widget, float x, float y, float width, float height) {
        assertEquals("X", x, widget.getX(), 0.1f);
        assertEquals("Y", y, widget.getY(), 0.1f);
        assertEquals("Width", width, widget.getLayoutWidth(), 0.1f);
        assertEquals("Height", height, widget.getLayoutHeight(), 0.1f);
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("horizontal-align"), new UXValueText(HorizontalAlign.CENTER.toString()));
        hash.put(UXHash.getHash("vertical-align"), new UXValueText(VerticalAlign.BOTTOM.toString()));
        hash.put(UXHash.getHash("pages-horizontal-align"), new UXValueText(HorizontalAlign.RIGHT.toString()));
        hash.put(UXHash.getHash("pages-vertical-align"), new UXValueText(VerticalAlign.MIDDLE.toString()));
        hash.put(UXHash.getHash("pages-vertical-position"), new UXValueText(VerticalPosition.BOTTOM.toString()));
        hash.put(UXHash.getHash("pages-preft-height"), new UXValueSizeDp(16));
        hash.put(UXHash.getHash("scroll-sensibilityt"), new UXValueNumber(5));
        return hash;
    }
}