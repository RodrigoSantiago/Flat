package flat.graphics;

import flat.exception.FlatException;
import flat.graphics.context.*;
import flat.graphics.context.enums.*;
import flat.graphics.context.paints.ColorPaint;
import flat.graphics.context.paints.GaussianShadow;
import flat.graphics.context.paints.ImagePattern;
import flat.graphics.image.PixelMap;
import flat.math.Affine;
import flat.math.Vector2;
import flat.math.shapes.*;
import flat.math.stroke.BasicStroke;

import java.nio.Buffer;
import java.util.ArrayList;

public class Graphics {

    private final Context context;
    private Surface surface;

    private final Affine transform2D = new Affine();
    private final ClipState clipState = new ClipState();
    private final ClipState surfaceClipState = new ClipState();
    private Stroke stroke;
    private float textSize;
    private final Rectangle noClip = new Rectangle();

    // Custom Draw
    private Shader vertex;
    private Shader fragment;
    private VertexArray ver;
    private BufferObject vbo;
    private BufferObject ebo;

    public Graphics(Context context) {
        this.context = context;

        context.setBlendEnabled(true);
        context.setBlendFunction(BlendFunction.SRC_ALPHA, BlendFunction.ONE_MINUS_SRC_ALPHA, BlendFunction.SRC_ALPHA, BlendFunction.ONE_MINUS_SRC_ALPHA);

        stroke = new BasicStroke(1);
        textSize = 24;
        context.svgTextScale(textSize);
        context.svgStroke(stroke);

        clipState.box = noClip;
        surfaceClipState.box = noClip;

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
                    oPos = vec2(iPos.x * 2.0 / view.x - 1.0, 1.0 - iPos.y * 2.0 / view.y);
                    oTex = iTex;
                	gl_Position = vec4(iPos.x, iPos.y, 0, 1);
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

    public void refreshState() {
        clipState.clipShapes.clear();
        clipState.clipBox.clear();
        clipState.box = noClip;
        surface = null;
        context.setFrameBuffer(null);
        refreshSurfaceState();
    }

    private void refreshSurfaceState() {
        surfaceClipState.clipShapes.clear();
        surfaceClipState.clipBox.clear();
        surfaceClipState.box = noClip;
        stroke = new BasicStroke(1);
        textSize = 24;
        context.svgTextScale(textSize);
        context.svgStroke(stroke);
        context.svgTextFont(Font.getDefault());
        context.svgTextBlur(0);
        context.svgPaint(new ColorPaint(0xFFFFFFFF));
        context.svgAntialias(true);
        setView(0, 0, getWidth(), getHeight());
    }

    public PixelMap createPixelMap() {
        if (surface != null) {
            return surface.createPixelMap();
        } else {
            int w = getWidth();
            int h = getHeight();
            byte[] imageData = new byte[w * h * 4];
            context.readPixels(0, 0, w, h, imageData, 0);
            int pixelBytes = PixelFormat.RGBA.getPixelBytes();
            int rowSize = w * pixelBytes;
            byte[] tempRow = new byte[rowSize];
            for (int y = 0; y < h / 2; y++) {
                int topRowStart = y * rowSize;
                int bottomRowStart = (h - y - 1) * rowSize;
                System.arraycopy(imageData, topRowStart, tempRow, 0, rowSize);
                System.arraycopy(imageData, bottomRowStart, imageData, topRowStart, rowSize);
                System.arraycopy(tempRow, 0, imageData, bottomRowStart, rowSize);
            }
            return new PixelMap(imageData, w, h, PixelFormat.RGBA);
        }
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
        clearClip();
        context.setClearColor(color);
        context.setClearDepth(depth);
        context.setClearStencil(stencil);
        context.clear(true, true, true);
    }

    public void setSurface(Surface surface) {
        if (this.surface != surface) {
            this.surface = surface;
            if (this.surface != null) {
                this.surface.begin(context);
                context.setFrameBuffer(this.surface.frameBuffer);
            } else {
                context.setFrameBuffer(null);
            }
            refreshSurfaceState();
        }
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

    public ClipState getClipState() {
        return surface == null ? clipState : surfaceClipState;
    }

    public void clearClip() {
        getClipState().clipShapes.clear();
        getClipState().clipBox.clear();
        getClipState().box = noClip;

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

        ClipState state = getClipState();

        state.clipShapes.add(inverse);
        if (state.clipBox.isEmpty()) {
            state.clipBox.add(bounds);
        } else {
            Rectangle currentBounds = state.box;
            if (currentBounds != null) {
                float inX = Math.max(currentBounds.x, bounds.x);
                float inY = Math.max(currentBounds.y, bounds.y);
                float inWidth = Math.min(currentBounds.x + currentBounds.width, bounds.x + bounds.width) - inX;
                float inHeight = Math.min(currentBounds.y + currentBounds.height, bounds.y + bounds.height) - inY;

                if (inWidth > 0 && inHeight > 0) {
                    state.clipBox.add(new Rectangle(inX, inY, inWidth, inHeight));
                } else {
                    state.clipBox.add(null);
                }
            } else {
                state.clipBox.add(null);
            }
        }
        state.box = state.clipBox.get(state.clipBox.size() - 1);

        updateClip();
    }

    public void popClip() {
        ClipState state = getClipState();
        if (!state.clipShapes.isEmpty()) {
            state.clipShapes.remove(state.clipShapes.size() - 1);
            state.clipBox.remove(state.clipBox.size() - 1);
            if (state.clipBox.isEmpty()) {
                state.box = noClip;
            } else {
                state.box = state.clipBox.get(state.clipBox.size() - 1);
            }
        }
        updateClip();
    }

    public void updateClip() {
        if (getClipState().clipBox.isEmpty()) {
            context.svgClearClip(false);

        } else if (getClipState().clipBox.get(getClipState().clipBox.size() - 1) == null) {
            context.svgClearClip(true);

        } else {
            context.svgClearClip(false);
            context.svgTransform(null);
            for (int i = 0; i < getClipState().clipShapes.size(); i++) {
                context.svgClip(getClipState().clipShapes.get(0));
            }
            context.svgTransform(transform2D);
        }
    }

    public boolean discardDraw(float x, float y, float w, float h) {
        Rectangle box = getClipState().box;
        if (box == noClip) return false;
        if (box == null) return true;
        if (!transform2D.isTranslationOnly()) return false;
        x += transform2D.m02;
        y += transform2D.m12;

        float fw = stroke.getLineWidth() + 1;
        float hw = fw * 0.5f;

        float x1 = box.x - hw;
        float y1 = box.y - hw;
        float x2 = x1 + box.width + fw;
        float y2 = y1 + box.height + fw;
        return x + w < x1 || x >= x2 || y + h < y1 || y >= y2;
    }

    public boolean discardDrawLine(float x1, float y1, float x2, float y2) {
        float minX = Math.min(x1, x2);
        float maxX = Math.max(x1, x2);
        float minY = Math.min(y1, y2);
        float maxY = Math.max(y1, y2);
        return discardDraw(minX, minY, maxX - minX, maxY - minY);
    }

    public boolean discardDrawLine(float x1, float y1, float cx, float cy, float x2, float y2) {
        float minX = Math.min(x1, Math.min(x2, cx));
        float maxX = Math.max(x1, Math.max(x2, cx));
        float minY = Math.min(y1, Math.min(y2, cy));
        float maxY = Math.max(y1, Math.max(y2, cy));
        return discardDraw(minX, minY, maxX - minX, maxY - minY);
    }

    public boolean discardDrawLine(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2) {
        float minX = Math.min(x1, Math.min(x2, Math.min(cx1, cx2)));
        float maxX = Math.max(x1, Math.max(x2, Math.max(cx1, cx2)));
        float minY = Math.min(y1, Math.min(y2, Math.min(cy1, cy2)));
        float maxY = Math.max(y1, Math.max(y2, Math.max(cy1, cy2)));
        return discardDraw(minX, minY, maxX - minX, maxY - minY);
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
        if (discardDraw(x - radius, y - radius, radius * 2, radius * 2)) return;
        context.svgDrawEllipse(x - radius, y - radius, radius * 2, radius * 2, fill);
    }

    public void drawCircle(Circle circle, boolean fill) {
        drawCircle(circle.x, circle.y, circle.radius, fill);
    }

    public void drawEllipse(float x, float y, float width, float height, boolean fill) {
        if (discardDraw(x, y, width, height)) return;
        context.svgDrawEllipse(x, y, width, height, fill);
    }

    public void drawEllipse(Ellipse ellipse, boolean fill) {
        drawEllipse(ellipse.x, ellipse.y, ellipse.width, ellipse.height, fill);
    }

    public void drawRect(float x, float y, float width, float height, boolean fill) {
        if (discardDraw(x, y, width, height)) return;
        context.svgDrawRect(x, y, width, height, fill);
    }

    public void drawRect(Rectangle rect, boolean fill) {
        drawRect(rect.x, rect.y, rect.width, rect.height, fill);
    }

    public void drawRoundRect(float x, float y, float width, float height, float cTop, float cRight, float cBottom, float cLeft, boolean fill) {
        if (discardDraw(x, y, width, height)) return;
        context.svgDrawRoundRect(x, y, width, height, cTop, cRight, cBottom, cLeft, fill);
    }

    public void drawRoundRect(RoundRectangle rect, boolean fill) {
        drawRoundRect(rect.x, rect.y, rect.width, rect.height, rect.arcTop, rect.arcRight, rect.arcBottom, rect.arcLeft, fill);
    }

    public void drawLine(float x1, float y1, float x2, float y2) {
        if (discardDrawLine(x1, y1, x2, y2)) return;
        context.svgDrawLine(x1, y1, x2, y2);
    }

    public void drawLine(Line line) {
        drawLine(line.x1, line.y1, line.x2, line.y2);
    }

    public void drawQuadCurve(float x1, float y1, float cx, float cy, float x2, float y2) {
        if (discardDrawLine(x1, y1, cx, cy, x2, y2)) return;
        context.svgDrawQuadCurve(x1, y1, cx, cy, x2, y2);
    }

    public void drawQuadCurve(QuadCurve curve) {
        drawQuadCurve(curve.x1, curve.y1, curve.ctrlx, curve.ctrly, curve.x2, curve.y2);
    }

    public void drawCubicCurve(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2) {
        if (discardDrawLine(x1, y1, cx1, cy1, cx2, cy2, x2, y2)) return;
        context.svgDrawCubicCurve(x1, y1, cx1, cy1, cx2, cy2, x2, y2);
    }

    public void drawCubicCurve(CubicCurve curve) {
        drawCubicCurve(curve.x1, curve.y1, curve.ctrlx1, curve.ctrly1,  curve.ctrlx2, curve.ctrly2, curve.x2, curve.y2);
    }

    public void drawText(float x, float y, String text) {
        drawTextSlice(x, y, 0, 0, text);
    }

    public void drawText(float x, float y, Buffer text, int offset, int length) {
        drawTextSlice(x, y, 0, 0, text, offset, length);
    }

    public void drawTextSlice(float x, float y, float maxWidth, float maxHeight, String text) {
        if (discardDraw(x, y,
                maxWidth == 0 ? text.length() * textSize * 10 : maxWidth,
                maxHeight == 0 ? textSize * 2 : maxHeight)) return;
        context.svgDrawText(x, y, text, maxWidth, maxHeight);
    }

    public void drawTextSlice(float x, float y, float maxWidth, float maxHeight, Buffer text, int offset, int length) {
        if (discardDraw(x, y,
                maxWidth == 0 ? length * textSize * 10 : maxWidth,
                maxHeight == 0 ? textSize * 2 : maxHeight)) return;
        context.svgDrawText(x, y, text, offset, length, maxWidth, maxHeight);
    }

    public void drawLinearShadowDown(float x, float y, float width, float height, float alpha) {
        if (discardDraw(x, y, width, height)) return;
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
        if (discardDraw(x, y, width, height)) return;
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
        if (discardDraw(x, y, width, height)) return;

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
        setPaint(paint);
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
        if (discardDraw(dstX1, dstY1, dstX2 - dstX1, dstY2 - dstY1)) return;

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

    public void blitCustomShader(ShaderProgram program, Texture... textures) {
        if (ver == null) {
            ver = new VertexArray(context);
            ebo = new BufferObject(context, BufferType.Element, 6 * 4, UsageType.STATIC_DRAW);
            ebo.setData(0, new int[]{0, 1, 2, 0, 2, 3}, 0, 6);

            vbo = new BufferObject(context, BufferType.Array, 16 * 4, UsageType.STATIC_DRAW);
            vbo.setData(0, new float[]{
                    -1, -1, 0, 0,
                    1, -1,  1,  0,
                    1,  1,  1,  1,
                    -1, 1, 0,  1}, 0, 16);

            ver.setAttributeEnabled(vbo, 0, 2, AttributeType.FLOAT, false, 4 * 4, 0);
            ver.setAttributeEnabled(vbo, 1, 2, AttributeType.FLOAT, false, 4 * 4, 2 * 4);
            ver.setElements(ebo);
        }

        ShaderProgram current = context.getShaderProgram();
        context.setShaderProgram(program);
        program.set("view", new Vector2(getWidth(), getHeight()));
        context.setShaderTextures(textures);
        ver.drawElements(VertexMode.TRIANGLES, 0, 6, 1);
        context.setShaderProgram(current);
        context.setShaderTextures();
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

    public static class ClipState {
        public Rectangle box;
        public final ArrayList<Shape> clipShapes = new ArrayList<>();
        public final ArrayList<Rectangle> clipBox = new ArrayList<>();
    }
}
