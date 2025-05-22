package flat.graphics.context;

import flat.backend.*;
import flat.exception.FlatException;
import flat.graphics.Graphics;
import flat.graphics.context.enums.*;
import flat.graphics.context.paints.ColorPaint;
import flat.graphics.symbols.Font;
import flat.math.Affine;
import flat.math.shapes.Path;
import flat.math.shapes.PathIterator;
import flat.math.shapes.Shape;
import flat.math.shapes.Stroke;
import flat.window.Activity;
import flat.window.Window;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;

public class Context {

    private static int maxTextureSize;
    private static int maxElementsVertices;
    private static int maxElementsIndices;
    private static int maxUniformBlockSize;

    public final long svgId;

    private Window window;
    private Graphics graphics;
    private boolean disposed;

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
    private ReadBuffer readBuffer;

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

    private boolean invalidFrameBufferSwap;

    // ---- Objects ---- //
    private FrameBuffer frameBuffer;
    private ShaderProgram shaderProgram;
    private VertexArray vertexArray;
    private Render render;
    private int activeTexture;
    private BufferObject[] buffers = new BufferObject[8];
    private Texture[] textures = new Texture[32];

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
    private AlphaComposite svgAlphaComposite;

    private Font svgTextFont;
    private float svgTextScale, svgTextSpacing, svgTextBlur;

    private Affine svgTransform;
    private ArrayList<Runnable> disposeTasks = new ArrayList<>();

    public static Context create(Window window, long svgId) {
        if (window.getContext() != null) {
            throw new FlatException("The Window already have a context");
        } else {
            return new Context(window, svgId);
        }
    }

    private Context(Window window, long svgId) {
        this.window = window;
        this.svgId = svgId;
        init();
    }

    public Activity getActivity() {
        return window.getActivity();
    }

    public Window getWindow() {
        return window;
    }

    void checkDisposed() {
        if (disposed) {
            throw new FlatException("The Context is disposed");
        }
        if (!window.isAssigned()) {
            throw new FlatException("The Context is not currently assigned");
        }
    }

    public Graphics getGraphics() {
        if (graphics == null) {
            this.graphics = new Graphics(this);
        }
        return graphics;
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
        readBuffer = ReadBuffer.FRONT;

        depthEnabled = false;
        depthMask = true;
        depthNear = 0;
        depthFar = 1;
        depthFunction = MathFunction.LESS;

        stencilEnabled = false;
        stencilBackMask = 0xFF;
        stencilFrontMask = 0xFF;
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
        svgPaint = new ColorPaint(0x000000FF);
        svgStrokeWidth = 1f;
        svgLineCap = LineCap.BUTT;
        svgLineJoin = LineJoin.ROUND;
        svgMiterLimit = 10.0f;
        svgDashPhase = 0;
        svgDash = null;
        svgAlphaComposite = AlphaComposite.SRC_OVER;

        svgTextFont = Font.getDefault();
        svgTextScale = 1.0f;
        svgTextSpacing = 1.0f;
        svgTextBlur = 0f;

        svgTransform = new Affine();

        // ---- Limits ---- //
        maxTextureSize = GL.GetMaxTextureSize();
        maxElementsVertices = GL.GetMaxElementsVertices();
        maxElementsIndices = GL.GetMaxElementsIndices();
        maxUniformBlockSize = GL.GetMaxUniformBlockSize();
    }

    static int getMaxTextureSize() {
        return maxTextureSize;
    }

    static int getMaxElementsIndices() {
        return maxElementsIndices;
    }

    static int getMaxElementsVertices() {
        return maxElementsVertices;
    }

    static int getMaxUniformBlockSize() {
        return maxUniformBlockSize;
    }

