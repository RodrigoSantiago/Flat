package flat.objects;

import flat.screen.Context;

public abstract class ContextObject {
    public final Context context;
    private boolean dispose;

    public ContextObject(Context context) {
        this.context = context;
        if (context == null || !context.isCurrent()) {
            throw new IllegalArgumentException("Invalid context");
        }
    }

    public void dispose() {
        dispose = true;
        context.disposeObject(this);
    }

    public boolean isDosposed() {
        return dispose;
    }

    @Override
    protected void finalize() throws Throwable {
        if (!isDosposed()) {
            context.disposeObject(this);
        }
        super.finalize();
    }
}
