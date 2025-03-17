package flat.graphics.context;

import flat.backend.GL;
import flat.graphics.context.enums.AttributeType;
import flat.graphics.context.enums.BufferType;

import java.util.ArrayList;
import java.util.List;

public final class VertexArray extends ContextObject {

    private int vertexArrayId;
    private BufferObject elementBuffer;
    private BufferObject[] arrayBuffers = new BufferObject[16];

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

    void addArrayBuffer(int att, BufferObject arrayBuffer) {
        arrayBuffers[att] = arrayBuffer;
    }

    public void begin() {
        getContext().bindVertexArray(this);
    }

    public void end() {
        getContext().unbindVertexArray();
    }

    public void setAttributeEnabled(int att, boolean enabled) {
        boundCheck();

        if (!enabled) {
            arrayBuffers[att] = null;
        }
        GL.VertexArrayAttribEnable(att, enabled);
    }

    public void setAttributePointer(int att, int size, AttributeType type, boolean normalized, int stride, int offset) {
        boundCheck();

        arrayBuffers[att] = getContext().getBoundBuffer(BufferType.Array);
        GL.VertexArrayAttribPointer(att, size, normalized, stride, type.getInternalEnum(), offset);
    }

    public void setAttributeDivisor(int att, int instanceCount) {
        boundCheck();

        GL.VertexArrayAttribSetDivisor(att, instanceCount);
    }
}
