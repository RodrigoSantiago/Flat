package flat.graphics.smart.effects;

import flat.graphics.context.ShaderProgram;

import java.io.Serializable;
import java.util.List;

public abstract class ImageMaterial {

    public ImageMaterial() {
    }

    public abstract ShaderProgram getShader();

    public abstract List<MaterialValue> getValues();
}
