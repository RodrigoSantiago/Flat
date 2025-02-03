package flat.widget.layout;

import flat.uxml.UXChildren;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enums.Visibility;

import java.util.List;

public class Box extends Parent {

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        Widget widget;
        while ((widget = children.next()) != null ) {
            add(widget);
        }
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        
        float mWidth = 0;
        float mHeight = 0;
        boolean wrapWidth = getPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getPrefHeight() == WRAP_CONTENT;

        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE) continue;

            child.onMeasure();
            if (wrapWidth) {
                if (child.getMeasureWidth() == MATCH_PARENT) {
                    float mW = Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth());
                    if (mW > mWidth) {
                        mWidth = mW;
                    }
                } else if (child.getMeasureWidth() > mWidth) {
                    mWidth = child.getMeasureWidth();
                }
            }
            if (wrapHeight) {
                if (child.getMeasureHeight() == MATCH_PARENT) {
                    float mH = Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight());
                    if (mH > mHeight) {
                        mHeight = mH;
                    }
                } else if (child.getMeasureHeight() > mHeight) {
                    mHeight = child.getMeasureHeight();
                }
            }
        }
        
        if (wrapWidth) {
            mWidth = Math.max(mWidth + extraWidth, Math.max(getPrefWidth(), getLayoutMinWidth()));
        } else {
            mWidth = Math.max(getPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(mHeight + extraHeight, Math.max(getPrefHeight(), getLayoutMinHeight()));
        } else {
            mHeight = Math.max(getPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);
        float lWidth = Math.max(0, getWidth()
                - getMarginLeft() - getMarginRight() - getPaddingLeft() - getPaddingRight());
        float lHeight = Math.max(0, getHeight()
                - getMarginTop() - getMarginBottom() - getPaddingTop() - getPaddingBottom());

        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE) continue;

            float childWidth;
            if (child.getMeasureWidth() == MATCH_PARENT) {
                childWidth = Math.min(Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth()), lWidth);
            } else {
                childWidth = Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth());
            }

            float childHeight;
            if (child.getMeasureHeight() == MATCH_PARENT) {
                childHeight = Math.min(Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight()), lHeight);
            } else {
                childHeight = Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight());
            }

            child.onLayout(childWidth, childHeight);
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
    public void add(List<Widget> children) {
        super.add(children);
    }
}
