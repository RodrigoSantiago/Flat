package flat.widget.containers;

import flat.widget.Parent;
import flat.widget.Widget;

import java.util.Collections;

public class Box extends Parent {

    public Box() {

    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
        for (Widget child : getChildren()) {
            if (child.getVisibility() != GONE) {
                child.onLayout(getWidth(), getHeight());
            } else {
                child.onLayout(0, 0);
            }
        }
    }

    @Override
    public void onMeasure() {
        float mWidth = getPrefWidth(), mHeight = getPrefHeight();

        for (Widget child : getChildren()) {
            child.onMeasure();
            if (child.getVisibility() == GONE) continue;

            if (mWidth != MATH_PARENT) {
                if (child.getMeasureWidth() == MATH_PARENT) {
                    if (getPrefWidth() == WRAP_CONTENT)
                        mWidth = MATH_PARENT;
                } else if (child.getMeasureWidth() > mWidth) {
                    mWidth = child.getMeasureWidth();
                }
            }
            if (mHeight != MATH_PARENT) {
                if (child.getMeasureHeight() == MATH_PARENT) {
                    if (getPrefHeight() == WRAP_CONTENT)
                        mHeight = MATH_PARENT;
                } else if (child.getMeasureHeight() > mHeight) {
                    mHeight = child.getMeasureHeight();
                }
            }
        }
        setMeasure(mWidth, mHeight);
    }

    public void add(Widget child) {
        childAttach(child);
        getChildren().add(child);
        invalidateChildrenOrder();
    }

    public void add(Widget... children) {
        for (Widget child : children) {
            childAttach(child);
        }
        Collections.addAll(getChildren(), children);
        invalidateChildrenOrder();
    }
}
