package flat.window.event;

import flat.backend.WLEnums;
import flat.widget.Widget;
import flat.window.Window;

import java.util.ArrayList;

public class EventDataMouseButton extends EventData {
    private static final ArrayList<EventDataMouseButton> list = new ArrayList<>();

    static EventDataMouseButton get(int button, int action) {
        EventDataMouseButton data = list.size() > 0 ? list.remove(list.size() - 1) : new EventDataMouseButton();
        data.set(button, action);
        return data;
    }

    private int button;
    private int action;

    private EventDataMouseButton() {

    }

    private void set(int button, int action) {
        this.button = button;
        this.action = action;
    }

    private void release() {
        set(0, 0);
        list.add(this);
    }

    @Override
    public void handle(Window window) {
        EventDataPointer pointer = window.getPointer();
        Widget widget = window.getActivity().findByPosition(pointer.getX(), pointer.getY(), false);

        if (action == WLEnums.PRESS) {
            pressed(window, pointer, widget);

        } else if (action == WLEnums.RELEASE) {
            released(window, pointer, widget);

        }

        release();
    }

    private void pressed(Window window, EventDataPointer pointer, Widget widget) {
        if (!pointer.isPressed()) {
            if (pointer.performPressedFilter(window.getActivity(), button, widget)) {
                pointer.setPressed(widget, button);
            }
        }
        pointer.performPressed(button);
    }

    private void released(Window window, EventDataPointer pointer, Widget widget) {
        if (pointer.isDragged(button)) {
            pointer.setDragDropped();
            pointer.setDragReleased();
        }

        if (pointer.isPressed(button)) {
            pointer.setReleased();
        } else {
            pointer.performReleased(widget, button);
        }
    }
}
