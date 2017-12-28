package flat.math;

/**
 * A ray consisting of an origin point and a unit direction vector.
 */
public class Ray2 implements IRay2 {

    /** The ray's point of origin. */
    public final Vector origin = new Vector();

    /** The ray's unit direction vector. */
    public final Vector direction = new Vector();

    /**
     * Creates a ray with the values contained in the supplied origin point and unit direction
     * vector.
     */
    public Ray2 (Vector origin, Vector direction) {
        set(origin, direction);
    }

    /**
     * Copy constructor.
     */
    public Ray2 (Ray2 other) {
        set(other);
    }

    /**
     * Creates an empty (invalid) ray.
     */
    public Ray2 () {
    }

    /**
     * Copies the parameters of another ray.
     *
     * @return a reference to this ray, for chaining.
     */
    public Ray2 set (IRay2 other) {
        return set(other.origin(), other.direction());
    }

    /**
     * Sets the ray parameters to the values contained in the supplied vectors.
     *
     * @return a reference to this ray, for chaining.
     */
    public Ray2 set (IVector origin, IVector direction) {
        this.origin.set(origin);
        this.direction.set(direction);
        return this;
    }

    /**
     * Transforms this ray in-place.
     *
     * @return a reference to this ray, for chaining.
     */
    public Ray2 transformLocal (Transform transform) {
        return transform(transform, this);
    }

    @Override // from IRay2
    public IVector origin () {
        return origin;
    }

    @Override // from IRay2
    public IVector direction () {
        return direction;
    }

    @Override // from IRay2
    public Ray2 transform (Transform transform) {
        return transform(transform, new Ray2());
    }

    @Override // from IRay2
    public Ray2 transform (Transform transform, Ray2 result) {
        transform.transformPoint(origin, result.origin);
        transform.transform(direction, result.direction).normalizeLocal();
        return result;
    }

    @Override // from IRay2
    public boolean intersects (IVector pt) {
        if (Math.abs(direction.x) > Math.abs(direction.y)) {
            float t = (pt.x() - origin.x) / direction.x;
            return t >= 0f && origin.y + t*direction.y == pt.y();
        } else {
            float t = (pt.y() - origin.y) / direction.y;
            return t >= 0f && origin.x + t*direction.x == pt.x();
        }
    }

    @Override // from IRay2
    public boolean getIntersection (IVector start, IVector end, Vector result) {
        // ray is a + t*b, segment is c + s*d
        float ax = origin.x, ay = origin.y;
        float bx = direction.x, by = direction.y;
        float cx = start.x(), cy = start.y();
        float dx = end.x() - start.x(), dy = end.y() - start.y();

        float divisor = bx*dy - by*dx;
        if (Math.abs(divisor) < MathUtil.EPSILON) {
            // the lines are parallel (or the segment is zero-length)
            float t = Math.min(getIntersection(start), getIntersection(end));
            boolean isect = (t != Float.MAX_VALUE);
            if (isect) {
                origin.addScaled(direction, t, result);
            }
            return isect;
        }
        float cxax = cx - ax, cyay = cy - ay;
        float s = (by*cxax - bx*cyay) / divisor;
        if (s < 0f || s > 1f) {
            return false;
        }
        float t = (dy*cxax - dx*cyay) / divisor;
        boolean isect = (t >= 0f);
        if (isect) {
            origin.addScaled(direction, t, result);
        }
        return isect;
    }

    @Override // from IRay2
    public boolean getIntersection (IVector start, IVector end, float radius, Vector result) {
        float startx = start.x(), starty = start.y();
        // compute the segment's line parameters
        float a = starty - end.y(), b = end.x() - startx;
        float len = FloatMath.hypot(a, b);
        if (len < MathUtil.EPSILON) { // start equals end; check as circle
            return getIntersection(start, radius, result);
        }
        float rlen = 1f / len;
        a *= rlen;
        b *= rlen;
        float c = -a*startx - b*starty;

        // find out where the origin lies with respect to the top and bottom
        float dist = a*origin.x + b*origin.y + c;
        boolean above = (dist > +radius), below = (dist < -radius);
        float x, y;
        if (above || below) { // check the intersection with the top/bottom boundary
            float divisor = a*direction.x + b*direction.y;
            if (Math.abs(divisor) < MathUtil.EPSILON) { // lines are parallel
                return false;
            }
            c += (above ? -radius : +radius);
            float t = (-a*origin.x - b*origin.y - c) / divisor;
            if (t < 0f) { // wrong direction
                return false;
            }
            x = origin.x + t*direction.x;
            y = origin.y + t*direction.y;

        } else { // middle; check the origin
            x = origin.x;
            y = origin.y;
        }
        // see where the test point lies with respect to the start and end boundaries
        float tmp = a;
        a = b;
        b = -tmp;
        c = -a*startx - b*starty;
        dist = a*x + b*y + c;
        if (dist < 0f) { // before start
            return getIntersection(start, radius, result);
        } else if (dist > len) { // after end
            return getIntersection(end, radius, result);
        } else { // middle
            result.set(x, y);
            return true;
        }
    }

    @Override // from IRay2
    public boolean getIntersection (IVector center, float radius, Vector result) {
        // see if we start inside the circle
        if (origin.distanceSq(center) <= radius*radius) {
            result.set(origin);
            return true;
        }
        // then if we intersect the circle
        float ax = origin.x - center.x(), ay = origin.y - center.y();
        float b = 2f*(direction.x*ax + direction.y*ay);
        float c = ax*ax + ay*ay - radius*radius;
        float radicand = b*b - 4f*c;
        if (radicand < 0f) {
            return false;
        }
        float t = (-b - FloatMath.sqrt(radicand)) * 0.5f;
        boolean isect = (t >= 0f);
        if (isect) {
            origin.addScaled(direction, t, result);
        }
        return isect;
    }

    @Override // from IRay2
    public Vector getNearestPoint (IVector point, Vector result) {
        if (result == null) {
            result = new Vector();
        }
        float r = point.subtract(origin).dot(direction);
        result.set(origin.add(direction.scale(r)));
        return result;
    }

    @Override
    public String toString () {
        return "[origin=" + origin + ", direction=" + direction + "]";
    }

    /**
     * Returns the parameter of the ray when it intersects the supplied point, or
     * {@link Float#MAX_VALUE} if there is no such intersection.
     */
    protected float getIntersection (IVector pt) {
        if (Math.abs(direction.x) > Math.abs(direction.y)) {
            float t = (pt.x() - origin.x) / direction.x;
            return (t >= 0f && origin.y + t*direction.y == pt.y()) ? t : Float.MAX_VALUE;
        } else {
            float t = (pt.y() - origin.y) / direction.y;
            return (t >= 0f && origin.x + t*direction.x == pt.x()) ? t : Float.MAX_VALUE;
        }
    }
}
