package flat.widget.layout;

import flat.animations.StateInfo;
import flat.events.ScrollEvent;
import flat.graphics.SmartContext;
import flat.math.shapes.Shape;
import flat.uxml.*;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enums.HorizontalPosition;
import flat.widget.enums.Policy;
import flat.widget.enums.VerticalPosition;
import flat.widget.enums.Visibility;
import flat.widget.value.ScrollBar;

public class ScrollBox extends Parent {

    private Policy horizontalPolicy = Policy.AS_NEEDED;
    private Policy verticalPolicy = Policy.AS_NEEDED;
    private HorizontalPosition horizontalPosition = HorizontalPosition.RIGHT;
    private VerticalPosition verticalPosition = VerticalPosition.BOTTOM;
    private ScrollBar horizontalBar;
    private ScrollBar verticalBar;
    private boolean horizontalVisible;
    private boolean verticalVisible;
    private float viewOffsetX;
    private float viewOffsetY;
    private float totalDimensionY;
    private float totalDimensionX;
    private float viewDimensionY;
    private float viewDimensionX;
    private float scrollSensibility = 10f;

    private UXValueListener<Float> scrollX = (change) -> setViewOffsetXBar(change.getValue());
    private UXValueListener<Float> scrollY = (change) -> setViewOffsetYBar(change.getValue());

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        Widget widget;
        while ((widget = children.next()) != null) {
            add(widget);
        }
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();

        String horizontalBarId = attrs.getAttributeString("horizontal-bar-id", null);
        if (horizontalBarId != null) {
            for (var child : getChildren()) {
                if (horizontalBarId.equals(child.getId()) && child instanceof ScrollBar bar) {
                    horizontalBar = bar;
                    break;
                }
            }
        }

        String verticalBarId = attrs.getAttributeString("vertical-bar-id", null);
        if (verticalBarId != null) {
            for (var child : getChildren()) {
                if (child != horizontalBar && verticalBarId.equals(child.getId()) && child instanceof ScrollBar bar) {
                    verticalBar = bar;
                    break;
                }
            }
        }
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setHorizontalPolicy(attrs.getConstant("horizontal-policy", info, getHorizontalPolicy()));
        setVerticalPolicy(attrs.getConstant("vertical-policy", info, getVerticalPolicy()));
        setHorizontalPosition(attrs.getConstant("horizontal-position", info, getHorizontalPosition()));
        setVerticalPosition(attrs.getConstant("vertical-position", info, getVerticalPosition()));
        setScrollSensibility(attrs.getNumber("scroll-sensibility", info, getScrollSensibility()));
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

            if (child == verticalBar || child == horizontalBar) continue;

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
    public void onLayout(float width, float height) {
        setLayout(width, height);

        float lWidth = Math.max(0, getLayoutWidth()
                - getMarginLeft() - getMarginRight() - getPaddingLeft() - getPaddingRight());
        float lHeight = Math.max(0, getLayoutHeight()
                - getMarginTop() - getMarginBottom() - getPaddingTop() - getPaddingBottom());

        float localDimensionX = 0;
        float localDimensionY = 0;

        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE || child == verticalBar || child == horizontalBar) continue;

