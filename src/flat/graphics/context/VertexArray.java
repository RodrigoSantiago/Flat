package flat.graphics.context;

import flat.backend.GL;

public class VertexArray extends ContextObject {

    private int vertexArrayId;

    public VertexArray() {
        super();
    }

    @Override
    protected void onInitialize() {
        final int vertexArrayId = GL.VertexArrayCreate();

        setDispose(() -> GL.VertexArrayDestroy(vertexArrayId));

        this.vertexArrayId = vertexArrayId;
    }

    int getInternalID() {
        init();
        return vertexArrayId;
    }

    public void begin() {
        init();
        Context.getContext().bindVertexArray(this);
    }

    public void end() {
        Context.getContext().bindVertexArray(null);
    }

    public void setAttributeEnabled(int att, boolean enabled) {
        GL.VertexArrayAttribEnable(att, enabled);
    }

    public void setAttributePointer(int att, boolean normalized, int size, int stride, int type, int offset) {
        GL.VertexArrayAttribPointer(att, size, normalized, stride, type, offset);
    }

    public void setAttributeDivisor(int att, int instanceCount) {
        GL.VertexArrayAttribSetDivisor(att, instanceCount);
    }
}
