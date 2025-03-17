package flat.widget.structure;

import flat.animations.Interpolation;
import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.ScrollEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.math.Vector2;
import flat.math.shapes.RoundRectangle;
import flat.math.stroke.BasicStroke;
import flat.uxml.*;
import flat.widget.Children;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enums.*;
import flat.window.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabView extends Parent {

    UXListener<ActionEvent> slideListener;

    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;
    private VerticalAlign tabsVerticalAlign = VerticalAlign.TOP;
    private HorizontalAlign tabsHorizontalAlign = HorizontalAlign.LEFT;
    private VerticalPosition tabsVerticalPosition = VerticalPosition.TOP;
    private float scrollSensibility = 10;

    private Widget content;
    private Tab selectedTab;
    private final ArrayList<Tab> tabs;
    private final List<Tab> unmodifiableTabs;

    private boolean hiddenTabs;
    private float lineWidth;
    private int lineColor;
    private LineCap lineCap = LineCap.BUTT;
    private float lineAnimationDuration;

    private float tabsPrefHeight;
    private float tabsElevation;
    private int tabsBgColor;
    private float tabsHeight;

    private float viewOffset;
    private float viewDimension;
    private float totalDimension;

    private Tab target;
    private float px1, px2;
    private LineChange lineChange = new LineChange(Interpolation.fade);

    private RoundRectangle clipShape = new RoundRectangle();

    public TabView() {
        tabs = new ArrayList<>();
        unmodifiableTabs = Collections.unmodifiableList(tabs);
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        for (var child : children) {
            if (child.getWidget() instanceof Tab tab) {
                addTab(tab);
            }
        }
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
        setVerticalAlign(attrs.getConstant("vertical-align", info, getVerticalAlign()));
        setTabsHorizontalAlign(attrs.getConstant("tabs-horizontal-align", info, getTabsHorizontalAlign()));
        setTabsVerticalAlign(attrs.getConstant("tabs-vertical-align", info, getTabsVerticalAlign()));
        setTabsVerticalPosition(attrs.getConstant("tabs-vertical-position", info, getTabsVerticalPosition()));
        setTabsPrefHeight(attrs.getSize("tabs-pref-height", info, getTabsPrefHeight()));
        setTabsElevation(attrs.getSize("tabs-elevation", info, getTabsElevation()));
        setTabsBgColor(attrs.getColor("tabs-bg-color", info, getTabsBgColor()));
        setHiddenTabs(attrs.getBool("hidden-tabs", info, isHiddenTabs()));
        setLineWidth(attrs.getSize("line-width", info, getLineWidth()));
        setLineColor(attrs.getColor("line-color", info, getLineColor()));
        setLineCap(attrs.getConstant("line-cap", info, getLineCap()));
        setLineAnimationDuration(attrs.getNumber("line-animation-duration", info, getLineAnimationDuration()));
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

        for (Widget child : tabs) {
            if (child.getVisibility() == Visibility.GONE) continue;

            child.onMeasure();

            if (isHiddenTabs()) continue;

            if (wrapWidth) {
                mWidth += getDefWidth(child);
            }
            if (wrapHeight) {
                mHeight = Math.max(Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight()), mHeight);
            }
        }

        if (content != null && content.getVisibility() != Visibility.GONE) {
            content.onMeasure();
            if (wrapWidth) {
                mWidth = Math.max(Math.min(content.getMeasureWidth(), content.getLayoutMaxWidth()), mWidth);
            }
            if (wrapHeight) {
                mHeight += Math.min(content.getMeasureHeight(), content.getLayoutMaxHeight());
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
        float prevViewOffset = getViewOffset();

        if (isHiddenTabs()) {
            this.tabsHeight = 0;
            totalDimension = 0;
            viewDimension = 0;
            if (content != null) {
                performSingleLayoutConstraints(getInWidth(), getInHeight(), getInX(), getInY()
                        , content, verticalAlign, horizontalAlign);
            }

            if (prevViewOffset != getViewOffset() && getActivity() != null) {
                getActivity().runLater(() -> setViewOffset(getViewOffset()));
            }
            return;
        }

        float inHeight = getInHeight();

        float contentMinHeight = content == null ? 0 : content.getLayoutMinHeight();
        float contentHeight = content == null ? 0 : Math.min(content.getMeasureHeight(), content.getLayoutMaxHeight());
        contentHeight -= contentMinHeight;

        float tabsMinHeight = tabsPrefHeight == MATCH_PARENT ? 0 : tabsPrefHeight;
        float tabsHeight = Math.min(inHeight, tabsPrefHeight);

        float contentWeight = content == null ? 0 : content.getWeight();
        float tabsWeight = 0;
        int tabsCount = 0;

        for (Widget child : tabs) {
            if (child.getVisibility() == Visibility.GONE) continue;

            tabsCount++;
            tabsWeight += child.getWeight();
            tabsMinHeight = Math.max(tabsMinHeight, child.getLayoutMinHeight());
            if (tabsPrefHeight == WRAP_CONTENT) {
                tabsHeight = Math.max(tabsHeight, Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight()));
            }
        }
        tabsHeight -= tabsMinHeight;
        tabsWeight = tabsCount == 0 ? 0 : tabsWeight / tabsCount;
        float totalWeight = tabsWeight + contentWeight;

        float tabsTotalheight = 0;
        if (tabsMinHeight + contentMinHeight > inHeight) {
            tabsTotalheight = tabsMinHeight / (contentMinHeight + tabsMinHeight) * inHeight;
        } else {
            float extraHeight = inHeight - tabsMinHeight - contentMinHeight;
            if (contentHeight != MATCH_PARENT && tabsHeight != MATCH_PARENT) {
                if (contentHeight + tabsHeight <= extraHeight) {
                    tabsTotalheight = tabsMinHeight + tabsHeight;
                } else {
                    tabsTotalheight = tabsMinHeight + (extraHeight / 2f);
                }
            } else if (contentHeight == MATCH_PARENT && tabsHeight == MATCH_PARENT) {
                tabsTotalheight = tabsMinHeight + (extraHeight * (totalWeight == 0 ? 0.5f : tabsWeight / totalWeight));
            } else if (contentHeight == MATCH_PARENT) {
                tabsTotalheight = tabsMinHeight + Math.min(tabsHeight, extraHeight);
            } else {
                tabsTotalheight = tabsMinHeight + Math.max(0, extraHeight - contentHeight);
            }
        }

        float tabY = tabsVerticalPosition == VerticalPosition.BOTTOM ? height - tabsTotalheight : 0;
        float contentY = tabsVerticalPosition == VerticalPosition.BOTTOM ? 0 : tabsTotalheight;

        this.tabsHeight = tabsTotalheight;

        totalDimension = performLayoutHorizontalScrollable(getInWidth(), tabsTotalheight, getInX(), getInY() + tabY
                , (ArrayList) tabs, VerticalAlign.MIDDLE, horizontalAlign, getViewOffset());
        viewDimension = getInWidth();
        if (content != null) {
            performSingleLayoutConstraints(getInWidth(), height - tabsTotalheight, getInX(), getInY() + contentY
                    , content, verticalAlign, horizontalAlign);
        }

        if (target != selectedTab) {
            var old = target;
            target = selectedTab;
            if (target == null) {
                px1 = 0;
                px2 = 0;
            } else {
                if (old != null && tabs.contains(old)) {
                    px1 = old.getLayoutX();
                    px2 = old.getLayoutX() + old.getLayoutWidth();
                } else {
                    px1 = 0;
                    px2 = 0;
                }
            }
            if (lineAnimationDuration > 0 && getActivity() != null) {
                lineChange.play(getActivity());
            }
        }

        if (prevViewOffset != getViewOffset() && getActivity() != null) {
            getActivity().runLater(() -> setViewOffset(getViewOffset()));
        }
    }

    @Override
    public void onDraw(Graphics graphics) {
        drawBackground(graphics);
        drawRipple(graphics);

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        if (isHiddenTabs()) {
            if (content != null && content.getVisibility() == Visibility.VISIBLE) {
                content.onDraw(graphics);
            }
        } else {
            graphics.setTransform2D(getTransform());
            if (isTabsScrollable()) {
                if (tabsVerticalPosition == VerticalPosition.TOP) {
                    clipShape.x = x;
                    clipShape.y = y;
                    clipShape.width = width;
                    clipShape.height = tabsHeight;
                    clipShape.arcTop = getRadiusTop();
                    clipShape.arcRight = getRadiusRight();
                    clipShape.arcBottom = 0;
                    clipShape.arcLeft = 0;
                } else {
                    clipShape.x = x;
                    clipShape.y = y + height - tabsHeight;
                    clipShape.width = width;
                    clipShape.height = tabsHeight;
                    clipShape.arcTop = 0;
                    clipShape.arcRight = 0;
                    clipShape.arcBottom = getRadiusBottom();
                    clipShape.arcLeft = getRadiusLeft();
                }
                graphics.pushClip(clipShape);
            }

            if (Color.getAlpha(getTabsBgColor()) > 0) {
                graphics.setTransform2D(getTransform());
                graphics.setColor(getTabsBgColor());
                if (tabsVerticalPosition == VerticalPosition.TOP) {
                    graphics.drawRect(0, 0, width, tabsHeight, true);
                } else {
                    graphics.drawRect(0, y + height - tabsHeight, width, tabsHeight, true);
                }
            }

            for (Widget child : tabs) {
                if (child.getVisibility() == Visibility.VISIBLE) {
                    child.onDraw(graphics);
                }
            }

            if (selectedTab != null && Color.getAlpha(getLineColor()) > 0
                    && getLineWidth() > 0 && selectedTab.getLayoutWidth() > 0) {
                float lineW = Math.min(width, Math.min(height, getLineWidth()));
                graphics.setTransform2D(getTransform());
                graphics.setStroke(new BasicStroke(lineW, getLineCap().ordinal(), 0));
                graphics.setColor(getLineColor());
                float ty = tabsVerticalPosition == VerticalPosition.TOP
                        ? selectedTab.getLayoutY() + selectedTab.getLayoutHeight() - lineW * 0.5f
                        : selectedTab.getLayoutY() + lineW * 0.5f;
                float tx1 = selectedTab.getLayoutX();
                float tx2 = selectedTab.getLayoutX() + selectedTab.getLayoutWidth();
                if (lineChange.isPlaying() && (px1 != 0 || px2 != 0)) {
                    float t = lineChange.getInterpolatedPosition();
                    tx1 = Interpolation.mix(px1, tx1, t);
                    tx2 = Interpolation.mix(px2, tx2, t);
                }
                graphics.drawLine(tx1, ty, tx2, ty);
            }

            if (isTabsScrollable()) {
                graphics.popClip();
            }

            float el = Math.min(tabsElevation, height - tabsHeight);
            if (el >= 1) {
                graphics.setTransform2D(getTransform());
                if (tabsVerticalPosition == VerticalPosition.TOP) {
                    graphics.drawLinearShadowDown(0, tabsHeight, width, el, 0.55f / ((tabsElevation + 7) / 8));
                } else {
                    graphics.drawLinearShadowUp(0, y + height - tabsHeight - el, width, el, 0.55f / ((tabsElevation + 7) / 8));
                }
            }

            if (content != null && content.getVisibility() == Visibility.VISIBLE) {
                content.onDraw(graphics);
            }
        }
    }

    public void addTab(Tab child) {
        TaskList tasks = new TaskList();
        if (super.attachAndAddChild(child, tasks)) {
            tabs.add(child);
        }
        tasks.run();
        if (selectedTab == null) {
            selectTab(child);
        }
    }

    public void addTab(Tab... children) {
        for (Tab child : children) {
            addTab(child);
        }
    }

    public void addTab(List<Tab> children) {
        for (Tab child : children) {
            addTab(child);
        }
    }

    public void moveTab(Tab tab, int index) {
        if (tabs.contains(tab)) {
            tabs.remove(tab);
            tabs.add(index, tab);
        }
    }

    @Override
    protected boolean detachChild(Widget child) {
        if (child instanceof Tab tab) {
            if (tabs.contains(tab)) {
                return false;
            }
        }
        if (child == content) {
            return false;
        }
        return super.detachChild(child);
    }


    @Override
    public void scroll(ScrollEvent event) {
        super.scroll(event);
        if (!event.isConsumed() && isTabsScrollable() && !isHiddenTabs()) {
            Vector2 pos = screenToLocal(event.getX(), event.getY());
            if ((tabsVerticalPosition == VerticalPosition.TOP && pos.y < getInY() + tabsHeight) ||
                (tabsVerticalPosition == VerticalPosition.BOTTOM && pos.y > getInY() + getInHeight() - tabsHeight)) {
                slide(-event.getDeltaY() * scrollSensibility);
                event.consume();
            }
        }
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        if (this.lineWidth != lineWidth) {
            this.lineWidth = lineWidth;
            invalidate(false);
        }
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        if (this.lineColor != lineColor) {
            this.lineColor = lineColor;
            invalidate(false);
        }
    }

    public LineCap getLineCap() {
        return lineCap;
    }

    public void setLineCap(LineCap lineCap) {
        if (lineCap == null) lineCap = LineCap.BUTT;

        if (this.lineCap != lineCap) {
            this.lineCap = lineCap;
            invalidate(false);
        }
    }

    public float getLineAnimationDuration() {
        return lineAnimationDuration;
    }

    public void setLineAnimationDuration(float lineAnimationDuration) {
        if (this.lineAnimationDuration != lineAnimationDuration) {
            this.lineAnimationDuration = lineAnimationDuration;
            if (lineAnimationDuration > 0) {
                lineChange.setDuration(lineAnimationDuration);
            } else {
                lineChange.stop();
            }
        }
    }

    public boolean isTabsScrollable() {
        return viewDimension < totalDimension - 0.001f;
    }

    public void slideTo(float offset) {
        float old = getViewOffset();
        setViewOffset(offset);
        if (old != getViewOffset()) {
            fireSlide();
        }
    }

    public void slide(float offset) {
        slideTo(getViewOffset() + offset);
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

    public float getScrollSensibility() {
        return scrollSensibility;
    }

    public void setScrollSensibility(float scrollSensibility) {
        this.scrollSensibility = scrollSensibility;
    }

    public List<Tab> getUnmodifiableTabs() {
        return unmodifiableTabs;
    }

    public Children<Tab> getTabsIterable() {
        return new Children<>(tabs);
    }

    public Children<Tab> getTabsIterableReverse() {
        return new Children<>(tabs, true);
    }

    void setContent(Widget content) {
        if (this.content != content) {
            Widget currentContent = this.content;
            this.content = content;
            if (currentContent != null) {
                remove(currentContent);
            }
            if (this.content != null) {
                super.add(this.content);
            }
        }
    }

    public Widget getContent() {
        return content;
    }

    public boolean isHiddenTabs() {
        return hiddenTabs;
    }

    public void setHiddenTabs(boolean hiddenTabs) {
        if (this.hiddenTabs != hiddenTabs) {
            this.hiddenTabs = hiddenTabs;
            invalidate(true);
        }
    }

    public float getTabsElevation() {
        return tabsElevation;
    }

    public void setTabsElevation(float tabsElevation) {
        if (this.tabsElevation != tabsElevation) {
            this.tabsElevation = tabsElevation;
            invalidate(true);
        }
    }

    public int getTabsBgColor() {
        return tabsBgColor;
    }

    public void setTabsBgColor(int tabsBgColor) {
        if (this.tabsBgColor != tabsBgColor) {
            this.tabsBgColor = tabsBgColor;
            invalidate(true);
        }
    }

    public float getTabsPrefHeight() {
        return tabsPrefHeight;
    }

    public void setTabsPrefHeight(float tabsPrefHeight) {
        if (this.tabsPrefHeight != tabsPrefHeight) {
            this.tabsPrefHeight = tabsPrefHeight;
            invalidate(true);
        }
    }

    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(VerticalAlign verticalAlign) {
        if (verticalAlign == null) verticalAlign = VerticalAlign.TOP;

        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(true);
        }
    }

    public HorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
        if (horizontalAlign == null) horizontalAlign = HorizontalAlign.LEFT;

        if (this.horizontalAlign != horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            invalidate(true);
        }
    }

    public VerticalAlign getTabsVerticalAlign() {
        return tabsVerticalAlign;
    }

    public void setTabsVerticalAlign(VerticalAlign tabsVerticalAlign) {
        if (tabsVerticalAlign == null) tabsVerticalAlign = VerticalAlign.TOP;

        if (this.tabsVerticalAlign != tabsVerticalAlign) {
            this.tabsVerticalAlign = tabsVerticalAlign;
            invalidate(true);
        }
    }

    public HorizontalAlign getTabsHorizontalAlign() {
        return tabsHorizontalAlign;
    }

    public void setTabsHorizontalAlign(HorizontalAlign tabsHorizontalAlign) {
        if (tabsHorizontalAlign == null) tabsHorizontalAlign = HorizontalAlign.LEFT;

        if (this.tabsHorizontalAlign != tabsHorizontalAlign) {
            this.tabsHorizontalAlign = tabsHorizontalAlign;
            invalidate(true);
        }
    }

    public VerticalPosition getTabsVerticalPosition() {
        return tabsVerticalPosition;
    }

    public void setTabsVerticalPosition(VerticalPosition tabsVerticalPosition) {
        if (tabsVerticalPosition == null) tabsVerticalPosition = VerticalPosition.TOP;

        if (this.tabsVerticalPosition != tabsVerticalPosition) {
            this.tabsVerticalPosition = tabsVerticalPosition;
            invalidate(true);
        }
    }

    public float getTotalDimension() {
        return totalDimension;
    }

    public float getViewDimension() {
        return viewDimension;
    }

    public float getViewOffset() {
        return viewOffset;
    }

    public void setViewOffset(float viewOffset) {
        if (viewOffset > totalDimension - viewDimension) viewOffset = totalDimension - viewDimension;
        if (viewOffset < 0) viewOffset = 0;

        if (this.viewOffset != viewOffset) {
            float old = this.viewOffset;
            this.viewOffset = viewOffset;
            invalidate(true);
        }
    }

    public void selectTab(Tab tab) {
        if (tab != null && !tabs.contains(tab)) return;

        if (this.selectedTab != tab) {
            var old = this.selectedTab;
            this.selectedTab = tab;
            if (old != null) {
                old.refreshSelectedState();
            }
            if (this.selectedTab != null) {
                this.selectedTab.refreshSelectedState();
            }
            setContent(this.selectedTab == null ? null : this.selectedTab.getFrame());
            invalidate(true);
        }
    }

    public Tab getSelectedTab() {
        return selectedTab;
    }

    public void removeTab(Tab tab) {
        Tab toSelect = null;
        if (this.selectedTab == tab) {
            int index = tabs.indexOf(tab);
            if (index == 0 && tabs.size() > 1) {
                toSelect = tabs.get(1);
            } else if (index > 0) {
                toSelect = tabs.get(index - 1);
            }
            tabs.remove(tab);
            remove(tab);
            selectTab(toSelect);
        } else {
            tabs.remove(tab);
            remove(tab);
        }
        invalidate(true);
    }

    public void refreshTab(Tab tab) {
        if (this.selectedTab == tab) {
            setContent(this.selectedTab == null ? null : this.selectedTab.getFrame());
        }
    }

    private class LineChange extends NormalizedAnimation {
        public LineChange(Interpolation interpolation) {
            super(interpolation);
        }

        @Override
        public Activity getSource() {
            return getActivity();
        }

        @Override
        protected void compute(float t) {
            invalidate(false);
        }
    }
}
