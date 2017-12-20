package flat.graphics.context.enuns;

import static flat.backend.GLEnuns.*;

public enum MagFilter {
    NEAREST(IF_NEAREST),
    LINEAR(IF_LINEAR);

    private final int glEnum;

    MagFilter(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
