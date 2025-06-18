package flat.graphics.image;

import flat.backend.SVG;
import flat.exception.FlatException;
import flat.graphics.context.enums.ImageFileFormat;
import flat.graphics.context.enums.PixelFormat;
import flat.resources.ResourceStream;

public class ImageData {
    public static ImageData parse(byte[] data) {
        if (data == null) {
            throw new FlatException("Invalid image data");
        }
        int[] imageData = new int[3];
        byte[] readImage = SVG.ReadImage(data, imageData);
        if (readImage == null) {
            throw new FlatException("Invalid image format");
        }
        return new ImageData(readImage, imageData[0], imageData[1], PixelFormat.RGBA);
    }
    
    public static ImageData parse(ResourceStream stream) {
        Object cache = stream.getCache();
        if (cache != null) {
            if (cache instanceof Exception) {
                return null;
            } else if (cache instanceof Object[] dual) {
                return (ImageData) dual[0];
            } else {
                stream.clearCache();
            }
        }
        try {
            ImageData data = ImageData.load(stream);
            ImageTexture texture = new ImageTexture(data);
            
            stream.putCache(new Object[]{data, texture});
            return data;
        } catch (Exception e) {
            stream.putCache(e);
            throw new FlatException(e);
        }
    }
    
    protected static ImageData load(ResourceStream stream) {
        byte[] data = stream.readData();
        if (data == null) {
            throw new FlatException("Invalid image " + stream.getResourceName());
        }
        
        try {
            return parse(data);
        } catch (FlatException e) {
            throw new FlatException("Invalid image format " + stream.getResourceName());
        }
    }
    
    private final PixelFormat format;
    private final int width, height;
    private final byte[] data;

    public ImageData(byte[] data, int width, int height, int channels) {
        this.format = PixelFormat.fromChannels(channels);
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public ImageData(byte[] data, int width, int height, PixelFormat format) {
        this.format = format;
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public PixelFormat getFormat() {
        return format;
    }

    public int getChannels() {
        return format.getPixelBytes();
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
    
    public byte[] export(ImageFileFormat imageFileFormat) {
        return export(imageFileFormat, 100);
    }
    
    public byte[] export(ImageFileFormat imageFileFormat, int quality) {
        quality = Math.max(100, Math.max(0, quality));
        return SVG.WriteImage(getData(), width, height, format.getPixelBytes(), imageFileFormat.ordinal(), quality);
    }
}
