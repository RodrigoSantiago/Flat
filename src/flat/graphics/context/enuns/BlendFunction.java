package flat.graphics.context.enuns;

import static flat.backend.GLEnuns.*;

public enum BlendFunction {

    ZERO(BF_ZERO),
    ONE(BF_ONE),
    SRC_COLOR(BF_SRC_COLOR),
    ONE_MINUS_SRC_COLOR(BF_ONE_MINUS_SRC_COLOR),
    SRC_ALPHA(BF_SRC_ALPHA),
    ONE_MINUS_SRC_ALPHA(BF_ONE_MINUS_SRC_ALPHA),
    DST_ALPHA(BF_DST_ALPHA),
    ONE_MINUS_DST_ALPHA(BF_ONE_MINUS_DST_ALPHA),
    DST_COLOR(BF_DST_COLOR),
    ONE_MINUS_DST_COLOR(BF_ONE_MINUS_DST_COLOR),
    SRC_ALPHA_SATURATE(BF_SRC_ALPHA_SATURATE),
    CONSTANT_COLOR(BF_CONSTANT_COLOR),
    ONE_MINUS_CONSTANT_COLOR(BF_ONE_MINUS_CONSTANT_COLOR),
    CONSTANT_ALPHA(BF_CONSTANT_ALPHA),
    ONE_MINUS_CONSTANT_ALPHA(BF_ONE_MINUS_CONSTANT_ALPHA);

    private final int glEnum;

    BlendFunction(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
