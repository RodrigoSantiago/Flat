package flat.graphics.material.mesh;

import flat.graphics.context.ShaderProgram;
import flat.graphics.material.Material;
import flat.graphics.material.MaterialValue;

import java.util.List;

public class MeshMaterial extends Material {

    public boolean isTransparent() {
        return false;
    }

    @Override
    public ShaderProgram getShader() {
        return null;
    }

    @Override
    public List<MaterialValue> getValues() {
        return null;
    }

}
