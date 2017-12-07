package flat.graphics;

import flat.Internal;

@Internal
public abstract class ContextObject {

    private boolean disposed;

    protected ContextObject() {
        Context.getContext().assignObject(this);
    }

    public final boolean isDisposed() {
        return disposed;
    }

    protected abstract void onDispose();

    public void dispose() {
        if (!disposed) {
            disposed = true;
            Context.getContext().releaseObject(this);
            onDispose();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        dispose();
        super.finalize();
    }
}
