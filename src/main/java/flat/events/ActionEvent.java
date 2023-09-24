package flat.events;

import flat.widget.Widget;

public class ActionEvent extends Event {

    public static final int ACTION = 1;

    public ActionEvent(Widget source) {
        super(source, ACTION);
    }

    @Override
    public String toString() {
        return "ActionEvent [ACTION]";
    }
}
