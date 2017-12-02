package flat.events;

import flat.widget.Widget;

public class FocusEvent extends Event {
    public static final int FOCUS_OWNED     = 1;
    public static final int FOCUS_LOST      = 2;

    public FocusEvent(Widget source, int type) {
        super(source, type);
    }

    @Override
    public FocusEvent recycle(Widget source) {
        super.recycle(source);
        return this;
    }

    @Override
    public String toString() {
        return getType() == FOCUS_OWNED ? "FOCUS_OWNED" : "FOCUS_LOST";
    }
}
