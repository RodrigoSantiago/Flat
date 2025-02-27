package flat.widget.layout;

import flat.animations.StateInfo;
import flat.events.SlideEvent;
import flat.math.Vector2;
import flat.uxml.*;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enums.HorizontalBarPosition;
import flat.widget.enums.Policy;
import flat.widget.enums.VerticalBarPosition;
import flat.widget.enums.Visibility;
import flat.widget.value.HorizontalScrollBar;
import flat.widget.value.VerticalScrollBar;

public abstract class Scrollable extends Parent {

    private UXListener<SlideEvent> slideHorizontalListener;
    private UXListener<SlideEvent> slideHorizontalFilter;
    private UXListener<SlideEvent> slideVerticalListener;
    private UXListener<SlideEvent> slideVerticalFilter;
    private UXValueListener<Float> viewOffsetXListener;
    private UXValueListener<Float> viewOffsetYListener;
    private float viewOffsetX;
    private float viewOffsetY;
    private HorizontalScrollBar horizontalBar;
    private VerticalScrollBar verticalBar;

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

    private final UXListener<SlideEvent> slideX = (event) -> {
        event.consume();
        slideHorizontalTo(event.getValue());
    };

    private final UXListener<SlideEvent> slideY = (event) -> {
        event.consume();
        slideVerticalTo(event.getValue());
    };

    public Scrollable() {
        setHorizontalBar(new HorizontalScrollBar());
        setVerticalBar(new VerticalScrollBar());
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setViewOffsetXListener(attrs.getAttributeValueListener("on-view-offset-x-change", Float.class, controller));
        setViewOffsetYListener(attrs.getAttributeValueListener("on-view-offset-y-change", Float.class, controller));
        setSlideHorizontalListener(attrs.getAttributeListener("on-slide-horizontal", SlideEvent.class, controller));
        setSlideVerticalListener(attrs.getAttributeListener("on-slide-vertical", SlideEvent.class, controller));
        setSlideHorizontalFilter(attrs.getAttributeListener("on-slide-horizontal-filter", SlideEvent.class, controller));
        setSlideVerticalFilter(attrs.getAttributeListener("on-slide-vertical-filter", SlideEvent.class, controller));
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

    public Vector2 onLayoutLocalDimension(float width, float height) {
        return new Vector2(0, 0);
    }

    public void setLayoutScrollOffset(float xx, float yy) {

    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);
        Vector2 localDimension = onLayoutLocalDimension(width, height);

        viewDimensionX = getInWidth();
        viewDimensionY = getInHeight();

        float barSizeX = verticalBar == null || floatingBars ? 0 :
                Math.min(viewDimensionX, Math.min(verticalBar.getMeasureWidth(), verticalBar.getLayoutMaxWidth()));
        float barSizeY = horizontalBar == null || floatingBars ? 0 :
                Math.min(viewDimensionY, Math.min(horizontalBar.getMeasureHeight(), horizontalBar.getLayoutMaxHeight()));

        boolean isHorizontalLocalVisible = (horizontalPolicy == Policy.ALWAYS) ||
                (horizontalPolicy == Policy.AS_NEEDED && viewDimensionX < localDimension.x - 0.001f);
        boolean isVerticalLocalVisible = (verticalPolicy == Policy.ALWAYS) ||
                (verticalPolicy == Policy.AS_NEEDED && viewDimensionY < localDimension.y - 0.001f);

        if (barSizeX > 0 && barSizeY > 0 && (isHorizontalLocalVisible != isVerticalLocalVisible)) {
            if (!isHorizontalLocalVisible && horizontalPolicy == Policy.AS_NEEDED) {
                isHorizontalLocalVisible = viewDimensionX - barSizeX < localDimension.x - 0.001f;
            }
            if (!isVerticalLocalVisible && verticalPolicy == Policy.AS_NEEDED) {
                isVerticalLocalVisible = viewDimensionY - barSizeY < localDimension.y - 0.001f;
            }
        }

        horizontalVisible = isHorizontalLocalVisible;
        verticalVisible = isVerticalLocalVisible;

        if (!isVerticalLocalVisible) barSizeX = 0;
        if (!isHorizontalLocalVisible) barSizeY = 0;

        viewDimensionX -= barSizeX;
        viewDimensionY -= barSizeY;
        totalDimensionX = Math.max(viewDimensionX, localDimension.x);
        totalDimensionY = Math.max(viewDimensionY, localDimension.y);

        if (horizontalBar != null) {
            float childWidth;
            if (horizontalBar.getMeasureWidth() == MATCH_PARENT) {
                childWidth = Math.min(getOutWidth() - barSizeX, horizontalBar.getLayoutMaxWidth());
            } else {
                childWidth = Math.min(horizontalBar.getMeasureWidth(), horizontalBar.getLayoutMaxWidth());
            }

            float childHeight;
            if (horizontalBar.getMeasureHeight() == MATCH_PARENT) {
                childHeight = Math.min(viewDimensionY, horizontalBar.getLayoutMaxHeight());
            } else {
                childHeight = Math.min(horizontalBar.getMeasureHeight(), horizontalBar.getLayoutMaxHeight());
            }
            horizontalBar.onLayout(Math.min(getOutWidth(), childWidth), Math.min(getOutHeight(), childHeight));
            float xx = (verticalBarPosition == VerticalBarPosition.LEFT) ? barSizeX : 0;
            if (horizontalBarPosition == HorizontalBarPosition.TOP) {
                horizontalBar.setLayoutPosition(getOutX() + xx, getOutY());
            } else {
                horizontalBar.setLayoutPosition(getOutX() + xx, getOutY() + getOutHeight() - horizontalBar.getLayoutHeight());
            }

            horizontalBar.setViewOffsetListener(null);
            horizontalBar.setSlideListener(null);
            horizontalBar.setViewDimension(viewDimensionX);
            horizontalBar.setTotalDimension(totalDimensionX);
            horizontalBar.setSlideFilter(slideX);
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
                childHeight = Math.min(getOutHeight(), verticalBar.getLayoutMaxHeight()); // Fill the height gap
            } else {
                childHeight = Math.min(verticalBar.getMeasureHeight(), verticalBar.getLayoutMaxHeight());
            }
            verticalBar.onLayout(Math.min(getOutWidth(), childWidth), Math.min(getOutHeight(), childHeight));
            if (verticalBarPosition == VerticalBarPosition.LEFT) {
                verticalBar.setLayoutPosition(getOutX(), getOutY());
            } else {
                verticalBar.setLayoutPosition(getOutX() + getOutWidth() - verticalBar.getLayoutWidth(), getOutY());
            }

            verticalBar.setViewOffsetListener(null);
            verticalBar.setSlideListener(null);
            verticalBar.setViewDimension(viewDimensionY);
            verticalBar.setTotalDimension(totalDimensionY);
            verticalBar.setSlideFilter(slideY);
        }

