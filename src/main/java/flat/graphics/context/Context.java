package flat.graphics.context;

import flat.backend.*;
import flat.graphics.SmartContext;
import flat.graphics.text.Align;
import flat.math.Affine;
import flat.math.Mathf;
import flat.math.shapes.PathIterator;
import flat.math.shapes.Shape;
import flat.math.shapes.Stroke;
import flat.graphics.context.enuns.*;

import java.lang.ref.WeakReference;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.HashMap;

public final class Context {

    public final long id;
    public final long svgId;

    private SmartContext smartContext;
    private Thread thread;

    // ---- Core ---- //
    private int clearColor, clearStencil;
    private double clearDepth;
    private int viewX, viewY, viewWidth, viewHeight;

    private boolean scizorEnabled;
    private int scissorX, scissorY, scissorWidth, scissorHeight;

    private boolean rasterizeEnabled;
    private boolean rMask, gMask, bMask, aMask;
    private int pixelPackAligment, pixelPackRowLength, pixelPackSkipPixels, pixelPackSkipRows, pixelPackImageHeight, pixelPackSkipImages;
    private int pixelUnpackAligment, pixelUnpackRowLength, pixelUnpackSkipPixels, pixelUnpackSkipRows, pixelUnpackImageHeight, pixelUnpackSkipImages;

    private boolean depthEnabled;
    private boolean depthMask;
    private double depthNear, depthFar;
    private MathFunction depthFuntion;

    private boolean stencilEnabled;
    private int stencilBackMask, stencilFrontMask;
    private MathFunction stencilBackFunction, stencilFrontFunction;
    private int stencilBackFunRef, stencilBackFunMask, stencilFrontFunRef, stencilFrontFunMask;
    private MathOperation stencilBackSFail, stencilBackDFail, stencilBackDPass, stencilFrontSFail, stencilFrontDFail, stencilFrontDPass;

    private boolean blendEnabled;
    private BlendFunction blendSrcColorFun, blendSrcAlphaFun, blendDstColorFun, blendDstAlphaFun;
    private BlendEquation blendColorEquation, blendAlphaEquation;
    private int blendColor;

    private boolean cullfaceEnabled, cullFronFace, cullBackFace;
    private boolean clockWiseFrontFace;

    private boolean multsampleEnabled;
    private float lineWidth;

    // ---- Objects ---- //
    private static HashMap<Long, WeakReference<ContextObject>> objects = new HashMap<>();

    private Frame drawFrame, readFrame;
    private ShaderProgram shaderProgram;
    private VertexArray vertexArray;
    private Render render;
    private int activeTexture;
    private BufferObejct[] buffers = new BufferObejct[8];
    private Texture[] textures = new Texture[32];

    private boolean unbindShaderProgram, unbindVertexArray, unbindRender;
    private boolean[] unbindBuffer = new boolean[8];
    private boolean[] unbindTexture = new boolean[32];
    private BufferObejct preVertexElementbuffer;

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

    public Context(long id, long svgId) {
        this.id = id;
        this.svgId = svgId;
        this.thread = Thread.currentThread();
    }

    public SmartContext getSmartContext() {
        if (smartContext == null) {
            this.smartContext = new SmartContext(this);
        }
        return smartContext;
    }

    public static void assign(ContextObject object) {
        synchronized (Context.class) {
            objects.put(object.getUnicID(), new WeakReference<>(object));
        }
    }

    public static void deassign(ContextObject object) {
        synchronized (Context.class) {
            objects.remove(object.getUnicID());
        }
    }

    private boolean initialized;

