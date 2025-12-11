package flat.events;

import flat.widget.Widget;

public class FocusEvent extends Event {
    public static final Type OWNED = new Type("OWNED");
    public static final Type LOST = new Type("LOST");
    public static final Type OBSERVER = new Type("OBSERVER");

    private final Widget target;

    public FocusEvent(Widget source, Widget target, FocusEvent.Type type) {
        super(source, type);
        this.target = target;
    }

    public Widget getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "(" + getSource() + ") FocusEvent " + getType() + ", [" + target + "]";
    }

    public static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}
