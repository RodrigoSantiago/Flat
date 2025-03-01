package flat.widget.text;

import flat.events.ActionEvent;
import flat.uxml.*;

public class ToggleButton extends Button {

    private UXListener<ActionEvent> toggleListener;
    private UXValueListener<Boolean> activeListener;
    private boolean active;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();

        setActive(attrs.getAttributeBool("active", isActive()));
        setToggleListener(attrs.getAttributeListener("on-toggle", ActionEvent.class, controller));
        setActiveListener(attrs.getAttributeValueListener("on-active-change", Boolean.class, controller));
    }

    @Override
    public void action() {
        super.action();
        toggle();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (this.active != active) {
            boolean old = this.active;
            this.active = active;

            setActivated(active);
            fireActiveListener(old);
        }
    }

    public UXListener<ActionEvent> getToggleListener() {
        return toggleListener;
    }

    public void setToggleListener(UXListener<ActionEvent> toggleListener) {
        this.toggleListener = toggleListener;
    }

    private void fireToggle() {
        if (toggleListener != null) {
            UXListener.safeHandle(toggleListener, new ActionEvent(this));
        }
    }

    public void toggle() {
        setActive(!isActive());
        fireToggle();
    }

    public UXValueListener<Boolean> getActiveListener() {
        return activeListener;
    }

    public void setActiveListener(UXValueListener<Boolean> activeListener) {
        this.activeListener = activeListener;
    }

    private void fireActiveListener(boolean oldValue) {
        if (activeListener != null && oldValue != active) {
            UXValueListener.safeHandle(activeListener, new ValueChange<>(this, oldValue, active));
        }
    }
}
