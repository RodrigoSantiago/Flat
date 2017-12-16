package flat.graphics.context.enuns;

import static flat.backend.GLEnuns.*;

public enum TextureFormat {
    RGB(TF_RGB),
    RGBA(TF_RGBA),
    DEPTH16(TF_DEPTH_COMPONENT16),
    DEPTH24(TF_DEPTH_COMPONENT24),
    DEPTH32(TF_DEPTH_COMPONENT32F),
    DEPTH24_STENCIL8(TF_DEPTH24_STENCIL8),
    DEPTH32_STENCIL8(TF_DEPTH32F_STENCIL8);

    private final int glEnum;

    TextureFormat(int glEnum) {
        this.glEnum = glEnum;
    }


    public int getInternalEnum() {
        return glEnum;
    }
}
