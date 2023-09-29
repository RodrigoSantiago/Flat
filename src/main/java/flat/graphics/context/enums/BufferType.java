package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum BufferType {
    Array(GLEnums.BB_ARRAY_BUFFER),
    Element(GLEnums.BB_ELEMENT_ARRAY_BUFFER),
    PixelPack(GLEnums.BB_PIXEL_PACK_BUFFER),
    PixelUnpack(GLEnums.BB_PIXEL_UNPACK_BUFFER),
    Uniform(GLEnums.BB_UNIFORM_BUFFER),
    TransformFeedback(GLEnums.BB_TRANSFORM_FEEDBACK_BUFFER),
    CopyRead(GLEnums.BB_COPY_READ_BUFFER),
    CopyWrite(GLEnums.BB_COPY_WRITE_BUFFER);

    private final int glEnum;

    BufferType(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
