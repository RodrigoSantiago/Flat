package flat.math.shapes;

import flat.math.Mathf;
import flat.math.Vector2;

/**
 * Rectangle-related utility methods.
 */
public final class Rectangles {

    /**
     * Intersects the supplied two rectangles, writing the result into {@code dst}.
     */
    public static void intersect (Rectangle src1, Rectangle src2, Rectangle dst) {
        float x1 = Math.max(src1.x, src2.x);
        float y1 = Math.max(src1.y, src2.y);
        float x2 = Math.min(src1.x + src1.width, src2.x + src2.width);
        float y2 = Math.min(src1.y + src1.height, src2.y + src2.height);
        dst.set(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * Unions the supplied two rectangles, writing the result into {@code dst}.
     */
    public static void union (Rectangle src1, Rectangle src2, Rectangle dst) {
        float x1 = Math.min(src1.x, src2.x);
        float y1 = Math.min(src1.y, src2.y);
        float x2 = Math.max(src1.x + src1.width, src2.x + src2.width);
        float y2 = Math.max(src1.y + src1.height, src2.y + src2.height);
        dst.set(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * Computes the point inside the bounds of the rectangle that's closest to the given point,
     * writing the result into {@code out}.
     * @return {@code out} for call chaining convenience.
     */
    public static Vector2 closestInteriorPoint (Rectangle r, Vector2 p, Vector2 out) {
        out.set(Mathf.clamp(p.x, r.x, r.x + r.width),
                Mathf.clamp(p.y, r.y, r.y + r.height));
        return out;
    }

    /**
     * Computes and returns the point inside the bounds of the rectangle that's closest to the
     * given point.
     */
    public static Vector2 closestInteriorPoint (Rectangle r, Vector2 p)  {
        return closestInteriorPoint(r, p, new Vector2());
    }

    /**
     * Returns the squared Euclidean distance between the given point and the nearest point inside
     * the bounds of the given rectangle. If the supplied point is inside the rectangle, the
     * distance will be zero.
     */
    public static float pointRectDistanceSq (Rectangle r, Vector2 p) {
        Vector2 p2 = closestInteriorPoint(r, p);
        return Vector2.distanceSqr(p.x, p.y, p2.x, p2.y);
    }

    /**
     * Returns the Euclidean distance between the given point and the nearest point inside the
     * bounds of the given rectangle. If the supplied point is inside the rectangle, the distance
     * will be zero.
     */
    public static float pointRectDistance (Rectangle r, Vector2 p) {
        return Mathf.sqrt(pointRectDistanceSq(r, p));
    }
}
