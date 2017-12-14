package flat.graphics.context.objects;

import flat.graphics.context.enuns.BufferType;

import java.nio.Buffer;

public class DataBuffer {

    private int internalID;
    private BufferType internalType;

    public DataBuffer() {

    }

    public void bind() {

    }

    public void bind(BufferType bufferType) {

    }

    public void setSize(int size) {

    }

    public BufferType getType() {
        return null;
    }

    public int getSize() {
        return 0;
    }

    public void setData(int internalOffset, byte[] data, int offset, int length) {

    }

    public void getData(int internalOffset, byte[] data, int offset, int length) {

    }

    public void setData(int internalOffset, int[] data, int offset, int length) {

    }

    public void getData(int internalOffset, int[] data, int offset, int length) {

    }

    public void setData(int internalOffset, float[] data, int offset, int length) {

    }

    public void getData(int internalOffset, float[] data, int offset, int length) {

    }

    public void setData(int internalOffset, Buffer data, int offset, int length) {

    }

    public void getData(int internalOffset, Buffer data, int offset, int length) {

    }

    public int getInternalType() {
        return 0;
    }

    public int getInternalID() {
        return internalID;
    }

    public void setInternalType(BufferType internalType) {
        this.internalType = internalType;
    }
}
