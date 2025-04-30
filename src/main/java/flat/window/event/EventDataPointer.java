package flat.window.event;

import flat.events.DragEvent;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.widget.Widget;
import flat.window.Activity;
import flat.window.Window;

public class EventDataPointer {
    public final int pointerId;

    Window window;

    Widget pressed, hover;
    Widget lastPressed;
    int pressedButton;

    float pressX, pressY;
    int clickCount = 1;
    float x, y;
    Widget dragHandler;
    Widget dragHover;
    Widget dragAccepted;
    boolean dragStarted;
    boolean dragRequested;
    boolean dragCanceled;
    Object dragData;
    long lastPress;

    public EventDataPointer(Window window, int pointerId) {
        this.window = window;
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

    boolean isPressed(int button) {
        return pressed != null && pressedButton == button;
    }

    boolean isDragged() {
        return dragStarted;
    }

    boolean isDragged(int button) {
        return dragStarted && pressedButton == button;
    }

    boolean isDragDone() {
        return !dragStarted && dragRequested;
    }

    void setPressed(Widget widget, int button) {
        this.pressed = widget;
        this.pressedButton = button;
    }

    boolean performPressedFilter(Activity activity, int button, Widget widget) {
        PointerEvent event = new PointerEvent(widget, window, PointerEvent.FILTER, button, x, y, clickCount);
        activity.onPointerFilter(event);
        return !event.isConsumed();
    }

    void performPressed(int button) {
        if (pressed != null) {
            pressX = x;
            pressY = y;
            long now = System.currentTimeMillis();
            if (now - lastPress > 500 || button != pressedButton || pressed != lastPressed) {
                clickCount = 1;
            } else {
                clickCount++;
            }
            lastPressed = pressed;
            lastPress = now;
            pressed.firePointer(new PointerEvent(pressed, window, PointerEvent.PRESSED, button, x, y, clickCount));
        }
    }

    void setReleased(Window window) {
        performReleased(window, pressed, pressedButton);

        this.pressed = null;
        this.pressedButton = 0;
    }

    void performReleased(Window window, Widget released, int button) {
        if (released != null) {
            PointerEvent event = new PointerEvent(released, window, PointerEvent.RELEASED, button, x, y, clickCount);
            released.firePointer(event);
            if (!event.isFocusConsumed()) {
                window.getActivity().setFocus(null);
            }
        } else {
            window.getActivity().setFocus(null);
        }
    }

    void setDragStarted(Widget dragHandler, Object dragData) {
        this.dragHandler = dragHandler;
        this.dragRequested = true;
        this.dragStarted = true;
        this.dragData = dragData;
        this.dragHover = null;
        this.dragCanceled = false;
    }

    void setDragDropped() {
        if (dragStarted) {
            Widget lastAccepted = null;
            if (dragAccepted != null) {
                DragEvent dragEvent = new DragEvent(dragAccepted, window, DragEvent.DROPPED, dragData, x, y, pressX, pressY, dragHandler, dragAccepted);
                dragAccepted.fireDrag(dragEvent);

                dragData = dragEvent.getData();
                lastAccepted = dragEvent.getDragAccepted();

                setDragHover(null);
            }

            DragEvent dragEvent = new DragEvent(dragHandler, window, DragEvent.DONE, dragData, x, y,  pressX, pressY, dragHandler, lastAccepted);
            dragHandler.fireDrag(dragEvent);
        }

        setDragHover(null);
    }

    void setDragCanceled() {
        this.dragCanceled = true;
        setDragHover(null);

        this.dragStarted = false;
        this.dragData = null;
        this.dragAccepted = null;
        this.dragHandler = null;
        this.dragHover = null;
    }

    void setDragReleased() {
        setDragHover(null);

        this.dragStarted = false;
        this.dragData = null;
        this.dragRequested = false;
        this.dragAccepted = null;
        this.dragHandler = null;
        this.dragHover = null;
        this.dragCanceled = false;
    }

    void setHover(Widget hover) {
        if (this.hover != hover) {
            if (this.hover != null) {
                this.hover.fireHover(new HoverEvent(this.hover, HoverEvent.EXITED, x, y));
            }
            this.hover = hover;
            if (this.hover != null) {
                this.hover.fireHover(new HoverEvent(this.hover, HoverEvent.ENTERED, x, y));
            }
        }
    }

    void performHover() {
        if (hover != null) {
            hover.fireHover(new HoverEvent(hover, HoverEvent.MOVED, x, y));
        }
    }

    void requestDrag() {
        if (!dragStarted && !dragRequested) {
            DragEvent event = new DragEvent(pressed, window, DragEvent.STARTED, null, x, y, pressX, pressY);
            pressed.fireDrag(event);

            if (event.isAccepted()) {
                setDragStarted(event.getDragHandler(), event.getData());
            } else {
                setDragCanceled();
            }
        }
    }

    void setDragHover(Widget widget) {
        Widget dragHover = widget == null || dragHandler == widget || widget.isChildOf(dragHandler) ? null : widget;
        if (dragHover != this.dragHover) {
            if (this.dragHover != null) {
                this.dragHover.fireDrag(new DragEvent(this.dragHover, window, DragEvent.EXITED, dragData
                        , x, y, pressX, pressY, dragHandler, null));
                dragAccepted = null;
            }
            this.dragHover = dragHover;
            if (this.dragHover != null) {
                DragEvent dragEvent = new DragEvent(this.dragHover, window, DragEvent.ENTERED, dragData
                        , x, y, pressX, pressY, dragHandler, null);
                this.dragHover.fireDrag(dragEvent);
                dragData = dragEvent.getData();
                dragAccepted = dragEvent.getDragAccepted();

                if (dragEvent.isCanceled() && !dragCanceled) {
                    requestCancelDrag();
                }
            }
        }
        if (this.dragHover != null) {
            DragEvent dragEvent = new DragEvent(this.dragHover, window, DragEvent.HOVER, dragData
                    , x, y, pressX, pressY, dragHandler, dragAccepted);
            this.dragHover.fireDrag(dragEvent);
            dragData = dragEvent.getData();
            dragAccepted = dragEvent.getDragAccepted();

            if (dragEvent.isCanceled() && !dragCanceled) {
                requestCancelDrag();
            }
        }
    }

    void performDrag() {
        DragEvent dragEvent = new DragEvent(dragHandler, window, DragEvent.OVER, dragData, x, y, pressX, pressY, dragHandler, dragAccepted);
        dragHandler.fireDrag(dragEvent);
        dragData = dragEvent.getData();

        if (dragEvent.isCanceled() && !dragCanceled) {
            requestCancelDrag();
        }
    }

    private void requestCancelDrag() {
        setDragHover(null);
        DragEvent cancelEvent = new DragEvent(dragHandler, window, DragEvent.DONE, dragData, x, y, pressX, pressY, dragHandler, null);
        cancelEvent.cancel();
        dragHandler.fireDrag(cancelEvent);
        setDragCanceled();
    }

    void performPointerDrag() {
        if (pressed != null) {
            pressed.firePointer(new PointerEvent(pressed, window, PointerEvent.DRAGGED, pressedButton, x, y, clickCount));
        }
    }

    public Widget getHover() {
        return hover;
    }

    public Widget getPressed() {
        return pressed;
    }

    public void reset(float x, float y) {
        this.x = x;
        this.y = y;

        float pasPressX = pressX;
        float pasPressY = pressY;
        int passClickCount = clickCount;
        var pastHover = hover;
        var pastPressed = pressed;
        var pastPressedButton = pressedButton;

        var pastDragCanceled = dragCanceled;
        var pastDragStarted = dragStarted;
        var pastDragData = dragData;
        var pastDragRequested = dragRequested;
        var pastDragAccepted = dragAccepted;
        var pastDragHandler = dragHandler;
        var pastDragHover = dragHover;

        pressX = x;
        pressY = y;
        lastPressed = null;
        clickCount = 1;
        lastPress = 0;
        hover = null;
        pressed = null;
        pressedButton = 0;

        dragStarted = false;
        dragData = null;
        dragRequested = false;
        dragAccepted = null;
        dragHandler = null;
        dragHover = null;
        dragCanceled = false;

        if (pastDragStarted && pastDragHover != null) {
            DragEvent dragEvent = new DragEvent(pastPressed, window, DragEvent.EXITED, pastDragData,
                    x, y, pasPressX, pasPressY, pastDragHandler, null);
            pastDragHover.fireDrag(dragEvent);
            pastDragData = dragEvent.getData();
        }
        if (pastDragStarted) {
            pastDragHandler.fireDrag(new DragEvent(pastDragHandler, window, DragEvent.DONE, pastDragData,
                    x, y, pasPressX, pasPressY, pastDragHandler, null));
        }
        if (pastPressed != null) {
            pastPressed.firePointer(new PointerEvent(pastPressed, window, PointerEvent.RELEASED, pastPressedButton, x, y, passClickCount));
        }
        if (pastHover != null) {
            pastHover.fireHover(new HoverEvent(pastHover, HoverEvent.EXITED, x, y));
        }
    }
}
