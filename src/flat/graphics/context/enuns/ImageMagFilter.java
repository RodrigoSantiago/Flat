package flat.graphics.context.enuns;

import static flat.backend.GLEnuns.*;

public enum ImageMagFilter {
    NEAREST(IF_NEAREST),
    LINEAR(IF_LINEAR);

    private final int glEnum;

    ImageMagFilter(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
