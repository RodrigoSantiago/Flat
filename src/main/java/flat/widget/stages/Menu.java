package flat.widget.stages;

import flat.animations.Interpolation;
import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.events.ScrollEvent;
import flat.events.SlideEvent;
import flat.exception.FlatException;
import flat.graphics.Graphics;
import flat.uxml.*;
import flat.widget.Stage;
import flat.widget.Widget;
import flat.widget.enums.*;
import flat.widget.value.VerticalScrollBar;
import flat.window.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Menu extends Stage {

    private UXListener<ActionEvent> slideListener;
    private UXValueListener<Float> viewOffsetListener;
    private HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;
    private float showTransitionDuration = 0;
    private float scrollSensibility = 20f;
    private boolean blockEvents;

    private VerticalScrollBar verticalBar;
    private Policy verticalBarPolicy = Policy.AS_NEEDED;
    private VerticalBarPosition verticalBarPosition = VerticalBarPosition.RIGHT;

    private float viewOffset;
    private float viewDimension;
    private float totalDimension;
    private float targetX, targetY;
    private float animX, animY;
    private final ShowAnimation showAnimation = new ShowAnimation();

    private final ArrayList<Widget> orderedList = new ArrayList<>();
    private final List<Widget> unmodifiableItemsList = Collections.unmodifiableList(orderedList);
    private float[] tempSize;

    private Menu parentMenu;
    private Menu childMenu;
    private boolean show;

    private final UXListener<SlideEvent> slideY = (event) -> {
        event.consume();
        slideTo(event.getValue());
    };

    public Menu() {
        var vbar = new VerticalScrollBar();
        vbar.addStyle(UXAttrs.convertToKebabCase(getClass().getSimpleName()) + "-vertical-scroll-bar");
        setVerticalBar(vbar);
    }

    @Override
    public void applyChildren(UXChildren children) {
        Widget widget;

        for (var child : children) {
            if (child.getWidget() instanceof MenuItem menuItem) {
                addMenuItem(menuItem);
            } else if (child.getWidget() instanceof Divider divider) {
                addDivider(divider);
            } else if (child.getAttributeBool("vertical-bar", false) &&
                    child.getWidget() instanceof VerticalScrollBar bar) {
                setVerticalBar(bar);
            }
        }
    }

    @Override
    public void setContextMenu(Menu contextMenu) {

    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setViewOffsetListener(attrs.getAttributeValueListener("on-view-offset-change", Float.class, controller, getViewOffsetListener()));
        setSlideListener(attrs.getAttributeListener("on-slide", ActionEvent.class, controller, getSlideListener()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
        setScrollSensibility(attrs.getNumber("scroll-sensibility", info, getScrollSensibility()));
        setShowTransitionDuration(attrs.getNumber("show-transition-duration", info, getShowTransitionDuration()));
        setVerticalBarPolicy(attrs.getConstant("vertical-bar-policy", info, getVerticalBarPolicy()));
        setVerticalBarPosition(attrs.getConstant("vertical-bar-position", info, getVerticalBarPosition()));
    }

    @Override
    public void onMeasure() {
        performMeasureVertical(orderedList);
        if (verticalBar != null) {
            verticalBar.onMeasure();
        }
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);
        float old = getViewOffset();

        totalDimension = performLayoutVerticalScrollable(getInWidth(), getInHeight(), getInX(), getInY(),
                orderedList, VerticalAlign.TOP, horizontalAlign, getViewOffset());
        viewDimension = getInHeight();

        float lx = getInX();
        float ly = getInY();
        float lWidth = getInWidth();
        float lHeight = getInHeight();

        if (verticalBar != null) {
            float childWidth = Math.min(getDefWidth(verticalBar), lWidth);
            float childHeight = Math.min(getDefHeight(verticalBar), lHeight);
            verticalBar.onLayout(childWidth, childHeight);

            if (verticalBarPosition == VerticalBarPosition.LEFT) {
                verticalBar.setLayoutPosition(lx, ly);
            } else {
                verticalBar.setLayoutPosition(lx + lWidth - verticalBar.getLayoutWidth(), ly);
            }

            verticalBar.setViewOffsetListener(null);
            verticalBar.setSlideListener(null);
            verticalBar.setViewDimension(viewDimension);
            verticalBar.setTotalDimension(totalDimension);
            verticalBar.setSlideFilter(slideY);
        }

        if (old != getViewOffset() && getActivity() != null) {
            getActivity().runLater(() -> setViewOffset(getViewOffset()));
        }

        setLayoutPosition(targetX, targetY);
        fireLayout();
    }

    @Override
    public void onDraw(Graphics graphics) {
        if (discardDraw(graphics)) return;

        drawBackground(graphics);
        drawRipple(graphics);

        if (getInWidth() <= 0 || getInHeight() <= 0) return;

        if (isScrollable()) {
            graphics.pushClip(getBackgroundShape());
            for (Widget child : getChildrenIterable()) {
                if (child != verticalBar && child.getVisibility() == Visibility.VISIBLE) {
                    child.onDraw(graphics);
                }
            }
            graphics.popClip();
            if (isVerticalVisible() && verticalBar != null && verticalBar.getVisibility() == Visibility.VISIBLE) {
                verticalBar.onDraw(graphics);
            }
        } else {
            for (Widget child : getChildrenIterable()) {
                if (child != verticalBar && child.getVisibility() == Visibility.VISIBLE) {
                    child.onDraw(graphics);
                }
            }
        }
    }

    public void addMenuItem(MenuItem child) {
        super.add(child);
    }

    public void addMenuItem(MenuItem... children) {
        for (MenuItem child : children) {
            addMenuItem(child);
        }
    }

    public void addMenuItem(List<MenuItem> children) {
        for (MenuItem child : children) {
            addMenuItem(child);
        }
    }

    public void addDivider(Divider child) {
        super.add(child);
    }

    public void addDivider(Divider... children) {
        for (Divider child : children) {
            addDivider(child);
        }
    }

    public void addDivider(List<Divider> children) {
        for (Divider child : children) {
            addDivider(child);
        }
    }

    public void moveChild(Widget child, int index) {
        if (index < 0 || index >= orderedList.size()) {
            throw new FlatException("Invalid child index position");
        }
        if (orderedList.contains(child)) {
            orderedList.remove(child);
            orderedList.add(index, child);
        }
    }

    public List<Widget> getUnmodifiableItemsList() {
        return unmodifiableItemsList;
    }

    @Override
    protected boolean attachChild(Widget child) {
        if (super.attachChild(child)) {
            if (!(child instanceof VerticalScrollBar)) {
                orderedList.add(child);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean detachChild(Widget child) {
        if (child == verticalBar) {
            return false;
        }
        if (super.detachChild(child)) {
            orderedList.remove(child);
            return true;
        }
        return false;
    }

    @Override
    public void scroll(ScrollEvent event) {
        super.scroll(event);
        if (!event.isConsumed() && isScrollable()) {
            slide(- event.getDeltaY() * scrollSensibility);
            event.consume();
        }
    }

    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        if (!event.isConsumed() && event.getType() == PointerEvent.FILTER) {
            if (!isSourceMenuFrom(event.getSource())) {
                hide();
            }
        }
    }

    @Override
    public void resize() {
        hide();
    }

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if (!isCurrentHandleEventsEnabled()
                || getVisibility() == Visibility.GONE
                || (!includeDisabled && !isEnabled())
                || (!contains(x, y))) {
            return null;
        }
        if (!blockEvents) {
            if (isVerticalVisible() && verticalBar != null) {
                Widget found = verticalBar.findByPosition(x, y, includeDisabled);
                if (found != null) return found;
            }

            for (Widget child : getChildrenIterableReverse()) {
                if (child != verticalBar) {
                    Widget found = child.findByPosition(x, y, includeDisabled);
                    if (found != null) return found;
                }
            }
            return isHandlePointerEnabled() ? this : null;
        }
        return null;
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
                    this.verticalBar.setViewDimension(viewDimension);
                    this.verticalBar.setTotalDimension(totalDimension);
                    this.verticalBar.setViewOffset(viewOffset);
                    this.verticalBar.setSlideFilter(slideY);
                }
            }
        }
    }

    public Policy getVerticalBarPolicy() {
        return verticalBarPolicy;
    }

    public void setVerticalBarPolicy(Policy verticalBarPolicy) {
        if (verticalBarPolicy == null) verticalBarPolicy = Policy.AS_NEEDED;

        if (this.verticalBarPolicy != verticalBarPolicy) {
            this.verticalBarPolicy = verticalBarPolicy;
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

    public HorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
        if (horizontalAlign == null) horizontalAlign = HorizontalAlign.CENTER;

        if (this.horizontalAlign != horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            invalidate(true);
        }
    }

    public float getShowTransitionDuration() {
        return showTransitionDuration;
    }

    public void setShowTransitionDuration(float showTransitionDuration) {
        if (this.showTransitionDuration != showTransitionDuration) {
            this.showTransitionDuration = showTransitionDuration;

            showAnimation.stop();
        }
    }

    public boolean isScrollable() {
        return viewDimension < totalDimension - 0.001f;
    }

    protected boolean isVerticalVisible() {
        return isScrollable() && verticalBarPolicy != Policy.NEVER;
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
            fireViewOffsetListener(old);
            if (verticalBar != null) {
                verticalBar.setViewOffsetListener(null);
                verticalBar.setViewOffset(this.viewOffset);
            }
        }
    }

    public float getScrollSensibility() {
        return scrollSensibility;
    }

    public void setScrollSensibility(float scrollSensibility) {
        this.scrollSensibility = scrollSensibility;
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

    public UXValueListener<Float> getViewOffsetListener() {
        return viewOffsetListener;
    }

    public void setViewOffsetListener(UXValueListener<Float> viewOffsetListener) {
        this.viewOffsetListener = viewOffsetListener;
    }

    private void fireViewOffsetListener(float old) {
        if (viewOffsetListener != null && old != viewOffset) {
            UXValueListener.safeHandle(viewOffsetListener, new ValueChange<>(this, old, viewOffset));
        }
    }

    @Override
    public boolean isShown() {
        return show;
    }

    private boolean isChildMenuOf(Menu menu) {
        if (menu == this) return true;

        if (parentMenu != null) {
            if (parentMenu == menu) {
                return true;
            } else {
                return parentMenu.isChildOf(menu);
            }
        } else {
            return false;
        }
    }

    protected boolean isSourceMenuFrom(Widget widget) {
        if (widget == this || widget.isChildOf(this)) return true;

        if (childMenu != null) {
            return childMenu.isSourceMenuFrom(widget);
        } else {
            return false;
        }
    }

    protected void showSubMenu(Menu menu, float x, float y, float width, float height, DropdownAlign align) {
        if (childMenu != null) {
            hideSubMenu();
        }
        childMenu = menu;
        childMenu.parentMenu = this;
        childMenu.show(getActivity(), x, y, width, height, align);
    }

    protected void hideSubMenu() {
        if (childMenu != null) {
            childMenu.hide();
        }
    }

    public void show(Activity activity, float x, float y, DropdownAlign align) {
        show(activity, x , y, 0, 0, align);
    }

    public void show(Activity activity, float x, float y, float width, float height, DropdownAlign align) {
        if (!isShown()) {
            show = true;
            setToShow(activity);
            activity.addPointerFilter(this);
            activity.addResizeFilter(this);
            onShow(activity, x, y, width, height, align);
        }
    }

    public void show(Menu menu, float x, float y, DropdownAlign align) {
        show(menu, x, y, 0, 0, align);
    }

    public void show(Menu menu, float x, float y, float width, float height, DropdownAlign align) {
        if (!isShown() && menu.isShown() && menu.getActivity() != null
                && !menu.isChildMenuOf(this) && !menu.isChildOf(this) && !this.isChildOf(menu)) {
            menu.showSubMenu(this, x, y, width, height, align);
        }
    }

    @Override
    public void hide() {
        if (isShown()) {
            show = false;
            if (parentMenu != null) {
                parentMenu.childMenu = null;
                parentMenu = null;
            }
            hideSubMenu();
            setToHide();
        }
    }

    public void hideCascade() {
        if (parentMenu != null) {
            parentMenu.hideCascade();
        } else {
            hide();
        }
    }

    private void onShow(Activity act, float x, float y, float width, float height, DropdownAlign align) {
        refreshStyle();

        onMeasure();
        float mW = Math.min(Math.min(getMeasureWidth(), getLayoutMaxWidth()), act.getWidth());
        float mH = Math.min(Math.min(getMeasureHeight(), getLayoutMaxHeight()), act.getHeight());
        onLayout(mW, mH);

        if (align == DropdownAlign.SCREEN_SPACE) {
            if (x < act.getWidth() * 0.5f) {
                align = y < act.getHeight() * 0.5f ? DropdownAlign.TOP_LEFT : DropdownAlign.BOTTOM_LEFT;
            } else {
                align = y < act.getHeight() * 0.5f ? DropdownAlign.TOP_RIGHT : DropdownAlign.BOTTOM_RIGHT;
            }
        } else if (align == DropdownAlign.TOP_LEFT_ADAPTATIVE) {
            x += width;
            if (x + getWidth() + getMarginRight() > act.getWidth()) {
                if (y + getHeight() + getMarginBottom() > act.getHeight()) {
                    y += height;
                    x -= width;
                    align = DropdownAlign.BOTTOM_RIGHT;
                } else {
                    x -= width;
                    align = DropdownAlign.TOP_RIGHT;
                }
            } else {
                if (y + getHeight() + getMarginBottom() > act.getHeight()) {
                    y += height;
                    align = DropdownAlign.BOTTOM_LEFT;
                } else {
                    align = DropdownAlign.TOP_LEFT;
                }
            }
        }

        if (align == DropdownAlign.TOP_LEFT) {
            targetX = Math.max(Math.min(x, act.getWidth() - getWidth() - getMarginRight()), 0) - getMarginLeft();
            targetY = Math.max(Math.min(y, act.getHeight() - getHeight() - getMarginBottom()), 0) - getMarginTop();

        } else if (align == DropdownAlign.TOP_RIGHT) {
            targetX = Math.max(x - getWidth(), getMarginLeft()) - getMarginLeft();
            targetY = Math.max(Math.min(y, act.getHeight() - getHeight() - getMarginBottom()), 0) - getMarginTop();

        } else if (align == DropdownAlign.BOTTOM_LEFT) {
            targetX = Math.max(Math.min(x, act.getWidth() - getWidth() - getMarginRight()), 0) - getMarginLeft();
            targetY = Math.max(y - getHeight(), getMarginTop()) - getMarginTop();

        } else if (align == DropdownAlign.BOTTOM_RIGHT) {
            targetX = Math.max(x - getWidth(), getMarginLeft()) - getMarginLeft();
            targetY = Math.max(y - getHeight(), getMarginTop()) - getMarginTop();

        }

        animX = (x - targetX) / mW;
        animY = (y - targetY) / mH;

        if (showTransitionDuration > 0) {
            showAnimation.setDuration(showTransitionDuration);
            showAnimation.play(act);
        }
    }

    private class ShowAnimation extends NormalizedAnimation {

        private float scaleX, scaleY, centerX, centerY;
        private boolean followX, followY, followCX, followCY;

        public ShowAnimation() {
            super(Interpolation.circleOut);
        }

        @Override
        public Activity getSource() {
            return getActivity();
        }

        @Override
        protected void compute(float t) {
            setScaleX(scaleX * 0.5f + (scaleX * 0.5f * t));
            setScaleY(scaleY * 0.5f + (scaleY * 0.5f * t));
            invalidate(false);
        }

        @Override
        protected void onStart() {
            scaleX = getScaleX();
            scaleY = getScaleY();
            centerX = getCenterX();
            centerY = getCenterY();
            followX = isFollowStyleProperty("scale-x");
            followY = isFollowStyleProperty("scale-y");
            followCX = isFollowStyleProperty("center-x");
            followCY = isFollowStyleProperty("center-y");
            setFollowStyleProperty("scale-x", false);
            setFollowStyleProperty("scale-y", false);
            setFollowStyleProperty("center-x", false);
            setFollowStyleProperty("center-y", false);
            setCenterX(animX);
            setCenterY(animY);
            blockEvents = true;
            compute(0);
        }

        @Override
        protected void onStop() {
            setScaleX(scaleX);
            setScaleY(scaleY);
            setCenterX(centerX);
            setCenterY(centerY);
            setFollowStyleProperty("scale-x", followX);
            setFollowStyleProperty("scale-y", followY);
            setFollowStyleProperty("scale-x", followCX);
            setFollowStyleProperty("scale-y", followCY);
            blockEvents = false;
        }
    }
}
