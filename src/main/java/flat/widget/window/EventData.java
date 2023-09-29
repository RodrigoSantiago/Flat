package flat.widget.window;

import flat.widget.Window;

public abstract class EventData {

    public static EventData getCharMode(int codepoint, int mods) {
        return CharModsData.get(codepoint, mods);
    }

    public static EventData getKey(int key, int scancode, int action, int mods) {
        return KeyData.get(key, scancode, action, mods);
    }

    public static EventData getMouseButton(int button, int action, int mods) {
        return MouseButtonData.get(button, action, mods);
    }

    public static EventData getMouseDrop(String[] paths) {
        return MouseDropData.get(paths);
    }

    public static EventData getMouseMove(float x, float y) {
        return MouseMoveData.get(x, y);
    }

    public static EventData getMouseScroll(float x, float y) {
        return MouseScrollData.get(x, y);
    }

    public static EventData getSize(int x, int y) {
        return SizeData.get(x, y);
    }

    public static void consume(Window window, EventData eventData) {
        eventData.handle(window);
    }

    abstract void handle(Window window);
}
