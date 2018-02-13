package flat.events;

import flat.widget.Widget;

public abstract class Event {

    private Widget source;
    private int type;
    private boolean consumed;

    public Event(Widget source, int type) {
        this.source = source;
        this.type = type;
    }

    protected Widget getSource() {
        return source;
    }

    public int getType() {
        return type;
    }

    public Event recycle(Widget source) {
        this.source = source;
        return this;
    }

    public void consume() {
        consumed = true;
    }

    public boolean isConsumed() {
        return consumed;
    }
}
