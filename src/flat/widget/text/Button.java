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
    public enum Type {
        TEXT, OUTLINE, CONTAINER, FLOAT;

        @Override
        public String toString() {
            return this == TEXT ? "text" : this == OUTLINE ? "outline" : this == CONTAINER ? "container" : "float";
        }
    }

    private ActionListener actionListener;

    private Type type;

    private Drawable drawable;
    private float imageMargin;
    private Align.Horizontal imageAlign;

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        Method handle = style.asListener("onAction", ActionEvent.class, controller);
        if (handle != null) {
            setActionListener(new ActionListener.AutoActionListener(controller, handle));
        }
    }

    public void applyStyle() {
        super.applyStyle();
        if (getStyle() == null) return;

        StateInfo info = getStateInfo();

        Resource res = getStyle().asResource("image", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setDrawable(drawable);
            }
        }

        setImageAlign(getStyle().asConstant("image-align", info, getHorizontalAlign()));
        setImageMargin(getStyle().asSize("image-margin", info, getImageMargin()));
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(getBackgroundColor(), getBorderColor(), getRippleColor(), context);

        context.setTransform2D(getTransform());

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (drawable == null) {
            if (getShowText() != null && !getShowText().isEmpty()) {
                context.setColor(getTextColor());
                context.setTextFont(getFont());
                context.setTextSize(getTextSize());
                context.setTextVerticalAlign(Align.Vertical.TOP);
                context.setTextHorizontalAlign(Align.Horizontal.LEFT);
                context.drawTextSlice(
                        xOff(x, x + width, Math.min(getTextWidth(), width)),
                        yOff(y, y + height, Math.min(getTextSize(), height)),
                        width, getShowText());
            }
        } else {
            if (getShowText() != null && !getShowText().isEmpty()) {
                context.setColor(getTextColor());
                context.setTextFont(getFont());
                context.setTextSize(getTextSize());
                context.setTextVerticalAlign(Align.Vertical.TOP);
                context.setTextHorizontalAlign(Align.Horizontal.LEFT);

                float xoff = xOff(x, x + width, Math.min(getTextWidth() + imageMargin + drawable.getWidth(), width));

                if (imageAlign == Align.Horizontal.RIGHT) {
                    context.drawTextSlice(xoff,
                            yOff(y, y + height, Math.min(getTextSize(), height)),
                            width - imageMargin - drawable.getWidth(), getShowText());
                    xoff += width - drawable.getWidth();
                    drawable.draw(context, xoff,
                            yOff(y, y + height, drawable.getHeight()),
                            drawable.getWidth(), drawable.getHeight(), 0);
                } else {
                    drawable.draw(context, xoff,
                            yOff(y, y + height, drawable.getHeight()),
                            drawable.getWidth(), drawable.getHeight(), 0);
                    xoff += drawable.getWidth() + imageMargin;
                    context.drawTextSlice(xoff,
                            yOff(y, y + height, Math.min(getTextSize(), height)),
                            width - imageMargin - drawable.getWidth(), getShowText());
                }
            } else {
                drawable.draw(context,
                        xOff(x, x + width, drawable.getWidth()),
                        yOff(y, y + height, drawable.getHeight()), drawable.getWidth(), drawable.getHeight(), 0);
            }
        }
    }

    @Override
    public void onMeasure() {
        if (drawable == null) {
            super.onMeasure();
        } else {
            float mWidth = getPrefWidth();
            float mHeight = getPrefHeight();
            mWidth = mWidth == WRAP_CONTENT ? getTextWidth() + drawable.getWidth() + imageMargin : mWidth;
            mHeight = mHeight == WRAP_CONTENT ? Math.max(getTextSize(), drawable.getHeight()) : mHeight;
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
        fireAction(new ActionEvent(this, ActionEvent.ACTION));
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        if (type == null) {
            type = Type.TEXT;
        }

        if (type != this.type) {
            this.type = type;
            setStates(getStateBitset());
            invalidate(false);
        }
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public float getImageMargin() {
        return imageMargin;
    }

    public void setImageMargin(float imageMargin) {
        this.imageMargin = imageMargin;
    }

    public Align.Horizontal getImageAlign() {
        return imageAlign;
    }

    public void setImageAlign(Align.Horizontal imageAlign) {
        this.imageAlign = imageAlign;
    }
}
