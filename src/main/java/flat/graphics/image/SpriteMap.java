package flat.graphics.image;

import flat.graphics.Graphics;
import flat.graphics.ImageTexture;
import flat.widget.enums.ImageFilter;

public class SpriteMap implements Drawable {
    private final ImageTexture texture;
    private final float srcX;
    private final float srcY;
    private final float srcWidth;
    private final float srcHeight;

    public SpriteMap(ImageTexture texture, float srcX, float srcY, float srcWidth, float srcHeight) {
        this.texture = texture;
        this.srcX = srcX;
        this.srcY = srcY;
        this.srcWidth = srcWidth;
        this.srcHeight = srcHeight;
    }

    public ImageTexture getImageTexture() {
        return texture;
    }

    public float getSrcX() {
        return srcX;
    }

    public float getSrcY() {
        return srcY;
    }

    public float getSrcWidth() {
        return srcWidth;
    }

    public float getSrcHeight() {
        return srcHeight;
    }

    @Override
    public float getWidth() {
        return srcWidth;
    }

    @Override
    public float getHeight() {
        return srcHeight;
    }

    @Override
    public void draw(Graphics graphics, float x, float y, float width, float height, int color, ImageFilter filter) {
        if (graphics.discardDraw(x, y, width, height)) return;
        if (texture.getTexture() == null) return;

        graphics.drawImage(texture, srcX, srcY, srcX + srcWidth, srcY + srcHeight, x, y, x + width, y + height, color, filter != ImageFilter.LINEAR);
    }

    @Override
    public void draw(Graphics graphics, float x, float y, float frame, ImageFilter filter) {
        draw(graphics, x, y, getWidth(), getHeight(), 0xFFFFFFFF, filter);
    }
}
