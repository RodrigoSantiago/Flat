package flat.graphics.cursor;

import flat.backend.WL;
import flat.backend.WLEnuns;
import flat.graphics.context.Texture2D;
import flat.widget.Application;

public class Cursor {
    public static final Cursor ARROW = new Cursor(WLEnuns.STANDARD_ARROW_CURSOR);
    public static final Cursor IBEAM = new Cursor(WLEnuns.STANDARD_IBEAM_CURSOR);
    public static final Cursor CROSSHAIR = new Cursor(WLEnuns.STANDARD_CROSSHAIR_CURSOR);
    public static final Cursor HAND = new Cursor(WLEnuns.STANDARD_HAND_CURSOR);
    public static final Cursor HRESIZE = new Cursor(WLEnuns.STANDARD_HRESIZE_CURSOR);
    public static final Cursor VRESIZE = new Cursor(WLEnuns.STANDARD_VRESIZE_CURSOR);

    private final int shape;
    private int width, height, xcenter, ycenter;
    private byte[] image;

    private long cursor = 0;

    Cursor(int shape) {
        this.shape = shape;
    }

    Cursor(Texture2D texture, int xCenter, int yCenter) {
        this.shape = 0;
        this.image = new byte[]{-1, -1, -1, -1};
        this.width = texture.getWidth();
        this.height = texture.getHeight();
        this.xcenter = xCenter;
        this.ycenter = yCenter;
    }

    public long getInternalCursor() {
        if (this.cursor == 0) {
            if (image == null) {
                this.cursor = WL.CreateStandardCursor(shape);
            } else {
                this.cursor = WL.CreateCursor(image, width, height, xcenter, ycenter);
            }
        }
        return cursor;
    }

    @Override
    protected void finalize() {
        if (this.cursor != 0) {
            final long cursor = this.cursor;
            Application.runSync(() -> WL.DestroyCursor(cursor));
        }
    }
}
