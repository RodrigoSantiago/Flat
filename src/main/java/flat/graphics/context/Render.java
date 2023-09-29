package flat.graphics.context;

import flat.backend.GL;
import flat.graphics.context.enums.PixelFormat;

public final class Render extends ContextObject {

    private final int renderBufferId;
    private PixelFormat format;

    private int width, height;

    public Render(Context context) {
        super(context);
        final int renderBufferId = GL.RenderBufferCreate();
        this.renderBufferId = renderBufferId;
        assignDispose(() -> GL.RenderBufferDestroy(renderBufferId));
    }

    @Override
    protected boolean isBound() {
        return getContext().isRenderBound(this);
    }

    int getInternalID() {
        return renderBufferId;
    }

    public void begin() {
        getContext().bindRender(this);
    }

    public void end() {
        getContext().unbindRender();
    }

    public void setSize(int width, int height, PixelFormat format) {
        boundCheck();

        this.width = width;
        this.height = height;
        this.format = format;
        GL.RenderBufferStorage(format.getInternalEnum(), width, height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public PixelFormat getFormat() {
        return format;
    }
}
