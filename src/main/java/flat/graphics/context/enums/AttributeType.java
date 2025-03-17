package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum AttributeType {
    FLOAT(GLEnums.AT_FLOAT, 1, true, 0, 0),
    FLOAT_VEC2(GLEnums.AT_FLOAT_VEC2, 2, true, 0, 0),
    FLOAT_VEC3(GLEnums.AT_FLOAT_VEC3, 3, true, 0, 0),
    FLOAT_VEC4(GLEnums.AT_FLOAT_VEC4, 4, true, 0, 0),
    INT(GLEnums.AT_INT, 1, false, 0, 0),
    INT_VEC2(GLEnums.AT_INT_VEC2, 2, false, 0, 0),
    INT_VEC3(GLEnums.AT_INT_VEC3, 3, false, 0, 0),
    INT_VEC4(GLEnums.AT_INT_VEC4, 4, false, 0, 0),
    BOOL(GLEnums.AT_BOOL, 1, false, 0, 0),
    BOOL_VEC2(GLEnums.AT_BOOL_VEC2, 2, false, 0, 0),
    BOOL_VEC3(GLEnums.AT_BOOL_VEC3, 3, false, 0, 0),
    BOOL_VEC4(GLEnums.AT_BOOL_VEC4, 4, false, 0, 0),
    FLOAT_MAT2(GLEnums.AT_FLOAT_MAT2, 4, true, 2, 2),
    FLOAT_MAT3(GLEnums.AT_FLOAT_MAT3, 9, true, 3, 3),
    FLOAT_MAT4(GLEnums.AT_FLOAT_MAT4, 16, true, 4, 4),
    FLOAT_MAT2x3(GLEnums.AT_FLOAT_MAT2x3, 6, true, 2, 3),
    FLOAT_MAT2x4(GLEnums.AT_FLOAT_MAT2x4, 8, true, 2, 4),
    FLOAT_MAT3x2(GLEnums.AT_FLOAT_MAT3x2, 6, true, 3, 2),
    FLOAT_MAT3x4(GLEnums.AT_FLOAT_MAT3x4, 12, true, 3, 4),
    FLOAT_MAT4x2(GLEnums.AT_FLOAT_MAT4x2, 8, true, 4, 2),
    FLOAT_MAT4x3(GLEnums.AT_FLOAT_MAT4x3, 12, true, 4, 3),
    SAMPLER_2D(GLEnums.AT_SAMPLER_2D, 1, false, 0, 0),
    SAMPLER_CUBE(GLEnums.AT_SAMPLER_CUBE, 1, false, 0, 0);

    private final int glEnum, size, mw, mh;
    private final boolean floating;

    AttributeType(int glEnum, int size, boolean floating, int mw, int mh) {
        this.glEnum = glEnum;
        this.size = size;
        this.floating = floating;
        this.mw = mw;
        this.mh = mh;
    }

    public int getInternalEnum() {
        return glEnum;
    }

    public int getSize() {
        return size;
    }

    public boolean isFloat() {
        return floating;
    }

    public int getMatrixWidth() {
        return mw;
    }

    public int getMatrixHeight() {
        return mh;
    }

    public static AttributeType fromInternalEnum(int glEnum) {
        AttributeType[] types = values();
        for (AttributeType type : types) {
            if (type.glEnum == glEnum) {
                return type;
            }
        }
        return null;
    }
}
