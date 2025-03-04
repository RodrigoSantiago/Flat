package flat.uxml;

import java.util.HashMap;
import java.util.Objects;

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

    public static String findByHash(Integer hash) {
        for (var entry : hashes.entrySet()) {
            if (Objects.equals(entry.getValue(), hash)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