            if (child.getMeasureWidth() != MATCH_PARENT) {
                localDimensionX = Math.max(localDimensionX, child.getMeasureWidth());
            }
            if (child.getMeasureHeight() != MATCH_PARENT) {
                localDimensionY = Math.max(localDimensionY, child.getMeasureHeight());
            }
        }

        viewDimensionX = lWidth;
        viewDimensionY = lHeight;

        float barSizeX = verticalBar == null ? 0 : verticalBar.getLayoutWidth();
        float barSizeY = horizontalBar == null ? 0 : horizontalBar.getLayoutHeight();
        boolean isHorizontalLocalVisible = (horizontalPolicy == Policy.ALWAYS) ||
                (horizontalPolicy == Policy.AS_NEEDED && viewDimensionX < localDimensionX - 0.001f);
        boolean isVerticalLocalVisible = (verticalPolicy == Policy.ALWAYS) ||
                (verticalPolicy == Policy.AS_NEEDED && viewDimensionY < localDimensionY - 0.001f);

        if (barSizeX > 0 && barSizeY > 0 && (isHorizontalLocalVisible != isVerticalLocalVisible)) {
            if (!isHorizontalLocalVisible && horizontalPolicy == Policy.AS_NEEDED) {
                isHorizontalLocalVisible = viewDimensionX - barSizeX < localDimensionX - 0.001f;
            }
            if (!isVerticalLocalVisible && verticalPolicy == Policy.AS_NEEDED) {
                isVerticalLocalVisible = viewDimensionY - barSizeY < localDimensionY - 0.001f;
            }
        }
        horizontalVisible = isHorizontalLocalVisible;
        verticalVisible = isVerticalLocalVisible;

        if (!isVerticalLocalVisible) barSizeX = 0;
        if (!isHorizontalLocalVisible) barSizeY = 0;


        viewDimensionX -= barSizeX;
        viewDimensionY -= barSizeY;
        totalDimensionX = Math.max(viewDimensionX, localDimensionX);
        totalDimensionY = Math.max(viewDimensionY, localDimensionY);

        if (horizontalBar != null) {
            float childWidth;
            if (horizontalBar.getMeasureWidth() == MATCH_PARENT) {
                childWidth = Math.min(viewDimensionX, horizontalBar.getLayoutMaxWidth());
            } else {
                childWidth = Math.min(horizontalBar.getMeasureWidth(), horizontalBar.getLayoutMaxWidth());
            }

            float childHeight;
            if (horizontalBar.getMeasureHeight() == MATCH_PARENT) {
                childHeight = Math.min(viewDimensionY, horizontalBar.getLayoutMaxHeight());
            } else {
                childHeight = Math.min(horizontalBar.getMeasureHeight(), horizontalBar.getLayoutMaxHeight());
            }
            horizontalBar.onLayout(childWidth, childHeight);
            float xx = (horizontalPosition == HorizontalPosition.LEFT) ? barSizeX : 0;
            if (verticalPosition == VerticalPosition.TOP) {
                horizontalBar.setPosition(getInX() + xx, getInY());
            } else {
                horizontalBar.setPosition(getInX() + xx, getInY() + getInHeight() - horizontalBar.getLayoutHeight());
            }
        }

        if (verticalBar != null) {
            float childWidth;
            if (verticalBar.getMeasureWidth() == MATCH_PARENT) {
                childWidth = Math.min(viewDimensionX, verticalBar.getLayoutMaxWidth());
            } else {
                childWidth = Math.min(verticalBar.getMeasureWidth(), verticalBar.getLayoutMaxWidth());
            }

            float childHeight;
            if (verticalBar.getMeasureHeight() == MATCH_PARENT) {
                childHeight = Math.min(lHeight, verticalBar.getLayoutMaxHeight()); // Fill the gap
            } else {
                childHeight = Math.min(verticalBar.getMeasureHeight(), verticalBar.getLayoutMaxHeight());
            }
            verticalBar.onLayout(childWidth, childHeight);
            if (horizontalPosition == HorizontalPosition.LEFT) {
                verticalBar.setPosition(getInX(), getInY());
            } else {
                verticalBar.setPosition(getInX() + getInWidth() - verticalBar.getLayoutWidth(), getInY());
            }
        }

        if (viewOffsetX > totalDimensionX - viewDimensionX) viewOffsetX = totalDimensionX - viewDimensionX;
        if (viewOffsetX < 0) viewOffsetX = 0;

        if (viewOffsetY > totalDimensionY - viewDimensionY) viewOffsetY = totalDimensionY - viewDimensionY;
        if (viewOffsetY < 0) viewOffsetY = 0;

        if (horizontalBar != null) {
            horizontalBar.setViewDimension(viewDimensionX);
            horizontalBar.setTotalDimension(totalDimensionX);
            horizontalBar.setViewOffsetListener(scrollX);
        }
        if (verticalBar != null) {
            verticalBar.setViewDimension(viewDimensionY);
            verticalBar.setTotalDimension(totalDimensionY);
            verticalBar.setViewOffsetListener(scrollY);
        }

        float xx = (horizontalPosition == HorizontalPosition.LEFT) ? barSizeX : 0;
        float yy = (verticalPosition == VerticalPosition.TOP) ? barSizeY : 0;
        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE || child == verticalBar || child == horizontalBar) continue;

            float childWidth;
            if (child.getMeasureWidth() == MATCH_PARENT) {
                childWidth = Math.min(child.getLayoutMaxWidth(), totalDimensionX);
            } else {
                childWidth = Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth());
            }

            float childHeight;
            if (child.getMeasureHeight() == MATCH_PARENT) {
                childHeight = Math.min(child.getLayoutMaxHeight(), totalDimensionY);
            } else {
                childHeight = Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight());
            }

            child.onLayout(childWidth, childHeight);
            child.setPosition(getInX() + xx - viewOffsetX, getInY() + yy - viewOffsetY);
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        drawBackground(context);
        drawRipple(context);

        if (getInWidth() <= 0 || getInHeight() <= 0) return;

        Shape oldClip = backgroundClip(context);
        for (Widget child : getChildrenIterable()) {
            if (child != horizontalBar && child != verticalBar && child.getVisibility() == Visibility.VISIBLE) {
                child.onDraw(context);
            }
        }

        if (horizontalBar != null && horizontalVisible) {
            horizontalBar.onDraw(context);
        }

        if (verticalBar != null && verticalVisible) {
            verticalBar.onDraw(context);
        }

        context.setTransform2D(null);
        context.setClip(oldClip);
    }

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if ((includeDisabled || isEnabled()) && (getVisibility() != Visibility.GONE)) {
            if (contains(x, y)) {
                if (verticalBar != null) {
                    Widget found = verticalBar.findByPosition(x, y, includeDisabled);
                    if (found != null) return found;
                }
                if (horizontalBar != null) {
                    Widget found = horizontalBar.findByPosition(x, y, includeDisabled);
                    if (found != null) return found;
                }

                for (Widget child : getChildrenIterableReverse()) {
                    Widget found = child.findByPosition(x, y, includeDisabled);
                    if (found != null) return found;
                }
                return isClickable() ? this : null;
            }
        }
        return null;
    }

    @Override
    public void fireScroll(ScrollEvent scrollEvent) {
        super.fireScroll(scrollEvent);
        if (!scrollEvent.isConsumed()) {
            setViewOffsetY(getViewOffsetY() - scrollEvent.getDeltaY() * scrollSensibility);
        }
    }

    public Policy getHorizontalPolicy() {
        return horizontalPolicy;
    }

    public void setHorizontalPolicy(Policy horizontalPolicy) {
        if (horizontalPolicy == null) horizontalPolicy = Policy.AS_NEEDED;

        if (this.horizontalPolicy != horizontalPolicy) {
            this.horizontalPolicy = horizontalPolicy;
            invalidate(true);
        }
    }

    public Policy getVerticalPolicy() {
        return verticalPolicy;
    }

    public void setVerticalPolicy(Policy verticalPolicy) {
        if (verticalPolicy == null) verticalPolicy = Policy.AS_NEEDED;

        if (this.verticalPolicy != verticalPolicy) {
            this.verticalPolicy = verticalPolicy;
            invalidate(true);
        }
    }

    public HorizontalPosition getHorizontalPosition() {
        return horizontalPosition;
    }

    public void setHorizontalPosition(HorizontalPosition horizontalPosition) {
        if (horizontalPosition == null) horizontalPosition = HorizontalPosition.RIGHT;

        if (this.horizontalPosition != horizontalPosition) {
            this.horizontalPosition = horizontalPosition;
            invalidate(true);
        }
    }

    public VerticalPosition getVerticalPosition() {
        return verticalPosition;
    }

    public void setVerticalPosition(VerticalPosition verticalPosition) {
        if (verticalPosition == null) verticalPosition = VerticalPosition.BOTTOM;

        if (this.verticalPosition != verticalPosition) {
            this.verticalPosition = verticalPosition;
            invalidate(true);
        }
    }

    public ScrollBar getVerticalBar() {
        return verticalBar;
    }

    public void setVerticalBar(ScrollBar verticalBar) {
        if (this.verticalBar != verticalBar) {
            add(verticalBar);
            if (verticalBar.getParent() == this) {
                if (this.verticalBar != null) {
                    remove(this.verticalBar);
                }
                this.verticalBar = verticalBar;
            }
        }
    }

    public ScrollBar getHorizontalBar() {
        return horizontalBar;
    }

    public void setHorizontalBar(ScrollBar horizontalBar) {
        if (this.horizontalBar != horizontalBar) {
            add(horizontalBar);
            if (horizontalBar.getParent() == this) {
                if (this.horizontalBar != null) {
                    remove(this.horizontalBar);
                }
                this.horizontalBar = horizontalBar;
            }
        }
    }

    public float getViewOffsetX() {
        return viewOffsetX;
    }

    public void setViewOffsetX(float viewOffsetX) {
        if (viewOffsetX > totalDimensionX - viewDimensionX) viewOffsetX = totalDimensionX - viewDimensionX;
        if (viewOffsetX < 0) viewOffsetX = 0;

        if (this.viewOffsetX != viewOffsetX) {
            float old = this.viewOffsetX;
            this.viewOffsetX = viewOffsetX;
            invalidate(true);
            if (horizontalBar != null) {
                horizontalBar.setViewOffset(viewOffsetX);
            }
            // fireViewOffsetXListener(old);
        }
    }

    private void setViewOffsetXBar(float viewOffsetX) {
        if (viewOffsetX > totalDimensionX - viewDimensionX) viewOffsetX = totalDimensionX - viewDimensionX;
        if (viewOffsetX < 0) viewOffsetX = 0;

        if (this.viewOffsetX != viewOffsetX) {
            float old = this.viewOffsetX;
            this.viewOffsetX = viewOffsetX;
            invalidate(true);
            // fireViewOffsetXListener(old);
        }
    }

    public float getViewOffsetY() {
        return viewOffsetY;
    }

    public void setViewOffsetY(float viewOffsetY) {
        if (viewOffsetY > totalDimensionY - viewDimensionY) viewOffsetY = totalDimensionY - viewDimensionY;
        if (viewOffsetY < 0) viewOffsetY = 0;

        if (this.viewOffsetY != viewOffsetY) {
            float old = this.viewOffsetY;
            this.viewOffsetY = viewOffsetY;
            invalidate(true);
            if (verticalBar != null) {
                verticalBar.setViewOffset(viewOffsetY);
            }
            // fireViewOffsetYListener(old);
        }
    }

    private void setViewOffsetYBar(float viewOffsetY) {
        if (viewOffsetY > totalDimensionY - viewDimensionY) viewOffsetY = totalDimensionY - viewDimensionY;
        if (viewOffsetY < 0) viewOffsetY = 0;

        if (this.viewOffsetY != viewOffsetY) {
            float old = this.viewOffsetY;
            this.viewOffsetY = viewOffsetY;
            invalidate(true);
            // fireViewOffsetYListener(old);
        }
    }

    public float getScrollSensibility() {
        return scrollSensibility;
    }

    public void setScrollSensibility(float scrollSensibility) {
        this.scrollSensibility = scrollSensibility;
    }
}
