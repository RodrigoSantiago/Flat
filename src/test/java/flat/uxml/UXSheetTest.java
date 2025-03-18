package flat.uxml;

import flat.resources.ResourceStream;
import flat.uxml.sheet.UXSheetParser;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueNumber;
import flat.uxml.value.UXValueText;
import flat.uxml.value.UXValueVariable;
import flat.widget.State;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class UXSheetTest {

    @Test
    public void empty() {
        UXSheet sheet = UXSheet.parse(mockStream(""));
        assertNotNull(sheet);
    }

    @Test
    public void cache() {
        UXSheet cache = new UXSheet();
        ResourceStream stream = mock(ResourceStream.class);
        when(stream.getCache()).thenReturn(cache);

        UXSheet sheet = UXSheet.parse(stream);
        assertEquals(cache, sheet);
    }

    @Test
    public void emptyStyle() {
        UXSheet sheet = UXSheet.parse(mockStream("simple { }"));
        assertNotNull(sheet);

        UXStyle style = sheet.getStyle("simple");
        assertNotNull(style);
        assertEmptyStyle(style);
        assertLogs(sheet.getLogs());
    }

    @Test
    public void simpleStyle() {
        UXSheet sheet = UXSheet.parse(mockStream("simple { width : 100; }"));
        assertNotNull(sheet);

        UXStyle style = sheet.getStyle("simple");
        assertNotNull(style);
        assertStyles(style, State.ENABLED,
                "width", new UXValueNumber(100)
        );
        assertLogs(sheet.getLogs());
    }

    @Test
    public void parentStyle() {
        UXSheet sheet = UXSheet.parse(mockStream("simple { width : 100; } complex : simple { height : 200; }"));
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
        assertLogs(sheet.getLogs());
    }

    @Test
    public void parentStyleBefore() {
        UXSheet sheet = UXSheet.parse(mockStream("complex : simple { height : 200; } simple { width : 100; }"));
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
        assertLogs(sheet.getLogs());
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
                "undefined { width : 700; } " +
                "disabled { width : 800; } " +
                "enabled { width : 900; } " +
                "}";
        UXSheet sheet = UXSheet.parse(mockStream(value));
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
        assertStyles(style, State.UNDEFINED,
                "width", new UXValueNumber(700)
        );
        assertStyles(style, State.DISABLED,
                "width", new UXValueNumber(800)
        );
        assertStyles(style, State.ENABLED,
                "width", new UXValueNumber(900)
        );
        assertLogs(sheet.getLogs());
    }

    @Test
    public void styleValueString() {
        UXSheet sheet = UXSheet.parse(mockStream("simple { text : \"open {\\\"}\"; }"));
        assertNotNull(sheet);

        UXStyle style = sheet.getStyle("simple");
        assertNotNull(style);
        assertStyles(style, State.ENABLED,
                "text", new UXValueText("open {\"}")
        );
        assertLogs(sheet.getLogs());
    }

    @Test
    public void variable() {
        UXSheet sheet = UXSheet.parse(mockStream("$var : 10; simple { size : $var; }"));
        assertNotNull(sheet);

        UXStyle style = sheet.getStyle("simple");
        assertNotNull(style);
        assertStyles(style, State.ENABLED,
                "size", new UXValueVariable("$var")
        );
        assertEquals(new UXValueNumber(10), sheet.getVariableInitialValue("$var"));
        assertLogs(sheet.getLogs());
    }

    @Test
    public void multipleFiles() {
        List<ResourceStream> streams = new ArrayList<>();
        streams.add(mockStream("complex : simple { height : 200; }"));
        streams.add(mockStream("simple { width : 100; }"));
        ResourceStream folderStream = mock(ResourceStream.class);
        when(folderStream.isFolder()).thenReturn(true);
        when(folderStream.getFiles()).thenReturn(streams);

        UXSheet sheet = UXSheet.parse(folderStream);
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
        assertLogs(sheet.getLogs());
    }

    @Test
    public void fail_CyclicParent() {
        UXSheet sheet = UXSheet.parse(mockStream("simple : parent { size : 10; } parent : simple { size : 10; }"));
        assertNotNull(sheet);

        UXStyle simple = sheet.getStyle("simple");
        assertNotNull(simple);
        assertStyles(simple, State.ENABLED,
                "size", new UXValueNumber(10)
        );
        UXStyle parent = sheet.getStyle("simple");
        assertNotNull(parent);
        assertStyles(parent, State.ENABLED,
                "size", new UXValueNumber(10)
        );
        assertLogs(sheet.getLogs(), UXSheetParser.ErroLog.CYCLIC_PARENT);
    }

    @Test
    public void fail_RepeatedVariable() {
        UXSheet sheet = UXSheet.parse(mockStream("$var : 10; $var : 20;"));
        assertNotNull(sheet);

        assertEquals(new UXValueNumber(20), sheet.getVariableInitialValue("$var"));
        assertLogs(sheet.getLogs(), UXSheetParser.ErroLog.REPEATED_VARIABLE);
    }

    @Test
    public void fail_RepeatedVariableAtMultipleFiles() {
        List<ResourceStream> streams = new ArrayList<>();
        streams.add(mockStream("$var : 10;"));
        streams.add(mockStream("$var : 10;"));
        ResourceStream folderStream = mock(ResourceStream.class);
        when(folderStream.isFolder()).thenReturn(true);
        when(folderStream.getFiles()).thenReturn(streams);

        UXSheet sheet = UXSheet.parse(folderStream);
        assertNotNull(sheet);

        assertEquals(new UXValueNumber(10), sheet.getVariableInitialValue("$var"));
        assertLogs(sheet.getLogs(), UXSheetParser.ErroLog.REPEATED_VARIABLE);
    }

    @Test
    public void fail_RepeatedStyle() {
        UXSheet sheet = UXSheet.parse(mockStream("simple { size : 10; } simple { size : 20; }"));
        assertNotNull(sheet);

        UXStyle simple = sheet.getStyle("simple");
        assertNotNull(simple);
        assertStyles(simple, State.ENABLED,
                "size", new UXValueNumber(20)
        );
        assertLogs(sheet.getLogs(), UXSheetParser.ErroLog.REPEATED_STYLE);
    }

    @Test
    public void fail_RepeatedStyleAtMultipleFiles() {
        List<ResourceStream> streams = new ArrayList<>();
        streams.add(mockStream("simple { height : 200; }"));
        streams.add(mockStream("simple { width : 100; }"));
        ResourceStream folderStream = mock(ResourceStream.class);
        when(folderStream.isFolder()).thenReturn(true);
        when(folderStream.getFiles()).thenReturn(streams);

        UXSheet sheet = UXSheet.parse(folderStream);
        assertNotNull(sheet);

        UXStyle simple = sheet.getStyle("simple");
        assertNotNull(simple);
        assertLogs(sheet.getLogs(), UXSheetParser.ErroLog.REPEATED_STYLE);
    }

    private ResourceStream mockStream(String value) {
        ResourceStream stream = mock(ResourceStream.class);
        when(stream.readData()).thenReturn(value.getBytes(StandardCharsets.UTF_8));
        return stream;
    }

    public void assertEmptyStyle(UXStyle style) {
        assertEquals(0, style.getEntries().size());
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

    public void assertLogs(List<UXSheetParser.ErroLog> actual, String... expected) {
        List<UXSheetParser.ErroLog> clone = new ArrayList<>(actual);
        for (var str : expected) {
            int found = -1;
            for (int i = 0; i < clone.size(); i++) {
                if (clone.get(i).message().startsWith(str)) {
                    found = i;
                    break;
                }
            }
            if (found == -1) {
                fail("Expected erro log not found '" + str + "'");
            }
            clone.remove(found);
        }
        if (clone.size() > 0) {
            fail("Unexpected erro log '" + clone.get(0) + "' total(" + clone.size()+")");
        }
    }
}