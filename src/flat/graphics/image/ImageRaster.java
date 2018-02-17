package flat.graphics.image;

import flat.graphics.SmartContext;
import flat.graphics.context.Texture2D;

public class ImageRaster implements Image {

    private Texture2D atlas;
    private float srcx, srcy, width, height;

    public ImageRaster(Texture2D atlas, float srcx, float srcy, float width, float height) {
        this.atlas = atlas;
        this.srcx = srcx;
        this.srcy = srcy;
        this.width = width;
        this.height = height;
    }

    public Texture2D getAtlas() {
        return atlas;
    }

    public float getSrcx() {
        return srcx;
    }

    public float getSrcy() {
        return srcy;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public void draw(SmartContext context, float x, float y, float width, float height, float frame) {
        context.drawImage(this, null, x, y, width, height);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        TextureManager.disposeImage(this);
    }
}
