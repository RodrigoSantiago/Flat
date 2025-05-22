package flat.graphics.cursor;

import flat.backend.WL;
import flat.backend.WLEnums;

public enum Cursor {
    UNSET(0),
    NONE(0),
    ARROW(WLEnums.STANDARD_ARROW_CURSOR),
    IBEAM(WLEnums.STANDARD_IBEAM_CURSOR),
    CROSSHAIR(WLEnums.STANDARD_CROSSHAIR_CURSOR),
    HAND(WLEnums.STANDARD_HAND_CURSOR),
    EW_RESIZE(WLEnums.STANDARD_EW_RESIZE_CURSOR),
    NS_RESIZE(WLEnums.STANDARD_NS_RESIZE_CURSOR),
    NWSE_RESIZE(WLEnums.STANDARD_NWSE_RESIZE_CURSOR),
    NESW_RESIZE(WLEnums.STANDARD_NESW_RESIZE_CURSOR),
    MOVE(WLEnums.STANDARD_RESIZE_ALL_CURSOR),
    NOT(WLEnums.STANDARD_NOT_ALLOWED_CURSOR);

    private final int shape;
    private long cursor = 0;

    Cursor(int shape) {
        this.shape = shape;
    }

    public long getInternalCursor() {
        if (this.shape == 0) {
            return 0;
        }
        if (this.cursor == 0) {
            this.cursor = WL.CreateStandardCursor(shape);
        }
        return cursor;
    }
}
