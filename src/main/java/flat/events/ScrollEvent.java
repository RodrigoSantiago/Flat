package flat.events;

import flat.widget.Widget;

public class ScrollEvent extends Event {
    public static final Type SCROLL = new Type("SCROLL");

    private final float deltaX, deltaY, x, y;

    public ScrollEvent(Widget source, Type type, float deltaX, float deltaY, float x, float y) {
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

    @Override
    public String toString() {
        return "(" + getSource() + ") ScrollEvent " + getType();
    }

    public static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}
