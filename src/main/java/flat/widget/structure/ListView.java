package flat.widget.structure;

import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.ScrollEvent;
import flat.events.SlideEvent;
import flat.graphics.SmartContext;
import flat.math.Vector2;
import flat.math.shapes.Shape;
import flat.uxml.*;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enums.Visibility;
import flat.widget.layout.Scrollable;
import flat.widget.value.HorizontalScrollBar;
import flat.widget.value.VerticalScrollBar;
import flat.window.Activity;

import java.util.ArrayList;

public class ListView extends Scrollable {

    private final ArrayList<Widget> items = new ArrayList<>();

    private ListViewAdapter<?> adapter;
    private float itemHeight = 8;

    private float viewDimensionExtraY;
    private float viewBackOffsetY;

    private int startIndex;
    private int endIndex;
    private int totalIndex;

    private RefreshAnimation refresh = new RefreshAnimation();

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
        int count = getRealItemsCount();

        float bestHeight = getInHeight() > 0 ? getInHeight()
                : getHeight() > 0 ? getHeight()
                : getPrefHeight() > 0 && getPrefHeight() != MATCH_PARENT ? getPrefHeight()
                : 16;
        bestHeight = Math.max(bestHeight, 16);
        setTotalDimensionY(itemHeight * count);
        viewDimensionExtraY = getActivity() == null ? bestHeight * 1.25f : getActivity().getHeight();

        startIndex = Math.max(0, (int) Math.floor((getViewOffsetY() - getInY()) / itemHeight));
        totalIndex = Math.max(0, (int) Math.ceil(viewDimensionExtraY / itemHeight)) + 1;
        endIndex = startIndex + totalIndex;
        if (endIndex > count) {
            endIndex = count;
            totalIndex = endIndex - startIndex;
        }

        viewBackOffsetY = -((getViewOffsetY() / itemHeight) % 1f) * itemHeight;

        if (items.size() < totalIndex) {
            createItem(totalIndex - items.size());
        } else if (items.size() > totalIndex) {
            destroyItem(items.size() - totalIndex);
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

    private void destroyItem(int count) {
        for (int i = 0; i < count; i++) {
            remove(items.remove(items.size() - 1));
        }
    }

    public void refreshItems() {
        refreshItems(startIndex, -1);
    }

    public void refreshItems(int start) {
        refreshItems(start, -1);
    }

    public void refreshItems(int start, int length) {
        updateDimensions();
        if (length == -1) {
            length = totalIndex;
        }
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
            }
        }
    }

    @Override
    protected boolean detachChild(Widget child) {
        if (items.contains(child)) {
            return false;
        }
        return super.detachChild(child);
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth = 0;
        float mHeight = 0;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        if (getHorizontalBar() != null) {
            getHorizontalBar().onMeasure();
        }
        if (getVerticalBar() != null) {
            getVerticalBar().onMeasure();
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
    public Vector2 onLayoutLocalDimension(float width, float height) {
        float localDimensionX = 0;
        for (int i = 0; i < items.size(); i++) {
            var child = items.get(i);

            if (child.getMeasureWidth() != MATCH_PARENT) {
                localDimensionX = Math.max(localDimensionX, child.getMeasureWidth());
            } else {
                localDimensionX = Math.max(localDimensionX, child.getLayoutMinWidth());
            }
        }
        return new Vector2(localDimensionX, itemHeight * getRealItemsCount());
    }

    @Override
    public void setLayoutScrollOffset(float xx, float yy) {
        for (int i = 0; i < items.size(); i++) {
            var child = items.get(i);

            float childWidth = Math.min(Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth()), getTotalDimensionX());
            child.onLayout(childWidth, itemHeight);
            child.setLayoutPosition(xx, yy + itemHeight * (i + startIndex));
        }
    }

    @Override
    public void onLayout(float width, float height) {
        super.onLayout(width, height);

        invalidateItems();
    }

    @Override
    public void onDraw(SmartContext context) {
        drawBackground(context);
        drawRipple(context);

        if (getInWidth() <= 0 || getInHeight() <= 0) return;

        int realCount = getRealItemsCount();

        context.pushClip(getBackgroundShape());
        for (int i = 0; i < items.size(); i++) {
            items.get(i).onDraw(context);
        }
        context.popClip();

        if (getHorizontalBar() != null && isHorizontalVisible()) {
            getHorizontalBar().onDraw(context);
        }

        if (getVerticalBar() != null && isVerticalVisible()) {
            getVerticalBar().onDraw(context);
        }
    }

    @Override
    public void fireScroll(ScrollEvent event) {
        super.fireScroll(event);
        if (!event.isConsumed()) {
            slideVertical(- event.getDeltaY() * getScrollSensibility());
        }
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

    private int getRealItemsCount() {
        return adapter == null ? 0 : adapter.size();
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
