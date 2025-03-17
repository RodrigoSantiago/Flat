package flat.graphics.context;

public abstract class Texture extends ContextObject {

    Texture(Context context) {
        super(context);
    }

    abstract int getInternalID();

    abstract int getInternalType();
}
