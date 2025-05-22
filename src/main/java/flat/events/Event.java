package flat.events;

import flat.widget.Widget;

public abstract class Event {

    private final Widget source;
    private final EventType type;
    private boolean consumed;
    private boolean focusConsumed;
    private Widget current;

    public Event(Widget source, EventType type) {
        this.source = source;
        this.type = type;
        this.current = source;
    }

    public <T extends Widget> T getSource() {
        return (T) source;
    }

    public <T extends Widget> T getCurrent() {
        return (T) current;
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

    public boolean recycle(Widget current) {
        this.current = current;
        return true;
    }

}
