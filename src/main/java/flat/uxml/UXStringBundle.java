package flat.uxml;

import flat.exception.FlatException;
import flat.resources.ResourceStream;
import flat.uxml.node.UXNodeAttribute;
import flat.uxml.node.UXNodeElement;
import flat.uxml.node.UXNodeParser;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

public class UXStringBundle {

    public static UXStringBundle parse(ResourceStream stream) {
        Object cache = stream.getCache();
        if (cache != null) {
            if (cache instanceof Exception) {
                return null;
            } else if (cache instanceof UXStringBundle) {
                return (UXStringBundle) cache;
            } else {
                throw new FlatException("Invalid UXStringBundle at:" + stream.getResourceName());
            }
        }

        try {
            UXStringBundle node = read(stream);
            if (node != null) {
                stream.putCache(node);
            }
            return node;
        } catch (Exception e) {
            stream.putCache(e);
            throw new FlatException(e);
        }
    }

    private static UXStringBundle read(ResourceStream stream) {
        byte[] data = stream.readData();
        if (data == null) {
            throw new FlatException("File not found at: " + stream.getResourceName());
        }

        String xml = new String(data, StandardCharsets.UTF_8);
        UXNodeParser reader = new UXNodeParser(xml);
        reader.parse();

        UXNodeElement root = reader.getRootElement();
        if (root != null && "Bundle".equals(root.getName())) {
            HashMap<String, String> map = new HashMap<>();
            for (var child : root.getChildren()) {
                readRecursive(map, null, child);
            }
            return new UXStringBundle(map);
        }
        return null;
    }

    private static void readRecursive(HashMap<String, String> map, String rootName, UXNodeElement element) {
        // Name
        String name = element.getName();
        if (!"String".equals(name)) {
            return;
        }

        String attKey = "";
        String attValue = "";

        // Attributes
        HashMap<String, UXNodeAttribute> attrs = element.getAttributes();
        if (element.getContent() != null) {
            attValue = element.getContent();
        }
        for (var item : attrs.values()) {
            String att = item.getName();
            if ("name".equals(att)) {
                attKey = item.getValue().asString(null);
                break;
            }
        }
        if (attKey == null) {
            return;
        }

        if (rootName != null) {
            attKey = rootName + "." + attKey;
        }

        // Children
        List<UXNodeElement> children = element.getChildren();
        if (!children.isEmpty()) {
            for (UXNodeElement child : children) {
                readRecursive(map, attKey, child);
            }
        } else {
            map.put(attKey, attValue);
        }
    }

    private HashMap<String, String> values;

    public UXStringBundle(HashMap<String, String> values) {
        this.values = values;
    }

    public String get(String key) {
        return values.get(key);
    }

    public String get(String key, String defaultValue) {
        String value = values.get(key);
        return value == null ? defaultValue : value;
    }
}
