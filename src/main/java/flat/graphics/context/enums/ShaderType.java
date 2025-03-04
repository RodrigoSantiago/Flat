package flat.graphics.context.enums;

import flat.backend.GLEnums;

public enum ShaderType {
    Vertex(GLEnums.ST_VERTEX_SHADER),
    Fragment(GLEnums.ST_FRAGMENT_SHADER)/*,
    GeometricFragment(ST_GEOMETRIC_SHADER)*/;

    private int glEnum;

    ShaderType(int glEnum) {
        this.glEnum = glEnum;
    }

    public int getInternalEnum() {
        return glEnum;
    }
}
