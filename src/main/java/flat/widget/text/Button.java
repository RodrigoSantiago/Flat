package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.math.Vector2;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.ImageFilter;

public class Button extends Label {

    private UXListener<ActionEvent> actionListener;

    private Drawable iconImage;
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

        ResourceStream resourceIcon = attrs.getResource("icon-image", info, null);
        if (resourceIcon != null) {
            try {
                Drawable drawable = DrawableReader.parse(resourceIcon);
                setIconImage(drawable);
            } catch (Exception ignored) {
                setIconImage(null);
            }
        }

        setIconScaleHeight(attrs.getBool("icon-scale-height", info, getIconScaleHeight()));
        setIconAlign(attrs.getConstant("icon-align", info, getIconAlign()));
        setIconSpacing(attrs.getSize("icon-spacing", info, getIconSpacing()));
        setIconImageFilter(attrs.getConstant("icon-image-filter", info, getIconImageFilter()));
    }

    @Override
    public void onDraw(SmartContext context) {
        if (iconImage == null) {
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

        float imgWidth = iconImage.getWidth();
        float imgHeight = iconImage.getHeight();
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

            iconImage.draw(context
                    , xOff(x, x + width, tw) + sp
                    , yOff(y, y + height, ih)
                    , iw, ih, 0, iconImageFilter);

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
                iconImage.draw(context
                        , xoff + textWidth + iconSpacing
                        , yoffImg
                        , iw, ih, 0, iconImageFilter);
            } else {
                iconImage.draw(context
                        , xoff
                        , yoffImg
                        , iw, ih, 0, iconImageFilter);
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
        if (iconImage == null) {
            super.onMeasure();
            return;
        }

        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getPrefHeight() == WRAP_CONTENT;

        float iW = iconImage.getWidth();
        float iH = iconImage.getHeight();
        if (iconScaleHeight && getFont() != null) {
            float diff = iW / iH;
            iH = getFont().getHeight(getTextSize());
            iW = iH * diff;
        }

        if (wrapWidth) {
            mWidth = Math.max(getTextWidth() + extraWidth + iW + iconSpacing, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(Math.max(getTextHeight(), iH) + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);
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

    public Drawable getIconImage() {
        return iconImage;
    }

    public void setIconImage(Drawable iconImage) {
        if (this.iconImage != iconImage) {
            this.iconImage = iconImage;
            invalidate(true);
        }
    }

    public float getIconSpacing() {
        return iconSpacing;
    }

    public void setIconSpacing(float iconSpacing) {
        if (this.iconSpacing != iconSpacing) {
            this.iconSpacing = iconSpacing;
            invalidate(true);
        }
    }

    public boolean getIconScaleHeight() {
        return iconScaleHeight;
    }

    public void setIconScaleHeight(boolean iconScaleHeight) {
        if (this.iconScaleHeight != iconScaleHeight) {
            this.iconScaleHeight = iconScaleHeight;
            invalidate(true);
        }
    }

    public HorizontalAlign getIconAlign() {
        return iconAlign;
    }

    public void setIconAlign(HorizontalAlign iconAlign) {
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