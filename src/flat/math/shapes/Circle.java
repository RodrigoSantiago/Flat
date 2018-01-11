package flat.math.shapes;

import flat.math.*;
import flat.math.util.FlatteningPathIterator;
import flat.math.util.Platform;

import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Represents a circle on a plane.
 */
public final class Circle implements Shape, Serializable {

    private static final long serialVersionUID = -4841212861047390886L;

    /**
     * The x-coordinate of the circle.
     */
    public float x;

    /**
     * The y-coordinate of the circle.
     */
    public float y;

    /**
     * The radius of the circle.
     */
    public float radius;

    /**
     * Constructs a circle at (0, 0) with radius 0
     */
    public Circle() {
    }

    /**
     * Constructs a circle with the specified properties
     */
    public Circle(float x, float y, float radius) {
        set(x, y, radius);
    }

    /**
     * Constructs a circle with properties equal to the supplied circle.
     */
    public Circle(Circle circle) {
        set(circle);
    }

    @Override
    public Circle clone() {
        return new Circle(this);
    }

    /**
     * Sets the properties of this circle to the supplied values.
     *
     * @return a reference to this this, for chaining.
     */
    public Circle set(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        return this;
    }

    /**
     * Sets the properties of this circle to be equal to those of the supplied circle.
     *
     * @return a reference to this this, for chaining.
     */
    public Circle set(Circle circle) {
        return set(circle.x, circle.y, circle.radius);
    }

    /**
     * Returns true if this circle intersects the supplied circle.
     */
    public boolean intersects(Circle circle) {
        float maxDist = radius + circle.radius;
        return Vector2.distanceSqr(x, y, circle.x, circle.y) < (maxDist * maxDist);
    }

    @Override
    public boolean isEmpty() {
        return radius <= 0;
    }

    @Override
    public Rectangle bounds() {
        return new Rectangle(x - radius, y - radius, radius * 2, radius * 2);
    }

    @Override
    public boolean contains(float x, float y) {
        return Vector2.distanceSqr(this.x, this.y, x, y) < radius * radius;
    }

    @Override
    public boolean contains(Vector2 point) {
        return contains(point.x, point.y);
    }

    @Override
    public boolean contains(float rx, float ry, float rw, float rh) {
        if (isEmpty() || rw <= 0f || rh <= 0f) return false;
        return contains(rx, ry) && contains(rx + rw, ry) && contains(rx + rw, ry + rh) && contains(rx, ry + rh);
    }

    @Override
    public boolean contains(Rectangle rectangle) {
        return contains(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public boolean intersects(float rx, float ry, float rw, float rh) {
        if (isEmpty() || rw <= 0f || rh <= 0f) return false;
        float rx2 = rx + rw, ry2 = ry + rh;
        float nx = x < rx ? rx : (x > rx2 ? rx2 : x);
        float ny = y < ry ? ry : (y > ry2 ? ry2 : y);
        return contains(nx, ny);
    }

    @Override
    public boolean intersects(Rectangle rectangle) {
        return intersects(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public PathIterator pathIterator(Affine at) {
        return new Iterator(this, at);
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
            Circle circle = (Circle) obj;
            return this.x == circle.x && this.y == circle.y && radius == circle.radius;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Platform.hashCode(x) ^ Platform.hashCode(y) ^ Platform.hashCode(radius);
    }

    @Override
    public String toString() {
        return "Circle[x:" + x + ", y:" + y + ", radius:" + radius + "]";
    }

    /**
     * Translates the circle by the specified offset.
     *
     * @return a reference to the result, for chaining.
     */
    public Circle offset(float x, float y) {
        return set(this.x + x, this.y + y, radius);
    }


    /**
     * An iterator over an {@link Ellipse}.
     */
    protected static class Iterator implements PathIterator {
        private final float x, y, diameter;
        private final Affine t;
        private int index;

        Iterator(Circle e, Affine t) {
            this.x = e.x - e.radius;
            this.y = e.y - e.radius;
            this.diameter = e.radius * 2;
            this.t = t;
            if (diameter <= 0f) {
                index = 6;
            }
        }

        @Override
        public int windingRule() {
            return WIND_NON_ZERO;
        }

        @Override
        public boolean isDone() {
            return index > 5;
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
            if (index == 5) {
                return SEG_CLOSE;
            }
            int type;
            int count;
            if (index == 0) {
                type = SEG_MOVETO;
                count = 1;
                float[] p = POINTS[3];
                coords[0] = x + p[4] * diameter;
                coords[1] = y + p[5] * diameter;
            } else {
                type = SEG_CUBICTO;
                count = 3;
                float[] p = POINTS[index - 1];
                int j = 0;
                for (int i = 0; i < 3; i++) {
                    coords[j] = x + p[j++] * diameter;
                    coords[j] = y + p[j++] * diameter;
                }
            }
            if (t != null) {
                t.transform(coords, 0, coords, 0, count);
            }
            return type;
        }
    }

    // An circle is subdivided into four quarters by x and y axis. Each part is approximated by a
    // cubic Bezier curve. The arc in the first quarter starts in (a, 0) and finishes in (0, b)
    // points. Control points for the cubic curve are (a, 0), (a, m), (n, b) and (0, b) where n and
    // m are calculated based on the requirement that the Bezier curve in point 0.5 should lay on
    // the arc.

    /**
     * The coefficient to calculate control points of Bezier curves.
     */
    private static final float U = 2f / 3f * (Mathf.sqrt(2) - 1f);

    /**
     * The points coordinates calculation table.
     */
    private static final float[][] POINTS = {
            {1f, 0.5f + U, 0.5f + U, 1f, 0.5f, 1f},
            {0.5f - U, 1f, 0f, 0.5f + U, 0f, 0.5f},
            {0f, 0.5f - U, 0.5f - U, 0f, 0.5f, 0f},
            {0.5f + U, 0f, 1f, 0.5f - U, 1f, 0.5f}};
}
