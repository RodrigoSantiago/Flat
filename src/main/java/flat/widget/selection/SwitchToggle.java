package flat.widget.selection;

import flat.animations.Interpolation;
import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.image.Drawable;
import flat.math.stroke.BasicStroke;
import flat.uxml.*;
import flat.widget.Widget;
import flat.widget.enums.Direction;
import flat.widget.enums.ImageFilter;
import flat.widget.enums.LineCap;
import flat.window.Activity;

public class SwitchToggle extends Widget {

    private UXListener<ActionEvent> toggleListener;
    private UXValueListener<Boolean> activatedListener;

    private Direction direction = Direction.HORIZONTAL;

    private Drawable icon;
    private float iconWidth;
    private float iconHeight;
    private int iconColor = Color.white;
    private int iconBgColor;
    private ImageFilter iconImageFilter = ImageFilter.LINEAR;
    private float slideTransitionDuration;

    private float lineWidth = 1;
    private int lineColor;
    private LineCap lineCap = LineCap.BUTT;

    private final IconChange iconSlideAnimation = new IconChange(Interpolation.fade);

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
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
        setIconBgColor(attrs.getColor("icon-bg-color", info, getIconBgColor()));
        setLineWidth(attrs.getSize("line-width", info, getLineWidth()));
        setLineColor(attrs.getColor("line-color", info, getLineColor()));
        setLineCap(attrs.getConstant("line-cap", info, getLineCap()));
        setSlideTransitionDuration(attrs.getNumber("slide-transition-duration", info, getSlideTransitionDuration()));
        setDirection(attrs.getConstant("direction", info, getDirection()));
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
            mWidth = Math.max(getLayoutIconWidth() * (hor ? 2f : 1f) + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(getLayoutIconHeight() * (hor ? 1f : 2f) + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onDraw(Graphics graphics) {
        drawBackground(graphics);
        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        boolean hor = direction == Direction.HORIZONTAL || direction == Direction.IHORIZONTAL;
        boolean rev = direction == Direction.IHORIZONTAL || direction == Direction.IVERTICAL;

        float slide = iconSlideAnimation.isPlaying() ? iconSlideAnimation.getInterpolatedPosition() : 1f;
        if (!isActivated()) {
            slide = 1 - slide;
        }
        if (rev) {
            slide = 1 - slide;
        }

        float cx1, cy1, cx2, cy2;

        float icoWidth = Math.min(getLayoutIconWidth(), width);
        float icoHeight = Math.min(getLayoutIconHeight(), height);
        if (hor) {
            cx1 = (x + icoWidth * 0.5f);
            cy1 = y + height * 0.5f;
            cx2 = (x + (width - icoWidth * 0.5f));
            cy2 = y + height * 0.5f;

        } else {
            cx1 = x + width * 0.5f;
            cy1 = (y + icoHeight * 0.5f);
            cx2 = x + width * 0.5f;
            cy2 = (y + (height - icoHeight * 0.5f));
        }

        float px = cx1 * (1 - slide) + cx2 * slide;
        float py = cy1 * (1 - slide) + cy2 * slide;

        graphics.setTransform2D(getTransform());

        float lineW = Math.min(getLineWidth(), Math.min(height, width));
        if (lineW > 0 && Color.getAlpha(getLineColor()) > 0) {
            graphics.setStroker(new BasicStroke(lineW, getLineCap().ordinal(), 0));
            graphics.setColor(getLineColor());
            graphics.drawLine(cx1, cy1, cx2, cy2);
        }

        if (Color.getAlpha(getIconBgColor()) > 0) {
            float w = Math.min(getOutWidth(), getOutHeight());
            graphics.setColor(getIconBgColor());
            graphics.drawEllipse(px - w * 0.5f, py - w * 0.5f, w, w, true);
        }

        if (isRippleEnabled()) {
            getRipple().setPosition(px, py);
            drawRipple(graphics);
        }

        if (icoWidth > 0 && icoHeight > 0 && getIcon() != null) {
            float el = getElevation();
            float sw = Math.min(getOutWidth(), icoWidth);
            float sh = Math.min(getOutHeight(), icoHeight);
            float c = Math.max(sw, sh);
            float op = Color.getOpacity(getIconColor()) * 0.20f;
            graphics.drawRoundRectShadow(px - sw * 0.5f, py - sh * 0.5f, sw, sh, c, c, c, c, el + 2f, op);

            getIcon().draw(graphics
                    , px - icoWidth * 0.5f
                    , py - icoHeight * 0.5f
                    , icoWidth, icoHeight, getIconColor(), getIconImageFilter());
        }
    }

    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        if (!event.isConsumed() && event.getPointerID() == 1 && event.getType() == PointerEvent.RELEASED) {
            toggle();
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

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        if (this.icon != icon) {
            this.icon = icon;
            invalidate(isWrapContent());
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

    public int getIconBgColor() {
        return iconBgColor;
    }

    public void setIconBgColor(int iconBgColor) {
        if (this.iconBgColor != iconBgColor) {
            this.iconBgColor = iconBgColor;
            invalidate(false);
        }
    }

    protected float getLayoutIconHeight() {
        return iconHeight == 0 && icon != null ? icon.getHeight() : iconHeight;
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

    @Override
    public void setActivated(boolean active) {
        if (this.isActivated() != active) {
            boolean old = this.isActivated();
            super.setActivated(active);

            if (slideTransitionDuration > 0) {
                iconSlideAnimation.setDuration(slideTransitionDuration);
                iconSlideAnimation.play(getActivity());
            }
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
        setActivated(!isActivated());
        fireToggle();
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

        public IconChange(Interpolation interpolation) {
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
