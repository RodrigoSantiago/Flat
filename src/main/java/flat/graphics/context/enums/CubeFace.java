package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum CubeFace {
    X1(GLEnums.TT_TEXTURE_CUBE_MAP_POSITIVE_X),
    X2(GLEnums.TT_TEXTURE_CUBE_MAP_NEGATIVE_X),
    Y1(GLEnums.TT_TEXTURE_CUBE_MAP_POSITIVE_Y),
    Y2(GLEnums.TT_TEXTURE_CUBE_MAP_NEGATIVE_Y),
    Z1(GLEnums.TT_TEXTURE_CUBE_MAP_POSITIVE_Z),
    Z2(GLEnums.TT_TEXTURE_CUBE_MAP_NEGATIVE_Z);

    private final int glEnum;

    CubeFace(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
