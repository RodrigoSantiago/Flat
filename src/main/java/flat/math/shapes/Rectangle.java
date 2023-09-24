package flat.math.shapes;

import flat.math.Affine;
import flat.math.Vector2;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Represents an area in two dimensions.
 */
public final class Rectangle implements Shape, Serializable {

    private static final long serialVersionUID = -803067517133475390L;

    /** The bitmask that indicates that a point lies to the left of this rectangle. See {@link #outcode}. */
    public static final int OUT_LEFT = 1;

    /** The bitmask that indicates that a point lies above this rectangle. See {@link #outcode}. */
    public static final int OUT_TOP = 2;

    /** The bitmask that indicates that a point lies to the right of this rectangle. See {@link #outcode}. */
    public static final int OUT_RIGHT = 4;

    /** The bitmask that indicates that a point lies below this rectangle. See {@link #outcode}. */
    public static final int OUT_BOTTOM = 8;

    /** The bounding values of this rectangle. */
    public float x, y, width, height;
    
    /**
     * Constructs a rectangle at (0,0) and with dimensions (0,0).
     */
    public Rectangle () {
    }

    /**
     * Constructs a rectangle with the specified upper-left corner and dimensions.
     */
    public Rectangle (float x, float y, float width, float height) {
        set(x, y, width, height);
    }

    /**
     * Constructs a rectangle between two points
     */
    public Rectangle (Vector2 pointA, Vector2 pointB) {
        set(Math.min(pointA.x, pointB.x), Math.min(pointA.y, pointB.y),
                Math.abs(pointB.x - pointA.x), Math.abs(pointB.y - pointA.y));
    }

