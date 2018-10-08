package flat.widget.selection;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.ActionListener;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.resources.Resource;
import flat.uxml.Controller;
import flat.uxml.UXStyle;
import flat.uxml.UXStyleAttrs;
import flat.widget.Widget;

public class Switch extends Widget {

    // Properties
    private ActionListener toggleListener;

    private int color;
    private Drawable icon;
    private Drawable delayIcon;

    private boolean delayed;

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        setActivated(style.asBool("activated", isActivated()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        StateInfo info = getStateInfo();

        setColor(getStyle().asColor("color", info, getColor()));

        Resource res = getStyle().asResource("icon", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setIcon(drawable);
            }
        }
        res = getStyle().asResource("delay-icon", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setDelayIcon(drawable);
            }
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        StateInfo info = getStateInfo();
        final float ac = info.get(UXStyle.ACTIVATED);

        final float x1 = x + width * ac;
        final float y1 = y + height * ac;

        context.setTransform2D(getTransform());
        context.setColor(getBackgroundColor());
        context.drawRoundRect(x, y, width, height,
                getRadiusTop(), getRadiusRight(), getRadiusBottom(), getRadiusLeft(), true);

        Drawable ic = delayed && delayIcon != null ? delayIcon : icon;
        if (ic != null) {

            if (isShadowEnabled()) {
                context.setTransform2D(getTransform().preTranslate(0, Math.max(0, getElevation())));
                context.setColor(0x000000FF);
                context.drawRoundRectShadow(x1 - ic.getWidth() / 2f, y1 - ic.getHeight() / 2f, ic.getWidth(), ic.getHeight(),
                        getRadiusTop(), getRadiusRight(), getRadiusBottom(), getRadiusLeft(), getElevation() * 2, 0.28f);
            }

            context.setTransform2D(getTransform());
            context.setColor(color);
            ic.draw(context, x1 - ic.getWidth() / 2f, y1 - ic.getHeight() / 2f, ic.getWidth(), ic.getHeight(), ac);
        }

        if (isRippleEnabled() && getRipple().isVisible()) {
            context.setTransform2D(getTransform().translate(x1, y1));
            getRipple().drawRipple(context, null, getRippleColor());
        }
        context.setTransform2D(null);
    }

    @Override
    public void fireRipple(float x, float y) {
        if (isRippleEnabled()) {
            getRipple().setSize(getInHeight());
            getRipple().fire(0, 0);
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        if (pointerEvent.getType() == PointerEvent.RELEASED) {
            toggle();
        }
        super.firePointer(pointerEvent);
    }

    public ActionListener getToggleListener() {
        return toggleListener;
    }

    public void setToggleListener(ActionListener toggleListener) {
        this.toggleListener = toggleListener;
    }

    public void toggle() {
        setActivated(!isActivated());
    }

    public void fireToggle(ActionEvent event) {
        if (toggleListener != null) {
            toggleListener.handle(event);
        }
    }

    public void setActivated(boolean activated) {
        if (this.isActivated() != activated) {
            super.setActivated(activated);
            fireToggle(new ActionEvent(this, ActionEvent.ACTION));
        }
    }

    public boolean isDelayed() {
        return delayed;
    }

    public void setDelayed(boolean delayed) {
        if (this.delayed != delayed) {
            this.delayed = delayed;
            invalidate(false);
        }
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        if (this.icon != icon) {
            this.icon = icon;
            invalidate(false);
        }
    }

    public Drawable getDelayIcon() {
        return delayIcon;
    }

    public void setDelayIcon(Drawable delayIcon) {
        if (this.delayIcon != delayIcon) {
            this.delayIcon = delayIcon;
            invalidate(false);
        }
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        if (this.color != color) {
            this.color = color;
            invalidate(false);
        }
    }
}
