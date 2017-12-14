package flat.graphics.context;

import flat.backend.GL;
import flat.backend.SVG;
import flat.graphics.context.enuns.*;
import flat.graphics.context.objects.DataBuffer;
import flat.graphics.context.objects.VertexArray;
import flat.graphics.context.objects.ShaderProgram;
import flat.graphics.context.objects.Frame;
import flat.graphics.context.objects.textures.Texture;
import flat.graphics.paint.*;
import flat.graphics.svg.Path;
import flat.graphics.text.*;
import flat.math.*;
import flat.screen.*;

import java.lang.ref.WeakReference;
import java.nio.Buffer;
import java.util.ArrayList;

import static flat.backend.GLEnuns.*;

public final class Context {

    private static Context context;
    private Thread thread;

    private Context(Thread thread) {
        this.thread = thread;
    }

    public static Context getContext() {
        if (context == null) {
            throw new RuntimeException("The context is not initialized yet");
        } else if (Thread.currentThread() != context.thread) {
            throw new RuntimeException("The context could not be acessed by others threads");
        }
        return context;
    }

    public static void initContext() {
        if (context != null) {
            throw new RuntimeException("The context is already initialized");
        } else if (Application.getApplication() == null) {
            throw new RuntimeException("The context could not be initialized before the application");
        } else if (Window.getWindow() == null) {
            throw new RuntimeException("The context could not be initialized before the window");
        } else {
            context = new Context(Thread.currentThread());
            context.init();
        }
    }

    //-----------------
    //    Core
    //-----------------
    private int clearColor, clearDepth, clearStencil;

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

    private Frame drawFrame, readFrame;
    private ShaderProgram shaderProgram;
    private VertexArray vertexArray;
    private int activeTexture;
    private final DataBuffer[] buffers = new DataBuffer[8];
    private final Texture[] textures = new Texture[32];

    //-----------------
    //    SVG
    //-----------------
    private boolean svgMode;
    private int svgColor;
    private float svgAlpha;
    private Paint svgPaint;
    private float svgStrokeWidth;
    private LineCap svgLineCap;
    private LineJoin svgLineJoin;

    private Font svgTextFont;
    private float svgTextSize, svgTextLetterSpacing, svgTextLineHeight;
    private Align.Vertical svgTextVerticalAlign;
    private Align.Horizontal svgTextHorizontalAlign;

    private Affine svgTransform;
    private Affine svgTransformView;

    private ArrayList<WeakReference<ContextObject>> objects = new ArrayList<>();

    public void assignObject(ContextObject object) {
        objects.add(new WeakReference<>(object));
    }

    public void releaseObject(ContextObject object) {
        for (int i = 0; i < objects.size(); i++) {
            WeakReference<ContextObject> wr = objects.get(i);
            ContextObject obj = wr.get();
            if (obj == object || obj == null) {
                objects.remove(i);
            }
        }
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

        scizorEnabled = false;
        scissorX = 0;
        scissorY = 0;
        scissorWidth = GL.GetScissorWidth();
        scissorHeight =  GL.GetScissorHeight();

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
        stencilBackFunction = MathFunction.ALLWAYS;
        stencilFrontFunction = MathFunction.ALLWAYS;
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
        ArrayList<WeakReference<ContextObject>> dis = objects;
        objects = new ArrayList<>();
        for (WeakReference<ContextObject> wr : dis) {
            ContextObject obj = wr.get();
            if (obj != null) {
                obj.dispose();
            }
        }
    }

    // ---- CORE ---- //

