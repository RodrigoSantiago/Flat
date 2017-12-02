package flat.events;

import flat.widget.Widget;

public class KeyEvent extends Event {
    public static final int PRESSED = 1;
    public static final int REPEATED = 2;
    public static final int RELEASED = 3;
    public static final int TYPED = 4;

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
        return (getType() == PRESSED ? "PRESSED" : getType() == RELEASED ? "RELEASED" : "TYPED") +
                "[Alt:" + alt +
                ", Ctrl:" + ctrl +
                ", Meta:" + meta +
                ", Shift:" + shift +
                ", Char:" + chr +
                ", KeyCode:" + keycode + "]";
    }
}
