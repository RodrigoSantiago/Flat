package flat.uxml;

import flat.uxml.value.UXValue;
import flat.widget.stages.Menu;
import flat.widget.Widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class UXChildren implements Iterable<UXChild> {

    private final ArrayList<UXChild> children = new ArrayList<>();
    private final UXBuilder loader;
    private Menu menu;

    public UXChildren(UXBuilder loader) {
        this.loader = loader;
    }

    public UXBuilder getLoader() {
        return loader;
    }

    public void add(Widget child, HashMap<Integer, UXValue> attributes) {
        children.add(new UXChild(child, attributes));
    }

    public int getChildrenCount() {
        return children.size();
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public Menu getMenu() {
        return menu;
    }

    @Override
    public Iterator<UXChild> iterator() {
        return new UXChildIterator();
    }

    private class UXChildIterator implements Iterator<UXChild> {
        private int pos;

        @Override
        public boolean hasNext() {
            return pos < children.size();
        }

        @Override
        public UXChild next() {
            return pos >= children.size() ? null : children.get(pos++);
        }
    }
}
