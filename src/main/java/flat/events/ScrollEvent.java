package flat.events;

import flat.widget.Widget;

public class ScrollEvent extends Event {
    public static final EventType SCROLL = new EventType();

    private float deltaX, deltaY, x, y;

    public ScrollEvent(Widget source, EventType type, float deltaX, float deltaY, float x, float y) {
        super(source, type);
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.x = x;
        this.y = y;
    }

    public float getDeltaX() {
        return deltaX;
    }

    public float getDeltaY() {
        return deltaY;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
