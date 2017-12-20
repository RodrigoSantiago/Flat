package flat.graphics.smart.effects;

import flat.graphics.context.Shader;
import flat.graphics.context.ShaderProgram;
import flat.graphics.context.enuns.ShaderType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class ImgMatDefault extends ImageMaterial {

    private static ShaderProgram shader;
    public ImgMatDefault() {
        if (shader == null) {
            try {
                String shadowVtx = new String(Files.readAllBytes(Paths.get(getClass().getResource("/resources/default.vtx.glsl").toURI())));
                String shadowFrg = new String(Files.readAllBytes(Paths.get(getClass().getResource("/resources/default.frg.glsl").toURI())));
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
