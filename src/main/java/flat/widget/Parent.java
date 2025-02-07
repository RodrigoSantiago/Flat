package flat.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Parent extends Widget {

    public Parent() {
        children = new ArrayList<>();
        unmodifiableChildren = Collections.unmodifiableList(children);
    }

    @Override
    protected ArrayList<Widget> getChildren() {
        return super.getChildren();
    }

    // add child - children list unaltered
    protected void attachChildren(Widget child) {
        child.setParent(this);
    }

    // remove child - children list unaltered
    protected void detachChildren(Widget child) {
        if (child.parent == this) {
            child.setParent(null);
        }
    }

    protected void add(Widget child) {
        child.setParent(this);
        children.add(child);
        invalidateChildrenOrder(null);
        invalidate(true);
    }

    protected void add(Widget... children) {
        for (Widget child : children) {
            add(child);
        }
    }

    protected void add(List<Widget> children) {
        for (Widget child : children) {
            add(child);
        }
    }

    public void remove(Widget widget) {
        children.remove(widget);
        if (widget.parent == this) {
            widget.setParent(null);
        }
        invalidateChildrenOrder(null);
        invalidate(true);
    }

    public void removeAll() {
        List<Widget> children = getChildren();
        if (children != null) {
            int size;
            while ((size = children.size()) > 0) {
                remove(children.get(children.size() - 1));

                if (children.size() >= size) {
                    // UNEXPECTED ADDITION
                    break;
                }
            }
        }
    }
}