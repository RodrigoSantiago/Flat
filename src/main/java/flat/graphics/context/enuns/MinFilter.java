package flat.graphics.context.enuns;

import flat.backend.GLEnuns;

public enum MinFilter {
    NEAREST(GLEnuns.IF_NEAREST),
    LINEAR(GLEnuns.IF_LINEAR),
    NEAREST_MIPMAP_NEAREST(GLEnuns.IF_NEAREST_MIPMAP_NEAREST),
    LINEAR_MIPMAP_NEAREST(GLEnuns.IF_LINEAR_MIPMAP_NEAREST),
    NEAREST_MIPMAP_LINEAR(GLEnuns.IF_NEAREST_MIPMAP_LINEAR),
    LINEAR_MIPMAP_LINEAR(GLEnuns.IF_LINEAR_MIPMAP_LINEAR);

    private final int glEnum;

    MinFilter(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
