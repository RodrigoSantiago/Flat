package flat.uxml.data;

import java.io.Serializable;
import java.util.HashMap;

public class StringBundle implements Serializable {

    private HashMap<String,String> values = new HashMap<>();

    public void put(String key, String value) {
        values.put(key, value);
    }

    public String get(String key) {
        return values.get(key);
    }

    public String get(String key, String defaultValue) {
        String value = values.get(key);
        return value == null ? defaultValue : value;
    }
}
