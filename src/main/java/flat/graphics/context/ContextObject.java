package flat.graphics.context;

import java.lang.ref.Cleaner;

abstract class ContextObject {
    protected final static Cleaner cleaner = Cleaner.create();

    private final Context context;
    private boolean disposed;

    public ContextObject(Context context) {
        this.context = context;
        this.context.checkDisposed();
    }

    public Context getContext() {
        return context;
    }

    protected final void assignDispose(Runnable task) {
        cleaner.register(this, context.createSyncDestroyTask(task));
    }

    protected boolean isBound() {
        return true;
    }

    public boolean isDisposed() {
        return disposed;
    }

    void dispose() {
        this.disposed = true;
    }
}
