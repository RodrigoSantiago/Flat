package flat.widget.selection;

import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.uxml.*;
import flat.widget.Group;
import flat.widget.Widget;
import flat.widget.enums.ImageFilter;
import flat.window.Activity;

public class RadioButton extends Widget {

    private UXListener<ActionEvent> toggleListener;
    private UXValueListener<Boolean> activeListener;
    private boolean active;

    private ImageFilter iconImageFilter = ImageFilter.LINEAR;
    private Drawable inactiveIcon;
    private Drawable activeIcon;
    private int iconColor = Color.white;
    private float iconTransitionDuration;

    RadioGroup radioGroup;

    private final IconChange iconChangeAnimation = new IconChange();
    private Drawable prevIcon;
    private Drawable currentIcon;
    private float iconWidth;
    private float iconHeight;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();

        String groupId = attrs.getAttributeString("radio-group-id", null);
        if (groupId != null) {
            Group group = getGroup();
            if (group != null) {
                Widget widget = group.findById(groupId);
                if (widget instanceof RadioGroup rGroup) {
                    rGroup.add(this);
                }
            }
        }
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
        setInactiveIcon(attrs.getResourceAsDrawable("inactive-icon", info, getInactiveIcon(), false));
        setActiveIcon(attrs.getResourceAsDrawable("active-icon", info, getActiveIcon(), false));
        setIconTransitionDuration(attrs.getNumber("icon-transition-duration", info, getIconTransitionDuration()));
    }

    @Override
    public void onDraw(SmartContext context) {
        drawBackground(context);
        drawRipple(context);

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        context.setTransform2D(getTransform());

        float pos = iconChangeAnimation.isPlaying() ? iconChangeAnimation.getInterpolatedPosition() : 1f;
        float prevAlpha = pos < 0.5f ? 1 : 1 - (pos - 0.5f) / 0.5f;
        float currentAlpha = pos < 0.5f ? pos / 0.5f : 1;
        if (iconTransitionDuration <= 0) {
            currentAlpha = 1f;
        }

        if (iconTransitionDuration > 0 && prevIcon != null) {
            float icoWidth = Math.min(prevIcon.getWidth(), width);
            float icoHeight = Math.min(prevIcon.getHeight(), height);
            float xOff = x + (width - icoWidth) * 0.5f;
            float yOff = y + (height - icoHeight) * 0.5f;
            int colorAlpha = Color.multiplyColorAlpha(iconColor, prevAlpha);
            prevIcon.draw(context
                    , xOff
                    , yOff
                    , icoWidth, icoHeight, colorAlpha, iconImageFilter);
        }

        if (currentIcon != null) {
            float icoWidth = Math.min(currentIcon.getWidth(), width);
            float icoHeight = Math.min(currentIcon.getHeight(), height);
            float xOff = x + (width - icoWidth) * 0.5f;
            float yOff = y + (height - icoHeight) * 0.5f;
            int colorAlpha = Color.multiplyColorAlpha(iconColor, currentAlpha);
            currentIcon.draw(context
                    , xOff
                    , yOff
                    , icoWidth, icoHeight, colorAlpha, iconImageFilter);
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

        if (wrapWidth) {
            mWidth = Math.max(iconWidth + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(iconHeight + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void firePointer(PointerEvent event) {
        super.firePointer(event);
        if (!event.isConsumed() && event.getType() == PointerEvent.RELEASED && event.getPointerID() == 1) {
            toggle();
        }
    }

    private void updateIconSize() {
        float iaWidth = activeIcon == null ? 0 : activeIcon.getWidth();
        float iaHeight = activeIcon == null ? 0 : activeIcon.getHeight();
        float iiWidth = inactiveIcon == null ? 0 : inactiveIcon.getWidth();
        float iiHeight = inactiveIcon == null ? 0 : inactiveIcon.getHeight();
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
        Drawable icon = isActive() ? activeIcon : inactiveIcon;
        if (icon == null) {
            icon = isActive() ? inactiveIcon : activeIcon;
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

    public RadioGroup getRadioGroup() {
        return radioGroup;
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

    public Drawable getInactiveIcon() {
        return inactiveIcon;
    }

    public void setInactiveIcon(Drawable inactiveIcon) {
        if (this.inactiveIcon != inactiveIcon) {
            this.inactiveIcon = inactiveIcon;
            updateIconSize();
            setCurrentIcon();
        }
    }

    public Drawable getActiveIcon() {
        return activeIcon;
    }

    public void setActiveIcon(Drawable activeIcon) {
        if (this.activeIcon != activeIcon) {
            this.activeIcon = activeIcon;

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

    public int getIconColor() {
        return iconColor;
    }

    public void setIconColor(int iconColor) {
        if (this.iconColor != iconColor) {
            this.iconColor = iconColor;
            invalidate(false);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (this.active != active) {
            boolean old = this.active;
            this.active = active;

            setCurrentIcon();
            setActivated(active);
            fireActiveListener(old);
            if (radioGroup != null) {
                if (active) {
                    radioGroup.select(this);
                } else {
                    radioGroup.unselect(this);
                }
            }
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
        if (!isActive()) {
            setActive(true);
            fireToggle();
        }
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
