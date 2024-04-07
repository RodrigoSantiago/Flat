package flat.events;

import flat.widget.Widget;

public class DragEvent extends Event {
    public static final EventType STARTED = new EventType();
    public static final EventType DONE    = new EventType();
    public static final EventType DROPPED = new EventType();
    public static final EventType ENTERED = new EventType();
    public static final EventType EXITED  = new EventType();
    public static final EventType OVER    = new EventType();

    private Object data;
    private float x, y;
    private Widget dragHandler;
    private Widget dragAccepted;
    private boolean canceled;

    public DragEvent(Widget source, EventType type, Object data, float x, float y) {
        super(source, type);
        this.data = data;
        this.x = x;
        this.y = y;
    }

    public DragEvent(Widget source, EventType type, Object data, float x, float y, Widget dragHandler, Widget dragAccepted) {
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
        StringBuilder s = new StringBuilder("(").append(getSource()).append(") DragEvent ");
        if (getType() == STARTED) s.append("[STARTED]");
        else if (getType() == DONE) s.append("[DONE]");
        else if (getType() == DROPPED) s.append("[DROPPED]");
        else if (getType() == ENTERED) s.append("[ENTERED]");
        else if (getType() == EXITED) s.append("[EXITED]");
        else if (getType() == OVER) s.append("[OVER]");
        s.append(", [").append(x).append(", ").append(y).append("]");
        return s.toString();
    }
}
