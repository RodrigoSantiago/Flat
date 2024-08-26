package flat.uxml.node;

import flat.uxml.value.UXValue;

public class UXNodeAttribute {
    private final String name;
    private final UXValue value;

    public UXNodeAttribute(String name, UXValue value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public UXValue getValue() {
        return value;
    }
}
