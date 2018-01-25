package flat.graphics.material.image;

import flat.graphics.context.ShaderProgram;
import flat.graphics.material.Material;
import flat.graphics.material.MaterialValue;

import java.util.Collections;
import java.util.List;

public class ImageMaterial extends Material {

    @Override
    public ShaderProgram getShader() {
        return null;
    }

    @Override
    public List<MaterialValue> getValues() {
        return Collections.emptyList();
    }
}
