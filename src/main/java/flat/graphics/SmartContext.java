package flat.graphics;

import flat.graphics.context.*;
import flat.graphics.context.enums.BlendFunction;
import flat.graphics.context.paints.ColorPaint;
import flat.graphics.context.paints.GaussianShadow;
import flat.graphics.context.paints.ImagePattern;
import flat.graphics.image.PixelMap;
import flat.graphics.material.Material;
import flat.graphics.material.MaterialValue;
import flat.graphics.mesh.Mesh;
import flat.math.Affine;
import flat.math.Matrix4;
import flat.math.shapes.*;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

public class SmartContext {

    private final Context context;

    private int mode;
    private Surface surface;

    // -- 2D
    private Affine transform2D = new Affine();
    private ArrayList<Shape> clipShapes = new ArrayList<>();
    private ArrayList<Rectangle> clipBox = new ArrayList<>();
    private Stroke stroker;
    private float textSize;
    private float textBlur;

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
    }

    public Context getContext() {
        return context;
    }

    public int getWidth() {
        return context.getWidth();
    }

    public int getHeight() {
        return context.getHeight();
    }

    public float getDensity() {
        return context.getHeight();
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

    public void hardFlush() {
        clearMode();
        context.hardFlush();
    }

    public void clear(int color) {
        clearMode();
        context.setClearColor(color);
        context.clear(true, false, false);
    }

    public void clear(int color, double depth) {
        clearMode();
        context.setClearColor(color);
        context.setClearDepth(depth);
        context.clear(true, true, false);
    }

    public void clear(int color, double depth, int stencil) {
        clearMode();
        context.setClearColor(color);
        context.setClearDepth(depth);
        context.setClearStencil(stencil);
        context.clear(true, true, true);
    }

    // ---- Properties ---- //
    public void setSurface(Surface surface) {
        if (this.surface != surface) {
            clearMode();
            if (this.surface != null) {
                this.surface.unbind();
            }
            this.surface = surface;
            if (this.surface != null) {
                this.surface.bind(context);
            }
        }
    }

    public Surface getSurface() {
        return surface;
    }

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
            if (!this.transform2D.isIdentity()) {
                this.transform2D.identity();
                context.svgTransform(null);
            }
        } else {
            if (!this.transform2D.isEquals(transform2D)) {
                this.transform2D.set(transform2D);
                context.svgTransform(transform2D);
            }
        }
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

    /**
     * Clear the clipping
     */
    public void clearClip() {
        clipShapes.clear();
        clipBox.clear();
        context.svgClearClip(false);
    }

    public void pushClip(Shape shape) {
        Path real = new Path(shape.pathIterator(transform2D));
        Path inverse = new Path();
        inverse.moveTo(0, 0);
        inverse.lineTo(getWidth(), 0);
        inverse.lineTo(getWidth(), getHeight());
        inverse.lineTo(0, getHeight());
        inverse.closePath();
        inverse.append(real, false);

        Rectangle bounds = real.bounds();

        clipShapes.add(inverse);
        if (clipBox.size() == 0) {
            clipBox.add(bounds);
        } else {
            Rectangle currentBounds = clipBox.get(clipBox.size() - 1);
            if (currentBounds != null) {
                float inX = Math.max(currentBounds.x, bounds.x);
                float inY = Math.max(currentBounds.y, bounds.y);
                float inWidth = Math.min(currentBounds.x + currentBounds.width, bounds.x + bounds.width) - inX;
                float inHeight = Math.min(currentBounds.y + currentBounds.height, bounds.y + bounds.height) - inY;

                if (inWidth > 0 && inHeight > 0) {
                    clipBox.add(new Rectangle(inX, inY, inWidth, inHeight));
                } else {
                    clipBox.add(null);
                }
            } else {
                clipBox.add(null);
            }
        }

        updateClip();
    }

    public void popClip() {
        if (clipShapes.size() > 0) {
            clipShapes.remove(clipShapes.size() - 1);
            clipBox.remove(clipBox.size() - 1);
        }
        updateClip();
    }

    private void updateClip() {
        if (clipBox.size() == 0) {
            context.svgClearClip(false);

        } else if (clipBox.get(clipBox.size() - 1) == null) {
            context.svgClearClip(true);

        } else {
            context.svgClearClip(false);
            context.svgTransform(null);
            for (int i = 0; i < clipShapes.size(); i++) {
                context.svgClip(clipShapes.get(0));
            }
            context.svgTransform(transform2D);

        }

    }

    public void setAntialiasEnabled(boolean enabled) {
        context.svgAntialias(enabled);
    }

    public boolean isAntialiasEnabled() {
        return context.svgAntialias();
    }

    public void setColor(int color) {
        setPaint(new ColorPaint(color));
    }

    public void setPaint(Paint paint) {
        context.svgPaint(paint);
    }

    public Paint getPaint() {
        return context.svgPaint();
    }

    public void setStroker(Stroke stroker) {
        this.stroker = stroker;
        context.svgStroke(stroker);
    }

    public Stroke getStroker() {
        return stroker;
    }

    public void setTextFont(Font font) {
        context.svgTextFont(font);
        context.svgTextScale(textSize / context.svgTextFont().getSize());
    }

    public Font getTextFont() {
        return context.svgTextFont();
    }

    public void setTextSize(float size) {
        textSize = size;
        context.svgTextScale(textSize / context.svgTextFont().getSize());
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextBlur(float blur) {
        textBlur = Math.min(1, Math.max(0, blur));
        context.svgTextBlur(textBlur);
    }

    public float getTextBlur() {
        return textBlur;
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

    public void drawShape(Shape shape, boolean fill) {
        svgMode();
        if (shape instanceof Path) {
            context.svgDrawShape(shape, fill);
        }
        else if (shape instanceof Circle) drawCircle((Circle) shape, fill);
        else if (shape instanceof Rectangle) drawRect((Rectangle) shape, fill);
        else if (shape instanceof RoundRectangle) drawRoundRect((RoundRectangle) shape, fill);
        else if (shape instanceof Ellipse) drawEllipse((Ellipse) shape, fill);
        else if (shape instanceof Line) drawLine((Line) shape);
        else if (shape instanceof QuadCurve) drawQuadCurve((QuadCurve) shape);
        else if (shape instanceof CubicCurve) drawCubicCurve((CubicCurve) shape);
        else {
            context.svgDrawShape(shape, fill);
        }
    }

    public void drawCircle(float x, float y, float radius, boolean fill) {
        svgMode();
        context.svgDrawEllipse(x - radius, y - radius, radius * 2, radius * 2, fill);
    }

    public void drawCircle(Circle circle,  boolean fill) {
        svgMode();
        context.svgDrawEllipse(circle.x - circle.radius, circle.y - circle.radius, circle.radius * 2, circle.radius * 2, fill);
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

    public void drawCubicCurve(CubicCurve curve) {
        svgMode();
        context.svgDrawCubicCurve(curve.x1, curve.y1, curve.ctrlx1, curve.ctrly1,  curve.ctrlx2, curve.ctrly2, curve.x2, curve.y2);
    }

    public void drawCubicCurve(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2) {
        svgMode();
        context.svgDrawCubicCurve(x1, y1, cx1, cy1, cx2, cy2, x2, y2);
    }

    public void drawText(float x, float y, String text) {
        svgMode();
        context.svgDrawText(x, y, text, 0, 0);
    }

    public void drawText(float x, float y, Buffer text, int offset, int length) {
        svgMode();
        context.svgDrawText(x, y, text, offset, length, 0, 0);
    }

    public int drawTextSlice(float x, float y, float maxWidth, float maxHeight, String text) {
        svgMode();
        return context.svgDrawText(x, y, text, maxWidth, maxHeight);
    }

    public int drawTextSlice(float x, float y, float maxWidth, float maxHeight, Buffer text, int offset, int length) {
        svgMode();
        return context.svgDrawText(x, y, text, offset, length, maxWidth, maxHeight);
    }

    // TODO - Implement draw round rect border (use gradient values to create the effect)

    public void drawRoundRectShadow(float x, float y, float width, float height,
                                    float cTop, float cRight, float cBottom, float cLeft, float blur, float alpha) {
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
            context.svgPaint(new GaussianShadow.Builder(x, y, width, height)
                    .corners(Math.min(width / 2f, Math.min(height / 2f, cTop + blur / 2f)))
                    .blur(blur * 2)
                    .alpha(alpha)
                    .color(Color.black)
                    .build());
            drawRect(x1, y1, w, h, true);
        } else {
            final float hw = w / 2f;
            final float hh = h / 2f;
            final float xm = x1 + hw;
            final float ym = y1 + hh;
            context.svgPaint(new GaussianShadow.Builder(x, y, width, height)
                    .corners(Math.min(width / 2f, Math.min(height / 2f, cTop + blur)))
                    .blur(blur * 2)
                    .alpha(alpha)
                    .color(Color.black)
                    .build());
            drawRect(x1, y1, hw, hh, true);

            context.svgPaint(new GaussianShadow.Builder(x, y, width, height)
                    .corners(Math.min(width / 2f, Math.min(height / 2f, cRight + blur)))
                    .blur(blur * 2)
                    .alpha(alpha)
                    .color(Color.black)
                    .build());
            drawRect(xm, y1, hw, hh, true);

            context.svgPaint(new GaussianShadow.Builder(x, y, width, height)
                    .corners(Math.min(width / 2f, Math.min(height / 2f, cBottom + blur)))
                    .blur(blur * 2)
                    .alpha(alpha)
                    .color(Color.black)
                    .build());
            drawRect(xm, ym, hw, hh, true);


            context.svgPaint(new GaussianShadow.Builder(x, y, width, height)
                    .corners( Math.min(width / 2f, Math.min(height / 2f, cLeft + blur)))
                    .blur(blur * 2)
                    .alpha(alpha)
                    .color(Color.black)
                    .build());
            drawRect(x1, ym, hw, hh, true);
        }

        context.svgPaint(paint);
    }

    public void drawRoundRectShadow(RoundRectangle rect, float blur, float alpha) {
        svgMode();
        drawRoundRectShadow(rect.x, rect.y, rect.width, rect.height, rect.arcTop, rect.arcRight, rect.arcBottom, rect.arcLeft, blur, alpha);
    }

    public void drawImage(PixelMap image, float x, float y) {
        svgMode();
        drawImage(image.readTexture(context),
                0, 0, image.getWidth(), image.getHeight(),
                x, y, x + image.getWidth(), y + image.getHeight(), 0xFFFFFFFF, null);
    }

    public void drawImage(PixelMap image, float x, float y, float width, float height) {
        svgMode();
        drawImage(image.readTexture(context),
                0, 0, image.getWidth(), image.getHeight(),
                x, y, x + width, y + height, 0xFFFFFFFF, null);
    }

    public void drawImage(PixelMap image, float x, float y, float width, float height, int color) {
        svgMode();
        drawImage(image.readTexture(context),
                0, 0, image.getWidth(), image.getHeight(),
                x, y, x + width, y + height, color, null);
    }

    public void drawImage(PixelMap image, float x, float y, float width, float height, int color, Affine transform) {
        svgMode();
        drawImage(image.readTexture(context),
                0, 0, image.getWidth(), image.getHeight(),
                x, y, x + width, y + height, color, transform);
    }

    public void drawImage(PixelMap image,
                          float srcX1, float srcY1, float srcX2, float srcY2,
                          float dstX1, float dstY1, float dstX2, float dstY2,
                          int color) {
        svgMode();
        drawImage(image.readTexture(context),
                srcX1, srcY1, srcX2, srcY2,
                dstX1, dstY1, dstX2, dstY2, color, null);
    }

    public void drawImage(PixelMap image,
                          float srcX1, float srcY1, float srcX2, float srcY2,
                          float dstX1, float dstY1, float dstX2, float dstY2,
                          int color, Affine transform) {
        svgMode();
        drawImage(image.readTexture(context),
                srcX1, srcY1, srcX2, srcY2,
                dstX1, dstY1, dstX2, dstY2, color, transform);
    }

    public void drawImage(Texture2D texture,
                          float srcX1, float srcY1, float srcX2, float srcY2,
                          float dstX1, float dstY1, float dstX2, float dstY2,
                          int color, Affine transform) {
        svgMode();
        if (dstX1 > dstX2) {
            float v = dstX1;
            dstX1 = dstX2;
            dstX2 = v;

            v = srcX1;
            srcX1 = srcX2;
            srcX2 = v;
        }
        if (dstY1 > dstY2) {
            float v = dstY1;
            dstY1 = dstY2;
            dstY2 = v;

            v = srcY1;
            srcY1 = srcY2;
            srcY2 = v;
        }
        Paint paint = context.svgPaint();
        context.svgPaint(new ImagePattern.Builder(texture)
                .source(srcX1, srcY1, srcX2, srcY2)
                .destin(dstX1, dstY1, dstX2, dstY2)
                .color(color)
                .build());
        drawRect(dstX1, dstY1, dstX2 - dstX1, dstY2 - dstY1, true);
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
