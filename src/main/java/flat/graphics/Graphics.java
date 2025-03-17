package flat.graphics;

import flat.backend.GL;
import flat.exception.FlatException;
import flat.graphics.context.*;
import flat.graphics.context.enums.*;
import flat.graphics.context.paints.ColorPaint;
import flat.graphics.context.paints.GaussianShadow;
import flat.graphics.context.paints.ImagePattern;
import flat.math.Affine;
import flat.math.Vector2;
import flat.math.shapes.*;

import java.nio.Buffer;
import java.util.ArrayList;

public class Graphics {

    private final Context context;

    private Surface surface;

    private final Affine transform2D = new Affine();
    private final ArrayList<Shape> clipShapes = new ArrayList<>();
    private final ArrayList<Rectangle> clipBox = new ArrayList<>();
    private Stroke stroke;
    private float textSize;

    // Custom Draw
    private Shader vertex;
    private Shader fragment;
    private VertexArray ver;
    private BufferObject vbo;
    private BufferObject ebo;

    public Graphics(Context context) {
        this.context = context;

        context.setBlendEnabled(true);
        context.setBlendFunction(BlendFunction.SRC_ALPHA, BlendFunction.ONE_MINUS_SRC_ALPHA, BlendFunction.ONE, BlendFunction.ONE);

        stroke = getStroke();

        textSize = 24;
        context.svgTextScale(textSize);

        vertex = new Shader(context, ShaderType.Vertex);
        vertex.setSource(
                """
                #version 330 core
                layout (location = 0) in vec2 iPos;
                layout (location = 1) in vec2 iTex;
                uniform vec2 view;
                out vec2 oPos;
                out vec2 oTex;
                void main() {
                    oPos = iPos;
                    oTex = iTex;
                	gl_Position = vec4(iPos.x * 2.0 / view.x - 1.0, 1.0 - iPos.y * 2.0 / view.y, 0, 1);
                }
                """
        );
        vertex.compile();
        if (!vertex.compile()) {
            throw new FlatException("The default image vertex shader fail to compile : " + vertex.getLog());
        }

        fragment = new Shader(context, ShaderType.Fragment);
        fragment.setSource(
                """
                #version 330 core
                out vec4 FragColor;
                in vec2 oPos;
                in vec2 oTex;
                
                vec4 fragment(vec2 pos, vec2 uv);
                
                void main() {
                    FragColor = fragment(oPos, oTex);
                }
                """
        );
        fragment.compile();
        if (!fragment.compile()) {
            throw new FlatException("The default image fragment shader fail to compile : " + fragment.getLog());
        }
    }

    public Context getContext() {
        return context;
    }

    public int getWidth() {
        return surface == null ? context.getWidth() : surface.getWidth();
    }

    public int getHeight() {
        return surface == null ? context.getHeight() : surface.getHeight();
    }

    public float getDensity() {
        return context.getHeight();
    }

    public void clear(int color) {
        context.setClearColor(color);
        context.clear(true, false, false);
    }

    public void clear(int color, double depth) {
        context.setClearColor(color);
        context.setClearDepth(depth);
        context.clear(true, true, false);
    }

    public void clear(int color, double depth, int stencil) {
        context.setClearColor(color);
        context.setClearDepth(depth);
        context.setClearStencil(stencil);
        context.clear(true, true, true);
    }

    public void setSurface(Surface surface) {
        if (this.surface != surface) {
            if (this.surface != null) {
                this.surface.unbind();
            }
            this.surface = surface;
            if (this.surface != null) {
                this.surface.bind(context);
            }
            setView(0, 0, getWidth(), getHeight());
        }
    }

    public Surface getSurface() {
        return surface;
    }

    public void setView(int x, int y, int width, int height) {
        context.setViewPort(x, y, width, height);
    }

    public Rectangle getView() {
        return new Rectangle(context.getViewX(), context.getViewY(), context.getViewWidth(), context.getViewHeight());
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
        if (!clipShapes.isEmpty()) {
            clipShapes.remove(clipShapes.size() - 1);
            clipBox.remove(clipBox.size() - 1);
        }
        updateClip();
    }

