package flat.widget.window;

import flat.widget.Widget;

public class PointerData {
    public final int pointerId;

    Widget pressed, hover;
    int dragButton;
    int pressedButton;

    float x, y;
    boolean dragStarted;
    Object dragData;

    public PointerData(int pointerId) {
        this.pointerId = pointerId;
    }

    void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isPressed() {
        return pressed != null;
    }

    public boolean isPressed(int button) {
        return pressed != null && pressedButton == button;
    }

    public void setPressed(Widget widget, int button) {
        this.pressed = widget;
        this.pressedButton = button;
    }

    public void setReleased() {
        this.pressed = null;
        this.pressedButton = 0;
    }

    public void setDragged(int dragButton, Object dragData) {
        this.dragStarted = true;
        this.dragButton = dragButton;
        this.dragData = dragData;
    }

    public void updateDragData(Object dragData) {
        this.dragData = dragData;
    }

    public void setDragReleased() {
        this.dragStarted = false;
        this.dragButton = -1;
        this.dragData = null;
    }

    public void setHover(Widget hover) {
        this.hover = hover;
    }

    void reset() {
        hover = null;
        pressed = null;
        dragData = null;
        dragStarted = false;
    }
}
