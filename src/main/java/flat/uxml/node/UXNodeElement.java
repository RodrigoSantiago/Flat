package flat.uxml.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UXNodeElement {
    private final String name;
    private final HashMap<String, UXNodeAttribute> attributes = new HashMap<>();
    private final List<UXNodeElement> children = new ArrayList<>();
    private final UXNodeElement parent;
    private String content;

    public UXNodeElement(String name, UXNodeElement parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public UXNodeElement getParent() {
        return parent;
    }

    public HashMap<String, UXNodeAttribute> getAttributes() {
        return attributes;
    }

    public List<UXNodeElement> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "Element :" + name;
    }
}
