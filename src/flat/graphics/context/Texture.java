package flat.graphics.context;

import flat.screen.Application;

public abstract class Texture extends ContextObject {

    protected int activePos;

    Texture() {
    }

    public void begin(int index) {
        Application.getCurrentContext().bindTexture(this, index);
    }

    public void end() {
        Application.getCurrentContext().unbindTexture(activePos);
    }

    void setActivePos(int activePos) {
        this.activePos = activePos;
    }

    abstract int getInternalID();

    abstract int getInternalType();
}
