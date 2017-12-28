package flat.math;

/**
 * Provides read-only access to an {@link Ellipse}.
 */
public interface IEllipse extends IRectangularShape, Cloneable {

    /** Returns a mutable copy of this ellipse. */
    Ellipse clone ();
}
