package flat.uxml;

public interface UXValueListener<T> {
    void handle(ValueChange<T> event);
}
