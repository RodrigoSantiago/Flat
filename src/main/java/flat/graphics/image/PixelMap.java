package flat.graphics.image;

import flat.backend.SVG;
import flat.exception.FlatException;
import flat.graphics.Graphics;
import flat.graphics.ImageTexture;
import flat.graphics.context.Texture2D;
import flat.graphics.context.enums.*;
import flat.resources.ResourceStream;
import flat.widget.enums.ImageFilter;

import java.lang.ref.WeakReference;

public class PixelMap implements Drawable, ImageTexture {

    public static PixelMap parse(ResourceStream stream) {
        Object cache = stream.getCache();
        if (cache != null) {
            if (cache instanceof Exception) {
                return null;
            } else if (cache instanceof PixelMap) {
                return (PixelMap) cache;
            } else {
                stream.clearCache();
            }
        }
        try {
            PixelMap pixelMap = loadPixelMap(stream);
            stream.putCache(pixelMap);
            return pixelMap;
        } catch (Exception e) {
            stream.putCache(e);
            throw new FlatException(e);
        }
    }

    private static PixelMap loadPixelMap(ResourceStream stream) {
        byte[] data = stream.readData();
        if (data == null) {
            throw new FlatException("Invalid image " + stream.getResourceName());
        }

        int[] imageData = new int[3];
        byte[] readImage = SVG.ReadImage(data, imageData);
        if (readImage == null) {
            throw new FlatException("Invalid image format " + stream.getResourceName());
        }
        return new PixelMap(readImage, imageData[0], imageData[1], PixelFormat.RGBA);
    }

    private final Texture2D texture;
    private final PixelFormat format;
    private final int width, height;
    private WeakReference<byte[]> localData;

    public PixelMap(byte[] data, int width, int height, PixelFormat format) {
        int required = width * height * format.getPixelBytes();
        if (data.length < required) {
            throw new FlatException("The image data is too short. Provided : " + data.length + ", Required : " + required);
        }

        this.width = width;
        this.height = height;
        this.format = format;
        this.localData = new WeakReference<>(data);

        texture = new Texture2D(width, height, format);
        texture.setData(0, data, 0, 0, 0, width, height);
        texture.setLevels(0);
        texture.generateMipmapLevels();
        texture.setScaleFilters(MagFilter.LINEAR, MinFilter.LINEAR);
        texture.setWrapModes(WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE);
    }

    public byte[] export(ImageFileFormat imageFileFormat) {
        return export(imageFileFormat, 100);
    }

    public byte[] export(ImageFileFormat imageFileFormat, int quality) {
        quality = Math.max(100, Math.max(0, quality));
        return SVG.WriteImage(getData(), width, height, format.getPixelBytes(), imageFileFormat.ordinal(), quality);
    }

    public byte[] readData() {
        return getData().clone();
    }

    private byte[] getData() {
        byte[] data = localData.get();
        if (data == null) {
            data = new byte[width * height * format.getPixelBytes()];
            localData = new WeakReference<>(data);
            texture.getData(0, data, 0);
        }
        return data;
    }

    public Texture2D getTexture() {
        return texture;
    }

    public PixelFormat getFormat() {
        return format;
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
    public void draw(Graphics graphics, float x, float y, float width, float height, int color, ImageFilter filter) {
        if (graphics.discardDraw(x, y, width, height)) return;

        graphics.drawImage(this, x, y, width, height, color, filter != ImageFilter.LINEAR);
    }

    @Override
    public void draw(Graphics graphics, float x, float y, float frame, ImageFilter filter) {
        draw(graphics, x, y, getWidth(), getHeight(), 0xFFFFFFFF, filter);
    }
}
