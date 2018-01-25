package flat.uxml;

import flat.widget.Widget;

import java.util.ArrayList;

public class UXChildren {

    private final ArrayList<Widget> children = new ArrayList<>();
    private final UXLoader loader;
    private int pos;

    public UXChildren(UXLoader loader) {
        this.loader = loader;
    }

    public UXLoader getLoader() {
        return loader;
    }

    public void add(Widget child) {
        children.add(child);
    }

    public Widget next() {
        return pos >= children.size() ? null : children.get(pos++);
    }

    public void logUnusedChildren() {
        if (pos < children.size()) {
            StringBuilder sb = new StringBuilder();
            for (int i = pos; i < children.size(); i++) {
                if (i > pos) sb.append(", ");
                sb.append(children.get(i));
            }
            loader.log("Unused children : [" + sb.toString() + "]");
        }
    }
}
