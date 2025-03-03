package flat.uxml;

import flat.resources.ResourceStream;
import flat.uxml.value.*;
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
public class UXNodeTest {

    @Test
    public void empty() {
        UXNode node = UXNode.parse(mockStream(""));
        assertNull(node);
    }

    @Test
    public void root() {
        UXNode node = UXNode.parse(mockStream("<scene/>"));
        assertNotNull(node);
        assertNode(node, "scene");
        assertChild(node);
    }

    @Test
    public void attribute() {
        UXNode node = UXNode.parse(mockStream("<scene width=\"500dp\"/>"));
        assertNotNull(node);
        assertNode(node, "scene",
                "width", new UXValueXML("500dp", new UXValueSizeDp(500))
        );
        assertChild(node);
    }

    @Test
    public void attributeBoolean() {
        UXNode node = UXNode.parse(mockStream("<scene active/>"));
        assertNotNull(node);
        assertNode(node, "scene",
                "active", new UXValueBool(true)
        );
        assertChild(node);
    }

    @Test
    public void content() {
        UXNode node = UXNode.parse(mockStream("<scene> Content </scene>"));
        assertNotNull(node);
        assertNode(node, "scene",
                "content", new UXValueText(" Content ")
        );
        assertChild(node);
    }

    @Test
    public void style() {
        UXNode node = UXNode.parse(mockStream("<scene style=\"500dp\"/>"));
        assertNotNull(node);
        assertNode(node, "scene");
        assertEquals(1, node.getStyles().size());
        assertEquals("500dp", node.getStyles().get(0));
        assertChild(node);
    }

    @Test
    public void simpleChild() {
        UXNode node = UXNode.parse(mockStream(
                """
                <scene><button></button></scene>
                """));
        assertNotNull(node);
        assertNode(node, "scene");
        assertChild(node, "button");
    }

    @Test
    public void ignoreParseErros() {
        UXNode node = UXNode.parse(mockStream("<scene parse=/>"));
        assertNotNull(node);
        assertNode(node, "scene",
                "parse", new UXValueBool(true)
        );
        assertChild(node);
    }

    @Test
    public void include() {
        ResourceStream stream = mockStream(
                """
                <scene><Include src=\"stream/include\"></button></scene>
                """);
        ResourceStream streamInclude = mockStream(
                """
                <button></button>
                """);
        when(stream.getResourceName()).thenReturn("/stream");
        when(streamInclude.getResourceName()).thenReturn("/stream/include");
        when(stream.getRelative("stream/include")).thenReturn(streamInclude);
        UXNode node = UXNode.parse(stream);
        assertNotNull(node);
        assertNode(node, "scene");
        assertChild(node, "button");
    }

    @Test
    public void includeOverrideAttributes() {
        ResourceStream stream = mockStream(
                """
                <scene><Include src=\"stream/include\" width=\"100\" height=\"20\"></button></scene>
                """);
        ResourceStream streamInclude = mockStream(
                """
                <button width=\"50\"></button>
                """);
        when(stream.getResourceName()).thenReturn("/stream");
        when(streamInclude.getResourceName()).thenReturn("/stream/include");
        when(stream.getRelative("stream/include")).thenReturn(streamInclude);
        UXNode node = UXNode.parse(stream);
        assertNotNull(node);
        assertNode(node, "scene");
        assertChild(node, "button");
        assertNode(node.getChildren().get(0), "button",
                "width", new UXValueXML("100", new UXValueNumber(100)),
                "height", new UXValueXML("20", new UXValueNumber(20))
        );
    }

    @Test
    public void includeCycle() {
        ResourceStream stream = mockStream(
                """
                <scene><Include src=\"stream/include\"></button></scene>
                """);
        ResourceStream streamInclude = mockStream(
                """
                <button><Include src=\"stream\"></button>
                """);
        when(stream.getResourceName()).thenReturn("/stream");
        when(streamInclude.getResourceName()).thenReturn("/stream/include");
        when(stream.getRelative("stream/include")).thenReturn(streamInclude);
        when(streamInclude.getRelative("stream")).thenReturn(stream);
        UXNode node = UXNode.parse(stream);
        assertNotNull(node);
        assertNode(node, "scene");
        assertChild(node, "button");
    }

    @Test
    public void includeCycleNested() {
        ResourceStream stream = mockStream(
                """
                <scene><Include src=\"stream/include\"></button></scene>
                """);
        ResourceStream streamInclude = mockStream(
                """
                <button><Include src=\"stream/include\"></button>
                """);
        when(stream.getResourceName()).thenReturn("/stream");
        when(streamInclude.getResourceName()).thenReturn("/stream/include");
        when(stream.getRelative("stream/include")).thenReturn(streamInclude);
        when(streamInclude.getRelative("stream/include")).thenReturn(stream);
        UXNode node = UXNode.parse(stream);
        assertNotNull(node);
        assertNode(node, "scene");
        assertChild(node, "button");
    }

    public void assertNode(UXNode node, String name, Object... pair) {
        assertEquals("Invalid Node name", name, node.getName());

        var list = new ArrayList<String>();
        for (int i = 0; i < pair.length; i += 2) {
            String attrName = (String) pair[i];
            UXValue value = (UXValue) pair[i + 1];
            Integer hash = UXHash.getHash(attrName);

            UXValue attr = node.getValues().get(hash);
            if (attr == null) {
                fail("Attribute not found : " + attrName + " - #" + hash);
            }
            assertEquals("Attribute \"" + attrName + "\" with a different value", value, attr);
            list.add(attrName);
        }
        int diff = node.getValues() == null ? 0 : node.getValues().size() - list.size();
        if (diff > 0) {
            fail("There are {" + diff + "} attributes not expected");
        }
    }

    public void assertChild(UXNode node, Object... children) {
        var list = new ArrayList<>(node.getChildren());
        for (int i = 0; i < children.length; i ++) {
            String childName = (String) children[i];
            boolean found = false;
            for (var child : list) {
                if (child.getName().equals(childName)) {
                    found = true;
                    list.remove(child);
                    break;
                }
            }
            if (!found) {
                fail("Child not found : '" + childName + "' in a total of " + node.getChildren().size());
            }
        }
        if (list.size() > 0) {
            fail("Child not expected(" + list.size() + ") : '" + listToString(list) + "'");
        }
    }

    private String listToString(List<?> list) {
        StringBuilder sb = new StringBuilder();
        for (var obj : list) {
            sb.append(", ").append(obj);
        }
        return sb.substring(Math.min(sb.length(), 2));
    }

    private ResourceStream mockStream(String value) {
        ResourceStream stream = mock(ResourceStream.class);
        when(stream.getStream()).thenReturn(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
        return stream;
    }
}