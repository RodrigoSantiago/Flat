package flat.widget.layout;

import flat.widget.Widget;

import java.util.ArrayList;
import java.util.Collections;

public class VBox extends Box {

    ArrayList<Widget> orderedList = new ArrayList<>();

    public VBox() {

    }

    @Override
    public void onLayout(float x, float y, float width, float height) {
        setLayout(x, y, Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
        ArrayList<Widget> children = getChildren();

        float yoff = 0, sum = 0;
        for (Widget child : children) {
            if (child.getVisibility() == GONE) continue;

            sum += Math.min(child.getMeasureHeight(), getHeight());
        }

        float mul = 1f;
        if (sum > getHeight()) {
            mul = getHeight() / sum;
        }

        float reaming = getHeight(), sum2 = 0;
        for (Widget child : children) {
            if (child.getVisibility() == GONE) continue;

            if (Math.min(child.getMeasureHeight(), getHeight()) * mul < Math.min(child.getMinHeight(), getHeight())) {
                reaming -= Math.min(child.getMinHeight(), getHeight());
            } else {
                sum2 += Math.min(child.getMeasureHeight(), getHeight());
            }
        }

        float mul2 = reaming / sum2;
        for (Widget child : children) {
            if (child.getVisibility() == GONE) continue;

            if (Math.min(child.getMeasureHeight(), getHeight()) * mul < Math.min(child.getMinHeight(), getHeight())) {
                child.onLayout(0, yoff, getWidth(), Math.min(child.getMinHeight(), getHeight()));
            } else {
                child.onLayout(0, yoff, getWidth(), Math.min(child.getMeasureHeight(), getHeight()) * mul2);
            }
            yoff += child.getHeight();
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
                } else {
                    mHeight += child.getMeasureHeight();
                }
            }
        }
        setMeasure(mWidth, mHeight);
    }

    public void add(Widget child) {
        orderedList.add(child);
        super.add(child);
    }

    public void add(Widget... children) {
        Collections.addAll(orderedList, children);
        super.add(children);
    }

    @Override
    public void childRemove(Widget widget) {
        orderedList.remove(widget);
        super.childRemove(widget);
    }
}
