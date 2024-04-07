package flat.events;

import flat.widget.Widget;

public class FocusEvent extends Event {
    public static final EventType OWNED = new EventType();
    public static final EventType LOST = new EventType();

    private Widget target;

    public FocusEvent(Widget source, Widget target) {
        super(source, source == target ? OWNED : LOST);
        this.target = target;
    }

    public Widget getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "FocusEvent " + (getType() == OWNED ? "[OWNED]" : "[LOST]") + ", [" + target + "]";
    }
}
