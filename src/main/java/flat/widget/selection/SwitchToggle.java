package flat.widget.selection;

import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.image.Drawable;
import flat.uxml.*;
import flat.widget.Widget;
import flat.widget.enums.Direction;
import flat.widget.enums.ImageFilter;
import flat.window.Activity;

public class SwitchToggle extends Widget {

    private UXListener<ActionEvent> toggleListener;
    private UXValueListener<Boolean> activeListener;
    private boolean active;

    private ImageFilter iconImageFilter = ImageFilter.LINEAR;
    private Drawable iconInactive;
    private Drawable iconActive;
    private int iconColor = Color.white;
    private float iconTransitionDuration;
    private float slideTransitionDuration;
    private Direction direction = Direction.HORIZONTAL;

    private final IconChange iconChangeAnimation = new IconChange();
    private final IconChange iconSlideAnimation = new IconChange();
    private Drawable prevIcon;
    private Drawable currentIcon;
    private float iconWidth;
    private float iconHeight;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setActive(attrs.getAttributeBool("active", isActive()));
        setToggleListener(attrs.getAttributeListener("on-toggle", ActionEvent.class, controller));
        setActiveListener(attrs.getAttributeValueListener("on-active-change", Boolean.class, controller));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setIconColor(attrs.getColor("icon-color", info, getIconColor()));
        setIconImageFilter(attrs.getConstant("icon-image-filter", info, getIconImageFilter()));
        setIconInactive(attrs.getResourceAsDrawable("icon-inactive", info, getIconInactive(), false));
        setIconActive(attrs.getResourceAsDrawable("icon-active", info, getIconActive(), false));
        setIconTransitionDuration(attrs.getNumber("icon-transition-duration", info, getIconTransitionDuration()));
        setSlideTransitionDuration(attrs.getNumber("slide-transition-duration", info, getSlideTransitionDuration()));
        setDirection(attrs.getConstant("direction", info, getDirection()));
    }

    @Override
    public void onDraw(Graphics graphics) {
        drawBackground(graphics);

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        float slide = iconSlideAnimation.isPlaying() ? iconSlideAnimation.getInterpolatedPosition() : 1f;
        if (!active) {
            slide = 1 - slide;
        }
        if (direction == Direction.HORIZONTAL || direction == Direction.VERTICAL) {
            slide = 1 - slide;
        }

        boolean hor = direction == Direction.HORIZONTAL || direction == Direction.IHORIZONTAL;
        float cx1, cy1, cx2, cy2;

        if (hor) {
            cx1 = (x + Math.min(iconWidth, width) * 0.5f);
            cy1 = y + height * 0.5f;
            cx2 = (x + (width - Math.min(iconWidth, width) * 0.5f));
            cy2 = y + height * 0.5f;

        } else {
            cx1 = x + width * 0.5f;
            cy1 = (y + Math.min(iconHeight, height) * 0.5f);
            cx2 = x + width * 0.5f;
            cy2 = (y + (height - Math.min(iconHeight, height) * 0.5f));
        }

        float px = cx1 * (1 - slide) + cx2 * slide;
        float py = cy1 * (1 - slide) + cy2 * slide;

        graphics.setTransform2D(getTransform());

        float pos = iconChangeAnimation.isPlaying() ? iconChangeAnimation.getInterpolatedPosition() : 1f;
        float prevAlpha = pos < 0.5f ? 1 : 1 - (pos - 0.5f) / 0.5f;
        float currentAlpha = pos < 0.5f ? pos / 0.5f : 1;
        if (iconTransitionDuration <= 0) {
            currentAlpha = 1f;
        }

        if (iconTransitionDuration > 0 && prevIcon != null) {
            float icoWidth = Math.min(prevIcon.getWidth(), width);
            float icoHeight = Math.min(prevIcon.getHeight(), height);

            int colorAlpha = Color.multiplyColorAlpha(iconColor, prevAlpha);
            prevIcon.draw(graphics
                    , px - icoWidth * 0.5f
                    , py - icoHeight * 0.5f
                    , icoWidth, icoHeight, colorAlpha, iconImageFilter);
        }

        if (currentIcon != null) {
            float icoWidth = Math.min(currentIcon.getWidth(), width);
            float icoHeight = Math.min(currentIcon.getHeight(), height);

            int colorAlpha = Color.multiplyColorAlpha(iconColor, currentAlpha);
            currentIcon.draw(graphics
                    , px - icoWidth * 0.5f
                    , py - icoHeight * 0.5f
                    , icoWidth, icoHeight, colorAlpha, iconImageFilter);
        }

        if (isRippleEnabled()) {
            getRipple().setSize((hor ? getLayoutHeight() : getLayoutWidth()) * 0.5f);
            getRipple().setPosition(px, py);
            drawRipple(graphics);
        }
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;
        boolean hor = direction == Direction.HORIZONTAL || direction == Direction.IHORIZONTAL;

        if (wrapWidth) {
            mWidth = Math.max(iconWidth * (hor ? 2f : 1f) + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(iconHeight * (hor ? 1f : 2f) + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        if (!event.isConsumed() && event.getPointerID() == 1 && event.getType() == PointerEvent.RELEASED) {
            toggle();
        }
    }

    private void updateIconSize() {
        float iaWidth = iconActive == null ? 0 : iconActive.getWidth();
        float iaHeight = iconActive == null ? 0 : iconActive.getHeight();
        float iiWidth = iconInactive == null ? 0 : iconInactive.getWidth();
        float iiHeight = iconInactive == null ? 0 : iconInactive.getHeight();
        float nextWidth = Math.max(iaWidth, iiWidth);
        float nextHeight = Math.max(iaHeight, iiHeight);
        if (nextWidth != iconWidth || nextHeight != iconHeight) {
            this.iconWidth = nextWidth;
            this.iconHeight = nextHeight;
            invalidate(isWrapContent());
        } else {
            invalidate(false);
        }
    }

    private void setCurrentIcon() {
        Drawable icon = isActive() ? iconActive : iconInactive;
        if (icon == null) {
            icon = isActive() ? iconInactive : iconActive;
        }
        if (currentIcon != icon) {
            if (iconTransitionDuration > 0) {
                iconChangeAnimation.setDuration(iconTransitionDuration);
                iconChangeAnimation.play(getActivity());
            }
            prevIcon = currentIcon;
            currentIcon = icon;
            invalidate(false);
        }
    }

    public ImageFilter getIconImageFilter() {
        return iconImageFilter;
    }

    public void setIconImageFilter(ImageFilter iconImageFilter) {
        if (iconImageFilter == null) iconImageFilter = ImageFilter.NEAREST;

        if (this.iconImageFilter != iconImageFilter) {
            this.iconImageFilter = iconImageFilter;
            invalidate(false);
        }
    }

    public Drawable getIconInactive() {
        return iconInactive;
    }

    public void setIconInactive(Drawable iconInactive) {
        if (this.iconInactive != iconInactive) {
            this.iconInactive = iconInactive;
            updateIconSize();
            setCurrentIcon();
        }
    }

    public Drawable getIconActive() {
        return iconActive;
    }

    public void setIconActive(Drawable iconActive) {
        if (this.iconActive != iconActive) {
            this.iconActive = iconActive;

            updateIconSize();
            setCurrentIcon();
        }
    }

    public float getIconTransitionDuration() {
        return iconTransitionDuration;
    }

    public void setIconTransitionDuration(float iconTransitionDuration) {
        if (this.iconTransitionDuration != iconTransitionDuration) {
            this.iconTransitionDuration = iconTransitionDuration;

            iconChangeAnimation.stop(true);
        }
    }

    public float getSlideTransitionDuration() {
        return slideTransitionDuration;
    }

    public void setSlideTransitionDuration(float slideTransitionDuration) {
        if (this.slideTransitionDuration != slideTransitionDuration) {
            this.slideTransitionDuration = slideTransitionDuration;

            iconSlideAnimation.stop(true);
        }
    }

    public int getIconColor() {
        return iconColor;
    }

    public void setIconColor(int iconColor) {
        if (this.iconColor != iconColor) {
            this.iconColor = iconColor;
            invalidate(false);
        }
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if (direction == null) direction = Direction.HORIZONTAL;

        if (this.direction != direction) {
            this.direction = direction;
            invalidate(true);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (this.active != active) {
            boolean old = this.active;
            this.active = active;

            if (slideTransitionDuration > 0) {
                iconSlideAnimation.setDuration(slideTransitionDuration);
                iconSlideAnimation.play(getActivity());
            }
            setCurrentIcon();
            setActivated(active);
            fireActiveListener(old);
        }
    }

    public UXListener<ActionEvent> getToggleListener() {
        return toggleListener;
    }

    public void setToggleListener(UXListener<ActionEvent> toggleListener) {
        this.toggleListener = toggleListener;
    }

    private void fireToggle() {
        if (toggleListener != null) {
            UXListener.safeHandle(toggleListener, new ActionEvent(this));
        }
    }

    public void toggle() {
        setActive(!isActive());
        fireToggle();
    }

    public UXValueListener<Boolean> getActiveListener() {
        return activeListener;
    }

    public void setActiveListener(UXValueListener<Boolean> activeListener) {
        this.activeListener = activeListener;
    }

    private void fireActiveListener(boolean oldValue) {
        if (activeListener != null && oldValue != active) {
            UXValueListener.safeHandle(activeListener, new ValueChange<>(this, oldValue, active));
        }
    }

    protected boolean isWrapContent() {
        return getPrefWidth() == WRAP_CONTENT || getPrefHeight() == WRAP_CONTENT;
    }

    private class IconChange extends NormalizedAnimation {
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