    public void flush() {
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

    public void setClearDepth(int depth) {
        if (clearDepth != depth) {
            svgEnd();
            GL.SetClearDepth(clearDepth = depth);
        }
    }

    public int getClearDepth() {
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
        svgEnd();
        if (pixelPackAligment != aligment)
            GL.SetPixelStore(PS_PACK_ALIGNMENT, this.pixelPackAligment = aligment);
        if (pixelPackRowLength != rowLength)
            GL.SetPixelStore(PS_PACK_ROW_LENGTH, this.pixelPackRowLength = rowLength);
        if (pixelPackSkipPixels != skipPixels)
            GL.SetPixelStore(PS_PACK_SKIP_PIXELS, this.pixelPackSkipPixels = skipPixels);
        if (pixelPackSkipRows != skipRows)
            GL.SetPixelStore(PS_PACK_SKIP_ROWS, this.pixelPackSkipRows = skipRows);
        if (pixelPackImageHeight != imageHeight)
            GL.SetPixelStore(PS_PACK_IMAGE_HEIGHT, this.pixelPackImageHeight = imageHeight);
        if (pixelPackSkipImages != skipImages)
            GL.SetPixelStore(PS_PACK_SKIP_IMAGES, this.pixelPackSkipImages = skipImages);
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
        svgEnd();
        if (pixelUnpackAligment != aligment)
            GL.SetPixelStore(PS_UNPACK_ALIGNMENT, this.pixelUnpackAligment = aligment);
        if (pixelUnpackRowLength != rowLength)
            GL.SetPixelStore(PS_UNPACK_ROW_LENGTH, this.pixelUnpackRowLength = rowLength);
        if (pixelUnpackSkipPixels != skipPixels)
            GL.SetPixelStore(PS_UNPACK_SKIP_PIXELS, this.pixelUnpackSkipPixels = skipPixels);
        if (pixelUnpackSkipRows != skipRows)
            GL.SetPixelStore(PS_UNPACK_SKIP_ROWS, this.pixelUnpackSkipRows = skipRows);
        if (pixelUnpackImageHeight != imageHeight)
            GL.SetPixelStore(PS_UNPACK_IMAGE_HEIGHT, this.pixelUnpackImageHeight = imageHeight);
        if (pixelUnpackSkipImages != skipImages)
            GL.SetPixelStore(PS_UNPACK_SKIP_IMAGES, this.pixelUnpackSkipImages = skipImages);
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
        svgEnd();
        GL.ReadPixels(x, y, width, height, DT_INT, offset);
    }

    public void readPixels(int x, int y, int width, int height, Buffer data, int offset) {
        svgEnd();
        GL.ReadPixelsBuffer(x, y, width, height, DT_INT, data, offset);
    }

    public void readPixels(int x, int y, int width, int height, int[] data, int offset) {
        svgEnd();
        GL.ReadPixelsI(x, y, width, height, data, offset);
    }

    public void readPixels(int x, int y, int width, int height, byte[] data, int offset) {
        svgEnd();
        GL.ReadPixelsB(x, y, width, height, data, offset);
    }

    public void drawArray(DrawVertexMode vertexMode, int first, int count, int instances) {
        svgEnd();
        GL.DrawArrays(vertexMode.getInternalEnum(), first, count, instances);
    }

    public void drawElements(DrawVertexMode vertexMode, int first, int count, int instances) {
        svgEnd();
        GL.DrawElements(vertexMode.getInternalEnum(), count, DT_INT, instances, first);
    }

    public void bindFrame(Frame frame, boolean draw, boolean read) {
        int id = frame == null ? 0 : frame.getInternalID();
        if (draw && !read) {
            if (drawFrame != frame) {
                svgEnd();
                drawFrame = frame;
                GL.FrameBufferBind(id, FB_DRAW_FRAMEBUFFER);
            }
        } else if (read && !draw) {
            if (readFrame != frame) {
                svgEnd();
                readFrame = frame;
                GL.FrameBufferBind(id, FB_READ_FRAMEBUFFER);
            }
        } else {
            if (drawFrame != frame || readFrame != frame) {
                svgEnd();
                drawFrame = readFrame = frame;
                GL.FrameBufferBind(id, FB_FRAMEBUFFER);
            }
        }
    }

    public Frame getBoundDrawFrame() {
        return drawFrame;
    }

    public Frame getBoundReadFrame() {
        return readFrame;
    }

    public void setActiveTexture(int activeTexture) {
        if (this.activeTexture != activeTexture) {
            svgEnd();
            GL.SetActiveTexture(this.activeTexture = activeTexture);
        }
    }

    public int getActiveTexture() {
        return activeTexture;
    }

    public void bindTexture(Texture texture, int index) {
        setActiveTexture(index);
        if (textures[index] != texture) {
            svgEnd();
            if (texture == null) {
                GL.TextureBind(textures[index].getInternalType(), 0);
            } else {
                GL.TextureBind(texture.getInternalType(), texture.getInternalID());
            }
            textures[index] = texture;
        }
    }

    public Texture getBoundTexture(int index) {
        return textures[index];
    }

    public void bindBuffer(DataBuffer buffer, BufferType type) {
        int index = type.getInternalIndex();
        if (buffers[index] != buffer) {
            svgEnd();
            if (buffer == null) {
                GL.BufferBind(type.getInternalEnum(), 0);
            } else {
                GL.TextureBind(type.getInternalEnum(), buffer.getInternalID());
                buffer.setInternalType(type);
            }
            buffers[index] = buffer;
        }
    }

    public DataBuffer getBoundBuffer(BufferType type) {
        return buffers[type.getInternalIndex()];
    }

    public void bindVertexArray(VertexArray vertexArray) {
        if (this.vertexArray != vertexArray) {
            svgEnd();
            if (vertexArray == null) {
                GL.VertexArrayBind(0);
            } else {
                GL.VertexArrayBind(vertexArray.getInternalID());
            }
            this.vertexArray = vertexArray;
        }
    }

    public VertexArray getBoundVertexArray() {
        return vertexArray;
    }

    public void bindShaderProgram(ShaderProgram shaderProgram) {
        if (this.shaderProgram != shaderProgram) {
            svgEnd();
            if (shaderProgram == null) {
                GL.ProgramUse(0);
            } else {
                GL.ProgramUse(shaderProgram.getInternalID());
            }
            this.shaderProgram = shaderProgram;
        }
    }

    public ShaderProgram getBoundShaderProgram() {
        return shaderProgram;
    }

    // ---- SVG ---- //

    protected void svgBegin() {
        if (!svgMode) {
            svgMode = true;
            SVG.BeginFrame(viewWidth, viewHeight, 1.0f);

            SVG.SetStrokeColor(svgColor);
            SVG.SetFillColor(svgColor);
            SVG.SetGlobalAlpha(svgAlpha);
            SVG.SetStrokeWidth(svgStrokeWidth);
            SVG.SetLineCap(svgLineCap.getInternalEnum());
            SVG.SetLineJoin(svgLineJoin.getInternalEnum());

            SVG.TextSetFont(svgTextFont.getInternalID());
            SVG.TextSetSize(svgTextSize);
            SVG.TextSetLetterSpacing(svgTextLetterSpacing);
            SVG.TextSetLineHeight(svgTextLineHeight);
            SVG.TextSetAlign(svgTextHorizontalAlign.getInternalEnum() | svgTextVerticalAlign.getInternalEnum());

            SVG.TransformSet(
                    svgTransform.m00, svgTransform.m10,
                    svgTransform.m01, svgTransform.m11,
                    svgTransform.m02, svgTransform.m12);
        }
    }

    protected void svgEnd() {
        if (svgMode) {
            svgMode = false;
            SVG.EndFrame();
            svgRestore();
        }
    }

    protected void svgRestore()  {
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
        DataBuffer uniformBuffer = buffers[BufferType.Uniform.getInternalIndex()];
        GL.BufferBind(BufferType.Uniform.getInternalEnum(), uniformBuffer == null ? 0 : uniformBuffer.getInternalID());

        DataBuffer arrayBuffer = buffers[BufferType.Array.getInternalIndex()];
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

        // Atual texture
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
            this.svgColor = color;
            if (svgMode) {
                SVG.SetStrokeColor(color);
                SVG.SetFillColor(color);
            }
        }
    }

