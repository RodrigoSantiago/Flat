package flat.widget.layout;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.ScrollEvent;
import flat.graphics.SmartContext;
import flat.math.shapes.Shape;
import flat.uxml.*;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enums.*;
import flat.widget.value.ScrollBar;

import java.util.ArrayList;
import java.util.List;

public class ScrollBox extends Parent {

    private UXListener<ActionEvent> slideListener;
    private UXValueListener<Float> viewOffsetXListener;
    private UXValueListener<Float> viewOffsetYListener;
    private ArrayList<ValueChange<Float>> viewOffsetXQueue = new ArrayList<>();
    private ArrayList<ValueChange<Float>> viewOffsetYQueue = new ArrayList<>();
    private float viewOffsetX;
    private float viewOffsetY;
    private ScrollBar horizontalBar;
    private ScrollBar verticalBar;

    private Policy horizontalPolicy = Policy.AS_NEEDED;
    private Policy verticalPolicy = Policy.AS_NEEDED;
    private VerticalBarPosition verticalBarPosition = VerticalBarPosition.RIGHT;
    private HorizontalBarPosition horizontalBarPosition = HorizontalBarPosition.BOTTOM;
    private float scrollSensibility = 10f;
    private boolean floatingBars;

    private boolean horizontalVisible;
    private boolean verticalVisible;
    private float totalDimensionY;
    private float totalDimensionX;
    private float viewDimensionY;
    private float viewDimensionX;

