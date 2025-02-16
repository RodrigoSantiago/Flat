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

    public UXBuilder instance(Controller controller) {
        return new UXBuilder(this, controller);
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
        ArrayList<String> includes = new ArrayList<>();
        return parseInclude(stream, includes);
    }

    private static UXNode parseInclude(ResourceStream stream, ArrayList<String> includes) {
        Object obj = stream.getCache();
        if (obj != null) {
            if (obj instanceof UXNode) {
                return (UXNode) obj;
            } else {
                throw new FlatException("Invalid UXNode at:" + stream.getResourceName());
            }
        }

        try {
            includes.add(stream.getResourceName());
            UXNode node = read(stream, includes);
            if (node != null) {
                stream.putCache(node);
            }
            return node;
        } catch (IOException e) {
            throw new FlatException(e);
        }
    }

    private static UXNode read(ResourceStream stream, ArrayList<String> includes) throws IOException {
        if (stream.getStream() == null) {
            throw new FlatException("File not found at: " + stream.getResourceName());
        }
        String data = new String(stream.getStream().readAllBytes(), StandardCharsets.UTF_8);

        UXNodeParser reader = new UXNodeParser(data);
        reader.parse();

        UXNodeElement root = reader.getRootElement();
        if (root == null) {
            return null;
        } else {
            return readRecursive(stream, root, includes);
        }
    }

    private static UXNode readRecursive(ResourceStream stream, UXNodeElement element, ArrayList<String> includes) {
        // Name
        String name = element.getName();
        String style = null;

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

        // Include
        if (name.equals("Include")) {
            UXValue value = values.get(UXHash.getHash("src"));
            if (value != null) {
                ResourceStream relative = stream.getRelative(value.asString(null));
                if (includes.contains(relative.getResourceName())) {
                    return null;
                } else {
                    int size = includes.size();
                    UXNode include = parseInclude(relative, includes);
                    includes.subList(size, includes.size()).clear();

                    values.remove(UXHash.getHash("content"));
                    values.remove(UXHash.getHash("src"));
                    for (var entry : include.getValues().entrySet()) {
                        if (!values.containsKey(entry.getKey())) {
                            values.put(entry.getKey(), entry.getValue());
                        }
                    }
                    if (style == null) {
                        style = include.getStyle();
                    }
                    return new UXNode(include.getName(), style, values, include.getChildren());
                }
            }
            return null;
        }

        // Children
        List<UXNode> children = new ArrayList<>();
        for (UXNodeElement childNode : element.getChildren()) {
            UXNode child = readRecursive(stream, childNode, includes);
            if (child != null) {
                children.add(child);
            }
        }

        return new UXNode(name, style, values, children);
    }
}
