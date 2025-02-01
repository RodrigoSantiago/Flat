package flat.widget;

import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.text.Align;
import flat.uxml.UXChildren;
import flat.widget.enums.Visibility;
import flat.window.Activity;

import java.util.ArrayList;
import java.util.Objects;

public class Menu extends Scene {

    private Align.Horizontal halign = Align.Horizontal.LEFT;
    private ArrayList<Widget> orderedList;
    //private MenuItem chooseItem;

    public Menu() {
        orderedList = new ArrayList<>();
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
        //setChooseItem(null);
    }

    @Override
    public void onDraw(SmartContext context) {
        /*if (!MenuItem.desktop
                && chooseItem != null
                && chooseItem.getVisibility() == Visibility.VISIBLE
                && chooseItem.isActivated()) {

            chooseItem.onDraw(context);
        } else {
            backgroundDraw(context, getBackgroundColor(), getBorderColor(), getRippleColor());
            for (Widget child : getChildrenIterable()) {
                if (child.getVisibility() == Visibility.VISIBLE) {
                    child.onDraw(context);
                }
            }
        }*/
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
        layoutHelperVertical(new Children(orderedList), getInX(), getInY(), getInWidth(), getInHeight(), halign);
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
            if (child.getVisibility() == Visibility.GONE) continue;

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
       /* if (chooseItem != null && chooseItem.isActivated()) {
            Widget found = chooseItem.findByPosition(x, y, includeDisabled);
            if (found != null) return found;
        }*/

        if ((includeDisabled || isEnabled()) &&
                (getVisibility() == Visibility.VISIBLE || getVisibility() == Visibility.INVISIBLE)) {
            if (getChildren() != null) {
                for (Widget child : getChildrenIterableReverse()) {
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
            return Objects.equals(getId(), id) ? this : null;
        } else {
            return super.findById(id);
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);

        if (pointerEvent.getSource() == this && pointerEvent.getType() == PointerEvent.RELEASED) {
            hide();
        }
    }

    public void show(Activity activity, float x, float y) {
        if (activity.getScene() != null) {
            activity.getScene().add(this);
        }
    }

    public void hide() {
        if (getParent() != null) {
            getParent().remove(this);
        }
    }

    /*
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
*/

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
