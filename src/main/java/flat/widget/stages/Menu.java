package flat.widget.stages;

import flat.animations.Interpolation;
import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.events.ScrollEvent;
import flat.graphics.SmartContext;
import flat.math.shapes.Shape;
import flat.uxml.*;
import flat.widget.Group;
import flat.widget.Parent;
import flat.widget.Stage;
import flat.widget.Widget;
import flat.widget.enums.DropdownAlign;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.widget.enums.Visibility;
import flat.window.Activity;

import java.util.ArrayList;
import java.util.List;

public class Menu extends Stage {

    private UXListener<ActionEvent> slideListener;
    private UXValueListener<Float> viewOffsetListener;
    private HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;
    private float showupTransitionDuration = 0;
    private float scrollSensibility = 10f;

    private float viewOffset;
    private float viewDimension;
    private float totalDimension;
    private float targetX, targetY;
    private float animX, animY;
    private final ShowupAnimation showupAnimation = new ShowupAnimation();

    private final ArrayList<Widget> orderedList = new ArrayList<>();
    private float[] tempSize;

    private Menu parentMenu;
    private Menu childMenu;
    private boolean show;

    @Override
    public void applyChildren(UXChildren children) {
        Widget widget;
        while ((widget = children.next()) != null ) {
            add(widget);
        }
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setViewOffsetListener(attrs.getAttributeValueListener("on-view-offset-change", Float.class, controller));
        setSlideListener(attrs.getAttributeListener("on-slide", ActionEvent.class, controller));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
        setScrollSensibility(attrs.getNumber("scroll-sensibility", info, getScrollSensibility()));
        setShowupTransitionDuration(attrs.getNumber("showup-transition-duration", info, getShowupTransitionDuration()));
    }

    @Override
    public void onMeasure() {
        performMeasureVertical();
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);
        totalDimension = performLayoutVerticalScrollable(getInWidth(), getInHeight(), getInX(), getInY(),
                orderedList, VerticalAlign.TOP, horizontalAlign, getViewOffset());
        viewDimension = getInHeight();
        setViewOffset(getViewOffset());
        setLayoutPosition(targetX, targetY);
    }

    @Override
    public void onDraw(SmartContext context) {
        if (getInWidth() <= 0 || getInHeight() <= 0) return;

        drawBackground(context);
        drawRipple(context);

        if (isScrollable()) {
            context.pushClip(getBackgroundShape());
            for (Widget child : getChildrenIterable()) {
                if (child.getVisibility() == Visibility.VISIBLE) {
                    child.onDraw(context);
                }
            }
            context.popClip();
        } else {
            drawChildren(context);
        }
    }

    @Override
    public void add(Widget child) {
        super.add(child);
    }

    @Override
    public void add(Widget... children) {
        super.add(children);
    }

    @Override
    public void add(List<Widget> children) {
        super.add(children);
    }

    @Override
    protected boolean attachChild(Widget child) {
        if (super.attachChild(child)) {
            orderedList.add(child);
            return true;
        }
        return false;
    }

    @Override
    protected boolean detachChild(Widget child) {
        if (super.detachChild(child)) {
            orderedList.remove(child);
            return true;
        }
        return false;
    }

    @Override
    public void fireScroll(ScrollEvent event) {
        super.fireScroll(event);
        if (!event.isConsumed()) {
            slide(- event.getDeltaY() * scrollSensibility);
        }
    }

    @Override
    public void firePointer(PointerEvent event) {
        super.firePointer(event);
        if (!event.isConsumed() && event.getType() == PointerEvent.FILTER) {
            if (event.getSource() != this && !event.getSource().isChildOf(this)) {
                hide();
            }
        }
    }

    @Override
    public void fireResize() {
        hide();
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

    public float getShowupTransitionDuration() {
        return showupTransitionDuration;
    }

    public void setShowupTransitionDuration(float showupTransitionDuration) {
        if (this.showupTransitionDuration != showupTransitionDuration) {
            this.showupTransitionDuration = showupTransitionDuration;

            showupAnimation.stop();
        }
    }

    public boolean isScrollable() {
        return viewDimension < totalDimension - 0.001f;
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

    protected void showSubMenu(Menu menu, float x, float y, DropdownAlign align) {
        if (childMenu != null) {
            hideSubMenu();
        }
        childMenu = menu;
        childMenu.parentMenu = this;
        childMenu.show(getActivity(), x, y, align);
    }

    protected void hideSubMenu() {
        if (childMenu != null) {
            childMenu.hide();
        }
    }

    public void show(Activity activity, float x, float y, DropdownAlign align) {
        if (!isShown()) {
            show = true;
            setToShow(activity);
            activity.addPointerFilter(this);
            activity.addResizeFilter(this);
            onShow(activity, x, y, align);
        }
    }

    public void show(Menu menu, float x, float y, DropdownAlign align) {
        if (!isShown() && menu.isShown() && menu.getActivity() != null
                && !menu.isChildMenuOf(this) && !menu.isChildOf(this) && !this.isChildOf(menu)) {
            menu.showSubMenu(this, x, y, align);
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

    private void onShow(Activity act, float x, float y, DropdownAlign align) {
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
            if (x + getWidth() + getMarginRight() > act.getWidth()) {
                if (y + getHeight() + getMarginBottom() > act.getHeight()) {
                    align = DropdownAlign.BOTTOM_RIGHT;
                } else {
                    align = DropdownAlign.TOP_RIGHT;
                }
            } else {
                if (y + getHeight() + getMarginBottom() > act.getHeight()) {
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

        if (showupTransitionDuration > 0) {
            showupAnimation.setDuration(showupTransitionDuration);
            showupAnimation.play(act);
        }
    }

    private class ShowupAnimation extends NormalizedAnimation {

        private float scaleX, scaleY, centerX, centerY;
        private boolean followX, followY, followCX, followCY;

        public ShowupAnimation() {
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
        }
    }
}
