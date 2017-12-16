package flat.graphics.context.enuns;

import static flat.backend.GLEnuns.*;

public enum DrawVertexMode {
    POINTS(VM_POINTS),
    LINE_STRIP(VM_LINE_STRIP),
    LINE_LOOP(VM_LINE_LOOP),
    LINES(VM_LINES),
    TRIANGLE_STRIP(VM_TRIANGLE_STRIP),
    TRIANGLE_FAN(VM_TRIANGLE_FAN),
    TRIANGLES(VM_TRIANGLES);

    private final int glEnum;

    DrawVertexMode(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }

}
