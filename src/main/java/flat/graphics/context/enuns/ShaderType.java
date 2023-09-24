package flat.graphics.context.enuns;

import flat.backend.GLEnuns;

public enum ShaderType {
    Vertex(GLEnuns.ST_VERTEX_SHADER),
    Fragment(GLEnuns.ST_FRAGMENT_SHADER)/*,
    GeometricFragment(ST_GEOMETRIC_SHADER)*/;

    private int glEnum;

    ShaderType(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
