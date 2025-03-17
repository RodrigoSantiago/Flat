package flat.graphics.context;

import flat.backend.GL;
import flat.backend.GLEnums;
import flat.graphics.ImageTexture;
import flat.graphics.context.enums.MagFilter;
import flat.graphics.context.enums.MinFilter;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.context.enums.WrapMode;

import java.nio.Buffer;

public final class Texture2D extends Texture implements ImageTexture {

    private final int textureId;

    private PixelFormat format;
    private int width, height, levels;
    private MinFilter minFilter;
    private MagFilter magFilter;
    private WrapMode wrapModeX, wrapModeY;

    public Texture2D(Context context) {
        super(context);
        final int textureId = GL.TextureCreate();
        this.textureId = textureId;
        assignDispose(() -> GL.TextureDestroy(textureId));
    }

    Texture2D(Context context, int id, int width, int height, int levels, PixelFormat format) {
        super(context);
        this.textureId = id;
        this.width = width;
        this.height = height;
        this.levels = levels;
        this.format = format;
    }

    int getInternalID() {
        return textureId;
    }

    int getInternalType() {
        return GLEnums.TB_TEXTURE_2D;
    }

    public void setSize(int width, int height, PixelFormat format) {
        boundCheck();

        this.width = width;
        this.height = height;
        this.format = format;
        GL.TextureDataBuffer(GLEnums.TT_TEXTURE_2D, 0, format.getInternalEnum(), width, height, 0, null, 0);
        generateMipmapLevels();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth(int level) {
        return (int) Math.max(1, Math.floor(width / Math.pow(2,level)));
    }

    public int getHeight(int level) {
        return (int) Math.max(1, Math.floor(height / Math.pow(2,level)));
    }

    public void getData(int level, Buffer buffer, int offset) {
        boundCheck();
        dataBoundsCheck(buffer.capacity() - offset, width, height);

        GL.TexGetImageBuffer(GLEnums.TT_TEXTURE_2D, level, format.getInternalEnum(), buffer, offset);
    }

    public void getData(int level, int[] data, int offset) {
        boundCheck();
        dataBoundsCheck(data.length - offset, width, height);

        GL.TexGetImageI(GLEnums.TT_TEXTURE_2D, level, format.getInternalEnum(), data, offset);
    }

    public void getData(int level, byte[] data, int offset) {
        boundCheck();
        dataBoundsCheck(data.length - offset, width, height);

        GL.TexGetImageB(GLEnums.TT_TEXTURE_2D, level, format.getInternalEnum(), data, offset);
    }

    public void setData(int level, Buffer buffer, int offset, int x, int y, int width, int height) {
        boundCheck();
        dataBoundsCheck(buffer.capacity() - offset, width, height);

        GL.TextureSubDataBuffer(GLEnums.TT_TEXTURE_2D, level, x, y, width, height, format.getInternalEnum(), buffer, offset);
    }

    public void setData(int level, int[] data, int offset, int x, int y, int width, int height) {
        boundCheck();
        dataBoundsCheck(data.length - offset, width, height);

        GL.TextureSubDataI(GLEnums.TT_TEXTURE_2D, level, x, y, width, height, format.getInternalEnum(), data, offset);
    }

    public void setData(int level, byte[] data, int offset, int x, int y, int width, int height) {
        boundCheck();
        dataBoundsCheck(data.length - offset, width, height);

        GL.TextureSubDataB(GLEnums.TT_TEXTURE_2D, level, x, y, width, height, format.getInternalEnum(), data, offset);
    }

    public void setLevels(int levels) {
        boundCheck();

        this.levels = levels;
        GL.TextureSetLevels(GLEnums.TT_TEXTURE_2D, levels);
    }

    public int getLevels() {
        return levels;
    }

    public void generateMipmapLevels() {
        boundCheck();

        GL.TextureGenerateMipmap(GLEnums.TT_TEXTURE_2D);
    }

    public void setScaleFilters(MagFilter magFilter, MinFilter minFilter) {
        boundCheck();

        this.magFilter = magFilter;
        this.minFilter = minFilter;
        GL.TextureSetFilter(GLEnums.TT_TEXTURE_2D, magFilter.getInternalEnum(), minFilter.getInternalEnum());
    }

    public MagFilter getMagFilter() {
        return magFilter;
    }

    public MinFilter getMinFilter() {
        return minFilter;
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

    private void dataBoundsCheck(int length, int width, int height) {
        int required = width * height * format.getPixelBytes();
        if (length < required) {
            throw new RuntimeException("The image data is too short. Provided : " + length + ", Required : " + required);
        }
    }

    @Override
    public Texture2D getTexture(Context context) {
        return context == this.getContext() ? this : null;
    }
}
