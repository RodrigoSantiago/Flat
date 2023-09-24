package flat.graphics.context.enuns;

import flat.backend.GLEnuns;

public enum BlendFunction {

    ZERO(GLEnuns.BF_ZERO),
    ONE(GLEnuns.BF_ONE),
    SRC_COLOR(GLEnuns.BF_SRC_COLOR),
    ONE_MINUS_SRC_COLOR(GLEnuns.BF_ONE_MINUS_SRC_COLOR),
    SRC_ALPHA(GLEnuns.BF_SRC_ALPHA),
    ONE_MINUS_SRC_ALPHA(GLEnuns.BF_ONE_MINUS_SRC_ALPHA),
    DST_ALPHA(GLEnuns.BF_DST_ALPHA),
    ONE_MINUS_DST_ALPHA(GLEnuns.BF_ONE_MINUS_DST_ALPHA),
    DST_COLOR(GLEnuns.BF_DST_COLOR),
    ONE_MINUS_DST_COLOR(GLEnuns.BF_ONE_MINUS_DST_COLOR),
    SRC_ALPHA_SATURATE(GLEnuns.BF_SRC_ALPHA_SATURATE),
    CONSTANT_COLOR(GLEnuns.BF_CONSTANT_COLOR),
    ONE_MINUS_CONSTANT_COLOR(GLEnuns.BF_ONE_MINUS_CONSTANT_COLOR),
    CONSTANT_ALPHA(GLEnuns.BF_CONSTANT_ALPHA),
    ONE_MINUS_CONSTANT_ALPHA(GLEnuns.BF_ONE_MINUS_CONSTANT_ALPHA);

    private final int glEnum;

    BlendFunction(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
