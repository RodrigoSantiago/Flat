package flat.math;

/**
 * Provides read-only access to a {@link Dimension}.
 */
public interface IDimension extends Cloneable {

    /**
     * Returns the magnitude in the x-dimension.
     */
    float width ();

    /**
     * Returns the magnitude in the y-dimension.
     */
    float height ();

    /**
     * Returns a mutable copy of this dimension.
     */
    Dimension clone ();
}
