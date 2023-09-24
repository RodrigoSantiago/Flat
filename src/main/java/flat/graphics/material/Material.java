package flat.graphics.material;

import flat.graphics.context.ShaderProgram;

import java.util.List;

public abstract class Material {

    public abstract boolean isTransparent();

    public abstract ShaderProgram getShader();

    public abstract List<MaterialValue> getValues();

}
