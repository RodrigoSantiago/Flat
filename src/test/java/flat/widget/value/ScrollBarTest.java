package flat.widget.value;

import flat.uxml.Controller;
import flat.uxml.UXBuilder;
import flat.uxml.UXHash;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueColor;
import flat.uxml.value.UXValueNumber;
import flat.uxml.value.UXValueText;
import flat.widget.Widget;
import flat.widget.enums.Direction;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;

public class ScrollBarTest {

    Controller controller;
    UXBuilder builder;

    @Before
    public void before() {
        controller = mock(Controller.class);
        builder = mock(UXBuilder.class);
    }

    @Test
    public void properties() {
        ScrollBar scrollBar = new ScrollBar();

        assertEquals(0, scrollBar.getTotalDimension(), 0.1f);
        assertEquals(0, scrollBar.getViewDimension(), 0.1f);
        assertEquals(0, scrollBar.getViewOffset(), 0.1f);
        assertEquals(Direction.VERTICAL, scrollBar.getDirection());
        assertEquals(0f, scrollBar.getMinSize(), 0.0001f);
        assertEquals(0xFFFFFFFF, scrollBar.getColor());

        scrollBar.setAttributes(createNonDefaultValues(), "scrollbar");
        scrollBar.applyAttributes(null);

        assertEquals(200, scrollBar.getTotalDimension(), 0.1f);
        assertEquals(50, scrollBar.getViewDimension(), 0.1f);
        assertEquals(100, scrollBar.getViewOffset(), 0.1f);
        assertEquals(Direction.HORIZONTAL, scrollBar.getDirection());
        assertEquals(0f, scrollBar.getMinSize(), 0.0001f);
        assertEquals(0xFFFFFFFF, scrollBar.getColor());

        scrollBar.applyStyle();

        assertEquals(200, scrollBar.getTotalDimension(), 0.1f);
        assertEquals(50, scrollBar.getViewDimension(), 0.1f);
        assertEquals(100, scrollBar.getViewOffset(), 0.1f);
        assertEquals(Direction.HORIZONTAL, scrollBar.getDirection());
        assertEquals(0.1f, scrollBar.getMinSize(), 0.0001f);
        assertEquals(0xFF0000FF, scrollBar.getColor());
    }

    @Test
    public void measure() {
        ScrollBar scrollBar = new ScrollBar();
        scrollBar.setTotalDimension(200);
        scrollBar.setViewDimension(50);
        scrollBar.setViewOffset(100);
        scrollBar.setDirection(Direction.VERTICAL);
        scrollBar.onMeasure();

        assertEquals(165, scrollBar.getMeasureWidth(), 0.1f);
        assertEquals(32, scrollBar.getMeasureHeight(), 0.1f);

        scrollBar.setMargins(1, 2, 3, 4);
        scrollBar.setPadding(5, 4, 2, 3);
        scrollBar.onMeasure();

        assertEquals(178, scrollBar.getMeasureWidth(), 0.1f);
        assertEquals(43, scrollBar.getMeasureHeight(), 0.1f);

        scrollBar.setPrefSize(100, 200);
        scrollBar.onMeasure();

        assertEquals(106, scrollBar.getMeasureWidth(), 0.1f);
        assertEquals(204, scrollBar.getMeasureHeight(), 0.1f);

        scrollBar.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scrollBar.onMeasure();

        assertEquals(Widget.MATCH_PARENT, scrollBar.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, scrollBar.getMeasureHeight(), 0.1f);
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("total-dimension"), new UXValueNumber(200));
        hash.put(UXHash.getHash("view-dimension"), new UXValueNumber(50));
        hash.put(UXHash.getHash("view-offset"), new UXValueNumber(100));
        hash.put(UXHash.getHash("direction"), new UXValueText(Direction.HORIZONTAL.toString()));
        hash.put(UXHash.getHash("min-size"), new UXValueNumber(0.1f));
        hash.put(UXHash.getHash("color"), new UXValueColor(0xFF0000FF));
        return hash;
    }
}