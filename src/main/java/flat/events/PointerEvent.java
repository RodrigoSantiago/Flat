package flat.events;

import flat.widget.Widget;

public class PointerEvent extends Event {
    public static final EventType PRESSED = new EventType();
    public static final EventType RELEASED = new EventType();
    public static final EventType DRAGGED = new EventType();

    private float x, y;
    private final int buttonID;
    private final boolean mouseEvent;

    public PointerEvent(Widget source, EventType type, boolean mouseEvent, int buttonID, float x, float y) {
        super(source, type);
        this.mouseEvent = mouseEvent;
        this.buttonID = buttonID;
        this.x = x;
        this.y = y;
    }

    public PointerEvent(Widget source, EventType type, int buttonID, float x, float y) {
        this(source, type, true, buttonID, x, y);
    }

    public boolean isMouseEvent() {
        return mouseEvent;
    }

    public int getPointerID() {
        return buttonID;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("(").append(getSource()).append(") PointerEvent ");
        if (getType() == PRESSED) s.append("[PRESSED]");
        else if (getType() == RELEASED) s.append("[RELEASED]");
        else if (getType() == DRAGGED) s.append("[DRAGGED]");
        return s.toString();
    }
}
