package flat.graphics.context;

import flat.backend.GL;
import flat.backend.GLEnums;
import flat.exception.FlatException;
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

    public Texture2D(Context context, int width, int height, PixelFormat format) {
        super(context);
        final int textureId = GL.TextureCreate();
        this.textureId = textureId;
        assignDispose(() -> GL.TextureDestroy(textureId));
        setSize(width, height, format);
    }

    Texture2D(Context context, int id, int width, int height, int levels, PixelFormat format) {
        super(context);
        this.textureId = id;
        this.width = width;
        this.height = height;
        this.levels = levels;
        this.format = format;
    }

    @Override
    int getInternalID() {
        return textureId;
    }

    @Override
    int getInternalType() {
        return GLEnums.TB_TEXTURE_2D;
    }

    private void boundCheck() {
        if (isDisposed()) {
            throw new RuntimeException("The " + getClass().getSimpleName() + " is disposed.");
        }
        getContext().bindTexture(this);
    }

    public void setSize(int width, int height, PixelFormat format) {
        parameterBoundsCheck(width, height);
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
        dataBoundsCheckSize(level, buffer.capacity() - offset, 1);
        boundCheck();

        GL.TexGetImageBuffer(GLEnums.TT_TEXTURE_2D, level, format.getInternalEnum(), buffer, offset);
    }

    public void getData(int level, int[] data, int offset) {
        dataBoundsCheckSize(level, data.length - offset, 4);
        boundCheck();

        GL.TexGetImageI(GLEnums.TT_TEXTURE_2D, level, format.getInternalEnum(), data, offset);
    }

    public void getData(int level, byte[] data, int offset) {
        dataBoundsCheckSize(level, data.length - offset, 1);
        boundCheck();

        GL.TexGetImageB(GLEnums.TT_TEXTURE_2D, level, format.getInternalEnum(), data, offset);
    }

    public void setData(int level, Buffer buffer, int offset, int x, int y, int width, int height) {
        dataBoundsCheckBox(level, buffer.capacity() - offset, 1, x, y, width, height);
        boundCheck();

        GL.TextureSubDataBuffer(GLEnums.TT_TEXTURE_2D, level, x, y, width, height, format.getInternalEnum(), buffer, offset);
    }

    public void setData(int level, int[] data, int offset, int x, int y, int width, int height) {
        dataBoundsCheckBox(level, data.length - offset, 4, x, y, width, height);
        boundCheck();

        GL.TextureSubDataI(GLEnums.TT_TEXTURE_2D, level, x, y, width, height, format.getInternalEnum(), data, offset);
    }

    public void setData(int level, byte[] data, int offset, int x, int y, int width, int height) {
        dataBoundsCheckBox(level, data.length - offset, 1, x, y, width, height);
        boundCheck();

        GL.TextureSubDataB(GLEnums.TT_TEXTURE_2D, level, x, y, width, height, format.getInternalEnum(), data, offset);
    }

    public void setLevels(int levels) {
        parameterLevelsCheck(levels);
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

    private void parameterLevelsCheck(int levels) {
        if (levels < 0) {
            throw new FlatException("Negative values are not allowed (" + levels + ")");
        }
    }

    private void parameterBoundsCheck(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new FlatException("Zero or negative values are not allowed (" + width + ", " + height + ")");
        }
    }

    private void dataBoundsCheckSize(int level, int arrayLen, int bytes) {
        int width = getWidth(level);
        int height = getHeight(level);
        int required = width * height * format.getPixelBytes();
        if (arrayLen * bytes < required) {
            throw new RuntimeException("The array is too short. Provided : " + arrayLen + ". Required : " + ((required - 1) / bytes + 1));
        }
    }

    private void dataBoundsCheckBox(int level, int arrayLen, int bytes, int x, int y, int w, int h) {
        int width = getWidth(level);
        int height = getHeight(level);
        if (x < 0 || w <= 0 || x + w > width || y < 0 || h <= 0 || y + h > height) {
            throw new FlatException("The coordinates are out of bounds (" + x + ", " + y + ", " + w + ", " + h + ")");
        }

        int required = w * h * format.getPixelBytes();
        if (arrayLen * bytes < required) {
            throw new RuntimeException("The array is too short. Provided : " + arrayLen + ". Required : " + ((required - 1) / bytes + 1));
        }
    }

    @Override
    public Texture2D getTexture(Context context) {
        return context == this.getContext() ? this : null;
    }
}
