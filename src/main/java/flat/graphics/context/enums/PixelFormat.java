package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum PixelFormat {
    RED(GLEnums.TF_RED, 1),
    RG(GLEnums.TF_RG, 2),
    RGB(GLEnums.TF_RGB, 3),
    RGBA(GLEnums.TF_RGBA, 4),
    RGBA16F(GLEnums.TF_RGBA16F, 8),
    DEPTH16(GLEnums.TF_DEPTH_COMPONENT16, 2),
    DEPTH24(GLEnums.TF_DEPTH_COMPONENT24, 3),
    DEPTH32(GLEnums.TF_DEPTH_COMPONENT32F, 4),
    DEPTH24_STENCIL8(GLEnums.TF_DEPTH24_STENCIL8, 4),
    DEPTH32_STENCIL8(GLEnums.TF_DEPTH32F_STENCIL8, 5);

    private final int glEnum;
    private final int bytes;

    PixelFormat(int glEnum, int bytes) {
        this.glEnum = glEnum;
        this.bytes= bytes;
    }

    public int getInternalEnum() {
        return glEnum;
    }

    public int getPixelBytes() {
        return bytes;
    }
}
