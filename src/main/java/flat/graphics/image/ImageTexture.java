package flat.graphics.image;

import flat.exception.FlatException;
import flat.graphics.Graphics;
import flat.graphics.RenderTexture;
import flat.graphics.context.Texture2D;
import flat.graphics.context.enums.*;
import flat.resources.ResourceStream;
import flat.widget.enums.ImageFilter;

import java.lang.ref.WeakReference;

public class ImageTexture implements Drawable, RenderTexture {

    public static ImageTexture parse(byte[] data) {
        return new ImageTexture(ImageData.parse(data));
    }

    public static ImageTexture parse(ResourceStream stream) {
        Object cache = stream.getCache();
        if (cache != null) {
            if (cache instanceof Exception) {
                return null;
            } else if (cache instanceof Object[] dual) {
                return (ImageTexture) dual[1];
            } else {
                stream.clearCache();
            }
        }
        try {
            ImageData data = ImageData.load(stream);
            ImageTexture texture = new ImageTexture(data);
            
            stream.putCache(new Object[]{data, texture});
            return texture;
        } catch (Exception e) {
            stream.putCache(e);
            throw new FlatException(e);
        }
    }

    private final PixelFormat format;
    private final int width, height;
    private byte[] initialData;
    private Texture2D texture;
    private WeakReference<byte[]> localData;

    public ImageTexture(Texture2D texture) {
        this.texture = texture;
        this.format = texture.getFormat();
        this.width = texture.getWidth(0);
        this.height = texture.getHeight(0);
        texture.setScaleFilters(MagFilter.LINEAR, MinFilter.LINEAR);
        texture.setWrapModes(WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE);
    }
    
    public ImageTexture(ImageData data) {
        this(data.getData(), data.getWidth(), data.getHeight(), data.getFormat());
    }

    public ImageTexture(byte[] data, int width, int height, PixelFormat format) {
        int required = width * height * format.getPixelBytes();
        if (data.length < required) {
            throw new FlatException("The image data is too short. Provided : " + data.length + ", Required : " + required);
        }

        this.width = width;
        this.height = height;
        this.format = format;
        this.localData = new WeakReference<>(data);
        this.initialData = data;
    }

    public ImageData readImageData() {
        byte[] data = localData == null ? null : localData.get();
        if (data == null) {
            data = new byte[width * height * format.getPixelBytes()];
            texture.getData(0, data, 0);
        } else {
            data = data.clone();
        }
        return new ImageData(data, width, height, format);
    }
    
    @Override
    public Texture2D getTexture() {
        if (texture == null) {
            texture = new Texture2D(width, height, format);
            texture.setData(0, initialData, 0, 0, 0, width, height);
            texture.setLevels(0);
            texture.generateMipmapLevels();
            texture.setScaleFilters(MagFilter.LINEAR, MinFilter.LINEAR);
            texture.setWrapModes(WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE);
            initialData = null;
        }
        return texture;
    }

    public PixelFormat getFormat() {
        return format;
    }

    public void invalidateTextureData() {
        localData = null;
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
