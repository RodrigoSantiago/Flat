package flat.widget.structure;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.ScrollEvent;
import flat.graphics.SmartContext;
import flat.math.Vector2;
import flat.math.shapes.RoundRectangle;
import flat.math.shapes.Shape;
import flat.uxml.*;
import flat.widget.Children;
import flat.widget.Group;
import flat.widget.Widget;
import flat.widget.enums.*;

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

    private float pagesPrefHeight;
    private float pagesHeight;

    private float viewOffset;
    private float viewDimension;
    private float totalDimension;

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
        setPagesPrefHeight(attrs.getSize("pages-preft-height", info, getPagesPrefHeight()));
        setScrollSensibility(attrs.getNumber("scroll-sensibilityt", info, getScrollSensibility()));
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

        float contentTotalheight = 0;
        float pagesTotalheight = 0;
        if (pagesMinHeight + contentMinHeight > inHeight) {
            contentTotalheight = contentMinHeight / (contentMinHeight + pagesMinHeight) * inHeight;
            pagesTotalheight = pagesMinHeight / (contentMinHeight + pagesMinHeight) * inHeight;
        } else {
            float extraHeight = inHeight - pagesMinHeight - contentMinHeight;
            if (contentHeight != MATCH_PARENT && pagesHeight != MATCH_PARENT) {
                if (contentHeight + pagesHeight <= extraHeight) {
                    contentTotalheight = contentMinHeight + contentHeight;
                    pagesTotalheight = pagesMinHeight + pagesHeight;
                } else {
                    contentTotalheight = contentMinHeight + (extraHeight / 2f);
                    pagesTotalheight = pagesMinHeight + (extraHeight / 2f);
                }
            } else if (contentHeight == MATCH_PARENT && pagesHeight == MATCH_PARENT) {
                contentTotalheight = contentMinHeight + (extraHeight * (totalWeight == 0 ? 0.5f : contentWeight / totalWeight));
                pagesTotalheight = pagesMinHeight + (extraHeight * (totalWeight == 0 ? 0.5f : pagesWeight / totalWeight));
            } else if (contentHeight == MATCH_PARENT) {
                contentTotalheight = contentMinHeight + Math.max(0, extraHeight - pagesHeight);
                pagesTotalheight = pagesMinHeight + Math.min(pagesHeight, extraHeight);
            } else {
                contentTotalheight = contentMinHeight + Math.min(contentHeight, extraHeight);
                pagesTotalheight = pagesMinHeight + Math.max(0, extraHeight - contentHeight);
            }
        }

        float pageY = pagesVerticalPosition == VerticalPosition.BOTTOM ? contentTotalheight : 0;
        float contentY = pagesVerticalPosition == VerticalPosition.BOTTOM ? 0 : pagesTotalheight;

        this.pagesHeight = pagesTotalheight;

        totalDimension = performLayoutHorizontalScrollable(getInWidth(), pagesTotalheight, getInX(), getInY() + pageY
                , (ArrayList) pages, VerticalAlign.MIDDLE, horizontalAlign, getViewOffset());
        viewDimension = getInWidth();
        setViewOffset(getViewOffset());
        if (content != null) {
            performSingleLayoutConstraints(getInWidth(), contentTotalheight, getInX(), getInY() + contentY
                    , content, verticalAlign, horizontalAlign);
        }
    }

    @Override
    public boolean onLayoutSingleChild(Widget child) {
        return false;
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

    RoundRectangle clipShape = new RoundRectangle();
    @Override
    public void onDraw(SmartContext context) {
        if (getInWidth() <= 0 || getInHeight() <= 0) return;

        drawBackground(context);
        drawRipple(context);

        if (isPagesScrollable()) {
            if (pagesVerticalPosition == VerticalPosition.TOP) {
                clipShape.x = getInX();
                clipShape.y = getInY();
                clipShape.width = getInWidth();
                clipShape.height = pagesHeight;
                clipShape.arcTop = getRadiusTop();
                clipShape.arcRight = getRadiusRight();
                clipShape.arcBottom = 0;
                clipShape.arcLeft = 0;
            } else {
                clipShape.x = getInX();
                clipShape.y = getInY() + getInHeight() - pagesHeight;
                clipShape.width = getInWidth();
                clipShape.height = pagesHeight;
                clipShape.arcTop = 0;
                clipShape.arcRight = 0;
                clipShape.arcBottom = getRadiusBottom();
                clipShape.arcLeft = getRadiusLeft();
            }

            context.setTransform2D(getTransform());
            context.pushClip(clipShape);
            for (Widget child : pages) {
                if (child.getVisibility() == Visibility.VISIBLE) {
                    child.onDraw(context);
                }
            }
            context.popClip();
            if (content != null && content.getVisibility() == Visibility.VISIBLE) {
                content.onDraw(context);
            }
        } else {
            drawChildren(context);
        }
    }


    @Override
    public void fireScroll(ScrollEvent event) {
        super.fireScroll(event);
        if (!event.isConsumed()) {
            Vector2 pos = screenToLocal(event.getX(), event.getY());
            if ((pagesVerticalPosition == VerticalPosition.TOP && pos.y < getInY() + pagesHeight) ||
                (pagesVerticalPosition == VerticalPosition.BOTTOM && pos.y > getInY() + getInHeight() - pagesHeight)) {
                slide(-event.getDeltaY() * scrollSensibility);
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
            if (this.selectedPage != null) {
                this.selectedPage.refreshSelectedState();
            }
            this.selectedPage = page;
            if (this.selectedPage != null) {
                this.selectedPage.refreshSelectedState();
            }
            setContent(this.selectedPage == null ? null : this.selectedPage.getFrame());
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
    }

    public void refreshPage(Page page) {
        if (this.selectedPage == page) {
            setContent(this.selectedPage == null ? null : this.selectedPage.getFrame());
        }
    }
}
