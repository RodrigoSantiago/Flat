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

public class RadioButton extends Widget {

    // Properties
    private ActionListener toggleListener;
    private RadioGroup radioGroup;

    private Drawable icon;
    private int color;

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        setActivated(getStyle().asBool("activated", isActivated()));

        style.link("group", (gadget) -> setRadioGroup((RadioGroup) gadget));
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
    }

    @Override
    public void onDraw(SmartContext context) {
        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        context.setTransform2D(getTransform());
        context.setColor(color);

        if (icon != null) {
            StateInfo info = getStateInfo();
            icon.draw(context, x, y, width, height, info.get(UXStyle.ACTIVATED));
        }

        if (isRippleEnabled() && getRipple().isVisible()) {
            context.setTransform2D(getTransform());
            getRipple().drawRipple(context, null, getRippleColor());
            context.setTransform2D(null);
        }
    }

    public ActionListener getToggleListener() {
        return toggleListener;
    }

    public void setToggleListener(ActionListener toggleListener) {
        this.toggleListener = toggleListener;
    }

    public void fireToggle(ActionEvent event) {
        if (toggleListener != null) {
            toggleListener.handle(event);
        }
    }

    public void toggle() {
        setActivated(!isActivated());
    }

    public RadioGroup getRadioGroup() {
        return radioGroup;
    }

    public void setRadioGroup(RadioGroup radioGroup) {
        if (this.radioGroup != radioGroup) {
            RadioGroup oldGroup = this.radioGroup;

            this.radioGroup = radioGroup;

            if (oldGroup != null) {
                oldGroup.radioRemove(this);
            }

            if (radioGroup != null) {
                radioGroup.radioAdd(this);
            }
        }
    }

    @Override
    public void setActivated(boolean actived) {
        if (this.isActivated() != actived) {
            if (radioGroup == null) {
                super.setActivated(actived);
            } else if (actived) {
                super.setActivated(true);
                radioGroup.radioSelect(this);
            } else if (radioGroup.getSelectionIndex() != radioGroup.radioIndex(this)) {
                super.setActivated(false);
            } else if (radioGroup.isEmptySelectionEnabled()) {
                super.setActivated(false);
                radioGroup.radioSelect(null);
            }
            if (this.isActivated() == actived) {
                fireToggle(new ActionEvent(this, ActionEvent.ACTION));
            }
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

    public int getColor() {
        return color;
    }

    public void setColor(int onColor) {
        if (this.color != onColor) {
            this.color = onColor;
            invalidate(false);
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

}
