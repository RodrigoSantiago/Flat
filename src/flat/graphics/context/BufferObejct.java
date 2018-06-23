package flat.graphics.context;

import flat.backend.GL;
import flat.graphics.context.enuns.BufferType;
import flat.graphics.context.enuns.UsageType;
import flat.widget.Application;

import java.nio.Buffer;

public final class BufferObejct extends ContextObject {

    private int bufferId;

    private int size;
    private BufferType type;
    private UsageType usageType;

    public BufferObejct() {
        init();
    }

    @Override
    protected void onInitialize() {
        this.bufferId = GL.BufferCreate();
    }

    @Override
    protected void onDispose() {
        GL.BufferDestroy(bufferId);
    }

    int getInternalID() {
        return bufferId;
    }

    public void begin(BufferType bufferType) {
        Application.getCurrentContext().bindBuffer(this, bufferType);
    }

    public void end() {
        Application.getCurrentContext().unbindBuffer(type);
    }

    void setBindType(BufferType type) {
        this.type = type;
    }

    public void setSize(int size, UsageType usageType) {
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
        GL.BufferSubDataB(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void getData(int internalOffset, byte[] data, int offset, int length) {
        GL.BufferReadDataB(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void setData(int internalOffset, int[] data, int offset, int length) {
        GL.BufferSubDataI(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void getData(int internalOffset, int[] data, int offset, int length) {
        GL.BufferReadDataI(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void setData(int internalOffset, float[] data, int offset, int length) {
        GL.BufferSubDataF(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void getData(int internalOffset, float[] data, int offset, int length) {
        GL.BufferReadDataF(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void setData(int internalOffset, Buffer data, int offset, int length) {
        GL.BufferSubDataBuffer(type.getInternalEnum(), data, offset, length, internalOffset);
    }

    public void getData(int internalOffset, Buffer data, int offset, int length) {
        GL.BufferReadDataBuffer(type.getInternalEnum(), data, offset, length, internalOffset);
    }
}
