package flat.graphics.context;

import flat.backend.GL;
import flat.graphics.context.enums.AttributeType;

public final class VertexArray extends ContextObject {

    private int vertexArrayId;
    private BufferObject elementBuffer;

    public VertexArray(Context context) {
        super(context);
        final int vertexArrayId = GL.VertexArrayCreate();
        this.vertexArrayId = vertexArrayId;
        assignDispose(() -> GL.VertexArrayDestroy(vertexArrayId));
    }

    @Override
    protected boolean isBound() {
        return getContext().isVertexArrayBound(this);
    }

    int getInternalID() {
        return vertexArrayId;
    }

    void setElementBuffer(BufferObject elementBuffer) {
        this.elementBuffer = elementBuffer;
    }

    BufferObject getElementBuffer() {
        return elementBuffer;
    }

    public void begin() {
        getContext().bindVertexArray(this);
    }

    public void end() {
        getContext().unbindVertexArray();
    }

    public void setAttributeEnabled(int att, boolean enabled) {
        boundCheck();

        GL.VertexArrayAttribEnable(att, enabled);
    }

    public void setAttributePointer(int att, boolean normalized, int size, int stride, AttributeType type, int offset) {
        boundCheck();

        GL.VertexArrayAttribPointer(att, size, normalized, stride, type.getInternalEnum(), offset);
    }

    public void setAttributeDivisor(int att, int instanceCount) {
        boundCheck();

        GL.VertexArrayAttribSetDivisor(att, instanceCount);
    }
}
