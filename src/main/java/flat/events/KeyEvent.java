package flat.events;

import flat.widget.Widget;

public class KeyEvent extends Event {
    public static final Type PRESSED = new Type("PRESSED");
    public static final Type REPEATED = new Type("REPEATED");
    public static final Type RELEASED = new Type("RELEASED");
    public static final Type TYPED = new Type("TYPED");
    public static final Type FILTER = new Type("FILTER");

    private final boolean shift, ctrl, alt, spr;
    private final String chr;
    private final int keycode;

    public KeyEvent(Widget source, Type type, boolean shift, boolean ctrl, boolean alt, boolean spr, String chr, int keycode) {
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
        return "(" + getSource() + ") KeyEvent " + getType() +
                ", [KEY:" + keycode + "], [CHAR:" + ("\n".equals(chr) ? "\\n" : chr) + "]"
                + (shift ? ", [SHIFT]" : "")
                + (ctrl ? ", [CTRL]" : "")
                + (alt ? ", [ALT]" : "")
                + (spr ? ", [META]" : "");
    }

    public static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}
