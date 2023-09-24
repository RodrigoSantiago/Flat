package flat.events;

import flat.widget.Widget;

public class ScrollEvent extends Event {
    public static final int SCROLL = 20;

    private float deltaX, deltaY;

    public ScrollEvent(Widget source, int type, float deltaX, float deltaY) {
        super(source, type);
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    public float getDeltaX() {
        return deltaX;
    }

    public float getDeltaY() {
        return deltaY;
    }
}
