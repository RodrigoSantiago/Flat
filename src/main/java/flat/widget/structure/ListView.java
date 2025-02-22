package flat.widget.structure;

import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.ScrollEvent;
import flat.events.SlideEvent;
import flat.graphics.SmartContext;
import flat.math.shapes.Shape;
import flat.uxml.*;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enums.Visibility;
import flat.widget.value.HorizontalScrollBar;
import flat.widget.value.VerticalScrollBar;
import flat.window.Activity;

import java.util.ArrayList;

public class ListView extends Parent {

    private final ArrayList<Widget> items = new ArrayList<>();

    private UXListener<SlideEvent> slideHorizontalListener;
    private UXListener<SlideEvent> slideHorizontalFilter;
    private UXListener<SlideEvent> slideVerticalListener;
    private UXListener<SlideEvent> slideVerticalFilter;
    private UXValueListener<Float> viewOffsetXListener;
    private UXValueListener<Float> viewOffsetYListener;

    private ListViewAdapter<?> adapter;
    private float itemHeight = 8;
    private float scrollSensibility = 10;

    private float viewOffsetY;
    private float viewDimensionY;
    private float viewDimensionExtraY;
    private float totalDimensionY;
    private float viewBackOffsetY;

    private float viewOffsetX;
    private float viewDimensionX;
    private float totalDimensionX;

    private int startIndex;
    private int endIndex;
    private int totalIndex;

    private RefreshAnimation refresh = new RefreshAnimation();

    private HorizontalScrollBar horizontalBar;
    private VerticalScrollBar verticalBar;
    
    private UXListener<SlideEvent> slideX = (event) -> {
        event.consume();
        slideHorizontalTo(event.getViewOffsetDimension());
    };

    private UXListener<SlideEvent> slideY = (event) -> {
        event.consume();
        slideVerticalTo(event.getViewOffsetDimension());
    };

    public ListView() {
        setHorizontalBar(new HorizontalScrollBar());
        setVerticalBar(new VerticalScrollBar());
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        UXAttrs attrs = getAttrs();
        String hBarId = attrs.getAttributeString("horizontal-bar-id", null);
        String vBarId = attrs.getAttributeString("vertical-bar-id", null);

        Widget widget;
        while ((widget = children.next()) != null ) {
            if (hBarId != null && hBarId.equals(widget.getId()) && widget instanceof HorizontalScrollBar bar) {
                setHorizontalBar(bar);
            }
            if (vBarId != null && vBarId.equals(widget.getId()) && widget instanceof VerticalScrollBar bar) {
                setVerticalBar(bar);
            }
        }
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
        setScrollSensibility(attrs.getNumber("scroll-sensibility", info, getScrollSensibility()));
        setItemHeight(attrs.getSize("item-height", info, getItemHeight()));
    }

    private void invalidateItems() {
        refresh.play(getActivity());
    }

    private void resetInvalidateItems() {
        refresh.stop(false);
    }

    private void updateDimensions() {
        int count = adapter == null ? 0 : adapter.size();
        float bestHeight = getInHeight() > 0 ? getInHeight()
                : getHeight() > 0 ? getHeight()
                : getPrefHeight() > 0 && getPrefHeight() != MATCH_PARENT ? getPrefHeight()
                : 16;
        bestHeight = Math.max(bestHeight, 16);
        totalDimensionY = itemHeight * count;
        viewDimensionY = getInHeight();
        viewDimensionExtraY = getActivity() == null ? bestHeight * 1.25f : getActivity().getHeight();
        viewOffsetY = Math.max(0, Math.min(viewOffsetY, totalDimensionY - viewDimensionY));

        startIndex = Math.max(0, (int) Math.floor(viewOffsetY / itemHeight));
        totalIndex = Math.max(0, (int) Math.ceil(viewDimensionExtraY / itemHeight)) + 1;
        endIndex = startIndex + totalIndex;

        viewBackOffsetY = -((viewOffsetY / itemHeight) % 1f) * itemHeight;

        if (items.size() < totalIndex) {
            createItem(totalIndex - items.size());
        }
        setViewOffsetX(getViewOffsetX());
        setViewOffsetY(getViewOffsetY());
        resetInvalidateItems();
    }

    private void createItem(int count) {
        if (adapter == null) {
            return;
        }
        for (int i = 0; i < count; i++) {
            Widget item = adapter.createListItem();
            items.add(item);
            super.add(item);
            item.applyStyle();
        }
    }

    public void refreshItems() {
        refreshItems(startIndex, totalIndex);
    }

    public void refreshItems(int start) {
        refreshItems(start, totalIndex);
    }

