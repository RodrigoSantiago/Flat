package flat.events;

import flat.widget.Widget;

public abstract class Event {

    private final Widget source;
    private final int type;
    private boolean consumed;
    private boolean focusConsumed;

    public Event(Widget source, int type) {
        this.source = source;
        this.type = type;
    }

    public Widget getSource() {
        return source;
    }

    public int getType() {
        return type;
    }

    public void consume() {
        consumed = true;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consumeFocus(boolean focusConsumed) {
        this.focusConsumed = focusConsumed;
    }

    public boolean isFocusConsumed() {
        return focusConsumed;
    }
}
