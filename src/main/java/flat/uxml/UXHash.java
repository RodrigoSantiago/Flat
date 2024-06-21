package flat.uxml;

import java.util.HashMap;

public class UXHash {

    private static HashMap<String, Integer> hashes = new HashMap<>();

    public static Integer getHash(String property) {
        Integer val = hashes.get(property);
        if (val == null) {
            val = hashes.size();
            hashes.put(property, val);
        }
        return val;
    }
}