    public void refreshItems(int start, int length) {
        updateDimensions();
        int end = start + length;
        if (end < startIndex || start >= endIndex) {
            return;
        }
        if (start < startIndex) start = startIndex;
        if (end > endIndex) end = endIndex;

        for (int i = start; i < end; i++) {
            refreshItem(i);
        }
    }

    private void refreshItem(int index) {
        if (index >= startIndex && index < endIndex && index - startIndex < items.size()) {
            if (index < adapter.size()) {
                adapter.buildListItem(index, items.get(index - startIndex));
            } else {
                adapter.clearListItem(index, items.get(index - startIndex));
            }
        }
    }

    @Override
    protected boolean detachChild(Widget child) {
        if (items.contains(child)) {
            return false;
        }
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

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth = 0;
        float mHeight = 0;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        if (horizontalBar != null) {
            horizontalBar.onMeasure();
        }
        if (verticalBar != null) {
            verticalBar.onMeasure();
        }

        for (Widget child : items) {
            child.onMeasure();

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
        }

        if (wrapWidth) {
            mWidth = Math.max(mWidth + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            int items = adapter == null ? 0 : adapter.size();
            mHeight = Math.max(items * itemHeight + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);

        float localDimensionX = 0;
        for (int i = 0; i < items.size(); i++) {
            var child = items.get(i);

            if (child.getMeasureWidth() != MATCH_PARENT) {
                localDimensionX = Math.max(localDimensionX, child.getMeasureWidth());
            } else {
                localDimensionX = Math.max(localDimensionX, child.getLayoutMinWidth());
            }
        }
        viewDimensionX = getInWidth();
        totalDimensionX = Math.max(viewDimensionX, localDimensionX);

        for (int i = 0; i < items.size(); i++) {
            var child = items.get(i);

            float childWidth = Math.min(Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth()), totalDimensionX);
            child.onLayout(childWidth, itemHeight);
            child.setLayoutPosition(getInX() - viewOffsetX, getInY() + itemHeight * i + viewBackOffsetY);
        }

        if (verticalBar != null) {
            float childWidth;
            if (verticalBar.getMeasureWidth() == MATCH_PARENT) {
                childWidth = Math.min(getInWidth(), verticalBar.getLayoutMaxWidth());
            } else {
                childWidth = Math.min(verticalBar.getMeasureWidth(), verticalBar.getLayoutMaxWidth());
            }

            float childHeight;
            if (verticalBar.getMeasureHeight() == MATCH_PARENT) {
                childHeight = Math.min(getInHeight(), verticalBar.getLayoutMaxHeight());
            } else {
                childHeight = Math.min(verticalBar.getMeasureHeight(), verticalBar.getLayoutMaxHeight());
            }
            verticalBar.onLayout(childWidth, childHeight);
            verticalBar.setLayoutPosition(getInX() + getInWidth() - verticalBar.getLayoutWidth(), getInY());

            verticalBar.setViewOffsetListener(null);
            verticalBar.setSlideListener(null);
            verticalBar.setViewDimension(viewDimensionY);
            verticalBar.setTotalDimension(totalDimensionY);
            verticalBar.setSlideFilter(slideY);
        }

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
            float barY = (verticalBar != null && viewDimensionY < totalDimensionY ? verticalBar.getLayoutWidth() : 0);
            horizontalBar.onLayout(childWidth - barY, childHeight);
            horizontalBar.setLayoutPosition(getInX(), getInY() + getInHeight() - horizontalBar.getLayoutHeight());

            horizontalBar.setViewOffsetListener(null);
            horizontalBar.setSlideListener(null);
            horizontalBar.setViewDimension(viewDimensionX);
            horizontalBar.setTotalDimension(totalDimensionX);
            horizontalBar.setSlideFilter(slideX);
        }

        invalidateItems();
    }

    @Override
    public void onDraw(SmartContext context) {
        if (getInWidth() <= 0 || getInHeight() <= 0) return;

        drawBackground(context);
        drawRipple(context);

        float maxPos = getInY() + getInHeight();
        Shape oldClip = backgroundClip(context);
        for (Widget child : items) {
            child.onDraw(context);
            if (child.getLayoutY() > maxPos) {
                break;
            }
        }
        context.setTransform2D(null);
        context.setClip(oldClip);

        if (verticalBar != null && verticalBar.getViewDimension() < verticalBar.getTotalDimension()) {
            verticalBar.onDraw(context);
        }

        if (horizontalBar != null && horizontalBar.getViewDimension() < horizontalBar.getTotalDimension()) {
            horizontalBar.onDraw(context);
        }
    }

    @Override
    public void fireScroll(ScrollEvent event) {
        super.fireScroll(event);
        if (!event.isConsumed()) {
            slideVertical(- event.getDeltaY() * scrollSensibility);
        }
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

        float old = getViewOffsetX();
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

        float old = getViewOffsetY();
        if (offsetY != old && filterSlideY(offsetY)) {
            setViewOffsetY(offsetY);
            fireSlideY();
        }
    }

