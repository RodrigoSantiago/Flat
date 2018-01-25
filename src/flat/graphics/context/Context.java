package flat.graphics.context;

import flat.backend.GL;
import flat.backend.SVG;
import flat.backend.WL;
import flat.graphics.context.enuns.*;
import flat.graphics.paint.*;
import flat.graphics.SmartContext;
import flat.graphics.text.*;
import flat.math.*;
import flat.math.shapes.PathIterator;
import flat.math.shapes.Shape;

import java.lang.ref.WeakReference;
import java.nio.Buffer;
import java.util.HashMap;

import static flat.backend.GLEnuns.*;
import static flat.backend.SVGEnuns.*;

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

    // ---- SVG ---- //
    private boolean svgMode;
    private int svgColor;
    private float svgAlpha;
    private Paint svgPaint;
    private float svgStrokeWidth;
    private LineCap svgLineCap;
    private LineJoin svgLineJoin;
    private boolean svgClip;
    private float svgClipX, svgClipY, svgClipWidth, svgClipHeight;

    private Font svgTextFont;
    private float svgTextSize, svgTextLetterSpacing, svgTextLineHeight;
    private Align.Vertical svgTextVerticalAlign;
    private Align.Horizontal svgTextHorizontalAlign;

    private Affine svgTransform;
    private Affine svgTransformView;

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

        multsampleEnabled = false;
        lineWidth = 1f;

        // ---- SVG ---- //
        svgMode = false;
        svgColor = 0xFFFFFFFF;
        svgAlpha = 1f;
        svgPaint = null;
        svgStrokeWidth = 1f;
        svgLineCap = LineCap.BUTT;
        svgLineJoin = LineJoin.ROUND;
        svgClip = false;
        svgClipX = 0;
        svgClipY = 0;
        svgClipWidth = 0;
        svgClipHeight = 0;

        svgTextFont = Font.DEFAULT;
        svgTextSize = 16f;
        svgTextLetterSpacing = 0f;
        svgTextLineHeight = 1f;
        svgTextVerticalAlign = Align.Vertical.TOP;
        svgTextHorizontalAlign = Align.Horizontal.LEFT;

        svgTransform = new Affine();
        svgTransformView = new Affine();
    }

    public void dispose() {
        smartContext = null;
        drawFrame = readFrame = null;
        shaderProgram = null;
        vertexArray = null;
        render = null;
        buffers = null;
        textures = null;
        Font.dispose(thread);
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
        GL.Clear((color ? CB_COLOR_BUFFER_BIT : 0) | (depth ? CB_DEPTH_BUFFER_BIT : 0) | (stencil ? CB_STENCIL_BUFFER_BIT : 0));
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

    public boolean isScizorEnabled() {
        return scizorEnabled;
    }

    public void setScissor(int x, int y, int width, int height) {
        if (scissorX != x || scissorY != y || scissorWidth != width || scissorHeight != height) {
            svgEnd();
            GL.SetScissor(scissorX = x, scissorY = y, scissorWidth = width, scissorHeight = height);
        }
    }

    public int getScissorX() {
        return scissorX;
    }

    public int getScissorY() {
        return scissorY;
    }

    public int getScissorWidth() {
        return scissorWidth;
    }

    public int getScissorHeight() {
        return scissorHeight;
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
                GL.SetPixelStore(PS_PACK_ALIGNMENT, pixelPackAligment = aligment);
            if (pixelPackRowLength != rowLength)
                GL.SetPixelStore(PS_PACK_ROW_LENGTH, pixelPackRowLength = rowLength);
            if (pixelPackSkipPixels != skipPixels)
                GL.SetPixelStore(PS_PACK_SKIP_PIXELS, pixelPackSkipPixels = skipPixels);
            if (pixelPackSkipRows != skipRows)
                GL.SetPixelStore(PS_PACK_SKIP_ROWS, pixelPackSkipRows = skipRows);
            if (pixelPackImageHeight != imageHeight)
                GL.SetPixelStore(PS_PACK_IMAGE_HEIGHT, pixelPackImageHeight = imageHeight);
            if (pixelPackSkipImages != skipImages)
                GL.SetPixelStore(PS_PACK_SKIP_IMAGES, pixelPackSkipImages = skipImages);
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
                GL.SetPixelStore(PS_UNPACK_ALIGNMENT, pixelUnpackAligment = aligment);
            if (pixelUnpackRowLength != rowLength)
                GL.SetPixelStore(PS_UNPACK_ROW_LENGTH, pixelUnpackRowLength = rowLength);
            if (pixelUnpackSkipPixels != skipPixels)
                GL.SetPixelStore(PS_UNPACK_SKIP_PIXELS, pixelUnpackSkipPixels = skipPixels);
            if (pixelUnpackSkipRows != skipRows)
                GL.SetPixelStore(PS_UNPACK_SKIP_ROWS, pixelUnpackSkipRows = skipRows);
            if (pixelUnpackImageHeight != imageHeight)
                GL.SetPixelStore(PS_UNPACK_IMAGE_HEIGHT, pixelUnpackImageHeight = imageHeight);
            if (pixelUnpackSkipImages != skipImages)
                GL.SetPixelStore(PS_UNPACK_SKIP_IMAGES, pixelUnpackSkipImages = skipImages);
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
            GL.SetStencilMask(FC_FRONT, stencilFrontMask = frontMask);
        }
        if (stencilBackMask != backMask) {
            svgEnd();
            GL.SetStencilMask(FC_BACK, stencilBackMask = backMask);
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
            GL.SetStencilFunction(FC_FRONT, (stencilFrontFunction = function).getInternalEnum(),
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
            GL.SetStencilFunction(FC_BACK, (stencilBackFunction = function).getInternalEnum(),
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
            GL.SetStencilOperation(FC_FRONT, (stencilFrontSFail = SFail).getInternalEnum(),
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
            GL.SetStencilOperation(FC_BACK, (stencilBackSFail = SFail).getInternalEnum(),
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
            GL.SetCullface(frontFace ? backFace ? FC_FRONT_AND_BACK : FC_FRONT : backFace ? FC_BACK : 0);
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
            GL.SetFrontFace((clockWiseFrontFace = clockWise) ? FF_CW : FF_CCW);
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
        GL.ReadPixels(x, y, width, height, DT_INT, offset);
    }

    public void readPixels(int x, int y, int width, int height, Buffer data, int offset) {
        softFlush();
        GL.ReadPixelsBuffer(x, y, width, height, DT_INT, data, offset);
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
        GL.DrawElements(vertexMode.getInternalEnum(), count, DT_INT, instances, first);
    }

    void bindFrame(Frame frame, boolean draw, boolean read) {
        int id = frame == null ? 0 : frame.getInternalID();
        if (draw && !read) {
            if (drawFrame != frame) {
                svgEnd();
                if (drawFrame != null) drawFrame.setDrawBindType(false);
                if (frame != null) frame.setDrawBindType(true);
                drawFrame = frame;
                GL.FrameBufferBind(FB_DRAW_FRAMEBUFFER, id);
            }
        } else if (read && !draw) {
            if (readFrame != frame) {
                svgEnd();
                if (readFrame != null) drawFrame.setReadBindType(false);
                if (frame != null) frame.setReadBindType(true);
                readFrame = frame;
                GL.FrameBufferBind(FB_READ_FRAMEBUFFER, id);
            }
        } else {
            if (drawFrame != frame || readFrame != frame) {
                svgEnd();
                drawFrame = readFrame = frame;
                if (drawFrame != null) drawFrame.setDrawBindType(false);
                if (readFrame != null) readFrame.setReadBindType(false);
                if (frame != null) frame.setBindType(true, true);
                GL.FrameBufferBind(FB_FRAMEBUFFER, id);
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
            if (buffer == null) {
                GL.BufferBind(type.getInternalEnum(), 0);
            } else {
                GL.BufferBind(type.getInternalEnum(), buffer.getInternalID());
                buffer.setBindType(type);
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
            if (array == null) {
                GL.VertexArrayBind(0);
            } else {
                GL.VertexArrayBind(array.getInternalID());
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

    protected void svgBegin() {
        if (!svgMode) {
            refreshBinds();

            svgMode = true;
            SVG.BeginFrame(svgId, viewWidth, viewHeight, 1.0f);

            SVG.SetStrokeColor(svgId, svgColor);
            SVG.SetFillColor(svgId, svgColor);
            SVG.SetGlobalAlpha(svgId, svgAlpha);
            SVG.SetStrokeWidth(svgId, svgStrokeWidth);
            SVG.SetLineCap(svgId, svgLineCap.getInternalEnum());
            SVG.SetLineJoin(svgId, svgLineJoin.getInternalEnum());

            SVG.TextSetFont(svgId, svgTextFont.getInternalID());
            SVG.TextSetSize(svgId, svgTextSize);
            SVG.TextSetLetterSpacing(svgId, svgTextLetterSpacing);
            SVG.TextSetLineHeight(svgId, svgTextLineHeight);
            SVG.TextSetAlign(svgId, svgTextHorizontalAlign.getInternalEnum() | svgTextVerticalAlign.getInternalEnum());

            SVG.TransformSet(svgId,
                    svgTransform.m00, svgTransform.m10,
                    svgTransform.m01, svgTransform.m11,
                    svgTransform.m02, svgTransform.m12);

            if (svgClip) {
                SVG.SetScissor(svgId, svgClipX, svgClipY, svgClipWidth, svgClipHeight);
            }
        }
    }

    protected void svgEnd() {
        if (svgMode) {
            svgMode = false;
            SVG.EndFrame(svgId);
            svgRestore();
        }
    }

    protected void svgRestore() {
        GL.EnableDepthTest(depthEnabled);
        GL.EnableScissorTest(scizorEnabled);

        GL.EnableStencilTest(stencilEnabled);
        GL.SetStencilMask(FC_FRONT, stencilFrontMask);
        GL.SetStencilMask(FC_BACK, stencilBackMask);

        GL.SetStencilFunction(FC_FRONT, stencilFrontFunction.getInternalEnum(), stencilFrontFunRef, stencilFrontFunMask);
        GL.SetStencilFunction(FC_BACK, stencilBackFunction.getInternalEnum(), stencilBackFunRef, stencilBackFunMask);

        GL.EnableBlend(blendEnabled);
        GL.SetBlendFunction(blendSrcColorFun.getInternalEnum(), blendDstColorFun.getInternalEnum(),
                blendSrcAlphaFun.getInternalEnum(), blendDstAlphaFun.getInternalEnum());

        GL.SetCullface(cullFronFace ? cullBackFace ? FC_FRONT_AND_BACK : FC_FRONT : cullBackFace ? FC_BACK : 0);

        GL.SetFrontFace(clockWiseFrontFace ? FF_CW : FF_CCW);

        GL.SetColorMask(rMask, gMask, bMask, aMask);

        //TODO - Nanovg doesn't unbind !?
        BufferObejct uniformBuffer = buffers[BufferType.Uniform.ordinal()];
        GL.BufferBind(BufferType.Uniform.getInternalEnum(), uniformBuffer == null ? 0 : uniformBuffer.getInternalID());

        BufferObejct arrayBuffer = buffers[BufferType.Array.ordinal()];
        if (arrayBuffer != null)
            GL.BufferBind(BufferType.Array.getInternalEnum(), arrayBuffer.getInternalID());

        if (vertexArray != null)
            GL.VertexArrayBind(vertexArray.getInternalID());

        if (shaderProgram != null)
            GL.ProgramUse(shaderProgram.getInternalID());

        GL.SetPixelStore(PS_UNPACK_ALIGNMENT, pixelUnpackAligment);
        GL.SetPixelStore(PS_UNPACK_ROW_LENGTH, pixelUnpackRowLength);
        GL.SetPixelStore(PS_UNPACK_SKIP_PIXELS, pixelUnpackSkipPixels);
        GL.SetPixelStore(PS_UNPACK_SKIP_ROWS, pixelUnpackSkipRows);

        if (textures[0] != null) {
            GL.SetActiveTexture(0);
            GL.TextureBind(textures[0].getInternalType(), textures[0].getInternalID());
        }
        if (activeTexture != 0) {
            GL.SetActiveTexture(activeTexture);
        }
    }

    public void svgColor(int color) {
        if (svgColor != color) {
            svgColor = color;
            if (svgMode) {
                SVG.SetStrokeColor(svgId, color);
                SVG.SetFillColor(svgId, color);
            }
        }
    }

    public int svgColor() {
        return svgColor;
    }

    public void svgAlpha(float alpha) {
        if (svgAlpha != alpha) {
            svgAlpha = alpha;
            if (svgMode) {
                SVG.SetGlobalAlpha(svgId, alpha);
            }
        }
    }

    public float svgAlpha() {
        return svgAlpha;
    }

    public void svgPaint(Paint paint) {
        if (svgPaint != paint) {
            svgPaint = paint;
            // todo add gradients
        }
    }

    public Paint svgPaint() {
        return svgPaint;
    }

    public void svgStrokeWidth(float strokeWifth) {
        if (svgStrokeWidth != strokeWifth) {
            svgStrokeWidth = strokeWifth;
            if (svgMode) {
                SVG.SetStrokeWidth(svgId, strokeWifth);
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
                SVG.SetLineCap(svgId, lineCap.getInternalEnum());
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
                SVG.SetLineJoin(svgId, lineJoin.getInternalEnum());
            }
        }
    }

    public LineJoin svgLineJoin() {
        return svgLineJoin;
    }

    public void svgTransform(Affine transform) {
        if (transform == null) {
            svgTransform.identity();
            if (svgMode) {
                SVG.TransformIdentity(svgId);
            }
        } else {
            svgTransform.set(transform);
            if (svgMode) {
                SVG.TransformSet(svgId,
                        svgTransform.m00, svgTransform.m10,
                        svgTransform.m01, svgTransform.m11,
                        svgTransform.m02, svgTransform.m12);
            }
        }
    }

    public Affine svgTransformView() {
        return svgTransformView.set(svgTransform);
    }

    public boolean svgClip() {
        return svgClip;
    }

    public void svgClip(boolean clip) {
        if (this.svgClip != clip) {
            this.svgClip = clip;
            if (svgMode) {
                if (clip) {
                    SVG.SetScissor(svgId, svgClipX, svgClipY, svgClipWidth, svgClipHeight);
                } else {
                    SVG.ResetScissor(svgId);
                }
            }
        }
    }

    public void svgClipBox(float x, float y, float width, float height) {
        if (svgClipX != x || svgClipY != y || svgClipWidth != width || svgClipHeight != height) {
            svgClipX = x;
            svgClipY = y;
            svgClipWidth = width;
            svgClipHeight = height;
            if (svgMode) {
                if (svgClip) {
                    SVG.SetScissor(svgId, svgClipX, svgClipY, svgClipWidth, svgClipHeight);
                }
            }
        }
    }

    public float svgClipX() {
        return svgClipX;
    }

    public float svgClipY() {
        return svgClipY;
    }

    public float svgClipWidth() {
        return svgClipWidth;
    }

    public float svgClipHeight() {
        return svgClipHeight;
    }

    public void svgDrawShape(Shape shape, boolean fill) {
        svgBegin();
        SVG.BeginPath(svgId);
        float[] data = new float[6];
        for (PathIterator pi = shape.pathIterator(null); !pi.isDone(); pi.next()) {
            switch (pi.currentSegment(data)) {
                case PathIterator.SEG_MOVETO:
                    SVG.PathWinding(svgId, SVG_CW);
                    SVG.MoveTo(svgId, data[0], data[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    SVG.LineTo(svgId, data[0], data[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    SVG.QuadTo(svgId, data[0], data[1], data[2], data[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    SVG.BezierTo(svgId, data[0], data[1], data[2], data[3], data[4], data[5]);
                    break;
                default:
                    SVG.ClosePath(svgId);
                    break;
            }
        }
        if (fill) {
            SVG.Fill(svgId);
        } else {
            SVG.Stroke(svgId);
        }
    }

    public void svgDrawShape(int[] types, float[] data, boolean fill) {
        svgBegin();
        SVG.BeginPath(svgId);
        int id = 0;
        for (int i = 0; i < types.length; i++) {
            switch (types[i]) {
                case 0:
                    SVG.MoveTo(svgId, data[id], data[id + 1]);
                    id += 2;
                    break;
                case 1:
                    SVG.LineTo(svgId, data[id], data[id + 1]);
                    id += 2;
                    break;
                case 2:
                    SVG.QuadTo(svgId, data[id], data[id + 1], data[id + 2], data[id + 3]);
                    id += 4;
                    break;
                case 3:
                    SVG.BezierTo(svgId, data[id], data[id + 1], data[id + 2], data[id + 3], data[id + 4], data[id + 5]);
                    id += 6;
                    break;
                default:
                    SVG.ClosePath(svgId);
                    break;
            }
        }
        if (fill) {
            SVG.Fill(svgId);
        } else {
            SVG.Stroke(svgId);
        }
    }

    public void svgDrawRect(float x, float y, float with, float height, boolean fill) {
        svgBegin();
        SVG.BeginPath(svgId);
        SVG.Rect(svgId, x, y, with, height);
        if (fill) {
            SVG.Fill(svgId);
        } else {
            SVG.Stroke(svgId);
        }
    }

    public void svgDrawEllipse(float x, float y, float width, float height, boolean fill) {
        svgBegin();
        SVG.BeginPath(svgId);
        SVG.Ellipse(svgId, x + width / 2f, y + height / 2f, width / 2f, height / 2f);
        if (fill) {
            SVG.Fill(svgId);
        } else {
            SVG.Stroke(svgId);
        }
    }

    public void svgDrawRoundRect(float x, float y, float width, float height, float cTop, float cRight, float cBottom, float cLeft, boolean fill) {
        svgBegin();
        SVG.BeginPath(svgId);
        SVG.RoundedRect(svgId, x, y, width, height, cTop, cRight, cBottom, cLeft);
        if (fill) {
            SVG.Fill(svgId);
        } else {
            SVG.Stroke(svgId);
        }
    }

    public void svgDrawArc(float x, float y, float radius, float angleA, float angleB, boolean fill) {
        svgBegin();
        SVG.BeginPath(svgId);
        SVG.Arc(svgId, x, y, radius, angleA, angleB, SVG_CCW);
        if (fill) {
            SVG.Fill(svgId);
        } else {
            SVG.Stroke(svgId);
        }
    }

    public void svgDrawLine(float x1, float y1, float x2, float y2) {
        svgBegin();
        SVG.BeginPath(svgId);
        SVG.MoveTo(svgId, x1, y1);
        SVG.LineTo(svgId, x2, y2);
        SVG.Stroke(svgId);
    }

    public void svgDrawQuadCurve(float x1, float y1, float x2, float y2, float cx, float cy) {
        svgBegin();
        SVG.BeginPath(svgId);
        SVG.MoveTo(svgId, x1, y1);
        SVG.QuadTo(svgId, cx, cy, x2, y2);
        SVG.Stroke(svgId);
    }

    public void svgDrawBezierCurve(float x1, float y1, float x2, float y2, float cx1, float cy1, float cx2, float cy2) {
        svgBegin();
        SVG.BeginPath(svgId);
        SVG.MoveTo(svgId, x1, y1);
        SVG.BezierTo(svgId, cx1, cy1, cx2, cy2, x2, y2);
        SVG.Stroke(svgId);
    }

    // ---- TEXT ----

    public void svgTextFont(Font font) {
        if (svgTextFont != font) {
            svgTextFont = font;
            if (svgMode) {
                SVG.TextSetFont(svgId, font.getInternalID());
            }
        }
    }

    public Font svgTextFont() {
        return svgTextFont;
    }

    public void svgTextSize(float size) {
        if (svgTextSize != size) {
            svgTextSize = size;
            if (svgMode) {
                SVG.TextSetSize(svgId, size);
            }
        }
    }

    public float svgTextSize() {
        return svgTextSize;
    }

    public void svgTextLetterSpacing(float spacing) {
        if (svgTextLetterSpacing != spacing) {
            svgTextLetterSpacing = spacing;
            if (svgMode) {
                SVG.TextSetLetterSpacing(svgId, spacing);
            }
        }
    }

    public float svgTextLetterSpacing() {
        return svgTextLetterSpacing;
    }

    public void svgTextLineHeight(float height) {
        if (svgTextLineHeight != height) {
            svgTextLineHeight = height;
            if (svgMode) {
                SVG.TextSetLineHeight(svgId, height);
            }
        }
    }

    public float svgTextLineHeight() {
        return svgTextLineHeight;
    }

    public void svgTextVerticalAlign(Align.Vertical align) {
        if (svgTextVerticalAlign != align) {
            svgTextVerticalAlign = align;
            if (svgMode) {
                SVG.TextSetAlign(svgId, svgTextHorizontalAlign.getInternalEnum() | svgTextVerticalAlign.getInternalEnum());
            }
        }
    }

    public Align.Vertical svgTextVerticalAlign() {
        return svgTextVerticalAlign;
    }

    public void svgTextHorizontalAlign(Align.Horizontal align) {
        if (svgTextHorizontalAlign != align) {
            svgTextHorizontalAlign = align;
            if (svgMode) {
                SVG.TextSetAlign(svgId, svgTextHorizontalAlign.getInternalEnum() | svgTextVerticalAlign.getInternalEnum());
            }
        }
    }

    public Align.Horizontal svgTextHorizontalAlign() {
        return svgTextHorizontalAlign;
    }

    public float svgTextGetWidth(String text) {
        if (text == null) throw new NullPointerException();
        svgBegin();
        return SVG.TextGetWidth(svgId, text);
    }

    public float svgTextGetWidth(Buffer text, int offset, int length) {
        if (text == null) throw new NullPointerException();
        svgBegin();
        return SVG.TextGetWidthBuffer(svgId, text, offset, length);
    }

    public void svgDrawText(float x, float y, String text) {
        if (text == null) throw new NullPointerException();
        svgBegin();
        SVG.BeginPath(svgId);
        SVG.DrawText(svgId, x, y, text);
    }

    public void svgDrawText(float x, float y, Buffer text, int offset, int length) {
        if (text == null) throw new NullPointerException();
        svgBegin();
        SVG.BeginPath(svgId);
        SVG.DrawTextBuffer(svgId, x, y, text, offset, length);
    }

    public void svgDrawTextBox(float x, float y, float maxWidth, String text) {
        if (text == null) throw new NullPointerException();
        svgBegin();
        SVG.BeginPath(svgId);
        SVG.DrawTextBox(svgId, x, y, maxWidth, text);
    }

    public void svgDrawTextBox(float x, float y, float maxWidth, Buffer text, int offset, int length) {
        if (text == null) throw new NullPointerException();
        svgBegin();
        SVG.BeginPath(svgId);
        SVG.DrawTextBoxBuffer(svgId, x, y, maxWidth, text, offset, length);
    }

    public void svgDrawTextSlice(float x, float y, float maxWidth, String text) {
        if (text == null) throw new NullPointerException();
        svgBegin();
        SVG.BeginPath(svgId);
        int last = SVG.TextGetLastGlyph(svgId, text, maxWidth);
        if (last > -1) {
            SVG.DrawText(svgId, x, y, text.substring(0, last + 1));
        }
    }

    public void svgDrawTextSlice(float x, float y, float maxWidth, Buffer text, int offset, int length) {
        if (text == null) throw new NullPointerException();
        svgBegin();
        SVG.BeginPath(svgId);
        int last = SVG.TextGetLastGlyphBuffer(svgId, text, offset, length, maxWidth);
        if (last > -1) {
            SVG.DrawTextBuffer(svgId, x, y, text, offset, last + 1);
        }
    }
}