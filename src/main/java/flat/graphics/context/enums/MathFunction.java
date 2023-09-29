package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum MathFunction {
   NEVER(GLEnums.MF_NEVER),
   LESS(GLEnums.MF_LESS),
   EQUAL(GLEnums.MF_EQUAL),
   LESS_EQUAL(GLEnums.MF_LEQUAL),
   GREATER(GLEnums.MF_GREATER),
   NOTEQUAL(GLEnums.MF_NOTEQUAL),
   GREATER_EQUAL(GLEnums.MF_GEQUAL),
   ALWAYS(GLEnums.MF_ALWAYS);
    
    private final int glEnum;

    MathFunction(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
