package flat.graphics.context;

import flat.widget.Application;

public abstract class Texture extends ContextObject {

    protected int activePos;

    Texture() {
    }

    public void begin(int index) {
        Application.getContext().bindTexture(this, index);
    }

    public void end() {
        Application.getContext().unbindTexture(activePos);
    }

    void setActivePos(int activePos) {
        this.activePos = activePos;
    }

    abstract int getInternalID();

    abstract int getInternalType();
}
