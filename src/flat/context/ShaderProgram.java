package flat.context;

public class ShaderProgram {
    VertexShader vertex;
    FragmentShader fragment;

    public ShaderProgram(VertexShader vertex, FragmentShader fragment) {
        this.vertex = vertex;
        this.fragment = fragment;
    }

    public ShaderProgram() {
        this(null, null);
    }

    public boolean link() {
        return false;
    }

    public boolean isLinked() {
        return false;
    }
}
