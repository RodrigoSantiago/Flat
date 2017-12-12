package flat.graphics;

import flat.backend.GL;
import flat.backend.SVG;
import flat.graphics.effects.Effect;
import flat.graphics.effects.RoundRectShadow;
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
    private float[] transform;

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
    private Surface applicationSurface;
    private Texture applicationTexture;
    private Texture applicationTextureBack;
    private Shader backShader;

    private ArrayList<WeakReference<ContextObject>> objects = new ArrayList<>();

    //-----------------
    //    SVG
    //-----------------
    private int color = 0xFFFFFFFF;
    private float alpha = 1f;
    private Paint paint = null;
    private float strokeWidth = 1f;
    private LineCap strokeCap = LineCap.BUTT;
    private LineJoin strokeJoin = LineJoin.ROUND;

    private Font font = Font.DEFAULT;
    private float fontSize = 16f;
    private VAlign textVAlign = VAlign.TOP;
    private HAlign textHAlign = HAlign.LEFT;

    private Affine tr2 = new Affine();
    private Affine trView = new Affine();

    //-----------------
    //    2D
    //-----------------
    private Shader imageShader;
    private Shader shadowShader;
    private VertexArray imageBatch;
    private Texture imageBatchAtlas;
    private int imageBatchLimit = 100;
    private int imageBatchCount;
    private float[] parser = new float[24];

    private Effect imageEffect;
    private RoundRectShadow shadowEffect;

    public String vertex, fragment, shadowVtx, shadowFrg;

    {
        try {
            vertex = new String(Files.readAllBytes(Paths.get(getClass().getResource("/resources/vertex.glsl").toURI())));
            fragment = new String(Files.readAllBytes(Paths.get(getClass().getResource("/resources/fragment.glsl").toURI())));

            shadowVtx = new String(Files.readAllBytes(Paths.get(getClass().getResource("/resources/shadow.vtx.glsl").toURI())));
            shadowFrg = new String(Files.readAllBytes(Paths.get(getClass().getResource("/resources/shadow.frg.glsl").toURI())));
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
        shadowShader = new Shader(shadowVtx, shadowFrg);
        shadowShader.compile();
        if (!shadowShader.isCompiled()) {
            System.out.println("erro ao compilar shader");
            System.out.println(shadowShader.getFragmentLog());
            System.out.println(shadowShader.getVertexLog());
            System.out.println(shadowShader.getLog());
        }

        imageBatch = new VertexArray();
        imageBatch.setData(imageBatchLimit * 24);
        imageBatch.setAttributes(0, 2, 4, 0);
        imageBatch.setAttributes(1, 2, 4, 2);

        applicationSurface = new Surface(64, 64);
        applicationTexture = new Texture(64, 64);
        applicationTextureBack = new Texture(64, 64);
        applicationSurface.attachTexture(applicationTexture);

        shadowEffect = new RoundRectShadow();

        GL.EnableBlend(true);
        GL.SetBlendFunction(BF_SRC_ALPHA, BF_ONE_MINUS_SRC_ALPHA, BF_ONE, BF_ZERO);
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

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        getApplicationSurface().resize(width, height);
        getApplicationTexture().resize(width, height);
        applicationTextureBack.resize(width, height);
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
        clear(0);
    }

    public void clear(int rgba) {
        clear(rgba, true, true, true);
    }

    public void clear(int rgba, boolean color, boolean depth, boolean stencil) {
        GL.SetClearColor(rgba);
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
        softFlush();

        GL.SetViewport(x, y, this.width = width, this.height = height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
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

            SVG.BeginFrame(GL.GetViewportWidth(), GL.GetViewportHeight(), 1);
            SVG.SetLineCap(strokeCap == LineCap.BUTT ? SVG_BUTT : strokeCap == LineCap.SQUARE ? SVG_SQUARE : SVG_ROUND);
            SVG.SetLineJoin(strokeJoin == LineJoin.BEVEL ? SVG_BEVEL : strokeJoin == LineJoin.MITER ? SVG_MITER : SVG_ROUND);
            SVG.SetStrokeWidth(strokeWidth);
            SVG.SetStrokeColor(color);
            SVG.SetFillColor(color);
            SVG.SetGlobalAlpha(alpha);
            SVG.TransformSet(tr2.m00, tr2.m10, tr2.m01, tr2.m11, tr2.m02, tr2.m12);

            SVG.TextSetFont(font.getInternalId());
            SVG.TextSetSize(fontSize);
            int h = textHAlign == HAlign.LEFT ? SVG_ALIGN_LEFT :
                    textHAlign == HAlign.RIGHT ? SVG_ALIGN_RIGHT : SVG_ALIGN_CENTER;
            int v = textVAlign == VAlign.MIDDLE ? SVG_ALIGN_MIDDLE :
                    textVAlign == VAlign.TOP ? SVG_ALIGN_TOP :
                            textVAlign == VAlign.BOTTOM ? SVG_ALIGN_BOTTOM : SVG_ALIGN_BASELINE;
            SVG.TextSetAlign(h | v);
        }
    }

    private void SVGFinalize() {
        SVG.EndFrame();

    }

    public void setTransform(Affine transform2D) {
        if (transform2D == null) {
            this.tr2.identity();
        } else {
            this.tr2.set(transform2D);
        }
        if (mode == 1) {
            SVG.TransformSet(tr2.m00, tr2.m10, tr2.m01, tr2.m11, tr2.m02, tr2.m12);
        }
    }

    public Affine getTransformView() {
        return trView.set(tr2);
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
        if (mode == 1) {
            SVG.SetFillColor(color);
            SVG.SetStrokeColor(color);
        }
    }

    public void setGlobalAlpha(float alpha) {
        this.alpha = alpha;
        if (mode == 1) {
            SVG.SetGlobalAlpha(alpha);
        }
    }

    public void setStrokeWidth(float size) {
        strokeWidth = Math.max(Float.MIN_VALUE, size);
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

    public void drawArc(float x, float y, float radius, float startAngle, float endAngle, boolean fill) {
        SVGMode();
        SVG.BeginPath();
        SVG.Arc(x, y, radius, startAngle, endAngle, SVG_CCW);
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
        drawRoundRect(x, y, width, height, corners, corners, corners, corners, fill);
    }

    public void drawRoundRect(float x, float y, float width, float height, float c1, float c2, float c3, float c4, boolean fill) {
        SVGMode();
        SVG.BeginPath();
        c1 = Math.max(0, Math.min(height / 2f, Math.min(width / 2f, c1)));
        c2 = Math.max(0, Math.min(height / 2f, Math.min(width / 2f, c2)));
        c3 = Math.max(0, Math.min(height / 2f, Math.min(width / 2f, c3)));
        c4 = Math.max(0, Math.min(height / 2f, Math.min(width / 2f, c4)));
        SVG.RoundedRect(x, y, width, height, c1, c2, c3, c4);
        if (fill) {
            SVG.Fill();
        } else {
            SVG.Stroke();
        }
    }

    public void drawRoundRectShadow(float x, float y, float width, float height, float corners, float blur, float alpha) {
        drawRoundRectShadow(x, y, width, height, corners, corners, corners, corners, blur, alpha);
    }

    public void drawRoundRectShadow(float x, float y, float width, float height, float c1, float c2, float c3, float c4,
                                    float blur, float alpha) {
        shadowEffect.setBlur(blur);
        shadowEffect.setAlpha(alpha);
        shadowEffect.setCorners(c1, c2, c3, c4);
        shadowEffect.setBox(x, y, width, height);
        setImageEffect(shadowEffect);
        drawImage(null, 0, 0, 1, 0, 1, 1, 0, 1, x, y, x + width, y, x + width, y + height, x, y + height);
    }

    public void drawSvg(Svg svg, float x, float y, boolean fill) {

    }

    public void setTextFont(Font font) {
        this.font = font;
        if (mode == 1) {
            SVG.TextSetFont(font.getInternalId());
        }
    }

    public void setTextSize(float size) {
        this.fontSize = size;
        if (mode == 1) {
            SVG.TextSetSize(size);
        }
    }

    public void setTextVerticalAlign(VAlign align) {
        this.textVAlign = align;
        if (mode == 1) {
            int h = textHAlign == HAlign.LEFT ? SVG_ALIGN_LEFT :
                    textHAlign == HAlign.RIGHT ? SVG_ALIGN_RIGHT : SVG_ALIGN_CENTER;
            int v = textVAlign == VAlign.MIDDLE ? SVG_ALIGN_MIDDLE :
                    textVAlign == VAlign.TOP ? SVG_ALIGN_TOP :
                            textVAlign == VAlign.BOTTOM ? SVG_ALIGN_BOTTOM : SVG_ALIGN_BASELINE;
            SVG.TextSetAlign(h | v);
        }
    }

    public float getTextWidth(String text) {
        SVGMode();
        return SVG.TextGetWidth(text);
    }

    public void setTextHorizontalAlign(HAlign align) {
        this.textHAlign = align;
        if (mode == 1) {
            int h = textHAlign == HAlign.LEFT ? SVG_ALIGN_LEFT :
                    textHAlign == HAlign.RIGHT ? SVG_ALIGN_RIGHT : SVG_ALIGN_CENTER;
            int v = textVAlign == VAlign.MIDDLE ? SVG_ALIGN_MIDDLE :
                    textVAlign == VAlign.TOP ? SVG_ALIGN_TOP :
                            textVAlign == VAlign.BOTTOM ? SVG_ALIGN_BOTTOM : SVG_ALIGN_BASELINE;
            SVG.TextSetAlign(h | v);
        }
    }

    public void drawText(String text, float x, float y) {
        SVGMode();
        SVG.BeginPath();
        SVG.DrawText(x, y, text);
    }

    public void drawTextBox(String text, float x, float y, float maxWidth) {
        SVGMode();
        SVG.BeginPath();
        SVG.DrawTextBox(x, y, maxWidth, text);
    }

    public void drawTextSlice(String text, float x, float y, float maxWidth) {
        SVGMode();
        SVG.BeginPath();

        int glyphs = SVG.TextGetLastGlyph(text, maxWidth);
        if (glyphs > 0) {
            SVG.DrawTextBox(x, y, maxWidth, text.substring(0, glyphs));
        }
    }

    //--------------------
    //    Image Mode
    //--------------------
    private void ImageMode() {
        if (mode != 2) {
            if (mode == 1) SVGFinalize();
            if (mode == 3) CoreFinalize();
            mode = 2;

        }
    }

    private void ImageFinalize() {
        flushImages();

    }

    private void flushImages() {
        if (imageBatchCount > 0) {

            int vao = GL.VertexArrayGetBound();
            if (vao != imageBatch.vertexArrayId) {
                GL.VertexArrayBind(imageBatch.vertexArrayId);
            }

            int tex = GL.TextureGetBound(TB_TEXTURE_2D);
            if (imageBatchAtlas != null && tex != imageBatchAtlas.textureId) {
                GL.TextureBind(TB_TEXTURE_2D, imageBatchAtlas.textureId);
            }

            GL.DrawArrays(VM_TRIANGLES, 0, imageBatchCount * 6, 1);

            imageBatchCount = 0;
            imageBatchAtlas = null;
        }
    }

    public void setImageEffect(Effect effect) {
        ImageMode();
        flushImages();

        effect.applyEffect(this);
    }

    public void setImageShader(Shader shader) {
        ImageMode();
        flushImages();

        int programId = shader == null ? 0 : shader.shaderProgramId;
        if  (programId != GL.ProgramGetUsed()) {
            GL.ProgramUse(programId);
        }
    }

    public void drawImage(Image image, float x, float y) {
        drawImage(image, x, y, image.getWidth(), image.getHeight());
    }

    public void drawImage(Image image, float x, float y, float width, float height) {
        drawImage(image.getAtlas(),
                image.getSrcx(), image.getSrcy(),
                image.getSrcx() + image.getWidth(), image.getSrcy(),
                image.getSrcx() + image.getWidth(), image.getSrcy() + image.getHeight(),
                image.getSrcx(), image.getSrcy() + image.getHeight(),
                x, y, x + width, y, x + width, y + height, x, y + height);
    }

    public void drawImage(Image image, Affine affine) {
        float x1 = affine.getPointX(0, 0);
        float y1 = affine.getPointY(0, 0);
        float x2 = affine.getPointX(image.getWidth(), 0);
        float y2 = affine.getPointY(image.getWidth(), 0);
        float x3 = affine.getPointX(image.getWidth(), image.getHeight());
        float y3 = affine.getPointY(image.getWidth(), image.getHeight());
        float x4 = affine.getPointX(0, image.getHeight());
        float y4 = affine.getPointY(0, image.getHeight());
        drawImage(image.getAtlas(),
                image.getSrcx(), image.getSrcy(),
                image.getSrcx() + image.getWidth(), image.getSrcy(),
                image.getSrcx() + image.getWidth(), image.getSrcy() + image.getHeight(),
                image.getSrcx(), image.getSrcy() + image.getHeight(),
                x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public void drawImage(Texture image, Affine affine) {
        float x1 = affine.getPointX(0, 0);
        float y1 = affine.getPointY(0, 0);
        float x2 = affine.getPointX(image.getWidth(), 0);
        float y2 = affine.getPointY(image.getWidth(), 0);
        float x3 = affine.getPointX(image.getWidth(), image.getHeight());
        float y3 = affine.getPointY(image.getWidth(), image.getHeight());
        float x4 = affine.getPointX(0, image.getHeight());
        float y4 = affine.getPointY(0, image.getHeight());
        drawImage(image, 0, 0,  image.getWidth(), 0,  image.getWidth(), image.getHeight(),  0, image.getHeight(),
                x1, y1, x2, y2, x3, y3, x4, y4);
    }

    public void drawImage(Texture texture,
                          float srcX1, float srcY1, float srcX2, float srcY2, float srcX3, float srcY3, float srcX4, float srcY4,
                          float dstX1, float dstY1, float dstX2, float dstY2, float dstX3, float dstY3, float dstX4, float dstY4) {
        ImageMode();

        if (imageBatchCount >= imageBatchLimit || texture != imageBatchAtlas) {
            flushImages();
        }

        imageBatchAtlas = texture;

        if (texture != null) {
            srcY1 = texture.height - srcY1;
            srcY2 = texture.height - srcY2;
            srcY3 = texture.height - srcY3;
            srcY4 = texture.height - srcY4;
        } else {
            srcY1 = 1 - srcY1;
            srcY2 = 1 - srcY2;
            srcY3 = 1 - srcY3;
            srcY4 = 1 - srcY4;
        }

        dstX1 = dstX1 * 2 / width - 1f;
        dstX2 = dstX2 * 2 / width - 1f;
        dstX3 = dstX3 * 2 / width - 1f;
        dstX4 = dstX4 * 2 / width - 1f;
        dstY1 = -((dstY1 * 2 / height) - 1f);
        dstY2 = -((dstY2 * 2 / height) - 1f);
        dstY3 = -((dstY3 * 2 / height) - 1f);
        dstY4 = -((dstY4 * 2 / height) - 1f);

        parser[ 0] = dstX1; parser[ 1] = dstY1; parser[ 2] = srcX1; parser[ 3] = srcY1;
        parser[ 4] = dstX2; parser[ 5] = dstY2; parser[ 6] = srcX2; parser[ 7] = srcY2;
        parser[ 8] = dstX3; parser[ 9] = dstY3; parser[10] = srcX3; parser[11] = srcY3;

        parser[12] = dstX3; parser[13] = dstY3; parser[14] = srcX3; parser[15] = srcY3;
        parser[16] = dstX4; parser[17] = dstY4; parser[18] = srcX4; parser[19] = srcY4;
        parser[20] = dstX1; parser[21] = dstY1; parser[22] = srcX1; parser[23] = srcY1;

        imageBatch.setSubData(parser, imageBatchCount++ * 24);
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

    public void drawVertexTriangles(VertexArray vertexArray, int first, int count) {
        CoreMode();

        int vao = GL.VertexArrayGetBound();
        if (vao != vertexArray.vertexArrayId) {
            GL.VertexArrayBind(vertexArray.vertexArrayId);
        }

        if (vertexArray.isElementMode()) {
            GL.DrawElements(VM_TRIANGLES, count, 1, DT_INT, (first * 4));
        } else {
            GL.DrawArrays(VM_TRIANGLES, first, count, 1);
        }
    }
}
