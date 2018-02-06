package flat.graphics;

import flat.graphics.context.*;
import flat.graphics.context.enuns.*;
import flat.graphics.material.*;
import flat.graphics.material.image.*;
import flat.graphics.image.*;
import flat.graphics.mesh.*;
import flat.graphics.text.*;
import flat.math.*;
import flat.math.operations.*;
import flat.math.shapes.*;
import flat.math.shapes.CubicCurve;
import flat.math.shapes.QuadCurve;
import flat.math.stroke.*;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

public class SmartContext {

    private final Context context;

    private int mode;

    private Matrix4 projection3D = new Matrix4();
    private Matrix4 projection2D = new Matrix4();

    // ---- SVG ---- //
    private Paint shadowPaint = new Paint();
    private Rectangle clipArea = new Rectangle();
    private boolean clip;


    // ---- IMAGE ---- //
    private Affine transform2D = new Affine();
    private ImageTexture imageTexture = new ImageTexture();

    private ShaderProgram shader2D;
    private List<MaterialValue> matValues2D = new ArrayList<>();

    private VertexData imageBatch;
    private int imageBatchCount;
    private Texture imageBatchAtlas;
    private float[] parser = new float[24];

    // ---- MODEL ---- //
    private Matrix4 transform3D = new Matrix4();
    private ShaderProgram shader3D;
    private List<MaterialValue> matValues3D = new ArrayList<>();

    public SmartContext(Context context) {
        this.context = context;
        this.imageBatch = new VertexData();
        imageBatch.setVertexSize(1000 * 24 * 4);
        imageBatch.enableAttribute(0, AttributeType.FLOAT_VEC2, (4 * 2) + (4 * 2), 0);
        imageBatch.enableAttribute(1, AttributeType.FLOAT_VEC2, (4 * 2) + (4 * 2), (4 * 2));

        context.setBlendEnabled(true);
        context.setBlendFunction(BlendFunction.SRC_ALPHA, BlendFunction.ONE_MINUS_SRC_ALPHA, BlendFunction.ONE, BlendFunction.ONE);

        setImageMaterial(null);
    }

    public Context getContext() {
        return context;
    }

    // ---- CLEAR ---- //
    private void clearMode() {
        if (mode != 0) {
            if (mode == 1) svgEnd();
            if (mode == 2) imageEnd();
            if (mode == 3) meshEnd();
            mode = 0;
        }
    }

    public void softFlush() {
        clearMode();
        context.softFlush();
    }

    public void clear(int color) {
        clearMode();
        context.setClearColor(color);
        context.clear(true, true, true);
    }

    public void clear(int color, double depth, int stencil) {
        clearMode();
        context.setClearColor(color);
        context.setClearDepth(depth);
        context.setClearStencil(stencil);
        context.clear(true, true, true);
    }

    // ---- Properties ---- //

    public void setView(int x, int y, int width, int height) {
        clearMode();
        context.setViewPort(x, y, width, height);
        projection2D.setToOrtho(x, width, height, y, 0, 1);
    }

    public Rectangle getView() {
        return new Rectangle(context.getViewX(), context.getViewY(), context.getViewWidth(), context.getViewHeight());
    }

    public void setProjection(Matrix4 projection) {
        if (projection == null) {
            this.projection3D.identity();
        } else {
            this.projection3D.set(projection);
        }
    }

    public Matrix4 getProjection() {
        return new Matrix4(projection3D);
    }

    public void setTransform2D(Affine transform2D) {
        if (transform2D == null) {
            this.transform2D.identity();
        } else {
            this.transform2D.set(transform2D);
        }
        context.svgTransform(transform2D);
    }

    public Affine getTransform2D() {
        return transform2D;
    }

    public void setTransform3D(Matrix4 transform3D) {
        if (transform3D == null) {
            this.transform3D.identity();
        } else {
            this.transform3D.set(transform3D);
        }
    }

    public Matrix4 getTransform3D() {
        return new Matrix4(this.transform3D);
    }

    public void setClip(float x, float y, float width, float height) {
        clipArea.set(x,y,width,height);
    }

    public void setClip(Rectangle rectangle) {
        clipArea.set(rectangle);
    }

    public void intersectClip(float x, float y, float width, float height) {
        clipArea.intersect(x, y, width, height);
    }

