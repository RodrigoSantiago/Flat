package flat.uxml;

import flat.widget.Menu;
import flat.widget.Widget;

import java.util.ArrayList;

public class UXChildren {

    private final ArrayList<Widget> children = new ArrayList<>();
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

    public void add(Widget child) {
        children.add(child);
    }

    public Widget next() {
        return pos >= children.size() ? null : children.get(pos++);
    }

    public Menu nextMenu() {
        return posMenu >= menus.size() ? null : menus.get(posMenu++);
    }
}
