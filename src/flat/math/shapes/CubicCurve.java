package flat.math.shapes;

import flat.math.*;
import flat.math.util.FlatteningPathIterator;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Represents a cubic curve.
 */
public final class CubicCurve implements Shape, Serializable {

    private static final long serialVersionUID = -3306427309314031213L;

    /**
     * The x-coordinate of the start of this curve.
     */
    public float x1;

    /**
     * The y-coordinate of the start of this curve.
     */
    public float y1;

    /**
     * The x-coordinate of the first control point.
     */
    public float ctrlx1;

    /**
     * The y-coordinate of the first control point.
     */
    public float ctrly1;

    /**
     * The x-coordinate of the second control point.
     */
    public float ctrlx2;

    /**
     * The x-coordinate of the second control point.
     */
    public float ctrly2;

    /**
     * The x-coordinate of the end of this curve.
     */
    public float x2;

    /**
     * The y-coordinate of the end of this curve.
     */
    public float y2;

    /**
     * Creates a cubic curve with all points at (0,0).
     */
    public CubicCurve() {
    }

    /**
     * Creates a cubic curve with the specified start, control, and end points.
     */
    public CubicCurve(float x1, float y1, float ctrlx1, float ctrly1,
                      float ctrlx2, float ctrly2, float x2, float y2) {
        set(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
    }

    @Override
    public CubicCurve clone() {
        return new CubicCurve(x1, y1, ctrlx1, ctrly1,
                ctrlx2, ctrly2, x2, y2);
    }

    /**
     * Configures the start, control and end points for this curve.
     */
    public void set(float x1, float y1, float ctrlx1, float ctrly1, float ctrlx2, float ctrly2, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.ctrlx1 = ctrlx1;
        this.ctrly1 = ctrly1;
        this.ctrlx2 = ctrlx2;
        this.ctrly2 = ctrly2;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * Configures the start, control and end points for this curve.
     */
    public void set(Vector2 p1, Vector2 cp1, Vector2 cp2, Vector2 p2) {
        set(p1.x, p1.y, cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y);
    }

    /**
     * Configures the start, control and end points for this curve, using the values at the
     * specified offset in the {@code coords} array.
     */
    public void set(float[] coords, int offset) {
        set(coords[offset + 0], coords[offset + 1], coords[offset + 2], coords[offset + 3],
                coords[offset + 4], coords[offset + 5], coords[offset + 6], coords[offset + 7]);
    }

    /**
     * Configures the start, control and end points for this curve, using the values at the
     * specified offset in the {@code points} array.
     */
    public void set(Vector2[] points, int offset) {
        set(points[offset + 0].x, points[offset + 0].y,
                points[offset + 1].x, points[offset + 1].y,
                points[offset + 2].x, points[offset + 2].y,
                points[offset + 3].x, points[offset + 3].y);
    }

    /**
     * Configures the start, control and end points for this curve to be the same as the supplied
     * curve.
     */
    public void set(CubicCurve curve) {
        set(curve.x1, curve.y1, curve.ctrlx1, curve.ctrly1, curve.ctrlx2, curve.ctrly2, curve.x2, curve.y2);
    }

    /**
     * Returns the square of the flatness (maximum distance of a control point from the line
     * connecting the end points) of this curve.
     */
    public float flatnessSq() {
        return CubicCurves.flatnessSq(x1, y1, ctrlx1, ctrly1,
                ctrlx2, ctrly2, x2, y2);
    }

    /**
     * Returns the flatness (maximum distance of a control point from the line connecting the end
     * points) of this curve.
     */
    public float flatness() {
        return CubicCurves.flatness(x1, y1, ctrlx1, ctrly1,
                ctrlx2, ctrly2, x2, y2);
    }

    /**
     * Subdivides this curve and stores the results into {@code left} and {@code right}.
     */
    public void subdivide(CubicCurve left, CubicCurve right) {
        CubicCurves.subdivide(this, left, right);
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
        return (cross != Crossing.CROSSING) && Crossing.isInsideEvenOdd(cross);
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
        return (cross == Crossing.CROSSING) || Crossing.isInsideEvenOdd(cross);
    }

    @Override
    public boolean intersects(Rectangle r) {
        return intersects(r.x, r.y, r.width, r.height);
    }

    @Override
    public Rectangle bounds() {
        float rx1 = Math.min(Math.min(x1, x2), Math.min(ctrlx1, ctrlx2));
        float ry1 = Math.min(Math.min(y1, y2), Math.min(ctrly1, ctrly2));
        float rx2 = Math.max(Math.max(x1, x2), Math.max(ctrlx1, ctrlx2));
        float ry2 = Math.max(Math.max(y1, y2), Math.max(ctrly1, ctrly2));
        return new Rectangle(rx1, ry1, rx2 - rx1, ry2 - ry1);
    }

    @Override
    public PathIterator pathIterator(Affine t) {
        return new Iterator(this, t);
    }

    @Override
    public PathIterator pathIterator(Affine at, float flatness) {
        return new FlatteningPathIterator(pathIterator(at), flatness);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() == getClass()) {
            CubicCurve oth = (CubicCurve) obj;
            return x1 == oth.x1 && y1 == oth.y1 && x2 == oth.x2 && y2 == oth.y2
                    && ctrlx1 == oth.ctrlx1 && ctrly1 == oth.ctrly1 && ctrlx2 == oth.ctrlx2 && ctrly2 == oth.ctrly2;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(x1) ^ Float.hashCode(y1) ^ Float.hashCode(x2) ^ Float.hashCode(y2) ^
                Float.hashCode(ctrlx1) ^ Float.hashCode(ctrly1) ^ Float.hashCode(ctrlx2) ^ Float.hashCode(ctrly2);
    }

    @Override
    public String toString() {
        return "Line[x1:" + x1 + ", y1:" + y1 + ", x2:" + x2 + ", y2:" + y2 +
                ", ctrl1 x:" + ctrlx1 + ", ctrl1 y:" + ctrly1 + ", ctrl2 x:" + ctrlx2 + ", ctrl2 y:" + ctrly2 + "]";
    }

    /**
     * An iterator over an {@link CubicCurve}.
     */
    protected static class Iterator implements PathIterator {

        private float x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2;
        private Affine t;
        private int index;

        Iterator(CubicCurve c, Affine t) {
            this.x1 = c.x1;
            this.y1 = c.y1;
            this.ctrlx1 = c.ctrlx1;
            this.ctrly1 = c.ctrly1;
            this.ctrlx2 = c.ctrlx2;
            this.ctrly2 = c.ctrly2;
            this.x2 = c.x2;
            this.y2 = c.y2;
            this.t = t;
        }

        @Override
        public int windingRule() {
            return WIND_NON_ZERO;
        }

        @Override
        public boolean isDone() {
            return index > 1;
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
                type = SEG_CUBICTO;
                coords[0] = ctrlx1;
                coords[1] = ctrly1;
                coords[2] = ctrlx2;
                coords[3] = ctrly2;
                coords[4] = x2;
                coords[5] = y2;
                count = 3;
            }
            if (t != null) {
                t.transform(coords, 0, coords, 0, count);
            }
            return type;
        }
    }
}