package flat.widget;

public enum State {
    ENABLED,
    FOCUSED,
    ACTIVATED,
    HOVERED,
    PRESSED,
    DRAGGED,
    ERROR,
    DISABLED;

    public int bitset() {
        return 1 << ordinal();
    }
}