    public void intersectClip(Rectangle rectangle) {
        clipArea.intersect(rectangle);
    }

    public Rectangle getClip() {
        return new Rectangle(clipArea);
    }

    public void setAntialiasEnabled(boolean enabled) {
        context.svgAntialias(enabled);
    }

    public boolean isAntialiasEnabled() {
        return context.svgAntialias();
    }

    public void setColor(int color) {
        context.svgColor(color);
    }

    public int getColor() {
        return context.svgColor();
    }

    public void setPaint(Paint paint) {
        context.svgPaint(paint);
    }

    public Paint getPaint() {
        return context.svgPaint();
    }

    public void setAlpha(float alpha) {
        context.svgAlpha(alpha);
    }

    public float getAlpha() {
        return context.svgAlpha();
    }

    public void setStroker(Stroker stroker) {
        context.svgStrokeWidth(stroker.getWidth());
        context.svgLineCap(stroker.getCap());
        context.svgLineJoin(stroker.getJoin());
        context.svgMiterLimit(stroker.getMiterLimit());
    }

    public Stroker getStroker() {
        return new Stroker(context.svgStrokeWidth(), context.svgLineCap(), context.svgLineJoin(), context.svgMiterLimit());
    }

    public void setTextFont(Font font) {
        context.svgTextFont(font);
    }

    public Font getTextFont() {
        return context.svgTextFont();
    }

    public void setTextSize(float size) {
        context.svgTextSize(size);
    }

    public float getTextSize() {
        return context.svgTextSize();
    }

    public void setTextVerticalAlign(Align.Vertical align) {
        context.svgTextVerticalAlign(align);
    }

    public Align.Vertical getTextVerticalAlign() {
        return context.svgTextVerticalAlign();
    }

    public void setTextHorizontalAlign(Align.Horizontal align) {
        context.svgTextHorizontalAlign(align);
    }

    public Align.Horizontal getTextHorizontalAlign() {
        return context.svgTextHorizontalAlign();
    }

    // ---- SVG ---- //

    private void svgMode() {
        if (mode != 1) {
            if (mode == 2) imageEnd();
            if (mode == 3) meshEnd();
            mode = 1;
        }
    }

    private void svgEnd() {
        context.softFlush();
    }

    public void drawShape(Shape path, boolean fill) {
        svgMode();
        context.svgDrawShape(path, fill);
    }

    public void drawEllipse(Ellipse ellipse, boolean fill) {
        svgMode();
        context.svgDrawEllipse(ellipse.x, ellipse.y, ellipse.width, ellipse.height, fill);
    }

    public void drawEllipse(float x, float y, float width, float height, boolean fill) {
        svgMode();
        context.svgDrawEllipse(x, y, width, height, fill);
    }

    public void drawRect(Rectangle rect, boolean fill) {
        svgMode();
        context.svgDrawRect(rect.x, rect.y, rect.width, rect.height, fill);
    }

    public void drawRect(float x, float y, float width, float height, boolean fill) {
        svgMode();
        context.svgDrawRect(x, y, width, height, fill);
    }

    public void drawRoundRect(RoundRectangle rect, boolean fill) {
        svgMode();
        context.svgDrawRoundRect(rect.x, rect.y, rect.width, rect.height, rect.arcTop, rect.arcRight, rect.arcBottom, rect.arcLeft, fill);
    }

    public void drawRoundRect(float x, float y, float width, float height, float cTop, float cRight, float cBottom, float cLeft, boolean fill) {
        svgMode();
        context.svgDrawRoundRect(x, y, width, height, cTop, cRight, cBottom, cLeft, fill);
    }

    public void drawArc(Arc arc, boolean fill) {
        svgMode();
        context.svgDrawShape(arc, fill);
    }

    public void drawArc(float x, float y, float radius, float angleA, float angleB, boolean fill) {
        svgMode();
        context.svgDrawArc(x, y, radius, angleA, angleB, fill);
    }

    public void drawLine(Line line) {
        svgMode();
        context.svgDrawLine(line.x1, line.y1, line.x2, line.y2);
    }

    public void drawLine(float x1, float y1, float x2, float y2) {
        svgMode();
        context.svgDrawLine(x1, y1, x2, y2);
    }

    public void drawQuadCurve(QuadCurve curve) {
        svgMode();
        context.svgDrawQuadCurve(curve.x1, curve.y1, curve.x2, curve.y2, curve.ctrlx, curve.ctrly);
    }

