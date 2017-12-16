package flat.graphics.context.enuns;

import static flat.backend.GLEnuns.*;

public enum BufferType {
    Array(BB_ARRAY_BUFFER),
    Element(BB_ELEMENT_ARRAY_BUFFER),
    PixelPack(BB_PIXEL_PACK_BUFFER),
    PixelUnpack(BB_PIXEL_UNPACK_BUFFER),
    Uniform(BB_UNIFORM_BUFFER),
    TransformFeedback(BB_TRANSFORM_FEEDBACK_BUFFER),
    CopyRead(BB_COPY_READ_BUFFER),
    CopyWrite(BB_COPY_WRITE_BUFFER);

    private final int glEnum;

    BufferType(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
