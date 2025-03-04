package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum BlendEquation {
    ADD(GLEnums.BE_FUNC_ADD),
    SUB(GLEnums.BE_FUNC_SUBTRACT),
    REVERSE_SUB(GLEnums.BE_FUNC_REVERSE_SUBTRACT),
    MIN(GLEnums.BE_MIN),
    MAX(GLEnums.BE_MAX);

    private final int glEnum;

    BlendEquation(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