    public void drawQuadCurve(float x1, float y1, float x2, float y2, float cx, float cy) {
        svgMode();
        context.svgDrawQuadCurve(x1, y1, x2, y2, cx, cy);
    }

    public void drawBezierCurve(CubicCurve curve) {
        svgMode();
        context.svgDrawBezierCurve(curve.x1, curve.y1, curve.x2, curve.y2, curve.ctrlx1, curve.ctrly1,  curve.ctrlx2, curve.ctrly2);
    }

    public void drawBezierCurve(float x1, float y1, float x2, float y2, float cx1, float cy1, float cx2, float cy2) {
        svgMode();
        context.svgDrawBezierCurve(x1, y1, x2, y2, cx1, cy1, cx2, cy2);
    }

    public void drawText(float x, float y, String text) {
        svgMode();
        context.svgDrawText(x, y, text);
    }

    public void drawText(float x, float y, Buffer text, int offset, int length) {
        svgMode();
        context.svgDrawText(x, y, text, offset, length);
    }

    public void drawTextBox(float x, float y, float maxWidth, String text) {
        svgMode();
        context.svgDrawTextBox(x, y, maxWidth, text);
    }

    public void drawTextBox(float x, float y, float maxWidth, Buffer text, int offset, int length) {
        svgMode();
        context.svgDrawTextBox(x, y, maxWidth, text, offset, length);
    }

    public void drawTextSlice(float x, float y, float maxWidth, String text) {
        svgMode();
        context.svgDrawTextSlice(x, y, maxWidth, text);
    }

    public void drawTextSlice(float x, float y, float maxWidth, Buffer text, int offset, int length) {
        svgMode();
        context.svgDrawTextSlice(x, y, maxWidth, text, offset, length);
    }

    public void drawRoundRectShadow(float x, float y, float width, float height, float cTop, float cRight, float cBottom, float cLeft,
                                    float blur, float alpha) {
        if (blur > Math.max(width, height)) {
            alpha *= Math.max(width, height) / blur;
        }
        Paint prev = context.svgIsColorMode() ? null : context.svgPaint();
        final float x1 = x - blur;
        final float y1 = y - blur;
        final float w = width + blur * 2;
        final float h = height + blur * 2;

        shadowPaint.setInterpolation(Paint.Interpolation.FADE);
        if (cTop == cRight && cBottom == cLeft && cLeft == cTop) {
            shadowPaint.setBoxShadow(x, y, x + width, y + height,
                    Math.min(width / 2f, Math.min(height / 2f, cTop + blur / 2f)), blur * 2, alpha);
            context.svgPaint(shadowPaint);
            drawRect(x1, y1, w, h, true);
        } else {
            final float hw = w / 2f;
            final float hh = h / 2f;
            final float xm = x1 + hw;
            final float ym = y1 + hh;
            shadowPaint.setBoxShadow(x, y, x + width, y + height,
                    Math.min(width / 2f, Math.min(height / 2f, cTop + blur)), blur, alpha);
            context.svgPaint(shadowPaint);
            drawRect(x1, y1, hw, hh, true);

            shadowPaint.setBoxShadow(x, y, x + width, y + height,
                    Math.min(width / 2f, Math.min(height / 2f, cRight + blur)), blur, alpha);
            context.svgPaint(shadowPaint);
            drawRect(xm, y1, hw, hh, true);

            shadowPaint.setBoxShadow(x, y, x + width, y + height,
                    Math.min(width / 2f, Math.min(height / 2f, cBottom + blur)), blur, alpha);
            context.svgPaint(shadowPaint);
            drawRect(xm, ym, hw, hh, true);

            shadowPaint.setBoxShadow(x, y, x + width, y + height,
                    Math.min(width / 2f, Math.min(height / 2f, cLeft + blur)), blur, alpha);
            context.svgPaint(shadowPaint);
            drawRect(x1, ym, hw, hh, true);
        }

        context.svgPaint(prev);
    }

    public void drawRoundRectShadow(RoundRectangle rect, float blur, float alpha) {
        imageMode();
        drawRoundRectShadow(rect.x, rect.y, rect.width, rect.height, rect.arcTop, rect.arcRight, rect.arcBottom, rect.arcLeft, blur, alpha);
    }

