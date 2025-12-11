package flat.uxml;

import flat.uxml.value.UXValue;
import flat.widget.State;

import java.util.HashMap;

public class UXStyle {

    private final HashMap<Integer, UXValue[]> entries = new HashMap<>();
    private final String name;
    private final String parentName;
    private UXStyle parent;
    private UXValue flow;
    private byte statePresent;

    UXStyle(String name) {
        this(name, null, null);
    }

    UXStyle(String name, String parent, UXValue flow) {
        this.name = name;
        this.parentName = parent;
        this.flow = flow;
    }

    UXStyle(String name, UXStyle parent) {
        this.name = name;
        this.setParent(parent);
        this.parentName = parent.getName();
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
        if (value == null && getParent() != null) {
            return getParent().get(hash);
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
        return getParent() != null && getParent().containsChange(stateA, stateB);
    }

    boolean setParent(UXStyle uxStyle) {
        if (!uxStyle.isChildOf(this)) {
            this.parent = uxStyle;
            return true;
        } else {
            return false;
        }
    }

    public UXStyle getParent() {
        return parent;
    }
    
    public UXValue getFlow() {
        return flow;
    }
    
    private boolean isChildOf(UXStyle style) {
        if (this == style) return true;
        if (parent != null) return parent.isChildOf(style);
        return false;
    }

    public String getName() {
        return name;
    }

    public String getParentName() {
        return parentName;
    }

    HashMap<Integer, UXValue[]> getEntries() {
        return entries;
    }
}
