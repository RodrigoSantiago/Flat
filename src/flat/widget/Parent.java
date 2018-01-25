package flat.widget;

import java.util.ArrayList;
import java.util.Collections;

public abstract class Parent extends Widget {

    public Parent() {
        children = new ArrayList<>();
        unmodifiableChildren = Collections.unmodifiableList(children);
    }

    @Override
    protected ArrayList<Widget> getChildren() {
        return super.getChildren();
    }

    protected final void childAttach(Widget child) {
        child.setParent(this);
        invalidateChildrenOrder();
        invalidate(true);
    }

    protected final void childDetach(Widget child) {
        if (child.parent == this) {
            child.setParent(null);
            invalidateChildrenOrder();
            invalidate(true);
        }
    }

    public void childRemove(Widget widget) {
        children.remove(widget);
        childDetach(widget);
        invalidateChildrenOrder();
        invalidate(true);
    }
}