package flat.widget.layout;

import flat.animations.StateInfo;
import flat.graphics.text.Align;
import flat.widget.Widget;
import flat.widget.enuns.Direction;
import flat.widget.enuns.Visibility;

import java.util.ArrayList;
import java.util.Collections;

public class LinearBox extends Box {

    private Direction direction = Direction.HORIZONTAL;
    private Align.Vertical valign = Align.Vertical.TOP;
    private Align.Horizontal halign = Align.Horizontal.LEFT;

    ArrayList<Widget> orderedList = new ArrayList<>();

    @Override
    public void applyStyle() {
        super.applyStyle();
        if (getStyle() == null) return;

        StateInfo info = getStateInfo();

        setDirection(getStyle().asConstant("direction", info, getDirection()));
        setVerticalAlign(getStyle().asConstant("v-align", info, getVerticalAlign()));
        setHorizontalAlign(getStyle().asConstant("h-align", info, getHorizontalAlign()));
    }

    private void hLayout(float width, float height) {
        setLayout(Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
        ArrayList<Widget> children = orderedList;
        boolean inverse = direction == Direction.IHORIZONTAL;
        int size = children.size() - 1;

        float xoff = Math.min(getWidth(), getPaddingLeft() + getMarginLeft()), sum = 0;
        for (int i = 0; i < children.size(); i++) {
            Widget child = children.get(inverse ? size - i : i);
            if (child.getVisibility() == Visibility.Gone) continue;

            sum += Math.min(child.getMeasureWidth(), getWidth());
        }

        float mul = 1f;
        if (sum > getWidth()) {
            mul = getWidth() / sum;
        }

        float w = Math.max(0, getWidth() - getPaddingLeft() - getPaddingRight() - getMarginLeft() - getMarginRight());
        float h = Math.max(0, getHeight() - getPaddingTop() - getPaddingBottom() - getMarginTop() - getMarginBottom());
        float reaming = w, sum2 = 0;
        for (int i = 0; i < children.size(); i++) {
            Widget child = children.get(inverse ? size - i : i);
            if (child.getVisibility() == Visibility.Gone) continue;

            if (Math.min(child.getMeasureWidth(), w) * mul < Math.min(child.getLayoutMinWidth(), w)) {
                reaming -= Math.min(child.getLayoutMinWidth(), w);
            } else {
                sum2 += Math.min(child.getMeasureWidth(), w);
            }
        }

        float mul2 = reaming / sum2;
        for (int i = 0; i < children.size(); i++) {
            Widget child = children.get(inverse ? size - i : i);
            if (child.getVisibility() == Visibility.Gone) continue;

            float childW;
            if (Math.min(child.getMeasureWidth(), w) * mul < Math.min(child.getLayoutMinWidth(), w)) {
                childW = Math.min(child.getLayoutMinWidth(), w);
            } else {
                childW = Math.min(child.getMeasureWidth(), w) * mul2;
            }
            child.onLayout(childW, h);
            child.setPosition(xoff, yOff(child.getHeight()));
            xoff += child.getWidth();
        }
    }

    private void hMeasure() {
        float mWidth = getPrefWidth(), mHeight = getPrefHeight();

        ArrayList<Widget> children = orderedList;
        boolean inverse = direction == Direction.IHORIZONTAL;
        int size = children.size() - 1;

        for (int i = 0; i < children.size(); i++) {
            Widget child = children.get(inverse ? size - i : i);

            child.onMeasure();
            if (child.getVisibility() == Visibility.Gone) continue;

            if (mWidth != MATCH_PARENT) {
                if (child.getMeasureWidth() == MATCH_PARENT) {
                    if (getPrefWidth() == WRAP_CONTENT)
                        mWidth = MATCH_PARENT;
                } else {
                    mWidth += child.getMeasureWidth();
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
        mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        setMeasure(mWidth, mHeight);
    }

    private void vLayout(float width, float height) {
        setLayout(Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
        ArrayList<Widget> children = orderedList;
        boolean inverse = direction == Direction.IVERTICAL;
        int size = children.size() - 1;

        float yoff = Math.min(getHeight(), getPaddingTop() + getMarginTop()), sum = 0;
        for (int i = 0; i < children.size(); i++) {
            Widget child = children.get(inverse ? size - i : i);
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
        for (int i = 0; i < children.size(); i++) {
            Widget child = children.get(inverse ? size - i : i);
            if (child.getVisibility() == Visibility.Gone) continue;

            if (Math.min(child.getMeasureHeight(), h) * mul < Math.min(child.getLayoutMinHeight(), h)) {
                reaming -= Math.min(child.getLayoutMinHeight(), h);
            } else {
                sum2 += Math.min(child.getMeasureHeight(), h);
            }
        }

        float mul2 = reaming / sum2;
        for (int i = 0; i < children.size(); i++) {
            Widget child = children.get(inverse ? size - i : i);
            if (child.getVisibility() == Visibility.Gone) continue;

            float childH;
            if (Math.min(child.getMeasureHeight(), h) * mul < Math.min(child.getLayoutMinHeight(), h)) {
                childH = Math.min(child.getLayoutMinHeight(), h);
            } else {
                childH = Math.min(child.getMeasureHeight(), h) * mul2;
            }
            child.onLayout(w, childH);
            child.setPosition(xOff(child.getWidth()), yoff);
            yoff += child.getHeight();
        }
    }

    private void vMeasure() {
        float mWidth = getPrefWidth(), mHeight = getPrefHeight();

        ArrayList<Widget> children = orderedList;
        boolean inverse = direction == Direction.IHORIZONTAL;
        int size = children.size() - 1;

        for (int i = 0; i < children.size(); i++) {
            Widget child = children.get(inverse ? size - i : i);
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

    @Override
    public void onLayout(float width, float height) {
        if (this.direction == Direction.VERTICAL || this.direction == Direction.IVERTICAL) {
            vLayout(width, height);
        } else {
            hLayout(width, height);
        }
    }

    @Override
    public void onMeasure() {
        if (this.direction == Direction.VERTICAL || this.direction == Direction.IVERTICAL) {
            vMeasure();
        } else {
            hMeasure();
        }
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

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            invalidate(false);
        }
    }

    public Align.Vertical getVerticalAlign() {
        return valign;
    }

    public void setVerticalAlign(Align.Vertical valign) {
        if (this.valign != valign) {
            this.valign = valign;
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

    private float xOff(float childWidth) {
        float start = getPaddingLeft() + getMarginLeft();
        float end = getWidth() - getPaddingRight() - getMarginRight();
        if (end < start) return (start + end) / 2f;
        if (halign == Align.Horizontal.RIGHT) return end - childWidth;
        if (halign == Align.Horizontal.CENTER) return (start + end - childWidth) / 2f;
        return start;
    }

    private float yOff(float childHeight) {
        float start = getPaddingTop() + getMarginTop();
        float end = getHeight() - getPaddingBottom() - getMarginBottom();
        if (end < start) return (start + end) / 2f;
        if (valign == Align.Vertical.BOTTOM || valign == Align.Vertical.BASELINE) return end - childHeight;
        if (valign == Align.Vertical.MIDDLE) return (start + end - childHeight) / 2f;
        return start;
    }
}
