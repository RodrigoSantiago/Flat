package flat.widget.layout;

import flat.widget.Parent;
import flat.widget.Widget;

import java.util.Collections;

public class Box extends Parent {

    public Box() {

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
}
