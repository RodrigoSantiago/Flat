package flat.graphics.mesh;

import flat.graphics.context.BufferObject;
import flat.graphics.context.Context;
import flat.graphics.context.VertexArray;
import flat.graphics.context.enums.AttributeType;
import flat.graphics.context.enums.BufferType;
import flat.graphics.context.enums.UsageType;

public class VertexData {

    private VertexArray vertexArray;
    private BufferObject vertices;
    private BufferObject elements;

    private boolean elementMode;

    public VertexData(Context context) {
        vertexArray = new VertexArray(context);
        vertices = new BufferObject(context);
        elements = new BufferObject(context);
    }

    public void setVertexSize(int size) {
        vertices.begin(BufferType.Array);
        vertices.setSize(size, UsageType.STATIC_DRAW);
        vertices.end();
    }

    public void setElementsSize(int size) {
        elements.begin(BufferType.Element);
        elements.setSize(size, UsageType.STATIC_DRAW);
        elements.end();
    }

    public void enableAttribute(int att, AttributeType type, int stride, int offset) {
        vertexArray.begin();
        vertices.begin(BufferType.Array);
        vertexArray.setAttributeEnabled(att, true);
        vertexArray.setAttributePointer(att, type.getSize(), AttributeType.FLOAT, false, stride, offset);
        vertices.end();
        vertexArray.end();
    }

    public void disableAttribute(int att) {
        vertexArray.begin();
        vertexArray.setAttributeEnabled(att, false);
        vertexArray.end();
    }

    public void setVertices(int dataOffset, float[] data) {
        setVertices(dataOffset, data, 0 , data.length);
    }

    public void setVertices(int dataOffset, float[] data, int offset, int length) {
        vertices.begin(BufferType.Array);
        vertices.setData(dataOffset, data, offset, length);
        vertices.end();
    }

    public void setElements(int dataOffset, int[] data) {
        setElements(dataOffset, data, 0 , data.length);
    }

    public void setElements(int dataOffset, int[] data, int offset, int length) {
        elements.begin(BufferType.Element);
        elements.setData(dataOffset, data, offset, length);
        elements.end();
    }

    public void setElementMode(boolean elementMode) {
        this.elementMode = elementMode;
    }

    public boolean isElementMode() {
        return elementMode;
    }

    public VertexArray getVertexArray() {
        return vertexArray;
    }

    public BufferObject getElements() {
        return elements;
    }

    public BufferObject getVertices() {
        return vertices;
    }
}
