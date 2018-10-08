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

public class Checkbox extends Widget {

    // Properties
    private ActionListener toggleListener;
    private CheckboxGroup group;
    private boolean undefined;

    private Drawable icon;
    private Drawable undefinedIcon;
    private int color;

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        setActivated(getStyle().asBool("activated", isActivated()));

        style.link("group", (gadget) -> setGroup((CheckboxGroup) gadget));
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
        res = getStyle().asResource("undefined-icon", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setUndefinedIcon(drawable);
            }
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        context.setTransform2D(getTransform());
        context.setColor(color);

        StateInfo info = getStateInfo();
        Drawable ic = undefined && undefinedIcon != null ? undefinedIcon : icon;
        ic.draw(context, x, y, width, height, info.get(UXStyle.ACTIVATED));

        if (isRippleEnabled() && getRipple().isVisible()) {
            context.setTransform2D(getTransform());
            getRipple().drawRipple(context, null, getRippleColor());
            context.setTransform2D(null);
        }
    }

    @Override
    public void fireRipple(float x, float y) {
        if (isRippleEnabled()) {
            getRipple().fire(getInX() + getInWidth() / 2f, getInY() + getInHeight() / 2f);
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

    public CheckboxGroup getGroup() {
        return group;
    }

    public void setGroup(CheckboxGroup group) {
        if (this.group != group) {
            CheckboxGroup oldGroup = this.group;

            this.group = group;

            if (oldGroup != null) {
                oldGroup.checkboxRemove(this);
            }

            if (group != null) {
                group.checkboxAdd(this);
            }
        }
    }


    void _setActivated(boolean actived) {
        if (actived != isActivated()) {
            super.setActivated(actived);

            fireToggle(new ActionEvent(this, ActionEvent.ACTION));
        }
    }

    public void setActivated(boolean actived) {
        if (this.isActivated() != actived) {
            if (group == null) {
                _setActivated(actived);
            } else {
                group.checkboxSetActive(this, actived);
            }
        }
    }

    public void setUndefined(boolean undefined) {
        this.undefined = undefined;
    }

    public boolean isUndefined() {
        return undefined;
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

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        if (this.icon != icon) {
            this.icon = icon;
            invalidate(false);
        }
    }

    public void setUndefinedIcon(Drawable undefinedIcon) {
        if (this.undefinedIcon != undefinedIcon) {
            this.undefinedIcon = undefinedIcon;
            invalidate(false);
        }
    }

    public Drawable getUndefinedIcon() {
        return undefinedIcon;
    }
}
