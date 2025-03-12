package flat.window.event;

import flat.events.DragEvent;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.widget.Widget;
import flat.window.Activity;
import flat.window.Application;
import flat.window.Window;

public class EventDataPointer {
    public final int pointerId;

    Widget pressed, hover;
    int pressedButton;

    float x, y;
    Widget dragHandler;
    Widget dragHover;
    Widget dragAccepted;
    boolean dragStarted;
    boolean dragRequested;
    boolean dragCanceled;
    Object dragData;

    public EventDataPointer(int pointerId) {
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
        PointerEvent event = new PointerEvent(widget, PointerEvent.FILTER, button, x, y);
        activity.onPointerFilter(event);
        return !event.isConsumed();
    }

    void performPressed(int button) {
        if (pressed != null) {
            pressed.firePointer(new PointerEvent(pressed, PointerEvent.PRESSED, button, x, y));
        }
    }

    void setReleased(Window window) {
        performReleased(window, pressed, pressedButton);

        this.pressed = null;
        this.pressedButton = 0;
    }

    void performReleased(Window window, Widget released, int button) {
        if (released != null) {
            PointerEvent event = new PointerEvent(released, PointerEvent.RELEASED, button, x, y);
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
                DragEvent dragEvent = new DragEvent(dragAccepted, DragEvent.DROPPED, dragData, x, y, dragHandler, dragAccepted);
                dragAccepted.fireDrag(dragEvent);

                dragData = dragEvent.getData();
                lastAccepted = dragEvent.getDragAccepted();

                setDragHover(null);
            }

            DragEvent dragEvent = new DragEvent(dragHandler, DragEvent.DONE, dragData, x, y, dragHandler, lastAccepted);
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
            DragEvent event = new DragEvent(pressed, DragEvent.STARTED, null, x, y);
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
                this.dragHover.fireDrag(new DragEvent(this.dragHover, DragEvent.EXITED, dragData, x, y, dragHandler, null));
                dragAccepted = null;
            }
            this.dragHover = dragHover;
            if (this.dragHover != null) {
                DragEvent dragEvent = new DragEvent(this.dragHover, DragEvent.ENTERED, dragData, x, y, dragHandler, null);
                this.dragHover.fireDrag(dragEvent);
                dragData = dragEvent.getData();
                dragAccepted = dragEvent.getDragAccepted();

                if (dragEvent.isCanceled() && !dragCanceled) {
                    requestCancelDrag();
                }
            }
        }
    }

    void performDrag() {
        DragEvent dragEvent = new DragEvent(dragHandler, DragEvent.OVER, dragData, x, y, dragHandler, dragAccepted);
        dragHandler.fireDrag(dragEvent);
        dragData = dragEvent.getData();

        if (dragEvent.isCanceled() && !dragCanceled) {
            requestCancelDrag();
        }
    }

    private void requestCancelDrag() {
        setDragHover(null);
        DragEvent cancelEvent = new DragEvent(dragHandler, DragEvent.DONE, dragData, x, y, dragHandler, null);
        cancelEvent.cancel();
        dragHandler.fireDrag(cancelEvent);
        setDragCanceled();
    }

    void performPointerDrag() {
        if (pressed != null) {
            pressed.firePointer(new PointerEvent(pressed, PointerEvent.DRAGGED, pressedButton, x, y));
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
            DragEvent dragEvent = new DragEvent(pastPressed, DragEvent.EXITED, pastDragData, x, y, pastDragHandler, null);
            pastDragHover.fireDrag(dragEvent);
            pastDragData = dragEvent.getData();
        }
        if (pastDragStarted) {
            pastDragHandler.fireDrag(new DragEvent(pastDragHandler, DragEvent.DONE, pastDragData, x, y, pastDragHandler, null));
        }
        if (pastPressed != null) {
            pastPressed.firePointer(new PointerEvent(pastPressed, PointerEvent.RELEASED, pastPressedButton, x, y));
        }
        if (pastHover != null) {
            pastHover.fireHover(new HoverEvent(pastHover, HoverEvent.EXITED, x, y));
        }
    }
}
