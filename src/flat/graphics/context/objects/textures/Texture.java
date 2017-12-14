package flat.graphics.context.objects.textures;

import flat.graphics.context.ContextObject;

public abstract class Texture extends ContextObject {

    private int internalEnum;

    public void bind(int index) {

    }

    public int getInternalID() {
        return 0;
    }

    public int getInternalType() {
        return internalEnum;
    }
}
