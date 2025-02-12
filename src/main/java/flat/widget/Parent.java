package flat.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Parent extends Widget {

    protected Parent() {
        children = new ArrayList<>();
        unmodifiableChildren = Collections.unmodifiableList(children);
    }

    @Override
    protected ArrayList<Widget> getChildren() {
        return super.getChildren();
    }

    protected boolean attachChild(Widget child) {
        if (child == this || this.isChildOf(child)) {
            return false;
        }
        if (child.getParent() != null && !child.getParent().detachChild(child)) {
            return false;
        }
        children.add(child);
        invalidateChildrenOrder(null);
        invalidate(true);
        return true;
    }

    protected boolean attachAndAddChild(Widget child) {
        if (attachChild(child)) {
            child.setParent(this);
            return true;
        }
        return false;
    }

    protected boolean detachChild(Widget child) {
        children.remove(child);
        invalidateChildrenOrder(null);
        invalidate(true);
        return true;
    }

    protected boolean detachAndRemoveChild(Widget widget) {
        if (detachChild(widget)) {
            widget.setParent(null);
            return true;
        }
        return false;
    }

    protected void add(Widget child) {
        attachAndAddChild(child);
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
        detachAndRemoveChild(widget);
    }

    public void removeAll() {
        List<Widget> children = new ArrayList<>(getChildren());
        for (Widget child : children) {
            remove(child);
        }
    }
}