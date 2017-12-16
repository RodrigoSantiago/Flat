package flat.graphics.context.enuns;

import static flat.backend.GLEnuns.*;

public enum ShaderType {
    Vertex(ST_FRAGMENT_SHADER),
    Fragment(ST_FRAGMENT_SHADER)/*,
    GeometricFragment(ST_GEOMETRIC_SHADER)*/;

    private int glEnum;

    ShaderType(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
