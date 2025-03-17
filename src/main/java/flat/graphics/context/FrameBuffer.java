package flat.graphics.context;

import flat.backend.GL;
import flat.backend.GLEnums;
import flat.graphics.context.enums.CubeFace;
import flat.graphics.context.enums.LayerTarget;

public final class FrameBuffer extends ContextObject {

    private final int frameBufferId;
    private final Layer[] layers = new Layer[11];
    private final int[] targets = new int[8];
    private boolean invalidLayers;
    private boolean invalidTargets;

    public FrameBuffer(Context context) {
        super(context);

        for (int i = 0; i < 11; i++) {
            layers[i] = new Layer(LayerTarget.values()[i]);
        }
        final int frameBufferId = GL.FrameBufferCreate();
        this.frameBufferId = frameBufferId;
        assignDispose(() -> GL.FrameBufferDestroy(frameBufferId));
    }

    int getInternalID() {
        return frameBufferId;
    }

    boolean isInvalid() {
        return invalidLayers || invalidTargets;
    }

    boolean isBond() {
        return getContext().getFrameBuffer() == FrameBuffer.this;
    }

    void onBound() {
        for (var layer : layers) {
            layer.checkInvalided();
        }
        if (invalidTargets) {
            GL.FrameBufferSetTargets(targets[0], targets[1], targets[2], targets[3],
                    targets[4], targets[5], targets[6], targets[7]);
        }
        invalidLayers = false;
        invalidTargets = false;
    }

    public void detach(LayerTarget target) {
        Layer layer = layers[target.ordinal()];
        layer.set();
        if (!isBond()) {
            invalidLayers = true;
        }
    }

    public void attach(LayerTarget target, Render render) {
        Layer layer = layers[target.ordinal()];
        layer.set(render);
        if (!isBond()) {
            invalidLayers = true;
        }
    }

    public void attach(LayerTarget target, Texture2D texture, int level) {
        Layer layer = layers[target.ordinal()];
        layer.set(texture, level);
        if (!isBond()) {
            invalidLayers = true;
        }
    }

    public void attach(LayerTarget target, TextureMultisample2D texture, int level) {
        Layer layer = layers[target.ordinal()];
        layer.set(texture, level);
        if (!isBond()) {
            invalidLayers = true;
        }
    }

    public void attach(LayerTarget target, Cubemap cubemap, CubeFace face, int level) {
        Layer layer = layers[target.ordinal()];
        layer.set(cubemap, face, level);
        if (!isBond()) {
            invalidLayers = true;
        }
    }

    public void setTargets(int c0, int c1, int c2, int c3, int c4, int c5, int c6, int c7) {
        targets[0] = c0 < 0 || c0 >= 8 ? 0 : LayerTarget.getColorAttachment(c0).getInternalEnum();
        targets[1] = c1 < 0 || c1 >= 8 ? 0 : LayerTarget.getColorAttachment(c1).getInternalEnum();
        targets[2] = c2 < 0 || c2 >= 8 ? 0 : LayerTarget.getColorAttachment(c2).getInternalEnum();
        targets[3] = c3 < 0 || c3 >= 8 ? 0 : LayerTarget.getColorAttachment(c3).getInternalEnum();
        targets[4] = c4 < 0 || c4 >= 8 ? 0 : LayerTarget.getColorAttachment(c4).getInternalEnum();
        targets[5] = c5 < 0 || c5 >= 8 ? 0 : LayerTarget.getColorAttachment(c5).getInternalEnum();
        targets[6] = c6 < 0 || c6 >= 8 ? 0 : LayerTarget.getColorAttachment(c6).getInternalEnum();
        targets[7] = c7 < 0 || c7 >= 8 ? 0 : LayerTarget.getColorAttachment(c7).getInternalEnum();

        if (isBond()) {
            GL.FrameBufferSetTargets(targets[0], targets[1], targets[2], targets[3],
                    targets[4], targets[5], targets[6], targets[7]);
        } else {
            invalidTargets = true;
        }
    }

