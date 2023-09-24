package flat.graphics.context.enuns;

import flat.backend.GLEnuns;

public enum BlendEquation {
    ADD(GLEnuns.BE_FUNC_ADD),
    SUB(GLEnuns.BE_FUNC_SUBTRACT),
    REVERSE_SUB(GLEnuns.BE_FUNC_REVERSE_SUBTRACT),
    MIN(GLEnuns.BE_MIN),
    MAX(GLEnuns.BE_MAX);

    private final int glEnum;

    BlendEquation(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
