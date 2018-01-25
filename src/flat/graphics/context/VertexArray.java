package flat.graphics.context;

import flat.backend.GL;

public class VertexArray extends ContextObject {

    private int vertexArrayId;
    private Context context;

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

    public void begin() {
        context.bindVertexArray(this);
    }

    public void end() {
        context.unbindVertexArray();
    }

    public void setAttributeEnabled(int att, boolean enabled) {
        context.refreshBufferBinds();
        GL.VertexArrayAttribEnable(att, enabled);
    }

    public void setAttributePointer(int att, boolean normalized, int size, int stride, int type, int offset) {
        context.refreshBufferBinds();
        GL.VertexArrayAttribPointer(att, size, normalized, stride, type, offset);
    }

    public void setAttributeDivisor(int att, int instanceCount) {
        context.refreshBufferBinds();
        GL.VertexArrayAttribSetDivisor(att, instanceCount);
    }
}
