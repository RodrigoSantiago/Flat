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
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.ImageFilter;

public class Button extends Label {

    private UXListener<ActionEvent> actionListener;

    private Drawable icon;
    private int iconColor = Color.white;
    private ImageFilter iconImageFilter = ImageFilter.LINEAR;
    private float iconSpacing;
    private boolean iconScaleHeight;
    private HorizontalAlign iconAlign = HorizontalAlign.LEFT;

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
        setIconScaleHeight(attrs.getBool("icon-scale-height", info, getIconScaleHeight()));
        setIconAlign(attrs.getConstant("icon-align", info, getIconAlign()));
        setIconSpacing(attrs.getSize("icon-spacing", info, getIconSpacing()));
        setIconImageFilter(attrs.getConstant("icon-image-filter", info, getIconImageFilter()));
    }

    @Override
    public void onDraw(SmartContext context) {
        if (icon == null) {
            super.onDraw(context);
            return;
        }

        backgroundDraw(context);
        context.setTransform2D(getTransform());

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        float imgWidth = icon.getWidth();
        float imgHeight = icon.getHeight();
        if (iconScaleHeight && getFont() != null) {
            float diff = imgWidth / imgHeight;
            imgHeight = getFont().getHeight(getTextSize());
            imgWidth = imgHeight * diff;
        }

        float textWidth = Math.min(getTextWidth(), Math.max(0, width - iconSpacing - imgWidth));
        float textHeight = Math.min(getTextHeight(), height);

        if (getShowText() == null || getFont() == null || textWidth <= 0) {
            float iw = Math.min(imgWidth, width);
            float ih = Math.min(imgHeight, height);
            float tw = Math.min(imgWidth + iconSpacing, width);
            float sp = iconAlign == HorizontalAlign.RIGHT ? Math.min(iconSpacing, width - iw) : 0;

            icon.draw(context
                    , xOff(x, x + width, tw) + sp
                    , yOff(y, y + height, ih)
                    , iw, ih, iconColor, iconImageFilter);

        } else {
            float iw = imgWidth;
            float ih = Math.min(imgHeight, height);

            float tw = textWidth + iconSpacing + imgWidth;
            float th = Math.min(Math.max(ih, getTextHeight()), height);

            float xoff = xOff(x, x + width, tw);
            float yoffGroup = yOff(y, y + height, th);
            float yoffText = yOff(yoffGroup, yoffGroup + th, textHeight);
            float yoffImg = yOff(yoffGroup, yoffGroup + th, ih);

            context.setColor(getTextColor());
            context.setTextFont(getFont());
            context.setTextSize(getTextSize());
            context.setTextBlur(0);

            if (iconAlign == HorizontalAlign.RIGHT) {
                context.drawTextSlice(
                          xoff
                        , yoffText
                        , textWidth
                        , textHeight
                        , getShowText());
                icon.draw(context
                        , xoff + textWidth + iconSpacing
                        , yoffImg
                        , iw, ih, iconColor, iconImageFilter);
            } else {
                icon.draw(context
                        , xoff
                        , yoffImg
                        , iw, ih, iconColor, iconImageFilter);
                context.drawTextSlice(
                          xoff + imgWidth + iconSpacing
                        , yoffText
                        , textWidth
                        , textHeight
                        , getShowText());
            }
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

        float iW = icon.getWidth();
        float iH = icon.getHeight();
        if (iconScaleHeight && getFont() != null) {
            float diff = iW / iH;
            iH = getFont().getHeight(getTextSize());
            iW = iH * diff;
        }

        if (wrapWidth) {
            mWidth = Math.max(getTextWidth() + extraWidth + iW + iconSpacing, getLayoutMinWidth());
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
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);
        if (!pointerEvent.isConsumed() && pointerEvent.getType() == PointerEvent.PRESSED) {
            requestFocus(true);
        }
        if (!pointerEvent.isConsumed() && pointerEvent.getType() == PointerEvent.RELEASED) {
            fire();
        }
    }

    public UXListener<ActionEvent> getActionListener() {
        return actionListener;
    }

    public void setActionListener(UXListener<ActionEvent> actionListener) {
        this.actionListener = actionListener;
    }

    public void fireAction(ActionEvent event) {
        if (actionListener != null) {
            actionListener.handle(event);
        }
    }

    public void fire() {
        fireAction(new ActionEvent(this));
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

    public boolean getIconScaleHeight() {
        return iconScaleHeight;
    }

    public void setIconScaleHeight(boolean iconScaleHeight) {
        if (this.iconScaleHeight != iconScaleHeight) {
            this.iconScaleHeight = iconScaleHeight;
            invalidate(isWrapContent());
        }
    }

    public HorizontalAlign getIconAlign() {
        return iconAlign;
    }

    public void setIconAlign(HorizontalAlign iconAlign) {
        if (iconAlign == null) iconAlign = HorizontalAlign.LEFT;

        if (this.iconAlign != iconAlign) {
            this.iconAlign = iconAlign;
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