package flat.widget.selection;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.ActionListener;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.math.Mathf;
import flat.resources.Resource;
import flat.uxml.Controller;
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
        if (getStyle() == null) return;

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
            icon.draw(context, x, y, width, height, info.get(StateInfo.ACTIVATED));
        }

        if (isRippleEnabled() && getRipple().isVisible()) {
            context.setTransform2D(getTransform());
            getRipple().drawRipple(context, null, getRippleColor());
            context.setTransform2D(null);
        }
    }

    @Override
    public void fireRipple(float x, float y) {
        if (isRippleEnabled()) {
            getRipple().setSize(Mathf.sqrt(getInWidth() * getInWidth() + getInHeight() * getInHeight()) * 0.75f);
            getRipple().fire(getInX() + getInWidth() / 2f, getInY() + getInHeight() / 2f);
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);
        if (!pointerEvent.isConsumed() && pointerEvent.getType() == PointerEvent.RELEASED) {
            toggle();
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
                oldGroup.remove(this);
            }

            if (radioGroup != null) {
                radioGroup.add(this);
            }
        }
    }

    void _setActivated(boolean activated) {
        if (activated != isActivated()) {
            super.setActivated(activated);

            fireToggle(new ActionEvent(this));
        }
    }

    public void setActivated(boolean actived) {
        if (this.isActivated() != actived) {
            if (radioGroup == null) {
                _setActivated(actived);
            } else {
                radioGroup.radioSetActivated(this, actived);
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

}
