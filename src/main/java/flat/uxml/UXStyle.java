package flat.uxml;

import flat.exception.FlatException;
import flat.widget.State;
import org.tinylog.Logger;

import java.util.HashMap;

public class UXStyle {

    public final HashMap<Integer, UXValue[]> entries = new HashMap<>();
    public final String name;
    public final String parentName;
    public UXStyle parent;
    private byte statePresent;

    UXStyle(String name) {
        this(name, (String) null);
    }

    UXStyle(String name, String parent) {
        this.name = name;
        this.parentName = parent;
    }

    UXStyle(String name, UXStyle parent) {
        this.name = name;
        this.parent = parent;
        this.parentName = parent.name;
    }

    public UXValue get(String name, int index) {
        return get(UXHash.getHash(name))[index];
    }

    void add(Integer hash, State index, UXValue value) {
        UXValue[] values = entries.get(hash);
        if (values == null) {
            values = new UXValue[8];
            entries.put(hash, values);
        }
        values[index.ordinal()] = value;
        statePresent |= index.bitset();
    }

    UXValue[] get(Integer hash) {
        UXValue[] value = entries.get(hash);
        if (value == null && parent != null) {
            return parent.get(hash);
        }
        return value;
    }

    public boolean contains(String name) {
        return contains(UXHash.getHash(name));
    }

    boolean contains(Integer hash) {
        return entries.containsKey(hash);
    }

    public boolean containsChange(byte stateA, byte stateB) {
        for (int i = 0; i < 8; i++) {
            if (((stateA & (1 << i)) != 0) != ((stateB & (1 << i)) != 0)) {
                if ((statePresent & (1 << i)) != 0) {
                    return true;
                }
            }
        }
        return parent != null && parent.containsChange(stateA, stateB);
    }

    void setParent(UXStyle uxStyle) {
        if (isChildOf(uxStyle)) {
            Logger.error("Cyclic reference at style <" + name + "> and <" + uxStyle.name + ">");
        } else {
            this.parent = uxStyle;
        }
    }

    private boolean isChildOf(UXStyle style) {
        if (this == style) return true;
        if (parent != null) return parent.isChildOf(style);
        return false;
    }
}
