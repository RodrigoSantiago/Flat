package flat.events;

import flat.widget.Widget;

public class HoverEvent extends Event {
    public static final EventType MOVED = new EventType();
    public static final EventType ENTERED = new EventType();
    public static final EventType EXITED = new EventType();

    private float x, y;

    public HoverEvent(Widget source, EventType type, float x, float y) {
        super(source, type);
        this.x = x;
        this.y = y;
    }

    public boolean isRecyclable(Widget source) {
        return true;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("(").append(getSource()).append(") HoverEvent ");
        if (getType() == MOVED) s.append("[MOVED]");
        else if (getType() == ENTERED) s.append("[ENTERED]");
        else if (getType() == EXITED) s.append("[EXITED]");
        return s.toString();
    }
}
