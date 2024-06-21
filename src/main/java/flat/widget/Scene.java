package flat.widget;

import flat.exception.FlatException;
import flat.graphics.SmartContext;
import flat.uxml.UXChildren;
import flat.widget.enuns.Visibility;
import flat.window.Activity;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.List;

public class Scene extends Parent {

    Activity activity;
    HashMap<String, Widget> idMap = new HashMap<>();

    public Scene() {
    }

    public void setActivity(Activity activity) {
        if (this.activity != activity) {
            Activity prev = this.activity;

            if (activity != null) {
                if (this.activity == null && activity.getScene() == this) {
                    this.activity = activity;
                    onActivityChange(null, activity);
                } else {
                    throw new FlatException("The activity cannot be assigned");
                }
            } else {
                if (this.activity.getScene() != this) {
                    this.activity = null;
                    onActivityChange(prev, null);
                } else {
                    throw new FlatException("The activity cannot be released");
                }
            }
        }
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

        for (Widget child : getChildrenIterable()) {
            child.onMeasure();
            if (child.getVisibility() == Visibility.GONE) continue;

            if (mWidth != MATCH_PARENT) {
                if (child.mWidth() == MATCH_PARENT) {
                    if (getPrefWidth() == WRAP_CONTENT)
                        mWidth = MATCH_PARENT;
                } else if (child.mWidth() > mWidth) {
                    mWidth = child.mWidth();
                }
            }
            if (mHeight != MATCH_PARENT) {
                if (child.mHeight() == MATCH_PARENT) {
                    if (getPrefHeight() == WRAP_CONTENT)
                        mHeight = MATCH_PARENT;
                } else if (child.mHeight() > mHeight) {
                    mHeight = child.mHeight();
                }
            }
        }
        mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);
        layoutHelperBox(getInX(), getInY(), getInWidth(), getInHeight());
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
    public void add(List<Widget> children) {
        super.add(children);
    }

    @Override
    public void invalidate(boolean layout) {
        if (getParent() == null) {
            if (activity != null) {
                activity.invalidate(layout);
            }
        } else {
            super.invalidate(layout);
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        if (getVisibility() == Visibility.VISIBLE) {
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
                Logger.info("Id override {}", id);
            }
        }
        if (!widget.isScene() && widget.children != null) {
            for (Widget child : widget.getChildrenIterable()) {
                assign(child);
            }
        }
    }

    final void unassign(Widget widget) {
        String id = widget.getId();
        if (id != null) {
            if (!idMap.remove(id, widget)) {
                Logger.info("Id {} not assigned", id);
            }
        }
        if (!widget.isScene() && widget.children != null) {
            for (Widget child : widget.getChildrenIterable()) {
                unassign(child);
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
                Logger.info("Id override {}", newID);
            }
        }
    }
}
