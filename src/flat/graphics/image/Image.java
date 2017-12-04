package flat.graphics.image;

public class Image {
    private int width, height;

    private int texId;

    public Image(int width, int height) {

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;

        // todo - GL-TEXIMAGE
    }

    public void setPixels() {

    }

    public void getPixels() {

    }
}
