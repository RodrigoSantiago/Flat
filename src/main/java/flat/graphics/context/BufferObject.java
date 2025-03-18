package flat.graphics.context;

import flat.backend.GL;
import flat.exception.FlatException;
import flat.graphics.context.enums.BufferType;
import flat.graphics.context.enums.UsageType;
import flat.window.Application;

import java.nio.Buffer;

public final class BufferObject extends ContextObject {

    private final int bufferId;
    private final BufferType type;

    private int size;
    private UsageType usageType;

    public BufferObject(Context context, BufferType type, int size, UsageType usageType) {
        super(context);

        final int bufferId = GL.BufferCreate();

        this.bufferId = bufferId;
        assignDispose(() -> GL.BufferDestroy(bufferId));

        this.type = type;
        setSize(size, usageType);
    }

    int getInternalID() {
        return bufferId;
    }

    private void boundCheck() {
        if (isDisposed()) {
            throw new FlatException("The Buffer is disposed.");
        }
        getContext().bindBuffer(this, type);
    }

    public void setSize(int size, UsageType usageType) {
        parameterSizeCheck(size);
        boundCheck();

        this.usageType = usageType;
        this.size = size;
        GL.BufferDataBuffer(type.getInternalEnum(), null, 0, size, usageType.getGlEnum());
    }

    public int getSize() {
        return this.size;
    }

    public BufferType getType() {
        return type;
    }

    public UsageType getUsageType() {
        return usageType;
    }

    public void setData(int internalOffset, byte[] data, int offset, int length) {
        dataBoundsCheck(data.length - offset, 1, internalOffset, length);
        boundCheck();

        GL.BufferSubDataB(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void getData(int internalOffset, byte[] data, int offset, int length) {
        dataBoundsCheck(data.length - offset, 1, internalOffset, length);
        boundCheck();

        GL.BufferReadDataB(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void setData(int internalOffset, int[] data, int offset, int length) {
        dataBoundsCheck(data.length - offset, 4, internalOffset, length);
        boundCheck();

        GL.BufferSubDataI(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void getData(int internalOffset, int[] data, int offset, int length) {
        dataBoundsCheck(data.length - offset, 4, internalOffset, length);
        boundCheck();

        GL.BufferReadDataI(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void setData(int internalOffset, float[] data, int offset, int length) {
        dataBoundsCheck(data.length - offset, 4, internalOffset, length);
        boundCheck();

        GL.BufferSubDataF(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void getData(int internalOffset, float[] data, int offset, int length) {
        dataBoundsCheck(data.length - offset, 4, internalOffset, length);
        boundCheck();

        GL.BufferReadDataF(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void setData(int internalOffset, Buffer data, int offset, int length) {
        dataBoundsCheck(data.capacity() - offset, 1, internalOffset, length);
        boundCheck();

        GL.BufferSubDataBuffer(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void getData(int internalOffset, Buffer data, int offset, int length) {
        dataBoundsCheck(data.capacity() - offset, 1, internalOffset, length);
        boundCheck();

        GL.BufferReadDataBuffer(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    private void parameterSizeCheck(int size) {
        if (size <= 0) {
            throw new FlatException("Zero or negative values are not allowed (" + size + ")");
        }
    }

    private void dataBoundsCheck(int arrayLen, int bytes, int offset, int length) {
        if (offset < 0 || length <= 0 || offset + length * bytes > size) {
            throw new FlatException("The size are out of bounds (" + offset + ", " + length * bytes + " bytes)");
        }

        int required = length * bytes;
        if (arrayLen * bytes < required) {
            throw new FlatException("The array is too short. Provided : " + arrayLen + ". Required : " + ((required - 1) / bytes + 1));
        }
    }
}
