package flat.graphics.context;

import flat.backend.GL;
import flat.graphics.context.enuns.AttributeType;

public final class VertexArray extends ContextObject {

    private int vertexArrayId;
    private Context context;
    private BufferObejct elementBuffer;

    public VertexArray(Context context) {
        this.context = context;
        init();
    }

    @Override
    protected void onInitialize() {
        this.vertexArrayId = GL.VertexArrayCreate();
    }

    @Override
    protected void onDispose() {
        GL.VertexArrayDestroy(vertexArrayId);
    }

    int getInternalID() {
        return vertexArrayId;
    }

    void setElementBuffer(BufferObejct elementBuffer) {
        this.elementBuffer = elementBuffer;
    }

    BufferObejct getElementBuffer() {
        return elementBuffer;
    }

    public void begin() {
        context.refreshBufferBinds();
        context.bindVertexArray(this);
    }

    public void end() {
        context.refreshBufferBinds();
        context.unbindVertexArray();
    }

    public void setAttributeEnabled(int att, boolean enabled) {
        context.refreshBufferBinds();
        GL.VertexArrayAttribEnable(att, enabled);
    }

    public void setAttributePointer(int att, boolean normalized, int size, int stride, AttributeType type, int offset) {
        context.refreshBufferBinds();
        GL.VertexArrayAttribPointer(att, size, normalized, stride, type.getInternalEnum(), offset);
    }

    public void setAttributeDivisor(int att, int instanceCount) {
        context.refreshBufferBinds();
        GL.VertexArrayAttribSetDivisor(att, instanceCount);
    }
}
