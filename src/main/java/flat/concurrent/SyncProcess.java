package flat.concurrent;

public interface SyncProcess {
    boolean hasNext();
    boolean execute();
}
