package flat.widget.window;

import flat.backend.WLEnums;
import flat.events.DragEvent;
import flat.events.PointerEvent;
import flat.widget.Widget;
import flat.widget.Window;

import java.util.ArrayList;

public class MouseButtonData extends EventData {
    private static final ArrayList<MouseButtonData> list = new ArrayList<>();

    static MouseButtonData get(int button, int action, int mods) {
        MouseButtonData data = list.size() > 0 ? list.remove(list.size() - 1) : new MouseButtonData();
        data.set(button, action, mods);
        return data;
    }

    private int button, action, mods;

    private MouseButtonData() {

    }

    private void set(int button, int action, int mods) {
        this.button = button;
        this.action = action;
        this.mods = mods;
    }

    private void release() {
        set(0, 0, 0);
        list.add(this);
    }

    @Override
    void handle(Window window) {
        PointerData pointer = window.getPointer();
        Widget widget = window.getActivity().findByPosition(pointer.getX(), pointer.getY(), false);

        if (action == WLEnums.PRESS) {
            pressed(window, pointer, widget);

        } else if (action == WLEnums.RELEASE) {
            released(window, pointer, widget);

        }

        release();
    }

    private void pressed(Window window, PointerData pointer, Widget widget) {
        if (!pointer.isPressed()) {
            pointer.setPressed(widget, button);
        } else {
            widget = pointer.pressed;
        }

        widget.firePointer(new PointerEvent(widget, PointerEvent.PRESSED, button, pointer.getX(), pointer.getY()));
    }

    private void released(Window window, PointerData pointer, Widget widget) {
        if (!pointer.isPressed()) {
            pointer.setPressed(widget, button);
        }

        float pX = pointer.getX();
        float pY = pointer.getY();

        if (pointer.dragStarted && pointer.dragButton == button) {
            if (!widget.isChildOf(pointer.pressed)) {
                DragEvent dragEvent = new DragEvent(widget, DragEvent.DROPPED, pointer.dragData, pX, pY);
                widget.fireDrag(dragEvent);
            }
            pointer.pressed.fireDrag(new DragEvent(pointer.pressed, DragEvent.DONE, pointer.dragData, pX, pY));
            pointer.setDragReleased();
        }

        pointer.pressed.firePointer(new PointerEvent(pointer.pressed, PointerEvent.RELEASED, button, pX, pY));
        if (pointer.isPressed(button)) {
            pointer.setReleased();
        }
    }
}
