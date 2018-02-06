package flat.widget.text;

import flat.events.ActionEvent;
import flat.events.ActionListener;

public class Button extends Label {
    ActionListener actionListener;

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
}
