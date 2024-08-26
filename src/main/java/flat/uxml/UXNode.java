package flat.uxml;

import flat.exception.FlatException;
import flat.resources.ResourceStream;
import flat.uxml.node.UXNodeAttribute;
import flat.uxml.node.UXNodeElement;
import flat.uxml.node.UXNodeParser;
import flat.uxml.sheet.UXSheetParser;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueBool;
import flat.uxml.value.UXValueText;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UXNode {
    private String name;
    private String style;
    private HashMap<Integer, UXValue> values;
    private List<UXNode> children;

    public UXNode(String name, String style, HashMap<Integer, UXValue> values, List<UXNode> children) {
        this.name = name;
        this.style = style;
        this.values = values;
        this.children = children;
    }

    public UXBuilder instance() {
        return new UXBuilder(this);
    }

    public String getName() {
        return name;
    }

    public String getStyle() {
        return style;
    }

    public HashMap<Integer, UXValue> getValues() {
        return values;
    }

    public List<UXNode> getChildren() {
        return children;
    }

    public static UXNode parse(ResourceStream stream) {
        Object obj = stream.getCache();
        if (obj != null) {
            if (obj instanceof UXNode) {
                return (UXNode) obj;
            } else {
                throw new FlatException("Invalid UXNode at:" + stream.getResourceName());
            }
        }

        try {
            UXNode node = read(stream);
            stream.putCache(node);
            return node;
        } catch (IOException e) {
            throw new FlatException(e);
        }
    }

    private static UXNode read(ResourceStream stream) throws IOException {
        String data = new String(stream.getStream().readAllBytes(), StandardCharsets.UTF_8);

        UXNodeParser reader = new UXNodeParser(data);
        reader.parse();

        UXNodeElement root = reader.getRootElement();
        if (root == null) {
            return new UXNode("scene", "scene", null, new ArrayList<>());
        } else {
            return readRecursive(root);
        }

    }

    private static UXNode readRecursive(UXNodeElement element) {
        // Name
        String name = element.getName();
        String style = name.toLowerCase();

        // Attributes
        HashMap<Integer, UXValue> values = new HashMap<>();
        HashMap<String, UXNodeAttribute> attrs = element.getAttributes();
        if (element.getContent() != null) {
            values.put(UXHash.getHash("content"), new UXValueText(element.getContent()));
        }
        for (var item : attrs.values()) {
            String att = item.getName();
            UXValue value = item.getValue();
            if (att.equals("style")) {
                style = value.asString(null);
            } else if (value == null) {
                values.put(UXHash.getHash(att), new UXValueBool(true));
            } else {
                values.put(UXHash.getHash(att), value);
            }
        }

        // Children
        List<UXNode> children = new ArrayList<>();
        for (UXNodeElement childNode : element.getChildren()) {
            children.add(readRecursive(childNode));
        }
        return new UXNode(name, style, values, children);
    }
}
