package flat.math.shapes;

import flat.math.Affine;
import flat.math.Mathf;
import flat.math.Vector2;
import flat.math.util.FlatteningPathIterator;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Represents an arc defined by a framing rectangle, start angle, angular extend, and closure type.
 */
public final class Arc implements Shape, Serializable {

    private static final long serialVersionUID = 378120636227888073L;

    public enum Type {
        /** An arc type indicating a simple, unconnected curve. */
        OPEN,

        /** An arc type indicating a closed curve, connected by a straight line from the starting to
         * the ending point of the arc. */
        CHORD,

        /** An arc type indicating a closed curve, connected by a line from the starting point of the
         * arc to the center of the circle defining the arc, and another straight line from that center
         * to the ending point of the arc. */
        PIE
    }

    /** The bounding values of this arc. */
    public float x, y, width, height;

    /** The starting angle of this arc. */
    public float start;

    /** The angular extent of this arc. */
    public float extent;

    /** The type of this arc. */
    private Type type;

    /**
     * Creates an open arc with frame (0x0+0+0) and zero angles.
     */
    public Arc () {
        this(Type.OPEN);
    }

    /**
     * Creates an arc of the specified type with frame (0x0+0+0) and zero angles.
     */
    public Arc (Type type) {
        this.type = type;
    }

    /**
     * Creates an arc of the specified type with the specified framing rectangle, starting angle
     * and angular extent.
     */
    public Arc (float x, float y, float width, float height, float start, float extent, Type type) {
        set(x, y, width, height, start, extent, type);
    }

    /**
     * Creates an arc of the specified type with the supplied framing rectangle, starting angle and
     * angular extent.
     */
    public Arc (Rectangle bounds, float start, float extent, Type type) {
        set(bounds.x, bounds.y, bounds.width, bounds.height, start, extent, type);
    }

