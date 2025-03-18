package flat.graphics.context;

import flat.backend.GL;
import flat.backend.GLEnums;
import flat.exception.FlatException;
import flat.graphics.context.enums.AttributeType;
import flat.graphics.context.enums.BufferType;
import flat.graphics.context.enums.VertexMode;

import java.util.ArrayList;
import java.util.List;

public final class VertexArray extends ContextObject {

    private final int vertexArrayId;

    private BufferObject elementBuffer;
    private BufferObject[] arrayBuffers = new BufferObject[16];

    public VertexArray(Context context) {
        super(context);
        final int vertexArrayId = GL.VertexArrayCreate();
        this.vertexArrayId = vertexArrayId;
        assignDispose(() -> GL.VertexArrayDestroy(vertexArrayId));
    }

    int getInternalID() {
        return vertexArrayId;
    }

    private void boundCheck() {
        if (isDisposed()) {
            throw new FlatException("The VertexArray is disposed.");
        }
    }

    public void setElements(BufferObject elementBuffer) {
        boundCheck();
        if (elementBuffer != null && elementBuffer.getType() != BufferType.Element) {
            throw new FlatException("The buffer should be Element type");
        }

        getContext().bindVertexArray(this);
        getContext().bindBuffer(elementBuffer, BufferType.Element);
        getContext().bindVertexArray(null);
        getContext().bindBuffer(null, BufferType.Element);
        this.elementBuffer = elementBuffer;
    }

    public void setAttributeEnabled(BufferObject buffer, int att, int size, AttributeType type, boolean normalized, int stride, int offset) {
        boundCheck();
        if (buffer.getType() != BufferType.Array) {
            throw new FlatException("The buffer should be Array type");
        }

        getContext().bindVertexArray(this);
        getContext().bindBuffer(buffer, BufferType.Array);
        GL.VertexArrayAttribEnable(att, true);
        GL.VertexArrayAttribPointer(att, size, normalized, stride, type.getInternalEnum(), offset);
        getContext().bindVertexArray(null);
        getContext().bindBuffer(null, BufferType.Array);
        arrayBuffers[att] = buffer;
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
