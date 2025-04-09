package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.context.paints.ImagePattern;
import flat.graphics.image.Drawable;
import flat.graphics.image.PixelMap;
import flat.math.shapes.Ellipse;
import flat.uxml.*;
import flat.widget.enums.HorizontalPosition;
import flat.widget.enums.ImageFilter;

public class Button extends Label {

    private UXListener<ActionEvent> actionListener;
    private UXValueListener<Boolean> activatedListener;

    private Drawable icon;
    private float iconWidth;
    private float iconHeight;
    private float iconSpacing;
    private int iconColor = Color.white;
    private ImageFilter iconImageFilter = ImageFilter.LINEAR;
    private HorizontalPosition iconPosition = HorizontalPosition.LEFT;
    private boolean iconClipCircle;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setActivated(attrs.getAttributeBool("activated", isActivated()));
        setActionListener(attrs.getAttributeListener("on-action", ActionEvent.class, controller));
        setActivatedListener(attrs.getAttributeValueListener("on-activated-change", Boolean.class, controller));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setIcon(attrs.getDrawable("icon", info, getIcon(), false));
        setIconColor(attrs.getColor("icon-color", info, getIconColor()));
        setIconWidth(attrs.getSize("icon-width", info, getIconWidth()));
        setIconHeight(attrs.getSize("icon-height", info, getIconHeight()));
        setIconSpacing(attrs.getSize("icon-spacing", info, getIconSpacing()));
        setIconPosition(attrs.getConstant("icon-position", info, getIconPosition()));
        setIconImageFilter(attrs.getConstant("icon-image-filter", info, getIconImageFilter()));
        setIconClipCircle(attrs.getBool("icon-clip-circle", info, isIconClipCircle()));
    }

    @Override
    public void onDraw(Graphics graphics) {
        if (discardDraw(graphics)) return;

        drawBackground(graphics);
        drawRipple(graphics);

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        graphics.setTransform2D(getTransform());

        float iW = getLayoutIconWidth();
        float iH = getLayoutIconHeight();

        float spaceForIcon = iW == 0 ? 0 : iW + (hasText() ? getIconSpacing() : 0);
        float spaceForText = getTextWidth();

        if (spaceForIcon > width) {
            spaceForText = 0;
            spaceForIcon = width;
        } else if (spaceForIcon + spaceForText > width) {
            spaceForText = width - spaceForIcon;
        }

        float tw = spaceForText;
        float th = Math.min(height, getTextHeight());
        float iw = Math.min(iW, Math.max(spaceForIcon, 0));
        float ih = Math.min(height, iH);

        boolean iconLeft = getIconPosition() == HorizontalPosition.LEFT;

        float boxX = xOff(x, x + width, spaceForIcon + spaceForText);

        if (iw > 0 && ih > 0 && getIcon() != null) {
            float xpos = iconLeft ? boxX : boxX + spaceForText + Math.max(0, spaceForIcon - iW);
            float ypos = yOff(y, y + height, ih);
            drawIcon(graphics, xpos, ypos, iw, ih);
        }

        if (tw > 0 && th > 0 && getTextFont() != null) {
            float xpos = iconLeft ? boxX + spaceForIcon : boxX;
            float ypos = yOff(y, y + height, th);
            drawText(graphics, xpos, ypos, tw, th);
        }
    }

    protected boolean hasText() {
        return getShowText() != null && !getShowText().isEmpty();
    }

    protected void drawIcon(Graphics graphics, float x, float y, float width, float height) {
        if (!isIconClipCircle()) {
            getIcon().draw(graphics, x, y, width, height, getIconColor(), getIconImageFilter());
        } else if (getIcon() instanceof PixelMap pixelMap) {
            var tex = pixelMap.getTexture();
            ImagePattern paint = new ImagePattern.Builder(tex, x, y, width, height)
                    .color(getIconColor())
                    .build();
            graphics.setPaint(paint);
            graphics.drawEllipse(x, y, width, height, true);
        } else {
            graphics.pushClip(new Ellipse(x, y, width, height));
            getIcon().draw(graphics, x, y, width, height, getIconColor(), getIconImageFilter());
            graphics.popClip();
        }
    }

    @Override
    public void onMeasure() {
        if (icon == null) {
            super.onMeasure();
            return;
        }

        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        float iW = getLayoutIconWidth();
        float iH = getLayoutIconHeight();

        if (wrapWidth) {
            mWidth = Math.max(getTextWidth() + extraWidth + (iW > 0 ? iW + (hasText() ? getIconSpacing() : 0) : 0), getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(Math.max(getTextHeight(), iH) + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        if (!event.isConsumed() && event.getPointerID() == 1 && event.getType() == PointerEvent.PRESSED) {
            event.consume();
        }
        if (!event.isConsumed() && event.getPointerID() == 1 && event.getType() == PointerEvent.RELEASED) {
            event.consume();
            action();
        }
    }

    public UXListener<ActionEvent> getActionListener() {
        return actionListener;
    }

    public void setActionListener(UXListener<ActionEvent> actionListener) {
        this.actionListener = actionListener;
    }

    private void fireAction() {
        if (actionListener != null) {
            UXListener.safeHandle(actionListener, new ActionEvent(this));
        }
    }

    public void action() {
        fireAction();
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

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        if (this.icon != icon) {
            this.icon = icon;
            invalidate(isWrapContent());
        }
    }

    public float getIconSpacing() {
        return iconSpacing;
    }

    public void setIconSpacing(float iconSpacing) {
        if (this.iconSpacing != iconSpacing) {
            this.iconSpacing = iconSpacing;
            invalidate(isWrapContent());
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
        return icon == null ? 0 : iconWidth == 0 || iconWidth == MATCH_PARENT ? getTextHeight() : iconWidth;
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
        return icon == null ? 0 : iconHeight == 0 || iconHeight == MATCH_PARENT ? getTextHeight() : iconHeight;
    }

    public HorizontalPosition getIconPosition() {
        return iconPosition;
    }

    public void setIconPosition(HorizontalPosition iconPosition) {
        if (iconPosition == null) iconPosition = HorizontalPosition.LEFT;

        if (this.iconPosition != iconPosition) {
            this.iconPosition = iconPosition;
            invalidate(false);
        }
    }

    public ImageFilter getIconImageFilter() {
        return iconImageFilter;
    }

    public void setIconImageFilter(ImageFilter iconImageFilter) {
        if (iconImageFilter == null) iconImageFilter = ImageFilter.LINEAR;

        if (this.iconImageFilter != iconImageFilter) {
            this.iconImageFilter = iconImageFilter;
            invalidate(false);
        }
    }

    public boolean isIconClipCircle() {
        return iconClipCircle;
    }

    public void setIconClipCircle(boolean iconClipCircle) {
        if (this.iconClipCircle != iconClipCircle) {
            this.iconClipCircle = iconClipCircle;
            invalidate(false);
        }
    }

    public void setActivated(boolean active) {
        if (this.isActivated() != active) {
            boolean old = this.isActivated();
            super.setActivated(active);

            fireActiveListener(old);
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
}