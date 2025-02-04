package flat.graphics.context;

import flat.backend.*;
import flat.graphics.SmartContext;
import flat.graphics.context.enums.*;
import flat.graphics.text.Align;
import flat.math.Affine;
import flat.math.Mathf;
import flat.math.shapes.PathIterator;
import flat.math.shapes.Shape;
import flat.math.shapes.Stroke;
import flat.window.Application;
import flat.window.Window;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;

public class Context {

    public final long id;
    public final long svgId;

    private Window window;
    private SmartContext smartContext;
    private Thread thread;
    private boolean disposed;
    private static ArrayList<Context> openContexts = new ArrayList<>();

    // ---- Core ---- //
    private int clearColor, clearStencil;
    private double clearDepth;
    private int viewX, viewY, viewWidth, viewHeight;

    private boolean scissorEnabled;
    private int scissorX, scissorY, scissorWidth, scissorHeight;

    private boolean rasterizeEnabled;
    private boolean rMask, gMask, bMask, aMask;
    private int pixelPackAlignment, pixelPackRowLength, pixelPackSkipPixels, pixelPackSkipRows, pixelPackImageHeight, pixelPackSkipImages;
    private int pixelUnpackAlignment, pixelUnpackRowLength, pixelUnpackSkipPixels, pixelUnpackSkipRows, pixelUnpackImageHeight, pixelUnpackSkipImages;

    private boolean depthEnabled;
    private boolean depthMask;
    private double depthNear, depthFar;
    private MathFunction depthFunction;

    private boolean stencilEnabled;
    private int stencilBackMask, stencilFrontMask;
    private MathFunction stencilBackFunction, stencilFrontFunction;
    private int stencilBackFunRef, stencilBackFunMask, stencilFrontFunRef, stencilFrontFunMask;
    private MathOperation stencilBackSFail, stencilBackDFail, stencilBackDPass, stencilFrontSFail, stencilFrontDFail, stencilFrontDPass;

    private boolean blendEnabled;
    private BlendFunction blendSrcColorFun, blendSrcAlphaFun, blendDstColorFun, blendDstAlphaFun;
    private BlendEquation blendColorEquation, blendAlphaEquation;
    private int blendColor;

    private boolean cullFaceEnabled, cullFrontFace, cullBackFace;
    private boolean clockWiseFrontFace;

    private boolean multiSampleEnabled;
    private float lineWidth;

    // ---- Objects ---- //
    private Frame frame;
    private ShaderProgram shaderProgram;
    private VertexArray vertexArray;
    private Render render;
    private int activeTexture;
    private BufferObject[] buffers = new BufferObject[8];
    private Texture[] textures = new Texture[32];

    private boolean unbindShaderProgram, unbindVertexArray, unbindRender;
    private boolean[] unbindBuffer = new boolean[8];
    private boolean[] unbindTexture = new boolean[32];
    private BufferObject preVertexElementBuffer;

    // ---- SVG ---- //
    private boolean svgMode;
    private boolean svgAntialias;
    private Paint svgPaint;
    private float svgStrokeWidth;
    private LineCap svgLineCap;
    private LineJoin svgLineJoin;
    private float svgMiterLimit;
    private float svgDashPhase;
    private float[] svgDash;

    private Font svgTextFont;
    private float svgTextScale, svgTextSpacing, svgTextBlur;
    private Align.Vertical svgTextVerticalAlign;
    private Align.Horizontal svgTextHorizontalAlign;

    private Affine svgTransform;
    private ArrayList<Runnable> disposeTasks = new ArrayList<>();

    public static Context create(Window window, long id, long svgId) {
        if (window.getContext() != null) {
            throw new RuntimeException("The Window already have a context");
        } else {
            return new Context(window, id, svgId);
        }
    }

    private Context(Window window, long id, long svgId) {
        this.window = window;
        this.id = id;
        this.svgId = svgId;
        this.thread = Thread.currentThread();
        init();
    }

    public static void propagateHardFlush() {
        for (var context : openContexts) {
            if (context.getWindow().isAssigned()) {
                context.hardFlush();
            }
        }
    }

    public Window getWindow() {
        return window;
    }

    void checkDisposed() {
        if (disposed) {
            throw new RuntimeException("Context is disposed.");
        }
    }

    public SmartContext getSmartContext() {
        if (smartContext == null) {
            this.smartContext = new SmartContext(this);
        }
        return smartContext;
    }

    Runnable createSyncDestroyTask(Runnable task) {
        disposeTasks.add(task);
        return () -> {
            window.runSync(() -> {
                if (disposeTasks.remove(task)) {
                    task.run();
                }
            });
        };
    }

    private void init() {
        // ---- Core ---- //
        clearColor = 0;
        clearDepth = 1;
        clearStencil = 0;

        viewX = 0;
        viewY = 0;
        viewWidth = GL.GetViewportWidth();
        viewHeight = GL.GetViewportHeight();

        scissorEnabled = false;
        scissorX = 0;
        scissorY = 0;
        scissorWidth = GL.GetScissorWidth();
        scissorHeight = GL.GetScissorHeight();

        rasterizeEnabled = true;
        rMask = true;
        gMask = true;
        bMask = true;
        aMask = true;
        pixelPackAlignment = 4;
        pixelPackRowLength = 0;
        pixelPackSkipPixels = 0;
        pixelPackSkipRows = 0;
        pixelPackImageHeight = 0;
        pixelPackSkipImages = 0;
        pixelUnpackAlignment = 4;
        pixelUnpackRowLength = 0;
        pixelUnpackSkipPixels = 0;
        pixelUnpackSkipRows = 0;
        pixelUnpackImageHeight = 0;
        pixelUnpackSkipImages = 0;

        depthEnabled = false;
        depthMask = true;
        depthNear = 0;
        depthFar = 1;
        depthFunction = MathFunction.LESS;

        stencilEnabled = false;
        stencilBackMask = 1;
        stencilFrontMask = 1;
        stencilBackFunction = MathFunction.ALWAYS;
        stencilFrontFunction = MathFunction.ALWAYS;
        stencilBackFunRef = 0;
        stencilBackFunMask = 1;
        stencilFrontFunRef = 0;
        stencilFrontFunMask = 1;
        stencilBackSFail = MathOperation.KEEP;
        stencilBackDFail = MathOperation.KEEP;
        stencilBackDPass = MathOperation.KEEP;
        stencilFrontSFail = MathOperation.KEEP;
        stencilFrontDFail = MathOperation.KEEP;
        stencilFrontDPass = MathOperation.KEEP;

        blendEnabled = false;
        blendSrcColorFun = BlendFunction.ONE;
        blendSrcAlphaFun = BlendFunction.ONE;
        blendDstColorFun = BlendFunction.ZERO;
        blendDstAlphaFun = BlendFunction.ZERO;
        blendColorEquation = BlendEquation.ADD;
        blendAlphaEquation = BlendEquation.ADD;
        blendColor = 0;

        cullFaceEnabled = false;
        cullFrontFace = false;
        cullBackFace = true;
        clockWiseFrontFace = false;

        multiSampleEnabled = true;

        lineWidth = 1f;

        // ---- SVG ---- //
        svgMode = false;
        svgAntialias = false;
        svgPaint = Paint.color(0x000000FF);
        svgStrokeWidth = 1f;
        svgLineCap = LineCap.BUTT;
        svgLineJoin = LineJoin.ROUND;
        svgMiterLimit = 10.0f;
        svgDashPhase = 0;
        svgDash = null;

        svgTextFont = Font.getDefault();
        svgTextScale = 1.0f;
        svgTextSpacing = 1.0f;
        svgTextBlur = 1f;
        svgTextVerticalAlign = Align.Vertical.TOP;
        svgTextHorizontalAlign = Align.Horizontal.LEFT;

        svgTransform = new Affine();

        openContexts.add(this);
    }

