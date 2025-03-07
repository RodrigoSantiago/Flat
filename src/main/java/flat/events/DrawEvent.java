package flat.events;

import flat.graphics.Graphics;
import flat.widget.Widget;

public class DrawEvent extends Event {
    public static final Type DRAW = new Type("DRAW");

    private final Graphics graphics;

    public DrawEvent(Widget source, Graphics graphics) {
        super(source, DRAW);
        this.graphics = graphics;
    }

    public Graphics getGraphics() {
        return graphics;
    }

    @Override
    public String toString() {
        return "(" + getSource() + ") DrawEvent [DRAW]";
    }

    public static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}
