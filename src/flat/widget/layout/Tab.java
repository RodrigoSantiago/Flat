package flat.widget.layout;

import flat.animations.Interpolation;
import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.events.ScrollEvent;
import flat.graphics.SmartContext;
import flat.graphics.text.Align;
import flat.math.Vector2;
import flat.math.shapes.Shape;
import flat.uxml.*;
import flat.widget.Activity;
import flat.widget.Gadget;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enuns.Visibility;
import flat.widget.text.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Tab extends Parent {

    private float headerHeight;
    private float headerElevation;
    private float headerOffset;
    private float headerScroll;
    private int headerColor;
    private int headerIndicatorColor;
    private boolean headerScrollable;
    private float headerIndicatorHeight;

    private float headerWidth;
    private boolean headerHovered;

    private int activePage = -1;
    private int showLeft, showRight;
    private ArrayList<Page> pages;
    private ArrayList<TabLabel> labels;
    private List<Page> unmodifiablePages;

    private NormalizedAnimation anim = new NormalizedAnimation() {
        @Override
        protected void compute(float t) {
            if (t == 1 && showLeft != -1 && showRight != -1) {
                if (showLeft == activePage) {
                    pages.get(showLeft).setActivated(true);
                    pages.get(showRight).setActivated(false);
                    labels.get(showLeft).setActivated(true);
                    labels.get(showRight).setActivated(false);
                } else {
                    pages.get(showLeft).setActivated(false);
                    pages.get(showRight).setActivated(true);
                    labels.get(showLeft).setActivated(false);
                    labels.get(showRight).setActivated(true);
                }
                showLeft = showRight = -1;
            }
            TabLabel act = labels.get(activePage);

            // Padding is for beauty !
            if ((headerWidth - getWidth()) > 0 &&
                    act.getX() < getInX() || act.getX() + act.getWidth() > getInX() + getInWidth()) {
                float minX = getPaddingLeft();
                for (TabLabel label : labels) {
                    if (label != act) {
                        minX += label.getWidth();
                    } else {
                        break;
                    }
                }
                float maxX = minX + act.getWidth();
                minX = (minX - getPaddingLeft()) / (headerWidth - getOutWidth());
                maxX = ((maxX + getPaddingRight()) - getOutWidth()) / (headerWidth - getOutWidth());
                if (headerScroll > minX) setHeaderScroll(headerScroll * (1 - anim.getT()) + minX * anim.getT());
                else if (headerScroll < maxX) setHeaderScroll(headerScroll * (1 - anim.getT()) + maxX * anim.getT());
            }
            invalidate(true);
        }
    };

    public Tab() {
        pages = new ArrayList<>();
        labels = new ArrayList<>();
        unmodifiablePages = Collections.unmodifiableList(pages);
    }

    void setLabelValue(Page page, String text) {
        int index = pages.indexOf(page);
        if (index > -1 && index < labels.size()) {
            labels.get(index).setText(text);
            invalidate(true);
        }
    }

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        activePage = (int) style.asNumber("active-page", -1);
        setHeaderScrollable(style.asBool("header-scrollable", isHeaderScrollable()));
        setHeaderScroll(style.asNumber("header-scroll", getHeaderScroll()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        if (getStyle() == null) return;

        StateInfo info = getStateInfo();

        setHeaderHeight(getStyle().asSize("header-height", info, getHeaderHeight()));
        setHeaderColor(getStyle().asColor("header-color", info, getHeaderColor()));
        setHeaderIndicatorHeight(getStyle().asSize("header-indicator-height", info, getHeaderIndicatorHeight()));
        setHeaderElevation(getStyle().asSize("header-elevation", info, getHeaderElevation()));
        setHeaderIndicatorColor(getStyle().asColor("header-indicator-color", info, getHeaderColor()));
        setHeaderOffset(getStyle().asNumber("header-offset", getHeaderOffset()));
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        Gadget child;
        while ((child = children.next()) != null ) {
            if (child instanceof Page) {
                add((Page) child);
            }
        }

        setActivePage(activePage);
    }

    @Override
    protected void onActivityChange(Activity prev, Activity activity) {
        super.onActivityChange(prev, activity);

        if (anim.isPlaying()) {
            if (prev != null) prev.removeAnimation(anim);
            if (activity != null) activity.addAnimation(anim);
        }
    }

    @Override
    public void onDraw(SmartContext context) {

        final float cx = getMarginLeft() + getMarginRight() > getWidth() ? (getMarginLeft() + getWidth() - getMarginRight()) / 2f : getMarginLeft();
        final float cy = getMarginTop() + getMarginBottom() > getHeight() ? (getMarginTop() + getHeight() - getMarginBottom()) / 2f : getMarginTop();
        final float cwidth = Math.max(0, getWidth() - getMarginLeft() - getMarginRight());
        final float cheight = Math.max(0, getHeight() - getMarginTop() - getMarginBottom());

        context.setTransform2D(getTransform());
        context.drawRoundRect(cx, cy, cwidth, cheight, getRadiusTop(), getRadiusRight(), getRadiusBottom(), getRadiusLeft(), true);

        Shape clip = backgroundClip(context);
        backgroundDraw(getBackgroundColor(), getBorderColor(), getRippleColor(), context);

        for (int i = 0; i < pages.size(); i++) {
            Page child = pages.get(i);
            if (i == activePage || i == showLeft || i == showRight) {
                child.onDraw(context);
            }
        }

        // Header Background Shadow - Ignore Padding
        if (headerElevation > 0) {
            if ((headerColor & 0xFF) > 0) {
                context.setTransform2D(getTransform().preTranslate(0, Math.max(0, headerElevation)));
                context.drawRoundRectShadow(getOutX(), getOutY(), getOutWidth(), headerHeight + getPaddingTop() + getPaddingBottom(),
                        0, 0, 0, 0, headerElevation * 2, 0.28f * ((headerColor & 0xFF) / 255f));
            }
        }

        // Header Background - Ignore Padding
        context.setTransform2D(getTransform());
        context.setColor(headerColor);
        context.drawRect(getOutX(), getOutY(), getOutWidth(), headerHeight + getPaddingTop() + getPaddingBottom(), true);

        // Header Labels
        for (TabLabel tabLabel : labels) {
            tabLabel.onDraw(context);
        }

        // Header Indicator
        if (anim.isPlaying()) {
            TabLabel labelA = labels.get(showLeft == activePage ? showRight : showLeft);
            TabLabel labelB = labels.get(showLeft == activePage ? showLeft : showRight);
            context.setTransform2D(getTransform());
            context.setColor(headerIndicatorColor);
            float x = Interpolation.mix(labelA.getX(), labelB.getX(), anim.getT());
            float w = Interpolation.mix(labelA.getWidth(), labelB.getWidth(), anim.getT());
            context.drawRect(x, getInY() + headerHeight - headerIndicatorHeight, w, headerIndicatorHeight, true);
        } else {
            TabLabel label = labels.get(activePage);
            context.setTransform2D(getTransform());
            context.setColor(headerIndicatorColor);
            context.drawRect(label.getX(), getInY() + headerHeight - headerIndicatorHeight, label.getWidth(), headerIndicatorHeight, true);
        }

        context.setTransform2D(null);
        context.setClip(clip);
    }

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if (!contains(x, y)) return null;

        if ((includeDisabled || isEnabled()) &&
                (getVisibility() == Visibility.Visible || getVisibility() == Visibility.Invisible)) {
            for (TabLabel label : labels) {
                Widget found = label.findByPosition(x, y, includeDisabled);
                if (found != null) {
                    return found;
                }
            }
            if (activePage > -1 && !anim.isPlaying()) {
                Widget found = pages.get(activePage).findByPosition(x, y, includeDisabled);
                if (found != null) return found;
            }
            return isClickable() && contains(x, y) ? this : null;
        } else {
            return null;
        }
    }

    @Override
    public void onMeasure() {
        final float offWidth = getPaddingLeft() + getPaddingRight();
        final float offHeight = getPaddingTop() + getPaddingBottom();
        float mWidth = Math.max(getPrefWidth(), Math.max(getMinWidth(), offWidth));
        float mHeight = Math.max(getPrefHeight(), Math.max(getMinHeight(), offHeight));

        for (int i = 0; i < pages.size(); i++) {
            Page child = pages.get(i);
            if (i == activePage || i == showLeft || i == showRight) {
                child.onMeasure();
            }
        }
        for (int i = 0; i < labels.size(); i++) {
            TabLabel label = labels.get(i);
            label.onMeasure();
        }

        setMeasure(mWidth + getMarginLeft() + getMarginRight(), mHeight + getMarginTop() + getMarginBottom());
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));

        if (isHeaderScrollable()) {
            // Defined Width
            int match_parent_count = 0;
            float maxW = 0;
            for (TabLabel child : labels) {
                float w = child.getMeasureWidth();
                if (w == MATCH_PARENT) {
                    match_parent_count += 1;
                } else {
                    child.onLayout(w, headerHeight);
                    maxW += child.getWidth();
                }
            }
            // Undefined Width (divide)
            float reamingW = getInWidth() - maxW;
            for (TabLabel child : labels) {
                float w = child.getMeasureWidth();
                if (w == MATCH_PARENT) {
                    child.onLayout(Math.max(child.getLayoutMinWidth(), reamingW / match_parent_count), headerHeight);
                    maxW += child.getWidth();
                }
            }
            // Positions
            float mx = Math.max(0, (maxW + getPaddingLeft() + getPaddingRight() - getOutWidth()));
            float x = -(headerScroll * mx) + getPaddingLeft() + getMarginLeft();
            for (TabLabel child : labels) {
                child.setPosition(x, getInY());
                x += child.getWidth();
            }
            headerWidth = maxW + getPaddingLeft() + getPaddingRight();
        } else {
            layoutHelperHorizontal(labels, getInX(), getInY(), getInWidth(), headerHeight, Align.Vertical.BOTTOM);
            headerWidth = getInWidth();
        }

        float yOff = headerHeight + getPaddingTop() + getPaddingBottom();
        for (int i = 0; i < pages.size(); i++) {
            Page child = pages.get(i);
            if (i == activePage || i == showLeft || i == showRight) {
                child.onLayout(getOutWidth(), getOutHeight() - yOff);
                if (showLeft == showRight) {
                    child.setPosition(getOutX(), getMarginTop() + yOff);
                } else {
                    if (i == showLeft && i == activePage) {
                        child.setPosition(getOutX() - getInWidth() * (1 - anim.getT()), getMarginTop() + yOff);
                    }
                    if (i == showRight && i != activePage) {
                        child.setPosition(getOutX() + getInWidth() * anim.getT(), getMarginTop() + yOff);
                    }
                    if (i == showLeft && i != activePage) {
                        child.setPosition(getOutX() - getInWidth() * anim.getT(), getMarginTop() + yOff);
                    }
                    if (i == showRight && i == activePage) {
                        child.setPosition(getOutX() + getInWidth() * (1 - anim.getT()), getMarginTop() + yOff);
                    }
                }
            }
        }
    }

    public void add(Page child) {
        child.setTab(this);
        TabLabel label = new TabLabel(this, pages.size());
        UXTheme theme = getStyle().getTheme();
        if (theme != null) {
            label.applyAttributes(new UXStyleAttrs("attributes", theme.getStyle("tab-header")), null);
        }
        label.setText(child.getName());

        pages.add(child);
        labels.add(label);

        super.add(child);
        super.add(label);
    }

    public void add(Page... children) {
        for (Page page : children) {
            add(page);
        }
    }

    public List<Page> getPages() {
        return unmodifiablePages;
    }

    @Override
    public void remove(Widget widget) {
        if (widget.getParent() == this) {
            if (widget instanceof Page) {
                int index = pages.indexOf(widget);
                if (index > -1) {
                    super.remove(pages.remove(index));
                    super.remove(labels.remove(index));
                }
            } else if (widget instanceof TabLabel) {
                int index = labels.indexOf(widget);
                if (index > -1) {
                    super.remove(pages.remove(index));
                    super.remove(labels.remove(index));
                }
            }
            setActivePage(activePage);
        }
    }

    @Override
    public void fireHover(HoverEvent hoverEvent) {
        super.fireHover(hoverEvent);

        if (!hoverEvent.isConsumed()) {
            if (hoverEvent.getType() != HoverEvent.EXITED) {
                Vector2 pos = new Vector2(hoverEvent.getX(), hoverEvent.getY());
                screenToLocal(pos);
                headerHovered = (pos.y >= getOutY() && pos.y <= getInY() + headerHeight + getPaddingBottom());
            } else {
                headerHovered = false;
            }
        }
    }

    @Override
    public void fireScroll(ScrollEvent scrollEvent) {
        if (!scrollEvent.isConsumed() && headerHovered) {
            float prev = headerScroll;
            if (headerWidth > 0 && getWidth() > 0) {
                setHeaderScroll(headerScroll - (scrollEvent.getDeltaY() * getWidth() / headerWidth) / 6f);
            } else {
                setHeaderScroll(headerScroll - (scrollEvent.getDeltaY() * 0.1f));
            }
            if (headerScroll != prev) {
                scrollEvent.consume();
            }
        }
        super.fireScroll(scrollEvent);
    }

    public int getActivePage() {
        return this.activePage;
    }

    public void setActivePage(int activePage) {
        int size = pages.size();
        if (size == 0) {
            activePage = -1;
        } else {
            activePage = Math.max(0, Math.min(size - 1, activePage));
        }
        if (this.activePage != activePage) {
            int old = this.activePage;
            this.activePage = activePage;

            if (old < 0 || old >= size) {
                showLeft = showRight = -1;
                labels.get(activePage).setActivated(true);
                pages.get(activePage).setActivated(true);
                anim.stop();
            } else if (old < activePage) {
                showLeft = old;
                showRight = activePage;
                anim.setDuration(getTransitionDuration());
                anim.play(getActivity());
            } else {
                showLeft = activePage;
                showRight = old;
                anim.setDuration(getTransitionDuration());
                anim.play(getActivity());
            }
            invalidate(true);
        }
    }

    public float getHeaderHeight() {
        return headerHeight;
    }

    public void setHeaderHeight(float headerHeight) {
        if (this.headerHeight != headerHeight) {
            this.headerHeight = headerHeight;
            invalidate(true);
        }
    }

    public float getHeaderElevation() {
        return headerElevation;
    }

    public void setHeaderElevation(float headerElevation) {
        if (this.headerElevation != headerElevation) {
            this.headerElevation = headerElevation;
            invalidate(false);
        }
    }

    public int getHeaderColor() {
        return headerColor;
    }

    public void setHeaderColor(int headerColor) {
        if (this.headerColor != headerColor) {
            this.headerColor = headerColor;
            invalidate(false);
        }
    }

    public int getHeaderIndicatorColor() {
        return headerIndicatorColor;
    }

    public void setHeaderIndicatorColor(int headerIndicatorColor) {
        if (this.headerIndicatorColor != headerIndicatorColor) {
            this.headerIndicatorColor = headerIndicatorColor;
            invalidate(false);
        }
    }

    public float getHeaderIndicatorHeight() {
        return headerIndicatorHeight;
    }

    public void setHeaderIndicatorHeight(float headerIndicatorHeight) {
        if (this.headerIndicatorHeight != headerIndicatorHeight) {
            this.headerIndicatorHeight = headerIndicatorHeight;
            invalidate(false);
        }
    }

    public boolean isHeaderScrollable() {
        return headerScrollable;
    }

    public void setHeaderScrollable(boolean headerScrollable) {
        if (this.headerScrollable != headerScrollable) {
            this.headerScrollable = headerScrollable;
            invalidate(false);
        }
    }

    public float getHeaderScroll() {
        return headerScroll;
    }

    public void setHeaderScroll(float headerScroll) {
        headerScroll = Math.max(0 , Math.min(1, headerScroll));
        if (this.headerScroll != headerScroll) {
            this.headerScroll = headerScroll;
            invalidate(true);
        }
    }

    public float getHeaderOffset() {
        return headerOffset;
    }

    public void setHeaderOffset(float headerOffset) {
        if (this.headerOffset != headerOffset) {
            this.headerOffset = headerOffset;
            invalidate(true);
        }
    }

    class TabLabel extends Button {
        Tab tab;
        int index;

        TabLabel(Tab tab, int index) {
            this.tab = tab;
            this.index = index;
        }

        @Override
        public void onDraw(SmartContext context) {
            super.onDraw(context);
        }

        @Override
        public void firePointer(PointerEvent pointerEvent) {
            super.firePointer(pointerEvent);
            if (!pointerEvent.isConsumed() && pointerEvent.getType() == PointerEvent.RELEASED) {
                tab.setActivePage(index);
            }
        }

        @Override
        public void fireRipple(float x, float y) {
            super.fireRipple(x, y);
        }

        @Override
        protected void setActivated(boolean actived) {
            super.setActivated(actived);
        }
    }
}
