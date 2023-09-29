package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum VertexMode {
    POINTS(GLEnums.VM_POINTS),
    LINE_STRIP(GLEnums.VM_LINE_STRIP),
    LINE_LOOP(GLEnums.VM_LINE_LOOP),
    LINES(GLEnums.VM_LINES),
    TRIANGLE_STRIP(GLEnums.VM_TRIANGLE_STRIP),
    TRIANGLE_FAN(GLEnums.VM_TRIANGLE_FAN),
    TRIANGLES(GLEnums.VM_TRIANGLES);

    private final int glEnum;

    VertexMode(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
