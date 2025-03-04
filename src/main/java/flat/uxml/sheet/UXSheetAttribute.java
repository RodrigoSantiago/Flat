package flat.uxml.sheet;

import flat.uxml.value.UXValue;

public class UXSheetAttribute {
    private final String name;
    private final UXValue value;

    public UXSheetAttribute(String name, UXValue value) {
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
