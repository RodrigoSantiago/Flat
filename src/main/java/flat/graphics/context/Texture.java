package flat.graphics.context;

public abstract class Texture extends ContextObject {

    public Texture(Context context) {
        super(context);
    }

    abstract int getInternalId();

    abstract int getInternalType();
}
