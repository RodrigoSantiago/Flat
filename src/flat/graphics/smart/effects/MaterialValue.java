package flat.graphics.smart.effects;

import java.io.Serializable;

public class MaterialValue implements Serializable {
    public final String name;
    public final Serializable value;

    public MaterialValue(String name, Serializable value) {
        this.name = name;
        this.value = value;
    }

    @Override
    protected MaterialValue clone() {
        return new MaterialValue(name, value);
    }
}