    public void init() {
        if (initialized) return;
        initialized = true;

        // ---- Core ---- //
        clearColor = 0;
        clearDepth = 1;
        clearStencil = 0;

        viewX = 0;
        viewY = 0;
        viewWidth = GL.GetViewportWidth();
        viewHeight = GL.GetViewportHeight();

        scizorEnabled = false;
        scissorX = 0;
        scissorY = 0;
        scissorWidth = GL.GetScissorWidth();
        scissorHeight = GL.GetScissorHeight();

        rasterizeEnabled = true;
        rMask = true;
        gMask = true;
        bMask = true;
        aMask = true;
        pixelPackAligment = 4;
        pixelPackRowLength = 0;
        pixelPackSkipPixels = 0;
        pixelPackSkipRows = 0;
        pixelPackImageHeight = 0;
        pixelPackSkipImages = 0;
        pixelUnpackAligment = 4;
        pixelUnpackRowLength = 0;
        pixelUnpackSkipPixels = 0;
        pixelUnpackSkipRows = 0;
        pixelUnpackImageHeight = 0;
        pixelUnpackSkipImages = 0;

        depthEnabled = false;
        depthMask = true;
        depthNear = 0;
        depthFar = 1;
        depthFuntion = MathFunction.LESS;

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

        cullfaceEnabled = false;
        cullFronFace = false;
        cullBackFace = true;
        clockWiseFrontFace = false;

        multsampleEnabled = true;

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

        svgTextFont = Font.DEFAULT;
        svgTextScale = 1.0f;
        svgTextSpacing = 1.0f;
        svgTextBlur = 1f;
        svgTextVerticalAlign = Align.Vertical.TOP;
        svgTextHorizontalAlign = Align.Horizontal.LEFT;

        svgTransform = new Affine();
    }

    public void dispose() {
        smartContext = null;
        drawFrame = readFrame = null;
        shaderProgram = null;
        vertexArray = null;
        render = null;
        buffers = null;
        textures = null;
    }

    // ---- CORE ---- //

    public void softFlush() {
        svgEnd();
        refreshBinds();
    }

    public void hardFlush() {
        svgEnd();
        GL.Flush();
    }

    public void finish() {
        svgEnd();
        GL.Finish();
    }

    public void clear(boolean color, boolean depth, boolean stencil) {
        svgEnd();
        GL.Clear((color ? GLEnuns.CB_COLOR_BUFFER_BIT : 0) | (depth ? GLEnuns.CB_DEPTH_BUFFER_BIT : 0) | (stencil ? GLEnuns.CB_STENCIL_BUFFER_BIT : 0));
    }

    public void setClearColor(int color) {
        if (clearColor != color) {
            svgEnd();
            GL.SetClearColor(clearColor = color);
        }
    }

    public int getClearColor() {
        return clearColor;
    }

    public void setClearDepth(double depth) {
        if (clearDepth != depth) {
            svgEnd();
            GL.SetClearDepth(clearDepth = depth);
        }
    }

    public double getClearDepth() {
        return clearDepth;
    }

    public void setClearStencil(int stencil) {
        if (clearStencil != stencil) {
            svgEnd();
            GL.SetClearDepth(clearStencil = stencil);
        }
    }

    public int getClearStencil() {
        return clearStencil;
    }

    public void setViewPort(int x, int y, int width, int height) {
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
        return WL.GetClientWidth();
    }

    public int getHeight() {
        return WL.GetClientHeight();
    }

    public void setScizorEnabled(boolean enable) {
        if (scizorEnabled != enable) {
            svgEnd();
            GL.EnableScissorTest(scizorEnabled = enable);
        }
    }

    public void setRasterizeEnabled(boolean enable) {
        if (rasterizeEnabled != enable) {
            svgEnd();
            GL.EnableRasterizer(rasterizeEnabled = enable);
        }
    }

    public boolean isRasterizeEnabled() {
        return rasterizeEnabled;
    }