    public int svgColor() {
        return svgColor;
    }

    public void svgAlpha(float alpha) {
        if (svgAlpha != alpha) {
            this.svgAlpha = alpha;
            if (svgMode) {
                SVG.SetGlobalAlpha(alpha);
            }
        }
    }

    public float svgAlpha() {
        return svgAlpha;
    }

    public void svgPaint(Paint paint) {
        if (svgPaint != paint) {
            this.svgPaint = paint;
            // todo add gradients
        }
    }

    public Paint svgPaint() {
        return svgPaint;
    }

    public void svgStrokeWidth(float strokeWifth) {
        if (svgStrokeWidth != strokeWifth) {
            this.svgStrokeWidth = strokeWifth;
            if (svgMode) {
                SVG.SetStrokeWidth(strokeWifth);
            }
        }
    }

    public float svgStrokeWidth() {
        return svgStrokeWidth;
    }

    public void svgLineCap(LineCap lineCap) {
        if (svgLineCap != lineCap) {
            this.svgLineCap = lineCap;
            if (svgMode) {
                SVG.SetLineCap(lineCap.getInternalEnum());
            }
        }
    }

    public LineCap svgLineCap() {
        return svgLineCap;
    }

    public void svgLineJoin(LineJoin lineJoin) {
        if (svgLineJoin != lineJoin) {
            this.svgLineJoin = lineJoin;
            if (svgMode) {
                SVG.SetLineJoin(lineJoin.getInternalEnum());
            }
        }
    }

