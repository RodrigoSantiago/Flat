package flat.graphics.context;

import flat.backend.GL;
import static flat.backend.GLEnuns.*;

import flat.graphics.context.enuns.*;
import flat.screen.Application;

import java.nio.Buffer;

public final class Cubemap extends Texture {

    private int cubemapId;
    private PixelFormat format;

    private int width, height, levels;
    private MinFilter minFilter;
    private MagFilter magFilter;
    private WrapMode wrapModeX, wrapModeY;

    public Cubemap() {
        init();
    }

    @Override
    protected void onInitialize() {
        this.cubemapId = GL.TextureCreate();
    }

    @Override
    protected void onDispose() {
        GL.TextureDestroy(cubemapId);
    }

    int getInternalID() {
        return cubemapId;
    }

    int getInternalType() {
        return TB_TEXTURE_CUBE_MAP;
    }

    public void setSize(int width, int height, PixelFormat format) {
        this.width = width;
        this.height = height;
        this.format = format;
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
        Application.getCurrentContext().refreshBufferBinds();
        GL.TextureSubDataBuffer(face.getInternalEnum(), level, x, y, width, height, format.getInternalEnum(), buffer, offset);
    }

    public void setData(CubeFace face, int level, int[] data, int offset, int x, int y, int width, int height) {
        Application.getCurrentContext().refreshBufferBinds();
        GL.TextureSubDataI(face.getInternalEnum(), level, x, y, width, height, format.getInternalEnum(), data, offset);
    }

    public void setData(CubeFace face, int level, byte[] data, int offset, int x, int y, int width, int height) {
        Application.getCurrentContext().refreshBufferBinds();
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

    public void setScaleFilters(MagFilter magFilter, MinFilter minFilter) {
        this.magFilter = magFilter;
        this.minFilter = minFilter;
        GL.TextureSetFilter(TB_TEXTURE_CUBE_MAP, magFilter.getInternalEnum(), minFilter.getInternalEnum());
    }

    public MagFilter getMagFilter() {
        return magFilter;
    }

    public MinFilter getMinFilter() {
        return minFilter;
    }

    public void setWrapModes(WrapMode wrapModeX, WrapMode wrapModeY) {
        this.wrapModeX = wrapModeX;
        this.wrapModeY = wrapModeY;
        GL.TextureSetWrap(TB_TEXTURE_CUBE_MAP, wrapModeX.getInternalEnum(), wrapModeY.getInternalEnum());
    }

    public WrapMode getWrapModeX() {
        return wrapModeX;
    }

    public WrapMode getWrapModeY() {
        return wrapModeY;
    }
}
