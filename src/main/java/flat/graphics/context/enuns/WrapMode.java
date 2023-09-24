package flat.graphics.context.enuns;

import flat.backend.GLEnuns;

public enum WrapMode {
    CLAMP_TO_EDGE(GLEnuns.IW_CLAMP_TO_EDGE),
    CLAMP_TO_BORDER(GLEnuns.IW_CLAMP_TO_BORDER),
    MIRRORED_REPEAT(GLEnuns.IW_MIRRORED_REPEAT),
    REPEAT(GLEnuns.IW_REPEAT);

    private final int glEnum;

    WrapMode(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