    public void slideVertical(float offsetY) {
        slideVerticalTo(getViewOffsetY() + offsetY);
    }

    public ListViewAdapter<?> getAdapter() {
        return adapter;
    }

    public void setAdapter(ListViewAdapter<?> adapter) {
        if (this.adapter != adapter) {
            var old = this.adapter;
            this.adapter = adapter;
            if (old != null) {
                old.setListView(null);
                items.clear();
                removeAll();
            }
            if (this.adapter != null) {
                this.adapter.setListView(this);
            }
            refreshItems();
        }
    }

    public float getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(float itemHeight) {
        if (itemHeight < 8 || itemHeight == MATCH_PARENT) itemHeight = 8;

        if (this.itemHeight != itemHeight) {
            this.itemHeight = itemHeight;
            invalidate(true);
            updateDimensions();
        }
    }

    public float getViewOffsetY() {
        return viewOffsetY;
    }

    public void setViewOffsetY(float viewOffsetY) {
        viewOffsetY = Math.max(0, Math.min(viewOffsetY, totalDimensionY - viewDimensionY));

        if (this.viewOffsetY != viewOffsetY) {
            float old = this.viewOffsetY;
            this.viewOffsetY = viewOffsetY;
            invalidate(true);
            invalidateItems();
            fireViewOffsetYListener(old);
            if (verticalBar != null) {
                verticalBar.setViewOffsetListener(null);
                verticalBar.setViewOffset(this.viewOffsetY);
            }
        }
    }

    public float getViewOffsetX() {
        return viewOffsetX;
    }

    public void setViewOffsetX(float viewOffsetX) {
        viewOffsetX = Math.max(0, Math.min(viewOffsetX, totalDimensionX - viewDimensionX));

        if (this.viewOffsetX != viewOffsetX) {
            float old = this.viewOffsetX;
            this.viewOffsetX = viewOffsetX;
            invalidate(true);
            // invalidateItems();
            fireViewOffsetXListener(old);
            if (horizontalBar != null) {
                horizontalBar.setViewOffsetListener(null);
                horizontalBar.setViewOffset(this.viewOffsetX);
            }
        }
    }

    public float getViewDimensionY() {
        return viewDimensionY;
    }

    public float getTotalDimensionY() {
        return totalDimensionY;
    }

    public float getViewDimensionX() {
        return viewDimensionX;
    }

    public float getTotalDimensionX() {
        return totalDimensionX;
    }

    public float getScrollSensibility() {
        return scrollSensibility;
    }

    public void setScrollSensibility(float scrollSensibility) {
        this.scrollSensibility = scrollSensibility;
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

    public UXListener<SlideEvent> getSlideHorizontalListener() {
        return slideHorizontalListener;
    }

    public void setSlideHorizontalListener(UXListener<SlideEvent> slideHorizontalListener) {
        this.slideHorizontalListener = slideHorizontalListener;
    }

    private void fireSlideX() {
        if (slideHorizontalListener != null) {
            UXListener.safeHandle(slideHorizontalListener, new SlideEvent(this, viewOffsetX));
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
            UXListener.safeHandle(slideVerticalListener, new SlideEvent(this, viewOffsetY));
        }
    }

    public UXValueListener<Float> getViewOffsetXListener() {
        return viewOffsetXListener;
    }

    public void setViewOffsetXListener(UXValueListener<Float> viewOffsetXListener) {
        this.viewOffsetXListener = viewOffsetXListener;
    }

    private void fireViewOffsetXListener(float old) {
        if (viewOffsetXListener != null && old != viewOffsetX) {
            UXValueListener.safeHandle(viewOffsetXListener, new ValueChange<>(this, old, viewOffsetX));
        }
    }

    public UXValueListener<Float> getViewOffsetYListener() {
        return viewOffsetYListener;
    }

    public void setViewOffsetYListener(UXValueListener<Float> viewOffsetYListener) {
        this.viewOffsetYListener = viewOffsetYListener;
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
            var event = new SlideEvent(this, viewOffsetX);
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
            var event = new SlideEvent(this, viewOffsetY);
            UXListener.safeHandle(slideVerticalFilter, event);
            return !event.isConsumed();
        }
        return true;
    }

    private class RefreshAnimation extends NormalizedAnimation {
        @Override
        public Activity getSource() {
            return getActivity();
        }

        @Override
        protected void compute(float t) {
            int prevStart = startIndex;
            int prevEnd = endIndex;
            updateDimensions();
            refreshItems(startIndex, endIndex);
        }
    }
}
