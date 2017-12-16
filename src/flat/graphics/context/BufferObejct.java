package flat.graphics.context;

import flat.backend.GL;
import static flat.backend.GLEnuns.*;
import flat.graphics.context.enuns.BufferType;

import java.nio.Buffer;

public class BufferObejct extends ContextObject {

    private int bufferId;

    private BufferType type;
    private int size;

    public BufferObejct() {
        super();
    }

    @Override
    protected void onInitialize() {
        Context.getContext();
        final int bufferId = GL.BufferCreate();

        setDispose(() -> GL.BufferDestroy(bufferId));

        this.bufferId = bufferId;
    }

    int getInternalID() {
        init();
        return bufferId;
    }

    public void begin(BufferType bufferType) {
        init();
        Context.getContext().bindBuffer(this, bufferType);
    }

    public void end() {
        Context.getContext().bindBuffer(null, type);
    }

    void setBindType(BufferType type) {
        this.type = type;
    }

    public void setSize(int size) {
        GL.BufferDataBuffer(type.getInternalEnum(), null, 0, this.size = size, UT_STATIC_DRAW);
    }

    public int getSize() {
        return this.size;
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
