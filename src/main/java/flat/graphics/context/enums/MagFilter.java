package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum MagFilter {
    NEAREST(GLEnums.IF_NEAREST),
    LINEAR(GLEnums.IF_LINEAR);

    private final int glEnum;

    MagFilter(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
