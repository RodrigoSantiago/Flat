package flat.math.shapes;

import flat.math.*;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Represents a line segment.
 */
public final class Line implements Shape, Serializable {

    private static final long serialVersionUID = -1771222822536940013L;

    /** The x-coordinate of the start of this line segment. */
    public float x1;

    /** The y-coordinate of the start of this line segment. */
    public float y1;

    /** The x-coordinate of the end of this line segment. */
    public float x2;

    /** The y-coordinate of the end of this line segment. */
    public float y2;

    /**
     * Creates a line from (0,0) to (0,0).
     */
    public Line () {
    }

    /**
     * Creates a line from (x1,y1), to (x2,y2).
     */
    public Line (float x1, float y1, float x2, float y2) {
        set(x1, y1, x2, y2);
    }

    /**
     * Creates a line from p1 to p2.
     */
    public Line (Vector2 p1, Vector2 p2) {
        set(p1, p2);
    }

    /** Returns a mutable copy of this line. */
    public Line clone () {
        return new Line(x1, y1, x2, y2);
    }

    /**
     * Sets the start and end point of this line to the specified values.
     */
    public void set(float x1, float y1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    /**
     * Sets the start and end of this line to the specified points.
     */
    public void set(Vector2 p1, Vector2 p2) {
        set(p1.x, p1.y, p2.x, p2.y);
    }

    /** Returns the square of the distance from the specified point to the line defined by this
     * line segment. */
    public float pointLineDistSqr(float px, float py) {
        return Lines.pointLineDistSq(px, py, x1, y1, x2, y2);
    }

    /** Returns the square of the distance from the supplied point to the line defined by this line
     * segment. */
    public float pointLineDistSqr(Vector2 p) {
        return Lines.pointLineDistSq(p.x, p.y, x1, y1, x2, y2);
    }

    /** Returns the distance from the specified point to the line defined by this line segment. */
    public float pointLineDist (float px, float py) {
        return Lines.pointLineDist(px, py, x1, y1, x2, y2);
    }

    /** Returns the distance from the supplied point to the line defined by this line segment. */
    public float pointLineDist (Vector2 p) {
        return Lines.pointLineDist(p.x, p.y, x1, y1, x2, y2);
    }

    /** Returns the square of the distance from the specified point this line segment. */
    public float pointSegDistSqr(float px, float py) {
        return Lines.pointSegDistSq(px, py, x1, y1, x2, y2);
    }

    /** Returns the square of the distance from the supplied point this line segment. */
    public float pointSegDistSqr(Vector2 p) {
        return Lines.pointSegDistSq(p.x, p.y, x1, y1, x2, y2);
    }

    /** Returns the distance from the specified point this line segment. */
    public float pointSegDist (float px, float py) {
        return Lines.pointSegDist(px, py, x1, y1, x2, y2);
    }

    /** Returns the distance from the supplied point this line segment. */
    public float pointSegDist (Vector2 p) {
        return Lines.pointSegDist(p.x, p.y, x1, y1, x2, y2);
    }

    /** Returns an indicator of where the specified point (px,py) lies with respect to this line
     * segment. */
    public int relativeCCW (float px, float py) {
        return Lines.relativeCCW(px, py, x1, y1, x2, y2);
    }

    /** Returns an indicator of where the specified point lies with respect to this line segment. */
    public int relativeCCW (Vector2 p) {
        return Lines.relativeCCW(p.x, p.y, x1, y1, x2, y2);
    }

    @Override
    public boolean isOptimized() {
        return true;
    }

    @Override
    public boolean isEmpty () {
        return true;
    }

    @Override
    public boolean contains (float x, float y) {
        return Lines.pointLineDistSq(x, y, x1, y1, x2, y2) <= Mathf.EPSILON;
    }

    @Override
    public boolean contains (Vector2 point) {
        return contains(point.x, point.y);
    }

    @Override
    public boolean contains (float x, float y, float w, float h) {
        return false;
    }

    @Override
    public boolean contains (Rectangle rectangle) {
        return false;
    }

    @Override
    public boolean intersects (float rx, float ry, float rw, float rh) {
        if (rw <= 0 || rh <= 0) return false;
        return Lines.lineIntersectsRect(x1, y1, x2, y2, rx, ry, rw, rh);
    }

    @Override
    public boolean intersects (Rectangle rectangle) {
        return intersects(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public Rectangle bounds () {
        float rx, ry, rw, rh;
        if (x1 < x2) {
            rx = x1;
            rw = x2 - x1;
        } else {
            rx = x2;
            rw = x1 - x2;
        }
        if (y1 < y2) {
            ry = y1;
            rh = y2 - y1;
        } else {
            ry = y2;
            rh = y1 - y2;
        }
        return new Rectangle(rx, ry, rw, rh);
    }

    @Override
    public PathIterator pathIterator (Affine at) {
        return new Iterator(this, at);
    }

    @Override
    public PathIterator pathIterator (Affine at, float flatness) {
        return new Iterator(this, at);
    }

    @Override
    public boolean equals (Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() == getClass()) {
            Line oth = (Line)obj;
            return x1 == oth.x1 && y1 == oth.y1 && x2 == oth.x2 && y2 == oth.y2;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(x1) ^ Float.hashCode(y1) ^ Float.hashCode(x2) ^ Float.hashCode(y2);
    }

    @Override
    public String toString() {
        return "Line[x1:" + x1 + ", y1:" + y1 + ", x2:" + x2 + ", y2:" + y2 + "]";
    }

    /** An iterator over an {@link Line}. */
    protected static class Iterator implements PathIterator {
        private float x1, y1, x2, y2;
        private Affine t;
        private int index;

        Iterator (Line l, Affine at) {
            this.x1 = l.x1;
            this.y1 = l.y1;
            this.x2 = l.x2;
            this.y2 = l.y2;
            this.t = at;
        }

        @Override public int windingRule () {
            return WIND_NON_ZERO;
        }

        @Override public boolean isDone () {
            return index > 1;
        }

        @Override public void next () {
            index++;
        }

        @Override public int currentSegment (float[] coords) {
            if (isDone()) {
                throw new NoSuchElementException("Iterator out of bounds");
            }
            int type;
            if (index == 0) {
                type = SEG_MOVETO;
                coords[0] = x1;
                coords[1] = y1;
            } else {
                type = SEG_LINETO;
                coords[0] = x2;
                coords[1] = y2;
            }
            if (t != null) {
                t.transform(coords, 0, coords, 0, 1);
            }
            return type;
        }
    }
}
