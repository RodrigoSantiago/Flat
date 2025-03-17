package flat.graphics.image;

import flat.backend.SVG;
import flat.graphics.Graphics;
import flat.graphics.ImageTexture;
import flat.graphics.context.Context;
import flat.graphics.context.Texture2D;
import flat.graphics.context.enums.*;
import flat.widget.enums.ImageFilter;

import java.util.HashMap;

public class PixelMap implements Drawable, ImageTexture {

    private HashMap<Context, Texture2D> textures = new HashMap<>();
    private PixelFormat format;
    private int width, height;
    private byte[] data;

    public PixelMap(byte[] data, int width, int height, PixelFormat format) {
        this.width = width;
        this.height = height;
        this.data = data;
        this.format = format;

        int required = width * height * format.getPixelBytes();
        if (data.length < required) {
            throw new RuntimeException("The image data is too short. Provided : " + data.length + ", Required : " + required);
        }
    }

    public byte[] export(ImageFileFormat imageFileFormat) {
        return export(imageFileFormat, 100);
    }

    public byte[] export(ImageFileFormat imageFileFormat, int quality) {
        quality = Math.max(100, Math.max(0, quality));
        return SVG.WriteImage(data, width, height, format.getPixelBytes(), imageFileFormat.ordinal(), quality);
    }

    public byte[] getData() {
        return data;
    }

    public Texture2D getTexture(Context context) {
        return getTexture(context, null);
    }

    public Texture2D getTexture(Context context, ImageFilter filter) {
        var texture = textures.get(context);
        if (texture == null) {
            texture = new Texture2D(context);
            texture.begin(0);
            texture.setSize(width, height, format);
            texture.setData(0, data, 0, 0, 0, width, height);
            texture.setLevels(0);
            texture.generateMipmapLevels();
            texture.setScaleFilters(MagFilter.NEAREST, MinFilter.NEAREST);
            texture.setWrapModes(WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE);
            texture.end();
            textures.put(context, texture);
        }
        for (var ctx : textures.keySet()) {
            if (ctx.isDisposed()) {
                textures.remove(ctx);
                break;
            }
        }
        if (filter != null) {
            if ((texture.getMagFilter() == MagFilter.NEAREST) != (filter == ImageFilter.NEAREST)) {
                texture.begin(0);
                texture.setScaleFilters(
                        filter == ImageFilter.LINEAR ? MagFilter.LINEAR : MagFilter.NEAREST,
                        filter == ImageFilter.LINEAR ? MinFilter.LINEAR : MinFilter.NEAREST);
                texture.end();
            }
        }
        return texture;
    }

    public PixelFormat getFormat() {
        return format;
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
    public void draw(Graphics context, float x, float y, float width, float height, int color, ImageFilter filter) {
        var texture = getTexture(context.getContext(), filter);
        context.drawImage(this, x, y, width, height, color);
    }

    @Override
    public void draw(Graphics context, float x, float y, float frame, ImageFilter filter) {
        draw(context, x, y, getWidth(), getHeight(), 0xFFFFFFFF, filter);
    }
}
