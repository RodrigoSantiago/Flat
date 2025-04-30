package flat.events;

import flat.widget.Widget;
import flat.window.Window;

public class DragEvent extends Event {
    public static final Type STARTED = new Type("STARTED");
    public static final Type DONE    = new Type("DONE");
    public static final Type DROPPED = new Type("DROPPED");
    public static final Type ENTERED = new Type("ENTERED");
    public static final Type EXITED  = new Type("EXITED");
    public static final Type OVER    = new Type("OVER");
    public static final Type HOVER   = new Type("HOVER");

    private final Window window;
    private Object data;
    private final float x, y, pressX, pressY;
    private Widget dragHandler;
    private Widget dragAccepted;
    private boolean canceled;

    public DragEvent(Widget source, Window window, Type type, Object data, float x, float y, float pressX, float pressY) {
        super(source, type);
        this.window = window;
        this.data = data;
        this.x = x;
        this.y = y;
        this.pressX = pressX;
        this.pressY = pressY;
    }

    public DragEvent(Widget source, Window window, Type type, Object data, float x, float y, float pressX, float pressY,
                     Widget dragHandler, Widget dragAccepted) {
        super(source, type);
        this.window = window;
        this.data = data;
        this.x = x;
        this.y = y;
        this.pressX = pressX;
        this.pressY = pressY;
        this.dragHandler = dragHandler;
        this.dragAccepted = dragAccepted;
    }

    public boolean isRecyclable(Widget source) {
        return !isAccepted() && !isCanceled();
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getPressX() {
        return pressX;
    }

    public float getPressY() {
        return pressY;
    }

    public float getDistance() {
        return (float) Math.sqrt((x - pressX) * (x - pressX) + (y - pressY) * (y - pressY));
    }

    public void accept(Widget source) {
        if (getType() == STARTED) {
            dragHandler = source;
        } else {
            dragAccepted = source;
        }
    }

    public boolean isAccepted() {
        if (getType() == STARTED) {
            return dragHandler != null;
        } else {
            return dragAccepted != null;
        }
    }

    public Widget getDragHandler() {
        return dragHandler;
    }

    public Widget getDragAccepted() {
        return dragAccepted;
    }

    public void cancel() {
        this.canceled = true;
    }

    public boolean isCanceled() {
        return canceled;
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
        return "(" + getSource() + ") DragEvent " + getType() + ", [" + x + ", " + y + "]";
    }

    public static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}
