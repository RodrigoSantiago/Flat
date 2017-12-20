package flat.graphics.context.enuns;

import static flat.backend.GLEnuns.*;

public enum MinFilter {
    NEAREST(IF_NEAREST),
    LINEAR(IF_LINEAR),
    NEAREST_MIPMAP_NEAREST(IF_NEAREST_MIPMAP_NEAREST),
    LINEAR_MIPMAP_NEAREST(IF_LINEAR_MIPMAP_NEAREST),
    NEAREST_MIPMAP_LINEAR(IF_NEAREST_MIPMAP_LINEAR),
    LINEAR_MIPMAP_LINEAR(IF_LINEAR_MIPMAP_LINEAR);

    private final int glEnum;

    MinFilter(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
