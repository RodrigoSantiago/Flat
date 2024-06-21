package flat.uxml;

import flat.uxml.value.UXValueNumber;
import flat.uxml.value.UXValueText;
import flat.widget.State;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
public class UXSheetTest {

    @Test
    public void empty() {
        UXSheet sheet = UXSheet.parse("");
        assertNotNull(sheet);
    }

    @Test
    public void emptyStyle() {
        UXSheet sheet = UXSheet.parse("simple { }");
        assertNotNull(sheet);

        UXStyle style = sheet.getStyle("simple");
        assertNotNull(style);
        assertEmptyStyle(style);
    }

    @Test
    public void simpleStyle() {
        UXSheet sheet = UXSheet.parse("simple { width : 100; }");
        assertNotNull(sheet);

        UXStyle style = sheet.getStyle("simple");
        assertNotNull(style);
        assertStyles(style, State.ENABLED,
                "width", new UXValueNumber(100)
        );
    }

    @Test
    public void parentStyle() {
        UXSheet sheet = UXSheet.parse("simple { width : 100; } complex : simple { height : 200; }");
        assertNotNull(sheet);

        UXStyle simple = sheet.getStyle("simple");
        assertNotNull(simple);
        assertStyles(simple, State.ENABLED,
                "width", new UXValueNumber(100)
        );

        UXStyle complex = sheet.getStyle("complex");
        assertNotNull(complex);
        assertStyles(complex, State.ENABLED,
                "width", new UXValueNumber(100),
                "height", new UXValueNumber(200)
        );
    }

    @Test
    public void parentStyleBefore() {
        UXSheet sheet = UXSheet.parse("complex : simple { height : 200; } simple { width : 100; }");
        assertNotNull(sheet);

        UXStyle simple = sheet.getStyle("simple");
        assertNotNull(simple);
        assertStyles(simple, State.ENABLED,
                "width", new UXValueNumber(100)
        );

        UXStyle complex = sheet.getStyle("complex");
        assertNotNull(complex);
        assertStyles(complex, State.ENABLED,
                "width", new UXValueNumber(100),
                "height", new UXValueNumber(200)
        );
    }

    @Test
    public void stateStyle() {
        String value = "simple { " +
                "width : 100; " +
                "focused { width : 200; } " +
                "activated { width : 300; } " +
                "hovered { width : 400; } " +
                "pressed { width : 500; } " +
                "dragged { width : 600; } " +
                "error { width : 700; } " +
                "disabled { width : 800; } " +
                "enabled { width : 900; } " +
                "}";
        UXSheet sheet = UXSheet.parse(value);
        assertNotNull(sheet);

        UXStyle style = sheet.getStyle("simple");
        assertNotNull(style);
        assertStyles(style, State.FOCUSED,
                "width", new UXValueNumber(200)
        );
        assertStyles(style, State.ACTIVATED,
                "width", new UXValueNumber(300)
        );
        assertStyles(style, State.HOVERED,
                "width", new UXValueNumber(400)
        );
        assertStyles(style, State.PRESSED,
                "width", new UXValueNumber(500)
        );
        assertStyles(style, State.DRAGGED,
                "width", new UXValueNumber(600)
        );
        assertStyles(style, State.ERROR,
                "width", new UXValueNumber(700)
        );
        assertStyles(style, State.DISABLED,
                "width", new UXValueNumber(800)
        );
        assertStyles(style, State.ENABLED,
                "width", new UXValueNumber(900)
        );
    }

    @Test
    public void styleValueString() {
        UXSheet sheet = UXSheet.parse("simple { text : \"open {\\\"}\"; }");
        assertNotNull(sheet);

        UXStyle style = sheet.getStyle("simple");
        assertNotNull(style);
        assertStyles(style, State.ENABLED,
                "text", new UXValueText("open {\"}")
        );
    }

    public void assertEmptyStyle(UXStyle style) {
        assertEquals(0, style.entries.size());
    }

    public void assertStyles(UXStyle style, State state, Object... pair) {
        for (int i = 0; i < pair.length / 2; i++) {
            Integer hash = UXHash.getHash((String) pair[i * 2]);
            UXValue value = (UXValue) pair[i * 2 + 1];

            UXValue[] values = style.get(hash);
            if (values == null) {
                fail("Unexpected property value at " + pair[i * 2]);
            }
            assertEquals(value, values[state.ordinal()]);
        }
    }
}