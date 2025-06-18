package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.image.Drawable;
import flat.math.Vector2;
import flat.uxml.*;
import flat.widget.enums.HorizontalPosition;
import flat.widget.enums.ImageFilter;

public class Chip extends Button {

    private UXListener<ActionEvent> requestCloseListener;

    private Drawable closeIcon;
    private int closeIconColor = Color.white;
    private ImageFilter closeIconImageFilter = ImageFilter.LINEAR;
    private float closeIconSpacing;
    private float closeIconWidth;
    private float closeIconHeight;

    private float x1, y1, x2, y2;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setRequestCloseListener(attrs.getAttributeListener("on-request-close", ActionEvent.class, controller));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setCloseIcon(attrs.getDrawable("close-icon", info, getCloseIcon(), false));
        setCloseIconColor(attrs.getColor("close-icon-color", info, getCloseIconColor()));
        setCloseIconWidth(attrs.getSize("close-icon-width", info, getCloseIconWidth()));
        setCloseIconHeight(attrs.getSize("close-icon-height", info, getCloseIconHeight()));
        setCloseIconSpacing(attrs.getSize("close-icon-spacing", info, getCloseIconSpacing()));
        setCloseIconImageFilter(attrs.getConstant("close-icon-image-filter", info, getCloseIconImageFilter()));
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        float iW = getLayoutIconWidth();
        float iH = getLayoutIconHeight();
        float ciW = getLayoutCloseIconWidth();
        float ciH = getLayoutCloseIconHeight();

