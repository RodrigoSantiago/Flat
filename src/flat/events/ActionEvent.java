package flat.events;

import flat.widget.Widget;

public class ActionEvent extends Event {

    public static final int ACTION = 1;

    public ActionEvent(Widget source) {
        super(source, ACTION);
    }

    @Override
    public ActionEvent recycle(Widget source) {
        super.recycle(source);
        return this;
    }

    @Override
    public String toString() {
        return "ActionEvent [ACTION]";
    }
}
