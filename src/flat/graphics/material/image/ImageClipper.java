package flat.graphics.material.image;

import flat.graphics.context.Shader;
import flat.graphics.context.ShaderProgram;
import flat.graphics.context.enuns.ShaderType;
import flat.graphics.material.MaterialValue;
import flat.math.Matrix3;
import flat.math.Vector2;
import flat.math.Vector4;
import flat.application.ResourcesManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageClipper extends ImageMaterial {

    private static ShaderProgram shader;

    private Matrix3 prjClip = new Matrix3();
    private Vector2 size = new Vector2();
    private Vector4 radius = new Vector4();
    private boolean invalided;

    private ArrayList<MaterialValue> values = new ArrayList<>();

    public ImageClipper() {
        if (shader == null) {
            try {
                String shadowVtx = ResourcesManager.readPersistentData("resources/clip.vtx.glsl");
                String shadowFrg = ResourcesManager.readPersistentData("resources/clip.frg.glsl");
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
        if (invalided) {
            values.clear();
            values.add(new MaterialValue("prjClip", prjClip));
            values.add(new MaterialValue("ext", size));
            values.add(new MaterialValue("rad", radius));
        }
        return Collections.unmodifiableList(values);
    }

    public Matrix3 getPrjClip() {
        return new Matrix3(prjClip);
    }

    public void setPrjClip(Matrix3 prjClip) {
        invalided = true;
        this.prjClip.set(prjClip);
    }

    public Vector2 getSize() {
        return new Vector2(size);
    }

    public void setSize(Vector2 size) {
        invalided = true;
        this.size.set(size);
    }

    public Vector4 getRadius() {
        return new Vector4(radius);
    }

    public void setRadius(Vector4 radius) {
        this.radius.set(radius);
    }
}
