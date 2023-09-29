package flat.widget.window;

import flat.widget.Window;

import java.util.ArrayList;

public class SizeData extends EventData {
    private static final ArrayList<SizeData> list = new ArrayList<>();

    static SizeData get(int width, int height) {
        SizeData data = list.size() > 0 ? list.remove(list.size() - 1) : new SizeData();
        data.set(width, height);
        return data;
    }

    void release() {
        set(0, 0);
        list.add(this);
    }

    private int width, height;

    private SizeData() {

    }

    private void set(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    void handle(Window window) {
        window.getActivity().invalidate(true); // Todo transition activities behavior / DPI change behavior
        release();
    }
}
