package flat.graphics;

import flat.backend.GL;
import static flat.backend.GLEnuns.*;

public class VertexArray extends ContextObject {

    public int vertexArrayId, arrayBufferId, elementsArrayId;
    private boolean elementMode;

    public VertexArray() {
        Context.getContext();

        vertexArrayId = GL.VertexArrayCreate();
        arrayBufferId = GL.BufferCreate();
        elementsArrayId = GL.BufferCreate();

        int vao = GL.VertexArrayGetBound();
        GL.VertexArrayBind(vertexArrayId);
        GL.BufferBind(BB_ELEMENT_ARRAY_BUFFER, elementsArrayId);
        GL.VertexArrayBind(vao);
    }

    public void setData(int bytes) {
        int buf = GL.BufferGetBound(BB_ARRAY_BUFFER);

        GL.BufferBind(BB_ARRAY_BUFFER, arrayBufferId);
        GL.BufferDataBuffer(BB_ARRAY_BUFFER, null, 0, bytes, UT_STATIC_DRAW);

        GL.BufferBind(BB_ARRAY_BUFFER, buf);
    }

    public void setData(float[] data) {
        int buf = GL.BufferGetBound(BB_ARRAY_BUFFER);

        GL.BufferBind(BB_ARRAY_BUFFER, arrayBufferId);
        GL.BufferDataF(BB_ARRAY_BUFFER, data, 0, data.length, UT_STATIC_DRAW);

        GL.BufferBind(BB_ARRAY_BUFFER, buf);
    }

    public void setSubData(float[] newData, int offset) {
        int buf = GL.BufferGetBound(BB_ARRAY_BUFFER);

        GL.BufferBind(BB_ARRAY_BUFFER, arrayBufferId);
        GL.BufferSubDataF(BB_ARRAY_BUFFER, newData, 0, newData.length, offset);

        GL.BufferBind(BB_ARRAY_BUFFER, buf);
    }

    public void setElements(int[] elements) {
        int buf = GL.BufferGetBound(BB_ELEMENT_ARRAY_BUFFER);
        GL.BufferBind(BB_ELEMENT_ARRAY_BUFFER, elementsArrayId);

        if (elements == null) {
            elementMode = false;
            GL.BufferDataBuffer(BB_ELEMENT_ARRAY_BUFFER, null, 0, 0, UT_STATIC_DRAW);
        } else {
            elementMode = true;
            GL.BufferDataI(BB_ELEMENT_ARRAY_BUFFER, elements, 0, elements.length, UT_STATIC_DRAW);
        }

        GL.BufferBind(BB_ELEMENT_ARRAY_BUFFER, buf);
    }

    public void setAttributes(int attributeId, int size, int stride, int offset) {
        int vao = GL.VertexArrayGetBound();
        int arrB = GL.BufferGetBound(BB_ARRAY_BUFFER);

        GL.VertexArrayBind(vertexArrayId);
        GL.BufferBind(BB_ARRAY_BUFFER, arrayBufferId);

        GL.VertexArrayAttribPointer(attributeId, size, false, stride * 4, DT_FLOAT, offset * 4);
        GL.VertexArrayAttribEnable(attributeId, true);

        GL.BufferBind(BB_ARRAY_BUFFER, arrB);
        GL.VertexArrayBind(vao);
    }

    public void setAttributeDivisor(int attributeId, int instanceCount) {
        int vao = GL.VertexArrayGetBound();
        GL.VertexArrayBind(vertexArrayId);
        GL.VertexArrayAttribSetDivisor(attributeId, instanceCount);
        GL.VertexArrayBind(vao);
    }

    public boolean isElementMode() {
        return elementMode;
    }

    @Override
    protected void onDispose() {
        GL.BufferDestroy(elementsArrayId);
        GL.BufferDestroy(arrayBufferId);
        GL.VertexArrayDestroy(vertexArrayId);
    }
}
