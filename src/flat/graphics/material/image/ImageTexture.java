package flat.graphics.material.image;

import flat.graphics.context.Shader;
import flat.graphics.context.ShaderProgram;
import flat.graphics.context.enuns.ShaderType;
import flat.graphics.material.MaterialValue;
import flat.FileUtils;

import java.util.Collections;
import java.util.List;

public class ImageTexture extends ImageMaterial {

    private static ShaderProgram shader;
    public ImageTexture() {
        if (shader == null) {
            try {
                String shadowVtx = FileUtils.readFromInternal("resources/default.vtx.glsl");
                String shadowFrg = FileUtils.readFromInternal("resources/default.frg.glsl");
                Shader vtx = new Shader(ShaderType.Vertex, shadowVtx);
                if (!vtx.compile()) {
                    throw new Exception(vtx.getLog());
                }
                Shader frg = new Shader(ShaderType.Fragment, shadowFrg);
                if (!frg.compile()) {
                    throw new Exception(frg.getLog());
                }
                shader = new ShaderProgram(vtx, frg);
                if (!shader.link()) {
                    throw new Exception(shader.getLog());
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Invalid shader file", e);
            }
        }
    }

    @Override
    public ShaderProgram getShader() {
        return shader;
    }

    @Override
    public List<MaterialValue> getValues() {
        return Collections.emptyList();
    }
}
