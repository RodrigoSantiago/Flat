package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum BlendFunction {

    ZERO(GLEnums.BF_ZERO),
    ONE(GLEnums.BF_ONE),
    SRC_COLOR(GLEnums.BF_SRC_COLOR),
    ONE_MINUS_SRC_COLOR(GLEnums.BF_ONE_MINUS_SRC_COLOR),
    SRC_ALPHA(GLEnums.BF_SRC_ALPHA),
    ONE_MINUS_SRC_ALPHA(GLEnums.BF_ONE_MINUS_SRC_ALPHA),
    DST_ALPHA(GLEnums.BF_DST_ALPHA),
    ONE_MINUS_DST_ALPHA(GLEnums.BF_ONE_MINUS_DST_ALPHA),
    DST_COLOR(GLEnums.BF_DST_COLOR),
    ONE_MINUS_DST_COLOR(GLEnums.BF_ONE_MINUS_DST_COLOR),
    SRC_ALPHA_SATURATE(GLEnums.BF_SRC_ALPHA_SATURATE),
    CONSTANT_COLOR(GLEnums.BF_CONSTANT_COLOR),
    ONE_MINUS_CONSTANT_COLOR(GLEnums.BF_ONE_MINUS_CONSTANT_COLOR),
    CONSTANT_ALPHA(GLEnums.BF_CONSTANT_ALPHA),
    ONE_MINUS_CONSTANT_ALPHA(GLEnums.BF_ONE_MINUS_CONSTANT_ALPHA);

    private final int glEnum;

    BlendFunction(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
