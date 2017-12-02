package flat.events;

import flat.widget.Widget;

public class DragEvent extends Event {
    public static final int STARTED = 1;
    public static final int DONE    = 2;
    public static final int DROPPED = 3;
    public static final int ENTERED = 4;
    public static final int EXITED  = 5;
    public static final int OVER    = 6;

    private Object data;
    private double screenX, screenY, x, y;
    private boolean dragAccpet;
    private int dragSucess;
    private boolean started;

    public DragEvent(Widget source, int type, Object data, double screenX, double screenY) {
        super(source, type);
        this.data = data;
        this.screenX = screenX;
        this.screenY = screenY;
    }

    @Override
    public DragEvent recycle(Widget source) {
        super.recycle(source);
        return this;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public double getScreenX() {
        return screenX;
    }

    public double getScreenY() {
        return screenY;
    }

    public double getX() {
        return x;
    }

    public double getY() {
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

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (getType() == STARTED) s.append("STARTED");
        if (getType() == DONE) s.append("DONE");
        if (getType() == DROPPED) s.append("DROPPED");
        if (getType() == ENTERED) s.append("ENTERED");
        if (getType() == EXITED) s.append("EXITED");
        if (getType() == OVER) s.append("OVER");
        s.append("[").append(x).append(", ").append(y).append("]");
        return s.toString();
    }


    public boolean getDragSucess() {
        return dragSucess == 1;
    }

    public boolean isStarted() {
        return started;
    }
}
