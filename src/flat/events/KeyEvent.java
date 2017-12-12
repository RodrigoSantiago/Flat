package flat.events;

import flat.widget.Widget;

public class KeyEvent extends Event {
    public static final int PRESSED = 10;
    public static final int REPEATED = 11;
    public static final int RELEASED = 12;
    public static final int TYPED = 13;

    private boolean shift, ctrl, alt, meta ;
    private String chr;
    private int keycode;

    public KeyEvent(Widget source, int type, boolean shift, boolean ctrl, boolean alt, boolean meta, String chr, int keycode) {
        super(source, type);
        this.shift = shift;
        this.ctrl = ctrl;
        this.alt = alt;
        this.meta = meta;
        this.chr = chr;
        this.keycode = keycode;
    }

    @Override
    public KeyEvent recycle(Widget source) {
        super.recycle(source);
        return this;
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

    public boolean isMetaDown() {
        return meta;
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
        s.append(" [").append(keycode).append("], [CHAR:").append("\n".equals(chr) ? "\n" : chr).append("]");

        if (shift) s.append(", [").append("SHIFT").append("]");
        if (ctrl) s.append(", [").append("CTRL").append("]");
        if (alt) s.append(", [").append("ALT").append("]");
        if (meta) s.append(", [").append("META").append("]");

        return s.toString();
    }
}
