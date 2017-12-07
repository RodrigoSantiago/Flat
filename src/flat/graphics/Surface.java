package flat.graphics;

import flat.backend.GL;

import static flat.backend.GLEnuns.*;

public class Surface extends ContextObject {

    private Texture[] textures = new Texture[8];
    int frameBufferId, renderBufferId;

    public Surface(int width, int height) {
        Context.getContext();

        frameBufferId = GL.FrameBufferCreate();
        renderBufferId = GL.RenderBufferCreate();

        resize(width, height);
    }

    public void resize(int width, int height) {
        Context.getContext();

        int fb = GL.FrameBufferGetBound(FB_DRAW_FRAMEBUFFER);
        int buf = GL.RenderBufferGetBound();

        GL.FrameBufferBind(FB_DRAW_FRAMEBUFFER, frameBufferId);

        GL.RenderBufferBind(renderBufferId);
        GL.RenderBufferStorage(TF_DEPTH24_STENCIL8, width, height);
        GL.FrameBufferRenderBuffer(FB_DRAW_FRAMEBUFFER, FA_DEPTH_STENCIL_ATTACHMENT, renderBufferId);

        GL.RenderBufferBind(buf);
        GL.FrameBufferBind(FB_DRAW_FRAMEBUFFER, fb);
    }

    public boolean attachTexture(Texture texture) {
        for (int i = 0; i < textures.length; i++) {
            if (textures[i] == null) {
                textures[i] = texture;

                int fb = GL.FrameBufferGetBound(FB_DRAW_FRAMEBUFFER);
                int tx = GL.TextureGetBound(TB_TEXTURE_2D);

                GL.TextureBind(TB_TEXTURE_2D, texture.textureId);
                GL.FrameBufferBind(FB_DRAW_FRAMEBUFFER, frameBufferId);
                GL.FrameBufferTexture2D(FB_DRAW_FRAMEBUFFER, FA_COLOR_ATTACHMENT0 + i, TT_TEXTURE_2D, texture.textureId, 0);

                GL.TextureBind(TB_TEXTURE_2D, tx);
                GL.FrameBufferBind(FB_DRAW_FRAMEBUFFER, fb);

                return true;
            }
        }
        return false;
    }

    public boolean detachTexture(Texture texture) {
        for (int i = 0; i < textures.length; i++) {
            if (textures[i] == texture) {
                textures[i] = null;
                int fb = GL.FrameBufferGetBound(FB_DRAW_FRAMEBUFFER);
                int tx = GL.TextureGetBound(TB_TEXTURE_2D);

                GL.TextureBind(TB_TEXTURE_2D, texture.textureId);
                GL.FrameBufferBind(FB_DRAW_FRAMEBUFFER, frameBufferId);
                GL.FrameBufferTexture2D(FB_DRAW_FRAMEBUFFER, FA_COLOR_ATTACHMENT0 + i, TT_TEXTURE_2D, 0, 0);

                GL.TextureBind(TB_TEXTURE_2D, tx);
                GL.FrameBufferBind(FB_DRAW_FRAMEBUFFER, fb);

                return true;
            }
        }
        return false;
    }

    public void setTargets(int c0, int c1, int c2, int c3, int c4, int c5, int c6, int c7) {
        int fb = GL.FrameBufferGetBound(FB_DRAW_FRAMEBUFFER);
        GL.FrameBufferSetTargets(att(c0), att(c1), att(c2), att(c3), att(c4), att(c5), att(c6), att(c7));
        GL.FrameBufferBind(FB_DRAW_FRAMEBUFFER, fb);
    }

    private int att(int c) {
        return c == -1 ? 0 : c + FA_COLOR_ATTACHMENT0;
    }

    @Override
    protected void onDispose() {
        for (int i = 0; i < textures.length; i++) {
            textures[i] = null;
        }
        GL.RenderBufferDestroy(renderBufferId);
        GL.FrameBufferDestroy(frameBufferId);
    }
}
