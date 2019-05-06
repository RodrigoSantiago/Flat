package flat.widget;

import flat.graphics.SmartContext;
import flat.uxml.UXChildren;
import flat.widget.enuns.Visibility;

import java.util.HashMap;

public class Scene extends Parent {

    Activity activity;
    HashMap<String, Widget> idMap = new HashMap<>();

    public Scene() {

    }

    @Override
    public Activity getActivity() {
        if (activity != null) {
            return activity;
        } else {
            return super.getActivity();
        }
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        Gadget child;
        while ((child = children.next()) != null ) {
            Widget widget = child.getWidget();
            if (widget != null) {
                add(widget);
            }
        }
    }

    @Override
    public void onMeasure() {
        float mWidth = getPrefWidth(), mHeight = getPrefHeight();

        for (Widget child : getChildren()) {
            child.onMeasure();
            if (child.getVisibility() == Visibility.Gone) continue;

            if (mWidth != MATCH_PARENT) {
                if (child.getMeasureWidth() == MATCH_PARENT) {
                    if (getPrefWidth() == WRAP_CONTENT)
                        mWidth = MATCH_PARENT;
                } else if (child.getMeasureWidth() > mWidth) {
                    mWidth = child.getMeasureWidth();
                }
            }
            if (mHeight != MATCH_PARENT) {
                if (child.getMeasureHeight() == MATCH_PARENT) {
                    if (getPrefHeight() == WRAP_CONTENT)
                        mHeight = MATCH_PARENT;
                } else if (child.getMeasureHeight() > mHeight) {
                    mHeight = child.getMeasureHeight();
                }
            }
        }
        mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
        for (Widget child : getChildren()) {
            if (child.getVisibility() == Visibility.Gone) continue;

            child.onLayout(getWidth(), getHeight());
        }
    }

    @Override
    public void add(Widget child) {
        super.add(child);
    }

    @Override
    public void add(Widget... children) {
        super.add(children);
    }

    @Override
    public void invalidate(boolean layout) {
        if (activity != null) {
            activity.invalidate(layout);
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        if (getVisibility() == Visibility.Visible) {
            super.onDraw(context);
        }
    }

    @Override
    public Scene getScene() {
        if (parent != null) {
            if (parent.isScene()) {
                return (Scene) parent;
            } else {
                return parent.getScene();
            }
        } else {
            return this;
        }
    }

    @Override
    final boolean isScene() {
        return true;
    }

    @Override
    public Widget findById(String id) {
        return idMap.get(id);
    }

    final void assign(Widget widget) {
        String id = widget.getId();
        if (id != null) {
            Widget old = idMap.put(id, widget);
            if (old != null && old != widget) {
                System.out.println("ID Overflow");
            }
        }
        if (!widget.isScene() && widget.children != null) {
            for (Widget child : widget.children) {
                assign(child);
            }
        }
    }

    final void deassign(Widget widget) {
        String id = widget.getId();
        if (id != null) {
            if (!idMap.remove(id, widget)) {
                System.out.println("The id \'"+ id+"\' wasn't assigned");
            }
        }
        if (!widget.isScene() && widget.children != null) {
            for (Widget child : widget.children) {
                deassign(child);
            }
        }
    }

    final void reassign(String oldId, Widget widget) {
        if (idMap.get(oldId) == widget) {
            idMap.remove(oldId);
        }

        String newID = widget.getId();
        if (newID != null) {
            Widget old = idMap.put(newID, widget);
            if (old != null && old != widget) {
                System.out.println("ID Overflow");
            }
        }
    }
}
