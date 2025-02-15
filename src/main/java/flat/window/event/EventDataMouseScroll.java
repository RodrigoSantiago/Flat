package flat.window.event;

import flat.events.ScrollEvent;
import flat.window.Activity;
import flat.widget.Widget;
import flat.window.Application;
import flat.window.Window;

import java.util.ArrayList;

public class EventDataMouseScroll extends EventData {
    static ArrayList<EventDataMouseScroll> list = new ArrayList<>();

    static EventDataMouseScroll get(double x, double y) {
        EventDataMouseScroll data = list.size() > 0 ? list.remove(list.size() - 1) : new EventDataMouseScroll();
        data.set((float) x, (float) y);
        return data;
    }

    private float x, y;

    private EventDataMouseScroll() {

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
            Activity activity = window.getActivity();
            Widget widget = activity.findByPosition(window.getPointerX(), window.getPointerY(), false);
            try {
                widget.fireScroll(new ScrollEvent(widget, ScrollEvent.SCROLL, x, y));
            } catch (Exception e) {
                Application.handleException(e);
            }
        } finally {
            release();
        }
    }
}
