package flat.graphics.context.enuns;

import flat.backend.GLEnuns;

public enum PixelFormat {
    RGB(GLEnuns.TF_RGB),
    RGBA(GLEnuns.TF_RGBA),
    DEPTH16(GLEnuns.TF_DEPTH_COMPONENT16),
    DEPTH24(GLEnuns.TF_DEPTH_COMPONENT24),
    DEPTH32(GLEnuns.TF_DEPTH_COMPONENT32F),
    DEPTH24_STENCIL8(GLEnuns.TF_DEPTH24_STENCIL8),
    DEPTH32_STENCIL8(GLEnuns.TF_DEPTH32F_STENCIL8);

    private final int glEnum;

    PixelFormat(int glEnum) {
        this.glEnum = glEnum;
    }


    public int getInternalEnum() {
        return glEnum;
    }
}
