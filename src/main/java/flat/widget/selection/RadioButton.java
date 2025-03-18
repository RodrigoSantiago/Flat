package flat.widget.selection;

import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.image.Drawable;
import flat.uxml.*;
import flat.widget.Group;
import flat.widget.Widget;
import flat.widget.enums.ImageFilter;
import flat.window.Activity;

public class RadioButton extends Widget {

    private UXListener<ActionEvent> toggleListener;
    private UXValueListener<Boolean> activatedListener;

    private Drawable icon;
    private float iconWidth;
    private float iconHeight;
    private int iconColor = Color.white;
    private ImageFilter iconImageFilter = ImageFilter.LINEAR;

    RadioGroup radioGroup;

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
        setActivated(attrs.getAttributeBool("activated", isActivated()));
        setToggleListener(attrs.getAttributeListener("on-toggle", ActionEvent.class, controller));
        setActivatedListener(attrs.getAttributeValueListener("on-activated-change", Boolean.class, controller));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setIconColor(attrs.getColor("icon-color", info, getIconColor()));
        setIconImageFilter(attrs.getConstant("icon-image-filter", info, getIconImageFilter()));
        setIcon(attrs.getResourceAsDrawable("icon", info, getIcon(), false));
        setIconWidth(attrs.getSize("icon-width", info, getIconWidth()));
        setIconHeight(attrs.getSize("icon-height", info, getIconHeight()));
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
            mWidth = Math.max(getLayoutIconWidth() + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(getLayoutIconHeight() + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onDraw(Graphics graphics) {
        if (discardDraw(graphics)) return;

        drawBackground(graphics);
        drawRipple(graphics);

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        graphics.setTransform2D(getTransform());

        float icoWidth = Math.min(getLayoutIconWidth(), width);
        float icoHeight = Math.min(getLayoutIconHeight(), height);
        if (icoWidth > 0 && icoHeight > 0 && getIcon() != null) {
            getIcon().draw(graphics, x, y, icoWidth, icoHeight, getIconColor(), getIconImageFilter());
        }
    }

    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        if (!event.isConsumed() && event.getPointerID() == 1 && event.getType() == PointerEvent.RELEASED) {
            toggle();
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

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        if (this.icon != icon) {
            this.icon = icon;
            invalidate(isWrapContent());
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

    public float getIconWidth() {
        return iconWidth;
    }

    public void setIconWidth(float iconWidth) {
        if (this.iconWidth != iconWidth) {
            this.iconWidth = iconWidth;
            invalidate(isWrapContent());
        }
    }
    
    protected float getLayoutIconWidth() {
        return iconWidth == 0 && icon != null ? icon.getWidth() : iconWidth;
    }

    public float getIconHeight() {
        return iconHeight;
    }

    public void setIconHeight(float iconHeight) {
        if (this.iconHeight != iconHeight) {
            this.iconHeight = iconHeight;
            invalidate(isWrapContent());
        }
    }

    protected float getLayoutIconHeight() {
        return iconHeight == 0 && icon != null ? icon.getHeight() : iconHeight;
    }

    @Override
    public void setActivated(boolean active) {
        if (this.isActivated() != active) {
            boolean old = this.isActivated();
            super.setActivated(active);

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
        if (!isActivated()) {
            setActivated(true);
            fireToggle();
        }
    }

    public UXValueListener<Boolean> getActivatedListener() {
        return activatedListener;
    }

    public void setActivatedListener(UXValueListener<Boolean> activatedListener) {
        this.activatedListener = activatedListener;
    }

    private void fireActiveListener(boolean oldValue) {
        if (activatedListener != null && oldValue != isActivated()) {
            UXValueListener.safeHandle(activatedListener, new ValueChange<>(this, oldValue, isActivated()));
        }
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
