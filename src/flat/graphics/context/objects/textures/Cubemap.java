package flat.graphics.context.objects.textures;

import flat.graphics.context.buffers.PixelPackBuffer;
import flat.graphics.context.buffers.PixelUnpackBuffer;
import flat.graphics.context.enuns.CubeFace;
import flat.graphics.context.enuns.ImageMagFilter;
import flat.graphics.context.enuns.ImageMinFilter;
import flat.graphics.context.enuns.ImageWrapMode;

public class Cubemap extends Texture {

    private int width, height, levels;
    private ImageMinFilter minFilter;
    private ImageMagFilter magFilter;
    private ImageWrapMode wrapModeX, wrapModeY;

    public Cubemap() {

    }

    public Layer getLayer(CubeFace face) {
        return null;
    }

    public void setSize(int level, int width, int height) {

    }

    public void getData(CubeFace face, int level, PixelPackBuffer buffer, int offset, int x, int y, int width, int height) {

    }

    public void setData(CubeFace face, int level, PixelUnpackBuffer buffer, int offset, int x, int y, int width, int height) {

    }

    public void getData(CubeFace face, int level, int[] data, int offset, int x, int y, int width, int height) {

    }

    public void setData(CubeFace face, int level, int[] data, int offset, int x, int y, int width, int height) {

    }

    public void getData(CubeFace face, int level, byte[] data, int offset, int x, int y, int width, int height) {

    }

    public void setData(CubeFace face, int level, byte[] data, int offset, int x, int y, int width, int height) {

    }

    public int getWidth(int level) {
        return width;
    }

    public int getHeight(int level) {
        return height;
    }

    public int getLevels() {
        return levels;
    }

    public void setLevels(int levels) {
        this.levels = levels;
    }

    public void generatMipmapLevels() {

    }

    public ImageMagFilter getMagFilter() {
        return magFilter;
    }

    public ImageMinFilter getMinFilter() {
        return minFilter;
    }

    @Override
    protected void onDispose() {

    }
}
