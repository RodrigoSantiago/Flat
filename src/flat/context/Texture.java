package flat.context;

import flat.image.ImageFormat;

public class Texture {
    private int width, height, page;
    private ImageFormat format;

    public Texture() {

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPageCount() {
        return page;
    }

    public ImageFormat getFormat() {
        return format;
    }

}
