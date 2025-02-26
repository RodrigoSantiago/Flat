package flat.events;

import flat.widget.Widget;

public class ActionEvent extends Event {

    public static final Type ACTION = new Type("ACTION");

    public ActionEvent(Widget source) {
        super(source, ACTION);
    }

    @Override
    public String toString() {
        return "(" + getSource() + ") ActionEvent [ACTION]";
    }

    public static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}
