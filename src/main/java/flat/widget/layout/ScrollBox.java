package flat.widget.layout;

import flat.events.ScrollEvent;
import flat.graphics.Graphics;
import flat.math.Vector2;
import flat.uxml.Controller;
import flat.uxml.UXChildren;
import flat.widget.Widget;
import flat.widget.enums.Policy;
import flat.widget.enums.Visibility;
import flat.widget.value.HorizontalScrollBar;
import flat.widget.value.VerticalScrollBar;

import java.util.List;

public class ScrollBox extends Scrollable {

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        for (var child : children) {
            if (child.getAttributeBool("horizontal-bar", false) &&
                    child.getWidget() instanceof HorizontalScrollBar bar) {
                setHorizontalBar(bar);
            } else if (child.getAttributeBool("vertical-bar", false) &&
                    child.getWidget() instanceof VerticalScrollBar bar) {
                setVerticalBar(bar);
            } else {
                add(child.getWidget());
            }
        }
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth = 0;
        float mHeight = 0;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE) continue;

            child.onMeasure();

            if (child == getVerticalBar() || child == getHorizontalBar()) continue;

            if (wrapWidth) {
                mWidth = Math.max(mWidth, getDefWidth(child));
            }
            if (wrapHeight) {
                mHeight = Math.max(mHeight, Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight()));
            }
        }

        if (wrapWidth) {
            if (getVerticalBar() != null && !isFloatingBars() && getVerticalPolicy() == Policy.ALWAYS) {
                mWidth += Math.min(getVerticalBar().getMeasureWidth(), getVerticalBar().getLayoutMaxWidth());
            }
        }

        if (wrapHeight) {
            if (getHorizontalBar() != null && !isFloatingBars() && getHorizontalPolicy() == Policy.ALWAYS) {
                mHeight += Math.min(getHorizontalBar().getMeasureHeight(), getHorizontalBar().getLayoutMaxHeight());
            }
        }

        if (wrapWidth) {
            mWidth = Math.max(mWidth + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(mHeight + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public Vector2 onLayoutTotalDimension(float width, float height) {
        float localDimensionX = 0;
        float localDimensionY = 0;

        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE || child == getVerticalBar() || child == getHorizontalBar())
                continue;

            if (child.getMeasureWidth() != MATCH_PARENT) {
                localDimensionX = Math.max(localDimensionX, child.getMeasureWidth());
            } else {
                localDimensionX = Math.max(localDimensionX, child.getLayoutMinWidth());
            }
            if (child.getMeasureHeight() != MATCH_PARENT) {
                localDimensionY = Math.max(localDimensionY, child.getMeasureHeight());
            } else {
                localDimensionY = Math.max(localDimensionY, child.getLayoutMinHeight());
            }
        }

        return new Vector2(localDimensionX, localDimensionY);
    }

    @Override
    public void setLayoutScrollOffset(float xx, float yy) {
        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE || child == getVerticalBar() || child == getHorizontalBar())
                continue;

            float childWidth;
            if (child.getMeasureWidth() == MATCH_PARENT) {
                childWidth = Math.min(child.getLayoutMaxWidth(), getTotalDimensionX());
            } else {
                childWidth = getDefWidth(child);
            }

            float childHeight;
            if (child.getMeasureHeight() == MATCH_PARENT) {
                childHeight = Math.min(child.getLayoutMaxHeight(), getTotalDimensionY());
            } else {
                childHeight = Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight());
            }

            child.onLayout(childWidth, childHeight);
            child.setLayoutPosition(xx, yy);
        }
    }

    @Override
    public void onLayout(float width, float height) {
        super.onLayout(width, height);
    }

    @Override
    public boolean onLayoutSingleChild(Widget child) {
        return false;
    }

    @Override
    public void add(Widget... children) {
        super.add(children);
    }

    @Override
    public void add(Widget child) {
        super.add(child);
    }

    @Override
    public void add(List<Widget> children) {
        super.add(children);
    }

    @Override
    public void onDraw(Graphics graphics) {
        drawBackground(graphics);
        drawRipple(graphics);

        if (getOutWidth() <= 0 || getOutHeight() <= 0) return;

        graphics.setTransform2D(getTransform());
        graphics.pushClip(getBackgroundShape());
        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.VISIBLE &&
                    child != getHorizontalBar() && child != getVerticalBar()) {
                child.onDraw(graphics);
            }
        }
        graphics.popClip();

        if (getHorizontalBar() != null && isHorizontalVisible()) {
            getHorizontalBar().onDraw(graphics);
        }

        if (getVerticalBar() != null && isVerticalVisible()) {
            getVerticalBar().onDraw(graphics);
        }
    }

    @Override
    public void scroll(ScrollEvent event) {
        super.scroll(event);
        if (!event.isConsumed()) {
            slideVertical(- event.getDeltaY() * getScrollSensibility());
            event.consume();
        }
    }
}
