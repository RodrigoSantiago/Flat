package flat.graphics;

import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;

import java.util.ArrayList;

class ClipState {
    static final Rectangle noClip = new Rectangle();

    public Rectangle box = noClip;
    public final ArrayList<Shape> clipShapes = new ArrayList<>();
    public final ArrayList<Rectangle> clipBox = new ArrayList<>();

    public void clear() {
        clipShapes.clear();
        clipBox.clear();
        box = noClip;
    }

    public boolean isClear() {
        return box == noClip;
    }

    public boolean isFullyClipped() {
        return box == null;
    }
}
