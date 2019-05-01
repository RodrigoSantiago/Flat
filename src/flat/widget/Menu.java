package flat.widget;

import flat.events.PointerEvent;
import flat.graphics.text.Align;
import flat.uxml.UXChildren;
import flat.widget.enuns.Visibility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Menu extends Parent {

    private Align.Horizontal halign = Align.Horizontal.LEFT;
    private ArrayList<MenuItem> children;
    private List<MenuItem> unmodifiableChildren;

    Activity activity;

    public Menu() {
        children = new ArrayList<>();
        unmodifiableChildren = Collections.unmodifiableList(children);
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);
        Gadget child;
        while ((child = children.next()) != null ) {
            Widget widget = child.getWidget();
            if (widget instanceof MenuItem) {
                add((MenuItem) widget);
            }
        }
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
        layoutHelperVertical(children, getInX(), getInY(), getInWidth(), getInHeight(), halign);
    }

    @Override
    public void onMeasure() {
        final float offWidth = getPaddingLeft() + getPaddingRight();
        final float offHeight = getPaddingTop() + getPaddingBottom();
        float mWidth = Math.max(getPrefWidth(), Math.max(getMinWidth(), offWidth));
        float mHeight = Math.max(getPrefHeight(), Math.max(getMinHeight(), offHeight));

        float childrenWidth = 0, childrenMinWidth = 0;
        float childrenHeight = 0, childrenMinHeight = 0;
        for (Widget child : children) {
            child.onMeasure();
            if (child.getVisibility() == Visibility.Gone) continue;

            if (child.getMeasureWidth() > childrenWidth) {
                childrenWidth = child.getMeasureWidth();
            }
            if (child.getLayoutMinWidth() > childrenMinWidth) {
                childrenMinWidth += child.getLayoutMinWidth();
            }
            childrenHeight += child.getMeasureHeight();
            childrenMinHeight += child.getLayoutMinHeight();
        }
        if (getPrefWidth() == WRAP_CONTENT) {
            mWidth = childrenWidth + offWidth;
        } else if (mWidth < childrenMinWidth + offWidth) {
            mWidth = childrenMinWidth + offWidth;
        }
        if (getPrefHeight() == WRAP_CONTENT) {
            mHeight = childrenHeight + offHeight;
        } else if (mHeight < childrenMinHeight + offHeight) {
            mHeight = childrenMinHeight + offHeight;
        }

        setMeasure(mWidth + getMarginLeft() + getMarginRight(), mHeight + getMarginTop() + getMarginBottom());
    }

    public void add(MenuItem child) {
        attachChildren(child);
        getChildren().add(child);

        this.children.add(child);
        child.parentMenu = this;

        invalidateChildrenOrder();
        invalidate(true);
    }

    public void add(MenuItem... children) {
        for (MenuItem child : children) {
            add(child);
        }
    }

    @Override
    public void remove(Widget widget) {
        if (widget != null && widget.getParent() == this) {
            ((MenuItem)widget).parentMenu = null;
            children.remove(widget);

            getChildren().remove(widget);
            detachChildren(widget);
            invalidateChildrenOrder();
            invalidate(true);
        }
    }

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if ((includeDisabled || isEnabled()) &&
                (getVisibility() == Visibility.Visible || getVisibility() == Visibility.Invisible)) {
            for (int i = children.size() - 1; i >= 0; i--) {
                Widget child = children.get(i);
                Widget found = child.findByPosition(x, y, includeDisabled);
                if (found != null) return found;
            }
            return this;
        } else {
            return null;
        }
    }

    @Override
    public Widget findById(String id) {
        if (getParent() == null) {
            if (activity != null) {
                return activity.findById(id);
            } else {
                return Objects.equals(getId(), id) ? this : null;
            }
        } else {
            return super.findById(id);
        }
    }

    @Override
    public Activity getActivity() {
        if (getParent() == null) {
            return activity;
        } else {
            return super.getActivity();
        }
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
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);

        if (pointerEvent.getSource() == this && pointerEvent.getType() == PointerEvent.RELEASED) {
            if (activity != null) {
                activity.hideMenu(this);
            }
        }
    }

    public List<MenuItem> getItens() {
        return unmodifiableChildren;
    }

    public Align.Horizontal getHorizontalAlign() {
        return halign;
    }

    public void setHorizontalAlign(Align.Horizontal halign) {
        if (this.halign != halign) {
            this.halign = halign;
            invalidate(true);
        }
    }
}
