package flat.uxml;

import flat.uxml.value.UXValueNumber;
import flat.widget.State;
import org.junit.Test;

import static org.junit.Assert.*;

public class UXStyleTest {

    @Test
    public void constructor() {
        UXStyle style = new UXStyle("style");
        assertEquals("style", style.getName());

        UXStyle style2 = new UXStyle("style", "parent", null);
        assertEquals("style", style2.getName());
        assertEquals("parent", style2.getParentName());

        UXStyle style3Parent = new UXStyle("style-parent", "parent", null);
        UXStyle style3 = new UXStyle("style", style3Parent);
        assertEquals("style", style3.getName());
        assertEquals("style-parent", style3.getParentName());
        assertEquals("style-parent", style3Parent.getName());
        assertEquals("parent", style3Parent.getParentName());
    }

    @Test
    public void attribute() {
        UXStyle style = new UXStyle("style");
        style.add(UXHash.getHash("property"), State.ENABLED, new UXValueNumber(100));
        style.add(UXHash.getHash("property"), State.PRESSED, new UXValueNumber(200));
        style.add(UXHash.getHash("property"), State.HOVERED, new UXValueNumber(300));

        assertTrue("Style attribute", style.contains("property"));
        assertTrue("Style attribute", style.contains(UXHash.getHash("property")));

        assertTrue("Style change between states", style.containsChange(
                State.ENABLED.bitset(), (byte) (State.ENABLED.bitset() | State.PRESSED.bitset())));

        assertFalse("Style change between states", style.containsChange(
                State.ENABLED.bitset(), (byte) (State.ENABLED.bitset() | State.FOCUSED.bitset())));

        assertTrue("Style change between states", style.containsChange(
                (byte) (State.ENABLED.bitset() | State.PRESSED.bitset()),
                (byte) (State.ENABLED.bitset() | State.HOVERED.bitset()))
        );

        assertEquals("Style attribute", new UXValueNumber(100), style.get("property", State.ENABLED.ordinal()));
    }

    @Test
    public void attributeFromParent() {
        UXStyle parentStyle = new UXStyle("style");
        parentStyle.add(UXHash.getHash("parent-property"), State.ENABLED, new UXValueNumber(100));
        parentStyle.add(UXHash.getHash("property"), State.HOVERED, new UXValueNumber(200));

        UXStyle style = new UXStyle("style", parentStyle);
        style.add(UXHash.getHash("property"), State.ENABLED, new UXValueNumber(300));

        assertEquals("Style attribute", new UXValueNumber(300), style.get("property", State.ENABLED.ordinal()));
        assertNull("Style attribute", style.get("property", State.HOVERED.ordinal()));
        assertEquals("Style attribute", new UXValueNumber(100), style.get("parent-property", State.ENABLED.ordinal()));

    }

    @Test
    public void parentNotAllowed() {
        UXStyle parentStyle = new UXStyle("parent-style");
        UXStyle style = new UXStyle("style", parentStyle);
        UXStyle childStyle = new UXStyle("child-style");
        assertFalse(parentStyle.setParent(style));
        assertTrue(childStyle.setParent(style));
    }
}