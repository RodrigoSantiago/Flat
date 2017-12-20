package flat.graphics.smart.effects;

import flat.graphics.context.Shader;
import flat.graphics.context.ShaderProgram;
import flat.graphics.context.enuns.ShaderType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public final class RoundRectShadow extends ImageMaterial {

    private static ShaderProgram shader;
    private ArrayList<MaterialValue> values = new ArrayList<>();

    private float blur, alpha, x, y, width, height, cTop, cRight, cBottom, cLeft;

    public RoundRectShadow() {
        if (shader == null) {
            try {
                String shadowVtx = new String(Files.readAllBytes(Paths.get(getClass().getResource("/resources/shadow.vtx.glsl").toURI())));
                String shadowFrg = new String(Files.readAllBytes(Paths.get(getClass().getResource("/resources/shadow.frg.glsl").toURI())));
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
                throw new RuntimeException("Invalid shader file", e);
            }
        }
    }

    @Override
    public ShaderProgram getShader() {
        return shader;
    }

    @Override
    public ArrayList<MaterialValue> getValues() {
        if (values.isEmpty()) {
            float width = Math.max(1, this.width);
            float height = Math.max(1, this.height);
            float alpha = Math.min(1, Math.max(0, this.alpha));
            float cTop = Math.max(0, Math.min(height / 2f, Math.min(width / 2f, this.cTop)));
            float cRight = Math.max(0, Math.min(height / 2f, Math.min(width / 2f, this.cRight)));
            float cBottom = Math.max(0, Math.min(height / 2f, Math.min(width / 2f, this.cBottom)));
            float cLeft = Math.max(0, Math.min(height / 2f, Math.min(width / 2f, this.cLeft)));

            float sigma = Math.max(0.0001f, blur) / 3f;
            float pad = sigma * 3f;

            // Internal box
            float arc = Math.max(Math.max(Math.max(cTop, cRight), cBottom), cLeft);
            float dc = -0.7071f * arc + arc + pad;
            if (dc >= width / 2f || dc >= height / 2f) {
                values.add(new MaterialValue("inbox", new float[]{-1, -1, -1, -1}));
            } else {
                values.add(new MaterialValue("inbox", new float[]{x + dc, y + dc, x + width - dc, y + height - dc}));
            }

            values.add(new MaterialValue("corners", new float[]{cTop, cRight, cBottom, cLeft}));
            values.add(new MaterialValue("box", new float[]{x, y, x + width, y + height}));
            values.add(new MaterialValue("color", new float[]{0, 0, 0, alpha}));
            values.add(new MaterialValue("sigma", sigma));
        }
        return values;
    }

    public float getBlur() {
        return blur;
    }

    public void setBlur(float blur) {
        values.clear();
        this.blur = blur;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        values.clear();
        this.alpha = alpha;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void setBox(float x, float y, float width, float height) {
        values.clear();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getTopCorner() {
        return cTop;
    }

    public float getRightCorner() {
        return cRight;
    }

    public float getBottomCorner() {
        return cBottom;
    }

    public float getLeftCorner() {
        return cLeft;
    }

    public void setCorners(float top, float right, float bottom, float left) {
        values.clear();
        this.cTop = top;
        this.cRight = right;
        this.cBottom = bottom;
        this.cLeft = left;
    }
}
