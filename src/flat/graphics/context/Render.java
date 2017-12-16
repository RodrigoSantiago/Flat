package flat.graphics.context;

import flat.backend.GL;
import flat.graphics.context.enuns.TextureFormat;

public class Render extends ContextObject {

    private int width, height;
    TextureFormat format;
    private int renderBufferId;

    public Render() {
        super();
    }

    @Override
    protected void onInitialize() {
        Context.getContext();
        final int renderBufferId = GL.RenderBufferCreate();

        setDispose(() -> GL.RenderBufferDestroy(renderBufferId));

        this.renderBufferId = renderBufferId;
    }

    int getInternalID() {
        init();
        return renderBufferId;
    }

    public void begin() {
        init();
        Context.getContext().bindRender(this);
    }

    public void end() {
        Context.getContext().bindRender(null);
    }

    public void setData(TextureFormat format, int width, int height) {
        this.format = format;
        this.width = width;
        this.height = height;
        GL.RenderBufferStorage(format.getInternalEnum(), width, height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public TextureFormat getFormat() {
        return format;
    }
}
