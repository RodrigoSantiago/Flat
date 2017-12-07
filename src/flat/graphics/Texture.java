package flat.graphics;

import flat.backend.GL;
import static flat.backend.GLEnuns.*;

public class Texture extends ContextObject {

    int textureId;
    int levels = 0;

    int width, height;

    public Texture(int width, int height) {
        Context.getContext();

        textureId = GL.TextureCreate();

        resize(width, height);
    }

    public void resize(int width, int height) {
        Context.getContext();

        this.width = width;
        this.height = height;

        int tx = GL.TextureGetBound(TB_TEXTURE_2D);

        GL.TextureBind(TB_TEXTURE_2D, textureId);
        GL.TextureSetFilter(TB_TEXTURE_2D, IF_NEAREST, IF_NEAREST);
        GL.TextureSetLevels(TB_TEXTURE_2D, levels);
        GL.TextureDataBuffer(TT_TEXTURE_2D, 0, TF_RGBA, width, height, 0, null, 0);

        GL.TextureBind(TB_TEXTURE_2D, tx);
    }

    public void setMipMaps(int levels) {
        Context.getContext();

        int tx = GL.TextureGetBound(TB_TEXTURE_2D);

        GL.TextureBind(TB_TEXTURE_2D, textureId);
        GL.TextureSetLevels(TB_TEXTURE_2D, levels);
        GL.TextureGenerateMipmap(TB_TEXTURE_2D);

        GL.TextureBind(TB_TEXTURE_2D, tx);
    }

    public void refreshMipMaps() {
        setMipMaps(levels);
    }

    @Override
    protected void onDispose() {
        GL.TextureDestroy(textureId);
    }
}
