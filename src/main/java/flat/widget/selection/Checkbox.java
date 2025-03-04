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
import flat.widget.enums.ImageFilter;
import flat.widget.enums.SelectionState;
import flat.window.Activity;

public class Checkbox extends Widget {

    private UXListener<ActionEvent> toggleListener;
    private UXValueListener<SelectionState> selectionStateListener;
    private SelectionState selectionState = SelectionState.INDETERMINATE;

    private ImageFilter iconImageFilter = ImageFilter.LINEAR;
    private Drawable iconInactive;
    private Drawable iconActive;
    private Drawable iconIndeterminate;
    private int iconColor = Color.white;
    private float iconTransitionDuration;

    private IconChange iconChangeAnimation = new IconChange();
    private Drawable prevIcon;
    private Drawable currentIcon;
    private float iconWidth;
    private float iconHeight;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setSelectionState(attrs.getAttributeConstant("selection-state", getSelectionState()));
        setToggleListener(attrs.getAttributeListener("on-toggle", ActionEvent.class, controller));
        setSelectionStateListener(attrs.getAttributeValueListener("on-selection-state-change", SelectionState.class, controller));
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
        setIconIndeterminate(attrs.getResourceAsDrawable("icon-indeterminate", info, getIconIndeterminate(), false));
        setIconTransitionDuration(attrs.getNumber("icon-transition-duration", info, getIconTransitionDuration()));
    }

    @Override
    public void onDraw(Graphics graphics) {
        drawBackground(graphics);
        drawRipple(graphics);

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (width <= 0 || height <= 0) return;

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
            float xOff = x + (width - icoWidth) * 0.5f;
            float yOff = y + (height - icoHeight) * 0.5f;
            int colorAlpha = Color.multiplyColorAlpha(iconColor, prevAlpha);
            prevIcon.draw(graphics
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
            currentIcon.draw(graphics
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
        float idWidth = iconIndeterminate == null ? 0 : iconIndeterminate.getWidth();
        float idHeight = iconIndeterminate == null ? 0 : iconIndeterminate.getHeight();
        float nextWidth = Math.max(Math.max(iaWidth, iiWidth), idWidth);
        float nextHeight = Math.max(Math.max(iaHeight, iiHeight), idHeight);
        if (nextWidth != iconWidth || nextHeight != iconHeight) {
            this.iconWidth = nextWidth;
            this.iconHeight = nextHeight;
            invalidate(isWrapContent());
        } else {
            invalidate(false);
        }
    }

    private void setCurrentIcon() {
        Drawable icon = isActive() ? iconActive : isIndeterminate() ? iconIndeterminate : iconInactive;
        if (icon == null) {
            icon = iconInactive;
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

    public SelectionState getSelectionState() {
        return selectionState;
    }

    public void setSelectionState(SelectionState selectionState) {
        if (selectionState == null) selectionState = SelectionState.INDETERMINATE;

        if (this.selectionState != selectionState) {
            SelectionState oldValue = this.selectionState;

            this.selectionState = selectionState;
            if (selectionState == SelectionState.ACTIVE) {
                setActivated(true);
            } else {
                setActivated(false);
            }

            setCurrentIcon();
            fireSelectedListener(oldValue);
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

    public Drawable getIconIndeterminate() {
        return iconIndeterminate;
    }

    public void setIconIndeterminate(Drawable iconIndeterminate) {
        if (this.iconIndeterminate != iconIndeterminate) {
            this.iconIndeterminate = iconIndeterminate;

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

    public boolean isInactive() {
        return getSelectionState() == SelectionState.INACTIVE;
    }

    public boolean isActive() {
        return getSelectionState() == SelectionState.ACTIVE;
    }

    public boolean isIndeterminate() {
        return getSelectionState() == SelectionState.INDETERMINATE;
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
        setSelectionState(isActive() ? SelectionState.INACTIVE : SelectionState.ACTIVE);
        fireToggle();
    }

    public UXValueListener<SelectionState> getSelectionStateListener() {
        return selectionStateListener;
    }

    public void setSelectionStateListener(UXValueListener<SelectionState> selectionStateListener) {
        this.selectionStateListener = selectionStateListener;
    }

    private void fireSelectedListener(SelectionState oldValue) {
        if (selectionStateListener != null && oldValue != selectionState) {
            UXValueListener.safeHandle(selectionStateListener, new ValueChange<>(this, oldValue, selectionState));
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
