package flat.widget;

import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.text.Align;
import flat.uxml.UXChildren;
import flat.widget.dialogs.MenuItem;
import flat.widget.enuns.Visibility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Menu extends Scene {

    private Align.Horizontal halign = Align.Horizontal.LEFT;
    private ArrayList<Widget> orderedList;
    private MenuItem chooseItem;

    public Menu() {
        orderedList = new ArrayList<>();
        unmodifiableChildren = Collections.unmodifiableList(orderedList);
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);
        Gadget child;
        while ((child = children.next()) != null) {
            Widget widget = child.getWidget();
            if (widget != null) {
                add(widget);
            }
        }
    }

    @Override
    protected void onActivityChange(Activity prev, Activity activity) {
        super.onActivityChange(prev, activity);
        setChooseItem(null);
    }

    @Override
    public void onDraw(SmartContext context) {
        if (!MenuItem.desktop
                && chooseItem != null
                && chooseItem.getVisibility() == Visibility.Visible
                && chooseItem.isActivated()) {

            chooseItem.onDraw(context);
        } else {
            backgroundDraw(getBackgroundColor(), getBorderColor(), getRippleColor(), context);
            for (Widget child : getChildren()) {
                if (child.getVisibility() == Visibility.Visible) {
                    child.onDraw(context);
                }
            }
        }
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
        layoutHelperVertical(orderedList, getInX(), getInY(), getInWidth(), getInHeight(), halign);
    }

    @Override
    public void onMeasure() {
        final float offWidth = getPaddingLeft() + getPaddingRight();
        final float offHeight = getPaddingTop() + getPaddingBottom();
        float mWidth = Math.max(getPrefWidth(), Math.max(getMinWidth(), offWidth));
        float mHeight = Math.max(getPrefHeight(), Math.max(getMinHeight(), offHeight));

        float childrenWidth = 0, childrenMinWidth = 0;
        float childrenHeight = 0, childrenMinHeight = 0;
        for (Widget child : orderedList) {
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

    @Override
    public void add(Widget child) {
        this.orderedList.add(child);
        super.add(child);
    }

    @Override
    public void add(Widget... children) {
        for (Widget child : children) {
            add(child);
        }
    }

    @Override
    public void remove(Widget widget) {
        this.orderedList.remove(widget);
        super.remove(widget);
    }

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if (chooseItem != null && chooseItem.isActivated()) {
            Widget found = chooseItem.findByPosition(x, y, includeDisabled);
            if (found != null) return found;
        }

        if ((includeDisabled || isEnabled()) &&
                (getVisibility() == Visibility.Visible || getVisibility() == Visibility.Invisible)) {
            List<Widget> children = getChildren();
            if (children != null) {
                for (int i = children.size() - 1; i >= 0; i--) {
                    Widget child = children.get(i);
                    Widget found = child.findByPosition(x, y, includeDisabled);
                    if (found != null) return found;
                }
            }
            if (getParent() == null) {
                return this;
            } else {
                return isClickable() && contains(x, y) ? this : null;
            }
        } else {
            return getParent() == null ? this : null;
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

    public void show(Activity activity, float x, float y) {
        activity.showMenu(this, x, y);
    }

    public void hide() {
        if (activity != null) {
            activity.hideMenu(this);
        }
    }

    public MenuItem getChooseItem() {
        return chooseItem;
    }

    public void setChooseItem(MenuItem chooseItem) {
        if (this.chooseItem != chooseItem) {
            if (this.chooseItem != null) {
                this.chooseItem.setActivated(false);
            }

            if (chooseItem != null && chooseItem.isChildOf(this)) {
                this.chooseItem = chooseItem;
            } else {
                this.chooseItem = null;
            }

            if (this.chooseItem != null) {
                this.chooseItem.setActivated(true);
            }
            invalidate(true);
        }
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
