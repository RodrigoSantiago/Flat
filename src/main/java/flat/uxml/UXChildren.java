package flat.uxml;

import flat.widget.Gadget;
import flat.widget.Menu;

import java.util.ArrayList;

public class UXChildren {

    private final ArrayList<Gadget> children = new ArrayList<>();
    private final ArrayList<Menu> menus = new ArrayList<>();
    private final UXBuilder loader;
    private int pos;
    private int posMenu;

    public UXChildren(UXBuilder loader) {
        this.loader = loader;
    }

    public UXBuilder getLoader() {
        return loader;
    }

    public void addMenu(Menu menu) {
        this.menus.add(menu);
    }

    public void add(Gadget child) {
        children.add(child);
    }

    public Gadget next() {
        return pos >= children.size() ? null : children.get(pos++);
    }

    public Menu nextMenu() {
        return posMenu >= menus.size() ? null : menus.get(posMenu++);
    }
}
