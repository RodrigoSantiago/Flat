package flat.events;

import flat.widget.Widget;
import flat.window.Window;

public class PointerEvent extends Event {
    public static final Type PRESSED = new Type("PRESSED");
    public static final Type RELEASED = new Type("RELEASED");
    public static final Type DRAGGED = new Type("DRAGGED");
    public static final Type FILTER = new Type("FILTER");

    private final Window window;
    private final float x, y;
    private final int buttonID;
    private final int clickCount;

    public PointerEvent(Widget source, Window window, Type type, int buttonID, float x, float y, int clickCount) {
        super(source, type);
        this.window = window;
        this.buttonID = buttonID;
        this.x = x;
        this.y = y;
        this.clickCount = clickCount;
    }

    public int getPointerID() {
        return buttonID;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getClickCount() {
        return clickCount;
    }

    public Window getWindow() {
        return window;
    }

    public boolean isShiftDown() {
        return window.isShiftDown();
    }

    public boolean isCtrlDown() {
        return window.isCtrlDown();
    }

    public boolean isAltDown() {
        return window.isAltDown();
    }

    public boolean isSprDown() {
        return window.isSprDown();
    }

    @Override
    public String toString() {
        return "(" + getSource() + ") PointerEvent " + getType() + ", [" + x + ", " + y + "] [BTN: " + buttonID + "]";
    }

    public static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}
