package flat.graphics.context;

import java.lang.ref.Cleaner;

abstract class ContextObject {
    protected final static Cleaner cleaner = Cleaner.create();

    private final Context context;
    private boolean disposed;
    private Runnable disposeTask;

    public ContextObject(Context context) {
        this.context = context;
        this.context.checkDisposed();
    }

    public Context getContext() {
        return context;
    }

    protected final void assignDispose(Runnable task) {
        cleaner.register(this, disposeTask = context.createSyncDestroyTask(task));
    }

    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        if (!disposed) {
            disposed = true;
            disposeTask.run();
        }
    }
}
