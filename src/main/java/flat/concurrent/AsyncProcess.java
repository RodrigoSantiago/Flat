package flat.concurrent;

public interface AsyncProcess<T> {
    T run(ProgressReport report) throws Exception;
}