    public void dispose() {
        checkDisposed();

        openContexts.remove(this);
        hardFlush();
        refreshBinds();
        bindFrame(null);

        for (Runnable disposeTask : disposeTasks) {
            disposeTask.run();
        }
        disposeTasks.clear();

        smartContext = null;
        frame = null;
        shaderProgram = null;
        vertexArray = null;
        render = null;
        buffers = null;
        textures = null;

        disposed = true;
    }

    // ---- CORE ---- //

    public void softFlush() {
        checkDisposed();

        svgEnd();
        refreshBinds();
    }

    public void hardFlush() {
        checkDisposed();

        svgEnd();
        GL.Flush();
    }

    public void finish() {
        checkDisposed();

        svgEnd();
        GL.Finish();
    }

    public void clear(boolean color, boolean depth, boolean stencil) {
        checkDisposed();

        svgEnd();
        GL.Clear((color ? GLEnums.CB_COLOR_BUFFER_BIT : 0) | (depth ? GLEnums.CB_DEPTH_BUFFER_BIT : 0) | (stencil ? GLEnums.CB_STENCIL_BUFFER_BIT : 0));
    }

    public void setClearColor(int color) {
        checkDisposed();

        if (clearColor != color) {
            svgEnd();
            GL.SetClearColor(clearColor = color);
        }
    }

    public int getClearColor() {
        return clearColor;
    }

    public void setClearDepth(double depth) {
        checkDisposed();

        if (clearDepth != depth) {
            svgEnd();
            GL.SetClearDepth(clearDepth = depth);
        }
    }

    public double getClearDepth() {
        return clearDepth;
    }

    public void setClearStencil(int stencil) {
        checkDisposed();

        if (clearStencil != stencil) {
            svgEnd();
            GL.SetClearDepth(clearStencil = stencil);
        }
    }

    public int getClearStencil() {
        return clearStencil;
    }

    public void setViewPort(int x, int y, int width, int height) {
        checkDisposed();

        if (viewX != x || viewY != y || viewWidth != width || viewHeight != height) {
            svgEnd();
            GL.SetViewport(viewX = x, viewY = y, viewWidth = width, viewHeight = height);
        }
    }

    public int getViewX() {
        return viewX;
    }

