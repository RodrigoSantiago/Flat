package flat.graphics.context.enuns;

import flat.backend.GLEnuns;

public enum BufferType {
    Array(GLEnuns.BB_ARRAY_BUFFER),
    Element(GLEnuns.BB_ELEMENT_ARRAY_BUFFER),
    PixelPack(GLEnuns.BB_PIXEL_PACK_BUFFER),
    PixelUnpack(GLEnuns.BB_PIXEL_UNPACK_BUFFER),
    Uniform(GLEnuns.BB_UNIFORM_BUFFER),
    TransformFeedback(GLEnuns.BB_TRANSFORM_FEEDBACK_BUFFER),
    CopyRead(GLEnuns.BB_COPY_READ_BUFFER),
    CopyWrite(GLEnuns.BB_COPY_WRITE_BUFFER);

    private final int glEnum;

    BufferType(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
