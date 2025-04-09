package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum ReadBuffer {
    FRONT(GLEnums.FG_FRONT),
    BACK(GLEnums.FG_BACK);

    private final int glEnum;

    ReadBuffer(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
