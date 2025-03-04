package flat.window.event;

import flat.window.Window;

public abstract class EventData {

    public static EventData getCharMode(int codepoint, int mods) {
        return EventDataCharMods.get(codepoint, mods);
    }

    public static EventData getKey(int key, int scancode, int action, int mods) {
        return EventDataKey.get(key, scancode, action, mods);
    }

    public static EventData getMouseButton(int button, int action) {
        return EventDataMouseButton.get(button, action);
    }

    public static EventData getMouseDrop(String[] paths) {
        return EventDataMouseDrop.get(paths);
    }

    public static EventData getMouseMove(float x, float y) {
        return EventDataMouseMove.get(x, y);
    }

    public static EventData getMouseScroll(float x, float y) {
        return EventDataMouseScroll.get(x, y);
    }

    public static EventData getSize(int x, int y) {
        return EventDataSize.get(x, y);
    }

    public abstract void handle(Window window);
}
