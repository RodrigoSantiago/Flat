package flat.events;

import flat.widget.Widget;

public class HoverEvent extends Event {
    public static final Type MOVED = new Type("MOVED");
    public static final Type ENTERED = new Type("ENTERED");
    public static final Type EXITED = new Type("EXITED");

    private final float x, y;

    public HoverEvent(Widget source, Type type, float x, float y) {
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
        return "(" + getSource() + ") HoverEvent " + getType() + ", [" + x + ", " + y + "]";
    }

    public static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}
