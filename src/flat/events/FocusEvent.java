package flat.events;

import flat.widget.Widget;

public class FocusEvent extends Event {
    public static final int OWNED = 8;
    public static final int LOST = 9;

    private Widget target;

    public FocusEvent(Widget source, Widget target) {
        super(source, source == target ? OWNED : LOST);
        this.target = target;
    }

    @Override
    public FocusEvent recycle(Widget source) {
        super.recycle(source);
        return this;
    }

    public Widget getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "FocusEvent " + (getType() == OWNED ? "[OWNED]" : "[LOST]") + ", [" + target + "]";
    }
}
