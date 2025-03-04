package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum MathOperation {
    KEEP(GLEnums.MO_KEEP),
    ZERO(GLEnums.MO_ZERO),
    REPLACE(GLEnums.MO_REPLACE),
    INCR(GLEnums.MO_INCR),
    INCR_WRAP(GLEnums.MO_INCR_WRAP),
    DECR(GLEnums.MO_DECR),
    DECR_WRAP(GLEnums.MO_DECR_WRAP);

    private final int glEnum;

    MathOperation(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
