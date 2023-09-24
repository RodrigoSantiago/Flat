package flat.math.util;

/**
 * An exception thrown by {@code Affine} when a request for an inverse transform cannot be
 * satisfied.
 */
public class NoninvertibleTransformException extends RuntimeException {
    private static final long serialVersionUID = 5208863644264280750L;

    public NoninvertibleTransformException (String s) {
        super(s);
    }
}
