package flat.graphics.context;

import flat.graphics.context.enums.AttributeType;

public class Attribute {

    private final int location;
    private final String name;
    private final AttributeType type;
    private final int arraySize;

    Attribute(int location, String name, AttributeType type, int arraySize) {
        this.location = location;
        this.name = name;
        this.type = type;
        this.arraySize = arraySize;
    }

    public int getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public AttributeType getType() {
        return type;
    }

    public int getArraySize() {
        return arraySize;
    }
}
