package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum PixelFormat {
    RED(GLEnums.TF_RED),
    RG(GLEnums.TF_RG),
    RGB(GLEnums.TF_RGB),
    RGBA(GLEnums.TF_RGBA),
    DEPTH16(GLEnums.TF_DEPTH_COMPONENT16),
    DEPTH24(GLEnums.TF_DEPTH_COMPONENT24),
    DEPTH32(GLEnums.TF_DEPTH_COMPONENT32F),
    DEPTH24_STENCIL8(GLEnums.TF_DEPTH24_STENCIL8),
    DEPTH32_STENCIL8(GLEnums.TF_DEPTH32F_STENCIL8);

    private final int glEnum;

    PixelFormat(int glEnum) {
        this.glEnum = glEnum;
    }


    public int getInternalEnum() {
        return glEnum;
    }

    public static int getPixelBytes(PixelFormat pixelFormat) {
        if (pixelFormat == RED) {
            return 1;
        } else if (pixelFormat == RG) {
            return 2;
        } else if (pixelFormat == RGB) {
            return 3;
        } else if (pixelFormat == RGBA) {
            return 4;
        } else if (pixelFormat == DEPTH32) {
            return 4;
        } else if (pixelFormat == DEPTH24) {
            return 3;
        } else if (pixelFormat == DEPTH16) {
            return 2;
        } else if (pixelFormat == DEPTH32_STENCIL8) {
            return 5;
        } else if (pixelFormat == DEPTH24_STENCIL8) {
            return 4;
        } else {
            return 1;
        }
    }
}
