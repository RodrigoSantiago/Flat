package flat.graphics.context;

import java.util.concurrent.atomic.AtomicLong;

public abstract class ContextObject {

    private static AtomicLong id = new AtomicLong();
    private final long unicID = id.getAndIncrement();

    private boolean initialized;

    protected ContextObject() {
    }

    public long getUnicID() {
        return unicID;
    }

    public final void init() {
        if (!initialized) {
            initialized = true;
            Context.assign(this);
            onInitialize();
        }
    }

    public final void dispose() {
        if (initialized) {
            initialized = false;
            Context.deassign(this);
            onDispose();
        }
    }

    public final boolean isInitialized() {
        return this.initialized;
    }

    protected abstract void onInitialize();

    protected abstract void onDispose();

    @Override
    protected void finalize() throws Throwable {
        dispose();
    }
}
