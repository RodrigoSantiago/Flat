package flat.graphics.context.objects;

import flat.graphics.context.ContextObject;

public class VertexArray extends ContextObject {

    private final DataBuffer arrayBuffer;
    private final DataBuffer elementBuffer;
    private int internalID;

    public VertexArray() {
        this(null);
    }

    public VertexArray(DataBuffer arrayBuffer) {
        this(arrayBuffer, null);
    }

    public VertexArray(DataBuffer arrayBuffer, DataBuffer elementBuffer) {
        this.arrayBuffer = arrayBuffer == null ? new DataBuffer() : arrayBuffer;
        this.elementBuffer = elementBuffer == null ? new DataBuffer() : elementBuffer;
    }

    public void bind() {

    }

    public DataBuffer getArrayBuffer() {
        return null;
    }

    public DataBuffer getElementBuffer() {
        return null;
    }

    public void setAttribute(int att, int size, int stride, int offset) {

    }

    public void setAttributeDivisor(int att, int instanceCount) {

    }

    public void setElementMode(boolean elementMode) {

    }

    public boolean isElementMode() {
        return false;
    }

    @Override
    protected void onDispose() {

    }

    public int getInternalID() {
        return internalID;
    }
}