    public void dispose() {
        checkDisposed();

        hardFlush();
        bindVertexArray(null);
        bindTexture(null);
        bindRender(null);
        setFrameBuffer(null);
        for (int i = 0; i < buffers.length; i++) {
            bindBuffer(null, BufferType.values()[i]);
        }

        for (Runnable disposeTask : disposeTasks) {
            disposeTask.run();
        }
        disposeTasks.clear();

        graphics = null;
        frameBuffer = null;
        shaderProgram = null;
        vertexArray = null;
        render = null;
        buffers = null;
        textures = null;

        disposed = true;
    }

    public boolean isDisposed() {
        return disposed;
    }

    public void endFrame() {
        softFlush();

        bindVertexArray(null);
        bindTexture(null);
        bindRender(null);
        setFrameBuffer(null);
        for (int i = 0; i < buffers.length; i++) {
            bindBuffer(null, BufferType.values()[i]);
        }
        invalidFrameBufferSwap = false;
    }

    public boolean isInvalidFrameBufferSwap() {
        return invalidFrameBufferSwap;
    }

    public void softFlush() {
        checkDisposed();

        svgEnd();
    }

    public void hardFlush() {
        checkDisposed();

        svgEnd();
        GL.Flush();
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
            GL.SetClearStencil(clearStencil = stencil);
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

    public int getPixelPackAlignment() {
        return pixelPackAlignment;
    }

    public void setPixelPackAlignment(int alignment) {
        checkDisposed();
        
        if (pixelPackAlignment != alignment) {
            svgEnd();
            GL.SetPixelStore(GLEnums.PS_PACK_ALIGNMENT, pixelPackAlignment = alignment);
        }
    }

    public int getPixelPackRowLength() {
        return pixelPackRowLength;
    }

    public void setPixelPackRowLength(int rowLength) {
        checkDisposed();
        
        if (pixelPackRowLength != rowLength) {
            svgEnd();
            GL.SetPixelStore(GLEnums.PS_PACK_ROW_LENGTH, pixelPackRowLength = rowLength);
        }
    }

    public int getPixelPackSkipPixels() {
        return pixelPackSkipPixels;
    }

    public void setPixelPackSkipPixels(int skipPixels) {
        checkDisposed();
        
        if (pixelPackSkipPixels != skipPixels) {
            svgEnd();
            GL.SetPixelStore(GLEnums.PS_PACK_SKIP_PIXELS, pixelPackSkipPixels = skipPixels);
        }
    }

    public int getPixelPackSkipRows() {
        return pixelPackSkipRows;
    }

    public void setPixelPackSkipRows(int skipRows) {
        checkDisposed();
        
        if (pixelPackSkipRows != skipRows) {
            svgEnd();
            GL.SetPixelStore(GLEnums.PS_PACK_SKIP_ROWS, pixelPackSkipRows = skipRows);
        }
    }

    public int getPixelPackImageHeight() {
        return pixelPackImageHeight;
    }

    public void setPixelPackImageHeight(int imageHeight) {
        checkDisposed();
        
        if (pixelPackImageHeight != imageHeight) {
            svgEnd();
            GL.SetPixelStore(GLEnums.PS_PACK_IMAGE_HEIGHT, pixelPackImageHeight = imageHeight);
        }
    }

    public int getPixelPackSkipImages() {
        return pixelPackSkipImages;
    }

    public void setPixelPackSkipImages(int skipImages) {
        checkDisposed();
        
        if (pixelPackSkipImages != skipImages) {
            svgEnd();
            GL.SetPixelStore(GLEnums.PS_PACK_SKIP_IMAGES, pixelPackSkipImages = skipImages);
        }
    }

    public int getPixelUnpackAlignment() {
        return pixelUnpackAlignment;
    }

    public void setPixelUnpackAlignment(int alignment) {
        checkDisposed();

        if (pixelUnpackAlignment != alignment) {
            svgEnd();
            GL.SetPixelStore(GLEnums.PS_UNPACK_ALIGNMENT, pixelUnpackAlignment = alignment);
        }
    }

    public int getPixelUnpackRowLength() {
        return pixelUnpackRowLength;
    }

    public void setPixelUnpackRowLength(int rowLength) {
        checkDisposed();

        if (pixelUnpackRowLength != rowLength) {
            svgEnd();
            GL.SetPixelStore(GLEnums.PS_UNPACK_ROW_LENGTH, pixelUnpackRowLength = rowLength);
        }
    }

    public int getPixelUnpackSkipPixels() {
        return pixelUnpackSkipPixels;
    }

    public void setPixelUnpackSkipPixels(int skipPixels) {
        checkDisposed();

        if (pixelUnpackSkipPixels != skipPixels) {
            svgEnd();
            GL.SetPixelStore(GLEnums.PS_UNPACK_SKIP_PIXELS, pixelUnpackSkipPixels = skipPixels);
        }
    }

    public int getPixelUnpackSkipRows() {
        return pixelUnpackSkipRows;
    }

    public void setPixelUnpackSkipRows(int skipRows) {
        checkDisposed();

        if (pixelUnpackSkipRows != skipRows) {
            svgEnd();
            GL.SetPixelStore(GLEnums.PS_UNPACK_SKIP_ROWS, pixelUnpackSkipRows = skipRows);
        }
    }

    public int getPixelUnpackImageHeight() {
        return pixelUnpackImageHeight;
    }

    public void setPixelUnpackImageHeight(int imageHeight) {
        checkDisposed();

        if (pixelUnpackImageHeight != imageHeight) {
            svgEnd();
            GL.SetPixelStore(GLEnums.PS_UNPACK_IMAGE_HEIGHT, pixelUnpackImageHeight = imageHeight);
        }
    }

    public int getPixelUnpackSkipImages() {
        return pixelUnpackSkipImages;
    }

    public void setPixelUnpackSkipImages(int skipImages) {
        checkDisposed();

        if (pixelUnpackSkipImages != skipImages) {
            svgEnd();
            GL.SetPixelStore(GLEnums.PS_UNPACK_SKIP_IMAGES, pixelUnpackSkipImages = skipImages);
        }
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

    public void setReadBuffer(ReadBuffer readBuffer) {
        softFlush();
        if (this.readBuffer != readBuffer) {
            this.readBuffer = readBuffer;
            GL.SetReadBuffer(readBuffer.getInternalEnum());
        }
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

    void drawArray(VertexMode vertexMode, int first, int count, int instances) {
        softFlush();
        GL.DrawArrays(vertexMode.getInternalEnum(), first, count, instances);
    }

    void drawElements(VertexMode vertexMode, int first, int count, int instances) {
        softFlush();
        GL.DrawElements(vertexMode.getInternalEnum(), count, instances, GLEnums.DT_INT, first);
    }

    public void setFrameBuffer(FrameBuffer frameBuffer) {
        checkDisposed();
        if (frameBuffer != null && frameBuffer.isDisposed()) {
            throw new FlatException("The FrameBuffer is disposed");
        }

        if (this.frameBuffer != frameBuffer) {
            svgEnd();

            if (frameBuffer == null) {
                GL.FrameBufferBind(GLEnums.FB_FRAMEBUFFER, 0);
            } else {
                GL.FrameBufferBind(GLEnums.FB_FRAMEBUFFER, frameBuffer.getInternalId());
            }
            this.frameBuffer = frameBuffer;
            if (this.frameBuffer != null) {
                this.frameBuffer.onBound();
            }

            invalidFrameBufferSwap = true;
        }
    }

    public boolean isCurrentFrameBufferValid() {
        return GL.FrameBufferGetStatus(GLEnums.FB_FRAMEBUFFER) == GLEnums.FS_FRAMEBUFFER_COMPLETE;
    }

    public FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    public void blitFrames(FrameBuffer source, FrameBuffer target,
                           int srcX, int srcY, int srcW, int srcH,
                           int dstX, int dstY, int dstW, int dstH, BlitMask blitMask, MagFilter filter) {
        checkDisposed();

        if (target == source) {
            throw new FlatException("The FrameBuffers must be different");
        }
        if (source != null && source.isDisposed()) {
            throw new FlatException("The FrameBuffer is disposed");
        }
        if (target != null && target.isDisposed()) {
            throw new FlatException("The FrameBuffer is disposed");
        }
        FrameBuffer current = getFrameBuffer();
        if (source != null && source.isInvalid()) {
            setFrameBuffer(source);
        }
        if (target != null && target.isInvalid()) {
            setFrameBuffer(target);
        }

        try {
            GL.FrameBufferBind(GLEnums.FB_DRAW_FRAMEBUFFER, target == null ? 0 : target.getInternalId());
            GL.FrameBufferBind(GLEnums.FB_READ_FRAMEBUFFER, source == null ? 0 : source.getInternalId());
            GL.FrameBufferBlit(srcX, srcY, srcW, srcH, dstX, dstY, dstW, dstH, blitMask.getInternalEnum(), filter.getInternalEnum());
        } finally {
            if (current != getFrameBuffer()) {
                setFrameBuffer(current);
            } else {
                GL.FrameBufferBind(GLEnums.FB_FRAMEBUFFER, frameBuffer == null ? 0 : frameBuffer.getInternalId());
            }
            invalidFrameBufferSwap = true;
        }
    }

    void setActiveTexture(int index) {
        checkDisposed();

        if (activeTexture != index) {
            svgEnd();
            GL.SetActiveTexture(activeTexture = index);
        }
    }

    public void setShaderTextures(Texture... textures) {
        checkDisposed();

        for (int i = 0; i < this.textures.length; i++) {
            if (this.textures[i] != null) {
                svgEnd();

                setActiveTexture(i);
                GL.TextureBind(this.textures[i].getInternalType(), 0);
                this.textures[i] = null;
            }
        }
        for (int i = 0; i < textures.length; i++) {
            svgEnd();

            this.textures[i] = textures[i];
            setActiveTexture(i);
            GL.TextureBind(textures[i].getInternalType(), textures[i].getInternalId());
        }
    }

    void bindTexture(Texture texture) {
        checkDisposed();

        for (int i = 1; i < textures.length; i++) {
            if (textures[i] != null) {
                svgEnd();

                setActiveTexture(i);
                GL.TextureBind(textures[i].getInternalType(), 0);
                textures[i] = null;
            }
        }

        setActiveTexture(0);
        if (textures[0] != texture) {
            svgEnd();
            if (texture == null) {
                GL.TextureBind(textures[0].getInternalType(), 0);
            } else {
                GL.TextureBind(texture.getInternalType(), texture.getInternalId());
            }
            textures[0] = texture;
        }
    }

    void bindRender(Render render) {
        checkDisposed();

        if (this.render != render) {
            svgEnd();

            if (render == null) {
                GL.RenderBufferBind(0);
            } else {
                GL.RenderBufferBind(render.getInternalId());
            }
            this.render = render;
        }
    }

    void bindBuffer(BufferObject buffer, BufferType type) {
        checkDisposed();
        int index = type.ordinal();

        if (buffers[index] != buffer) {
            svgEnd();

            if (buffer == null) {
                GL.BufferBind(type.getInternalEnum(), 0);
            } else {
                GL.BufferBind(type.getInternalEnum(), buffer.getInternalId());
            }
            buffers[index] = buffer;
        }
    }

    void bindVertexArray(VertexArray array) {
        checkDisposed();

        if (vertexArray != array) {
            svgEnd();

            if (array == null) {
                GL.VertexArrayBind(0);
                buffers[BufferType.Element.ordinal()] = null;
                GL.BufferBind(BufferType.Element.getInternalEnum(), 0);
            } else {
                GL.VertexArrayBind(array.getInternalId());
            }
            vertexArray = array;
        }
    }

    public void setShaderProgram(ShaderProgram shaderProgram) {
        checkDisposed();
        if (shaderProgram != null && shaderProgram.isDisposed()) {
            throw new FlatException("The ShaderProgram is disposed");
        }

        if (this.shaderProgram != shaderProgram) {
            svgEnd();

            if (shaderProgram == null) {
                GL.ProgramUse(0);
            } else {
                GL.ProgramUse(shaderProgram.getInternalId());
            }
            this.shaderProgram = shaderProgram;
            if (this.shaderProgram != null) {
                this.shaderProgram.onBound();
            }
        }
    }

    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

    // ---- SVG ---- //
    private void svgBegin() {
        checkDisposed();

        if (!svgMode) {
            svgMode = true;

            buffers[BufferType.Array.ordinal()] = null;
            buffers[BufferType.Uniform.ordinal()] = null;
            vertexArray = null;
            shaderProgram = null;
            textures[0] = null;
            textures[1] = null;
            activeTexture = 0;

            SVG.BeginFrame(svgId, viewWidth, viewHeight);

            svgApplyTransformGradients();

            SVG.SetAntiAlias(svgId, svgAntialias);
            SVG.SetBlendMode(svgId, svgAlphaComposite.ordinal());

            SVG.SetStroke(svgId, svgStrokeWidth,
                    svgLineCap.getInternalEnum(),
                    svgLineJoin.getInternalEnum(), svgMiterLimit, svgDash, svgDashPhase);

            if (svgTextFont.isDisposed()) {
                svgTextFont = Font.getDefault();
            }
            SVG.SetFont(svgId, svgTextFont.getInternalId());
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
        GL.SetClearStencil(clearStencil);
        GL.EnableDepthTest(depthEnabled);
        GL.EnableStencilTest(stencilEnabled);
        GL.SetStencilMask(GLEnums.FC_FRONT, stencilFrontMask);
        GL.SetStencilMask(GLEnums.FC_BACK, stencilBackMask);

        GL.SetStencilFunction(GLEnums.FC_FRONT, stencilFrontFunction.getInternalEnum(), stencilFrontFunRef, stencilFrontFunMask);
        GL.SetStencilFunction(GLEnums.FC_BACK, stencilBackFunction.getInternalEnum(), stencilBackFunRef, stencilBackFunMask);

        GL.SetStencilOperation(GLEnums.FC_FRONT,
                stencilFrontSFail.getInternalEnum(), stencilFrontDFail.getInternalEnum(), stencilFrontDPass.getInternalEnum());
        GL.SetStencilOperation(GLEnums.FC_BACK,
                stencilBackSFail.getInternalEnum(), stencilBackDFail.getInternalEnum(), stencilBackDPass.getInternalEnum());

        GL.EnableBlend(blendEnabled);
        GL.SetBlendFunction(
                blendSrcColorFun.getInternalEnum(), blendDstColorFun.getInternalEnum(),
                blendSrcAlphaFun.getInternalEnum(), blendDstAlphaFun.getInternalEnum());

        GL.EnableCullface(cullFaceEnabled);

        GL.SetColorMask(rMask, gMask, bMask, aMask);

        GL.SetPixelStore(GLEnums.PS_UNPACK_ALIGNMENT, pixelUnpackAlignment);
        GL.SetPixelStore(GLEnums.PS_UNPACK_ROW_LENGTH, pixelUnpackRowLength);
        GL.SetPixelStore(GLEnums.PS_UNPACK_SKIP_PIXELS, pixelUnpackSkipPixels);
        GL.SetPixelStore(GLEnums.PS_UNPACK_SKIP_ROWS, pixelUnpackSkipRows);

        GL.SetActiveTexture(activeTexture);
    }

    private void svgApplyTransformGradients() {
        svgPaint.setInternal(svgId);

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

    public void svgAlphaComposite(AlphaComposite alphaComposite) {
        checkDisposed();

        if (svgAlphaComposite != alphaComposite) {
            svgAlphaComposite = alphaComposite;
            if (svgMode) {
                SVG.SetBlendMode(svgId, alphaComposite.ordinal());
            }
        }
    }

    public AlphaComposite svgAlphaComposite() {
        return svgAlphaComposite;
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

    public void svgStroke(Stroke stroke) {
        checkDisposed();

        if (svgStrokeWidth != stroke.getLineWidth() ||
                svgLineCap != LineCap.values()[stroke.getEndCap()] ||
                svgLineJoin != LineJoin.values()[stroke.getLineJoin()] ||
                svgMiterLimit != stroke.getMiterLimit() ||
                svgDashPhase != stroke.getDashPhase() ||
                !Arrays.equals(svgDash, stroke.getDashArray())) {

            svgStrokeWidth = stroke.getLineWidth();
            svgLineCap = LineCap.values()[stroke.getEndCap()];
            svgLineJoin = LineJoin.values()[stroke.getLineJoin()];
            svgMiterLimit = stroke.getMiterLimit();
            svgDashPhase = stroke.getDashPhase();
            svgDash = stroke.getDashArray();

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

    public void svgUnclip(Shape shape) {
        checkDisposed();

        PathIterator pi = shape.pathIterator(null);

        svgBegin();
        SVG.PathBegin(svgId, SVGEnums.SVG_UNCLIP, pi.windingRule());
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

    public void svgDrawShapeOptimized(Path p) {
        PathIterator pi = p.pathIterator(null);
        svgBegin();
        SVG.PathBegin(svgId, SVGEnums.SVG_FILL, pi.windingRule());
        while (!pi.isDone()) {
            switch (pi.currentSegment(data)) {
                case PathIterator.SEG_MOVETO:
                    SVG.MoveTo(svgId, data[0], data[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    SVG.LineTo(svgId, data[0], data[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    SVG.LineTo(svgId, data[0], data[1]);
                    SVG.LineTo(svgId, data[2], data[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    SVG.LineTo(svgId, data[0], data[1]);
                    SVG.LineTo(svgId, data[2], data[3]);
                    SVG.LineTo(svgId, data[4], data[5]);
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
        SVG.Rect(svgId, x, y, width, height, fill);
    }

    public void svgDrawEllipse(float x, float y, float width, float height, boolean fill) {
        checkDisposed();

        if (width <= 0 || height <= 0) return;

        svgBegin();
        SVG.Ellipse(svgId, x, y, width, height, fill);
    }

    public void svgDrawRoundRect(float x, float y, float width, float height, float cTop, float cRight, float cBottom, float cLeft, boolean fill) {
        checkDisposed();
        if (width <= 0 || height <= 0) return;

        svgBegin();
        SVG.RoundRect(svgId, x, y, width, height, cTop, cRight, cBottom, cLeft, fill);
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
                SVG.SetFont(svgId, svgTextFont.getInternalId());
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

    public int svgDrawText(float x, float y, String text, float maxWidth, float maxHeight) {
        checkDisposed();

        int w = 0;
        if (text != null && svgTextScale > 0) {
            svgBegin();
            if (svgTextFont.isDisposed()) {
                svgTextFont = Font.getDefault();
                SVG.SetFont(svgId, svgTextFont.getInternalId());
            }
            w = SVG.DrawText(svgId, x, y, text, maxWidth, maxHeight);
        }
        return w;
    }

    public int svgDrawText(float x, float y, Buffer text, int offset, int length, float maxWidth, float maxHeight) {
        checkDisposed();

        int w = 0;
        if (text != null && offset >= 0 && offset + length <= text.limit() && svgTextScale > 0) {
            svgBegin();
            if (svgTextFont.isDisposed()) {
                svgTextFont = Font.getDefault();
                SVG.SetFont(svgId, svgTextFont.getInternalId());
            }
            w = SVG.DrawTextBuffer(svgId, x, y, text, offset, length, maxWidth, maxHeight);
        }
        return w;
    }

    public int getError() {
        return GL.GetError();
    }
}