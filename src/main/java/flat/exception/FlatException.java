package flat.exception;

public class FlatException extends RuntimeException {
    public FlatException(String message) {
        super(message);
    }

    public FlatException(Throwable cause) {
        super(cause);
    }

    public FlatException(String message, Throwable cause) {
        super(message, cause);
    }
}
