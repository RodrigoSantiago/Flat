package flat.graphics;

import flat.backend.GL;
import static flat.backend.GLEnuns.*;

public class VertexArray extends ContextObject {

    int vertexArrayId, arrayBufferId;

    public VertexArray() {
        Context.getContext();

        vertexArrayId = GL.VertexArrayCreate();
        arrayBufferId = GL.BufferCreate();
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

    public void setAttributes(int attributeId, int size, int stride, int offset) {
        int buf = GL.BufferGetBound(BB_ARRAY_BUFFER);
        int vao = GL.VertexArrayGetBound();
        GL.VertexArrayBind(vertexArrayId);
        GL.BufferBind(BB_ARRAY_BUFFER, arrayBufferId);

        GL.VertexArrayAttribPointer(attributeId, size, false, stride * 4, DT_FLOAT, offset * 4);
        GL.VertexArrayAttribEnable(attributeId, true);

        GL.BufferBind(BB_ARRAY_BUFFER, buf);
        GL.VertexArrayBind(vao);
    }

    @Override
    protected void onDispose() {
        GL.VertexArrayDestroy(vertexArrayId);
        GL.BufferDestroy(arrayBufferId);
    }
}
