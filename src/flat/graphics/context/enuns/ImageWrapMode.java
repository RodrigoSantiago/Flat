package flat.graphics.context.enuns;

import static flat.backend.GLEnuns.*;

public enum ImageWrapMode {
    CLAMP_TO_EDGE(IW_CLAMP_TO_EDGE),
    CLAMP_TO_BORDER(IW_CLAMP_TO_BORDER),
    MIRRORED_REPEAT(IW_MIRRORED_REPEAT),
    REPEAT(IW_REPEAT);

    private final int glEnum;

    ImageWrapMode(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
