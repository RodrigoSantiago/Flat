package flat.graphics.context;

import flat.backend.GL;
import flat.backend.GLEnums;
import flat.graphics.context.enums.PixelFormat;

public final class TextureMultisample2D extends Texture {

    private final int textureId;

    private PixelFormat format;
    private int width, height, samples;

    public TextureMultisample2D(Context context) {
        super(context);
        final int textureId = GL.TextureCreate();
        this.textureId = textureId;
        assignDispose(() -> GL.TextureDestroy(textureId));
    }

    int getInternalID() {
        return textureId;
    }

    int getInternalType() {
        return GLEnums.TB_TEXTURE_2D_MULTISAMPLE;
    }

    public void setSize(int width, int height, int samples, PixelFormat format) {
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

    private void dataBoundsCheck(int length, int width, int height) {
        int required = width * height * format.getPixelBytes();
        if (length < required) {
            throw new RuntimeException("The image data is too short. Provided : " + length + ", Required : " + required);
        }
    }
}
