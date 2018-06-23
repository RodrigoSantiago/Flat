package flat.widget;

import flat.graphics.SmartContext;
import flat.uxml.Controller;
import flat.uxml.UXAttributes;
import flat.uxml.UXChildren;

import java.util.Collections;
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
            Scene scene = getScene();
            if (scene != null) {
                return scene.getActivity();
            } else {
                return null;
            }
        }
    }

    @Override
    public void applyAttributes(Controller controller, UXAttributes attributes) {
        super.applyAttributes(controller, attributes);
        setPrefWidth(attributes.asSize("width", MATCH_PARENT));
        setPrefHeight(attributes.asSize("height", MATCH_PARENT));
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);
        Widget child;
        while ((child = children.next()) != null ) {
            add(child);
        }
    }

    @Override
    public void onLayout(float x, float y, float width, float height) {
        setLayout(x, y, Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
        for (Widget child : getChildren()) {
            if (child.getVisibility() == GONE) continue;

            child.onLayout(child.getX(), child.getY(), getWidth(), getHeight());
        }
    }

    @Override
    public void onMeasure() {
        float mWidth = getPrefWidth(), mHeight = getPrefHeight();

        for (Widget child : getChildren()) {
            child.onMeasure();
            if (child.getVisibility() == GONE) continue;

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

    public void add(Widget child) {
        childAttach(child);
        getChildren().add(child);
    }

    public void add(Widget... children) {
        for (Widget child : children) {
            childAttach(child);
        }
        Collections.addAll(getChildren(), children);
    }

    @Override
    public void invalidate(boolean layout) {
        if (activity != null) {
            activity.invalidate(layout);
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        if (getVisibility() == VISIBLE) {
            super.onDraw(context);
        }
    }

    @Override
    public Scene getScene() {
        Scene scene;
        if (parent != null) {
            if (parent.isScene()) {
                scene = (Scene) parent;
            } else {
                scene = parent.getScene();
            }
        } else {
            scene = this;
        }
        return scene;
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
