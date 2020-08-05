package flat.math.shapes;

import flat.math.Affine;
import flat.math.Vector2;

/**
 * An interface provided by all shapes.
 */
public interface Shape {

    /** Returns true if this shape encloses no area. */
    boolean isEmpty ();

    /** Returns a bounding rectangle for this shape. */
    Rectangle bounds ();

    /** Returns true if this shape contains the specified point. */
    boolean contains (float x, float y);

    /** Returns true if this shape contains the specified point. */
    boolean contains (Vector2 point);

    /** Returns true if this shape completely contains the specified rectangle. */
    boolean contains (float x, float y, float width, float height);

    /** Returns true if this shape completely contains the specified rectangle. */
    boolean contains (Rectangle rectangle);

    /** Returns true if this shape intersects the specified rectangle. */
    boolean intersects (float x, float y, float width, float height);

    /** Returns true if this shape intersects the specified rectangle. */
    boolean intersects (Rectangle rectangle);

    /**
     * Returns an iterator over the path described by this shape.
     *
     * @param at if supplied, the points in the path are transformed using this.
     */
    PathIterator pathIterator (Affine at);

    /**
     * Returns an iterator over the path described by this shape.
     *
     * @param at if supplied, the points in the path are transformed using this.
     * @param flatness when approximating curved segments with lines, this controls the maximum
     *        distance the lines are allowed to deviate from the approximated curve, thus a higher
     *        flatness value generally allows for a path with fewer segments.
     */
    PathIterator pathIterator (Affine at, float flatness);

    Shape clone();
}
