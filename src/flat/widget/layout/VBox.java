package flat.widget.layout;

import flat.animations.StateInfo;
import flat.graphics.text.Align;
import flat.widget.Widget;
import flat.widget.enuns.Visibility;

import java.util.ArrayList;
import java.util.Collections;

public class VBox extends Box {

    // todo - Vertical ordering
    private Align.Horizontal align = Align.Horizontal.LEFT;

    ArrayList<Widget> orderedList = new ArrayList<>();

    @Override
    public void applyStyle() {
        super.applyStyle();

        StateInfo info = getStateInfo();

        setAlign(getStyle().asConstant("align", info, Align.Horizontal.LEFT));
    }

    @Override
    public void onLayout(float x, float y, float width, float height) {
        setLayout(x, y, Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
        ArrayList<Widget> children = orderedList;

        float yoff = Math.min(getHeight(), getPaddingTop() + getMarginTop()), sum = 0;
        for (Widget child : children) {
            if (child.getVisibility() == Visibility.Gone) continue;

            sum += Math.min(child.getMeasureHeight(), getHeight());
        }

        float mul = 1f;
        if (sum > getHeight()) {
            mul = getHeight() / sum;
        }

        float w = Math.max(0, getWidth() - getPaddingLeft() - getPaddingRight() - getMarginLeft() - getMarginRight());
        float h = Math.max(0, getHeight() - getPaddingTop() - getPaddingBottom() - getMarginTop() - getMarginBottom());
        float reaming = h, sum2 = 0;
        for (Widget child : children) {
            if (child.getVisibility() == Visibility.Gone) continue;

            if (Math.min(child.getMeasureHeight(), h) * mul < Math.min(child.getLayoutMinHeight(), h)) {
                reaming -= Math.min(child.getLayoutMinHeight(), h);
            } else {
                sum2 += Math.min(child.getMeasureHeight(), h);
            }
        }

        float mul2 = reaming / sum2;
        for (Widget child : children) {
            if (child.getVisibility() == Visibility.Gone) continue;

            float childH;
            if (Math.min(child.getMeasureHeight(), h) * mul < Math.min(child.getLayoutMinHeight(), h)) {
                childH = Math.min(child.getLayoutMinHeight(), h);
            } else {
                childH = Math.min(child.getMeasureHeight(), h) * mul2;
            }
            child.onLayout(xOff(childH), yoff, w, childH);
            yoff += child.getHeight();
        }
    }

    @Override
    public void onMeasure() {
        float mWidth = getPrefWidth(), mHeight = getPrefHeight();

        for (Widget child : getChildren()) {
            child.onMeasure();
            if (child.getVisibility() == Visibility.Gone) continue;

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
                } else {
                    mHeight += child.getMeasureHeight();
                }
            }
        }
        mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
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

    public Align.Horizontal getAlign() {
        return align;
    }

    public void setAlign(Align.Horizontal align) {
        this.align = align;
    }

    private float xOff(float childWidth) {
        float start = getPaddingLeft() + getMarginLeft();
        float end = getWidth() - getPaddingRight() - getMarginRight();
        if (end < start) return (start + end) / 2f;
        if (align == Align.Horizontal.RIGHT) return end - childWidth;
        if (align == Align.Horizontal.CENTER) return (start + end - childWidth) / 2f;
        return start;
    }
}
