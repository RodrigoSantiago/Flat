package flat.widget.window;

import flat.events.DragEvent;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.widget.Widget;
import flat.widget.Window;

import java.util.ArrayList;

public class MouseMoveData extends EventData {
    private static final ArrayList<MouseMoveData> list = new ArrayList<>();

    static MouseMoveData get(double x, double y) {
        MouseMoveData data = list.size() > 0 ? list.remove(list.size() - 1) : new MouseMoveData();
        data.set((float) x, (float) y);
        return data;
    }

    private float x, y;

    private MouseMoveData() {

    }

    private void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    private void release() {
        set(0, 0);
        list.add(this);
    }

    @Override
    void handle(Window window) {
        PointerData pointer = window.getPointer();
        pointer.setPosition(x, y);
        Widget widget = window.getActivity().findByPosition(x, y, false);

        if (pointer.isPressed()) {
            drag(window, pointer, widget);
        } else {
            move(window, pointer, widget);
        }

        release();
    }

    private void move(Window window, PointerData pointer, Widget widget) {
        if (pointer.hover == null) {
            widget.fireHover(new HoverEvent(widget, HoverEvent.ENTERED, widget, x, y));

        } else if (pointer.hover != widget) {
            if (!widget.isChildOf(pointer.hover)) {
                pointer.hover.fireHover(new HoverEvent(pointer.hover, HoverEvent.EXITED, widget, x, y));
            }
            if (!pointer.hover.isChildOf(widget)) {
                widget.fireHover(new HoverEvent(widget, HoverEvent.ENTERED, pointer.hover, x, y));
            }
        }

        widget.fireHover(new HoverEvent(widget, HoverEvent.MOVED, widget, x, y));
        pointer.setHover(widget);

        window.setCursor(widget.getShowCursor());
    }

    private void drag(Window window, PointerData pointer, Widget widget) {
        if (!pointer.dragStarted) {
            DragEvent event = new DragEvent(pointer.pressed, DragEvent.STARTED, pointer.dragData, x, y);
            pointer.pressed.fireDrag(event);
            if (event.isStarted()) {
                pointer.setDragged(pointer.pressedButton, event.getData());
            }
            pointer.setHover(widget);
        }

        if (pointer.dragStarted) {
            if (pointer.hover != widget) {
                if (pointer.hover != pointer.pressed && !widget.isChildOf(pointer.hover)) {
                    DragEvent event = new DragEvent(pointer.hover, DragEvent.EXITED, widget, pointer.dragData, x, y);
                    pointer.hover.fireDrag(event);
                    pointer.updateDragData(event.getData());
                }
                if (widget != pointer.pressed && !pointer.hover.isChildOf(widget)) {
                    DragEvent event = new DragEvent(widget, DragEvent.ENTERED, pointer.hover, pointer.dragData, x, y);
                    widget.fireDrag(event);
                    pointer.updateDragData(event.getData());
                }
            }

            if (widget != pointer.pressed) {
                DragEvent event = new DragEvent(widget, DragEvent.OVER, pointer.dragData, x, y);
                widget.fireDrag(event);
                pointer.updateDragData(event.getData());
            }
        }

        pointer.pressed.firePointer(new PointerEvent(pointer.pressed, PointerEvent.DRAGGED, pointer.dragButton, x, y));
        pointer.setHover(widget);
    }
}
