package flat.widget.structure;

import flat.events.SlideEvent;
import flat.graphics.context.Font;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueNumber;
import flat.uxml.value.UXValueSizeSp;
import flat.uxml.value.UXValueText;
import flat.widget.Widget;
import flat.widget.layout.Panel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class, Font.class, DrawableReader.class})
public class ListViewTest {

    Font defaultFont;

    ResourceStream resIcon;
    Drawable icon;
    ResourceStream resStateIconOpen;
    Drawable stateIconOpen;
    ResourceStream resStateIconClosed;
    Drawable stateIconClosed;

    @Before
    public void before() {
        mockStatic(Font.class);

        defaultFont = mock(Font.class);
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

        stateIconClosed = mock(Drawable.class);
        when(stateIconClosed.getWidth()).thenReturn(20f);
        when(stateIconClosed.getHeight()).thenReturn(18f);

        resStateIconClosed = mock(ResourceStream.class);
        when(DrawableReader.parse(resStateIconClosed)).thenReturn(stateIconClosed);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        var slideHorizontal = (UXListener<SlideEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onSlideHorizontalWork", SlideEvent.class)).thenReturn(slideHorizontal);

        var slideVertical = (UXListener<SlideEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onSlideVerticalWork", SlideEvent.class)).thenReturn(slideVertical);

        var filterHorizontal = (UXListener<SlideEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onFilterHorizontalWork", SlideEvent.class)).thenReturn(filterHorizontal);

        var filterVertical = (UXListener<SlideEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onFilterVerticalWork", SlideEvent.class)).thenReturn(filterVertical);

        var listenerx = (UXValueListener<Float>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onViewOffsetXWork", Float.class)).thenReturn(listenerx);

        var listenery = (UXValueListener<Float>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onViewOffsetYWork", Float.class)).thenReturn(listenery);

        ListView listView = new ListView();

        assertEquals(8, listView.getItemHeight(), 0.001f);
        assertEquals(10, listView.getScrollSensibility(), 0.001f);
        assertNull(listView.getSlideHorizontalFilter());
        assertNull(listView.getSlideVerticalFilter());
        assertNull(listView.getSlideHorizontalListener());
        assertNull(listView.getSlideVerticalListener());
        assertNull(listView.getViewOffsetXListener());
        assertNull(listView.getViewOffsetYListener());

        listView.setAttributes(createNonDefaultValues(), "list-view");
        listView.applyAttributes(controller);

        assertEquals(8, listView.getItemHeight(), 0.001f);
        assertEquals(10, listView.getScrollSensibility(), 0.001f);
        assertEquals(filterHorizontal, listView.getSlideHorizontalFilter());
        assertEquals(filterVertical, listView.getSlideVerticalFilter());
        assertEquals(slideHorizontal, listView.getSlideHorizontalListener());
        assertEquals(slideVertical, listView.getSlideVerticalListener());
        assertEquals(listenerx, listView.getViewOffsetXListener());
        assertEquals(listenery, listView.getViewOffsetYListener());

        listView.applyStyle();

        assertEquals(32, listView.getItemHeight(), 0.001f);
        assertEquals(5, listView.getScrollSensibility(), 0.001f);
        assertEquals(filterHorizontal, listView.getSlideHorizontalFilter());
        assertEquals(filterVertical, listView.getSlideVerticalFilter());
        assertEquals(slideHorizontal, listView.getSlideHorizontalListener());
        assertEquals(slideVertical, listView.getSlideVerticalListener());
        assertEquals(listenerx, listView.getViewOffsetXListener());
        assertEquals(listenery, listView.getViewOffsetYListener());
    }

    @Test
    public void adapter() {
        ListViewAdapter adapter = mock(ListViewAdapter.class);
        when(adapter.createListItem()).thenReturn(new Panel());

        ListViewAdapter adapter2 = mock(ListViewAdapter.class);
        when(adapter2.createListItem()).thenReturn(new Panel());
        when(adapter2.size()).thenReturn(5);

        ListView listView = new ListView();
        listView.setItemHeight(8);    // ceil((16 * 1.25) / 8) + 1 = 4
        listView.setAdapter(adapter);
        verify(adapter, times(4)).createListItem();
        verify(adapter, times(0)).buildListItem(anyInt(), any());
        verify(adapter, times(0)).clearListItem(anyInt(), any());

        listView.setPrefSize(200, 100);
        listView.setItemHeight(20);    // ceil((100 * 1.25) / 20) + 1 = 8
        listView.setAdapter(adapter);
        when(adapter.getListView()).thenReturn(listView);
        listView.refreshItems();

        verify(adapter, times(8)).createListItem();
        verify(adapter, times(0)).buildListItem(anyInt(), any());
        verify(adapter, times(8)).clearListItem(anyInt(), any());

        when(adapter.size()).thenReturn(6);
        listView.refreshItems();

        verify(adapter, times(8)).createListItem();
        verify(adapter, times(6)).buildListItem(anyInt(), any());
        verify(adapter, times(10)).clearListItem(anyInt(), any());

        listView.setAdapter(adapter2);

        verify(adapter, times(8)).createListItem();
        verify(adapter, times(6)).buildListItem(anyInt(), any());
        verify(adapter, times(10)).clearListItem(anyInt(), any());
        verify(adapter2, times(8)).createListItem();
        verify(adapter2, times(5)).buildListItem(anyInt(), any());
        verify(adapter2, times(3)).clearListItem(anyInt(), any());
    }

    @Test
    public void measure() {
        ListViewAdapter adapter = mock(ListViewAdapter.class);
        when(adapter.createListItem()).thenAnswer((a) -> {
            var panel = new Panel();
            panel.setPrefSize(25, 60);
            return panel;
        });
        when(adapter.size()).thenReturn(5);

        ListViewAdapter adapter2 = mock(ListViewAdapter.class);
        when(adapter2.createListItem()).thenAnswer((a) -> {
            var panel = new Panel();
            panel.setPrefSize(25, Widget.MATCH_PARENT);
            return panel;
        });
        when(adapter2.size()).thenReturn(5);

        ListView listView = new ListView();
        listView.setItemHeight(8);
        listView.setAdapter(adapter);

        listView.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        listView.setAdapter(adapter);
        listView.onMeasure();
        assertEquals(25, listView.getMeasureWidth(), 0.0001f);
        assertEquals(40, listView.getMeasureHeight(), 0.0001f);

        listView.setAdapter(adapter2);
        listView.onMeasure();
        assertEquals(25, listView.getMeasureWidth(), 0.0001f);
        assertEquals(40, listView.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void scroll() {
        ListViewAdapter adapter = mock(ListViewAdapter.class);
        when(adapter.createListItem()).thenAnswer((a) -> {
            var panel = new Panel();
            panel.setPrefSize(300, 25);
            return panel;
        });
        when(adapter.size()).thenReturn(10);

        ListView listView = new ListView();
        listView.setItemHeight(25);
        listView.setAdapter(adapter);

        listView.setPrefSize(200, 100);
        listView.setAdapter(adapter);
        listView.refreshItems();

        listView.onMeasure();
        listView.onLayout(200, 100);
        listView.refreshItems();

        assertEquals(0, listView.getViewOffsetX(), 0.001f);
        assertEquals(200, listView.getViewDimensionX(), 0.001f);
        assertEquals(300, listView.getTotalDimensionX(), 0.001f);

        assertEquals(0, listView.getViewOffsetY(), 0.001f);
        assertEquals(100, listView.getViewDimensionY(), 0.001f);
        assertEquals(250, listView.getTotalDimensionY(), 0.001f);

        listView.setViewOffsetY(50);
        listView.setViewOffsetX(15);

        assertEquals(15, listView.getViewOffsetX(), 0.001f);
        assertEquals(200, listView.getViewDimensionX(), 0.001f);
        assertEquals(300, listView.getTotalDimensionX(), 0.001f);

        assertEquals(50, listView.getViewOffsetY(), 0.001f);
        assertEquals(100, listView.getViewDimensionY(), 0.001f);
        assertEquals(250, listView.getTotalDimensionY(), 0.001f);

        listView.slideTo(0, 0);

        assertEquals(0, listView.getViewOffsetX(), 0.001f);
        assertEquals(200, listView.getViewDimensionX(), 0.001f);
        assertEquals(300, listView.getTotalDimensionX(), 0.001f);

        assertEquals(0, listView.getViewOffsetY(), 0.001f);
        assertEquals(100, listView.getViewDimensionY(), 0.001f);
        assertEquals(250, listView.getTotalDimensionY(), 0.001f);

        listView.slide(10, 10);

        assertEquals(10, listView.getViewOffsetX(), 0.001f);
        assertEquals(200, listView.getViewDimensionX(), 0.001f);
        assertEquals(300, listView.getTotalDimensionX(), 0.001f);

        assertEquals(10, listView.getViewOffsetY(), 0.001f);
        assertEquals(100, listView.getViewDimensionY(), 0.001f);
        assertEquals(250, listView.getTotalDimensionY(), 0.001f);

        listView.slide(1000, 500);

        assertEquals(100, listView.getViewOffsetX(), 0.001f);
        assertEquals(200, listView.getViewDimensionX(), 0.001f);
        assertEquals(300, listView.getTotalDimensionX(), 0.001f);

        assertEquals(150, listView.getViewOffsetY(), 0.001f);
        assertEquals(100, listView.getViewDimensionY(), 0.001f);
        assertEquals(250, listView.getTotalDimensionY(), 0.001f);

        listView.slide(1000, 500);

        assertEquals(100, listView.getViewOffsetX(), 0.001f);
        assertEquals(200, listView.getViewDimensionX(), 0.001f);
        assertEquals(300, listView.getTotalDimensionX(), 0.001f);

        assertEquals(150, listView.getViewOffsetY(), 0.001f);
        assertEquals(100, listView.getViewDimensionY(), 0.001f);
        assertEquals(250, listView.getTotalDimensionY(), 0.001f);
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("on-slide-horizontal"), new UXValueText("onSlideHorizontalWork"));
        hash.put(UXHash.getHash("on-slide-vertical"), new UXValueText("onSlideVerticalWork"));
        hash.put(UXHash.getHash("on-slide-horizontal-filter"), new UXValueText("onFilterHorizontalWork"));
        hash.put(UXHash.getHash("on-slide-vertical-filter"), new UXValueText("onFilterVerticalWork"));
        hash.put(UXHash.getHash("on-view-offset-x-change"), new UXValueText("onViewOffsetXWork"));
        hash.put(UXHash.getHash("on-view-offset-y-change"), new UXValueText("onViewOffsetYWork"));

        hash.put(UXHash.getHash("item-height"), new UXValueSizeSp(32));
        hash.put(UXHash.getHash("scroll-sensibility"), new UXValueNumber(5));

        return hash;
    }

}