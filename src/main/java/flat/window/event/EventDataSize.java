package flat.window.event;

import flat.window.Window;

import java.util.ArrayList;

public class EventDataSize extends EventData {
    private static final ArrayList<EventDataSize> list = new ArrayList<>();

    static EventDataSize get(int width, int height) {
        EventDataSize data = list.size() > 0 ? list.remove(list.size() - 1) : new EventDataSize();
        data.set(width, height);
        return data;
    }

    private int width, height;

    private EventDataSize() {

    }

    private void set(int width, int height) {
        this.width = width;
        this.height = height;
    }

    private void release() {
        set(0, 0);
        list.add(this);
    }

    @Override
    public void handle(Window window) {
        window.getActivity().invalidate();
        release();
    }
}
