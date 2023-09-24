package flat.graphics.context.enuns;

import flat.backend.GLEnuns;

public enum MathOperation {
    KEEP(GLEnuns.MO_KEEP),
    ZERO(GLEnuns.MO_ZERO),
    REPLACE(GLEnuns.MO_REPLACE),
    INCR(GLEnuns.MO_INCR),
    INCR_WRAP(GLEnuns.MO_INCR_WRAP),
    DECR(GLEnuns.MO_DECR),
    DECR_WRAP(GLEnuns.MO_DECR_WRAP);

    private final int glEnum;

    MathOperation(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