    /**
     * Constructs a rectangle with bounds equal to the supplied rectangle.
     */
    public Rectangle (Rectangle rectangle) {
        set(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    /**
     * Sets the size of this rectangle to the specified dimensions.
     */
    public void setSize (float width, float height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Sets the bounds of this rectangle to the specified bounds.
     */
    public void set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
    }

    /**
     * Sets the bounds of this rectangle to those of the supplied rectangle.
     */
    public void set(Rectangle rectangle) {
        set(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    /**
     * Grows the bounds of this rectangle by the specified amount (i.e. the upper-left corner moves
     * by the specified amount in the negative x and y direction and the width and height grow by
     * twice the specified amount).
     */
    public void grow (float dx, float dy) {
        x -= dx;
        y -= dy;
        width += dx + dx;
        height += dy + dy;
    }

    /**
     * Expands the bounds of this rectangle to contain the specified point.
     */
    public void add (float px, float py) {
        float x1 = Math.min(x, px);
        float x2 = Math.max(x + width, px);
        float y1 = Math.min(y, py);
        float y2 = Math.max(y + height, py);
        set(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * Expands the bounds of this rectangle to contain the supplied point.
     */
    public void add (Vector2 p) {
        add(p.x, p.y);
    }

    /**
     * Expands the bounds of this rectangle to contain the supplied rectangle.
     */
    public void add (float rx, float ry, float rw, float rh) {
        float x1 = Math.min(x, rx);
        float x2 = Math.max(x + width, rx + rw);
        float y1 = Math.min(y, ry);
        float y2 = Math.max(y + height, ry + rh);
        set(x1, y1, x2 - x1, y2 - y1);
    }
    /**
     * Expands the bounds of this rectangle to contain the supplied rectangle.
     */
    public void add (Rectangle r) {
        add(r.x, r.y, r.width, r.height);
    }

    /** Returns a copy of this rectangle's upper-left corner. */
    public Vector2 location () {
        return location(new Vector2());
    }

    /** Initializes the supplied point with this rectangle's upper-left corner.
     * @return the supplied point. */
    public Vector2 location (Vector2 target) {
        return target.set(x, y);
    }

    /** Set as the intersection of the specified rectangle and this rectangle (i.e. the largest
     * rectangle contained in both this and the specified rectangle). */
    public void intersect (float rx, float ry, float rw, float rh) {
        float x1 = Math.max(x, rx);
        float y1 = Math.max(y, ry);
        float x2 = Math.min(x + width, rx + rw);
        float y2 = Math.min(y + height, ry + rh);
        set(x1, y1, x2 - x1, y2 - y1);
    }

    /** Set as the intersection of the supplied rectangle and this rectangle (i.e. the largest
     * rectangle contained in both this and the supplied rectangle). */
    public void intersect (Rectangle r) {
        intersect(r.x, r.y, r.width, r.height);
    }

    /** Returns the union of the supplied rectangle and this rectangle (i.e. the smallest rectangle
     * that contains both this and the supplied rectangle). */
    public Rectangle union (Rectangle r) {
        Rectangle rect = new Rectangle(this);
        rect.add(r);
        return rect;
    }

    /** Returns true if the specified line segment intersects this rectangle. */
    public boolean intersectsLine (float x1, float y1, float x2, float y2) {
        return Lines.lineIntersectsRect(x1, y1, x2, y2, x, y, width, height);
    }

    /** Returns true if the supplied line segment intersects this rectangle. */
    public boolean intersectsLine (Line l) {
        return intersectsLine(l.x1, l.y1, l.x2, l.y2);
    }

    /** Returns a set of flags indicating where the specified point lies in relation to the bounds
     * of this rectangle. See {@link #OUT_LEFT}, etc. */
    public int outcode (float px, float py) {
        int code = 0;

        if (width <= 0) {
            code |= OUT_LEFT | OUT_RIGHT;
        } else if (px < x) {
            code |= OUT_LEFT;
        } else if (px > x + width) {
            code |= OUT_RIGHT;
        }

        if (height <= 0) {
            code |= OUT_TOP | OUT_BOTTOM;
        } else if (py < y) {
            code |= OUT_TOP;
        } else if (py > y + height) {
            code |= OUT_BOTTOM;
        }

        return code;
    }

    /** Returns a set of flags indicating where the supplied point lies in relation to the bounds of
     * this rectangle.*/
    public int outcode (Vector2 point) {
        return outcode(point.x, point.y);
    }

    /** Returns a mutable copy of this rectangle. */
    public Rectangle clone () {
        return new Rectangle(this);
    }

    @Override
    public boolean isEmpty() {
        return width <= 0 || height <= 0;
    }

    @Override
    public Rectangle bounds() {
        return new Rectangle(this);
    }

    @Override
    public boolean contains (float px, float py) {
        if (isEmpty()) return false;

        if (px < x || py < y) return false;

        px -= x;
        py -= y;
        return px <= width && py <= height;
    }

    @Override
    public boolean contains (Vector2 point) {
        return contains(point.x, point.y);
    }

    @Override
    public boolean contains (float rx, float ry, float rw, float rh) {
        if (isEmpty() || rw <= 0f || rh <= 0f) return false;

        float x1 = x, y1 = y, x2 = x1 + width, y2 = y1 + height;
        return (x1 <= rx) && (rx + rw <= x2) && (y1 <= ry) && (ry + rh <= y2);
    }

    @Override
    public boolean contains (Rectangle rectangle) {
        return contains(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public boolean intersects (float rx, float ry, float rw, float rh) {
        if (isEmpty() || rw <= 0f || rh <= 0f) return false;

        float x1 = x, y1 = y, x2 = x1 + width, y2 = y1 + height;
        return (rx + rw > x1) && (rx < x2) && (ry + rh > y1) && (ry < y2);
    }

    @Override
    public boolean intersects (Rectangle rectangle) {
        return intersects(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public PathIterator pathIterator (Affine t) {
        return new Iterator(this, t);
    }

    @Override
    public PathIterator pathIterator (Affine t, float flatness) {
        return new Iterator(this, t);
    }

    @Override
    public boolean equals (Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() == getClass()) {
            Rectangle r = (Rectangle)obj;
            return r.x == x && r.y == y && r.width == width && r.height == height;
        }
        return false;
    }

    @Override
    public int hashCode () {
        return Float.hashCode(x) ^ Float.hashCode(y) ^ Float.hashCode(width) ^ Float.hashCode(height);
    }

    @Override
    public String toString () {
        return "Rectangle[x: " + x + ", y:" + y + ", width:" + width + ", height:" + height + "]";
    }

    /** An iterator over an {@link Rectangle}. */
    protected static class Iterator implements PathIterator {
        private float x, y, width, height;
        private Affine t;

        /** The current segment index. */
        private int index;

        Iterator (Rectangle rectangle, Affine at) {
            this.x = rectangle.x;
            this.y = rectangle.y;
            this.width = rectangle.width;
            this.height = rectangle.height;
            this.t = at;
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
            if (index == 0) {
                type = SEG_MOVETO;
                coords[0] = x;
                coords[1] = y;
            } else {
                type = SEG_LINETO;
                switch (index) {
                    case 1:
                        coords[0] = x;
                        coords[1] = y + height;
                        break;
                    case 2:
                        coords[0] = x + width;
                        coords[1] = y + height;
                        break;
                    case 3:
                        coords[0] = x + width;
                        coords[1] = y;
                        break;
                    case 4:
                        coords[0] = x;
                        coords[1] = y;
                        break;
                }
            }
            if (t != null) {
                t.transform(coords, 0, coords, 0, 1);
            }
            return type;
        }
    }
}
