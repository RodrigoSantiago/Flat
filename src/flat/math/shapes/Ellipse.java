package flat.math.shapes;

import flat.math.Affine;
import flat.math.Mathf;
import flat.math.Vector2;
import flat.math.util.FlatteningPathIterator;
import flat.math.util.Platform;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Represents an ellipse that is described by a framing rectangle.
 */
public final class Ellipse implements Shape, Serializable {

    private static final long serialVersionUID = -1205529661373764424L;

    /** The bounding values of this ellipse. */
    public float x, y, width, height;

    /**
     * Creates an ellipse with framing rectangle (0x0+0+0).
     */
    public Ellipse () {
    }

    /**
     * Creates an ellipse with the specified framing rectangle.
     */
    public Ellipse (float x, float y, float width, float height) {
        set(x, y, width, height);
    }

    @Override
    public Ellipse clone () {
        return new Ellipse(x, y, width, height);
    }

    public void set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean isEmpty() {
        return width <= 0 || height <= 0;
    }

    @Override
    public Rectangle bounds() {
        return new Rectangle(x, y, width, height);
    }

    @Override
    public boolean contains (float px, float py) {
        if (isEmpty()) return false;
        float a = (px - x) / width - 0.5f;
        float b = (py - y) / height - 0.5f;
        return a * a + b * b < 0.25f;
    }

    @Override
    public boolean contains (Vector2 point) {
        return contains(point.x, point.y);
    }

    @Override
    public boolean contains (float rx, float ry, float rw, float rh) {
        if (isEmpty() || rw <= 0f || rh <= 0f) return false;
        float rx2 = rx + rw, ry2 = ry + rh;
        return contains(rx, ry) && contains(rx2, ry) && contains(rx2, ry2) && contains(rx, ry2);
    }

    @Override
    public boolean contains (Rectangle rectangle) {
        return contains(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public boolean intersects (float rx, float ry, float rw, float rh) {
        if (isEmpty() || rw <= 0f || rh <= 0f) return false;
        float cx = x + width / 2f;
        float cy = y + height / 2f;
        float rx2 = rx + rw, ry2 = ry + rh;
        float nx = cx < rx ? rx : (cx > rx2 ? rx2 : cx);
        float ny = cy < ry ? ry : (cy > ry2 ? ry2 : cy);
        return contains(nx, ny);
    }

    @Override
    public boolean intersects (Rectangle rectangle) {
        return intersects(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public PathIterator pathIterator (Affine at) {
        return new Iterator(this, at);
    }

    @Override
    public PathIterator pathIterator(Affine at, float flatness) {
        return new FlatteningPathIterator(pathIterator(at), flatness);
    }

    @Override
    public boolean equals (Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() == getClass()) {
            Ellipse elp = (Ellipse)obj;
            return x == elp.x && y == elp.y && width == elp.width && height == elp.height;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Platform.hashCode(x) ^ Platform.hashCode(y) ^ Platform.hashCode(width) ^ Platform.hashCode(height);
    }

    @Override
    public String toString() {
        return "Ellipse[x:" + x + ", y:" + y + ", width:" + width + ", height:" + height + "]";
    }

    /** An iterator over an {@link Ellipse}. */
    protected static class Iterator implements PathIterator {
        private final float x, y, width, height;
        private final Affine t;
        private int index;

        Iterator (Ellipse e, Affine t) {
            this.x = e.x;
            this.y = e.y;
            this.width = e.width;
            this.height = e.height;
            this.t = t;
            if (width < 0f || height < 0f) {
                index = 6;
            }
        }

        @Override public int windingRule () {
            return WIND_NON_ZERO;
        }

        @Override public boolean isDone () {
            return index > 5;
        }

        @Override public void next () {
            index++;
        }

        @Override public int currentSegment (float[] coords) {
            if (isDone()) {
                throw new NoSuchElementException("Iterator out of bounds");
            }
            if (index == 5) {
                return SEG_CLOSE;
            }
            int type;
            int count;
            if (index == 0) {
                type = SEG_MOVETO;
                count = 1;
                float[] p = POINTS[3];
                coords[0] = x + p[4] * width;
                coords[1] = y + p[5] * height;
            } else {
                type = SEG_CUBICTO;
                count = 3;
                float[] p = POINTS[index - 1];
                int j = 0;
                for (int i = 0; i < 3; i++) {
                    coords[j] = x + p[j++] * width;
                    coords[j] = y + p[j++] * height;
                }
            }
            if (t != null) {
                t.transform(coords, 0, coords, 0, count);
            }
            return type;
        }
    }

    // An ellipse is subdivided into four quarters by x and y axis. Each part is approximated by a
    // cubic Bezier curve. The arc in the first quarter starts in (a, 0) and finishes in (0, b)
    // points. Control points for the cubic curve are (a, 0), (a, m), (n, b) and (0, b) where n and
    // m are calculated based on the requirement that the Bezier curve in point 0.5 should lay on
    // the arc.

    /** The coefficient to calculate control points of Bezier curves. */
    private static final float U = 2f / 3f * (Mathf.sqrt(2) - 1f);

    /** The points coordinates calculation table. */
    private static final float[][] POINTS = {
            { 1f,       0.5f + U, 0.5f + U, 1f,       0.5f, 1f },
            { 0.5f - U, 1f,       0f,       0.5f + U, 0f,   0.5f },
            { 0f,       0.5f - U, 0.5f - U, 0f,       0.5f, 0f },
            { 0.5f + U, 0f,       1f,       0.5f - U, 1f,   0.5f } };
}
