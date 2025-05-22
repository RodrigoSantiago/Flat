package flat.graphics.image;

import flat.graphics.context.enums.PixelFormat;

public class ImageData {
    private final PixelFormat format;
    private final int width, height;
    private final byte[] data;

    public ImageData(byte[] data, int width, int height, PixelFormat format) {
        this.format = format;
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public PixelFormat getFormat() {
        return format;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getData() {
        return data;
    }
}
