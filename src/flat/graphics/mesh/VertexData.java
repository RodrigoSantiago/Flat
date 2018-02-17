package flat.graphics.mesh;

import flat.graphics.context.BufferObejct;
import flat.graphics.context.VertexArray;
import flat.graphics.context.enuns.AttributeType;
import flat.graphics.context.enuns.BufferType;
import flat.graphics.context.enuns.UsageType;
import flat.application.Application;

public class VertexData {

    private VertexArray vertexArray;
    private BufferObejct vertices;
    private BufferObejct elements;

    private boolean elementMode;

    public VertexData() {
        vertexArray = new VertexArray(Application.getCurrentContext());
        vertices = new BufferObejct();
        elements = new BufferObejct();
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
        vertexArray.setAttributePointer(att, false, type.getSize(), stride, AttributeType.FLOAT.getInternalEnum(), offset);
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

    public BufferObejct getElements() {
        return elements;
    }

    public BufferObejct getVertices() {
        return vertices;
    }
}
