package flat.graphics.cursor;

import flat.backend.WL;
import flat.backend.WLEnums;

public enum Cursor {
    UNSET(0),
    NONE(0),
    ARROW(WLEnums.STANDARD_ARROW_CURSOR),
    IBEAM(WLEnums.STANDARD_IBEAM_CURSOR),
    CROSSHAIR(WLEnums.STANDARD_CROSSHAIR_CURSOR),
    HAND (WLEnums.STANDARD_HAND_CURSOR),
    HRESIZE(WLEnums.STANDARD_HRESIZE_CURSOR),
    VRESIZE(WLEnums.STANDARD_VRESIZE_CURSOR);

    private final int shape;
    private long cursor = 0;

    Cursor(int shape) {
        this.shape = shape;
    }

    public long getInternalCursor() {
        if (this.cursor == 0 && this.shape != 0) {
            this.cursor = WL.CreateStandardCursor(shape);
        }
        return cursor;
    }
}
