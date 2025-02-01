package flat.widget.layout;

import flat.graphics.text.Align;
import flat.uxml.UXHash;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueText;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class StackBoxTest {

    @Test
    public void alignStyles() {
        StackBox box = new StackBox();
        box.setAttributes(createNonDefaultValues(), "stack-box");
        box.applyStyle();

        assertEquals(Align.Horizontal.LEFT, box.getHorizontalAlign());
        assertEquals(Align.Vertical.TOP, box.getVerticalAlign());
    }

    @Test
    public void alignOneChild() {
        StackBox parent = new StackBox();
        StackBox child = new StackBox();

        parent.setPrefSize(200, 350);
        child.setPrefSize(100, 120);
        parent.add(child);

        parent.setHorizontalAlign(Align.Horizontal.LEFT);
        parent.setVerticalAlign(Align.Vertical.BOTTOM);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(0, child.getX(), 0.001f);
        assertEquals(230, child.getY(), 0.001f);

        parent.setHorizontalAlign(Align.Horizontal.RIGHT);
        parent.setVerticalAlign(Align.Vertical.TOP);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(100, child.getX(), 0.001f);
        assertEquals(0, child.getY(), 0.001f);

        parent.setHorizontalAlign(Align.Horizontal.CENTER);
        parent.setVerticalAlign(Align.Vertical.MIDDLE);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(50, child.getX(), 0.001f);
        assertEquals(115, child.getY(), 0.001f);

        parent.setHorizontalAlign(Align.Horizontal.LEFT);
        parent.setVerticalAlign(Align.Vertical.TOP);
        parent.setMargins(2, 3, 4, 5);
        parent.setPadding(3, 4, 5, 6);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(11, child.getX(), 0.001f);
        assertEquals(5, child.getY(), 0.001f);

        parent.setHorizontalAlign(Align.Horizontal.RIGHT);
        parent.setVerticalAlign(Align.Vertical.BASELINE);
        parent.setMargins(2, 3, 4, 5);
        parent.setPadding(3, 4, 5, 6);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(93, child.getX(), 0.001f);
        assertEquals(221, child.getY(), 0.001f);
    }

    @Test
    public void alignSiblings() {
        StackBox parent = new StackBox();
        StackBox child1 = new StackBox();
        StackBox child2 = new StackBox();

        parent.setPrefSize(200, 350);
        child1.setPrefSize(100, 120);
        child2.setPrefSize(50, 60);
        parent.add(child1, child2);

        parent.setHorizontalAlign(Align.Horizontal.LEFT);
        parent.setVerticalAlign(Align.Vertical.BOTTOM);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(0, child1.getX(), 0.001f);
        assertEquals(230, child1.getY(), 0.001f);

        assertEquals(0, child2.getX(), 0.001f);
        assertEquals(290, child2.getY(), 0.001f);

        parent.setHorizontalAlign(Align.Horizontal.RIGHT);
        parent.setVerticalAlign(Align.Vertical.TOP);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(100, child1.getX(), 0.001f);
        assertEquals(0, child1.getY(), 0.001f);
        assertEquals(150, child2.getX(), 0.001f);
        assertEquals(0, child2.getY(), 0.001f);

        parent.setHorizontalAlign(Align.Horizontal.CENTER);
        parent.setVerticalAlign(Align.Vertical.MIDDLE);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(50, child1.getX(), 0.001f);
        assertEquals(115, child1.getY(), 0.001f);
        assertEquals(75, child2.getX(), 0.001f);
        assertEquals(145, child2.getY(), 0.001f);

        parent.setHorizontalAlign(Align.Horizontal.LEFT);
        parent.setVerticalAlign(Align.Vertical.TOP);
        parent.setMargins(2, 3, 4, 5);
        parent.setPadding(3, 4, 5, 6);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(11, child1.getX(), 0.001f);
        assertEquals(5, child1.getY(), 0.001f);
        assertEquals(11, child2.getX(), 0.001f);
        assertEquals(5, child2.getY(), 0.001f);

        parent.setHorizontalAlign(Align.Horizontal.RIGHT);
        parent.setVerticalAlign(Align.Vertical.BASELINE);
        parent.setMargins(2, 3, 4, 5);
        parent.setPadding(3, 4, 5, 6);
        parent.onMeasure();
        parent.onLayout(200, 350);

        assertEquals(93, child1.getX(), 0.001f);
        assertEquals(221, child1.getY(), 0.001f);
        assertEquals(143, child2.getX(), 0.001f);
        assertEquals(281, child2.getY(), 0.001f);
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();
        hash.put(UXHash.getHash("horizontal-align"), new UXValueText(Align.Horizontal.LEFT.toString()));
        hash.put(UXHash.getHash("vertical-align"), new UXValueText(Align.Vertical.TOP.toString()));
        return hash;
    }
}
