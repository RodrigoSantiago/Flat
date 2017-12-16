package flat.graphics.context.enuns;

import flat.Internal;
import flat.backend.GLEnuns;

public enum ShaderType {
    Vertex(GLEnuns.ST_FRAGMENT_SHADER),
    Fragment(GLEnuns.ST_FRAGMENT_SHADER) /*, GeometricFragment(GLEnuns.ST_GEOMETRIC_SHADER)*/;

    private int internalEnum;
    ShaderType(int internalEnum) {
        this.internalEnum = internalEnum;
    }

    @Internal
    public int getInternalEnum() {
        return internalEnum;
    }
}
