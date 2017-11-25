package flat.screen;

import flat.objects.ContextFrame;
import flat.screen.enums.Face;
import flat.screen.enums.MathFunction;
import flat.screen.enums.MathOperation;
import flat.screen.enums.PolygonMode;
import flat.svg.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Context {

    private final Thread graphicThread;
    private final AtomicBoolean current = new AtomicBoolean();

    private ArrayList<WeakReference<ContextObject>> contextObjects = new ArrayList<>();
    private ArrayList<ContextObject> disposeList = new ArrayList<>();

    private ContextFrame contextFrame;

    protected Context(Thread graphicThread) {
        this.graphicThread = graphicThread;
    }

    protected void setSize(int width, int height) {

    }

    protected void assignObject(ContextObject contextObject) {

    }

    protected void disposeObject(ContextObject contextObject) {

    }

    protected void setCurrent(boolean current) {
        this.current.set(current);
    }

    public boolean isCurrent() {
        return Thread.currentThread() == graphicThread && current.get();
    }

    public void assertIfIsCurrent() {
        if (!isCurrent()) throw new RuntimeException("Invalid context acess");
    }

    public void setContextFrame(ContextFrame contextFrame) {
        assertIfIsCurrent();
        this.contextFrame = contextFrame;
    }

    public ContextFrame getContextFrame() {
        assertIfIsCurrent();
        return contextFrame;
    }

    public void setViewport(int x, int y, int width, int height) {
        assertIfIsCurrent();
    }

    public int getViewportX() {
        assertIfIsCurrent();
        return 0;
    }

    public int getViewportY() {
        assertIfIsCurrent();
        return 0;
    }

    public int getViewportWidth() {
        assertIfIsCurrent();
        return 0;
    }

    public int getViewportHeight() {
        assertIfIsCurrent();
        return 0;
    }

    public void clear(int color) {
        assertIfIsCurrent();
    }

    public void clear(int color, boolean depth, boolean stencil) {
        assertIfIsCurrent();
    }

    public void setBlendFunction() {

    }

    public MathFunction getDepthFunction() {
        assertIfIsCurrent();
        return null;
    }

    public void setDepthFunction(MathFunction depthFunction) {
        assertIfIsCurrent();
    }

    public MathFunction getStencilFunction() {
        assertIfIsCurrent();
        return null;
    }

    public int getStencilReference() {
        assertIfIsCurrent();
        return 0;
    }

    public int getStencilMask() {
        assertIfIsCurrent();
        return 0;
    }

    public void setStencilFunction(MathFunction function, int ref, int mask) {
        assertIfIsCurrent();

    }

    public MathOperation getStencilFail() {
        assertIfIsCurrent();
        return null;
    }

    public MathOperation getDepthFail() {
        assertIfIsCurrent();
        return null;
    }

    public MathOperation getDepthStencilFail() {
        assertIfIsCurrent();
        return null;
    }

    public void setStencilFailOperation(MathOperation stencilFail, MathOperation depthFail, MathOperation bothFail) {
        assertIfIsCurrent();

    }

    public Face getCullface() {
        assertIfIsCurrent();
        return null;
    }

    public void setCullfaces(Face face) {
        assertIfIsCurrent();
    }

    public PolygonMode getPolygonMode() {
        assertIfIsCurrent();
        return null;
    }

    public void setPolygonMode(PolygonMode mode) {
        assertIfIsCurrent();
    }

    public void setVertexIndices(int[] indices, int offset, int stride) {
        assertIfIsCurrent();
    }

    public void setVertexAttributes(byte[] data, int offset, int stride, boolean instance) {
        assertIfIsCurrent();
    }

    public void setVertexAttributes(short[] data, int offset, int stride, boolean instance) {
        assertIfIsCurrent();
    }

    public void setVertexAttributes(int[] data, int offset, int stride, boolean instance) {
        assertIfIsCurrent();
    }

    public void setVertexAttributes(float[] data, int offset, int stride, boolean instance) {
        assertIfIsCurrent();
    }

    public void drawArrays(int vertices, int instances) {
        assertIfIsCurrent();
    }

    public void drawElements(int indices, int instances) {
        assertIfIsCurrent();
    }

    public void drawSvg(Svg svg, boolean fill, Paint paint, double x, double y, double scaleX, double scaleY, double rot) {
        assertIfIsCurrent();

    }

    public void drawSvg(Svg svg, int count, SvgInstance... instances) {
        assertIfIsCurrent();
    }

    public byte[] read(byte[] data, int x, int y, int width, int height, int stride, int offset) {
        assertIfIsCurrent();
        return data;
    }
}
