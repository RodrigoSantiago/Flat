package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.ActionListener;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.graphics.text.Align;
import flat.resources.Resource;
import flat.uxml.Controller;
import flat.uxml.UXStyleAttrs;

import java.lang.reflect.Method;

public class Button extends Label {

    private ActionListener actionListener;

    private Drawable iconImage;
    private float iconSpacing;
    private Align.Horizontal iconAlign = Align.Horizontal.LEFT;

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        Method handle = style.asListener("on-action", ActionEvent.class, controller);
        if (handle != null) {
            setActionListener(new ActionListener.AutoActionListener(controller, handle));
        }
    }

    public void applyStyle() {
        super.applyStyle();
        if (getStyle() == null) return;

        StateInfo info = getStateInfo();

        Resource res = getStyle().asResource("icon-image", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setIconImage(drawable);
            }
        }

        setIconAlign(getStyle().asConstant("icon-align", info, getIconAlign()));
        setIconSpacing(getStyle().asSize("icon-spacing", info, getIconSpacing()));
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(getBackgroundColor(), getBorderColor(), getRippleColor(), context);

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
                            iconImage.getWidth(), iconImage.getHeight(), 0);
                } else {
                    context.drawTextSlice(xoff + iconImage.getWidth() + iconSpacing,
                            yOff(y, y + height, getTextHeight()),
                            width - iconSpacing - iconImage.getWidth(), getShowText());
                    iconImage.draw(context, xoff,
                            yOff(y, y + height, iconImage.getHeight()),
                            iconImage.getWidth(), iconImage.getHeight(), 0);
                }
            } else {
                context.setColor(getTextColor());
                iconImage.draw(context,
                        xOff(x, x + width, iconImage.getWidth()),
                        yOff(y, y + height, iconImage.getHeight()), iconImage.getWidth(), iconImage.getHeight(), 0);
            }
        }
    }

    @Override
    public void onMeasure() {
        if (iconImage == null) {
            super.onMeasure();
        } else {
            float mWidth = getPrefWidth();
            float mHeight = getPrefHeight();
            mWidth = mWidth == WRAP_CONTENT ? getTextWidth() + iconImage.getWidth() + iconSpacing : mWidth;
            mHeight = mHeight == WRAP_CONTENT ? Math.max(getTextHeight(), iconImage.getHeight()) : mHeight;
            mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
            mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
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

    public ActionListener getActionListener() {
        return actionListener;
    }

    public void setActionListener(ActionListener actionListener) {
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
}
