package flat.events;

import flat.widget.Widget;

public class DragEvent extends Event {
    public static final int STARTED = 2;
    public static final int DONE    = 3;
    public static final int DROPPED = 4;
    public static final int ENTERED = 5;
    public static final int EXITED  = 6;
    public static final int OVER    = 7;

    private Widget widget;
    private Object data;
    private float x, y;
    private boolean dragAccpet;
    private int dragSucess;
    private boolean started;

    public DragEvent(Widget source, int type, Object data, float x, float y) {
        this(source, type, source, data, x, y);
    }

    public DragEvent(Widget source, int type, Widget widget, Object data, float x, float y) {
        super(source, type);
        this.widget = widget;
        this.data = data;
        this.x = x;
        this.y = y;
    }

    @Override
    public DragEvent recycle(Widget source) {
        super.recycle(source);
        return this;
    }

    public boolean isRecyclable(Widget source) {
        return (getType() != EXITED && getType() != ENTERED) || source != widget;
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

    public void dragStart() {
        started = true;
    }

    public void dragAccept(boolean accpet) {
        dragAccpet = accpet;
    }

    public boolean isDragAccpeted() {
        return dragAccpet;
    }

    public void dragComplete(boolean sucess) {
        dragSucess = sucess ? 1 : 2;
    }

    public boolean isDragCompleted() {
        return dragSucess != 0;
    }

    public boolean getDragSucess() {
        return dragSucess == 1;
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("DragEvent ");
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
