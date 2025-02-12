package flat.events;

import flat.widget.Widget;

public class KeyEvent extends Event {
    public static final EventType PRESSED = new EventType();
    public static final EventType REPEATED = new EventType();
    public static final EventType RELEASED = new EventType();
    public static final EventType TYPED = new EventType();
    public static final EventType FILTER = new EventType();

    private boolean shift, ctrl, alt, spr;
    private String chr;
    private int keycode;

    public KeyEvent(Widget source, EventType type, boolean shift, boolean ctrl, boolean alt, boolean spr, String chr, int keycode) {
        super(source, type);
        this.shift = shift;
        this.ctrl = ctrl;
        this.alt = alt;
        this.spr = spr;
        this.chr = chr;
        this.keycode = keycode;
    }

    public boolean isShiftDown() {
        return shift;
    }

    public boolean isCtrlDown() {
        return ctrl;
    }

    public boolean isAltDown() {
        return alt;
    }

    public boolean isSuperDown() {
        return spr;
    }

    public String getChar() {
        return chr;
    }

    public int getKeycode() {
        return keycode;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("KeyEvent ");
        if (getType() == PRESSED) s.append("[PRESSED]");
        else if (getType() == REPEATED) s.append("[REPEATED]");
        else if (getType() == RELEASED) s.append("[RELEASED]");
        else if (getType() == TYPED) s.append("[TYPED]");
        else if (getType() == FILTER) s.append("[FILTER]");
        s.append(" [").append(keycode).append("], [CHAR:").append("\n".equals(chr) ? "\n" : chr).append("]");

        if (shift) s.append(", [").append("SHIFT").append("]");
        if (ctrl) s.append(", [").append("CTRL").append("]");
        if (alt) s.append(", [").append("ALT").append("]");
        if (spr) s.append(", [").append("META").append("]");

        return s.toString();
    }
}
