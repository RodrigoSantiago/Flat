package flat.graphics.image;

import flat.graphics.SmartContext;
import flat.graphics.context.Context;
import flat.graphics.context.Texture2D;
import flat.graphics.context.enums.MagFilter;
import flat.graphics.context.enums.MinFilter;
import flat.graphics.context.enums.PixelFormat;

public class PixelMap implements Drawable {

    private Texture2D texture;
    private int width, height;
    private int[] data;

    PixelMap(int[] data, int width, int height) {
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public Texture2D readTexture(Context context) {
        if (texture == null) {
            texture = new Texture2D(context);
            texture.begin(0);
            texture.setSize(width, height, PixelFormat.RGBA);
            texture.setData(0, data, 0, 0, 0, width, height);
            texture.setLevels(0);
            texture.generateMipmapLevels();
            texture.setScaleFilters(MagFilter.NEAREST, MinFilter.NEAREST);
            texture.end();
            data = null;
        }
        return texture;
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
    public void draw(SmartContext context, float x, float y, float frame) {
        draw(context, x, y, getWidth(), getHeight(), frame);
    }
}
