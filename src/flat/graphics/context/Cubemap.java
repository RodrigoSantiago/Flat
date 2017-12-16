package flat.graphics.context;

import flat.backend.GL;
import static flat.backend.GLEnuns.*;

import flat.graphics.context.enuns.*;

import java.nio.Buffer;

public class Cubemap extends Texture {

    private int cubemapId;
    private TextureFormat format;

    private int width, height, levels;
    private ImageMinFilter minFilter;
    private ImageMagFilter magFilter;
    private ImageWrapMode wrapModeX, wrapModeY;

    public Cubemap() {
        super();
    }

    @Override
    protected void onInitialize() {
        Context.getContext();
        final int cubmapId =  GL.TextureCreate();

        setDispose(() -> GL.TextureDestroy(cubmapId));

        this.cubemapId = cubmapId;
    }

    int getInternalID() {
        init();
        return cubemapId;
    }

    int getInternalType() {
        init();
        return TB_TEXTURE_CUBE_MAP;
    }

    public void setSize(int width, int height, TextureFormat format) {
        this.format = format;
        this.width = width;
        this.height = height;
        GL.TextureDataBuffer(TT_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, format.getInternalEnum(), width, height, 0, null, 0);
        GL.TextureDataBuffer(TT_TEXTURE_CUBE_MAP_POSITIVE_X, 0, format.getInternalEnum(), width, height, 0, null, 0);
        GL.TextureDataBuffer(TT_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, format.getInternalEnum(), width, height, 0, null, 0);
        GL.TextureDataBuffer(TT_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, format.getInternalEnum(), width, height, 0, null, 0);
        GL.TextureDataBuffer(TT_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, format.getInternalEnum(), width, height, 0, null, 0);
        GL.TextureDataBuffer(TT_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, format.getInternalEnum(), width, height, 0, null, 0);
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

    public void setData(CubeFace face, int level, Buffer buffer, int offset, int x, int y, int width, int height) {
        GL.TextureSubDataBuffer(face.getInternalEnum(), level, x, y, width, height, format.getInternalEnum(), buffer, offset);
    }

    public void setData(CubeFace face, int level, int[] data, int offset, int x, int y, int width, int height) {
        GL.TextureSubDataI(face.getInternalEnum(), level, x, y, width, height, format.getInternalEnum(), data, offset);
    }

    public void setData(CubeFace face, int level, byte[] data, int offset, int x, int y, int width, int height) {
        GL.TextureSubDataB(face.getInternalEnum(), level, x, y, width, height, format.getInternalEnum(), data, offset);
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