    private enum LayerType {
        EMPTY, RENDER, TEXTURE, TEXTUREMULTISAMPLE, CUBEMAP,
    }

    private class Layer {
        private final LayerTarget target;

        private LayerType type = LayerType.EMPTY;
        private LayerType prevType = LayerType.EMPTY;
        private CubeFace prevFace;

        private Render render;
        private Texture2D texture;
        private TextureMultisample2D textureMultisample;
        private Cubemap cubemap;
        private CubeFace face;
        private int level;

        private boolean invalid;

        public Layer(LayerTarget target) {
            this.target = target;
        }

        public void checkInvalided() {
            if (invalid) {
                onUpdate();
            }
        }

        private void onUpdate() {
            if (isBond()) {
                setFrameValue();
                invalid = false;
            } else {
                invalid = true;
            }
        }

        private void setFrameValue() {
            // Clear
            if (prevType == LayerType.RENDER) {
                GL.FrameBufferRenderBuffer(GLEnums.FB_FRAMEBUFFER, target.getInternalEnum(),
                        0);
            }
            if (prevType == LayerType.TEXTURE) {
                GL.FrameBufferTexture2D(GLEnums.FB_FRAMEBUFFER, target.getInternalEnum(),
                        GLEnums.TB_TEXTURE_2D, 0, 0);
            }
            if (prevType == LayerType.CUBEMAP) {
                GL.FrameBufferTexture2D(GLEnums.FB_FRAMEBUFFER, target.getInternalEnum(),
                        prevFace.getInternalEnum(), 0, 0);
            }
            if (prevType == LayerType.TEXTUREMULTISAMPLE) {
                GL.FrameBufferTextureMultisample(GLEnums.FB_FRAMEBUFFER, target.getInternalEnum(),
                        0);
            }

            // Set
            if (type == LayerType.RENDER) {
                GL.FrameBufferRenderBuffer(GLEnums.FB_FRAMEBUFFER, target.getInternalEnum(),
                        render.getInternalID());
            }
            if (type == LayerType.TEXTURE) {
                GL.FrameBufferTexture2D(GLEnums.FB_FRAMEBUFFER, target.getInternalEnum(),
                        texture.getInternalType(), texture.getInternalID(), level);
            }
            if (type == LayerType.CUBEMAP) {
                GL.FrameBufferTexture2D(GLEnums.FB_FRAMEBUFFER, target.getInternalEnum(),
                        face.getInternalEnum(), cubemap.getInternalID(), level);
            }
            if (type == LayerType.TEXTUREMULTISAMPLE) {
                GL.FrameBufferTextureMultisample(GLEnums.FB_FRAMEBUFFER, target.getInternalEnum(),
                        textureMultisample.getInternalID());
            }
            prevFace = face;
            prevType = type;
        }

        private void set() {
            type = LayerType.EMPTY;
            render = null;
            texture = null;
            textureMultisample = null;
            cubemap = null;
            face = null;
            level = 0;
            onUpdate();
        }

        private void set(Render render) {
            type = LayerType.RENDER;
            this.render = render;

            texture = null;
            textureMultisample = null;
            cubemap = null;
            face = null;
            level = 0;
            onUpdate();
        }

        private void set(Texture2D texture, int level) {
            type = LayerType.TEXTURE;
            this.texture = texture;
            this.level = level;

            render = null;
            textureMultisample = null;
            cubemap = null;
            face = null;
            onUpdate();
        }

        private void set(TextureMultisample2D textureMultisample, int level) {
            type = LayerType.TEXTUREMULTISAMPLE;
            this.textureMultisample = textureMultisample;
            this.level = level;

            render = null;
            texture = null;
            cubemap = null;
            face = null;
            onUpdate();
        }

        private void set(Cubemap cubemap, CubeFace face, int level) {
            type = LayerType.CUBEMAP;
            this.cubemap = cubemap;
            this.face = face;
            this.level = level;

            render = null;
            texture = null;
            textureMultisample = null;
            onUpdate();
        }
    }
}
