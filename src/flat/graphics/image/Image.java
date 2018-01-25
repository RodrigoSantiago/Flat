package flat.graphics.image;

import flat.graphics.context.Texture2D;

public class Image {

    private Texture2D atlas;
    private int srcx, srcy, width, height;

    public Image(Texture2D atlas, int srcx, int srcy, int width, int height) {
        this.atlas = atlas;
        this.srcx = srcx;
        this.srcy = srcy;
        this.width = width;
        this.height = height;
    }

    public Texture2D getAtlas() {
        return atlas;
    }

    public void setAtlas(Texture2D atlas) {
        this.atlas = atlas;
    }

    public int getSrcx() {
        return srcx;
    }

    public void setSrcx(int srcx) {
        this.srcx = srcx;
    }

    public int getSrcy() {
        return srcy;
    }

    public void setSrcy(int srcy) {
        this.srcy = srcy;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void resize(Texture2D atlas, int srcx, int srcy, int width, int height) {
        this.atlas = atlas;
        this.srcx = srcx;
        this.srcy = srcy;
        this.width = width;
        this.height = height;
    }
}
