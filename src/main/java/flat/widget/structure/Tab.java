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
import flat.widget.Group;
import flat.widget.Widget;
import flat.widget.enums.*;
import flat.window.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tab extends Group {

    UXListener<ActionEvent> slideListener;

    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;
    private VerticalAlign pagesVerticalAlign = VerticalAlign.TOP;
    private HorizontalAlign pagesHorizontalAlign = HorizontalAlign.LEFT;
    private VerticalPosition pagesVerticalPosition = VerticalPosition.TOP;
    private float scrollSensibility = 10;

    private Widget content;
    private Page selectedPage;
    private final ArrayList<Page> pages;
    private final List<Page> unmodifiablePages;

    private boolean hiddenPages;
    private float lineWidth;
    private int lineColor;
    private LineCap lineCap = LineCap.BUTT;
    private float lineAnimationDuration;

    private float pagesPrefHeight;
    private float pagesElevation;
    private int pagesBgColor;
    private float pagesHeight;

    private float viewOffset;
    private float viewDimension;
    private float totalDimension;

    private Page target;
    private float px1, px2;
    private LineChange lineChange = new LineChange(Interpolation.fade);

    private RoundRectangle clipShape = new RoundRectangle();

    public Tab() {
        pages = new ArrayList<>();
        unmodifiablePages = Collections.unmodifiableList(pages);
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        for (var child : children) {
            if (child.getWidget() instanceof Page page) {
                addPage(page);
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
        setPagesHorizontalAlign(attrs.getConstant("pages-horizontal-align", info, getPagesHorizontalAlign()));
        setPagesVerticalAlign(attrs.getConstant("pages-vertical-align", info, getPagesVerticalAlign()));
        setPagesVerticalPosition(attrs.getConstant("pages-vertical-position", info, getPagesVerticalPosition()));
        setPagesPrefHeight(attrs.getSize("pages-pref-height", info, getPagesPrefHeight()));
        setPagesElevation(attrs.getSize("pages-elevation", info, getPagesElevation()));
        setPagesBgColor(attrs.getColor("pages-bg-color", info, getPagesBgColor()));
        setHiddenPages(attrs.getBool("hidden-pages", info, isHiddenPages()));
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

        for (Widget child : pages) {
            if (child.getVisibility() == Visibility.GONE) continue;

            child.onMeasure();

            if (isHiddenPages()) continue;

            if (wrapWidth) {
                if (child.getMeasureWidth() == MATCH_PARENT) {
                    mWidth += Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth());
                } else {
                    mWidth += child.getMeasureWidth();
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

        if (content != null && content.getVisibility() != Visibility.GONE) {
            content.onMeasure();
            if (wrapWidth) {
                if (content.getMeasureWidth() == MATCH_PARENT) {
                    float mW = Math.min(content.getMeasureWidth(), content.getLayoutMaxWidth());
                    if (mW > mWidth) {
                        mWidth = mW;
                    }
                } else if (content.getMeasureWidth() > mWidth) {
                    mWidth = content.getMeasureWidth();
                }
            }
            if (wrapHeight) {
                if (content.getMeasureHeight() == MATCH_PARENT) {
                    mHeight += Math.min(content.getMeasureHeight(), content.getLayoutMaxHeight());
                } else {
                    mHeight += content.getMeasureHeight();
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
        float prevViewOffset = getViewOffset();

        if (isHiddenPages()) {
            this.pagesHeight = 0;
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

        float pagesMinHeight = pagesPrefHeight == MATCH_PARENT ? 0 : pagesPrefHeight;
        float pagesHeight = Math.min(inHeight, pagesPrefHeight);

        float contentWeight = content == null ? 0 : content.getWeight();
        float pagesWeight = 0;
        int pagesCount = 0;

        for (Widget child : pages) {
            if (child.getVisibility() == Visibility.GONE) continue;

            pagesCount++;
            pagesWeight += child.getWeight();
            pagesMinHeight = Math.max(pagesMinHeight, child.getLayoutMinHeight());
            if (pagesPrefHeight == WRAP_CONTENT) {
                pagesHeight = Math.max(pagesHeight, Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight()));
            }
        }
        pagesHeight -= pagesMinHeight;
        pagesWeight = pagesCount == 0 ? 0 : pagesWeight / pagesCount;
        float totalWeight = pagesWeight + contentWeight;

        float pagesTotalheight = 0;
        if (pagesMinHeight + contentMinHeight > inHeight) {
            pagesTotalheight = pagesMinHeight / (contentMinHeight + pagesMinHeight) * inHeight;
        } else {
            float extraHeight = inHeight - pagesMinHeight - contentMinHeight;
            if (contentHeight != MATCH_PARENT && pagesHeight != MATCH_PARENT) {
                if (contentHeight + pagesHeight <= extraHeight) {
                    pagesTotalheight = pagesMinHeight + pagesHeight;
                } else {
                    pagesTotalheight = pagesMinHeight + (extraHeight / 2f);
                }
            } else if (contentHeight == MATCH_PARENT && pagesHeight == MATCH_PARENT) {
                pagesTotalheight = pagesMinHeight + (extraHeight * (totalWeight == 0 ? 0.5f : pagesWeight / totalWeight));
            } else if (contentHeight == MATCH_PARENT) {
                pagesTotalheight = pagesMinHeight + Math.min(pagesHeight, extraHeight);
            } else {
                pagesTotalheight = pagesMinHeight + Math.max(0, extraHeight - contentHeight);
            }
        }

        float pageY = pagesVerticalPosition == VerticalPosition.BOTTOM ? height - pagesTotalheight : 0;
        float contentY = pagesVerticalPosition == VerticalPosition.BOTTOM ? 0 : pagesTotalheight;

        this.pagesHeight = pagesTotalheight;

        totalDimension = performLayoutHorizontalScrollable(getInWidth(), pagesTotalheight, getInX(), getInY() + pageY
                , (ArrayList) pages, VerticalAlign.MIDDLE, horizontalAlign, getViewOffset());
        viewDimension = getInWidth();
        if (content != null) {
            performSingleLayoutConstraints(getInWidth(), height - pagesTotalheight, getInX(), getInY() + contentY
                    , content, verticalAlign, horizontalAlign);
        }

        if (target != selectedPage) {
            var old = target;
            target = selectedPage;
            if (target == null) {
                px1 = 0;
                px2 = 0;
            } else {
                if (old != null && pages.contains(old)) {
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

        if (isHiddenPages()) {
            if (content != null && content.getVisibility() == Visibility.VISIBLE) {
                content.onDraw(graphics);
            }
        } else {
            graphics.setTransform2D(getTransform());
            if (isPagesScrollable()) {
                if (pagesVerticalPosition == VerticalPosition.TOP) {
                    clipShape.x = x;
                    clipShape.y = y;
                    clipShape.width = width;
                    clipShape.height = pagesHeight;
                    clipShape.arcTop = getRadiusTop();
                    clipShape.arcRight = getRadiusRight();
                    clipShape.arcBottom = 0;
                    clipShape.arcLeft = 0;
                } else {
                    clipShape.x = x;
                    clipShape.y = y + height - pagesHeight;
                    clipShape.width = width;
                    clipShape.height = pagesHeight;
                    clipShape.arcTop = 0;
                    clipShape.arcRight = 0;
                    clipShape.arcBottom = getRadiusBottom();
                    clipShape.arcLeft = getRadiusLeft();
                }
                graphics.pushClip(clipShape);
            }

            if (Color.getAlpha(getPagesBgColor()) > 0) {
                graphics.setTransform2D(getTransform());
                graphics.setColor(getPagesBgColor());
                if (pagesVerticalPosition == VerticalPosition.TOP) {
                    graphics.drawRect(0, 0, width, pagesHeight, true);
                } else {
                    graphics.drawRect(0, y + height - pagesHeight, width, pagesHeight, true);
                }
            }

            for (Widget child : pages) {
                if (child.getVisibility() == Visibility.VISIBLE) {
                    child.onDraw(graphics);
                }
            }

            if (selectedPage != null && Color.getAlpha(getLineColor()) > 0
                    && getLineWidth() > 0 && selectedPage.getLayoutWidth() > 0) {
                float lineW = Math.min(width, Math.min(height, getLineWidth()));
                graphics.setTransform2D(getTransform());
                graphics.setStroker(new BasicStroke(lineW, getLineCap().ordinal(), 0));
                graphics.setColor(getLineColor());
                float ty = pagesVerticalPosition == VerticalPosition.TOP
                        ? selectedPage.getLayoutY() + selectedPage.getLayoutHeight() - lineW * 0.5f
                        : selectedPage.getLayoutY() + lineW * 0.5f;
                float tx1 = selectedPage.getLayoutX();
                float tx2 = selectedPage.getLayoutX() + selectedPage.getLayoutWidth();
                if (lineChange.isPlaying() && (px1 != 0 || px2 != 0)) {
                    float t = lineChange.getInterpolatedPosition();
                    tx1 = Interpolation.mix(px1, tx1, t);
                    tx2 = Interpolation.mix(px2, tx2, t);
                }
                graphics.drawLine(tx1, ty, tx2, ty);
            }

            if (isPagesScrollable()) {
                graphics.popClip();
            }

            float el = Math.min(pagesElevation, height - pagesHeight);
            if (el >= 1) {
                graphics.setTransform2D(getTransform());
                if (pagesVerticalPosition == VerticalPosition.TOP) {
                    graphics.drawLinearShadowDown(0, pagesHeight, width, el, 0.55f / ((pagesElevation + 7) / 8));
                } else {
                    graphics.drawLinearShadowUp(0, y + height - pagesHeight - el, width, el, 0.55f / ((pagesElevation + 7) / 8));
                }
            }

            if (content != null && content.getVisibility() == Visibility.VISIBLE) {
                content.onDraw(graphics);
            }
        }
    }

    public void addPage(Page child) {
        TaskList tasks = new TaskList();
        if (super.attachAndAddChild(child, tasks)) {
            pages.add(child);
        }
        tasks.run();
        if (selectedPage == null) {
            selectPage(child);
        }
    }

    public void addPage(Page... children) {
        for (Page child : children) {
            addPage(child);
        }
    }

    public void addPage(List<Page> children) {
        for (Page child : children) {
            addPage(child);
        }
    }

    public void movePage(Page page, int index) {
        if (pages.contains(page)) {
            pages.remove(page);
            pages.add(index, page);
        }
    }

    @Override
    protected boolean detachChild(Widget child) {
        if (child instanceof Page page) {
            if (pages.contains(page)) {
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
        if (!event.isConsumed() && isPagesScrollable() && !isHiddenPages()) {
            Vector2 pos = screenToLocal(event.getX(), event.getY());
            if ((pagesVerticalPosition == VerticalPosition.TOP && pos.y < getInY() + pagesHeight) ||
                (pagesVerticalPosition == VerticalPosition.BOTTOM && pos.y > getInY() + getInHeight() - pagesHeight)) {
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

    public boolean isPagesScrollable() {
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

    public List<Page> getUnmodifiablePages() {
        return unmodifiablePages;
    }

    public Children<Page> getPagesIterable() {
        return new Children<>(pages);
    }

    public Children<Page> getPagesIterableReverse() {
        return new Children<>(pages, true);
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

    public boolean isHiddenPages() {
        return hiddenPages;
    }

    public void setHiddenPages(boolean hiddenPages) {
        if (this.hiddenPages != hiddenPages) {
            this.hiddenPages = hiddenPages;
            invalidate(true);
        }
    }

    public float getPagesElevation() {
        return pagesElevation;
    }

    public void setPagesElevation(float pagesElevation) {
        if (this.pagesElevation != pagesElevation) {
            this.pagesElevation = pagesElevation;
            invalidate(true);
        }
    }

    public int getPagesBgColor() {
        return pagesBgColor;
    }

    public void setPagesBgColor(int pagesBgColor) {
        if (this.pagesBgColor != pagesBgColor) {
            this.pagesBgColor = pagesBgColor;
            invalidate(true);
        }
    }

    public float getPagesPrefHeight() {
        return pagesPrefHeight;
    }

    public void setPagesPrefHeight(float pagesPrefHeight) {
        if (this.pagesPrefHeight != pagesPrefHeight) {
            this.pagesPrefHeight = pagesPrefHeight;
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

    public VerticalAlign getPagesVerticalAlign() {
        return pagesVerticalAlign;
    }

    public void setPagesVerticalAlign(VerticalAlign pagesVerticalAlign) {
        if (pagesVerticalAlign == null) pagesVerticalAlign = VerticalAlign.TOP;

        if (this.pagesVerticalAlign != pagesVerticalAlign) {
            this.pagesVerticalAlign = pagesVerticalAlign;
            invalidate(true);
        }
    }

    public HorizontalAlign getPagesHorizontalAlign() {
        return pagesHorizontalAlign;
    }

    public void setPagesHorizontalAlign(HorizontalAlign pagesHorizontalAlign) {
        if (pagesHorizontalAlign == null) pagesHorizontalAlign = HorizontalAlign.LEFT;

        if (this.pagesHorizontalAlign != pagesHorizontalAlign) {
            this.pagesHorizontalAlign = pagesHorizontalAlign;
            invalidate(true);
        }
    }

    public VerticalPosition getPagesVerticalPosition() {
        return pagesVerticalPosition;
    }

    public void setPagesVerticalPosition(VerticalPosition pagesVerticalPosition) {
        if (pagesVerticalPosition == null) pagesVerticalPosition = VerticalPosition.TOP;

        if (this.pagesVerticalPosition != pagesVerticalPosition) {
            this.pagesVerticalPosition = pagesVerticalPosition;
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

    public void selectPage(Page page) {
        if (page != null && !pages.contains(page)) return;

        if (this.selectedPage != page) {
            var old = this.selectedPage;
            this.selectedPage = page;
            if (old != null) {
                old.refreshSelectedState();
            }
            if (this.selectedPage != null) {
                this.selectedPage.refreshSelectedState();
            }
            setContent(this.selectedPage == null ? null : this.selectedPage.getFrame());
            invalidate(true);
        }
    }

    public Page getSelectedPage() {
        return selectedPage;
    }

    public void removePage(Page page) {
        Page toSelect = null;
        if (this.selectedPage == page) {
            int index = pages.indexOf(page);
            if (index == 0 && pages.size() > 1) {
                toSelect = pages.get(1);
            } else if (index > 0) {
                toSelect = pages.get(index - 1);
            }
            pages.remove(page);
            remove(page);
            selectPage(toSelect);
        } else {
            pages.remove(page);
            remove(page);
        }
        invalidate(true);
    }

    public void refreshPage(Page page) {
        if (this.selectedPage == page) {
            setContent(this.selectedPage == null ? null : this.selectedPage.getFrame());
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
