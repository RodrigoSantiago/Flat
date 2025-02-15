package flat.window.event;

import flat.widget.Widget;
import flat.window.Window;

import java.util.ArrayList;

public class EventDataMouseMove extends EventData {
    private static final ArrayList<EventDataMouseMove> list = new ArrayList<>();

    static EventDataMouseMove get(double x, double y) {
        EventDataMouseMove data = list.size() > 0 ? list.remove(list.size() - 1) : new EventDataMouseMove();
        data.set((float) x, (float) y);
        return data;
    }

    private float x, y;

    private EventDataMouseMove() {

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
    public void handle(Window window) {
        try {
            EventDataPointer pointer = window.getPointer();
            pointer.setPosition(x, y);
            Widget widget = window.getActivity().findByPosition(x, y, false);

            move(window, pointer, widget);

            if (pointer.isPressed()) {
                drag(window, pointer, widget);
            }
        } finally {
            release();
        }
    }

    private void move(Window window, EventDataPointer pointer, Widget widget) {
        pointer.setHover(widget);
        pointer.performHover();
        if (widget != null) {
            window.setCursor(widget.getShowCursor());
        } else {
            window.setCursor(null);
        }
    }

    private void drag(Window window, EventDataPointer pointer, Widget widget) {
        if (!pointer.isDragged()) {
            pointer.requestDrag();
        }
        if (pointer.isDragged()) {
            pointer.setDragHover(widget);
        }
        if (pointer.isDragged()) {
            pointer.performDrag();
        }

        pointer.performPointerDrag();
    }
}
