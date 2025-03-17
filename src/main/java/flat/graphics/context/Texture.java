package flat.graphics.context;

public abstract class Texture extends ContextObject {

    protected int activePos;

    Texture(Context context) {
        super(context);
    }

    @Override
    protected boolean isBound() {
        return getContext().indexOfTextureBound(this) != -1;
    }

    public void begin(int index) {
        getContext().bindTexture(this, index);
    }

    public void end() {
        getContext().unbindTexture(activePos);
    }

    void setActivePos(int activePos) {
        this.activePos = activePos;
    }

    abstract int getInternalID();

    abstract int getInternalType();
}
