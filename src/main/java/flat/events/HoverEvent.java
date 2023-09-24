package flat.events;

import flat.widget.Widget;

public class HoverEvent extends Event {
    public static final int MOVED       = 17;
    public static final int ENTERED     = 18;
    public static final int EXITED      = 19;

    private Widget widget;
    private float x, y;

    public HoverEvent(Widget source, int type, Widget widget, float x, float y) {
        super(source, type);
        this.widget = widget;
        this.x = x;
        this.y = y;
    }

    public boolean isRecyclable(Widget source) {
        return getType() == MOVED || source != widget;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("(").append(getSource()).append(") HoverEvent ");
        if (getType() == MOVED) s.append("[MOVED]");
        else if (getType() == ENTERED) s.append("[ENTERED]");
        else if (getType() == EXITED) s.append("[EXITED]");
        return s.toString();
    }
}
