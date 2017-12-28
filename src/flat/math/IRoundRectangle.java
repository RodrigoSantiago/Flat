package flat.math;

/**
 * Provides read-only access to a {@link RoundRectangle}.
 */
public interface IRoundRectangle extends IRectangularShape, Cloneable {

    /** Returns the width of the corner arc. */
    float arcWidth ();

    /** Returns the height of the corner arc. */
    float arcHeight ();

    /** Returns a mutable copy of this round rectangle. */
    RoundRectangle clone ();
}