    private void updateClip() {
        if (clipBox.isEmpty()) {
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

    public void setStroke(Stroke stroke) {
        this.stroke = stroke;
        context.svgStroke(stroke);
    }

    public Stroke getStroke() {
        return stroke;
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
        context.svgTextBlur(Math.min(1, Math.max(0, blur)));
    }

    public float getTextBlur() {
        return context.svgTextBlur();
    }

    public void drawShape(Shape shape, boolean fill) {
        if (shape instanceof Path path) drawPath(path, fill, true);
        else if (shape instanceof RoundRectangle roundRect) drawRoundRect(roundRect, fill);
        else if (shape instanceof Circle circle) drawCircle(circle, fill);
        else if (shape instanceof Rectangle rect) drawRect(rect, fill);
        else if (shape instanceof Ellipse ellipse) drawEllipse(ellipse, fill);
        else if (shape instanceof Line line) drawLine(line);
        else if (shape instanceof QuadCurve quad) drawQuadCurve(quad);
        else if (shape instanceof CubicCurve cubic) drawCubicCurve(cubic);
        else context.svgDrawShape(shape, fill);
    }

    public void drawPath(Path path, boolean fill, boolean optimize) {
        if (optimize && fill && path.length() > 3000) {
            context.svgDrawShapeOptimized(path);
        } else {
            context.svgDrawShape(path, fill);
        }
    }

    public void drawCircle(float x, float y, float radius, boolean fill) {
        context.svgDrawEllipse(x - radius, y - radius, radius * 2, radius * 2, fill);
    }

    public void drawCircle(Circle circle,  boolean fill) {
        context.svgDrawEllipse(circle.x - circle.radius, circle.y - circle.radius, circle.radius * 2, circle.radius * 2, fill);
    }

    public void drawEllipse(Ellipse ellipse, boolean fill) {
        context.svgDrawEllipse(ellipse.x, ellipse.y, ellipse.width, ellipse.height, fill);
    }

    public void drawEllipse(float x, float y, float width, float height, boolean fill) {
        context.svgDrawEllipse(x, y, width, height, fill);
    }

    public void drawRect(Rectangle rect, boolean fill) {
        context.svgDrawRect(rect.x, rect.y, rect.width, rect.height, fill);
    }

    public void drawRect(float x, float y, float width, float height, boolean fill) {
        context.svgDrawRect(x, y, width, height, fill);
    }

    public void drawRoundRect(RoundRectangle rect, boolean fill) {
        context.svgDrawRoundRect(rect.x, rect.y, rect.width, rect.height, rect.arcTop, rect.arcRight, rect.arcBottom, rect.arcLeft, fill);
    }

    public void drawRoundRect(float x, float y, float width, float height, float cTop, float cRight, float cBottom, float cLeft, boolean fill) {
        context.svgDrawRoundRect(x, y, width, height, cTop, cRight, cBottom, cLeft, fill);
    }

    public void drawLine(Line line) {
        context.svgDrawLine(line.x1, line.y1, line.x2, line.y2);
    }

    public void drawLine(float x1, float y1, float x2, float y2) {
        context.svgDrawLine(x1, y1, x2, y2);
    }

    public void drawQuadCurve(QuadCurve curve) {
        context.svgDrawQuadCurve(curve.x1, curve.y1, curve.ctrlx, curve.ctrly, curve.x2, curve.y2);
    }

    public void drawQuadCurve(float x1, float y1, float cx, float cy, float x2, float y2) {
        context.svgDrawQuadCurve(x1, y1, cx, cy, x2, y2);
    }

    public void drawCubicCurve(CubicCurve curve) {
        context.svgDrawCubicCurve(curve.x1, curve.y1, curve.ctrlx1, curve.ctrly1,  curve.ctrlx2, curve.ctrly2, curve.x2, curve.y2);
    }

    public void drawCubicCurve(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2) {
        context.svgDrawCubicCurve(x1, y1, cx1, cy1, cx2, cy2, x2, y2);
    }

    public void drawText(float x, float y, String text) {
        context.svgDrawText(x, y, text, 0, 0);
    }

    public void drawText(float x, float y, Buffer text, int offset, int length) {
        context.svgDrawText(x, y, text, offset, length, 0, 0);
    }

    public void drawTextSlice(float x, float y, float maxWidth, float maxHeight, String text) {
        context.svgDrawText(x, y, text, maxWidth, maxHeight);
    }

    public void drawTextSlice(float x, float y, float maxWidth, float maxHeight, Buffer text, int offset, int length) {
        context.svgDrawText(x, y, text, offset, length, maxWidth, maxHeight);
    }

    public void drawLinearShadowDown(float x, float y, float width, float height, float alpha) {
        Paint paint = context.svgPaint();
        context.svgPaint(new GaussianShadow.Builder(x - height, y - height * 1.5f, width + height * 2f, height * 2f)
                .corners(0)
                .blur(height)
                .alpha(alpha)
                .color(Color.black)
                .build());
        drawRect(x, y, width, height, true);
    }

    public void drawLinearShadowUp(float x, float y, float width, float height, float alpha) {
        Paint paint = context.svgPaint();
        context.svgPaint(new GaussianShadow.Builder(x - height, y + height * 0.5f, width + height * 2f, height * 2f)
                .corners(0)
                .blur(height)
                .alpha(alpha)
                .color(Color.black)
                .build());
        drawRect(x, y, width, height, true);
    }

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
    }

    public void drawRoundRectShadow(RoundRectangle rect, float blur, float alpha) {
        drawRoundRectShadow(rect.x, rect.y, rect.width, rect.height, rect.arcTop, rect.arcRight, rect.arcBottom, rect.arcLeft, blur, alpha);
    }

    public void drawImage(ImageTexture image, float x, float y) {
        Texture2D tex = image.getTexture(context);
        drawImage(tex,
                0, 0, tex.getWidth(), tex.getHeight(),
                x, y, x + tex.getWidth(), y + tex.getHeight(), 0xFFFFFFFF, null);
    }

    public void drawImage(ImageTexture image, float x, float y, float width, float height) {
        Texture2D tex = image.getTexture(context);
        drawImage(tex,
                0, 0, tex.getWidth(), tex.getHeight(),
                x, y, x + width, y + height, 0xFFFFFFFF, null);
    }

    public void drawImage(ImageTexture image, float x, float y, float width, float height, int color) {
        Texture2D tex = image.getTexture(context);
        drawImage(tex,
                0, 0, tex.getWidth(), tex.getHeight(),
                x, y, x + width, y + height, color, null);
    }

    public void drawImage(ImageTexture image, float x, float y, float width, float height, int color, Affine transform) {
        Texture2D tex = image.getTexture(context);
        drawImage(tex,
                0, 0, tex.getWidth(), tex.getHeight(),
                x, y, x + width, y + height, color, transform);
    }

    public void drawImage(ImageTexture image,
                          float srcX1, float srcY1, float srcX2, float srcY2,
                          float dstX1, float dstY1, float dstX2, float dstY2,
                          int color) {
        Texture2D tex = image.getTexture(context);
        drawImage(tex,
                srcX1, srcY1, srcX2, srcY2,
                dstX1, dstY1, dstX2, dstY2, color, null);
    }

    public void drawImage(ImageTexture image,
                          float srcX1, float srcY1, float srcX2, float srcY2,
                          float dstX1, float dstY1, float dstX2, float dstY2,
                          int color, Affine transform) {
        Texture2D tex = image.getTexture(context);
        drawImage(tex,
                srcX1, srcY1, srcX2, srcY2,
                dstX1, dstY1, dstX2, dstY2, color, transform);
    }

    public void drawImage(Texture2D texture,
                          float srcX1, float srcY1, float srcX2, float srcY2,
                          float dstX1, float dstY1, float dstX2, float dstY2,
                          int color, Affine transform) {
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
                .transform(transform)
                .build());
        drawRect(dstX1, dstY1, dstX2 - dstX1, dstY2 - dstY1, true);
        context.svgPaint(paint);
    }

