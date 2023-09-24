package flat.graphics.context.enuns;

import flat.backend.GLEnuns;

public enum VertexMode {
    POINTS(GLEnuns.VM_POINTS),
    LINE_STRIP(GLEnuns.VM_LINE_STRIP),
    LINE_LOOP(GLEnuns.VM_LINE_LOOP),
    LINES(GLEnuns.VM_LINES),
    TRIANGLE_STRIP(GLEnuns.VM_TRIANGLE_STRIP),
    TRIANGLE_FAN(GLEnuns.VM_TRIANGLE_FAN),
    TRIANGLES(GLEnuns.VM_TRIANGLES);

    private final int glEnum;

    VertexMode(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
