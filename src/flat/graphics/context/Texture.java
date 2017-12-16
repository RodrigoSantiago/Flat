package flat.graphics.context;

public abstract class Texture extends ContextObject {

    protected int activePos;

    public Texture() {
        super();
    }

    public void begin(int index) {
        init();
        Context.getContext().bindTexture(this, index);
    }

    public void end() {
        Context.getContext().bindTexture(this, activePos);
    }

    void setActivePos(int activePos) {
        this.activePos = activePos;
    }

    abstract int getInternalID();

    abstract int getInternalType();
}
