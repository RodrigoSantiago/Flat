package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.graphics.text.Align;
import flat.math.Vector2;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
import flat.widget.enums.ImageFilter;

public class Button extends Label {

    private UXListener<ActionEvent> actionListener;

    private Drawable iconImage;
    private ImageFilter iconImageFilter = ImageFilter.NEAREST;
    private float iconSpacing;
    private Align.Horizontal iconAlign = Align.Horizontal.LEFT;

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

        setIconAlign(attrs.getConstant("icon-align", info, getIconAlign()));
        setIconSpacing(attrs.getSize("icon-spacing", info, getIconSpacing()));
        setIconImageFilter(attrs.getConstant("icon-image-filter", info, getIconImageFilter()));
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(context, getBackgroundColor(), getBorderColor(), getRippleColor());

        context.setTransform2D(getTransform());

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (iconImage == null) {
            if (getShowText() != null && !getShowText().isEmpty()) {
                context.setColor(getTextColor());
                context.setTextFont(getFont());
                context.setTextSize(getTextSize());
                context.setTextVerticalAlign(Align.Vertical.TOP);
                context.setTextHorizontalAlign(Align.Horizontal.LEFT);
                context.drawTextSlice(
                        xOff(x, x + width, Math.min(getTextWidth(), width)),
                        yOff(y, y + height, Math.min(getTextHeight(), height)),
                        width, getShowText());
            }
        } else {
            if (getShowText() != null && !getShowText().isEmpty()) {
                context.setColor(getTextColor());
                context.setTextFont(getFont());
                context.setTextSize(getTextSize());
                context.setTextVerticalAlign(Align.Vertical.TOP);
                context.setTextHorizontalAlign(Align.Horizontal.LEFT);

                float tw = Math.min(getTextWidth() + iconSpacing + iconImage.getWidth(), width);
                float xoff = xOff(x, x + width, tw);

                if (iconAlign == Align.Horizontal.RIGHT) {
                    context.drawTextSlice(xoff,
                            yOff(y, y + height, getTextHeight()),
                            width - iconSpacing - iconImage.getWidth(), getShowText());
                    iconImage.draw(context, xoff + getTextWidth() + iconSpacing,
                            yOff(y, y + height, iconImage.getHeight()),
                            iconImage.getWidth(), iconImage.getHeight(), 0, iconImageFilter);
                } else {
                    context.drawTextSlice(xoff + iconImage.getWidth() + iconSpacing,
                            yOff(y, y + height, getTextHeight()),
                            width - iconSpacing - iconImage.getWidth(), getShowText());
                    iconImage.draw(context, xoff,
                            yOff(y, y + height, iconImage.getHeight()),
                            iconImage.getWidth(), iconImage.getHeight(), 0, iconImageFilter);
                }
            } else {
                context.setColor(getTextColor());
                iconImage.draw(context,
                        xOff(x, x + width, iconImage.getWidth()),
                        yOff(y, y + height, iconImage.getHeight()), iconImage.getWidth(), iconImage.getHeight()
                        , 0, iconImageFilter);
            }
        }
    }

    @Override
    public void onMeasure() {
        if (iconImage == null) {
            super.onMeasure();
        } else {
            float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
            float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

            float mWidth;
            float mHeight;
            boolean wrapWidth = getPrefWidth() == WRAP_CONTENT;
            boolean wrapHeight = getPrefHeight() == WRAP_CONTENT;

            if (wrapWidth) {
                mWidth = Math.max(getTextWidth() + extraWidth + iconImage.getWidth() + iconSpacing, Math.max(getPrefWidth(), getLayoutMinWidth()));
            } else {
                mWidth = Math.max(getPrefWidth(), getLayoutMinWidth());
            }
            if (wrapHeight) {
                mHeight = Math.max(getTextHeight() + extraHeight + iconImage.getHeight(), Math.max(getPrefHeight(), getLayoutMinHeight()));
            } else {
                mHeight = Math.max(getPrefHeight(), getLayoutMinHeight());
            }

            setMeasure(mWidth, mHeight);
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);
        if (!pointerEvent.isConsumed() && pointerEvent.getType() == PointerEvent.RELEASED) {
            fire();
        }
    }

    @Override
    public void fireRipple(float x, float y) {
        if (isRippleOverflow() && (getText() == null || getText().isEmpty()) && iconImage != null) {
            Vector2 p = localToScreen(getInX() + getInWidth() / 2, getInY() + getHeight() / 2);
            getRipple().setSize(getInWidth());
            super.fireRipple(p.x, p.y);
        } else {
            super.fireRipple(x, y);
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

    public Align.Horizontal getIconAlign() {
        return iconAlign;
    }

    public void setIconAlign(Align.Horizontal iconAlign) {
        if (this.iconAlign != iconAlign) {
            this.iconAlign = iconAlign;
            invalidate(false);
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
}