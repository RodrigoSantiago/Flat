package flat.uxml.sheet;

import flat.uxml.UXValue;

public class UXSheetAttribute {
    private String name;
    private UXValue value;

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