    @Override
    public Arc clone () {
        return new Arc(x, y, width, height, start, extent, type);
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc to the specified
     * values.
     */
    public void set(float x, float y, float width, float height, float start, float extent, Type type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.start = start;
        this.extent = extent;
        this.type = type;
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc to the specified
     * values.
     */
    public void set(Rectangle rect, float start, float extent, Type type) {
        set(rect.x, rect.y, rect.width, rect.height, start, extent, type);
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc to the same values as
     * the supplied arc.
     */
    public void set(Arc arc) {
        set(arc.x, arc.y, arc.width, arc.height, arc.start, arc.extent, arc.type);
    }

    /**
     * Sets the bounding size of this arc
     */
    public void setSize(float x, float y, float width, float height) {
        set(x, y, width, height, start, extent, type);
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc based on the
     * specified values.
     */
    public void setByCenter(float x, float y, float radius, float start, float extent, Type type) {
        set(x - radius, y - radius, radius * 2f, radius * 2f, start, extent, type);
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc based on the
     * specified values.
     */
    public void setByTangent(Vector2 p1, Vector2 p2, Vector2 p3, float radius) {
        // use simple geometric calculations of arc center, radius and angles by tangents
        float a1 = -Mathf.atan2(p1.y - p2.y, p1.x - p2.x);
        float a2 = -Mathf.atan2(p3.y - p2.y, p3.x - p2.x);
        float am = (a1 + a2) / 2f;
        float ah = a1 - am;
        float d = radius / Math.abs(Mathf.sin(ah));
        float x = p2.x + d * Mathf.cos(am);
        float y = p2.y - d * Mathf.sin(am);
        ah = ah >= 0f ? Mathf.PI * 1.5f - ah : Mathf.PI * 0.5f - ah;
        a1 = normAngle(Mathf.toDegrees(am - ah));
        a2 = normAngle(Mathf.toDegrees(am + ah));
        float delta = a2 - a1;
        if (delta <= 0f) {
            delta += 360f;
        }
        setByCenter(x, y, radius, a1, delta, type);
    }

    /**
     * Sets the starting angle of this arc to the angle defined by the supplied point relative to
     * the center of this arc.
     */
    public void setAngleStart (float x, float y) {
        float angle = Mathf.atan2(y - (this.y + height) / 2f, x - (this.x + width) / 2f);
        start = normAngle(-Mathf.toDegrees(angle));
    }

    /**
     * Sets the starting angle of this arc to the angle defined by the supplied point relative to
     * the center of this arc.
     */
    public void setAngleStart (Vector2 point) {
        setAngleStart(point.x, point.y);
    }

    /**
     * Sets the starting angle and angular extent of this arc using two sets of coordinates. The
     * first set of coordinates is used to determine the angle of the starting point relative to
     * the arc's center. The second set of coordinates is used to determine the angle of the end
     * point relative to the arc's center. The arc will always be non-empty and extend
     * counterclockwise from the first point around to the second point.
     */
    public void setAngles (float x1, float y1, float x2, float y2) {
        float cx = (x + width) / 2f;
        float cy = (y + height) / 2f;
        float a1 = normAngle(-Mathf.toDegrees(Mathf.atan2(y1 - cy, x1 - cx)));
        float a2 = normAngle(-Mathf.toDegrees(Mathf.atan2(y2 - cy, x2 - cx)));
        a2 -= a1;
        if (a2 <= 0f) {
            a2 += 360f;
        }
        start = a1;
        extent = a2;
    }

    /**
     * Sets the starting angle and angular extent of this arc using two sets of coordinates. The
     * first set of coordinates is used to determine the angle of the starting point relative to
     * the arc's center. The second set of coordinates is used to determine the angle of the end
     * point relative to the arc's center. The arc will always be non-empty and extend
     * counterclockwise from the first point around to the second point.
     */
    public void setAngles (Vector2 p1, Vector2 p2) {
        setAngles(p1.x, p1.y, p2.x, p2.y);
    }

    /** Returns the intersection of the ray from the center (defined by the starting angle) and the
     * elliptical boundary of the arc. */
    public Vector2 startPoint () {
        float a = Mathf.toRadians(start);
        return new Vector2(x + (1f + Mathf.cos(a)) * width / 2f, y + (1f - Mathf.sin(a)) * height / 2f);
    }

    /** Returns the intersection of the ray from the center (defined by the starting angle plus the
     * angular extent of the arc) and the elliptical boundary of the arc. */
    public Vector2 endPoint () {
        float a = Mathf.toRadians(start + extent);
        return new Vector2(x + (1f + Mathf.cos(a)) * width / 2f, y + (1f - Mathf.sin(a)) * height / 2f);
    }

    /** Returns whether the specified angle is within the angular extents of this arc. */
    public boolean containsAngle (float angle) {
        float extent = this.extent;
        if (extent >= 360f) {
            return true;
        }
        angle = normAngle(angle);
        float a1 = normAngle(start);
        float a2 = a1 + extent;
        if (a2 > 360f) {
            return angle >= a1 || angle <= a2 - 360f;
        }
        if (a2 < 0f) {
            return angle >= a2 + 360f || angle <= a1;
        }
        return (extent > 0f) ? a1 <= angle && angle <= a2 : a2 <= angle && angle <= a1;
    }

    @Override
    public boolean isEmpty () {
        return extent == 0 || width <= 0 || height <= 0;
    }

    @Override
    public Rectangle bounds () {
        if (isEmpty()) {
            return new Rectangle(x, y, width, height);
        }

        float rx1 = x;
        float ry1 = y;
        float rx2 = rx1 + width;
        float ry2 = ry1 + height;

        Vector2 p1 = startPoint(), p2 = endPoint();

        float bx1 = containsAngle(180f) ? rx1 : Math.min(p1.x, p2.x);
        float by1 = containsAngle(90f) ? ry1 : Math.min(p1.y, p2.y);
        float bx2 = containsAngle(0f) ? rx2 : Math.max(p1.x, p2.x);
        float by2 = containsAngle(270f) ? ry2 : Math.max(p1.y, p2.y);

        if (type == Type.PIE) {
            float cx = (x + width) / 2f;
            float cy = (y + height) / 2f;
            bx1 = Math.min(bx1, cx);
            by1 = Math.min(by1, cy);
            bx2 = Math.max(bx2, cx);
            by2 = Math.max(by2, cy);
        }
        return new Rectangle(bx1, by1, bx2 - bx1, by2 - by1);
    }

    @Override
    public boolean contains (Vector2 point) {
        return contains(point.x, point.y);
    }

    @Override
    public boolean contains (float px, float py) {
        // normalize point
        float nx = (px - x) / width - 0.5f;
        float ny = (py - y) / height - 0.5f;
        if ((nx * nx + ny * ny) > 0.25) {
            return false;
        }

        float absExtent = Math.abs(extent);
        if (absExtent >= 360f) {
            return true;
        }

        boolean containsAngle = containsAngle(Mathf.toDegrees(-Mathf.atan2(ny, nx)));
        if (type == Type.PIE) {
            return containsAngle;
        }
        if (absExtent <= 180f && !containsAngle) {
            return false;
        }

        Line l = new Line(startPoint(), endPoint());
        int ccw1 = l.relativeCCW(px, py);
        int ccw2 = l.relativeCCW((x + width) / 2f, (y + height) / 2f);
        return ccw1 == 0 || ccw2 == 0 || ((ccw1 + ccw2) == 0 ^ absExtent > 180f);
    }

    @Override
    public boolean contains (float rx, float ry, float rw, float rh) {
        if (isEmpty() || rw <= 0f || rh <= 0f) return false;
        if (!(contains(rx, ry) && contains(rx + rw, ry) &&
                contains(rx + rw, ry + rh) && contains(rx, ry + rh))) {
            return false;
        }

        float absExtent = Math.abs(extent);
        if (type != Type.PIE || absExtent <= 180f || absExtent >= 360f) {
            return true;
        }

        Rectangle r = new Rectangle(rx, ry, rw, rh);
        float cx = (x + width) / 2f, cy = (y + height) / 2f;
        if (r.contains(cx, cy)) {
            return false;
        }

        Vector2 p1 = startPoint(), p2 = endPoint();
        return !r.intersectsLine(cx, cy, p1.x, p1.y) && !r.intersectsLine(cx, cy, p2.x, p2.y);
    }

    @Override
    public boolean contains (Rectangle rectangle) {
        return contains(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public boolean intersects (float rx, float ry, float rw, float rh) {
        if (isEmpty() || rw <= 0f || rh <= 0f) return false;

        // check: does arc contain rectangle's points
        if (contains(rx, ry) || contains(rx + rw, ry) ||
                contains(rx, ry + rh) || contains(rx + rw, ry + rh)) {
            return true;
        }

        float cx = (x + width) / 2f, cy = (y + height) / 2f;
        Vector2 p1 = startPoint(), p2 = endPoint();

        // check: does rectangle contain arc's points
        Rectangle r = new Rectangle(rx, ry, rw, rh);
        if (r.contains(p1) || r.contains(p2) || (type == Type.PIE && r.contains(cx, cy))) {
            return true;
        }

        if (type == Type.PIE) {
            if (r.intersectsLine(p1.x, p1.y, cx, cy) || r.intersectsLine(p2.x, p2.y, cx, cy)) {
                return true;
            }
        } else {
            if (r.intersectsLine(p1.x, p1.y, p2.x, p2.y)) {
                return true;
            }
        }

        // nearest rectangle point
        float nx = cx < rx ? rx : (cx > rx + rw ? rx + rw : cx);
        float ny = cy < ry ? ry : (cy > ry + rh ? ry + rh : cy);
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
            Arc arc = (Arc)obj;
            return x == arc.x && y == arc.y && width == arc.width && height == arc.height && start == arc.start &&
                    extent == arc.extent;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Float.hashCode(x) ^ Float.hashCode(y) ^ Float.hashCode(width) ^ Float.hashCode(height) ^
                Float.hashCode(start) ^ Float.hashCode(extent);
    }

    @Override
    public String toString() {
        return "Arc[x:" + x + ", y:" + y + ", width:" + width + ", height:" + height +
                ", start:" + start + ", extent:" + extent + "]";
    }

    /** Returns a normalized angle (bound between 0 and 360 degrees). */
    protected float normAngle (float angle) {
        return angle - Mathf.floor(angle / 360f) * 360f;
    }

    /** An iterator over an {@link Arc}. */
    protected static class Iterator implements PathIterator {
        /** The x coordinate of left-upper corner of the arc rectangle bounds */
        private float x;

        /** The y coordinate of left-upper corner of the arc rectangle bounds */
        private float y;

        /** The width of the arc rectangle bounds */
        private float width;

        /** The height of the arc rectangle bounds */
        private float height;

        /** The start angle of the arc in degrees */
        private float angle;

        /** The angle extent in degrees */
        private float extent;

        /** The closure type of the arc */
        private Type type;

        /** The path iterator transformation */
        private Affine t;

        /** The current segment index */
        private int index;

        /** The number of arc segments the source arc subdivided to be approximated by Bezier
         * curves. Depends on extent value. */
        private int arcCount;

        /** The number of line segments. Depends on closure type. */
        private int lineCount;

        /** The step to calculate next arc subdivision point */
        private float step;

        /** The temporary value of cosinus of the current angle */
        private float cos;

        /** The temporary value of sinus of the current angle */
        private float sin;

        /** The coefficient to calculate control points of Bezier curves */
        private float k;

        /** The temporary value of x coordinate of the Bezier curve control point */
        private float kx;

        /** The temporary value of y coordinate of the Bezier curve control point */
        private float ky;

        /** The x coordinate of the first path point (MOVE_TO) */
        private float mx;

        /** The y coordinate of the first path point (MOVE_TO) */
        private float my;

        Iterator (Arc a, Affine t) {
            this.width = a.width / 2f;
            this.height = a.height / 2f;
            this.x = a.x + width;
            this.y = a.y + height;
            this.angle = -Mathf.toRadians(a.start);
            this.extent = -a.extent;
            this.type = a.type;
            this.t = t;

            if (width < 0 || height < 0) {
                arcCount = 0;
                lineCount = 0;
                index = 1;
                return;
            }

            if (Math.abs(extent) >= 360f) {
                arcCount = 4;
                k = 4f / 3f * (Mathf.sqrt(2f) - 1f);
                step = Mathf.PI / 2f;
                if (extent < 0f) {
                    step = -step;
                    k = -k;
                }
            } else {
                arcCount = Mathf.iceil(Math.abs(extent) / 90f);
                step = Mathf.toRadians(extent / arcCount);
                k = 4f / 3f * (1f - Mathf.cos(step / 2f)) / Mathf.sin(step / 2f);
            }

            lineCount = 0;
            if (type == Type.CHORD) {
                lineCount++;
            } else if (type == Type.PIE) {
                lineCount += 2;
            }
        }

        @Override public int windingRule () {
            return WIND_NON_ZERO;
        }

        @Override public boolean isDone () {
            return index > arcCount + lineCount;
        }

        @Override public void next () {
            index++;
        }

        @Override public int currentSegment (float[] coords) {
            if (isDone()) {
                throw new NoSuchElementException("Iterator out of bounds");
            }
            int type;
            int count;
            if (index == 0) {
                type = SEG_MOVETO;
                count = 1;
                cos = Mathf.cos(angle);
                sin = Mathf.sin(angle);
                kx = k * width * sin;
                ky = k * height * cos;
                coords[0] = mx = x + cos * width;
                coords[1] = my = y + sin * height;
            } else if (index <= arcCount) {
                type = SEG_CUBICTO;
                count = 3;
                coords[0] = mx - kx;
                coords[1] = my + ky;
                angle += step;
                cos = Mathf.cos(angle);
                sin = Mathf.sin(angle);
                kx = k * width * sin;
                ky = k * height * cos;
                coords[4] = mx = x + cos * width;
                coords[5] = my = y + sin * height;
                coords[2] = mx + kx;
                coords[3] = my - ky;
            } else if (index == arcCount + lineCount) {
                type = SEG_CLOSE;
                count = 0;
            } else {
                type = SEG_LINETO;
                count = 1;
                coords[0] = x;
                coords[1] = y;
            }
            if (t != null) {
                t.transform(coords, 0, coords, 0, count);
            }
            return type;
        }
    }

}
