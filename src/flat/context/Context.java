package flat.context;

import flat.math.Matrix4;
import flat.image.*;
import flat.model.*;
import flat.svg.*;

public class Context {
    public enum MathFunction {ALWAYS, NEVER, LESS, EQUAL, LEQUAL, GREATER, NOTEQUAL, GEQUAL}
    public enum MathOperation {KEEP, ZERO, REPLACE, INCR, INCR_WRAP, DECR, DECR_WRAP, INVERT}
    public enum Face {BACK, FRONT, ALL}
    public enum PolygonMode {POINT, LINE, FILL}

    private boolean current;

    Context() {
    }

    void setSize(int width, int height) {

    }

    public boolean isCurrent() {
        return current;
    }

    public void setProjection(Matrix4 projection) {

    }

    public Matrix4 getProjection() {
        return null;
    }

    public void setViewport(int x, int y, int width, int height) {

    }

    public void clear(int color) {

    }

    public void clear(int color, boolean depth, boolean stencil) {

    }

    public MathFunction getDepthFunction() {
        return null;
    }

    public void setDepthFunction(MathFunction depthFunction) {

    }

    public MathFunction getStencilFunction() {
        return null;
    }

    public int getStencilReference() {
        return 0;
    }

    public int getStencilMask() {
        return 0;
    }

    public void setStencilFunction(MathFunction function, int ref, int mask) {

    }

    public MathOperation getStencilFail() {
        return null;
    }

    public MathOperation getDepthFail() {
        return null;
    }

    public MathOperation getDepthStencilFail() {
        return null;
    }

    public void setStencilFailOperation(MathOperation stencilFail, MathOperation depthFail, MathOperation bothFail) {

    }

    public Face getCullface() {
        return null;
    }

    public void setCullfaces(Face face) {

    }

    public PolygonMode getPolygonMode() {
        return null;
    }

    public void setPolygonMode(PolygonMode mode) {

    }

    public void drawImage(Image image, double x, double y) {

    }

    public void drawImage(Image image, double x, double y, double width, double height) {

    }

    public void drawImage(Image image, double srcX, double srcY, double srcWidth, double srcHeight,
                          double desX, double desY, double desWidth, double desHeight) {

    }

    public void drawImages(Image image, int count, ImageInstance... instances) {

    }

    public void drawSvg(Svg svg, boolean fill, Paint paint, double x, double y) {

    }

    public void drawSvgs(Svg svg, int count, SvgInstance... instances) {

    }

    public void drawSvg(Svg svg, boolean fill, Paint paint, double x, double y, double width, double height) {

    }

    public void drawModel(Model model, Matrix4 transform, int animIndex, float animPos) {

    }

    public void drawModels(Model model, int count, ModelInstance... instances) {

    }

    public void drawModelsInstanced(Model model, int count, Matrix4... instances) {

    }

    public byte[] read(byte[] data, int x, int y, int width, int height, int stride, int offset) {
        return data;
    }
}
