package flat.graphics.context.objects;

import flat.graphics.context.ContextObject;
import flat.graphics.context.objects.textures.Layer;

public class Frame extends ContextObject {

    private Layer[] layers = new Layer[8];
    private int internalID;

    public Frame() {

    }

    public void begin(boolean draw, boolean read) {

    }

    public void begin() {

    }

    public void end() {

    }

    public void setSize(int width, int height, boolean stencil, boolean depth) {

    }

    public void attachTexture(Layer... layer) {

    }

    public void detachTexture(Layer... layer) {

    }

    public void setTargets(int c0, int c1, int c2, int c3, int c4, int c5, int c6, int c7) {

    }

    public int getInternalID() {
        return 0;
    }

    @Override
    protected void onDispose() {

    }
}
