package flat.graphics.context.enuns;

import flat.backend.GLEnuns;

public enum CubeFace {
    X1(GLEnuns.TT_TEXTURE_CUBE_MAP_POSITIVE_X),
    X2(GLEnuns.TT_TEXTURE_CUBE_MAP_NEGATIVE_X),
    Y1(GLEnuns.TT_TEXTURE_CUBE_MAP_POSITIVE_Y),
    Y2(GLEnuns.TT_TEXTURE_CUBE_MAP_NEGATIVE_Y),
    Z1(GLEnuns.TT_TEXTURE_CUBE_MAP_POSITIVE_Z),
    Z2(GLEnuns.TT_TEXTURE_CUBE_MAP_NEGATIVE_Z);

    private final int glEnum;

    CubeFace(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
