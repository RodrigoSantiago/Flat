package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum BlitMask {
    Color(GLEnums.CB_COLOR_BUFFER_BIT),
    Depth(GLEnums.CB_DEPTH_BUFFER_BIT),
    Stencil(GLEnums.CB_STENCIL_BUFFER_BIT),
    ColorDepth(GLEnums.CB_COLOR_BUFFER_BIT | GLEnums.CB_DEPTH_BUFFER_BIT),
    ColorStencil(GLEnums.CB_COLOR_BUFFER_BIT | GLEnums.CB_STENCIL_BUFFER_BIT),
    DepthStencil(GLEnums.CB_DEPTH_BUFFER_BIT | GLEnums.CB_STENCIL_BUFFER_BIT),
    All(GLEnums.CB_COLOR_BUFFER_BIT | GLEnums.CB_DEPTH_BUFFER_BIT | GLEnums.CB_STENCIL_BUFFER_BIT);

    private final int glEnum;

    BlitMask(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
