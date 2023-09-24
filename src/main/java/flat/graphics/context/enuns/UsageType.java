package flat.graphics.context.enuns;

import flat.backend.GLEnuns;

public enum UsageType {
    STREAM_DRAW(GLEnuns.UT_STREAM_DRAW),
    STREAM_READ(GLEnuns.UT_STREAM_READ),
    STREAM_COPY(GLEnuns.UT_STREAM_COPY),
    STATIC_DRAW(GLEnuns.UT_STATIC_DRAW),
    STATIC_READ(GLEnuns.UT_STATIC_READ),
    STATIC_COPY(GLEnuns.UT_STATIC_COPY),
    DYNAMIC_DRAW(GLEnuns.UT_DYNAMIC_DRAW),
    DYNAMIC_READ(GLEnuns.UT_DYNAMIC_READ),
    DYNAMIC_COPY(GLEnuns.UT_DYNAMIC_COPY);

    private final int glEnum;

    UsageType(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getGlEnum() {
        return glEnum;
    }

}
