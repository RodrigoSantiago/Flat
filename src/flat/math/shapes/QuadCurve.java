package flat.math.shapes;

import flat.math.*;
import flat.math.util.FlatteningPathIterator;
import flat.math.util.Platform;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Represents a quadratic curve.
 */
public final class QuadCurve implements Shape, Serializable {

    private static final long serialVersionUID = -6760122161413212105L;

    /**
     * The x-coordinate of the start of this curve.
     */
    public float x1;

    /**
     * The y-coordinate of the start of this curve.
     */
    public float y1;

    /**
     * The x-coordinate of the control point.
     */
    public float ctrlx;

    /**
     * The y-coordinate of the control point.
     */
    public float ctrly;

    /**
     * The x-coordinate of the end of this curve.
     */
    public float x2;

    /**
     * The y-coordinate of the end of this curve.
     */
    public float y2;

    /**
     * Creates a quad curve with all points at (0,0).
     */
    public QuadCurve() {
    }

    /**
     * Creates a quad curve with the specified start, control, and end points.
     */
    public QuadCurve(float x1, float y1, float ctrlx, float ctrly, float x2, float y2) {
        set(x1, y1, ctrlx, ctrly, x2, y2);
    }

    /**
     * Configures the start, control, and end points for this curve.
     */
    public void set(float x1, float y1, float ctrlx, float ctrly, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.ctrlx = ctrlx;
        this.ctrly = ctrly;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * Configures the start, control, and end points for this curve.
     */
    public void set(Vector2 p1, Vector2 cp, Vector2 p2) {
        set(p1.x, p1.y, cp.x, cp.y, p2.x, p2.y);
    }

    /**
     * Configures the start, control, and end points for this curve, using the values at the
     * specified offset in the {@code coords} array.
     */
    public void set(float[] coords, int offset) {
        set(coords[offset + 0], coords[offset + 1],
                coords[offset + 2], coords[offset + 3],
                coords[offset + 4], coords[offset + 5]);
    }

    /**
     * Configures the start, control, and end points for this curve, using the values at the
     * specified offset in the {@code points} array.
     */
    public void set(Vector2[] points, int offset) {
        set(points[offset + 0].x, points[offset + 0].y,
                points[offset + 1].x, points[offset + 1].y,
                points[offset + 2].x, points[offset + 2].y);
    }

    /**
     * Configures the start, control, and end points for this curve to be the same as the supplied
     * curve.
     */
    public void set(QuadCurve curve) {
        set(curve.x1, curve.y1, curve.ctrlx, curve.ctrly,
                curve.x2, curve.y2);
    }

    /**
     * Returns the square of the flatness (maximum distance of a control point from the line
     * connecting the end points) of this curve.
     */
    public float flatnessSq() {
        return Lines.pointSegDistSq(ctrlx, ctrly, x1, y1, x2, y2);
    }

    /**
     * Returns the flatness (maximum distance of a control point from the line connecting the end
     * points) of this curve.
     */
    public float flatness() {
        return Lines.pointSegDist(ctrlx, ctrly, x1, y1, x2, y2);
    }

    /**
     * Subdivides this curve and stores the results into {@code left} and {@code right}.
     */
    public void subdivide(QuadCurve left, QuadCurve right) {
        QuadCurves.subdivide(this, left, right);
    }

    /**
     * Returns a mutable copy of this curve.
     */
    public QuadCurve clone() {
        return new QuadCurve(x1, y1, ctrlx, ctrly, x2, y2);
    }

    @Override
    public boolean isEmpty() {
        return true; // curves contain no space
    }

    @Override
    public boolean contains(float px, float py) {
        return Crossing.isInsideEvenOdd(Crossing.crossShape(this, px, py));
    }

    @Override
    public boolean contains(float rx, float ry, float rw, float rh) {
        int cross = Crossing.intersectShape(this, rx, ry, rw, rh);
        return cross != Crossing.CROSSING && Crossing.isInsideEvenOdd(cross);
    }

    @Override
    public boolean contains(Vector2 p) {
        return contains(p.x, p.y);
    }

    @Override
    public boolean contains(Rectangle r) {
        return contains(r.x, r.y, r.width, r.height);
    }

    @Override
    public boolean intersects(float rx, float ry, float rw, float rh) {
        int cross = Crossing.intersectShape(this, rx, ry, rw, rh);
        return cross == Crossing.CROSSING || Crossing.isInsideEvenOdd(cross);
    }

    @Override
    public boolean intersects(Rectangle r) {
        return intersects(r.x, r.y, r.width, r.height);
    }

    @Override
    public Rectangle bounds() {
        float rx0 = Math.min(Math.min(x1, x2), ctrlx);
        float ry0 = Math.min(Math.min(y1, y2), ctrly);
        float rx1 = Math.max(Math.max(x1, x2), ctrlx);
        float ry1 = Math.max(Math.max(y1, y2), ctrly);
        return new Rectangle(rx0, ry0, rx1 - rx0, ry1 - ry0);
    }

    @Override
    public PathIterator pathIterator(Affine t) {
        return new Iterator(this, t);
    }

    @Override
    public PathIterator pathIterator(Affine t, float flatness) {
        return new FlatteningPathIterator(pathIterator(t), flatness);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() == getClass()) {
            QuadCurve oth = (QuadCurve) obj;
            return x1 == oth.x1 && y1 == oth.y1 && x2 == oth.x2 && y2 == oth.y2
                    && ctrlx == oth.ctrlx && ctrly == oth.ctrly;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Platform.hashCode(x1) ^ Platform.hashCode(y1) ^ Platform.hashCode(x2) ^ Platform.hashCode(y2) ^
                Platform.hashCode(ctrlx) ^ Platform.hashCode(ctrly);
    }

    @Override
    public String toString() {
        return "Line[x1:" + x1 + ", y1:" + y1 + ", x2:" + x2 + ", y2:" + y2 +
                ", ctrl x:" + ctrlx + ", ctrl y:" + ctrly + "]";
    }

    /**
     * An iterator over an {@link QuadCurve}.
     */
    protected static class Iterator implements PathIterator {
        private float x1, y1, ctrlx, ctrly, x2, y2;
        private Affine t;
        private int index;

        Iterator(QuadCurve q, Affine t) {
            this.x1 = q.x1;
            this.y1 = q.y1;
            this.ctrlx = q.ctrlx;
            this.ctrly = q.ctrly;
            this.x2 = q.x2;
            this.y2 = q.y2;
            this.t = t;
        }

        @Override
        public int windingRule() {
            return WIND_NON_ZERO;
        }

        @Override
        public boolean isDone() {
            return (index > 1);
        }

        @Override
        public void next() {
            index++;
        }

        @Override
        public int currentSegment(float[] coords) {
            if (isDone()) {
                throw new NoSuchElementException("Iterator out of bounds");
            }
            int type;
            int count;
            if (index == 0) {
                type = SEG_MOVETO;
                coords[0] = x1;
                coords[1] = y1;
                count = 1;
            } else {
                type = SEG_QUADTO;
                coords[0] = ctrlx;
                coords[1] = ctrly;
                coords[2] = x2;
                coords[3] = y2;
                count = 2;
            }
            if (t != null) {
                t.transform(coords, 0, coords, 0, count);
            }
            return type;
        }
    }
}