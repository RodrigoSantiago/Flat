package flat.graphics;

import flat.graphics.context.*;
import flat.graphics.context.enuns.*;
import flat.graphics.material.*;
import flat.graphics.image.*;
import flat.graphics.mesh.*;
import flat.graphics.text.*;
import flat.math.*;
import flat.math.operations.Area;
import flat.math.shapes.*;
import flat.math.shapes.CubicCurve;
import flat.math.shapes.QuadCurve;
import flat.math.stroke.BasicStroke;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

public class SmartContext {

    private final Context context;

    private int mode;

    // -- 2D
    private Affine transform2D = new Affine();
    private Area clipArea = new Area();
    private BasicStroke stroker;

    // -- 3D
    private Matrix4 projection3D = new Matrix4();
    private Matrix4 transform3D = new Matrix4();
    private ShaderProgram shader3D;
    private List<MaterialValue> matValues3D = new ArrayList<>();

    public SmartContext(Context context) {
        this.context = context;

        context.setBlendEnabled(true);
        context.setBlendFunction(BlendFunction.SRC_ALPHA, BlendFunction.ONE_MINUS_SRC_ALPHA, BlendFunction.ONE, BlendFunction.ONE);

        stroker = getStroker();
        setMeshMaterial(null);
    }

    public Context getContext() {
        return context;
    }

    // ---- CLEAR ---- //
    private void clearMode() {
        if (mode != 0) {
            if (mode == 1) svgEnd();
            if (mode == 2) meshEnd();
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
        return context.svgTransform();
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

    public void setClip(Shape shape) {
        clipArea.set(shape);
    }

    public void intersectClip(Shape shape) {
        clipArea.intersect(new Area(shape));
    }

    public Area getClip() {
        return new Area(clipArea);
    }

    public void setAntialiasEnabled(boolean enabled) {
        context.svgAntialias(enabled);
    }

    public boolean isAntialiasEnabled() {
        return context.svgAntialias();
    }

    public void setColor(int color) {
        setPaint(Paint.color(color));
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

    public void setStroker(BasicStroke stroker) {
        this.stroker = stroker;
        context.svgStrokeWidth(stroker.getLineWidth());
        context.svgLineCap(LineCap.values()[stroker.getEndCap()]);
        context.svgLineJoin(LineJoin.values()[stroker.getLineJoin()]);
        context.svgMiterLimit(stroker.getMiterLimit());
    }

    public BasicStroke getStroker() {
        return stroker;
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

    public void setTextLineHeight(float height) {
        context.svgTextLineHeight(height);
    }

    public float getTextLineHeight() {
        return context.svgTextLineHeight();
    }

    // ---- CANVAS ---- //

    // ---- SVG ---- //

    private void svgMode() {
        if (mode != 1) {
            if (mode == 2) meshEnd();
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
        context.svgDrawQuadCurve(curve.x1, curve.y1, curve.ctrlx, curve.ctrly, curve.x2, curve.y2);
    }

    public void drawQuadCurve(float x1, float y1, float cx, float cy, float x2, float y2) {
        svgMode();
        context.svgDrawQuadCurve(x1, y1, cx, cy, x2, y2);
    }

    public void drawBezierCurve(CubicCurve curve) {
        svgMode();
        context.svgDrawBezierCurve(curve.x1, curve.y1, curve.ctrlx1, curve.ctrly1,  curve.ctrlx2, curve.ctrly2, curve.x2, curve.y2);
    }

    public void drawBezierCurve(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2) {
        svgMode();
        context.svgDrawBezierCurve(x1, y1, cx1, cy1, cx2, cy2, x2, y2);
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
        Paint paint = context.svgPaint();
        if (blur > Math.max(width, height)) {
            alpha *= Math.max(width, height) / blur;
            if (alpha < 0.01f) return;
        }
        final float x1 = x - blur;
        final float y1 = y - blur;
        final float w = width + blur * 2;
        final float h = height + blur * 2;

        if (cTop == cRight && cBottom == cLeft && cLeft == cTop) {
            context.svgPaint(Paint.shadow(x, y, x + width, y + height,
                    Math.min(width / 2f, Math.min(height / 2f, cTop + blur / 2f)), blur * 2, alpha, transform2D));
            drawRect(x1, y1, w, h, true);
        } else {
            final float hw = w / 2f;
            final float hh = h / 2f;
            final float xm = x1 + hw;
            final float ym = y1 + hh;
            context.svgPaint(Paint.shadow(x, y, x + width, y + height,
                    Math.min(width / 2f, Math.min(height / 2f, cTop + blur)), blur * 2, alpha, transform2D));
            drawRect(x1, y1, hw, hh, true);

            context.svgPaint(Paint.shadow(x, y, x + width, y + height,
                    Math.min(width / 2f, Math.min(height / 2f, cRight + blur)), blur * 2, alpha, transform2D));
            drawRect(xm, y1, hw, hh, true);

            context.svgPaint(Paint.shadow(x, y, x + width, y + height,
                    Math.min(width / 2f, Math.min(height / 2f, cBottom + blur)), blur * 2, alpha, transform2D));
            drawRect(xm, ym, hw, hh, true);

            context.svgPaint(Paint.shadow(x, y, x + width, y + height,
                    Math.min(width / 2f, Math.min(height / 2f, cLeft + blur)), blur * 2, alpha, transform2D));
            drawRect(x1, ym, hw, hh, true);
        }

        context.svgPaint(paint);
    }

    public void drawRoundRectShadow(RoundRectangle rect, float blur, float alpha) {
        svgMode();
        drawRoundRectShadow(rect.x, rect.y, rect.width, rect.height, rect.arcTop, rect.arcRight, rect.arcBottom, rect.arcLeft, blur, alpha);
    }

    public void drawImage(ImageRaster image) {
        svgMode();
        drawImage(image, null);
    }

    public void drawImage(ImageRaster image, Affine transform) {
        svgMode();
        drawImage(image, transform, 0, 0, image.getWidth(), image.getHeight());
    }

    public void drawImage(ImageRaster image, Affine transform, float x, float y, float width, float height) {
        svgMode();
        drawImage(
                image.getAtlas(),
                image.getSrcx(),
                image.getSrcy(),
                image.getSrcx() + image.getWidth(),
                image.getSrcy() + image.getHeight(),
                x, y, x + width, y + height, transform);
    }

    public void drawImage(Texture2D texture,
                          float srcX1, float srcY1, float srcX2, float srcY2,
                          float dstX1, float dstY1, float dstX2, float dstY2,
                          Affine transform2D) {
        svgMode();
        Paint paint = context.svgPaint();
        if (transform2D == null) transform2D = this.transform2D;
        context.svgPaint(Paint.image(srcX1, srcY1, srcX2, srcY2, dstX1, dstY1, dstX2, dstY2, texture, transform2D));
        drawRect(Math.min(dstX1, dstX2), Math.min(dstY1, dstY2), Math.abs(dstX2 - dstX1), Math.abs(dstY2 - dstY1), true);
        context.svgPaint(paint);
    }

    // ---- MESH ---- //

    private void meshMode() {
        if (mode != 2) {
            if (mode == 1) svgEnd();
            mode = 2;
        }
    }

    private void meshEnd() {

    }

    public void setMeshMaterial(Material material) {
        meshMode();

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
