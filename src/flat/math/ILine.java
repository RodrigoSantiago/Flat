package flat.math;

/**
 * Provides read-only access to a {@link Line}.
 */
public interface ILine extends IShape, Cloneable {

    /** Returns the x-coordinate of the start of this line. */
    float x1 ();

    /** Returns the y-coordinate of the start of this line. */
    float y1 ();

    /** Returns the x-coordinate of the end of this line. */
    float x2 ();

    /** Returns the y-coordinate of the end of this line. */
    float y2 ();

    /** Returns a copy of the starting point of this line. */
    Point p1 ();

    /** Initializes the supplied point with this line's starting point.
     * @return the supplied point. */
    Point p1 (Point target);

    /** Returns a copy of the ending point of this line. */
    Point p2 ();

    /** Initializes the supplied point with this line's ending point.
     * @return the supplied point. */
    Point p2 (Point target);

    /** Returns the square of the distance from the specified point to the line defined by this
     * line segment. */
    float pointLineDistSq (float px, float py);

    /** Returns the square of the distance from the supplied point to the line defined by this line
     * segment. */
    float pointLineDistSq (XY p);

    /** Returns the distance from the specified point to the line defined by this line segment. */
    float pointLineDist (float px, float py);

    /** Returns the distance from the supplied point to the line defined by this line segment. */
    float pointLineDist (XY p);

    /** Returns the square of the distance from the specified point this line segment. */
    float pointSegDistSq (float px, float py);

    /** Returns the square of the distance from the supplied point this line segment. */
    float pointSegDistSq (XY p);

    /** Returns the distance from the specified point this line segment. */
    float pointSegDist (float px, float py);

    /** Returns the distance from the supplied point this line segment. */
    float pointSegDist (XY p);

    /** Returns an indicator of where the specified point (px,py) lies with respect to this line
     * segment. */
    int relativeCCW (float px, float py);

    /** Returns an indicator of where the specified point lies with respect to this line segment. */
    int relativeCCW (XY p);

    /** Returns a mutable copy of this line. */
    Line clone ();
}
