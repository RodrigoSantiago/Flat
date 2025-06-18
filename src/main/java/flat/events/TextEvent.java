package flat.events;

import flat.widget.Widget;

public class TextEvent extends Event {

    public static final Type CHANGE = new Type("CHANGE");
    public static final Type FILTER = new Type("FILTER");
    public static final Type TYPE = new Type("TYPE");

    private final int start;
    private final int end;
    private String text;

    public TextEvent(Widget source, Type type, int start, int end, String text) {
        super(source, type);
        this.start = start;
        this.end = end;
        this.text = text;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "(" + getSource() + ") TextEvent " + getType();
    }

    public static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}

