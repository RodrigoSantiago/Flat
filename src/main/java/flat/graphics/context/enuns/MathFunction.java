package flat.graphics.context.enuns;

import flat.backend.GLEnuns;

public enum MathFunction {
   NEVER(GLEnuns.MF_NEVER),
   LESS(GLEnuns.MF_LESS),
   EQUAL(GLEnuns.MF_EQUAL),
   LESS_EQUAL(GLEnuns.MF_LEQUAL),
   GREATER(GLEnuns.MF_GREATER),
   NOTEQUAL(GLEnuns.MF_NOTEQUAL),
   GREATER_EQUAL(GLEnuns.MF_GEQUAL),
   ALWAYS(GLEnuns.MF_ALWAYS);
    
    private final int glEnum;

    MathFunction(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
