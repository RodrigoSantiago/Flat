package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum AttributeType {
    FLOAT(GLEnums.AT_FLOAT, 1, true),
    FLOAT_VEC2(GLEnums.AT_FLOAT_VEC2, 2, true),
    FLOAT_VEC3(GLEnums.AT_FLOAT_VEC3, 3, true),
    FLOAT_VEC4(GLEnums.AT_FLOAT_VEC4, 4, true),
    INT(GLEnums.AT_INT, 1, false),
    INT_VEC2(GLEnums.AT_INT_VEC2, 2, false),
    INT_VEC3(GLEnums.AT_INT_VEC3, 3, false),
    INT_VEC4(GLEnums.AT_INT_VEC4, 4, false),
    BOOL(GLEnums.AT_BOOL, 1, false),
    BOOL_VEC2(GLEnums.AT_BOOL_VEC2, 2, false),
    BOOL_VEC3(GLEnums.AT_BOOL_VEC3, 3, false),
    BOOL_VEC4(GLEnums.AT_BOOL_VEC4, 4, false),
    FLOAT_MAT2(GLEnums.AT_FLOAT_MAT2, 4, true),
    FLOAT_MAT3(GLEnums.AT_FLOAT_MAT3, 9, true),
    FLOAT_MAT4(GLEnums.AT_FLOAT_MAT4, 16, true),
    FLOAT_MAT2x3(GLEnums.AT_FLOAT_MAT2x3, 6, true),
    FLOAT_MAT2x4(GLEnums.AT_FLOAT_MAT2x4, 8, true),
    FLOAT_MAT3x2(GLEnums.AT_FLOAT_MAT3x2, 6, true),
    FLOAT_MAT3x4(GLEnums.AT_FLOAT_MAT3x4, 12, true),
    FLOAT_MAT4x2(GLEnums.AT_FLOAT_MAT4x2, 8, true),
    FLOAT_MAT4x3(GLEnums.AT_FLOAT_MAT4x3, 12, true),
    SAMPLER_2D(GLEnums.AT_SAMPLER_2D, 1, false),
    SAMPLER_CUBE(GLEnums.AT_SAMPLER_CUBE, 1, false);

    private final int glEnum, size;
    private final boolean floating;

    AttributeType(int glEnum, int size, boolean floating) {
        this.glEnum = glEnum;
        this.size = size;
        this.floating = floating;
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
