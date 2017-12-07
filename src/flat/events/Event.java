package flat.events;

import flat.widget.Widget;

public abstract class Event {

    private Widget source;
    private int type;

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
}
