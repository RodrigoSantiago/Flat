package flat.graphics.context.enuns;

import static flat.backend.GLEnuns.*;

public enum MathFunction {
   NEVER(MF_NEVER),
   LESS(MF_LESS),
   EQUAL(MF_EQUAL),
   LESS_EQUAL(MF_LEQUAL),
   GREATER(MF_GREATER),
   NOTEQUAL(MF_NOTEQUAL),
   GREATER_EQUAL(MF_GEQUAL),
   ALWAYS(MF_ALWAYS);
    
    private final int glEnum;

    MathFunction(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