    public void drawImageCustomShader(ShaderProgram program) {
        if (ver == null) {
            ver = new VertexArray(context);
            ver.begin();

            ebo = new BufferObject(context);
            ebo.begin(BufferType.Element);
            ebo.setSize(6 * 4, UsageType.STATIC_DRAW);
            ebo.setData(0, new int[]{0, 1, 2, 0, 2, 3}, 0, 6);

            vbo = new BufferObject(context);
            vbo.begin(BufferType.Array);
            vbo.setSize(16 * 4, UsageType.STATIC_DRAW);
            vbo.setData(0, new float[]{0, 0, 0, 0, 100, 0,  1,  0, 100,  100,  1,  1, 0, 100, 0,  1}, 0, 16);

            ver.setAttributePointer(0, 2, AttributeType.FLOAT, false, 4 * 4, 0);
            ver.setAttributeEnabled(0, true);
            ver.setAttributePointer(1, 2, AttributeType.FLOAT, false, 4 * 4, 2 * 4);
            ver.setAttributeEnabled(1, true);

            vbo.end();
            ver.end();
        }

        program.begin();
        program.set("view", new Vector2(getWidth(), getHeight()));
        ver.begin();
        context.drawElements(VertexMode.TRIANGLES, 0, 6, 1);
        ver.end();
        program.end();
    }

    public ShaderProgram createImageRenderShader(String compatibleFragment) {

        Shader compatible = new Shader(context, ShaderType.Fragment);
        compatible.setSource(compatibleFragment);
        compatible.compile();
        if (!compatible.compile()) {
            throw new FlatException("The Fragment is not compatible. Compile returned the error: " + compatible.getLog());
        }

        ShaderProgram program = new ShaderProgram(context, vertex, fragment, compatible);
        if (!program.link()) {
            throw new FlatException("The Fragment is not compatible. Compile returned the error: " + compatible.getLog());
        }

        return program;
    }
}
