package flat.screen;

import flat.math.Matrix4;
import flat.shader.ShaderProgram;
import flat.image.*;
import flat.model.*;

public class Context {

    public void setShaderProgram(ShaderProgram program) {

    }

    public ShaderProgram getShaderProram() {
        return null;
    }

    public void drawImage(Image image, double x, double y) {

    }

    public void drawImage(Image image, double x, double y, double width, double height) {

    }

    public void drawImage(Image image, double srcX, double srcY, double srcWidth, double srcHeight,
                          double desX, double desY, double desWidth, double desHeight) {

    }

    public void drawSvg(Svg svg, boolean fill, Paint paint, double x, double y) {

    }

    public void drawSvg(Svg svg, boolean fill, Paint paint, double x, double y, double width, double height) {

    }

    public void drawModel(Model model, Matrix4 transform, int animIndex, float animPos) {

    }

    public void drawImages(Image image, int count, ImageInstance... instances) {

    }

    public void drawSvgs(Svg svg, int count, SvgInstance... instances) {

    }

    public void drawModels(Model model, int count, ModelInstance... instances) {

    }

    public byte[] read(byte[] data, int x, int y, int width, int height, int stride, int offset) {
        return null;
    }

    public void write(byte[] data, int x, int y, int width, int height, int stride, int offset) {

    }
}