    // ---- IMAGE ---- //

    private void imageMode() {
        if (mode != 2) {
            if (mode == 1) svgEnd();
            if (mode == 3) meshEnd();
            mode = 2;
        }
    }

    private void imageEnd() {
        imageFlush();
    }

    private void imageFlush() {
        if (imageBatchCount > 0) {

            shader2D.begin();
            shader2D.set("view", new float[] {context.getViewWidth(), context.getViewHeight()});
            shader2D.set("prj2D", projection2D);
            shader2D.set("src", 0);
            for (MaterialValue value : matValues2D) {
                shader2D.set(value.name, value.value);
            }

            if (imageBatchAtlas == null) {
                context.clearBindTexture(0);
            } else {
                imageBatchAtlas.begin(0);
            }

            VertexArray vertexArray = imageBatch.getVertexArray();
            vertexArray.begin();
            context.drawArray(VertexMode.TRIANGLES, 0, imageBatchCount * 6, 1);
            vertexArray.end();

            imageBatchCount = 0;
        }
    }

    public void setImageMaterial(ImageMaterial imageMaterial) {
        imageFlush();

        if (imageMaterial == null) {
            imageMaterial = imageTexture;
        }

        shader2D = imageMaterial.getShader();
        matValues2D.clear();
        matValues2D.addAll(imageMaterial.getValues());
    }

    public void drawImage(Image image) {
        imageMode();
        drawImage(image, transform2D);
    }

    public void drawImage(Image image, Affine transform) {
        imageMode();
        drawImage(image, transform, 0, 0, image.getWidth(), image.getHeight());
    }

    public void drawImage(Image image, Affine transform, float x, float y, float width, float height) {
        imageMode();
        drawImage(image.getAtlas(), image.getSrcx(), image.getSrcy(),
                image.getSrcx() + image.getWidth(), image.getSrcy() + image.getHeight(),
                x, y, x + width, y + height, transform);
    }

    public void drawImage(Texture texture,
                          float srcX1, float srcY1, float srcX2, float srcY2,
                          float dstX1, float dstY1, float dstX2, float dstY2,
                          Affine tr2) {
        imageMode();
        if (texture != imageBatchAtlas || imageBatchCount >= 1000) {
            imageFlush();
        }

        imageBatchAtlas = texture;
        if (tr2 == null) {
            tr2 = transform2D;
        }

        parser[ 0] = tr2.pointX(dstX1, dstY1); parser[ 1] = tr2.pointY(dstX1, dstY1); parser[ 2] = srcX1; parser[ 3] = srcY1;
        parser[ 4] = tr2.pointX(dstX2, dstY1); parser[ 5] = tr2.pointY(dstX2, dstY1); parser[ 6] = srcX2; parser[ 7] = srcY1;
        parser[ 8] = tr2.pointX(dstX2, dstY2); parser[ 9] = tr2.pointY(dstX2, dstY2); parser[10] = srcX2; parser[11] = srcY2;

        parser[12] = tr2.pointX(dstX2, dstY2); parser[13] = tr2.pointY(dstX2, dstY2); parser[14] = srcX2; parser[15] = srcY2;
        parser[16] = tr2.pointX(dstX1, dstY2); parser[17] = tr2.pointY(dstX1, dstY2); parser[18] = srcX1; parser[19] = srcY2;
        parser[20] = tr2.pointX(dstX1, dstY1); parser[21] = tr2.pointY(dstX1, dstY1); parser[22] = srcX1; parser[23] = srcY1;

        imageBatch.setVertices(imageBatchCount * 24 * 4, parser);

        imageBatchCount ++;
    }

    // ---- MESH ---- //

    private void meshMode() {
        if (mode != 3) {
            if (mode == 1) svgEnd();
            if (mode == 2) imageEnd();
            mode = 3;
        }
    }

    private void meshEnd() {

    }

    public void setMeshMaterial() {

    }

    public void drawMesh(Mesh mesh) {
        meshMode();

    }

    public void drawMesh(Mesh mesh, Matrix4 transform) {
        meshMode();

    }

    public void drawMesh(Mesh mesh, Matrix4 transform, int anim, float frame) {
        meshMode();

    }

    public void drawMesh(Mesh mesh, Matrix4[] transforms, int offset, int length) {
        meshMode();

    }
}
