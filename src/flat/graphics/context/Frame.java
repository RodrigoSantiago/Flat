package flat.graphics.context;

import flat.backend.GL;
import flat.graphics.context.enuns.CubeFace;

import static flat.backend.GLEnuns.*;

public class Frame extends ContextObject {

    public static final int DEPTH = -1;
    public static final int STENCIL = -2;
    public static final int DEPTH_STENCIL = -3;

    private Context context;
    private final Layer[] layers = new Layer[10];
    private int frameBufferId;
    private boolean draw, read;

    public Frame(Context context) {
        this.context = context;

        for (int i = 0; i < 10; i++) {
            layers[i] = new Layer();
        }
        init();
    }

    @Override
    protected void onInitialize() {
        this.frameBufferId = GL.FrameBufferCreate();
    }

    @Override
    protected void onDispose() {
        GL.FrameBufferDestroy(frameBufferId);
    }

    int getInternalID() {
        return frameBufferId;
    }

    public void beginDraw() {
        context.bindFrame(this, true, false);
    }

    public void beginRead() {
        context.bindFrame(this, false, true);
    }

    public void begin() {
        context.bindFrame(this, true, true);
    }

    public void end() {
        context.bindFrame(null, draw, read);
    }

    void setBindType(boolean draw, boolean read) {
        this.draw = draw;
        this.read = read;
    }

    void setDrawBindType(boolean draw) {
        this.draw = draw;
    }

    void setReadBindType(boolean read) {
        this.read = read;
    }

    public boolean isReady() {
        return GL.FrameBufferGetStatus(getBindEnum()) == FS_FRAMEBUFFER_COMPLETE;
    }

    public void attach(int index, Render render) {
        if (index == DEPTH_STENCIL) {
            attach(DEPTH, render);
            attach(STENCIL, render);
        } else {
            Layer layer = layers[index + 2].set(render);
            GL.FrameBufferRenderBuffer(getBindEnum(), getAttacEnum(index), layer.getInternalID());
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
            Layer layer = layers[index + 2].set(texture, level);
            GL.FrameBufferTexture2D(getBindEnum(), getAttacEnum(index), layer.getInternalEnum(), layer.getInternalID(), layer.getInternalLevel());
        }
    }

    public void attach(int index, Cubemap cubemap, CubeFace face) {
        attach(index, cubemap, face, 0);
    }

    public void attach(int index, Cubemap cubemap, CubeFace face, int level) {
        if (index == DEPTH_STENCIL) {
            attach(DEPTH, cubemap, face, level);
            attach(STENCIL, cubemap, face, level);
        } else {
            Layer layer = layers[index + 2].set(cubemap, face, level);
            GL.FrameBufferTexture2D(getBindEnum(), getAttacEnum(index), layer.getInternalEnum(), layer.getInternalID(), layer.getInternalLevel());

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
                    GL.FrameBufferRenderBuffer(getBindEnum(), getAttacEnum(index), 0);
                } else {
                    GL.FrameBufferTexture2D(getBindEnum(), getAttacEnum(index), layer.getInternalEnum(), 0, 0);
                }
            }
            layer.set();
        }
    }

    public void setTargets(int c0, int c1, int c2, int c3, int c4, int c5, int c6, int c7) {
        GL.FrameBufferSetTargets(c0, c1, c2, c3, c4, c5, c6, c7);
    }

    private int getBindEnum() {
        return read ? draw ? FB_FRAMEBUFFER : FB_READ_FRAMEBUFFER : FB_DRAW_FRAMEBUFFER;
    }

    private int getAttacEnum(int index) {
        return index == DEPTH ? FA_DEPTH_ATTACHMENT :
                index == STENCIL ? FA_STENCIL_ATTACHMENT :
                        index == DEPTH_STENCIL ? FA_DEPTH_STENCIL_ATTACHMENT : FA_COLOR_ATTACHMENT0 + index;
    }

    class Layer {

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
            internalEnum = TT_TEXTURE_2D;
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