    private UXValueListener<Float> scrollX = (change) -> setViewOffsetXBar(change.getValue());
    private UXValueListener<Float> scrollY = (change) -> setViewOffsetYBar(change.getValue());
    private UXListener<ActionEvent> slideX = (event) -> fireSlide();
    private UXListener<ActionEvent> slideY = (event) -> fireSlide();

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
            for (var child : getChildrenIterable()) {
                if (horizontalBarId.equals(child.getId()) && child instanceof ScrollBar bar) {
                    setHorizontalBar(bar);
                    break;
                }
            }
        }

        String verticalBarId = attrs.getAttributeString("vertical-bar-id", null);
        if (verticalBarId != null) {
            for (var child : getChildrenIterable()) {
                if (child != horizontalBar && verticalBarId.equals(child.getId()) && child instanceof ScrollBar bar) {
                    setVerticalBar(bar);
                    break;
                }
            }
        }
        setViewOffsetXListener(attrs.getAttributeValueListener("on-view-offset-x-change", Float.class, controller));
        setViewOffsetYListener(attrs.getAttributeValueListener("on-view-offset-y-change", Float.class, controller));
        setSlideListener(attrs.getAttributeListener("on-slide", ActionEvent.class, controller));

        if (getHorizontalBar() == null) {
            var bar = new ScrollBar();
            bar.setDirection(Direction.HORIZONTAL);
            bar.setPrefWidth(MATCH_PARENT);
            setHorizontalBar(bar);
        }

        if (getVerticalBar() == null) {
            var bar = new ScrollBar();
            bar.setDirection(Direction.VERTICAL);
            bar.setPrefHeight(MATCH_PARENT);
            setVerticalBar(bar);
        }
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setHorizontalPolicy(attrs.getConstant("horizontal-policy", info, getHorizontalPolicy()));
        setVerticalPolicy(attrs.getConstant("vertical-policy", info, getVerticalPolicy()));
        setVerticalBarPosition(attrs.getConstant("vertical-bar-position", info, getVerticalBarPosition()));
        setHorizontalBarPosition(attrs.getConstant("horizontal-bar-position", info, getHorizontalBarPosition()));
        setScrollSensibility(attrs.getNumber("scroll-sensibility", info, getScrollSensibility()));
        setFloatingBars(attrs.getBool("floating-bars", info, isFloatingBars()));
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

        float barSizeX = verticalBar == null || floatingBars ? 0 :
                Math.min(viewDimensionX, Math.min(verticalBar.getMeasureWidth(), verticalBar.getLayoutMaxWidth()));
        float barSizeY = horizontalBar == null || floatingBars ? 0 :
                Math.min(viewDimensionY, Math.min(horizontalBar.getMeasureHeight(), horizontalBar.getLayoutMaxHeight()));

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
            float xx = (verticalBarPosition == VerticalBarPosition.LEFT) ? barSizeX : 0;
            if (horizontalBarPosition == HorizontalBarPosition.TOP) {
                horizontalBar.setLayoutPosition(getInX() + xx, getInY());
            } else {
                horizontalBar.setLayoutPosition(getInX() + xx, getInY() + getInHeight() - horizontalBar.getLayoutHeight());
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
            if (verticalBarPosition == VerticalBarPosition.LEFT) {
                verticalBar.setLayoutPosition(getInX(), getInY());
            } else {
                verticalBar.setLayoutPosition(getInX() + getInWidth() - verticalBar.getLayoutWidth(), getInY());
            }
        }

        float viewX = Math.max(0, Math.min(viewOffsetX, totalDimensionX - viewDimensionX));
        float viewY = Math.max(0, Math.min(viewOffsetY, totalDimensionY - viewDimensionY));

        float xx = (verticalBarPosition == VerticalBarPosition.LEFT) ? barSizeX : 0;
        float yy = (horizontalBarPosition == HorizontalBarPosition.TOP) ? barSizeY : 0;
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
            child.setLayoutPosition(getInX() + xx - viewX, getInY() + yy - viewY);
        }

        float oldX = viewOffsetX;
        float oldY = viewOffsetY;
        viewOffsetX = viewX;
        viewOffsetY = viewY;

        if (oldX != viewOffsetX) {
            viewOffsetXQueue.add(new ValueChange<>(this, oldX, viewOffsetX));
            if (getActivity() != null) {
                getActivity().getWindow().runSync(this::executeQueueX);
            }
        }
        if (oldY != viewOffsetY) {
            viewOffsetYQueue.add(new ValueChange<>(this, oldY, viewOffsetY));
            if (getActivity() != null) {
                getActivity().getWindow().runSync(this::executeQueueY);
            }
        }

        if (horizontalBar != null) {
            horizontalBar.setViewOffsetListener(null);
            horizontalBar.setSlideListener(null);
            horizontalBar.setViewDimension(viewDimensionX);
            horizontalBar.setTotalDimension(totalDimensionX);
            horizontalBar.setViewOffsetListener(scrollX);
            horizontalBar.setSlideListener(slideX);
        }
        if (verticalBar != null) {
            verticalBar.setViewOffsetListener(null);
            verticalBar.setSlideListener(null);
            verticalBar.setViewDimension(viewDimensionY);
            verticalBar.setTotalDimension(totalDimensionY);
            verticalBar.setViewOffsetListener(scrollY);
            verticalBar.setSlideListener(slideY);
        }
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
    protected boolean detachChild(Widget child) {
        if (super.detachChild(child)) {
            if (child == horizontalBar) {
                horizontalBar = null;
            }
            if (child == verticalBar) {
                verticalBar = null;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onDraw(SmartContext context) {
        if (getInWidth() <= 0 || getInHeight() <= 0) return;

        drawBackground(context);
        drawRipple(context);

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
    public void fireScroll(ScrollEvent event) {
        super.fireScroll(event);
        if (!event.isConsumed()) {
            slide(0, - event.getDeltaY() * scrollSensibility);
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

    public VerticalBarPosition getVerticalBarPosition() {
        return verticalBarPosition;
    }

    public void setVerticalBarPosition(VerticalBarPosition verticalBarPosition) {
        if (verticalBarPosition == null) verticalBarPosition = VerticalBarPosition.RIGHT;

        if (this.verticalBarPosition != verticalBarPosition) {
            this.verticalBarPosition = verticalBarPosition;
            invalidate(true);
        }
    }

    public HorizontalBarPosition getHorizontalBarPosition() {
        return horizontalBarPosition;
    }

    public void setHorizontalBarPosition(HorizontalBarPosition horizontalBarPosition) {
        if (horizontalBarPosition == null) horizontalBarPosition = HorizontalBarPosition.BOTTOM;

        if (this.horizontalBarPosition != horizontalBarPosition) {
            this.horizontalBarPosition = horizontalBarPosition;
            invalidate(true);
        }
    }

    public ScrollBar getVerticalBar() {
        return verticalBar;
    }

    public void setVerticalBar(ScrollBar verticalBar) {
        if (this.verticalBar != verticalBar) {
            if (verticalBar == null) {
                var old = this.verticalBar;
                this.verticalBar = null;
                remove(old);
            } else {
                add(verticalBar);
                if (verticalBar.getParent() == this) {
                    var old = this.verticalBar;
                    this.verticalBar = verticalBar;
                    if (old != null) {
                        remove(old);
                    }
                    this.verticalBar.setViewOffsetListener(null);
                    this.verticalBar.setSlideListener(null);
                    this.verticalBar.setViewDimension(viewDimensionY);
                    this.verticalBar.setTotalDimension(totalDimensionY);
                    this.verticalBar.setViewOffset(viewOffsetY);
                    this.verticalBar.setViewOffsetListener(scrollY);
                    this.verticalBar.setSlideListener(slideY);
                }
            }
        }
    }

    public ScrollBar getHorizontalBar() {
        return horizontalBar;
    }

    public void setHorizontalBar(ScrollBar horizontalBar) {
        if (this.horizontalBar != horizontalBar) {
            if (horizontalBar == null) {
                var old = this.horizontalBar;
                this.horizontalBar = null;
                remove(old);
            } else {
                add(horizontalBar);
                if (horizontalBar.getParent() == this) {
                    var old = this.horizontalBar;
                    this.horizontalBar = horizontalBar;
                    if (old != null) {
                        remove(old);
                    }
                    if (this.horizontalBar != null) {
                        this.horizontalBar.setViewOffsetListener(null);
                        this.horizontalBar.setSlideListener(null);
                        this.horizontalBar.setViewDimension(viewDimensionX);
                        this.horizontalBar.setTotalDimension(totalDimensionX);
                        this.horizontalBar.setViewOffset(viewOffsetX);
                        this.horizontalBar.setViewOffsetListener(scrollX);
                        this.horizontalBar.setSlideListener(slideX);
                    }
                }
            }
        }
    }

    public boolean isFloatingBars() {
        return floatingBars;
    }

    public void setFloatingBars(boolean floatingBars) {
        if (this.floatingBars != floatingBars) {
            this.floatingBars = floatingBars;
            invalidate(true);
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
            fireViewOffsetXListener(old);
            if (horizontalBar != null) {
                horizontalBar.setViewOffset(viewOffsetX);
            }
        } else {
            executeQueueX();
        }
    }

    private void setViewOffsetXBar(float viewOffsetX) {
        if (viewOffsetX > totalDimensionX - viewDimensionX) viewOffsetX = totalDimensionX - viewDimensionX;
        if (viewOffsetX < 0) viewOffsetX = 0;

        if (this.viewOffsetX != viewOffsetX) {
            float old = this.viewOffsetX;
            this.viewOffsetX = viewOffsetX;
            invalidate(true);
            fireViewOffsetXListener(old);
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
            fireViewOffsetYListener(old);
            if (verticalBar != null) {
                verticalBar.setViewOffset(viewOffsetY);
            }
        } else {
            executeQueueY();
        }
    }

    private void setViewOffsetYBar(float viewOffsetY) {
        if (viewOffsetY > totalDimensionY - viewDimensionY) viewOffsetY = totalDimensionY - viewDimensionY;
        if (viewOffsetY < 0) viewOffsetY = 0;

        if (this.viewOffsetY != viewOffsetY) {
            float old = this.viewOffsetY;
            this.viewOffsetY = viewOffsetY;
            invalidate(true);
            fireViewOffsetYListener(old);
        }
    }

    public UXValueListener<Float> getViewOffsetXListener() {
        return viewOffsetXListener;
    }

    public void setViewOffsetXListener(UXValueListener<Float> viewOffsetXListener) {
        this.viewOffsetXListener = viewOffsetXListener;
    }

    public UXValueListener<Float> getViewOffsetYListener() {
        return viewOffsetYListener;
    }

    public void setViewOffsetYListener(UXValueListener<Float> viewOffsetYListener) {
        this.viewOffsetYListener = viewOffsetYListener;
    }

    public void slideTo(float offsetX, float offsetY) {
        float oldX = getViewOffsetX();
        setViewOffsetX(offsetX);
        float oldY = getViewOffsetY();
        setViewOffsetY(offsetY);
        if (oldX != getViewOffsetX() || oldY != getViewOffsetY()) {
            fireSlide();
        }
    }

    public void slide(float offsetX, float offsetY) {
        slideTo(getViewOffsetX() + offsetX, getViewOffsetY() + offsetY);
    }

    public UXListener<ActionEvent> getSlideListener() {
        return slideListener;
    }

    public void setSlideListener(UXListener<ActionEvent> slideListener) {
        this.slideListener = slideListener;
    }

    private void fireSlide() {
        if (slideListener != null) {
            UXListener.safeHandle(slideListener, new ActionEvent(this));
        }
    }

    private void fireViewOffsetXListener(float old) {
        if (viewOffsetXListener != null && old != viewOffsetX) {
            if (viewOffsetXQueue.size() > 0) {
                viewOffsetXQueue.add(new ValueChange<>(this, old, viewOffsetX));
                executeQueueX();
            } else {
                UXValueListener.safeHandle(viewOffsetXListener, new ValueChange<>(this, old, viewOffsetX));
            }
        }
    }

    private void executeQueueX() {
        while (viewOffsetXQueue.size() > 0) {
            UXValueListener.safeHandle(viewOffsetXListener, viewOffsetXQueue.remove(0));
        }
    }

    private void fireViewOffsetYListener(float old) {
        if (viewOffsetYListener != null && old != viewOffsetY) {
            if (viewOffsetYQueue.size() > 0) {
                viewOffsetYQueue.add(new ValueChange<>(this, old, viewOffsetY));
                executeQueueY();
            } else {
                UXValueListener.safeHandle(viewOffsetYListener, new ValueChange<>(this, old, viewOffsetY));
            }
        }
    }

    private void executeQueueY() {
        while (viewOffsetYQueue.size() > 0) {
            UXValueListener.safeHandle(viewOffsetYListener, viewOffsetYQueue.remove(0));
        }
    }

    public float getScrollSensibility() {
        return scrollSensibility;
    }

    public void setScrollSensibility(float scrollSensibility) {
        this.scrollSensibility = scrollSensibility;
    }

    public float getViewDimensionX() {
        return viewDimensionX;
    }

    public float getViewDimensionY() {
        return viewDimensionY;
    }

    public float getTotalDimensionX() {
        return totalDimensionX;
    }

    public float getTotalDimensionY() {
        return totalDimensionY;
    }
}
