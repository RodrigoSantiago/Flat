package flat.widget.window;

import flat.events.ScrollEvent;
import flat.widget.Activity;
import flat.widget.Widget;
import flat.widget.Window;

import java.util.ArrayList;

public class MouseScrollData extends EventData {
    static ArrayList<MouseScrollData> list = new ArrayList<>();

    static MouseScrollData get(double x, double y) {
        MouseScrollData data = list.size() > 0 ? list.remove(list.size() - 1) : new MouseScrollData();
        data.set((float) x, (float) y);
        return data;
    }

    private float x, y;

    private MouseScrollData() {

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
        Activity activity = window.getActivity();
        Widget widget = activity.findByPosition(window.getPointerX(), window.getPointerY(), false);
        widget.fireScroll(new ScrollEvent(widget, ScrollEvent.SCROLL, x, y));

        release();
    }
}
