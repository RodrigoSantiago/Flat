package flat.graphics.cursor;

import flat.backend.WL;
import flat.backend.WLEnums;

public class Cursor {
    public static final Cursor ARROW = new Cursor(WLEnums.STANDARD_ARROW_CURSOR);
    public static final Cursor IBEAM = new Cursor(WLEnums.STANDARD_IBEAM_CURSOR);
    public static final Cursor CROSSHAIR = new Cursor(WLEnums.STANDARD_CROSSHAIR_CURSOR);
    public static final Cursor HAND = new Cursor(WLEnums.STANDARD_HAND_CURSOR);
    public static final Cursor HRESIZE = new Cursor(WLEnums.STANDARD_HRESIZE_CURSOR);
    public static final Cursor VRESIZE = new Cursor(WLEnums.STANDARD_VRESIZE_CURSOR);

    private final int shape;
    private long cursor = 0;

    Cursor(int shape) {
        this.shape = shape;
    }

    public Cursor(int width, int height, int xCenter, int yCenter, byte[] image) {
        this.shape = 0;
        this.cursor = WL.CreateCursor(image, width, height, xCenter, yCenter);
    }

    public long getInternalCursor() {
        if (this.cursor == 0) {
            this.cursor = WL.CreateStandardCursor(shape);
        }
        return cursor;
    }
}
