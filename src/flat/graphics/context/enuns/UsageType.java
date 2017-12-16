package flat.graphics.context.enuns;

import static flat.backend.GLEnuns.*;

public enum UsageType {
    STREAM_DRAW(UT_STREAM_DRAW),
    STREAM_READ(UT_STREAM_READ),
    STREAM_COPY(UT_STREAM_COPY),
    STATIC_DRAW(UT_STATIC_DRAW),
    STATIC_READ(UT_STATIC_READ),
    STATIC_COPY(UT_STATIC_COPY),
    DYNAMIC_DRAW(UT_DYNAMIC_DRAW),
    DYNAMIC_READ(UT_DYNAMIC_READ),
    DYNAMIC_COPY(UT_DYNAMIC_COPY);

    private final int glEnum;

    UsageType(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getGlEnum() {
        return glEnum;
    }

}
