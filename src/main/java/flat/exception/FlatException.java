package flat.exception;

public class FlatException extends RuntimeException {
    public FlatException(String message) {
        super(message);
    }

    public FlatException(Throwable cause) {
        super(cause);
    }
}
