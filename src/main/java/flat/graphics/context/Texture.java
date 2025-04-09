package flat.graphics.context;

import flat.exception.FlatException;
import flat.window.Application;

public abstract class Texture extends ContextObject {

    public Texture() {
    }

    abstract int getInternalId();

    abstract int getInternalType();

    protected void boundCheck() {
        if (isDisposed()) {
            throw new FlatException("The " + getClass().getSimpleName() + " is disposed");
        }
        Context context = Application.getCurrentContext();
        if (context == null) {
            throw new FlatException("The " + getClass().getSimpleName() + " should not be accessed outside of a assigned context");
        }
        Application.getCurrentContext().bindTexture(this);
    }
}
