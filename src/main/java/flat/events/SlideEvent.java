package flat.events;

import flat.widget.Widget;

public class SlideEvent extends Event {

    public static final Type SLIDE = new Type("SLIDE");
    public static final Type FILTER = new Type("FILTER");

    private final float value;

    public SlideEvent(Widget source, Type type, float value) {
        super(source, type);
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "(" + getSource() + ")" + "SlideEvent " + getType();
    }

    public static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}
