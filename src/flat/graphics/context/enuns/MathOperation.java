package flat.graphics.context.enuns;

import static flat.backend.GLEnuns.*;

public enum MathOperation {
    KEEP(MO_KEEP),
    ZERO(MO_ZERO),
    REPLACE(MO_REPLACE),
    INCR(MO_INCR),
    INCR_WRAP(MO_INCR_WRAP),
    DECR(MO_DECR),
    DECR_WRAP(MO_DECR_WRAP);

    private final int glEnum;

    MathOperation(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
