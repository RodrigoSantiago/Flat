package flat.widget.layout;

import flat.uxml.*;
import flat.widget.Gadget;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enuns.Visibility;

import java.util.Collections;

public class Box extends Parent {

    public Box() {

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
        final float offWidth = getPaddingLeft() + getPaddingRight();
        final float offHeight = getPaddingTop() + getPaddingBottom();
        float mWidth = Math.max(getPrefWidth(), Math.max(getMinWidth(), offWidth));
        float mHeight = Math.max(getPrefHeight(), Math.max(getMinHeight(), offHeight));

        float childrenWidth = 0, childrenMinWidth = 0;
        float childrenHeight = 0, childrenMinHeight = 0;
        for (Widget child : getChildren()) {
            child.onMeasure();
            if (child.getVisibility() == Visibility.Gone) continue;


            if (child.getMeasureWidth() > childrenWidth) {
                childrenWidth = child.getMeasureWidth();
            }
            if (child.getLayoutMinWidth() > childrenMinWidth) {
                childrenMinWidth = child.getLayoutMinWidth();
            }
            if (child.getMeasureHeight() > childrenHeight) {
                childrenHeight = child.getMeasureHeight();
            }
            if (child.getLayoutMinHeight() > childrenMinHeight) {
                childrenMinHeight = child.getLayoutMinHeight();
            }
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
    public void onLayout(float width, float height) {
        setLayout(Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
        layoutHelperBox(getChildren(), getInX(), getInY(), getInWidth(), getInHeight());
    }

    @Override
    public void add(Widget child) {
        super.add(child);
    }

    @Override
    public void add(Widget... children) {
        super.add(children);
    }
}
