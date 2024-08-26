package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.graphics.text.Align;
import flat.math.Vector2;
import flat.resources.Resource;
import flat.uxml.Controller;
import flat.uxml.UXBuilder;
import flat.uxml.UXListener;
import flat.uxml.UXTheme;

import java.lang.reflect.Method;

public class Chip extends Label {

    private UXListener<ActionEvent> actionListener;

    private Drawable iconImage;
    private float iconSpacing;

    private Drawable actionImage;
    private float actionSpacing;

    @Override
    public void applyAttributes(UXTheme theme, Controller controller, UXBuilder builder) {
        super.applyAttributes(theme, controller, builder);

        /*Method handle = theme.linkListener("on-action", ActionEvent.class, controller);
        if (handle != null) {
            setActionListener(new ActionListener.AutoActionListener(controller, handle));
        }*/
    }

    public void applyStyle() {
        super.applyStyle();
       /* if (getAttrs() == null) return;

        StateInfo info = getStateInfo();

        Resource res = getAttrs().asResource("icon-image", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setIconImage(drawable);
            }
        }

        setIconSpacing(getAttrs().asSize("icon-spacing", info, getIconSpacing()));

        res = getAttrs().asResource("action-image", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setActionImage(drawable);
            }
        }
        setActionSpacing(getAttrs().asSize("action-spacing", info, getIconSpacing()));*/
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(getBackgroundColor(), getBorderColor(), getRippleColor(), context);


        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        context.setColor(getTextColor());
        context.setTextFont(getFont());
        context.setTextSize(getTextSize());
        context.setTextVerticalAlign(Align.Vertical.TOP);
        context.setTextHorizontalAlign(Align.Horizontal.LEFT);

        float is = iconSpacing + (iconImage != null ? iconImage.getWidth() : 0);
        float as = actionSpacing + (actionImage != null ? actionImage.getWidth() : 0);

        float tw = Math.min(getTextWidth() + (is + as), width);
        float xoff = xOff(x, x + width, tw);

        if (getShowText() != null && !getShowText().isEmpty()) {
            context.setTransform2D(getTransform());
            context.drawTextSlice(xoff + is,
                    yOff(y, y + height, getTextHeight()),
                    width - (is + as), getShowText());
        }

        if (iconImage != null) {
            context.setTransform2D(getTransform());
            iconImage.draw(context, xoff,
                    yOff(y, y + height, iconImage.getHeight()),
                    iconImage.getWidth(), iconImage.getHeight(), 0);
        }

        if (actionImage != null) {
            context.setTransform2D(getTransform());
            actionImage.draw(context, xoff + is + getTextWidth() + actionSpacing,
                    yOff(y, y + height, actionImage.getHeight()),
                    actionImage.getWidth(), actionImage.getHeight(), 0);
        }
    }

    @Override
    public void onMeasure() {

        float is = iconSpacing + (iconImage != null ? iconImage.getWidth() : 0);
        float as = actionSpacing + (actionImage != null ? actionImage.getWidth() : 0);
        float h = Math.max(iconImage != null ? iconImage.getHeight() : 0, actionImage != null ? actionImage.getHeight() : 0);

        float mWidth = getPrefWidth();
        float mHeight = getPrefHeight();
        mWidth = mWidth == WRAP_CONTENT ? getTextWidth() + is + as : mWidth;
        mHeight = mHeight == WRAP_CONTENT ? Math.max(getTextHeight(), h) : mHeight;
        mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        setMeasure(mWidth, mHeight);
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);
        if (!pointerEvent.isConsumed() && pointerEvent.getType() == PointerEvent.RELEASED) {
            Vector2 point = new Vector2(pointerEvent.getX(), pointerEvent.getY());
            screenToLocal(point);

            if (point.x >= getInX() + getTextWidth() + (iconImage != null ? iconImage.getWidth() + iconSpacing : 0)) {
                fire();
            }
        }
    }

    @Override
    public void setActivated(boolean actived) {
        super.setActivated(actived);
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

    public Drawable getActionImage() {
        return actionImage;
    }

    public void setActionImage(Drawable actionImage) {
        if (this.actionImage != actionImage) {
            this.actionImage = actionImage;
            invalidate(true);
        }
    }

    public float getActionSpacing() {
        return actionSpacing;
    }

    public void setActionSpacing(float actionSpacing) {
        if (this.actionSpacing != actionSpacing) {
            this.actionSpacing = actionSpacing;
            invalidate(true);
        }
    }
}
