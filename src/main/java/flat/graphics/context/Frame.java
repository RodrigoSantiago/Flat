package flat.graphics.context;

import flat.backend.GL;
import flat.backend.GLEnums;
import flat.graphics.context.enums.CubeFace;

public final class Frame extends ContextObject {

    public static final int DEPTH = -1;
    public static final int STENCIL = -2;
    public static final int DEPTH_STENCIL = -3;

    private final int frameBufferId;
    private final Layer[] layers = new Layer[10];

    public Frame(Context context) {
        super(context);

        for (int i = 0; i < 10; i++) {
            layers[i] = new Layer();
        }
        final int frameBufferId = GL.FrameBufferCreate();
        this.frameBufferId = frameBufferId;
        assignDispose(() -> GL.FrameBufferDestroy(frameBufferId));
    }

    @Override
    protected boolean isBound() {
        return getContext().isFrameBound(this);
    }

    int getInternalID() {
        return frameBufferId;
    }

    public void begin() {
        getContext().bindFrame(this);
    }

    public void end() {
        getContext().unbindFrame();
    }

    public boolean isReady() {
        return GL.FrameBufferGetStatus(GLEnums.FB_FRAMEBUFFER) == GLEnums.FS_FRAMEBUFFER_COMPLETE;
    }

    public void attach(int index, Render render) {
        if (index == DEPTH_STENCIL) {
            attach(DEPTH, render);
            attach(STENCIL, render);
        } else {
            boundCheck();

            Layer layer = layers[index + 2].set(render);
            GL.FrameBufferRenderBuffer(GLEnums.FB_FRAMEBUFFER, getAttacEnum(index), layer.getInternalID());
        }
    }

    public void attach(int index, Texture2D texture) {
        attach(index, texture, 0);
    }

    public void attach(int index, Texture2D texture, int level) {
        if (index == DEPTH_STENCIL) {
            attach(DEPTH, texture, level);
            attach(STENCIL, texture, level);
        } else {
            boundCheck();

            Layer layer = layers[index + 2].set(texture, level);
            GL.FrameBufferTexture2D(GLEnums.FB_FRAMEBUFFER, getAttacEnum(index), layer.getInternalEnum(), layer.getInternalID(), layer.getInternalLevel());
        }
    }

    public void attach(int index, TextureMultisample2D texture, int level) {
        if (index == DEPTH_STENCIL) {
            attach(DEPTH, texture, level);
            attach(STENCIL, texture, level);
        } else {
            boundCheck();

            Layer layer = layers[index + 2].set(texture, level);
            GL.FrameBufferTextureMultisample(GLEnums.FB_FRAMEBUFFER, getAttacEnum(index), layer.getInternalID());
        }
    }

    public void attach(int index, TextureMultisample2D texture) {
        attach(index, texture, 0);
    }

    public void attach(int index, Cubemap cubemap, CubeFace face) {
        attach(index, cubemap, face, 0);
    }

    public void attach(int index, Cubemap cubemap, CubeFace face, int level) {
        if (index == DEPTH_STENCIL) {
            attach(DEPTH, cubemap, face, level);
            attach(STENCIL, cubemap, face, level);
        } else {
            boundCheck();

            Layer layer = layers[index + 2].set(cubemap, face, level);
            GL.FrameBufferTexture2D(GLEnums.FB_FRAMEBUFFER, getAttacEnum(index), layer.getInternalEnum(), layer.getInternalID(), layer.getInternalLevel());

        }
    }

    public void detach(int index) {
        if (index == DEPTH_STENCIL) {
            detach(DEPTH);
            detach(STENCIL);
        } else {
            Layer layer = layers[index + 2];
            if (!layer.isNull()) {
                if (layer.isRender()) {
                    GL.FrameBufferRenderBuffer(GLEnums.FB_FRAMEBUFFER, getAttacEnum(index), 0);
                } else {
                    GL.FrameBufferTexture2D(GLEnums.FB_FRAMEBUFFER, getAttacEnum(index), layer.getInternalEnum(), 0, 0);
                }
            }
            layer.set();
        }
    }

    public void setTargets(int c0, int c1, int c2, int c3, int c4, int c5, int c6, int c7) {
        boundCheck();

        GL.FrameBufferSetTargets(c0, c1, c2, c3, c4, c5, c6, c7);
    }

    private int getAttacEnum(int index) {
        return index == DEPTH ? GLEnums.FA_DEPTH_ATTACHMENT :
                index == STENCIL ? GLEnums.FA_STENCIL_ATTACHMENT :
                        index == DEPTH_STENCIL ? GLEnums.FA_DEPTH_STENCIL_ATTACHMENT : GLEnums.FA_COLOR_ATTACHMENT0 + index;
    }

    private static class Layer {
        private boolean render;
        private int internalID;
        private int internalEnum;
        private int internalLevel;

        // Null
        void set() {
            render = false;
            internalID = 0;
            internalEnum = 0;
            internalLevel = 0;
        }

        // RenderBuffer
        Layer set(Render source) {
            internalID = source.getInternalID();
            internalEnum = -1;
            internalLevel = -1;
            render = true;
            return this;
        }

        // Texture2D
        Layer set(Texture2D source, int level) {
            internalID = source.getInternalID();
            internalEnum = GLEnums.TT_TEXTURE_2D;
            internalLevel = level;
            render = false;
            return this;
        }

        // Texture2D
        Layer set(TextureMultisample2D source, int level) {
            internalID = source.getInternalID();
            internalEnum = GLEnums.TB_TEXTURE_2D_MULTISAMPLE;
            internalLevel = level;
            render = false;
            return this;
        }

        // CubemapFace
        Layer set(Cubemap source, CubeFace face, int level) {
            internalID = source.getInternalID();
            internalEnum = face.getInternalEnum();
            internalLevel = level;
            render = false;
            return this;
        }

        boolean isRender() {
            return render;
        }

        int getInternalID() {
            return internalID;
        }

        int getInternalEnum() {
            return internalEnum;
        }

        int getInternalLevel() {
            return internalLevel;
        }

        public boolean isNull() {
            return internalID == 0;
        }
    }
}
