package flat.model;

import flat.math.Matrix4;
import flat.screen.Context;
import flat.shader.ShaderProgram;
import flat.shader.Shader;

import java.util.HashMap;

public class ModelMaterial {
    ShaderProgram program;
    Shader fragment, vertex;

    HashMap<String, Object> shaderData;

    public boolean isInstanceAllowed() {
        return false;
    }

    public void applyAttributes(Context context, Model model, int count, Matrix4[] transforms) {

    }

    public void applyAttributes(Context context, Model model) {

    }
}
