package flat.events;

import flat.widget.Widget;

public abstract class Event {

    private final Widget source;
    private final EventType type;
    private boolean consumed;
    private boolean focusConsumed;

    public Event(Widget source, EventType type) {
        this.source = source;
        this.type = type;
    }

    public <T extends Widget> T getSource() {
        return (T) source;
    }

    public EventType getType() {
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
