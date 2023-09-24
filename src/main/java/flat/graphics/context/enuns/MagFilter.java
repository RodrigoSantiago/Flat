package flat.graphics.context.enuns;

import flat.backend.GLEnuns;

public enum MagFilter {
    NEAREST(GLEnuns.IF_NEAREST),
    LINEAR(GLEnuns.IF_LINEAR);

    private final int glEnum;

    MagFilter(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
