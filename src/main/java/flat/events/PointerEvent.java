package flat.events;

import flat.widget.Widget;

public class PointerEvent extends Event {
    public static final Type PRESSED = new Type("PRESSED");
    public static final Type RELEASED = new Type("RELEASED");
    public static final Type DRAGGED = new Type("DRAGGED");
    public static final Type FILTER = new Type("FILTER");

    private final float x, y;
    private final int buttonID;

    public PointerEvent(Widget source, Type type, int buttonID, float x, float y) {
        super(source, type);
        this.buttonID = buttonID;
        this.x = x;
        this.y = y;
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
        return "(" + getSource() + ") PointerEvent " + getType() + ", [" + x + ", " + y + "] [BTN: " + buttonID + "]";
    }

    public static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}
