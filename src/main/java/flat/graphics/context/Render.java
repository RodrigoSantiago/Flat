package flat.graphics.context;

import flat.backend.GL;
import flat.exception.FlatException;
import flat.graphics.context.enums.PixelFormat;

public final class Render extends ContextObject {

    private final int renderBufferId;
    private PixelFormat format;

    private int width, height, samples;

    public Render(Context context, int width, int height, int samples, PixelFormat format) {
        super(context);
        final int renderBufferId = GL.RenderBufferCreate();
        if (renderBufferId == 0) {
            throw new FlatException("Unable to create a new OpenGL RenderBuffer");
        }

        this.renderBufferId = renderBufferId;
        assignDispose(() -> GL.RenderBufferDestroy(renderBufferId));
        setSize(width, height, samples, format);
    }

    int getInternalId() {
        return renderBufferId;
    }

    private void boundCheck() {
        if (isDisposed()) {
            throw new FlatException("The Render is disposed");
        }
        getContext().bindRender(this);
    }

    public void setSize(int width, int height, int samples, PixelFormat format) {
        dataBoundsCheck(width, height, samples);
        boundCheck();

        this.width = width;
        this.height = height;
        this.samples = samples;
        this.format = format;
        if (samples <= 0) {
            GL.RenderBufferStorage(format.getInternalEnum(), width, height);
        } else {
            GL.RenderBufferStorageMultsample(format.getInternalEnum(), samples, width, height);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSamples() {
        return samples;
    }

    public PixelFormat getFormat() {
        return format;
    }

    private void dataBoundsCheck(int width, int height, int samples) {
        if (width <= 0 || height <= 0 || samples < 0) {
            throw new FlatException("Zero or negative values are not allowed (" + width + ", " + height + ", " + samples + ")");
        }
    }
}