    public int getViewY() {
        return viewY;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    public int getWidth() {
        return window.getClientWidth();
    }

    public int getHeight() {
        return window.getClientHeight();
    }

    public float getDensity() {
        return window.getDpi();
    }

    public void setScissorEnabled(boolean enable) {
        checkDisposed();

        if (scissorEnabled != enable) {
            svgEnd();
            GL.EnableScissorTest(scissorEnabled = enable);
        }
    }

    public void setRasterizeEnabled(boolean enable) {
        checkDisposed();

        if (rasterizeEnabled != enable) {
            svgEnd();
            GL.EnableRasterizer(rasterizeEnabled = enable);
        }
    }

    public boolean isRasterizeEnabled() {
        return rasterizeEnabled;
    }

    public void setColorMask(boolean r, boolean g, boolean b, boolean a) {
        checkDisposed();

        if (r != rMask || g != gMask || b != bMask || a != aMask) {
            svgEnd();
            GL.SetColorMask(rMask = r, gMask = g, bMask = b, aMask = a);
        }
    }

    public boolean getRedMask() {
        return rMask;
    }

    public boolean getGreenMask() {
        return gMask;
    }

    public boolean getBlueMask() {
        return bMask;
    }

    public boolean getAlphaMask() {
        return aMask;
    }

    public void setPixelPack(int alignment, int rowLength, int skipPixels, int skipRows) {
        checkDisposed();

        setPixelPack(alignment, rowLength, skipPixels, skipRows, pixelPackImageHeight, pixelPackSkipImages);
    }

    public void setPixelPack(int alignment, int rowLength, int skipPixels, int skipRows, int imageHeight, int skipImages) {
        checkDisposed();

        if (pixelPackAlignment != alignment || pixelPackRowLength != rowLength || pixelPackSkipPixels != skipPixels
                || pixelPackSkipRows != skipRows || pixelPackImageHeight != imageHeight || pixelPackSkipImages != skipImages) {
            svgEnd();
            if (pixelPackAlignment != alignment)
                GL.SetPixelStore(GLEnums.PS_PACK_ALIGNMENT, pixelPackAlignment = alignment);
            if (pixelPackRowLength != rowLength)
                GL.SetPixelStore(GLEnums.PS_PACK_ROW_LENGTH, pixelPackRowLength = rowLength);
            if (pixelPackSkipPixels != skipPixels)
                GL.SetPixelStore(GLEnums.PS_PACK_SKIP_PIXELS, pixelPackSkipPixels = skipPixels);
            if (pixelPackSkipRows != skipRows)
                GL.SetPixelStore(GLEnums.PS_PACK_SKIP_ROWS, pixelPackSkipRows = skipRows);
            if (pixelPackImageHeight != imageHeight)
                GL.SetPixelStore(GLEnums.PS_PACK_IMAGE_HEIGHT, pixelPackImageHeight = imageHeight);
            if (pixelPackSkipImages != skipImages)
                GL.SetPixelStore(GLEnums.PS_PACK_SKIP_IMAGES, pixelPackSkipImages = skipImages);
        }
    }

    public int getPixelPackAlignment() {
        return pixelPackAlignment;
    }

    public int getPixelPackRowLength() {
        return pixelPackRowLength;
    }

    public int getPixelPackSkipPixels() {
        return pixelPackSkipPixels;
    }

    public int getPixelPackSkipRows() {
        return pixelPackSkipRows;
    }

    public int getPixelPackImageHeight() {
        return pixelPackImageHeight;
    }

    public int getPixelPackSkipImages() {
        return pixelPackSkipImages;
    }

    public void setPixelUnpack(int alignment, int rowLength, int skipPixels, int skipRows) {
        checkDisposed();

        setPixelUnpack(alignment, rowLength, skipPixels, skipRows, pixelUnpackImageHeight, pixelUnpackSkipImages);
    }

    public void setPixelUnpack(int alignment, int rowLength, int skipPixels, int skipRows, int imageHeight, int skipImages) {
        checkDisposed();

        if (pixelUnpackAlignment != alignment || pixelUnpackRowLength != rowLength || pixelUnpackSkipPixels != skipPixels
                || pixelUnpackSkipRows != skipRows || pixelUnpackImageHeight != imageHeight || pixelUnpackSkipImages != skipImages) {
            svgEnd();
            if (pixelUnpackAlignment != alignment)
                GL.SetPixelStore(GLEnums.PS_UNPACK_ALIGNMENT, pixelUnpackAlignment = alignment);
            if (pixelUnpackRowLength != rowLength)
                GL.SetPixelStore(GLEnums.PS_UNPACK_ROW_LENGTH, pixelUnpackRowLength = rowLength);
            if (pixelUnpackSkipPixels != skipPixels)
                GL.SetPixelStore(GLEnums.PS_UNPACK_SKIP_PIXELS, pixelUnpackSkipPixels = skipPixels);
            if (pixelUnpackSkipRows != skipRows)
                GL.SetPixelStore(GLEnums.PS_UNPACK_SKIP_ROWS, pixelUnpackSkipRows = skipRows);
            if (pixelUnpackImageHeight != imageHeight)
                GL.SetPixelStore(GLEnums.PS_UNPACK_IMAGE_HEIGHT, pixelUnpackImageHeight = imageHeight);
            if (pixelUnpackSkipImages != skipImages)
                GL.SetPixelStore(GLEnums.PS_UNPACK_SKIP_IMAGES, pixelUnpackSkipImages = skipImages);
        }
    }

    public int getPixelUnpackAlignment() {
        return pixelUnpackAlignment;
    }

    public int getPixelUnpackRowLength() {
        return pixelUnpackRowLength;
    }

    public int getPixelUnpackSkipPixels() {
        return pixelUnpackSkipPixels;
    }

    public int getPixelUnpackSkipRows() {
        return pixelUnpackSkipRows;
    }

    public int getPixelUnpackImageHeight() {
        return pixelUnpackImageHeight;
    }

    public int getPixelUnpackSkipImages() {
        return pixelUnpackSkipImages;
    }

    public void setDepthEnabled(boolean enable) {
        checkDisposed();

        if (depthEnabled != enable) {
            svgEnd();
            GL.EnableDepthTest(depthEnabled = enable);
        }
    }

    public boolean isDepthEnabled() {
        return depthEnabled;
    }

    public void setDepthMask(boolean mask) {
        checkDisposed();

        if (depthMask != mask) {
            svgEnd();
            GL.SetDepthMask(depthMask = mask);
        }
    }

    public boolean getDepthMask() {
        return depthMask;
    }

    public void setDepthFunction(MathFunction function) {
        checkDisposed();

        if (depthFunction != function) {
            svgEnd();
            GL.SetDepthFunction((depthFunction = function).getInternalEnum());
        }
    }

    public MathFunction getDepthFunction() {
        return depthFunction;
    }

    public void setDepthRange(double near, double far) {
        checkDisposed();

        if (depthNear != near || depthFar != far) {
            svgEnd();
            GL.SetDepthRange(depthNear = near, depthFar = far);
        }
    }

    public double getDepthNear() {
        return depthNear;
    }

    public double getDepthFar() {
        return depthFar;
    }

    public void setStencilEnabled(boolean enable) {
        checkDisposed();

        if (stencilEnabled != enable) {
            svgEnd();
            GL.EnableStencilTest(stencilEnabled = enable);
        }
    }

    public boolean isStencilEnabled() {
        return stencilEnabled;
    }

    public void setStencilMask(int frontMask, int backMask) {
        checkDisposed();

        if (stencilFrontMask != frontMask) {
            svgEnd();
            GL.SetStencilMask(GLEnums.FC_FRONT, stencilFrontMask = frontMask);
        }
        if (stencilBackMask != backMask) {
            svgEnd();
            GL.SetStencilMask(GLEnums.FC_BACK, stencilBackMask = backMask);
        }
    }

    public int getStencilBackMask() {
        return stencilBackMask;
    }

    public int getStencilFrontMask() {
        return stencilFrontMask;
    }

    public void setStencilFrontFunction(MathFunction function, int ref, int mask) {
        checkDisposed();

        if (stencilFrontFunction != function || stencilFrontFunRef != ref || stencilFrontFunMask != mask) {
            svgEnd();
            GL.SetStencilFunction(GLEnums.FC_FRONT, (stencilFrontFunction = function).getInternalEnum(),
                    stencilFrontFunRef = ref, stencilFrontFunMask = mask);
        }
    }

    public MathFunction getStencilFrontFunction() {
        return stencilFrontFunction;
    }

    public int getStencilFrontFunRef() {
        return stencilFrontFunRef;
    }

    public int getStencilFrontFunMask() {
        return stencilFrontFunMask;
    }

    public void setStencilBackFunction(MathFunction function, int ref, int mask) {
        checkDisposed();

        if (stencilBackFunction != function || stencilBackFunRef != ref || stencilBackFunMask != mask) {
            svgEnd();
            GL.SetStencilFunction(GLEnums.FC_BACK, (stencilBackFunction = function).getInternalEnum(),
                    stencilBackFunRef = ref, stencilBackFunMask = mask);
        }
    }

    public MathFunction getStencilBackFunction() {
        return stencilBackFunction;
    }

    public int getStencilBackFunRef() {
        return stencilBackFunRef;
    }

    public int getStencilBackFunMask() {
        return stencilBackFunMask;
    }

    public void setStencilFrontOperation(MathOperation SFail, MathOperation DFail, MathOperation DPass) {
        checkDisposed();

        if (stencilFrontSFail != SFail || stencilFrontDFail != DFail || stencilFrontDPass != DPass) {
            svgEnd();
            GL.SetStencilOperation(GLEnums.FC_FRONT, (stencilFrontSFail = SFail).getInternalEnum(),
                    (stencilFrontDFail = DFail).getInternalEnum(), (stencilFrontDPass = DPass).getInternalEnum());
        }
    }

    public MathOperation getStencilFrontSFail() {
        return stencilFrontSFail;
    }

    public MathOperation getStencilFrontDFail() {
        return stencilFrontDFail;
    }

    public MathOperation getStencilFrontDPass() {
        return stencilFrontDPass;
    }

    public void setStencilBackOperation(MathOperation SFail, MathOperation DFail, MathOperation DPass) {
        checkDisposed();

        if (stencilBackSFail != SFail || stencilBackDFail != DFail || stencilBackDPass != DPass) {
            svgEnd();
            GL.SetStencilOperation(GLEnums.FC_BACK, (stencilBackSFail = SFail).getInternalEnum(),
                    (stencilBackDFail = DFail).getInternalEnum(), (stencilBackDPass = DPass).getInternalEnum());
        }
    }

    public MathOperation getStencilBackSFail() {
        return stencilBackSFail;
    }

    public MathOperation getStencilBackDFail() {
        return stencilBackDFail;
    }

    public MathOperation getStencilBackDPass() {
        return stencilBackDPass;
    }

    public void setBlendEnabled(boolean enable) {
        checkDisposed();

        if (blendEnabled != enable) {
            svgEnd();
            GL.EnableBlend(blendEnabled = enable);
        }
    }

    public boolean isBlendEnabled() {
        return blendEnabled;
    }

    public void setBlendFunction(BlendFunction srcColor, BlendFunction dstColor, BlendFunction srcAlpha, BlendFunction dstAlpha) {
        checkDisposed();

        if (blendSrcColorFun != srcColor || blendDstColorFun != dstColor || blendSrcAlphaFun != srcAlpha || blendDstAlphaFun != dstAlpha) {
            svgEnd();
            GL.SetBlendFunction((blendSrcColorFun = srcColor).getInternalEnum(),
                    (blendDstColorFun = dstColor).getInternalEnum(),
                    (blendSrcAlphaFun = srcAlpha).getInternalEnum(),
                    (blendDstAlphaFun = dstAlpha).getInternalEnum());
        }
    }

    public BlendFunction getBlendSrcColorFun() {
        return blendSrcColorFun;
    }

    public BlendFunction getBlendSrcAlphaFun() {
        return blendSrcAlphaFun;
    }

    public BlendFunction getBlendDstColorFun() {
        return blendDstColorFun;
    }

    public BlendFunction getBlendDstAlphaFun() {
        return blendDstAlphaFun;
    }

    public void setBlendEquation(BlendEquation colorEquation, BlendEquation alphaEquation) {
        checkDisposed();

        if (blendColorEquation != colorEquation || blendAlphaEquation != alphaEquation) {
            svgEnd();
            GL.SetBlendEquation((blendColorEquation = colorEquation).getInternalEnum(),
                    (blendAlphaEquation = alphaEquation).getInternalEnum());
        }
    }

    public BlendEquation getBlendColorEquation() {
        return blendColorEquation;
    }

    public BlendEquation getBlendAlphaEquation() {
        return blendAlphaEquation;
    }

    public void setBlendColor(int color) {
        checkDisposed();

        if (blendColor != color) {
            svgEnd();
            GL.SetBlendColor(blendColor = color);
        }
    }

    public int getBlendColor() {
        return blendColor;
    }

    public void setCullFaceEnabled(boolean enable) {
        checkDisposed();

        if (cullFaceEnabled != enable) {
            svgEnd();
            GL.EnableCullface(cullFaceEnabled = enable);
        }
    }

    public boolean isCullFaceEnabled() {
        return cullFaceEnabled;
    }

    public void setCullFace(boolean frontFace, boolean backFace) {
        checkDisposed();

        if (cullFrontFace != frontFace || cullBackFace != backFace) {
            svgEnd();
            cullFrontFace = frontFace;
            cullBackFace = backFace;
            GL.SetCullface(frontFace ? backFace ? GLEnums.FC_FRONT_AND_BACK : GLEnums.FC_FRONT : backFace ? GLEnums.FC_BACK : 0);
        }
    }

    public boolean isCullFrontFace() {
        return cullFrontFace;
    }

    public boolean isCullBackFace() {
        return cullBackFace;
    }

    public void setClockWiseFrontFace(boolean clockWise) {
        checkDisposed();

        if (clockWiseFrontFace != clockWise) {
            svgEnd();
            GL.SetFrontFace((clockWiseFrontFace = clockWise) ? GLEnums.FF_CW : GLEnums.FF_CCW);
        }
    }

    public boolean isClockWiseFrontFace() {
        return clockWiseFrontFace;
    }

    public void setMultiSampleEnabled(boolean enable) {
        checkDisposed();

        if (multiSampleEnabled != enable) {
            svgEnd();
            GL.EnableMultisample(multiSampleEnabled = enable);
        }
    }

    public boolean isMultiSampleEnabled() {
        return multiSampleEnabled;
    }

    public void setLineWidth(float width) {
        checkDisposed();

        if (lineWidth != width) {
            svgEnd();
            GL.SetLineWidth(lineWidth = width);
        }
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void readPixels(int x, int y, int width, int height, int offset) {
        softFlush();
        GL.ReadPixels(x, y, width, height, GLEnums.DT_INT, offset);
    }

    public void readPixels(int x, int y, int width, int height, Buffer data, int offset) {
        softFlush();
        GL.ReadPixelsBuffer(x, y, width, height, GLEnums.DT_INT, data, offset);
    }

    public void readPixels(int x, int y, int width, int height, int[] data, int offset) {
        softFlush();
        GL.ReadPixelsI(x, y, width, height, data, offset);
    }

    public void readPixels(int x, int y, int width, int height, byte[] data, int offset) {
        softFlush();
        GL.ReadPixelsB(x, y, width, height, data, offset);
    }

    public void drawArray(VertexMode vertexMode, int first, int count, int instances) {
        softFlush();
        GL.DrawArrays(vertexMode.getInternalEnum(), first, count, instances);
    }

    public void drawElements(VertexMode vertexMode, int first, int count, int instances) {
        softFlush();
        GL.DrawElements(vertexMode.getInternalEnum(), count, instances, GLEnums.DT_INT, first);
    }

    boolean isFrameBound(Frame frame) {
        return this.frame == frame;
    }

    void bindFrame(Frame frame) {
        checkDisposed();

        if (this.frame != frame) {
            svgEnd();

            int id = frame == null ? 0 : frame.getInternalID();
            this.frame = frame;
            GL.FrameBufferBind(GLEnums.FB_FRAMEBUFFER, id);
        }
    }

    void unbindFrame() {
        hardFlush();
        bindFrame(null);
    }

    public Frame getBoundFrame() {
        return frame;
    }

    public void setActiveTexture(int index) {
        checkDisposed();

        if (activeTexture != index) {
            svgEnd();
            GL.SetActiveTexture(activeTexture = index);
        }
    }

    public int getActiveTexture() {
        return activeTexture;
    }

    boolean isTextureBound(Texture texture) {
        for (int i = 0; i < textures.length; i++) {
            if (textures[i] == texture) return true;
        }
        return false;
    }

    void bindTexture(Texture texture, int index) {
        checkDisposed();

        setActiveTexture(index);
        unbindTexture[index] = false;
        if (textures[index] != texture) {
            svgEnd();
            if (texture == null) {
                GL.TextureBind(textures[index].getInternalType(), 0);
            } else {
                GL.TextureBind(texture.getInternalType(), texture.getInternalID());
                texture.setActivePos(index);
            }
            textures[index] = texture;
        }
    }

    void unbindTexture(int index) {
        unbindTexture[index] = true;
    }

    public void clearBindTexture(int index) {
        bindTexture(null, index);
    }

    public Texture getBoundTexture(int index) {
        return textures[index];
    }

    boolean isBufferBound(BufferObject buffer) {
        for (int i = 0; i < buffers.length; i++) {
            if (buffers[i] == buffer) return true;
        }
        return false;
    }

    void bindBuffer(BufferObject buffer, BufferType type) {
        checkDisposed();

        int index = type.ordinal();
        unbindBuffer[index] = false;
        if (buffers[index] != buffer) {
            svgEnd();
            clearBindVertexArray();
            if (buffer == null) {
                GL.BufferBind(type.getInternalEnum(), 0);
            } else {
                GL.BufferBind(type.getInternalEnum(), buffer.getInternalID());
                buffer.setBindType(type);
            }
            if (vertexArray != null && type == BufferType.Element) {
                vertexArray.setElementBuffer(buffer);
            }
            buffers[index] = buffer;
        }
    }

    void unbindBuffer(BufferType type) {
        int index = type.ordinal();
        unbindBuffer[index] = true;
    }

    public void clearBindBuffer(BufferType type) {
        bindBuffer(null, type);
    }

    public BufferObject getBoundBuffer(BufferType type) {
        return buffers[type.ordinal()];
    }

    boolean isVertexArrayBound(VertexArray array) {
        return vertexArray == array;
    }

    void bindVertexArray(VertexArray array) {
        checkDisposed();

        unbindVertexArray = false;
        if (vertexArray != array) {
            svgEnd();

            // unbind element buffer is always false
            final int i = BufferType.Element.ordinal();

            if (array == null) {
                GL.VertexArrayBind(0);
                buffers[i] = preVertexElementBuffer;
                preVertexElementBuffer = null;
            } else {
                GL.VertexArrayBind(array.getInternalID());
                preVertexElementBuffer = buffers[i];
                buffers[i] = array.getElementBuffer();
            }
            vertexArray = array;
        }
    }

    void unbindVertexArray() {
        unbindVertexArray = true;
    }

    private void clearBindVertexArray() {
        if (unbindVertexArray) {
            bindVertexArray(null);
        }
    }

    public VertexArray getBoundVertexArray() {
        return vertexArray;
    }

    boolean isShaderProgramBound(ShaderProgram program) {
        return this.shaderProgram == program;
    }

    void bindShaderProgram(ShaderProgram program) {
        checkDisposed();

        unbindShaderProgram = false;
        if (shaderProgram != program) {
            svgEnd();
            if (program == null) {
                GL.ProgramUse(0);
            } else {
                GL.ProgramUse(program.getInternalID());
            }
            shaderProgram = program;
        }
    }

    void unbindShaderProgram() {
        unbindShaderProgram = true;
    }

    private void clearBindShaderProgram() {
        if (unbindShaderProgram) {
            bindShaderProgram(null);
        }
    }

    public ShaderProgram getBoundShaderProgram() {
        return shaderProgram;
    }

    boolean isRenderBound(Render render) {
        return this.render == render;
    }

    void bindRender(Render render) {
        checkDisposed();

        unbindRender = false;
        if (this.render != render) {
            svgEnd();
            if (render == null) {
                GL.RenderBufferBind(0);
            } else {
                GL.RenderBufferBind(render.getInternalID());
            }
            this.render = render;
        }
    }

    void unbindRender() {
        unbindRender = true;
    }

    private void clearBindRender() {
        if (unbindRender) {
            bindRender(null);
        }
    }

    public Render getBoundRender() {
        return render;
    }

    private void refreshBinds() {
        clearBindShaderProgram();
        clearBindVertexArray();
        clearBindRender();
        refreshBufferBinds();
        refreshTextureBinds();
    }

    private void refreshBufferBinds() {
        BufferType[] values = BufferType.values();
        for (int i = 0; i < unbindBuffer.length; i++) {
            if (unbindBuffer[i]) {
                clearBindBuffer(values[i]);
            }
        }
    }

    private void refreshTextureBinds() {
        for (int i = 0; i < unbindTexture.length; i++) {
            if (unbindTexture[i]) {
                clearBindTexture(i);
            }
        }
    }

    // ---- SVG ---- //
    private void svgBegin() {
        checkDisposed();

        if (!svgMode) {
            refreshBinds();

            svgMode = true;
            SVG.BeginFrame(svgId, viewWidth, viewHeight);

            svgApplyTransformGradients();

            SVG.SetAntiAlias(svgId, svgAntialias);

            SVG.SetStroke(svgId, svgStrokeWidth,
                    svgLineCap.getInternalEnum(),
                    svgLineJoin.getInternalEnum(), svgMiterLimit, svgDash, svgDashPhase);

            if (svgTextFont.isDisposed()) {
                svgTextFont = Font.getDefault();
            }
            SVG.SetFont(svgId, svgTextFont.getInternalID(this));
            SVG.SetFontScale(svgId, svgTextScale);
            SVG.SetFontSpacing(svgId, svgTextSpacing);
            SVG.SetFontBlur(svgId, svgTextBlur);
        }
    }

    private void svgEnd() {
        checkDisposed();

        if (svgMode) {
            svgMode = false;
            SVG.EndFrame(svgId);
            svgRestore();
        }
    }

    public void svgFlush() {
        checkDisposed();

        if (svgMode) {
            SVG.Flush(svgId);
        }
    }

    private void svgRestore() {
        GL.EnableDepthTest(depthEnabled);
        GL.EnableScissorTest(scissorEnabled);

        GL.EnableStencilTest(stencilEnabled);
        GL.SetStencilMask(GLEnums.FC_FRONT, stencilFrontMask);
        GL.SetStencilMask(GLEnums.FC_BACK, stencilBackMask);

        GL.SetStencilFunction(GLEnums.FC_FRONT, stencilFrontFunction.getInternalEnum(), stencilFrontFunRef, stencilFrontFunMask);
        GL.SetStencilFunction(GLEnums.FC_BACK, stencilBackFunction.getInternalEnum(), stencilBackFunRef, stencilBackFunMask);

        GL.EnableBlend(blendEnabled);
        GL.SetBlendFunction(blendSrcColorFun.getInternalEnum(), blendDstColorFun.getInternalEnum(),
                blendSrcAlphaFun.getInternalEnum(), blendDstAlphaFun.getInternalEnum());

        GL.SetCullface(cullFrontFace ? cullBackFace ? GLEnums.FC_FRONT_AND_BACK : GLEnums.FC_FRONT : cullBackFace ? GLEnums.FC_BACK : 0);

        GL.SetFrontFace(clockWiseFrontFace ? GLEnums.FF_CW : GLEnums.FF_CCW);

        GL.SetColorMask(rMask, gMask, bMask, aMask);

        BufferObject uniformBuffer = buffers[BufferType.Uniform.ordinal()];
        GL.BufferBind(BufferType.Uniform.getInternalEnum(), uniformBuffer == null ? 0 : uniformBuffer.getInternalID());

        BufferObject arrayBuffer = buffers[BufferType.Array.ordinal()];
        if (arrayBuffer != null)
            GL.BufferBind(BufferType.Array.getInternalEnum(), arrayBuffer.getInternalID());

        if (vertexArray != null)
            GL.VertexArrayBind(vertexArray.getInternalID());

        if (shaderProgram != null)
            GL.ProgramUse(shaderProgram.getInternalID());

        GL.SetPixelStore(GLEnums.PS_UNPACK_ALIGNMENT, pixelUnpackAlignment);
        GL.SetPixelStore(GLEnums.PS_UNPACK_ROW_LENGTH, pixelUnpackRowLength);
        GL.SetPixelStore(GLEnums.PS_UNPACK_SKIP_PIXELS, pixelUnpackSkipPixels);
        GL.SetPixelStore(GLEnums.PS_UNPACK_SKIP_ROWS, pixelUnpackSkipRows);

        if (textures[0] != null) {
            GL.SetActiveTexture(0);
            GL.TextureBind(textures[0].getInternalType(), textures[0].getInternalID());
        }
        if (activeTexture != 0) {
            GL.SetActiveTexture(activeTexture);
        }
    }

    private void svgApplyTransformGradients() {
        if (svgPaint.isColor()) {
            SVG.SetPaintColor(svgId, svgPaint.color);
        } else if (svgPaint.isLinearGradient()) {
            SVG.SetPaintLinearGradient(svgId, svgPaint.transform, svgPaint.x1, svgPaint.y1, svgPaint.x2, svgPaint.y2, svgPaint.stops.length, svgPaint.stops, svgPaint.colors, svgPaint.cycleMethod.ordinal());
        } else if (svgPaint.isRadialGradient()) {
            SVG.SetPaintRadialGradient(svgId, svgPaint.transform, svgPaint.x1, svgPaint.y1, svgPaint.x2, svgPaint.y2, svgPaint.fx, svgPaint.fy, svgPaint.stops.length, svgPaint.stops, svgPaint.colors, svgPaint.cycleMethod.ordinal());
        } else if (svgPaint.isBoxShadow()) {
            SVG.SetPaintBoxGradient(svgId, svgPaint.transform, svgPaint.x1, svgPaint.y1, svgPaint.x2, svgPaint.y2, svgPaint.corners, svgPaint.blur, svgPaint.stops.length, svgPaint.stops, svgPaint.colors, svgPaint.cycleMethod.ordinal());
        } else if (svgPaint.isImagePattern()) {
            SVG.SetPaintImage(svgId, svgPaint.texture.getInternalID(), svgPaint.transformImage, 0);
        }

        SVG.TransformSet(svgId,
                svgTransform.m00, svgTransform.m10,
                svgTransform.m01, svgTransform.m11,
                svgTransform.m02, svgTransform.m12);
    }

    public void svgAntialias(boolean enabled) {
        checkDisposed();

        if (svgAntialias != enabled) {
            svgAntialias = enabled;
            if (svgMode) {
                SVG.SetAntiAlias(svgId, enabled);
            }
        }
    }

    public boolean svgAntialias() {
        return svgAntialias;
    }

    public void svgPaint(Paint paint) {
        checkDisposed();

        if (!svgPaint.equals(paint)) {
            svgPaint = paint;
            if (svgMode) {
                svgApplyTransformGradients();
            }
        }
    }

    public Paint svgPaint() {
        return svgPaint;
    }

    public void svgStrokeWidth(float strokeWidth) {
        checkDisposed();

        if (svgStrokeWidth != strokeWidth) {
            svgStrokeWidth = strokeWidth;
            if (svgMode) {
                SVG.SetStroke(svgId, svgStrokeWidth,
                        svgLineCap.getInternalEnum(),
                        svgLineJoin.getInternalEnum(), svgMiterLimit, svgDash, svgDashPhase);
            }
        }
    }

    public float svgStrokeWidth() {
        return svgStrokeWidth;
    }

    public void svgLineCap(LineCap lineCap) {
        checkDisposed();

        if (svgLineCap != lineCap) {
            svgLineCap = lineCap;
            if (svgMode) {
                SVG.SetStroke(svgId, svgStrokeWidth,
                        svgLineCap.getInternalEnum(),
                        svgLineJoin.getInternalEnum(), svgMiterLimit, svgDash, svgDashPhase);
            }
        }
    }

    public LineCap svgLineCap() {
        return svgLineCap;
    }

    public void svgLineJoin(LineJoin lineJoin) {
        checkDisposed();

        if (svgLineJoin != lineJoin) {
            svgLineJoin = lineJoin;
            if (svgMode) {
                SVG.SetStroke(svgId, svgStrokeWidth,
                        svgLineCap.getInternalEnum(),
                        svgLineJoin.getInternalEnum(), svgMiterLimit, svgDash, svgDashPhase);
            }
        }
    }

    public LineJoin svgLineJoin() {
        return svgLineJoin;
    }

    public void svgMiterLimit(float miterLimit) {
        checkDisposed();

        if (svgMiterLimit != miterLimit) {
            svgMiterLimit = miterLimit;
            if (svgMode) {
                SVG.SetStroke(svgId, svgStrokeWidth,
                        svgLineCap.getInternalEnum(),
                        svgLineJoin.getInternalEnum(), svgMiterLimit, svgDash, svgDashPhase);
            }
        }
    }

    public float svgMiterLimit() {
        return svgMiterLimit;
    }

    public void svgDash(float[] dash) {
        checkDisposed();

        if (!Arrays.equals(svgDash, dash)) {
            svgDash = dash;
            if (svgMode) {
                SVG.SetStroke(svgId, svgStrokeWidth,
                        svgLineCap.getInternalEnum(),
                        svgLineJoin.getInternalEnum(), svgMiterLimit, svgDash, svgDashPhase);
            }
        }
    }

    public float[] svgDash() {
        return svgDash.clone();
    }

    public void svgDashPhase(float dashPhase) {
        checkDisposed();

        if (svgDashPhase != dashPhase) {
            svgDashPhase = dashPhase;
            if (svgMode) {
                SVG.SetStroke(svgId, svgStrokeWidth,
                        svgLineCap.getInternalEnum(),
                        svgLineJoin.getInternalEnum(), svgMiterLimit, svgDash, svgDashPhase);
            }
        }
    }

    public float svgDashPhase() {
        return svgDashPhase;
    }

    public void svgStroke(Stroke stroker) {
        checkDisposed();

        if (svgStrokeWidth != stroker.getLineWidth() ||
                svgLineCap != LineCap.values()[stroker.getEndCap()] ||
                svgLineJoin != LineJoin.values()[stroker.getLineJoin()] ||
                svgMiterLimit != stroker.getMiterLimit() ||
                svgDashPhase != stroker.getDashPhase() ||
                !Arrays.equals(svgDash, stroker.getDashArray())) {

            svgStrokeWidth = stroker.getLineWidth();
            svgLineCap = LineCap.values()[stroker.getEndCap()];
            svgLineJoin = LineJoin.values()[stroker.getLineJoin()];
            svgMiterLimit = stroker.getMiterLimit();
            svgDashPhase = stroker.getDashPhase();
            svgDash = stroker.getDashArray();

            if (svgMode) {
                SVG.SetStroke(svgId, svgStrokeWidth,
                        svgLineCap.getInternalEnum(),
                        svgLineJoin.getInternalEnum(), svgMiterLimit, svgDash, svgDashPhase);
            }
        }
    }

    public void svgTransform(Affine transform) {
        checkDisposed();

        if (transform == null) {
            svgTransform.identity();
            if (svgMode) {
                svgApplyTransformGradients();
            }
        } else {
            svgTransform.set(transform);
            if (svgMode) {
                svgApplyTransformGradients();
            }
        }
    }

    public Affine svgTransform() {
        return new Affine(svgTransform);
    }

    // ---- Temp Vars
    private final float[] data = new float[6];

    public void svgClearClip(boolean clip) {
        checkDisposed();

        svgFlush();
        svgBegin();
        SVG.ClearClip(svgId, clip ? 1 : 0);
    }

    public void svgClip(Shape shape) {
        checkDisposed();

        if (shape.isEmpty()) return;

        PathIterator pi = shape.pathIterator(null);

        svgBegin();
        SVG.PathBegin(svgId, SVGEnums.SVG_CLIP, pi.windingRule());
        while (!pi.isDone()) {
            switch (pi.currentSegment(data)) {
                case PathIterator.SEG_MOVETO:
                    SVG.MoveTo(svgId, data[0], data[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    SVG.LineTo(svgId, data[0], data[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    SVG.QuadTo(svgId, data[0], data[1], data[2], data[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    SVG.CubicTo(svgId, data[0], data[1], data[2], data[3], data[4], data[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    SVG.Close(svgId);
                    break;
            }
            pi.next();
        }
        SVG.PathEnd(svgId);
    }

    public void svgDrawShape(Shape shape, boolean fill) {
        checkDisposed();

        if (shape.isEmpty()) return;

        PathIterator pi = shape.pathIterator(null);
        svgBegin();
        SVG.PathBegin(svgId, fill ? SVGEnums.SVG_FILL : SVGEnums.SVG_STROKE, pi.windingRule());
        while (!pi.isDone()) {
            switch (pi.currentSegment(data)) {
                case PathIterator.SEG_MOVETO:
                    SVG.MoveTo(svgId, data[0], data[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    SVG.LineTo(svgId, data[0], data[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    SVG.QuadTo(svgId, data[0], data[1], data[2], data[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    SVG.CubicTo(svgId, data[0], data[1], data[2], data[3], data[4], data[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    SVG.Close(svgId);
                    break;
            }
            pi.next();
        }
        SVG.PathEnd(svgId);
    }

    public void svgDrawRect(float x, float y, float width, float height, boolean fill) {
        checkDisposed();

        if (width <= 0 || height <= 0) return;

        svgBegin();
        if (fill) {
            SVG.Rect(svgId, x, y, width, height);
        } else {
            SVG.PathBegin(svgId, SVGEnums.SVG_STROKE, 0);
            SVG.MoveTo(svgId, x, y);
            SVG.LineTo(svgId, x, y + height);
            SVG.LineTo(svgId, x + width, y + height);
            SVG.LineTo(svgId, x + width, y);
            SVG.Close(svgId);
            SVG.PathEnd(svgId);
        }
    }

    private static final float _el90 = 0.5522847493f;

    public void svgDrawEllipse(float x, float y, float width, float height, boolean fill) {
        checkDisposed();

        if (width <= 0 || height <= 0) return;

        svgBegin();
        if (fill) {
            SVG.Ellipse(svgId, x, y, width, height);
        } else {
            float cx = x + width / 2f;
            float cy = y + height / 2f;
            float rx = width / 2f;
            float ry = height / 2f;
            SVG.PathBegin(svgId, SVGEnums.SVG_STROKE, 0);
            SVG.MoveTo(svgId, cx - rx, cy);
            SVG.CubicTo(svgId, cx - rx, cy + ry * _el90, cx - rx * _el90, cy + ry, cx, cy + ry);
            SVG.CubicTo(svgId, cx + rx * _el90, cy + ry, cx + rx, cy + ry * _el90, cx + rx, cy);
            SVG.CubicTo(svgId, cx + rx, cy - ry * _el90, cx + rx * _el90, cy - ry, cx, cy - ry);
            SVG.CubicTo(svgId, cx - rx * _el90, cy - ry, cx - rx, cy - ry * _el90, cx - rx, cy);
            SVG.Close(svgId);
            SVG.PathEnd(svgId);
        }
    }

    public void svgDrawRoundRect(float x, float y, float width, float height, float cTop, float cRight, float cBottom, float cLeft, boolean fill) {
        checkDisposed();
        if (width <= 0 || height <= 0) return;

        float max = Math.min(width, height) / 2f;
        cTop = Math.max(0, Math.min(max, cTop));
        cRight = Math.max(0, Math.min(max, cRight));
        cBottom = Math.max(0, Math.min(max, cBottom));
        cLeft = Math.max(0, Math.min(max, cLeft));

        if (Mathf.epsilonEquals(cTop, cRight) && Mathf.epsilonEquals(cRight, cBottom)
                && Mathf.epsilonEquals(cBottom, cLeft) && Mathf.epsilonEquals(cLeft, 0)) {
            svgDrawRect(x, y, width, height, fill);
            return;
        }

        svgBegin();
        if (fill) {
            SVG.RoundRect(svgId, x, y, width, height, cTop, cRight, cBottom, cLeft);
        } else {
            float halfw = Math.abs(width) * 0.5f;
            float halfh = Math.abs(height) * 0.5f;
            float rxBL = Math.min(cLeft, halfw) * Math.signum(width), ryBL = Math.min(cLeft, halfh) * Math.signum(height);
            float rxBR = Math.min(cBottom, halfw) * Math.signum(width), ryBR = Math.min(cBottom, halfh) * Math.signum(height);
            float rxTR = Math.min(cRight, halfw) * Math.signum(width), ryTR = Math.min(cRight, halfh) * Math.signum(height);
            float rxTL = Math.min(cTop, halfw) * Math.signum(width), ryTL = Math.min(cTop, halfh) * Math.signum(height);
            SVG.PathBegin(svgId, SVGEnums.SVG_STROKE, 0);
            SVG.MoveTo(svgId, x, y + ryTL);
            SVG.CubicTo(svgId, x, y + ryTL * (1 - _el90), x + rxTL * (1 - _el90), y, x + rxTL, y);
            SVG.LineTo(svgId, x + width - rxTR, y);
            SVG.CubicTo(svgId, x + width - rxTR * (1 - _el90), y, x + width, y + ryTR * (1 - _el90), x + width, y + ryTR);
            SVG.LineTo(svgId, x + width, y + height - ryBR);
            SVG.CubicTo(svgId, x + width, y + height - ryBR * (1 - _el90), x + width - rxBR * (1 - _el90), y + height, x + width - rxBR, y + height);
            SVG.LineTo(svgId, x + rxBL, y + height);
            SVG.CubicTo(svgId, x + rxBL * (1 - _el90), y + height, x, y + height - ryBL * (1 - _el90), x, y + height - ryBL);
            SVG.Close(svgId);
            SVG.PathEnd(svgId);
        }
    }

    public void svgDrawLine(float x1, float y1, float x2, float y2) {
        checkDisposed();

        svgBegin();
        SVG.PathBegin(svgId, SVGEnums.SVG_STROKE, 0);
        SVG.MoveTo(svgId, x1, y1);
        SVG.LineTo(svgId, x2, y2);
        SVG.PathEnd(svgId);
    }

    public void svgDrawQuadCurve(float x1, float y1, float cx, float cy, float x2, float y2) {
        checkDisposed();

        svgBegin();
        SVG.PathBegin(svgId, SVGEnums.SVG_STROKE, 0);
        SVG.MoveTo(svgId, x1, y1);
        SVG.QuadTo(svgId, cx, cy, x2, y2);
        SVG.PathEnd(svgId);
    }

    public void svgDrawCubicCurve(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2) {
        checkDisposed();

        svgBegin();
        SVG.PathBegin(svgId, SVGEnums.SVG_STROKE, 0);
        SVG.MoveTo(svgId, x1, y1);
        SVG.CubicTo(svgId, cx1, cy1, cx2, cy2, x2, y2);
        SVG.PathEnd(svgId);
    }

    // ---- TEXT ----

    public void svgTextFont(Font font) {
        checkDisposed();

        if (font == null) {
            font = Font.getDefault();
        }

        if (svgTextFont != font) {
            svgTextFont = font;
            if (svgMode) {
                if (svgTextFont.isDisposed()) {
                    svgTextFont = Font.getDefault();
                }
                SVG.SetFont(svgId, svgTextFont.getInternalID(this));
            }
        }
    }

    public Font svgTextFont() {
        return svgTextFont;
    }

    public void svgTextScale(float scale) {
        checkDisposed();

        if (svgTextScale != scale) {
            svgTextScale = scale;
            if (svgMode) {
                SVG.SetFontScale(svgId, scale);
            }
        }
    }

    public float svgTextScale() {
        return svgTextScale;
    }

    public void svgTextSpacing(float spacing) {
        checkDisposed();

        if (svgTextSpacing != spacing) {
            svgTextSpacing = spacing;
            if (svgMode) {
                SVG.SetFontSpacing(svgId, spacing);
            }
        }
    }

    public float svgTextSpacing() {
        return svgTextSpacing;
    }

    public void svgTextVerticalAlign(Align.Vertical align) {
        checkDisposed();

        if (svgTextVerticalAlign != align) {
            svgTextVerticalAlign = align;
        }
    }

    public float svgTextBlur() {
        return svgTextBlur;
    }

    public void svgTextBlur(float blur) {
        checkDisposed();

        if (svgTextBlur != blur) {
            svgTextBlur = blur;
            if (svgMode) {
                SVG.SetFontBlur(svgId, blur);
            }
        }
    }

    public Align.Vertical svgTextVerticalAlign() {
        return svgTextVerticalAlign;
    }

    public void svgTextHorizontalAlign(Align.Horizontal align) {
        checkDisposed();

        if (svgTextHorizontalAlign != align) {
            svgTextHorizontalAlign = align;
        }
    }

    public Align.Horizontal svgTextHorizontalAlign() {
        return svgTextHorizontalAlign;
    }

    public int svgDrawText(float x, float y, String text, float maxWidth) {
        checkDisposed();

        int w = 0;
        if (text != null) {
            svgTextFont.checkInternalLoadState(text);
            svgBegin();
            // SVG.PathBegin(svgId, SVG_TEXT, 0);
            w = SVG.DrawText(svgId, x, y, text, maxWidth, svgTextHorizontalAlign.getInternalEnum(), svgTextVerticalAlign.getInternalEnum());
            // SVG.PathEnd(svgId);
        }
        return w;
    }

    public int svgDrawText(float x, float y, Buffer text, int offset, int length, float maxWidth) {
        checkDisposed();

        int w = 0;
        if (text != null && offset >= 0 && offset + length <= text.limit()) {
            svgTextFont.loadAllGlyphs();
            svgBegin();
            // SVG.PathBegin(svgId, SVG_TEXT, 0);
            w = SVG.DrawTextBuffer(svgId, x, y, text, offset, length, maxWidth, svgTextHorizontalAlign.getInternalEnum(), svgTextVerticalAlign.getInternalEnum());
            // SVG.PathEnd(svgId);
        }
        return w;
    }

    public int getError() {
        return GL.GetError();
    }
}