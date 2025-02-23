package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.SmartContext;
import flat.graphics.cursor.Cursor;
import flat.graphics.image.Drawable;
import flat.math.Vector2;
import flat.math.shapes.Ellipse;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
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
    private int closeIconBgColor = Color.transparent;

    private boolean isHoveringClose;
    private Cursor closeIconCursor = Cursor.UNSET;

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

        setCloseIcon(attrs.getResourceAsDrawable("close-icon", info, getCloseIcon(), false));
        setCloseIconColor(attrs.getColor("close-icon-color", info, getCloseIconColor()));
        setCloseIconBgColor(attrs.getColor("close-icon-bg-color", info, getCloseIconBgColor()));
        setCloseIconWidth(attrs.getSize("close-icon-width", info, getCloseIconWidth()));
        setCloseIconHeight(attrs.getSize("close-icon-height", info, getCloseIconHeight()));
        setCloseIconSpacing(attrs.getSize("close-icon-spacing", info, getCloseIconSpacing()));
        setCloseIconImageFilter(attrs.getConstant("close-icon-image-filter", info, getCloseIconImageFilter()));
        setCloseIconCursor(attrs.getConstant("close-icon-cursor", info, getCloseIconCursor()));
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

            if (isIconClipCircle()) {
                context.pushClip(new Ellipse(xpos, ypos, iw, ih));
            }
            getIcon().draw(context, xpos, ypos, iw, ih, getIconColor(), getIconImageFilter());
            if (isIconClipCircle()) {
                context.popClip();
            }
        }

        if (tw > 0 && th > 0) {
            float xpos = iconLeft ? boxX + spaceForIcon : spaceForCloseIcon + boxX;
            float ypos = yOff(y, y + height, th);
            drawText(context, xpos, ypos, tw, th);
        }

        if (ciw > 0 && cih > 0 && getCloseIcon() != null) {
            float xpos = iconLeft ? x + width - ciw : x;
            float ypos = yOff(y, y + height, cih);

            if (isHoveringClose) {
                context.setColor(getCloseIconBgColor());
                context.drawEllipse(xpos, ypos, ciw, cih, true);
            }
            getCloseIcon().draw(context, xpos, ypos, ciw, cih, getCloseIconColor(), getCloseIconImageFilter());
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
    public void onLayout(float width, float height) {
        super.onLayout(width, height);
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
    public void firePointer(PointerEvent event) {
        super.firePointer(event);
        if (!event.isConsumed() && event.getType() == PointerEvent.RELEASED && event.getPointerID() == 1) {
            if (isOverActionButton(screenToLocal(event.getX(), event.getY()))) {
                requestClose();
            } else {
                action();
            }
        }
    }

    @Override
    public void fireRipple(float x, float y) {
        if (isOverActionButton(screenToLocal(x, y))) {
            if (isRippleEnabled()) {
                getRipple().setSize(Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)) * 0.5f);
                getRipple().fire((x1 + x2) / 2f, (y1 + y2) / 2f);
                getRipple().release();
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
    public void fireHover(HoverEvent event) {
        super.fireHover(event);
        if (!event.isConsumed() && event.getType() == HoverEvent.MOVED) {
            if (isOverActionButton(screenToLocal(event.getX(), event.getY())) != isHoveringClose) {
                isHoveringClose = !isHoveringClose;
                invalidate(false);
            }
        }
    }

    @Override
    public Cursor getCurrentCursor() {
        return isHoveringClose && closeIconCursor != Cursor.UNSET ? closeIconCursor : super.getCursor();
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

    public int getCloseIconBgColor() {
        return closeIconBgColor;
    }

    public void setCloseIconBgColor(int closeIconBgColor) {
        if (this.closeIconBgColor != closeIconBgColor) {
            this.closeIconBgColor = closeIconBgColor;
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

    public Cursor getCloseIconCursor() {
        return closeIconCursor;
    }

    public void setCloseIconCursor(Cursor closeIconCursor) {
        if (closeIconCursor == null) closeIconCursor = Cursor.UNSET;

        this.closeIconCursor = closeIconCursor;
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
