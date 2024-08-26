package flat.widget.text;

import flat.events.ActionEvent;
import flat.uxml.Controller;
import flat.uxml.UXBuilder;
import flat.uxml.UXListener;
import flat.uxml.UXTheme;

public class ToggleButton extends Button {

    private UXListener<ActionEvent> toggleListener;

    @Override
    public void applyAttributes(UXTheme theme, Controller controller, UXBuilder builder) {
        super.applyAttributes(theme, controller, builder);

        /*setActivated(theme.asBool("activated", isActivated()));*/
    }

    @Override
    public void setActivated(boolean actived) {
        if (this.isActivated() != actived) {
            super.setActivated(actived);
            fireToggle(new ActionEvent(this));
        }
    }

    @Override
    public void fireAction(ActionEvent actionEvent) {
        super.fireAction(actionEvent);
        if (!actionEvent.isConsumed()) {
            toggle();
        }
    }

    public UXListener<ActionEvent> getToggleListener() {
        return toggleListener;
    }

    public void setToggleListener(UXListener<ActionEvent> toggleListener) {
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

}
