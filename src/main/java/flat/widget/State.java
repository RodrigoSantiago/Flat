package flat.widget;

public enum State {
    ENABLED,
    HOVERED,
    FOCUSED,
    PRESSED,
    DRAGGED,
    ACTIVATED,
    UNDEFINED,
    DISABLED;

    public byte bitset() {
        return (byte) (1 << ordinal());
    }

    public boolean contains(int flag) {
        return (flag & bitset()) == bitset();
    }
}
