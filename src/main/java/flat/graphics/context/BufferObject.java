package flat.graphics.context;

import flat.backend.GL;
import flat.graphics.context.enums.BufferType;
import flat.graphics.context.enums.UsageType;
import flat.window.Application;

import java.nio.Buffer;

public final class BufferObject extends ContextObject {

    private final int bufferId;

    private int size;
    private BufferType type;
    private UsageType usageType;

    public BufferObject(Context context) {
        super(context);

        final int bufferId = GL.BufferCreate();

        this.bufferId = bufferId;
        assignDispose(() -> GL.BufferDestroy(bufferId));
    }

    int getInternalID() {
        return bufferId;
    }

    @Override
    protected boolean isBound() {
        return getContext().indexOfBufferBound(this) != -1;
    }

    public void begin(BufferType bufferType) {
        getContext().bindBuffer(this, bufferType);
    }

    public void end() {
        getContext().unbindBuffer(type);
    }

    void setBindType(BufferType type) {
        this.type = type;
    }

    public void setSize(int size, UsageType usageType) {
        boundCheck();

        this.usageType = usageType;
        this.size = size;
        GL.BufferDataBuffer(type.getInternalEnum(), null, 0, size, usageType.getGlEnum());
    }

    public int getSize() {
        return this.size;
    }

    public UsageType getUsageType() {
        return usageType;
    }

    public void setData(int internalOffset, byte[] data, int offset, int length) {
        boundCheck();

        GL.BufferSubDataB(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void getData(int internalOffset, byte[] data, int offset, int length) {
        boundCheck();

        GL.BufferReadDataB(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void setData(int internalOffset, int[] data, int offset, int length) {
        boundCheck();

        GL.BufferSubDataI(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void getData(int internalOffset, int[] data, int offset, int length) {
        boundCheck();

        GL.BufferReadDataI(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void setData(int internalOffset, float[] data, int offset, int length) {
        boundCheck();

        GL.BufferSubDataF(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void getData(int internalOffset, float[] data, int offset, int length) {
        boundCheck();

        GL.BufferReadDataF(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void setData(int internalOffset, Buffer data, int offset, int length) {
        boundCheck();

        GL.BufferSubDataBuffer(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void getData(int internalOffset, Buffer data, int offset, int length) {
        boundCheck();

        GL.BufferReadDataBuffer(type.getInternalEnum(), data, offset, length, internalOffset);
    }
}
