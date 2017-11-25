package flat.objects;

import flat.objects.texture.Texture;
import flat.screen.Context;

public class ContextFrame extends ContextObject {

    public ContextFrame(Context context) {
        super(context);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public void setTexture(Texture texture) {

    }

    public Texture getTexture() {
        return null;
    }
}
