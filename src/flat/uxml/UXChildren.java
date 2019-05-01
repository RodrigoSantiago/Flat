package flat.uxml;

import flat.widget.Gadget;
import flat.widget.Menu;

import java.util.ArrayList;

public class UXChildren {

    private final ArrayList<Gadget> children = new ArrayList<>();
    private final UXLoader loader;
    private int pos;
    private Menu contextMenu;

    public UXChildren(UXLoader loader) {
        this.loader = loader;
    }

    public UXLoader getLoader() {
        return loader;
    }

    public void setContextMenu(Menu contextMenu) {
        this.contextMenu = contextMenu;
    }

    public Menu getContextMenu() {
        return contextMenu;
    }

    public void add(Gadget child) {
        children.add(child);
    }

    public Gadget next() {
        return pos >= children.size() ? null : children.get(pos++);
    }
}
