package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum LayerTarget {
    COLOR_0(GLEnums.FA_COLOR_ATTACHMENT0),
    COLOR_1(GLEnums.FA_COLOR_ATTACHMENT1),
    COLOR_2(GLEnums.FA_COLOR_ATTACHMENT2),
    COLOR_3(GLEnums.FA_COLOR_ATTACHMENT3),
    COLOR_4(GLEnums.FA_COLOR_ATTACHMENT4),
    COLOR_5(GLEnums.FA_COLOR_ATTACHMENT5),
    COLOR_6(GLEnums.FA_COLOR_ATTACHMENT6),
    COLOR_7(GLEnums.FA_COLOR_ATTACHMENT7),
    DEPTH(GLEnums.FA_DEPTH_ATTACHMENT),
    STENCIL(GLEnums.FA_STENCIL_ATTACHMENT),
    DEPTH_STENCIL(GLEnums.FA_DEPTH_STENCIL_ATTACHMENT);

    private final int glEnum;

    LayerTarget(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

    public static LayerTarget getColorAttachment(int id) {
        return switch (id) {
            case 1 -> COLOR_1;
            case 2 -> COLOR_2;
            case 3 -> COLOR_3;
            case 4 -> COLOR_4;
            case 5 -> COLOR_5;
            case 6 -> COLOR_6;
            case 7 -> COLOR_7;
            default -> COLOR_0;
        };
    }
}