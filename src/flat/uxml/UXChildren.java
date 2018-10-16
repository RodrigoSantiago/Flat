package flat.uxml;

import flat.widget.Gadget;

import java.util.ArrayList;

public class UXChildren {

    private final ArrayList<Gadget> children = new ArrayList<>();
    private final UXLoader loader;
    private int pos;

    public UXChildren(UXLoader loader) {
        this.loader = loader;
    }

    public UXLoader getLoader() {
        return loader;
    }

    public void add(Gadget child) {
        children.add(child);
    }

    public Gadget next() {
        return pos >= children.size() ? null : children.get(pos++);
    }
}
