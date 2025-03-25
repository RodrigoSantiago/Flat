package flat.graphics.context;

import flat.window.Application;

import java.lang.ref.Cleaner;

abstract class ContextObject {
    protected final static Cleaner cleaner = Cleaner.create();

    private final Context context;
    private boolean disposed;
    private Runnable disposeTask;

    ContextObject(Context context) {
        this.context = context;
        this.context.checkDisposed();
    }

    ContextObject() {
        this.context = null;
        if (Application.getCurrentContext() == null) {

        }
    }

    public Context getContext() {
        return context;
    }

    protected final void assignDispose(Runnable task) {
        if (context != null) {
            cleaner.register(this, disposeTask = context.createSyncDestroyTask(task));
        } else {
            cleaner.register(this, disposeTask = new DisposeTask(task));
        }
    }

    public boolean isDisposed() {
        return disposed || (context != null && context.isDisposed());
    }

    public void dispose() {
        if (!disposed) {
            disposed = true;
            if (!context.isDisposed() && disposeTask != null) disposeTask.run();
        }
    }
}
