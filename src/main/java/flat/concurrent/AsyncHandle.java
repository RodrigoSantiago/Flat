package flat.concurrent;

public interface AsyncHandle<T> {
    void handle(T result);
}
