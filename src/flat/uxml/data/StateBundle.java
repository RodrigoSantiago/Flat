package flat.uxml.data;

import java.io.Serializable;
import java.util.HashMap;

public class StateBundle implements Serializable {

    public final HashMap<String, Serializable> map = new HashMap<>();

    public void put(String name, Serializable value) {
        map.put(name, value);
    }

    public <T extends Serializable> T get(String name) {
        return get(name, null);
    }

    public <T extends Serializable> T get(String name, T defaultValue) {
        T value = (T) map.get(name);
        return value == null ? defaultValue : value;
    }
}
