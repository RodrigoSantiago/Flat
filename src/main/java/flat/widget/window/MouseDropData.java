package flat.widget.window;

import flat.events.DragEvent;
import flat.widget.Activity;
import flat.widget.Widget;
import flat.widget.Window;

import java.util.ArrayList;

public class MouseDropData extends EventData {
    private static final ArrayList<MouseDropData> list = new ArrayList<>();

    static MouseDropData get(String[] paths) {
        MouseDropData data = list.size() > 0 ? list.remove(list.size() - 1) : new MouseDropData();
        data.set(paths);
        return data;
    }

    private String[] paths;

    private MouseDropData() {

    }

    private void set(String[] paths) {
        this.paths = paths;
    }

    private void release() {
        set(null);
        list.add(this);
    }

    @Override
    void handle(Window window) {
        Activity activity = window.getActivity();
        Widget widget = activity.findByPosition(window.getPointerX(), window.getPointerY(), false);
        widget.fireDrag(new DragEvent(widget, DragEvent.DROPPED, paths, window.getPointerX(), window.getPointerY()));

        release();
    }
}