    public void setColorMask(boolean r, boolean g, boolean b, boolean a) {
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

    public void setPixelPack(int aligment, int rowLength, int skipPixels, int skipRows) {
        setPixelPack(aligment, rowLength, skipPixels, skipRows, pixelPackImageHeight, pixelPackSkipImages);
    }

    public void setPixelPack(int aligment, int rowLength, int skipPixels, int skipRows, int imageHeight, int skipImages) {
        if (pixelPackAligment != aligment || pixelPackRowLength != rowLength || pixelPackSkipPixels != skipPixels
                || pixelPackSkipRows != skipRows || pixelPackImageHeight != imageHeight || pixelPackSkipImages != skipImages) {
            svgEnd();
            if (pixelPackAligment != aligment)
                GL.SetPixelStore(GLEnuns.PS_PACK_ALIGNMENT, pixelPackAligment = aligment);
            if (pixelPackRowLength != rowLength)
                GL.SetPixelStore(GLEnuns.PS_PACK_ROW_LENGTH, pixelPackRowLength = rowLength);
            if (pixelPackSkipPixels != skipPixels)
                GL.SetPixelStore(GLEnuns.PS_PACK_SKIP_PIXELS, pixelPackSkipPixels = skipPixels);
            if (pixelPackSkipRows != skipRows)
                GL.SetPixelStore(GLEnuns.PS_PACK_SKIP_ROWS, pixelPackSkipRows = skipRows);
            if (pixelPackImageHeight != imageHeight)
                GL.SetPixelStore(GLEnuns.PS_PACK_IMAGE_HEIGHT, pixelPackImageHeight = imageHeight);
            if (pixelPackSkipImages != skipImages)
                GL.SetPixelStore(GLEnuns.PS_PACK_SKIP_IMAGES, pixelPackSkipImages = skipImages);
        }
    }

    public int getPixelPackAligment() {
        return pixelPackAligment;
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

    public void setPixelUnpack(int aligment, int rowLength, int skipPixels, int skipRows) {
        setPixelUnpack(aligment, rowLength, skipPixels, skipRows, pixelUnpackImageHeight, pixelUnpackSkipImages);
    }

    public void setPixelUnpack(int aligment, int rowLength, int skipPixels, int skipRows, int imageHeight, int skipImages) {
        if (pixelUnpackAligment != aligment || pixelUnpackRowLength != rowLength || pixelUnpackSkipPixels != skipPixels
                || pixelUnpackSkipRows != skipRows || pixelUnpackImageHeight != imageHeight || pixelUnpackSkipImages != skipImages) {
            svgEnd();
            if (pixelUnpackAligment != aligment)
                GL.SetPixelStore(GLEnuns.PS_UNPACK_ALIGNMENT, pixelUnpackAligment = aligment);
            if (pixelUnpackRowLength != rowLength)
                GL.SetPixelStore(GLEnuns.PS_UNPACK_ROW_LENGTH, pixelUnpackRowLength = rowLength);
            if (pixelUnpackSkipPixels != skipPixels)
                GL.SetPixelStore(GLEnuns.PS_UNPACK_SKIP_PIXELS, pixelUnpackSkipPixels = skipPixels);
            if (pixelUnpackSkipRows != skipRows)
                GL.SetPixelStore(GLEnuns.PS_UNPACK_SKIP_ROWS, pixelUnpackSkipRows = skipRows);
            if (pixelUnpackImageHeight != imageHeight)
                GL.SetPixelStore(GLEnuns.PS_UNPACK_IMAGE_HEIGHT, pixelUnpackImageHeight = imageHeight);
            if (pixelUnpackSkipImages != skipImages)
                GL.SetPixelStore(GLEnuns.PS_UNPACK_SKIP_IMAGES, pixelUnpackSkipImages = skipImages);
        }
    }

    public int getPixelUnpackAligment() {
        return pixelUnpackAligment;
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
        if (depthEnabled != enable) {
            svgEnd();
            GL.EnableDepthTest(depthEnabled = enable);
        }
    }

    public boolean isDepthEnabled() {
        return depthEnabled;
    }

    public void setDepthMask(boolean mask) {
        if (depthMask != mask) {
            svgEnd();
            GL.SetDepthMask(depthMask = mask);
        }
    }

    public boolean getDepthMask() {
        return depthMask;
    }

    public void setDepthFuntion(MathFunction funtion) {
        if (depthFuntion != funtion) {
            svgEnd();
            GL.SetDepthFunction((depthFuntion = funtion).getInternalEnum());
        }
    }

    public MathFunction getDepthFuntion() {
        return depthFuntion;
    }

    public void setDepthRange(double near, double far) {
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
        if (stencilEnabled != enable) {
            svgEnd();
            GL.EnableStencilTest(stencilEnabled = enable);
        }
    }

    public boolean isStencilEnabled() {
        return stencilEnabled;
    }

    public void setStencilMask(int frontMask, int backMask) {
        if (stencilFrontMask != frontMask) {
            svgEnd();
            GL.SetStencilMask(GLEnuns.FC_FRONT, stencilFrontMask = frontMask);
        }
        if (stencilBackMask != backMask) {
            svgEnd();
            GL.SetStencilMask(GLEnuns.FC_BACK, stencilBackMask = backMask);
        }
    }

    public int getStencilBackMask() {
        return stencilBackMask;
    }

    public int getStencilFrontMask() {
        return stencilFrontMask;
    }

    public void setStencilFrontFunction(MathFunction function, int ref, int mask) {
        if (stencilFrontFunction != function || stencilFrontFunRef != ref || stencilFrontFunMask != mask) {
            svgEnd();
            GL.SetStencilFunction(GLEnuns.FC_FRONT, (stencilFrontFunction = function).getInternalEnum(),
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
        if (stencilBackFunction != function || stencilBackFunRef != ref || stencilBackFunMask != mask) {
            svgEnd();
            GL.SetStencilFunction(GLEnuns.FC_BACK, (stencilBackFunction = function).getInternalEnum(),
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
        if (stencilFrontSFail != SFail || stencilFrontDFail != DFail || stencilFrontDPass != DPass) {
            svgEnd();
            GL.SetStencilOperation(GLEnuns.FC_FRONT, (stencilFrontSFail = SFail).getInternalEnum(),
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
        if (stencilBackSFail != SFail || stencilBackDFail != DFail || stencilBackDPass != DPass) {
            svgEnd();
            GL.SetStencilOperation(GLEnuns.FC_BACK, (stencilBackSFail = SFail).getInternalEnum(),
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
        if (blendEnabled != enable) {
            svgEnd();
            GL.EnableBlend(blendEnabled = enable);
        }
    }

    public boolean isBlendEnabled() {
        return blendEnabled;
    }

    public void setBlendFunction(BlendFunction srcColor, BlendFunction dstColor, BlendFunction srcAlpha, BlendFunction dstAlpha) {
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
        if (blendColor != color) {
            svgEnd();
            GL.SetBlendColor(blendColor = color);
        }
    }

    public int getBlendColor() {
        return blendColor;
    }

    public void setCullfaceEnabled(boolean enable) {
        if (cullfaceEnabled != enable) {
            svgEnd();
            GL.EnableCullface(cullfaceEnabled = enable);
        }
    }

    public boolean isCullfaceEnabled() {
        return cullfaceEnabled;
    }

    public void setCullFace(boolean frontFace, boolean backFace) {
        if (cullFronFace != frontFace || cullBackFace != backFace) {
            svgEnd();
            cullFronFace = frontFace;
            cullBackFace = backFace;
            GL.SetCullface(frontFace ? backFace ? GLEnuns.FC_FRONT_AND_BACK : GLEnuns.FC_FRONT : backFace ? GLEnuns.FC_BACK : 0);
        }
    }

    public boolean isCullFronFace() {
        return cullFronFace;
    }

    public boolean isCullBackFace() {
        return cullBackFace;
    }

    public void setClockWiseFrontFace(boolean clockWise) {
        if (clockWiseFrontFace != clockWise) {
            svgEnd();
            GL.SetFrontFace((clockWiseFrontFace = clockWise) ? GLEnuns.FF_CW : GLEnuns.FF_CCW);
        }
    }

    public boolean isClockWiseFrontFace() {
        return clockWiseFrontFace;
    }

    public void setMultsampleEnabled(boolean enable) {
        if (multsampleEnabled != enable) {
            svgEnd();
            GL.EnableMultisample(multsampleEnabled = enable);
        }
    }

    public boolean isMultsampleEnabled() {
        return multsampleEnabled;
    }

    public void setLineWidth(float width) {
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
        GL.ReadPixels(x, y, width, height, GLEnuns.DT_INT, offset);
    }

    public void readPixels(int x, int y, int width, int height, Buffer data, int offset) {
        softFlush();
        GL.ReadPixelsBuffer(x, y, width, height, GLEnuns.DT_INT, data, offset);
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
        GL.DrawElements(vertexMode.getInternalEnum(), count, instances, GLEnuns.DT_INT, first);
    }

    void bindFrame(Frame frame, boolean draw, boolean read) {
        int id = frame == null ? 0 : frame.getInternalID();
        if (draw && !read) {
            if (drawFrame != frame) {
                svgEnd();
                if (drawFrame != null) drawFrame.setDrawBindType(false);
                if (frame != null) frame.setDrawBindType(true);
                drawFrame = frame;
                GL.FrameBufferBind(GLEnuns.FB_DRAW_FRAMEBUFFER, id);
            }
        } else if (read && !draw) {
            if (readFrame != frame) {
                svgEnd();
                if (readFrame != null) drawFrame.setReadBindType(false);
                if (frame != null) frame.setReadBindType(true);
                readFrame = frame;
                GL.FrameBufferBind(GLEnuns.FB_READ_FRAMEBUFFER, id);
            }
        } else {
            if (drawFrame != frame || readFrame != frame) {
                svgEnd();
                drawFrame = readFrame = frame;
                if (drawFrame != null) drawFrame.setDrawBindType(false);
                if (readFrame != null) readFrame.setReadBindType(false);
                if (frame != null) frame.setBindType(true, true);
                GL.FrameBufferBind(GLEnuns.FB_FRAMEBUFFER, id);
            }
        }
    }

    void unbindFrame(boolean draw, boolean read) {
        bindFrame(null, draw, read);
    }

    public void clearBindFrame(boolean draw, boolean read) {
        bindFrame(null, draw, read);
    }

    public Frame getBoundDrawFrame() {
        return drawFrame;
    }

    public Frame getBoundReadFrame() {
        return readFrame;
    }

    public void setActiveTexture(int index) {
        if (activeTexture != index) {
            svgEnd();
            GL.SetActiveTexture(activeTexture = index);
        }
    }

    public int getActiveTexture() {
        return activeTexture;
    }

    void bindTexture(Texture texture, int index) {
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

    void bindBuffer(BufferObejct buffer, BufferType type) {
        int index = type.ordinal();
        unbindBuffer[index] = false;
        if (buffers[index] != buffer) {
            svgEnd();
            if (unbindVertexArray) {
                clearBindVertexArray();
            }
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

    public BufferObejct getBoundBuffer(BufferType type) {
        return buffers[type.ordinal()];
    }

    void bindVertexArray(VertexArray array) {
        unbindVertexArray = false;
        if (vertexArray != array) {
            svgEnd();

            // unbind element buffer is always false
            final int i = BufferType.Element.ordinal();

            if (array == null) {
                GL.VertexArrayBind(0);
                buffers[i] = preVertexElementbuffer;
                preVertexElementbuffer = null;
            } else {
                GL.VertexArrayBind(array.getInternalID());
                preVertexElementbuffer = buffers[i];
                buffers[i] = array.getElementBuffer();
            }
            vertexArray = array;
        }
    }

    void unbindVertexArray() {
        unbindVertexArray = true;
    }

    public void clearBindVertexArray() {
        bindVertexArray(null);
    }

    public VertexArray getBoundVertexArray() {
        return vertexArray;
    }

    void bindShaderProgram(ShaderProgram program) {
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

    public void clearBindShaderProgramm() {
        bindShaderProgram(null);
    }

    public ShaderProgram getBoundShaderProgram() {
        return shaderProgram;
    }

    void bindRender(Render render) {
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

    public void clearBindRender() {
        bindRender(null);
    }

    public Render getBoundRender() {
        return render;
    }

    public void refreshBinds() {
        if (unbindShaderProgram) {
            clearBindShaderProgramm();
        }
        if (unbindVertexArray) {
            clearBindVertexArray();
        }
        if (unbindRender) {
            clearBindRender();
        }
        refreshBufferBinds();
        refreshTextureBinds();
    }

    public void refreshBufferBinds() {
        BufferType[] values = BufferType.values();
        for (int i = 0; i < unbindBuffer.length; i++) {
            if (unbindBuffer[i]) {
                clearBindBuffer(values[i]);
            }
        }
    }

    public void refreshTextureBinds() {
        for (int i = 0; i < unbindTexture.length; i++) {
            if (unbindTexture[i]) {
                clearBindTexture(i);
            }
        }
    }

    // ---- SVG ---- //
    public void svgBegin() {
        if (!svgMode) {
            refreshBinds();

            svgMode = true;
            SVG.BeginFrame(svgId, viewWidth, viewHeight);

            svgApplyTransformGradientes();

            SVG.SetAntiAlias(svgId, svgAntialias);

            SVG.SetStroke(svgId, svgStrokeWidth,
                    svgLineCap.getInternalEnum(),
                    svgLineJoin.getInternalEnum(), svgMiterLimit, svgDash, svgDashPhase);

            SVG.SetFont(svgId, svgTextFont.getInternalID());
            SVG.SetFontScale(svgId, svgTextScale);
            SVG.SetFontSpacing(svgId, svgTextSpacing);
            SVG.SetFontBlur(svgId, svgTextBlur);
        }
    }

    public void svgEnd() {
        if (svgMode) {
            svgMode = false;
            SVG.EndFrame(svgId);
            svgRestore();
        }
    }

    public void svgFlush() {
        if (svgMode) {
            SVG.Flush(svgId);
        }
    }

    protected void svgRestore() {
        GL.EnableDepthTest(depthEnabled);
        GL.EnableScissorTest(scizorEnabled);

        GL.EnableStencilTest(stencilEnabled);
        GL.SetStencilMask(GLEnuns.FC_FRONT, stencilFrontMask);
        GL.SetStencilMask(GLEnuns.FC_BACK, stencilBackMask);

        GL.SetStencilFunction(GLEnuns.FC_FRONT, stencilFrontFunction.getInternalEnum(), stencilFrontFunRef, stencilFrontFunMask);
        GL.SetStencilFunction(GLEnuns.FC_BACK, stencilBackFunction.getInternalEnum(), stencilBackFunRef, stencilBackFunMask);

        GL.EnableBlend(blendEnabled);
        GL.SetBlendFunction(blendSrcColorFun.getInternalEnum(), blendDstColorFun.getInternalEnum(),
                blendSrcAlphaFun.getInternalEnum(), blendDstAlphaFun.getInternalEnum());

        GL.SetCullface(cullFronFace ? cullBackFace ? GLEnuns.FC_FRONT_AND_BACK : GLEnuns.FC_FRONT : cullBackFace ? GLEnuns.FC_BACK : 0);

        GL.SetFrontFace(clockWiseFrontFace ? GLEnuns.FF_CW : GLEnuns.FF_CCW);

        GL.SetColorMask(rMask, gMask, bMask, aMask);

        BufferObejct uniformBuffer = buffers[BufferType.Uniform.ordinal()];
        GL.BufferBind(BufferType.Uniform.getInternalEnum(), uniformBuffer == null ? 0 : uniformBuffer.getInternalID());

        BufferObejct arrayBuffer = buffers[BufferType.Array.ordinal()];
        if (arrayBuffer != null)
            GL.BufferBind(BufferType.Array.getInternalEnum(), arrayBuffer.getInternalID());

        if (vertexArray != null)
            GL.VertexArrayBind(vertexArray.getInternalID());

        if (shaderProgram != null)
            GL.ProgramUse(shaderProgram.getInternalID());

        GL.SetPixelStore(GLEnuns.PS_UNPACK_ALIGNMENT, pixelUnpackAligment);
        GL.SetPixelStore(GLEnuns.PS_UNPACK_ROW_LENGTH, pixelUnpackRowLength);
        GL.SetPixelStore(GLEnuns.PS_UNPACK_SKIP_PIXELS, pixelUnpackSkipPixels);
        GL.SetPixelStore(GLEnuns.PS_UNPACK_SKIP_ROWS, pixelUnpackSkipRows);

        if (textures[0] != null) {
            GL.SetActiveTexture(0);
            GL.TextureBind(textures[0].getInternalType(), textures[0].getInternalID());
        }
        if (activeTexture != 0) {
            GL.SetActiveTexture(activeTexture);
        }
    }

    private void svgApplyTransformGradientes() {
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
        if (!svgPaint.equals(paint)) {
            svgPaint = paint;
            if (svgMode) {
                svgApplyTransformGradientes();
            }
        }
    }

    public Paint svgPaint() {
        return svgPaint;
    }

    public void svgStrokeWidth(float strokeWidth) {
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
        if (transform == null) {
            svgTransform.identity();
            if (svgMode) {
                svgApplyTransformGradientes();
            }
        } else {
            svgTransform.set(transform);
            if (svgMode) {
                svgApplyTransformGradientes();
            }
        }
    }

    public Affine svgTransform() {
        return new Affine(svgTransform);
    }

    // ---- Temp Vars
    private float[] data = new float[6];

    public void svgClearClip(boolean clip) {
        svgFlush();
        svgBegin();
        SVG.ClearClip(svgId, clip ? 1 : 0);
    }

    public void svgClip(Shape shape) {
        if (shape.isEmpty()) return;

        PathIterator pi = shape.pathIterator(null);

        svgBegin();
        SVG.PathBegin(svgId, SVGEnuns.SVG_CLIP, pi.windingRule());
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
        if (shape.isEmpty()) return;

        PathIterator pi = shape.pathIterator(null);
        svgBegin();
        SVG.PathBegin(svgId, fill ? SVGEnuns.SVG_FILL : SVGEnuns.SVG_STROKE, pi.windingRule());
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
        if (width <= 0 || height <= 0) return;

        svgBegin();
        if (fill) {
            SVG.Rect(svgId, x, y, width, height);
        } else {
            SVG.PathBegin(svgId, SVGEnuns.SVG_STROKE, 0);
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
        if (width <= 0 || height <= 0) return;

        svgBegin();
        if (fill) {
            SVG.Ellipse(svgId, x, y, width, height);
        } else {
            float cx = x + width / 2f;
            float cy = y + height / 2f;
            float rx = width / 2f;
            float ry = height / 2f;
            SVG.PathBegin(svgId, SVGEnuns.SVG_STROKE, 0);
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
            SVG.PathBegin(svgId, SVGEnuns.SVG_STROKE, 0);
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
        svgBegin();
        SVG.PathBegin(svgId, SVGEnuns.SVG_STROKE, 0);
        SVG.MoveTo(svgId, x1, y1);
        SVG.LineTo(svgId, x2, y2);
        SVG.PathEnd(svgId);
    }

    public void svgDrawQuadCurve(float x1, float y1, float cx, float cy, float x2, float y2) {
        svgBegin();
        SVG.PathBegin(svgId, SVGEnuns.SVG_STROKE, 0);
        SVG.MoveTo(svgId, x1, y1);
        SVG.QuadTo(svgId, cx, cy, x2, y2);
        SVG.PathEnd(svgId);
    }

    public void svgDrawCubicCurve(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2) {
        svgBegin();
        SVG.PathBegin(svgId, SVGEnuns.SVG_STROKE, 0);
        SVG.MoveTo(svgId, x1, y1);
        SVG.CubicTo(svgId, cx1, cy1, cx2, cy2, x2, y2);
        SVG.PathEnd(svgId);
    }

    // ---- TEXT ----

    public void svgTextFont(Font font) {
        if (font == null) {
            font = Font.DEFAULT;
        }

        if (svgTextFont != font) {
            svgTextFont = font;
            if (svgMode) {
                SVG.SetFont(svgId, font.getInternalID());
            }
        }
    }

    public Font svgTextFont() {
        return svgTextFont;
    }

    public void svgTextScale(float scale) {
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
        if (svgTextVerticalAlign != align) {
            svgTextVerticalAlign = align;
        }
    }

    public float svgTextBlur() {
        return svgTextBlur;
    }

    public void svgTextBlur(float blur) {
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
        if (svgTextHorizontalAlign != align) {
            svgTextHorizontalAlign = align;
        }
    }

    public Align.Horizontal svgTextHorizontalAlign() {
        return svgTextHorizontalAlign;
    }

    public int svgDrawText(float x, float y, String text, float maxWidth) {
        int w = 0;
        if (text != null) {
            svgBegin();
            // SVG.PathBegin(svgId, SVG_TEXT, 0);
            w = SVG.DrawText(svgId, x, y, text, maxWidth, svgTextHorizontalAlign.getInternalEnum(), svgTextVerticalAlign.getInternalEnum());
            // SVG.PathEnd(svgId);
        }
        return w;
    }

    public int svgDrawText(float x, float y, Buffer text, int offset, int length, float maxWidth) {
        int w = 0;
        if (text != null && offset >= 0 && offset + length <= text.limit()) {
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