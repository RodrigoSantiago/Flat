package flat.data;

public interface ListChangeListener<T> {
    enum Operation {
        INSERT, DELETE, UPDATE, RANGE
    }
    void handle(int index, int length, Operation operation);
}
