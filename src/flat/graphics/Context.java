package flat.graphics;

import flat.backend.GL;
import flat.backend.SVG;
import flat.graphics.effects.Effect;
import flat.graphics.effects.HBlur;
import flat.graphics.effects.VBlur;
import flat.graphics.image.Image;
import flat.graphics.paint.*;
import flat.graphics.svg.*;
import flat.graphics.text.*;
import flat.math.*;
import flat.screen.*;

import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static flat.backend.GLEnuns.*;
import static flat.backend.SVGEnuns.*;

public final class Context {

    private static Context context;
    private final Thread thread;

    private Context(Thread thread) {
        this.thread = thread;
    }

    public static Context getContext() {
        if (Thread.currentThread() != context.thread) {
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

    private int mode;
    private int width;
    private int height;
    private int svgViewWidth;
    private int svgViewHeight;
    private Surface applicationSurface;
    private Texture applicationTexture;
    private Texture applicationTextureBack;
    private Shader backShader;

    private ArrayList<WeakReference<ContextObject>> objects = new ArrayList<>();

    //-----------------
    //    SVG
    //-----------------
    private int color;
    private Paint paint;
    private float strokeWidth = 1.0f;
    private LineCap strokeCap;
    private LineJoin strokeJoin;

    private final Affine idt = new Affine();
    private Affine tr2 = new Affine();

    //-----------------
    //    2D
    //-----------------
    private Shader imageShader;
    private VertexArray imageBatch;
    private int imageBatchCount;
    private Texture imageBatchAtlas;
    private float[] parser = new float[24];
    private HBlur shadowPassH;
    private VBlur shadowPassV;

    public String vertex, fragment;
    {
        try {
            vertex = new String(Files.readAllBytes(Paths.get(getClass().getResource("resources/vertex.glsl").toURI())));
            fragment = new String(Files.readAllBytes(Paths.get(getClass().getResource("resources/fragment.glsl").toURI())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void assignObject(ContextObject object) {
        objects.add(new WeakReference<>(object));
    }

    protected void releaseObject(ContextObject object) {
        for (int i = 0; i < objects.size(); i++) {
            WeakReference<ContextObject> wr = objects.get(i);
            ContextObject obj = wr.get();
            if (obj == object || obj == null) {
                objects.remove(i);
            }
        }
    }

    private void init() {
        imageShader = new Shader(vertex, fragment);
        imageShader.compile();
        if (!imageShader.isCompiled()) {
            System.out.println("erro ao compilar shader");
            System.out.println(imageShader.getFragmentLog());
            System.out.println(imageShader.getVertexLog());
            System.out.println(imageShader.getLog());
        }

        imageBatch = new VertexArray();
        imageBatch.setData(1000 * 24);
        imageBatch.setAttributes(0, 2, 4, 0);
        imageBatch.setAttributes(1, 2, 4, 2);


        applicationSurface = new Surface(64, 64);
        applicationTexture = new Texture(64, 64);
        applicationTextureBack = new Texture(64, 64);
        applicationSurface.attachTexture(applicationTexture);

        shadowPassH = new HBlur();
        shadowPassV = new VBlur();
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

    //--------------------
    //    Clear Mode
    //--------------------
    private void ClearMode() {
        if (mode != 0) {
            if (mode == 1) SVGFinalize();
            if (mode == 2) ImageFinalize();
            if (mode == 3) CoreFinalize();
            mode = 0;
        }
    }

    public void clear() {
        clear(true, true, true);
    }

    public void clear(boolean color, boolean depth, boolean stencil) {
        GL.Clear((color ? CB_COLOR_BUFFER_BIT : 0) | (depth ? CB_DEPTH_BUFFER_BIT : 0) | (stencil ? CB_STENCIL_BUFFER_BIT : 0));
    }

    public void softFlush() {
        ClearMode();
    }

    public void hardFlush() {
        ClearMode();
        GL.Flush();
    }

    public void setView(int x, int y, int width, int height) {
        GL.SetViewport(x, y, svgViewWidth = width, svgViewHeight = height);
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        getApplicationSurface().resize(width, height);
        getApplicationTexture().resize(width, height);
        applicationTextureBack.resize(width, height);
    }

    public void setSurface(Surface surface) {
        softFlush();

        int toBind = surface == null ? 0 : surface.frameBufferId;
        int fb = GL.FrameBufferGetBound(FB_FRAMEBUFFER);
        if (toBind != fb) {
            GL.FrameBufferBind(FB_FRAMEBUFFER, toBind);
        }
    }

    public Surface getApplicationSurface() {
        return applicationSurface;
    }

    public Texture getApplicationTexture() {
        return applicationTexture;
    }

    //--------------------
    //    SVG Mode
    //--------------------
    private void SVGMode() {
        if (mode != 1) {
            if (mode == 2) ImageFinalize();
            if (mode == 3) CoreFinalize();
            mode = 1;

            SVG.BeginFrame(svgViewWidth, svgViewHeight, 1);
            SVG.SetLineCap(strokeCap == LineCap.BUTT ? SVG_BUTT : strokeCap == LineCap.SQUARE ? SVG_SQUARE : SVG_ROUND);
            SVG.SetLineJoin(strokeJoin == LineJoin.BEVEL ? SVG_BEVEL : strokeJoin == LineJoin.MITER ? SVG_MITER : SVG_ROUND);
            SVG.SetStrokeWidth(strokeWidth);
            SVG.SetStrokeColor(color);
            SVG.SetFillColor(color);
            SVG.TransformSet(tr2.m00, tr2.m10, tr2.m01, tr2.m11, tr2.m02, tr2.m12);
        }
    }

    private void SVGFinalize() {
        SVG.EndFrame();

    }

    public void setSVGTransform(Affine transform2D) {
        if (transform2D == null) {
            this.tr2 = idt;
        } else {
            this.tr2 = transform2D;
        }
        if (mode == 1) {
            SVG.TransformSet(tr2.m00, tr2.m10, tr2.m01, tr2.m11, tr2.m02, tr2.m12);
        }
    }

    public void setComposite() {

    }

    public void setBlendMode() {

    }

    public void setPaint(Paint paint) {
        this.paint = paint;
}

    public void setColor(int rgba) {
        this.color = rgba;
        this.paint = null;
        if (mode == 1) {
            SVG.SetFillColor(color);
            SVG.SetStrokeColor(color);
        }
    }

    public void setStrokeWidth(float size) {
        strokeWidth = Math.max(0.0001f, size);
        if (mode == 1) {
            SVG.SetStrokeWidth(strokeWidth);
        }
    }

    public void setStrokeCap(LineCap cap) {
        strokeCap = cap;
        if (mode == 1) {
            SVG.SetLineCap(strokeCap == LineCap.BUTT ? SVG_BUTT : strokeCap == LineCap.SQUARE ? SVG_SQUARE : SVG_ROUND);
        }
    }

    public void setStrokeJoints(LineJoin join) {
        strokeJoin = join;
        if (mode == 1) {
            SVG.SetLineJoin(strokeJoin == LineJoin.BEVEL ? SVG_BEVEL : strokeJoin == LineJoin.MITER ? SVG_MITER : SVG_ROUND);
        }
    }

    public void drawLine(float x1, float y1, float x2, float y2) {
        SVGMode();
        SVG.BeginPath();
        SVG.MoveTo(x1, y1);
        SVG.LineTo(x2, y2);
        SVG.Stroke();
    }

    public void drawQuad(float x1, float y1, float x2, float y2, float cx, float cy) {
        SVGMode();
        SVG.BeginPath();
        SVG.MoveTo(x1, y1);
        SVG.QuadTo(cx, cy, x2, y2);
        SVG.Stroke();
    }

    public void drawBezier(float x1, float y1, float x2, float y2, float cx1, float cy1, float cx2, float cy2) {
        SVGMode();
        SVG.BeginPath();
        SVG.MoveTo(x1, y1);
        SVG.BezierTo(cx1, cy1, cx2, cy2,x2, y2);
        SVG.Stroke();
    }

    public void drawCircle(float x, float y, float r, boolean fill) {
        SVGMode();
        SVG.BeginPath();
        SVG.Circle(x, y, r);
        if (fill) {
            SVG.Fill();
        } else {
            SVG.Stroke();
        }
    }

    public void drawEllipse(float x, float y, float width, float height, boolean fill) {
        SVGMode();
        SVG.BeginPath();
        SVG.Ellipse(x, y, width, height);
        if (fill) {
            SVG.Fill();
        } else {
            SVG.Stroke();
        }
    }

    public void drawRect(float x, float y, float width, float height, boolean fill) {
        SVGMode();
        SVG.BeginPath();
        SVG.Rect(x, y, width, height);
        if (fill) {
            SVG.Fill();
        } else {
            SVG.Stroke();
        }
    }

    public void drawRoundRect(float x, float y, float width, float height, float corners, boolean fill) {
        SVGMode();
        SVG.BeginPath();
        SVG.RoundedRect(x, y, width, height, corners / 2f);
        if (fill) {
            SVG.Fill();
        } else {
            SVG.Stroke();
        }
    }

    public void drawSvg(Svg svg, float x, float y, boolean fill) {

    }

    public void setTextFont(Font font) {

    }

    public void setTextSize(float size) {

    }

    public void setTextAlign(TextAligment align) {

    }

    public void drawText(String text, float x, float y) {

    }

    public void drawText(String text, float x, float y, float maxWidth) {

    }

    //--------------------
    //    Image Mode
    //--------------------
    private void ImageMode() {
        if (mode != 2) {
            if (mode == 1) SVGFinalize();
            if (mode == 3) CoreFinalize();
            mode = 2;

            int programId = imageShader.shaderProgramId;
            if (programId != GL.ProgramGetUsed()) {
                GL.ProgramUse(programId);
            }
        }
    }

    private void ImageFinalize() {
        drawImages(null);

    }

    private void drawImages(Effect effect) {
        if (imageBatchCount > 0) {
            int vao = imageBatch.vertexArrayId;
            int bVao = GL.VertexArrayGetBound();

            if  (vao != bVao) {
                GL.VertexArrayBind(vao);
            }

            if (effect == null) {
                imageShader.setInt("effectType", 0);
            } else {
                effect.applyAttributes(imageShader);
            }

            GL.TextureBind(TB_TEXTURE_2D, imageBatchAtlas.textureId);
            GL.DrawArrays(VM_TRIANGLES, 0, imageBatchCount * 6, 1);

            if (vao != bVao) {
                GL.VertexArrayBind(bVao);
            }

            imageBatchCount = 0;
            imageBatchAtlas = null;
        }
    }

    public void drawImage(Image image, Affine affine) {
        drawImage(image, 0, 0, image.getWidth(), image.getHeight(),
                affine.getPointX(0, 0),
                affine.getPointY(0, 0),
                affine.getPointX(image.getWidth(), image.getHeight()),
                affine.getPointY(image.getWidth(), image.getHeight()));
    }

    public void drawImage(Image image, float x, float y) {
        drawImage(image, x, y, image.getWidth(), image.getHeight());
    }

    public void drawImage(Image image, float x, float y, float width, float height) {
        drawImage(image, 0, 0, image.getWidth(), image.getHeight(), x, y, width, height);
    }

    public void drawImage(Image image, float srcX1, float srcY1, float srcX2, float srcY2, float dstX1, float dstY1, float dstX2, float dstY2) {
        drawImage(image.getAtlas(), srcX1, srcY1, srcX2, srcY2, dstX1, dstY1, dstX2, dstY2);
    }

    public void drawImage(Texture texture, float srcX1, float srcY1, float srcX2, float srcY2, float dstX1, float dstY1, float dstX2, float dstY2) {
        drawImage(null, texture, srcX1, srcY1, srcX2, srcY2, dstX1, dstY1, dstX2, dstY2);
    }

    public void drawImage(Effect effect, Texture texture, float srcX1, float srcY1, float srcX2, float srcY2, float dstX1, float dstY1, float dstX2, float dstY2) {
        ImageMode();

        if (imageBatchCount > 1000 || texture != imageBatchAtlas) {
            drawImages(null);
        }

        imageBatchAtlas = texture;

        srcY1 = texture.height - srcY1;
        srcY2 = texture.height - srcY2;

        dstX1 = dstX1 * 2 / width - 1f;
        dstX2 = dstX2 * 2 / width - 1f;
        dstY1 = -((dstY1 * 2 / height) - 1f);
        dstY2 = -((dstY2 * 2 / height) - 1f);

        parser[ 0] = dstX1; parser[ 1] = dstY1; parser[ 2] = srcX1; parser[ 3] = srcY1;
        parser[ 4] = dstX2; parser[ 5] = dstY1; parser[ 6] = srcX2; parser[ 7] = srcY1;
        parser[ 8] = dstX2; parser[ 9] = dstY2; parser[10] = srcX2; parser[11] = srcY2;

        parser[12] = dstX2; parser[13] = dstY2; parser[14] = srcX2; parser[15] = srcY2;
        parser[16] = dstX1; parser[17] = dstY2; parser[18] = srcX1; parser[19] = srcY2;
        parser[20] = dstX1; parser[21] = dstY1; parser[22] = srcX1; parser[23] = srcY1;

        imageBatch.setSubData(parser, imageBatchCount++ * 24);
        if (effect != null) {
            drawImages(effect);
        }
    }

    public void drawRoundRectShadow(float x, float y, float width, float height, float corners, float blur, float alpha) {

        // Draw the round rect
        setSurface(getApplicationSurface());
        clear();
        SVGMode();
        SVG.SetGlobalAlpha(alpha);
        SVG.SetFillColor(0x000000FF);
        drawRoundRect(x, y, width, height, corners, true);
        SVG.SetFillColor(color);
        SVG.SetGlobalAlpha(1);
        setSurface(null);

        // Calculate the internal blur limits
        boolean use;
        float ix1, ix2, iy1, iy2;
        {
            float aw = Math.min(width, corners) / 2.0f;
            float ah = Math.min(height, corners) / 2.0f;

            float tix1 = -0.7071f * aw + aw + blur / 2f;
            float tiy1 = -0.7071f * ah + ah + blur / 2f;
            float tix2 = width - tix1;
            float tiy2 = height - tiy1;
            tix1 += x;
            tiy1 += y;
            tix2 += x;
            tiy2 += y;
            ix1 = tr2.getPointX(tix1, tiy1);
            iy1 = tr2.getPointY(tix1, tiy1);
            ix2 = tr2.getPointX(tix2, tiy2);
            iy2 = tr2.getPointY(tix2, tiy2);
            use = tix2 > tix1 && tiy2 > tiy1;
        }
        // Calculate the external blur limits
        float ox1, ox2, oy1, oy2;
        {
            ox1 = tr2.getPointX(x - blur, y - blur);
            oy1 = tr2.getPointY(x - blur, y - blur);
            ox2 = tr2.getPointX(x + width + blur, y + height + blur);
            oy2 = tr2.getPointY(x + width + blur, y + height + blur);
        }
        shadowPassH.setBlur((int) blur);
        shadowPassV.setBlur((int) blur);

        //Desenha blur horizontal
        applicationSurface.detachTexture(applicationTexture);
        applicationSurface.attachTexture(applicationTextureBack);
        setSurface(applicationSurface);
        clear();
        if (use) {
            drawImage(shadowPassH, applicationTexture, ix1, oy1, ox2, iy1, ix1, oy1, ox2, iy1);
            drawImage(shadowPassH, applicationTexture, ix2, iy1, ox2, oy2, ix2, iy1, ox2, oy2);
            drawImage(shadowPassH, applicationTexture, ox1, iy2, ix2, oy2, ox1, iy2, ix2, oy2);
            drawImage(shadowPassH, applicationTexture, ox1, oy1, ix1, iy2, ox1, oy1, ix1, iy2);
            drawImage(applicationTexture, ix1, iy1, ix2, iy2, ix1, iy1, ix2, iy2);
        } else {
            drawImage(shadowPassH, applicationTexture, ox1, oy1, ox2, oy2, ox1, oy1, ox2, oy2);
        }
        setSurface(null);
        applicationSurface.detachTexture(applicationTextureBack);
        applicationSurface.attachTexture(applicationTexture);

        //Desenha blur vertical, finalizando a operação
        if (use) {
            drawImage(shadowPassV, applicationTextureBack, ix1, oy1, ox2, iy1, ix1, oy1, ox2, iy1);
            drawImage(shadowPassV, applicationTextureBack, ix2, iy1, ox2, oy2, ix2, iy1, ox2, oy2);
            drawImage(shadowPassV, applicationTextureBack, ox1, iy2, ix2, oy2, ox1, iy2, ix2, oy2);
            drawImage(shadowPassV, applicationTextureBack, ox1, oy1, ix1, iy2, ox1, oy1, ix1, iy2);
            drawImage(applicationTextureBack, ix1, iy1, ix2, iy2, ix1, iy1, ix2, iy2);
        } else {
            drawImage(shadowPassV, applicationTextureBack, ox1, oy1, ox2, oy2, ox1, oy1, ox2, oy2);
        }
    }

    //--------------------
    //    Core Mode
    //--------------------
    private void CoreMode() {
        if (mode != 3) {
            if (mode == 1) SVGFinalize();
            if (mode == 2) ImageFinalize();
            mode = 3;
        }
    }

    private void CoreFinalize() {

    }

    public void setShader(Shader shader) {
        CoreMode();

        int programId = shader == null ? 0 : shader.shaderProgramId;
        if  (programId != GL.ProgramGetUsed()) {
            GL.ProgramUse(programId);
        }
    }

    public void drawVertexTriangles(VertexArray vertexArray, int offset, int length) {
        CoreMode();

        int vao = GL.VertexArrayGetBound();

        GL.VertexArrayBind(vertexArray.vertexArrayId);
        GL.DrawArrays(VM_TRIANGLES, offset, length, 1);

        GL.VertexArrayBind(vao);
    }

    public void drawVertexLines(VertexArray vertexArray, int offset, int length) {
        CoreMode();

        int vao = GL.VertexArrayGetBound();

        GL.VertexArrayBind(vertexArray.vertexArrayId);
        GL.DrawArrays(VM_LINES, offset, length, 1);

        GL.VertexArrayBind(vao);
    }
}
