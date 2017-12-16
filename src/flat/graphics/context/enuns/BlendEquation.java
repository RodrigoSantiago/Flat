package flat.graphics.context.enuns;

import static flat.backend.GLEnuns.*;

public enum BlendEquation {
    ADD(BE_FUNC_ADD),
    SUB(BE_FUNC_SUBTRACT),
    REVERSE_SUB(BE_FUNC_REVERSE_SUBTRACT),
    MIN(BE_MIN),
    MAX(BE_MAX);

    private final int glEnum;

    BlendEquation(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
