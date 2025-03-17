package flat.graphics.context;

import flat.backend.GL;
import flat.backend.GLEnums;
import flat.graphics.context.enums.PixelFormat;

public final class TextureMultisample2D extends Texture {

    private final int textureId;

    private PixelFormat format;
    private int width, height, samples;

    public TextureMultisample2D(Context context, int width, int height, int samples, PixelFormat format) {
        super(context);
        final int textureId = GL.TextureCreate();
        this.textureId = textureId;
        assignDispose(() -> GL.TextureDestroy(textureId));
        setSize(width, height, samples, format);
    }

    @Override
    int getInternalID() {
        return textureId;
    }

    @Override
    int getInternalType() {
        return GLEnums.TB_TEXTURE_2D_MULTISAMPLE;
    }

    private void boundCheck() {
        if (isDisposed()) {
            throw new RuntimeException("The " + getClass().getSimpleName() + " is disposed.");
        }
        getContext().bindTexture(this);
    }

    public void setSize(int width, int height, int samples, PixelFormat format) {
        dataBoundsCheck(width, height, samples);
        boundCheck();

        this.width = width;
        this.height = height;
        this.samples = samples;
        this.format = format;
        GL.TextureMultisample(samples, 0x8058/*format.getInternalEnum()*/, width, height, true);
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

    private void dataBoundsCheck(int width, int height, int samples) {
        if (width <= 0 || height <= 0 || samples < 0) {
            throw new RuntimeException("Zero or negative values are not allowed (" + width + ", " + height + ", " + samples + ")");
        }
    }
}
