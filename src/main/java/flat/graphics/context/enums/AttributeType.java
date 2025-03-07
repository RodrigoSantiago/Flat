package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum AttributeType {
    FLOAT(GLEnums.AT_FLOAT, 1),
    FLOAT_VEC2(GLEnums.AT_FLOAT_VEC2, 2),
    FLOAT_VEC3(GLEnums.AT_FLOAT_VEC3, 3),
    FLOAT_VEC4(GLEnums.AT_FLOAT_VEC4, 4),
    INT(GLEnums.AT_INT, 1),
    INT_VEC2(GLEnums.AT_INT_VEC2, 2),
    INT_VEC3(GLEnums.AT_INT_VEC3, 3),
    INT_VEC4(GLEnums.AT_INT_VEC4, 4),
    BOOL(GLEnums.AT_BOOL, 1),
    BOOL_VEC2(GLEnums.AT_BOOL_VEC2, 2),
    BOOL_VEC3(GLEnums.AT_BOOL_VEC3, 3),
    BOOL_VEC4(GLEnums.AT_BOOL_VEC4, 4),
    FLOAT_MAT2(GLEnums.AT_FLOAT_MAT2, 4),
    FLOAT_MAT3(GLEnums.AT_FLOAT_MAT3, 9),
    FLOAT_MAT4(GLEnums.AT_FLOAT_MAT4, 16),
    FLOAT_MAT2x3(GLEnums.AT_FLOAT_MAT2x3, 6),
    FLOAT_MAT2x4(GLEnums.AT_FLOAT_MAT2x4, 8),
    FLOAT_MAT3x2(GLEnums.AT_FLOAT_MAT3x2, 6),
    FLOAT_MAT3x4(GLEnums.AT_FLOAT_MAT3x4, 12),
    FLOAT_MAT4x2(GLEnums.AT_FLOAT_MAT4x2, 8),
    FLOAT_MAT4x3(GLEnums.AT_FLOAT_MAT4x3, 12),
    SAMPLER_2D(GLEnums.AT_SAMPLER_2D, 1),
    SAMPLER_CUBE(GLEnums.AT_SAMPLER_CUBE, 1);

    private final int glEnum, size;

    AttributeType(int glEnum, int size) {
        this.glEnum = glEnum;
        this.size = size;
    }

    public int getInternalEnum() {
        return glEnum;
    }

    public int getSize() {
        return size;
    }

    public static AttributeType fromInternalEnum(int glEnum) {
        AttributeType[] types = values();
        for (AttributeType type : types) {
            if (type.glEnum == glEnum) {
                return type;
            }
        }
        System.out.println("Type not found : " + glEnum);
        return null;
    }
}
