package flat.window.event;

import flat.events.DragEvent;
import flat.window.Activity;
import flat.widget.Widget;
import flat.window.Application;
import flat.window.Window;

import java.util.ArrayList;

public class EventDataMouseDrop extends EventData {
    private static final ArrayList<EventDataMouseDrop> list = new ArrayList<>();

    static EventDataMouseDrop get(String[] paths) {
        EventDataMouseDrop data = list.size() > 0 ? list.remove(list.size() - 1) : new EventDataMouseDrop();
        data.set(paths);
        return data;
    }

    private String[] paths;

    private EventDataMouseDrop() {

    }

    private void set(String[] paths) {
        this.paths = paths;
    }

    private void release() {
        set(null);
        list.add(this);
    }

    @Override
    public void handle(Window window) {
        try {
            Activity activity = window.getActivity();
            Widget widget = activity.findByPosition(window.getPointerX(), window.getPointerY(), false);
            widget.fireDrag(new DragEvent(widget, window, DragEvent.DROPPED, paths,
                    window.getPointerX(), window.getPointerY(),
                    window.getPointerX(), window.getPointerY(), widget, null));
        } finally {
            release();
        }
    }
}
