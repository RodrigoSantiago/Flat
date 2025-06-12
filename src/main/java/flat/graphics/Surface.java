package flat.graphics;

import flat.exception.FlatException;
import flat.graphics.context.*;
import flat.graphics.context.enums.*;

public class Surface {
    private final int width;
    private final int height;
    private final int multiSamples;

    private FrameBuffer frameBuffer;
    private FrameBuffer frameBufferTransfer;
    private Render render;
    private TextureMultisample2D textureMultisamples;
    private Texture2D texture;
    private boolean disposed;

    private ClipState clipState = new ClipState();

    public Surface(int width, int height) {
        this(width, height, 8);
    }

    public Surface(int width, int height, int multiSamples) {
        this.width = width;
        this.height = height;
        this.multiSamples = Math.max(0, Math.min(8, multiSamples));
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMultiSamples() {
        return multiSamples;
    }

    FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    ClipState getClipState() {
        return clipState;
    }

    Texture2D renderToTexture(Graphics graphics, int x, int y, int width, int height, Texture2D textureTransfer) {
        checkDisposed();

        if (frameBuffer == null) {
            return null;
        }
        frameBuffer.attach(LayerTarget.COLOR_0, textureTransfer, 0);
        frameBuffer.detach(LayerTarget.DEPTH_STENCIL);
        if (multiSamples > 0) {
            graphics.bakeSurface(x, y, textureMultisamples);
            frameBuffer.attach(LayerTarget.COLOR_0, textureMultisamples, 0);
        } else {
            graphics.bakeSurface(x, y, texture);
            frameBuffer.attach(LayerTarget.COLOR_0, texture, 0);
        }
        frameBuffer.attach(LayerTarget.DEPTH_STENCIL, render);

        return textureTransfer;
    }

    protected void checkDisposed() {
        if (isDisposed()) {
            throw new FlatException("The Surface is disposed");
        }
    }

    public void dispose() {
        if (!disposed) {
            disposed = true;
            if (textureMultisamples != null) {
                textureMultisamples.dispose();
            }
            if (texture != null) {
                texture.dispose();
            }
            if (render != null) {
                render.dispose();
            }
            if (frameBuffer != null) {
                frameBuffer.dispose();
            }
            if (frameBufferTransfer != null) {
                frameBufferTransfer.dispose();
            }
        }
    }

    public boolean isDisposed() {
        return disposed;
    }

    void begin(Graphics graphics) {
        checkDisposed();

        Context context = graphics.getContext();

        if (frameBuffer != null && frameBuffer.getContext() != context) {
            throw new FlatException("A surface cannot be reused in different contexts");
        }

        if (frameBuffer == null) {
            frameBuffer = new FrameBuffer(context);
        }
        if (multiSamples > 0 && textureMultisamples == null) {
            textureMultisamples = new TextureMultisample2D(width, height, multiSamples, PixelFormat.RGBA16F);
            textureMultisamples.setWrapModes(WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE);
        }
        if (multiSamples <= 0 && texture == null) {
            texture = new Texture2D(width, height, PixelFormat.RGBA16F);
            texture.setWrapModes(WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE);
        }
        if (render == null) {
            render = new Render(context, width, height, multiSamples, PixelFormat.DEPTH24_STENCIL8);
        }

        context.setFrameBuffer(frameBuffer);
        if (multiSamples > 0) {
            frameBuffer.attach(LayerTarget.COLOR_0, textureMultisamples, 0);
        } else {
            frameBuffer.attach(LayerTarget.COLOR_0, texture, 0);
        }
        frameBuffer.attach(LayerTarget.DEPTH_STENCIL, render);
    }
}
