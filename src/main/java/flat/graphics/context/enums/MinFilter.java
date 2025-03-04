package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum MinFilter {
    NEAREST(GLEnums.IF_NEAREST),
    LINEAR(GLEnums.IF_LINEAR),
    NEAREST_MIPMAP_NEAREST(GLEnums.IF_NEAREST_MIPMAP_NEAREST),
    LINEAR_MIPMAP_NEAREST(GLEnums.IF_LINEAR_MIPMAP_NEAREST),
    NEAREST_MIPMAP_LINEAR(GLEnums.IF_NEAREST_MIPMAP_LINEAR),
    LINEAR_MIPMAP_LINEAR(GLEnums.IF_LINEAR_MIPMAP_LINEAR);

    private final int glEnum;

    MinFilter(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
