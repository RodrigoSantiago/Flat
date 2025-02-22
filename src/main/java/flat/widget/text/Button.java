package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
import flat.widget.enums.HorizontalPosition;
import flat.widget.enums.ImageFilter;

public class Button extends Label {

    private UXListener<ActionEvent> actionListener;

    private Drawable icon;
    private float iconWidth;
    private float iconHeight;
    private float iconSpacing;
    private int iconColor = Color.white;
    private ImageFilter iconImageFilter = ImageFilter.LINEAR;
    private HorizontalPosition iconPosition = HorizontalPosition.LEFT;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setActionListener(attrs.getAttributeListener("on-action", ActionEvent.class, controller));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setIcon(attrs.getResourceAsDrawable("icon", info, getIcon(), false));
        setIconColor(attrs.getColor("icon-color", info, getIconColor()));
        setIconWidth(attrs.getSize("icon-width", info, getIconWidth()));
        setIconHeight(attrs.getSize("icon-height", info, getIconHeight()));
        setIconSpacing(attrs.getSize("icon-spacing", info, getIconSpacing()));
        setIconPosition(attrs.getConstant("icon-position", info, getIconPosition()));
        setIconImageFilter(attrs.getConstant("icon-image-filter", info, getIconImageFilter()));
    }

    @Override
    public void onDraw(SmartContext context) {
        drawBackground(context);
        drawRipple(context);

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        context.setTransform2D(getTransform());

        float iW = getLayoutIconWidth();
        float iH = getLayoutIconHeight();

        float spaceForIcon = iW == 0 ? 0 : iW + getIconSpacing();
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

        if (iw > 0 && ih > 0 && getIcon() != null) {
            getIcon().draw(context
                    , getIconPosition() == HorizontalPosition.LEFT ? x : x + spaceForText
                    , yOff(y, y + height, ih)
                    , iw, ih, getIconColor(), getIconImageFilter());
        }

        if (tw > 0 && th > 0 && getFont() != null) {
            context.setColor(getTextColor());
            context.setTextFont(getFont());
            context.setTextSize(getTextSize());
            context.setTextBlur(0);

            context.drawTextSlice(
                      getIconPosition() == HorizontalPosition.LEFT ? x + spaceForIcon : x
                    , yOff(y, y + height, th)
                    , tw, th, getShowText());
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
            mWidth = Math.max(getTextWidth() + extraWidth + (iW > 0 ? iW + getIconSpacing() : 0), getLayoutMinWidth());
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
    public void firePointer(PointerEvent event) {
        super.firePointer(event);
        if (!event.isConsumed() && event.getType() == PointerEvent.RELEASED && event.getPointerID() == 1) {
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

    private float getLayoutIconWidth() {
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

    private float getLayoutIconHeight() {
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
}