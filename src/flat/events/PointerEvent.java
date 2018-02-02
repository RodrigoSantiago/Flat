package flat.events;

import flat.widget.Widget;

public class PointerEvent extends Event {
    public static final int PRESSED     = 14;
    public static final int RELEASED    = 15;
    public static final int DRAGGED     = 16;

    private float x, y;
    private final int buttonID;
    private final boolean mouseEvent;

    public PointerEvent(Widget source, int type, boolean mouseEvent, int buttonID, float x, float y) {
        super(source, type);
        this.mouseEvent = mouseEvent;
        this.buttonID = buttonID;
        this.x = x;
        this.y = y;
    }

    public PointerEvent(Widget source, int type, int buttonID, float x, float y) {
        this(source, type, true, buttonID, x, y);
    }

    @Override
    public PointerEvent recycle(Widget source) {
        super.recycle(source);
        return this;
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
        StringBuilder s = new StringBuilder("PointerEvent ");
        if (getType() == PRESSED) s.append("[PRESSED]");
        else if (getType() == RELEASED) s.append("[RELEASED]");
        else if (getType() == DRAGGED) s.append("[DRAGGED]");
        return s.toString();
    }
}