    public LineJoin svgLineJoin() {
        return svgLineJoin;
    }

    public void svgTransform(Affine transform) {
        if (transform == null) {
            this.svgTransform.identity();
            if (svgMode) {
                SVG.TransformIdentity();
            }
        } else {
            this.svgTransform.set(transform);
            if (svgMode) {
                SVG.TransformSet(
                        svgTransform.m00, svgTransform.m10,
                        svgTransform.m01, svgTransform.m11,
                        svgTransform.m02, svgTransform.m12);
            }
        }
    }

    public Affine svgTransformView() {
        return svgTransformView.set(svgTransform);
    }

    public void svgDrawPath(Path path) {
        svgBegin();
        SVG.BeginPath();
    }

    public void svgDrawRect() {
        svgBegin();
        SVG.BeginPath();
    }

    public void svgDrawEllipse() {
        svgBegin();
        SVG.BeginPath();
    }

    public void svgDrawRoundRect() {
        svgBegin();
        SVG.BeginPath();
    }

    public void svgDrawArch() {
        svgBegin();
        SVG.BeginPath();
    }

    public void svgDrawLine() {
        svgBegin();
        SVG.BeginPath();
    }

    public void svgDrawQuadCurve() {
        svgBegin();
        SVG.BeginPath();
    }

    public void svgDrawBezierCurve() {
        svgBegin();
        SVG.BeginPath();
    }

    // ---- SVG/TEXT ----
    public void svgTextFont(Font font) {
        if  (svgTextFont != font) {
            this.svgTextFont = font;
            if (svgMode) {
                SVG.TextSetFont(font.getInternalID());
            }
        }
    }

    public Font svgTextFont() {
        return svgTextFont;
    }

    public void svgTextSize(float size) {
        if (svgTextSize != size) {
            this.svgTextSize = size;
            if (svgMode) {
                SVG.TextSetSize(size);
            }
        }
    }

    public float svgTextSize() {
        return svgTextSize;
    }

    public void svgTextLetterSpacing(float spacing) {
        if (svgTextLetterSpacing != spacing) {
            this.svgTextLetterSpacing = spacing;
            if (svgMode) {
                SVG.TextSetLetterSpacing(spacing);
            }
        }
    }

    public float svgTextLetterSpacing() {
        return svgTextLetterSpacing;
    }

    public void svgTextLineHeight(float height) {
        if (svgTextLineHeight != height) {
            this.svgTextLineHeight = height;
            if (svgMode) {
                SVG.TextSetLineHeight(height);
            }
        }
    }

    public float svgTextLineHeight() {
        return svgTextLineHeight;
    }

    public void svgTextVerticalAlign(Align.Vertical align) {
        this.svgTextVerticalAlign = align;
        if (svgMode) {
            SVG.TextSetAlign(svgTextHorizontalAlign.getInternalEnum() | svgTextVerticalAlign.getInternalEnum());
        }
    }

    public Align.Vertical svgTextVerticalAlign() {
        return svgTextVerticalAlign;
    }

    public void svgTextHorizontalAlign(Align.Horizontal align) {
        this.svgTextHorizontalAlign = align;
        if (svgMode) {
            SVG.TextSetAlign(svgTextHorizontalAlign.getInternalEnum() | svgTextVerticalAlign.getInternalEnum());
        }
    }

    public Align.Horizontal svgTextHorizontalAlign() {
        return svgTextHorizontalAlign;
    }

    public float svgTextGetWidth() {
        return 0f;
    }

    public void svgDrawText() {

    }

    public void svgDrawTextBox() {

    }

    public void svgDrawTextSlice() {

    }
}