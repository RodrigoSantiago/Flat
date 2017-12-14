package flat.graphics.context;

import flat.Internal;
import flat.graphics.context.Context;

@Internal
public abstract class ContextObject {

    private boolean disposed;

    protected ContextObject() {
        Context.getContext().assignObject(this);
    }

    public void bind() {

    }

    public boolean isDisposed() {
        return disposed;
    }

    protected abstract void onDispose();

    public final void dispose() {
        if (!disposed) {
            disposed = true;
            Context.getContext().releaseObject(this);
            onDispose();
        }
    }

    public final void finalize() throws Throwable {
        dispose();
        super.finalize();
    }
}
