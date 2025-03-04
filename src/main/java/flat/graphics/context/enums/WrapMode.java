package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum WrapMode {
    CLAMP_TO_EDGE(GLEnums.IW_CLAMP_TO_EDGE),
    CLAMP_TO_BORDER(GLEnums.IW_CLAMP_TO_BORDER),
    MIRRORED_REPEAT(GLEnums.IW_MIRRORED_REPEAT),
    REPEAT(GLEnums.IW_REPEAT);

    private final int glEnum;

    WrapMode(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
