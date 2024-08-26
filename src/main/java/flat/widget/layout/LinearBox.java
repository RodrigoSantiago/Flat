package flat.widget.layout;

import flat.animations.StateInfo;
import flat.graphics.text.Align;
import flat.uxml.UXAttrs;
import flat.widget.Children;
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
        /*UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setDirection(attrs.value("direction").asConstant(info, getDirection()));
        setVerticalAlign(attrs.value("v-align").asConstant(info, getVerticalAlign()));
        setHorizontalAlign(attrs.value("h-align").asConstant(info, getHorizontalAlign()));*/
    }

    @Override
    public void onMeasure() {
        final float offWidth = getPaddingLeft() + getPaddingRight();
        final float offHeight = getPaddingTop() + getPaddingBottom();
        float mWidth = Math.max(getPrefWidth(), Math.max(getMinWidth(), offWidth));
        float mHeight = Math.max(getPrefHeight(), Math.max(getMinHeight(), offHeight));

        boolean vertical = (this.direction == Direction.VERTICAL || this.direction == Direction.IVERTICAL);
        ArrayList<Widget> children = orderedList;

        float childrenWidth = 0, childrenMinWidth = 0;
        float childrenHeight = 0, childrenMinHeight = 0;
        for (Widget child : getChildrenIterable()) {
            child.onMeasure();
            if (child.getVisibility() == Visibility.GONE) continue;

            if (vertical) {
                if (child.mWidth() > childrenWidth) {
                    childrenWidth = child.mWidth();
                }
                if (child.lMinWidth() > childrenMinWidth) {
                    childrenMinWidth += child.lMinWidth();
                }
                childrenHeight += child.mHeight();
                childrenMinHeight += child.lMinHeight();
            } else {
                childrenWidth += child.mWidth();
                childrenMinWidth += child.lMinWidth();
                if (child.mHeight() > childrenHeight) {
                    childrenHeight = child.mHeight();
                }
                if (child.lMinHeight() > childrenMinHeight) {
                    childrenMinHeight += child.lMinHeight();
                }
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
        setLayout(width, height);
        if (this.direction == Direction.VERTICAL || this.direction == Direction.IVERTICAL) {
            layoutHelperVertical(new Children<>(orderedList), getInX(), getInY(), getInWidth(), getInHeight(), halign);
        } else {
            layoutHelperHorizontal(new Children<>(orderedList), getInX(), getInY(), getInWidth(), getInHeight(), valign);
        }
    }

    @Override
    public void add(Widget child) {
        orderedList.add(child);
        super.add(child);
    }

    @Override
    public void add(Widget... children) {
        Collections.addAll(orderedList, children);
        super.add(children);
    }

    @Override
    public void remove(Widget widget) {
        orderedList.remove(widget);
        super.remove(widget);
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
}