        float viewX = Math.max(0, Math.min(viewOffsetX, totalDimensionX - viewDimensionX));
        float viewY = Math.max(0, Math.min(viewOffsetY, totalDimensionY - viewDimensionY));

        float xx = (verticalBarPosition == VerticalBarPosition.LEFT) ? barSizeX : 0;
        float yy = (horizontalBarPosition == HorizontalBarPosition.TOP) ? barSizeY : 0;
        setLayoutScrollOffset(getInX() + xx - viewX, getInY() + yy - viewY);

        float oldX = viewOffsetX;
        float oldY = viewOffsetY;
        if (viewOffsetX != viewX && getActivity() != null) {
            getActivity().getWindow().runSync(() -> setViewOffsetX(getViewOffsetX()));
        }
        if (viewOffsetY != viewY && getActivity() != null) {
            getActivity().getWindow().runSync(() -> setViewOffsetY(getViewOffsetY()));
        }
    }

    @Override
    public boolean onLayoutSingleChild(Widget child) {
        return false;
    }

    @Override
    protected boolean detachChild(Widget child) {
        if (child == horizontalBar) {
            return false;
        }
        if (child == verticalBar) {
            return false;
        }
        return super.detachChild(child);
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

    public VerticalScrollBar getVerticalBar() {
        return verticalBar;
    }

    public void setVerticalBar(VerticalScrollBar verticalBar) {
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
                    this.verticalBar.setSlideFilter(slideY);
                }
            }
        }
    }

    public HorizontalScrollBar getHorizontalBar() {
        return horizontalBar;
    }

    public void setHorizontalBar(HorizontalScrollBar horizontalBar) {
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
                        this.horizontalBar.setSlideFilter(slideX);
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
        return Math.max(0, Math.min(viewOffsetX, totalDimensionX - viewDimensionX));
    }

    public void setViewOffsetX(float viewOffsetX) {
        viewOffsetX = Math.max(0, Math.min(viewOffsetX, totalDimensionX - viewDimensionX));

        if (this.viewOffsetX != viewOffsetX) {
            float old = this.viewOffsetX;
            this.viewOffsetX = viewOffsetX;
            invalidate(true);
            fireViewOffsetXListener(old);
            if (horizontalBar != null) {
                horizontalBar.setViewOffsetListener(null);
                horizontalBar.setViewOffset(this.viewOffsetX);
            }
        }
    }

    public float getViewOffsetY() {
        return Math.max(0, Math.min(viewOffsetY, totalDimensionY - viewDimensionY));
    }

    public void setViewOffsetY(float viewOffsetY) {
        viewOffsetY = Math.max(0, Math.min(viewOffsetY, totalDimensionY - viewDimensionY));

        if (this.viewOffsetY != viewOffsetY) {
            float old = this.viewOffsetY;
            this.viewOffsetY = viewOffsetY;
            invalidate(true);
            fireViewOffsetYListener(old);
            if (verticalBar != null) {
                verticalBar.setViewOffsetListener(null);
                verticalBar.setViewOffset(this.viewOffsetY);
            }
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

    public void slide(float offsetX, float offsetY) {
        slideHorizontal(offsetX);
        slideVertical(offsetY);
    }

    public void slideTo(float offsetX, float offsetY) {
        slideHorizontalTo(offsetX);
        slideVerticalTo(offsetY);
    }

    public void slideHorizontalTo(float offsetX) {
        offsetX = Math.max(0, Math.min(offsetX, totalDimensionX - viewDimensionX));

        float old = viewOffsetX;
        if (offsetX != old && filterSlideX(offsetX)) {
            setViewOffsetX(offsetX);
            fireSlideX();
        }
    }

    public void slideHorizontal(float offsetX) {
        slideHorizontalTo(getViewOffsetX() + offsetX);
    }

    public void slideVerticalTo(float offsetY) {
        offsetY = Math.max(0, Math.min(offsetY, totalDimensionY - viewDimensionY));

        float old = viewOffsetY;
        if (offsetY != old && filterSlideY(offsetY)) {
            setViewOffsetY(offsetY);
            fireSlideY();
        }
    }

    public void slideVertical(float offsetY) {
        slideVerticalTo(getViewOffsetY() + offsetY);
    }

    public UXListener<SlideEvent> getSlideHorizontalListener() {
        return slideHorizontalListener;
    }

    public void setSlideHorizontalListener(UXListener<SlideEvent> slideHorizontalListener) {
        this.slideHorizontalListener = slideHorizontalListener;
    }

    private void fireSlideX() {
        if (slideHorizontalListener != null) {
            UXListener.safeHandle(slideHorizontalListener, new SlideEvent(this, SlideEvent.SLIDE, viewOffsetX));
        }
    }

    public UXListener<SlideEvent> getSlideVerticalListener() {
        return slideVerticalListener;
    }

    public void setSlideVerticalListener(UXListener<SlideEvent> slideVerticalListener) {
        this.slideVerticalListener = slideVerticalListener;
    }

    private void fireSlideY() {
        if (slideVerticalListener != null) {
            UXListener.safeHandle(slideVerticalListener, new SlideEvent(this, SlideEvent.SLIDE, viewOffsetY));
        }
    }

    private void fireViewOffsetXListener(float old) {
        if (viewOffsetXListener != null && old != viewOffsetX) {
            UXValueListener.safeHandle(viewOffsetXListener, new ValueChange<>(this, old, viewOffsetX));
        }
    }

    private void fireViewOffsetYListener(float old) {
        if (viewOffsetYListener != null && old != viewOffsetY) {
            UXValueListener.safeHandle(viewOffsetYListener, new ValueChange<>(this, old, viewOffsetY));
        }
    }

    public UXListener<SlideEvent> getSlideHorizontalFilter() {
        return slideHorizontalFilter;
    }

    public void setSlideHorizontalFilter(UXListener<SlideEvent> slideHorizontalFilter) {
        this.slideHorizontalFilter = slideHorizontalFilter;
    }

    private boolean filterSlideX(float viewOffsetX) {
        if (slideHorizontalFilter != null) {
            var event = new SlideEvent(this, SlideEvent.FILTER, viewOffsetX);
            UXListener.safeHandle(slideHorizontalFilter, event);
            return !event.isConsumed();
        }
        return true;
    }

    public UXListener<SlideEvent> getSlideVerticalFilter() {
        return slideVerticalFilter;
    }

    public void setSlideVerticalFilter(UXListener<SlideEvent> slideVerticalFilter) {
        this.slideVerticalFilter = slideVerticalFilter;
    }

    private boolean filterSlideY(float viewOffsetY) {
        if (slideVerticalFilter != null) {
            var event = new SlideEvent(this, SlideEvent.FILTER, viewOffsetY);
            UXListener.safeHandle(slideVerticalFilter, event);
            return !event.isConsumed();
        }
        return true;
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

    protected void setViewDimensionX(float viewDimensionX) {
        this.viewDimensionX = viewDimensionX;
    }

    public float getViewDimensionY() {
        return viewDimensionY;
    }

    protected void setViewDimensionY(float viewDimensionY) {
        this.viewDimensionY = viewDimensionY;
    }

    public float getTotalDimensionX() {
        return totalDimensionX;
    }

    protected void setTotalDimensionX(float totalDimensionX) {
        this.totalDimensionX = totalDimensionX;
    }

    public float getTotalDimensionY() {
        return totalDimensionY;
    }

    protected void setTotalDimensionY(float totalDimensionY) {
        this.totalDimensionY = totalDimensionY;
    }

    protected boolean isHorizontalVisible() {
        return horizontalVisible;
    }

    protected boolean isVerticalVisible() {
        return verticalVisible;
    }

    protected boolean isHorizontalDimensionScroll() {
        return totalDimensionX > viewDimensionX + 0.01f;
    }

    protected boolean isVerticalDimensionScroll() {
        return totalDimensionY > viewDimensionY + 0.01f;
    }
}
