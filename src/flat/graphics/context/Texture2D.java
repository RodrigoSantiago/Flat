package flat.graphics.context;

import flat.backend.GL;
import static flat.backend.GLEnuns.*;
import flat.graphics.context.enuns.ImageMagFilter;
import flat.graphics.context.enuns.ImageMinFilter;
import flat.graphics.context.enuns.ImageWrapMode;
import flat.graphics.context.enuns.TextureFormat;

import java.nio.Buffer;

public class Texture2D extends Texture {

    private int textureId;
    private TextureFormat format;

    private int width, height, levels;
    private ImageMinFilter minFilter;
    private ImageMagFilter magFilter;
    private ImageWrapMode wrapModeX, wrapModeY;

    public Texture2D() {
        super();
    }

    @Override
    protected void onInitialize() {
        Context.getContext();
        final int textureId =  GL.TextureCreate();

        setDispose(() -> GL.TextureDestroy(textureId));

        this.textureId = textureId;
    }

    int getInternalID() {
        init();
        return textureId;
    }

    int getInternalType() {
        init();
        return TB_TEXTURE_2D;
    }

    public void setSize(int width, int height, TextureFormat format) {
        this.format = format;
        this.width = width;
        this.height = height;
        GL.TextureDataBuffer(TT_TEXTURE_2D, 0, format.getInternalEnum(), width, height, 0, null, 0);
        generatMipmapLevels();
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

    public void setData(int level, Buffer buffer, int offset, int x, int y, int width, int height) {
        GL.TextureSubDataBuffer(TT_TEXTURE_2D, level, x, y, width, height, format.getInternalEnum(), buffer, offset);
    }

    public void setData(int level, int[] data, int offset, int x, int y, int width, int height) {
        GL.TextureSubDataI(TT_TEXTURE_2D, level, x, y, width, height, format.getInternalEnum(), data, offset);
    }

    public void setData(int level, byte[] data, int offset, int x, int y, int width, int height) {
        GL.TextureSubDataB(TT_TEXTURE_2D, level, x, y, width, height, format.getInternalEnum(), data, offset);
    }

    public void setLevels(int levels) {
        this.levels = levels;
        GL.TextureSetLevels(TB_TEXTURE_CUBE_MAP, levels);
    }

    public int getLevels() {
        return levels;
    }

    public void generatMipmapLevels() {
        GL.TextureGenerateMipmap(TB_TEXTURE_CUBE_MAP);
    }

    public void setScaleFilters(ImageMagFilter magFilter, ImageMinFilter minFilter) {
        this.magFilter = magFilter;
        this.minFilter = minFilter;
        GL.TextureSetFilter(TB_TEXTURE_CUBE_MAP, magFilter.getInternalEnum(), minFilter.getInternalEnum());
    }

    public ImageMagFilter getMagFilter() {
        return magFilter;
    }

    public ImageMinFilter getMinFilter() {
        return minFilter;
    }

    public void setWrapModes(ImageWrapMode wrapModeX, ImageWrapMode wrapModeY) {
        this.wrapModeX = wrapModeX;
        this.wrapModeY = wrapModeY;
        GL.TextureSetWrap(TB_TEXTURE_CUBE_MAP, wrapModeX.getInternalEnum(), wrapModeY.getInternalEnum());
    }

    public ImageWrapMode getWrapModeX() {
        return wrapModeX;
    }

    public ImageWrapMode getWrapModeY() {
        return wrapModeY;
    }
}
