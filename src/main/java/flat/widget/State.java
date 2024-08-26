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

    public byte bitset() {
        return (byte) (1 << ordinal());
    }

    public boolean contains(int flag) {
        return (flag & bitset()) == bitset();
    }
}
