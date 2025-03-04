package flat.events;

import flat.widget.Widget;

public class DragEvent extends Event {
    public static final Type STARTED = new Type("STARTED");
    public static final Type DONE    = new Type("DONE");
    public static final Type DROPPED = new Type("DROPPED");
    public static final Type ENTERED = new Type("ENTERED");
    public static final Type EXITED  = new Type("EXITED");
    public static final Type OVER    = new Type("OVER");

    private Object data;
    private final float x, y;
    private Widget dragHandler;
    private Widget dragAccepted;
    private boolean canceled;

    public DragEvent(Widget source, Type type, Object data, float x, float y) {
        super(source, type);
        this.data = data;
        this.x = x;
        this.y = y;
    }

    public DragEvent(Widget source, Type type, Object data, float x, float y, Widget dragHandler, Widget dragAccepted) {
        super(source, type);
        this.data = data;
        this.x = x;
        this.y = y;
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

    @Override
    public String toString() {
        return "(" + getSource() + ") DragEvent " + getType() + ", [" + x + ", " + y + "]";
    }

    private static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}
