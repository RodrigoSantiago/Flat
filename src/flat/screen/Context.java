package flat.screen;

import flat.objects.ContextObject;
import flat.objects.ContextFrame;
import flat.svg.*;

public class Context {
    public enum MathFunction {ALWAYS, NEVER, LESS, EQUAL, LEQUAL, GREATER, NOTEQUAL, GEQUAL}
    public enum MathOperation {KEEP, ZERO, REPLACE, INCR, INCR_WRAP, DECR, DECR_WRAP, INVERT}
    public enum Face {BACK, FRONT, ALL}
    public enum PolygonMode {POINT, LINE, FILL}

    protected volatile boolean current;
    private ContextFrame contextFrame;

    Context() {
    }

    void setSize(int width, int height) {

    }

    public void disposeObject(ContextObject contextObject) {

    }

    public void setContextFrame(ContextFrame contextFrame) {
        this.contextFrame = contextFrame;
    }

    public ContextFrame getContextFrame() {
        return contextFrame;
    }

    public boolean isCurrent() {
        return current;
    }

    public void setViewport(int x, int y, int width, int height) {

    }

    public int getViewportX() {
        return 0;
    }

    public int getViewportY() {
        return 0;
    }

    public int getViewportWidth() {
        return 0;
    }

    public int getViewportHeight() {
        return 0;
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

    public void setVertexIndices(int[] indices, int offset, int stride) {

    }

    public void setVertexAttributes(byte[] data, int offset, int stride, boolean instance) {

    }

    public void setVertexAttributes(short[] data, int offset, int stride, boolean instance) {

    }

    public void setVertexAttributes(int[] data, int offset, int stride, boolean instance) {

    }

    public void setVertexAttributes(float[] data, int offset, int stride, boolean instance) {

    }

    public void drawArrays(int vertices, int instances) {

    }

    public void drawElements(int indices, int instances) {

    }

    public void drawSvg(Svg svg, boolean fill, Paint paint, double x, double y, double scaleX, double scaleY, double rot) {

    }

    public void drawSvg(Svg svg, int count, SvgInstance... instances) {

    }

    public byte[] read(byte[] data, int x, int y, int width, int height, int stride, int offset) {
        return data;
    }
}