        if (wrapWidth) {
            mWidth = Math.max(getTextWidth() + extraWidth
                    + (iW > 0 ? iW + getIconSpacing() : 0)
                    + (ciW > 0 ? ciW + getCloseIconSpacing() : 0), getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(Math.max(getTextHeight(), Math.max(iH, ciH)) + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void setLayout(float layoutWidth, float layoutHeight) {
        super.setLayout(layoutWidth, layoutHeight);
        updateClosePosition();
    }

    private void updateClosePosition() {
        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        float iaw = Math.min(width, getLayoutCloseIconWidth());
        float iah = Math.min(height, getLayoutCloseIconHeight());

        if (getIconPosition() == HorizontalPosition.LEFT) {
            x1 = x + width - iaw;
            x2 = x + width;
        } else {
            x1 = x;
            x2 = x + iaw;
        }
        y1 = yOff(y, y + height, iah);
        y2 = yOff(y, y + height, iah) + iah;
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

        float iW = getLayoutIconWidth();
        float iH = getLayoutIconHeight();
        float ciW = getLayoutCloseIconWidth();
        float ciH = getLayoutCloseIconHeight();

        float spaceForIcon = (iW > 0 ? iW + getIconSpacing() : 0);
        float spaceForCloseIcon = (ciW > 0 ? ciW + getCloseIconSpacing() : 0);
        float spaceForText = getTextWidth();

        if (spaceForIcon + spaceForCloseIcon + spaceForText > width) {
            if (spaceForCloseIcon > width) {
                spaceForIcon = 0;
                spaceForText = 0;
                spaceForCloseIcon = width;
            } else if (spaceForIcon + spaceForCloseIcon > width) {
                spaceForText = 0;
                spaceForIcon = width - spaceForCloseIcon;
            } else {
                spaceForText = width - spaceForCloseIcon - spaceForIcon;
            }
        }

        float tw = spaceForText;
        float th = Math.min(height, getTextHeight());
        float iw = Math.min(iW, Math.max(spaceForIcon, 0));
        float ih = Math.min(height, iH);
        float ciw = Math.min(ciW, Math.max(spaceForCloseIcon, 0));
        float cih = Math.min(height, ciH);

        boolean iconLeft = getIconPosition() == HorizontalPosition.LEFT;
        float boxX = xOff(x, x + width - spaceForCloseIcon, spaceForIcon + spaceForText);

        if (iw > 0 && ih > 0 && getIcon() != null) {
            float xpos = iconLeft ? boxX : spaceForCloseIcon + boxX + spaceForText + spaceForIcon - iw;
            float ypos = yOff(y, y + height, ih);
            drawIcon(graphics, xpos, ypos, iw, ih);
        }

        if (tw > 0 && th > 0) {
            float xpos = iconLeft ? boxX + spaceForIcon : spaceForCloseIcon + boxX;
            float ypos = yOff(y, y + height, th);
            drawText(graphics, xpos, ypos, tw, th);
        }

        if (ciw > 0 && cih > 0 && getCloseIcon() != null) {
            float xpos = iconLeft ? x + width - ciw : x;
            float ypos = yOff(y, y + height, cih);
            getCloseIcon().draw(graphics, xpos, ypos, ciw, cih, getCloseIconColor(), getCloseIconImageFilter());
        }
    }

    @Override
    public void fireRipple(float x, float y) {
        if (isOverActionButton(screenToLocal(x, y))) {
            if (isRippleEnabled()) {
                float sp = getCloseIconSpacing();
                var ripple = getRipple();
                ripple.setSize(Math.max(Math.abs(x1 - x2) + sp, Math.abs(y1 - y2) + sp) * 0.5f);
                ripple.fire((x1 + x2) / 2f, (y1 + y2) / 2f);
            }
        } else {
            super.fireRipple(x, y);
        }
    }

    public UXListener<ActionEvent> getRequestCloseListener() {
        return requestCloseListener;
    }

    public void setRequestCloseListener(UXListener<ActionEvent> requestCloseListener) {
        this.requestCloseListener = requestCloseListener;
    }

    private void fireRequestClose() {
        if (requestCloseListener != null) {
            UXListener.safeHandle(requestCloseListener, new ActionEvent(this));
        }
    }

    public void requestClose() {
        fireRequestClose();
    }

    @Override
    public void hover(HoverEvent event) {
        super.hover(event);
        if (!event.isConsumed() && event.getType() == HoverEvent.MOVED) {
            setUndefined(isOverActionButton(screenToLocal(event.getX(), event.getY())));
        }
        if (!event.isConsumed() && event.getType() == HoverEvent.EXITED) {
            setUndefined(false);
        }
    }

    @Override
    public void pointer(PointerEvent event) {
        UXListener.safeHandle(getPointerListener(), event);
        if (!event.isConsumed() && event.getPointerID() == 1 && event.getType() == PointerEvent.PRESSED) {
            event.consume();
        }
        if (!event.isConsumed() && event.getPointerID() == 1 && event.getType() == PointerEvent.RELEASED) {
            event.consume();
            if (isOverActionButton(screenToLocal(event.getX(), event.getY()))) {
                requestClose();
            } else {
                action();
            }
        }
    }

    private boolean isOverActionButton(Vector2 local) {
        return closeIcon != null && !(local.x < x1 || local.x > x2 || local.y < y1 || local.y > y2);
    }

    public int getCloseIconColor() {
        return closeIconColor;
    }

    public void setCloseIconColor(int closeIconColor) {
        if (this.closeIconColor != closeIconColor) {
            this.closeIconColor = closeIconColor;
            invalidate(false);
        }
    }

    public Drawable getCloseIcon() {
        return closeIcon;
    }

    public void setCloseIcon(Drawable closeIcon) {
        if (this.closeIcon != closeIcon) {
            this.closeIcon = closeIcon;
            invalidate(isWrapContent());
        }
    }

    public float getCloseIconSpacing() {
        return closeIconSpacing;
    }

    public void setCloseIconSpacing(float closeIconSpacing) {
        if (this.closeIconSpacing != closeIconSpacing) {
            this.closeIconSpacing = closeIconSpacing;
            invalidate(isWrapContent());
        }
    }

    public float getCloseIconWidth() {
        return closeIconWidth;
    }

    public void setCloseIconWidth(float closeIconWidth) {
        if (this.closeIconWidth != closeIconWidth) {
            this.closeIconWidth = closeIconWidth;
            invalidate(isWrapContent());
        }
    }

    private float getLayoutCloseIconWidth() {
        return closeIcon == null ? 0 : closeIconWidth == 0 || closeIconWidth == MATCH_PARENT ? getTextHeight() : closeIconWidth;
    }

    public float getCloseIconHeight() {
        return closeIconHeight;
    }

    public void setCloseIconHeight(float closeIconHeight) {
        if (this.closeIconHeight != closeIconHeight) {
            this.closeIconHeight = closeIconHeight;
            invalidate(isWrapContent());
        }
    }

    private float getLayoutCloseIconHeight() {
        return closeIcon == null ? 0 : closeIconHeight == 0 || closeIconHeight == MATCH_PARENT ? getTextHeight() : closeIconHeight;
    }

    public ImageFilter getCloseIconImageFilter() {
        return closeIconImageFilter;
    }

    public void setCloseIconImageFilter(ImageFilter closeIconImageFilter) {
        if (closeIconImageFilter == null) closeIconImageFilter = ImageFilter.LINEAR;

        if (this.closeIconImageFilter != closeIconImageFilter) {
            this.closeIconImageFilter = closeIconImageFilter;
            invalidate(false);
        }
    }
}
