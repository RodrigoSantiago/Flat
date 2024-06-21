package flat.widget.text;

import flat.events.ActionEvent;
import flat.events.ActionListener;
import flat.uxml.Controller;
import flat.uxml.UXBuilder;
import flat.uxml.UXTheme;

public class ToggleButton extends Button {

    private ActionListener toggleListener;

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

}
