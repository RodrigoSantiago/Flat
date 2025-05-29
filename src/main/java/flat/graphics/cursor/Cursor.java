package flat.graphics.cursor;

import flat.backend.WL;
import flat.backend.WLEnums;
import flat.exception.FlatException;

import java.lang.ref.Cleaner;
import java.util.HashMap;

public class Cursor {
    protected final static Cleaner cleaner = Cleaner.create();

    public static final Cursor UNSET = new Cursor(0);
    public static final Cursor NONE = new Cursor(0);
    public static final Cursor ARROW = new Cursor(WLEnums.STANDARD_ARROW_CURSOR);
    public static final Cursor IBEAM = new Cursor(WLEnums.STANDARD_IBEAM_CURSOR);
    public static final Cursor CROSSHAIR = new Cursor(WLEnums.STANDARD_CROSSHAIR_CURSOR);
    public static final Cursor HAND = new Cursor(WLEnums.STANDARD_HAND_CURSOR);
    public static final Cursor EW_RESIZE = new Cursor(WLEnums.STANDARD_EW_RESIZE_CURSOR);
    public static final Cursor NS_RESIZE = new Cursor(WLEnums.STANDARD_NS_RESIZE_CURSOR);
    public static final Cursor NWSE_RESIZE = new Cursor(WLEnums.STANDARD_NWSE_RESIZE_CURSOR);
    public static final Cursor NESW_RESIZE = new Cursor(WLEnums.STANDARD_NESW_RESIZE_CURSOR);
    public static final Cursor MOVE = new Cursor(WLEnums.STANDARD_RESIZE_ALL_CURSOR);
    public static final Cursor NOT = new Cursor(WLEnums.STANDARD_NOT_ALLOWED_CURSOR);

    private static HashMap<String, Cursor> standardCursors;
    private final int shape;
    private long cursor = 0;

    private Cursor(int shape) {
        this.shape = shape;
    }

    public Cursor(byte[] image, int x, int y, int width, int height) {
        if (image.length < width * height * 4) {
            throw new FlatException("Invalid image data size");
        }

        final long cursorId = WL.CreateCursor(image, width, height, x, y);
        if (cursorId == 0) {
            throw new FlatException("The cursor cannot be created");
        }
        cleaner.register(this, () -> WL.DestroyCursor(cursorId));

        this.shape = 0;
        this.cursor = cursorId;
    }

    public static Cursor getByName(String string) {
        if (standardCursors == null) {
            standardCursors = new HashMap<>();
            standardCursors.put("UNSET", Cursor.UNSET);
            standardCursors.put("NONE", Cursor.NONE);
            standardCursors.put("ARROW", Cursor.ARROW);
            standardCursors.put("IBEAM", Cursor.IBEAM);
            standardCursors.put("CROSSHAIR", Cursor.CROSSHAIR);
            standardCursors.put("HAND", Cursor.HAND);
            standardCursors.put("EW_RESIZE", Cursor.EW_RESIZE);
            standardCursors.put("NS_RESIZE", Cursor.NS_RESIZE);
            standardCursors.put("NWSE_RESIZE", Cursor.NWSE_RESIZE);
            standardCursors.put("NESW_RESIZE", Cursor.NESW_RESIZE);
            standardCursors.put("MOVE", Cursor.MOVE);
            standardCursors.put("NOT", Cursor.NOT);
        }
        return standardCursors.get(string.toUpperCase());
    }

    public long getInternalCursor() {
        if (this.cursor == 0 && this.shape != 0) {
            this.cursor = WL.CreateStandardCursor(shape);
        }
        return cursor;
    }
}
