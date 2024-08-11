package flat.uxml;

import flat.exception.FlatException;
import flat.resources.ResourceStream;
import flat.uxml.sheet.UXSheetParser;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueBool;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
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

    public static UXNode parse(ResourceStream resourceStream) {
        Object obj = resourceStream.getCache();
        if (obj != null) {
            if (obj instanceof UXNode) {
                return (UXNode) obj;
            } else {
                throw new FlatException("Invalid UXNode at:" + resourceStream.getResourceName());
            }

        } else {
            try (InputStream inputStream = resourceStream.getStream()) {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(inputStream);
                doc.getDocumentElement().normalize();

                NodeList nList = doc.getChildNodes();
                List<UXNode> children = new ArrayList<>();
                for (int i = 0; i < nList.getLength(); i++) {
                    Node childNode = nList.item(i);
                    UXNode child = readRecursive(childNode);
                    if (child != null) {
                        children.add(child);
                    }
                }

                UXNode root;
                if (children.size() == 1) {
                    root = children.get(0);
                } else {
                    root = new UXNode("scene", "scene", null, children);
                }
                resourceStream.putCache(root);
                return root;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static UXNode readRecursive(Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }

        // Name
        String name = node.getNodeName();
        String style = name.toLowerCase();

        // Attributes
        HashMap<Integer, UXValue> values = new HashMap<>();
        NamedNodeMap nnm = node.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node item = nnm.item(i);
            String att = item.getNodeName();
            String value = item.getNodeValue();
            if (att.equals("style")) {
                style = value;
            } else {
                if (value == null) {
                    values.put(UXHash.getHash(att), new UXValueBool(true));
                } else {
                    values.put(UXHash.getHash(att), UXSheetParser.instance(value).parseXML());
                }
            }
        }

        // Children
        NodeList nList = node.getChildNodes();
        List<UXNode> children = new ArrayList<>();
        for (int i = 0; i < nList.getLength(); i++) {
            Node childNode = nList.item(i);
            UXNode child = readRecursive(childNode);
            if (child != null) {
                children.add(child);
            }
        }
        return new UXNode(name, style, values, children);
    }
}
