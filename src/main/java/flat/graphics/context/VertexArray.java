package flat.graphics.context;

import flat.backend.GL;
import flat.exception.FlatException;
import flat.graphics.context.enums.AttributeType;
import flat.graphics.context.enums.BufferType;
import flat.graphics.context.enums.VertexMode;

public final class VertexArray extends ContextObject {

    private final int vertexArrayId;

    private BufferObject elementBuffer;
    private final BufferObject[] arrayBuffers = new BufferObject[16];

    public VertexArray(Context context) {
        super(context);
        final int vertexArrayId = GL.VertexArrayCreate();
        this.vertexArrayId = vertexArrayId;
        assignDispose(() -> GL.VertexArrayDestroy(vertexArrayId));
    }

    int getInternalId() {
        return vertexArrayId;
    }

    private void boundCheck() {
        if (isDisposed()) {
            throw new FlatException("The VertexArray is disposed");
        }
        if (elementBuffer != null && elementBuffer.isDisposed()) {
            throw new FlatException("The ElementBuffer is disposed");
        }
        for (BufferObject arrayBuffer : arrayBuffers) {
            if (arrayBuffer != null && arrayBuffer.isDisposed()) {
                throw new FlatException("The ArrayBuffer is disposed");
            }
        }
    }

    public void setElements(BufferObject elementBuffer) {
        boundCheck();
        if (elementBuffer != null && elementBuffer.getType() != BufferType.Element) {
            throw new FlatException("The buffer should be Element type");
        }
        if (elementBuffer != null && elementBuffer.isDisposed()) {
            throw new FlatException("The ElementBuffer is disposed");
        }

        getContext().bindVertexArray(this);
        getContext().bindBuffer(elementBuffer, BufferType.Element);
        getContext().bindVertexArray(null);
        getContext().bindBuffer(null, BufferType.Element);
        this.elementBuffer = elementBuffer;
    }

    public void setAttributeEnabled(BufferObject arrayBuffer, int att, int size, AttributeType type, boolean normalized, int stride, int offset) {
        boundCheck();
        if (arrayBuffer.getType() != BufferType.Array) {
            throw new FlatException("The buffer should be Array type");
        }
        if (arrayBuffer.isDisposed()) {
            throw new FlatException("The Arraybuffer is disposed");
        }

        getContext().bindVertexArray(this);
        getContext().bindBuffer(arrayBuffer, BufferType.Array);
        GL.VertexArrayAttribEnable(att, true);
        GL.VertexArrayAttribPointer(att, size, normalized, stride, type.getInternalEnum(), offset);
        getContext().bindVertexArray(null);
        getContext().bindBuffer(null, BufferType.Array);
        arrayBuffers[att] = arrayBuffer;
    }

    public void setAttributeDisabled(int att) {
        boundCheck();

        getContext().bindVertexArray(this);
        GL.VertexArrayAttribEnable(att, false);
        getContext().bindVertexArray(null);
        arrayBuffers[att] = null;
    }

    public void setAttributeDivisor(int att, int instanceCount) {
        boundCheck();

        getContext().bindVertexArray(this);
        GL.VertexArrayAttribSetDivisor(att, instanceCount);
        getContext().bindVertexArray(null);
    }

    public void drawArray(VertexMode vertexMode, int first, int count, int instances) {
        boundCheck();

        getContext().bindVertexArray(this);
        getContext().drawArray(vertexMode, first, count, instances);
        getContext().bindVertexArray(null);
    }

    public void drawElements(VertexMode vertexMode, int first, int count, int instances) {
        boundCheck();

        getContext().bindVertexArray(this);
        getContext().drawElements(vertexMode, first, count, instances);
        getContext().bindVertexArray(null);
    }
}
