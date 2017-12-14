package flat.graphics.effects;

import flat.graphics.context.Context;
import flat.graphics.context.objects.ShaderProgram;

import java.nio.file.Files;
import java.nio.file.Paths;

public final class RoundRectShadow extends Effect {

    private float blur, alpha, x, y, width, height, cTop, cRight, cBottom, cLeft;
    private static ShaderProgram shader;

    public RoundRectShadow() {
        if (shader == null) {
            try {
                String shadowVtx = new String(Files.readAllBytes(Paths.get(getClass().getResource("/resources/shadow.vtx.glsl").toURI())));
                String shadowFrg = new String(Files.readAllBytes(Paths.get(getClass().getResource("/resources/shadow.frg.glsl").toURI())));
                shader = new ShaderProgram(shadowVtx, shadowFrg);
                shader.compile();
                if (!shader.isCompiled()) {
                    throw new Exception(shader.getLog());
                }
            } catch (Exception e) {
                throw new RuntimeException("Invalid shader file", e);
            }
        }
    }

    @Override
    public void applyEffect(Context context) {
        // Shader
        context.setImageShader(shader);

        // Clamp values
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
            shader.setVec4("inbox", -1, -1, -1, -1);
        } else {
            shader.setVec4("inbox", x + dc, y + dc, x + width - dc, y + height - dc);
        }

        shader.setFloatArray("corners", cTop, cRight, cBottom, cLeft);
        shader.setVec4("box", x, y, x + width, y + height);
        shader.setVec4("color", 0, 0, 0, alpha);
        shader.setFloat("sigma", sigma);
    }

    public float getBlur() {
        return blur;
    }

    public void setBlur(float blur) {
        this.blur = blur;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
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
        this.cTop = top;
        this.cRight = right;
        this.cBottom = bottom;
        this.cLeft = left;
    }
}
