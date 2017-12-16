package flat.graphics.context;

import java.util.concurrent.atomic.AtomicLong;

public abstract class ContextObject {

    private static AtomicLong id = new AtomicLong();
    private final long unicID = id.getAndIncrement();

    private boolean initialized;
    private Runnable dispose;

    protected ContextObject() {
        Context.assign(this);
    }

    protected abstract void onInitialize();

    public void init() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    public long getUnicID() {
        return unicID;
    }

    protected void setDispose(Runnable dispose) {
        this.dispose = dispose;
    }

    public Runnable getDispose() {
        return dispose;
    }

    @Override
    public final void finalize() throws Throwable {
        Context.deassign(this);
    }
}
