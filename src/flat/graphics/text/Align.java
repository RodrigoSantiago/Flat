package flat.graphics.text;

public final class Align {

    public enum Vertical {
        MIDDLE, TOP, BOTTOM, BASELINE;

        public int getInternalEnum() {
            return 0;
        }
    }

    public enum Horizontal {
        LEFT, CENTER, RIGHT;

        public int getInternalEnum() {
            return 0;
        }
    }

    private Align() {
    }
}
