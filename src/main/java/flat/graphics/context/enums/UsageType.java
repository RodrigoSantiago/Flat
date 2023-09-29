package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum UsageType {
    STREAM_DRAW(GLEnums.UT_STREAM_DRAW),
    STREAM_READ(GLEnums.UT_STREAM_READ),
    STREAM_COPY(GLEnums.UT_STREAM_COPY),
    STATIC_DRAW(GLEnums.UT_STATIC_DRAW),
    STATIC_READ(GLEnums.UT_STATIC_READ),
    STATIC_COPY(GLEnums.UT_STATIC_COPY),
    DYNAMIC_DRAW(GLEnums.UT_DYNAMIC_DRAW),
    DYNAMIC_READ(GLEnums.UT_DYNAMIC_READ),
    DYNAMIC_COPY(GLEnums.UT_DYNAMIC_COPY);

    private final int glEnum;

    UsageType(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getGlEnum() {
        return glEnum;
    }

}
