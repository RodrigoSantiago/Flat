package flat.events;

import flat.graphics.SmartContext;
import flat.widget.Widget;

public class DrawEvent extends Event {
    public static final Type DRAW = new Type("DRAW");

    private final SmartContext smartContext;

    public DrawEvent(Widget source, SmartContext smartContext) {
        super(source, DRAW);
        this.smartContext = smartContext;
    }

    public SmartContext getSmartContext() {
        return smartContext;
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
