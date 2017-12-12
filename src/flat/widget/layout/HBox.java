package flat.widget.layout;

import flat.widget.Widget;

import java.util.ArrayList;
import java.util.Collections;

public class HBox extends Box {

    ArrayList<Widget> orderedList = new ArrayList<>();

    public HBox() {

    }

    @Override
    public void onLayout(float x, float y, float width, float height) {
        setLayout(x, y, Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
        ArrayList<Widget> children = getChildren();

        float xoff = 0, sum = 0;
        for (Widget child : children) {
            if (child.getVisibility() == GONE) continue;

            sum += Math.min(child.getMeasureWidth(), getWidth());
        }

        float mul = 1f;
        if (sum > getWidth()) {
            mul = getWidth() / sum;
        }

        float reaming = getWidth(), sum2 = 0;
        for (Widget child : children) {
            if (child.getVisibility() == GONE) continue;

            if (Math.min(child.getMeasureWidth(), getWidth()) * mul < Math.min(child.getMinWidth(), getWidth())) {
                reaming -= Math.min(child.getMinWidth(), getWidth());
            } else {
                sum2 += Math.min(child.getMeasureWidth(), getWidth());
            }
        }

        float mul2 = reaming / sum2;
        for (Widget child : children) {
            if (child.getVisibility() == GONE) continue;

            if (Math.min(child.getMeasureWidth(), getWidth()) * mul < Math.min(child.getMinWidth(), getWidth())) {
                child.onLayout(xoff, 0, Math.min(child.getMinWidth(), getWidth()), getHeight());
            } else {
                child.onLayout(xoff, 0, Math.min(child.getMeasureWidth(), getWidth()) * mul2, getHeight());
            }
            xoff += child.getWidth();
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
                } else {
                    mWidth += child.getMeasureWidth();
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
