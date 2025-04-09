package flat.graphics.context;

import flat.backend.GL;
import flat.backend.GLEnums;
import flat.exception.FlatException;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.context.enums.WrapMode;

public final class TextureMultisample2D extends Texture {

    private final int textureId;

    private PixelFormat format;
    private int width, height, samples;
    private WrapMode wrapModeX = WrapMode.REPEAT;
    private WrapMode wrapModeY = WrapMode.REPEAT;

    public TextureMultisample2D(int width, int height, int samples, PixelFormat format) {
        final int textureId = GL.TextureCreate();
        if (textureId == 0) {
            throw new FlatException("Unable to create a new OpenGL Texture");
        }

        this.textureId = textureId;
        assignDispose(() -> GL.TextureDestroy(textureId));
        setSize(width, height, samples, format);
    }

    @Override
    int getInternalId() {
        return textureId;
    }

    @Override
    int getInternalType() {
        return GLEnums.TB_TEXTURE_2D_MULTISAMPLE;
    }

    public void setSize(int width, int height, int samples, PixelFormat format) {
        dataBoundsCheck(width, height, samples);
        boundCheck();

        this.width = width;
        this.height = height;
        this.samples = samples;
        this.format = format;
        GL.TextureMultisample(samples, format.getInternalEnum(), width, height, true);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSamples() {
        return samples;
    }

    public PixelFormat getFormat() {
        return format;
    }

    public void setWrapModes(WrapMode wrapModeX, WrapMode wrapModeY) {
        boundCheck();

        this.wrapModeX = wrapModeX;
        this.wrapModeY = wrapModeY;
        GL.TextureSetWrap(GLEnums.TT_TEXTURE_2D, wrapModeX.getInternalEnum(), wrapModeY.getInternalEnum());
    }

    public WrapMode getWrapModeX() {
        return wrapModeX;
    }

    public WrapMode getWrapModeY() {
        return wrapModeY;
    }

    private void dataBoundsCheck(int width, int height, int samples) {
        if (width <= 0 || height <= 0 || samples < 0) {
            throw new FlatException("Zero or negative values are not allowed (" + width + ", " + height + ", " + samples + ")");
        }
        if (width > Context.getMaxTextureSize() || height > Context.getMaxTextureSize()) {
            throw new FlatException("The texture size is bigger than the max allowed (" + Context.getMaxTextureSize() + ")");
        }
    }
}
