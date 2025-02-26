package flat.events;

import flat.widget.Widget;

public class FocusEvent extends Event {
    public static final Type OWNED = new Type("OWNED");
    public static final Type LOST = new Type("LOST");

    private final Widget target;

    public FocusEvent(Widget source, Widget target) {
        super(source, source == target ? OWNED : LOST);
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
