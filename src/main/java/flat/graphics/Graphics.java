package flat.graphics;

import flat.exception.FlatException;
import flat.graphics.context.*;
import flat.graphics.context.enums.*;
import flat.graphics.context.paints.ColorPaint;
import flat.graphics.context.paints.GaussianShadow;
import flat.graphics.context.paints.ImagePattern;
import flat.graphics.image.PixelMap;
import flat.graphics.symbols.Font;
import flat.math.Affine;
import flat.math.Vector2;
import flat.math.Vector4;
import flat.math.operations.Area;
import flat.math.shapes.*;
import flat.math.stroke.BasicStroke;

import java.nio.Buffer;

public class Graphics {

    private final Context context;
    private Surface surface;

    private final Affine transform2D = new Affine();
    private final ClipState clipState = new ClipState();
    private Stroke stroke;
    private float textSize;

    // Custom Draw
    private Shader vertex;
    private Shader fragment;
    private Shader bakeFragment;
    private Shader bakeMsFragment;
    private ShaderProgram bakeMsProgram;
    private ShaderProgram bakeProgram;
    private VertexArray ver;
    private BufferObject vbo;
    private BufferObject ebo;

    public Graphics(Context context) {
        this.context = context;

        context.setBlendEnabled(true);
        context.setBlendFunction(
                BlendFunction.ONE, BlendFunction.ONE_MINUS_SRC_ALPHA,
                BlendFunction.ONE, BlendFunction.ONE_MINUS_SRC_ALPHA);
        context.setPixelPackAlignment(1);
        context.setPixelUnpackAlignment(1);

        stroke = new BasicStroke(1);
        textSize = 24;
        context.svgTextScale(textSize);
        context.svgStroke(stroke);

        vertex = new Shader(context, ShaderType.Vertex);
        vertex.setSource(
                """
                #version 330 core
                layout (location = 0) in vec2 iPos;
                layout (location = 1) in vec2 iTex;
                uniform vec2 view;
                uniform vec4 area;
                out vec2 oPos;
                out vec2 oTex;
                void main() {
                    vec2 real = vec2(area.x + iPos.x * area.z, area.y + iPos.y * area.w);
                    oPos = real;
                    oTex = iTex;
                	gl_Position = vec4(real.x / view.x * 2 - 1, - (real.y / view.y * 2 - 1), 0, 1);
                }
                """
        );
        if (!vertex.compile()) {
            throw new FlatException("The default image Vertex Shader fail to compile : " + vertex.getLog());
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
                    vec4 col = fragment(oPos, oTex);
                    FragColor = vec4(col.rgb * col.a, col.a);
                }
                """
        );
        if (!fragment.compile()) {
            throw new FlatException("The default image Fragment Shader fail to compile : " + fragment.getLog());
        }

        bakeFragment = new Shader(context, ShaderType.Fragment);
        bakeFragment.setSource(
                """
                #version 330 core
                out vec4 FragColor;
                in vec2 oTex;
                
                uniform sampler2D mainTexture;
                
                void main() {
                    vec4 col = texture(mainTexture, oTex);
                    if (col.a > 0) {
                        col.rgb /= col.a;
                    } else {
                        vec2 size = textureSize(mainTexture, 0);
                        vec4 c0 = texture(mainTexture, oTex + vec2(1 / size.x, 0.0));
                        vec4 c1 = texture(mainTexture, oTex - vec2(1 / size.x, 0.0));
                        vec4 c2 = texture(mainTexture, oTex + vec2(0.0, 1 / size.y));
                        vec4 c3 = texture(mainTexture, oTex - vec2(0.0, 1 / size.y));
                        col.rgb = (c0 * c0.a + c1 * c1.a + c2 * c2.a + c3 * c3.a).rgb / max(0.001, (c0.a + c1.a + c2.a + c3.a));
                        col.a = 0;
                    }
                    FragColor = col;
                }
                """
        );
        if (!bakeFragment.compile()) {
            throw new FlatException("The default image bake Fragment Shader fail to compile : " + bakeFragment.getLog());
        }

        bakeProgram = new ShaderProgram(context, vertex, bakeFragment);
        if (!bakeProgram.link()) {
            throw new FlatException("The default image bake Shader Program fail to link : " + bakeProgram.getLog());
        }

        bakeMsFragment = new Shader(context, ShaderType.Fragment);
        bakeMsFragment.setSource(
                """
                #version 330 core
                out vec4 FragColor;
                in vec2 oTex;
                
                uniform int samples;
                uniform sampler2DMS mainTexture;
                
                void main() {
                    vec2 size = textureSize(mainTexture);
                    ivec2 texCoord = ivec2(oTex * size);
                    vec4 col = vec4(0.0);
    
                    for (int i = 0; i < samples; i++) {
                        col += texelFetch(mainTexture, texCoord, i);
                    }
                    col /= float(samples);
                    if (col.a > 0) {
                        col.rgb /= col.a;
                    } else {
                        vec4 c0 = texelFetch(mainTexture, texCoord + ivec2(1, 0), 0);
                        vec4 c1 = texelFetch(mainTexture, texCoord - ivec2(1, 0), 0);
                        vec4 c2 = texelFetch(mainTexture, texCoord + ivec2(0, 1), 0);
                        vec4 c3 = texelFetch(mainTexture, texCoord - ivec2(0, 1), 0);
                        col.rgb = (c0 * c0.a + c1 * c1.a + c2 * c2.a + c3 * c3.a).rgb / max(0.001, (c0.a + c1.a + c2.a + c3.a));
                        col.a = 0;
                    }
                    FragColor = col;
                }
                """
        );
        if (!bakeMsFragment.compile()) {
            throw new FlatException("The default image bake Fragment Shader fail to compile : " + bakeMsFragment.getLog());
        }

        bakeMsProgram = new ShaderProgram(context, vertex, bakeMsFragment);
        if (!bakeMsProgram.link()) {
            throw new FlatException("The default image bake Shader Program fail to link : " + bakeMsProgram.getLog());
        }
    }

    public Context getContext() {
        return context;
    }

    public void resetState() {
        if (surface != null) {
            setSurface(null);
        } else {
            resetStateConfig();
        }
    }

    public void resetStateConfig() {
        stroke = new BasicStroke(1);
        textSize = 24;
        context.svgTextScale(textSize);
        context.svgStroke(stroke);
        context.svgTextFont(Font.getDefault());
        context.svgTextBlur(0);
        context.svgPaint(new ColorPaint(0xFFFFFFFF));
        context.svgAntialias(true);
        setAlphaComposite(AlphaComposite.SRC_OVER);
        setView(0, 0, getWidth(), getHeight());
    }

    public PixelMap createPixelMap() {
        return createPixelMap(PixelFormat.RGBA);
    }

    public PixelMap createPixelMap(PixelFormat format) {
        context.softFlush();
        if (surface != null) {
            return new PixelMap(surface.bakeToTexture(this, format, null));
        } else {
            FrameBuffer fbo = new FrameBuffer(context);
            Texture2D target = new Texture2D(getWidth(), getHeight(), format);
            fbo.attach(LayerTarget.COLOR_0, target, 0);
            context.blitFrames(null, fbo,
                    0, 0, getWidth(), getHeight(),
                    0, getHeight(), getWidth(), -getHeight(), BlitMask.Color, MagFilter.NEAREST);
            return new PixelMap(target);
        }
    }

    public PixelMap bakeToTexture(Texture2D texture) {
        context.softFlush();
        if (surface != null) {
            return new PixelMap(surface.bakeToTexture(this, null, texture));
        } else {
            FrameBuffer fbo = new FrameBuffer(context);
            fbo.attach(LayerTarget.COLOR_0, texture, 0);
            context.blitFrames(null, fbo,
                    0, 0, getWidth(), getHeight(),
                    0, getHeight(), getWidth(), -getHeight(), BlitMask.Color, MagFilter.NEAREST);
            return new PixelMap(texture);
        }
    }

    public int getWidth() {
        return surface == null ? context.getWidth() : surface.getWidth();
    }

    public int getHeight() {
        return surface == null ? context.getHeight() : surface.getHeight();
    }

    public float getDensity() {
        return context.getDensity();
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
        getClipState().clear();
        if (stencil != 0x00) {
            getClipState().clipShapes.add(new Rectangle(0, 0, getWidth(), getHeight()));
            getClipState().clipBox.add(null);
            getClipState().box = null;
        }
        context.setClearColor(color);
        context.setClearDepth(depth);
        context.setClearStencil(stencil);
        context.clear(true, true, true);
    }

    public void setSurface(Surface surface) {
        if (this.surface != surface) {
            this.surface = surface;
            if (this.surface != null) {
                this.surface.begin(this);
                context.setFrameBuffer(this.surface.getFrameBuffer());
            } else {
                context.setFrameBuffer(null);
            }
            resetStateConfig();
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

    private ClipState getClipState() {
        return surface == null ? clipState : surface.getClipState();
    }

    public void clearClip() {
        getClipState().clear();

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

        if (state.clipShapes.isEmpty()) {
            state.clipShapes.add(real);
        } else if (!state.isFullyClipped()) {
            if (state.clipShapes.size() == 1) {
                var s = state.clipShapes.get(0);
                if (!(s instanceof Area)) {
                    state.clipShapes.set(0, new Area(s));
                }
            }
            var s = (Area) state.clipShapes.get(state.clipShapes.size() - 1);
            state.clipShapes.add(new Area(real).intersect(s));
        } else {
            state.clipShapes.add(new Area());
        }

        updateClip();
    }

    public void popClip() {
        ClipState state = getClipState();
        if (!state.clipShapes.isEmpty()) {
            state.clipShapes.remove(state.clipShapes.size() - 1);
            state.clipBox.remove(state.clipBox.size() - 1);
            if (state.clipBox.isEmpty()) {
                state.box = ClipState.noClip;
            } else {
                state.box = state.clipBox.get(state.clipBox.size() - 1);
            }
        }
        updateClip();
    }

    public void updateClip() {
        ClipState state = getClipState();
        if (state.isClear()) {
            context.svgClearClip(false);

        } else if (state.isFullyClipped()) {
            context.svgClearClip(true);

        } else {
            context.svgClearClip(true);
            context.svgTransform(null);
            context.svgUnclip(state.clipShapes.get(state.clipShapes.size() - 1));
            context.svgTransform(transform2D);
        }
    }

    public boolean discardDraw(float x, float y, float w, float h) {
        if (getClipState().isClear()) return false;
        if (getClipState().isFullyClipped()) return true;
        if (!transform2D.isTranslationOnly()) return false;
        x += transform2D.m02;
        y += transform2D.m12;

        float fw = stroke.getLineWidth() + 1;
        float hw = fw * 0.5f;

        Rectangle box = getClipState().box;
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
                Math.min(textSize * 2, maxHeight))) return;
        context.svgDrawText(x, y, text, maxWidth, maxHeight);
    }

    public void drawTextSlice(float x, float y, float maxWidth, float maxHeight, Buffer text, int offset, int length) {
        if (discardDraw(x, y,
                maxWidth == 0 ? length * textSize * 10 : maxWidth,
                Math.min(textSize * 2, maxHeight))) return;
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
        Texture2D tex = image.getTexture();
        drawImage(tex,
                0, 0, tex.getWidth(), tex.getHeight(),
                x, y, x + tex.getWidth(), y + tex.getHeight(), 0xFFFFFFFF, false, null);
    }

    public void drawImage(ImageTexture image, float x, float y, float width, float height) {
        Texture2D tex = image.getTexture();
        drawImage(tex,
                0, 0, tex.getWidth(), tex.getHeight(),
                x, y, x + width, y + height, 0xFFFFFFFF, false, null);
    }

    public void drawImage(ImageTexture image, float x, float y, float width, float height, int color) {
        Texture2D tex = image.getTexture();
        drawImage(tex,
                0, 0, tex.getWidth(), tex.getHeight(),
                x, y, x + width, y + height, color, false, null);
    }

    public void drawImage(ImageTexture image, float x, float y, float width, float height, int color, boolean pixelated) {
        Texture2D tex = image.getTexture();
        drawImage(tex,
                0, 0, tex.getWidth(), tex.getHeight(),
                x, y, x + width, y + height, color, pixelated, null);
    }

    public void drawImage(ImageTexture image, float x, float y, float width, float height, int color, boolean pixelated, Affine transform) {
        Texture2D tex = image.getTexture();
        drawImage(tex,
                0, 0, tex.getWidth(), tex.getHeight(),
                x, y, x + width, y + height, color, pixelated, transform);
    }

    public void drawImage(ImageTexture image,
                          float srcX1, float srcY1, float srcX2, float srcY2,
                          float dstX1, float dstY1, float dstX2, float dstY2,
                          int color) {
        Texture2D tex = image.getTexture();
        drawImage(tex,
                srcX1, srcY1, srcX2, srcY2,
                dstX1, dstY1, dstX2, dstY2, color, false, null);
    }

    public void drawImage(ImageTexture image,
                          float srcX1, float srcY1, float srcX2, float srcY2,
                          float dstX1, float dstY1, float dstX2, float dstY2,
                          int color, boolean pixelated) {
        Texture2D tex = image.getTexture();
        drawImage(tex,
                srcX1, srcY1, srcX2, srcY2,
                dstX1, dstY1, dstX2, dstY2, color, pixelated, null);
    }

    public void drawImage(ImageTexture image,
                          float srcX1, float srcY1, float srcX2, float srcY2,
                          float dstX1, float dstY1, float dstX2, float dstY2,
                          int color, boolean pixelated, Affine transform) {
        Texture2D tex = image.getTexture();
        drawImage(tex,
                srcX1, srcY1, srcX2, srcY2,
                dstX1, dstY1, dstX2, dstY2, color, pixelated, transform);
    }

    public void drawImage(Texture2D texture,
                          float srcX1, float srcY1, float srcX2, float srcY2,
                          float dstX1, float dstY1, float dstX2, float dstY2,
                          int color, boolean pixelated, Affine transform) {
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
                .nearest(pixelated)
                .transform(transform)
                .build());
        drawRect(dstX1, dstY1, dstX2 - dstX1, dstY2 - dstY1, true);
        context.svgPaint(paint);
    }

    void bakeSurface(TextureMultisample2D texture) {
        bakeMsProgram.set("samples", texture.getSamples());
        bakeMsProgram.set("mainTexture", 0);
        var comp = getAlphaComposite();
        setAlphaComposite(AlphaComposite.SRC);
        blitCustomShader(bakeMsProgram, 0, 0, getWidth(), getHeight(), texture);
        setAlphaComposite(comp);
    }

    void bakeSurface(Texture texture) {
        bakeProgram.set("mainTexture", 0);
        var comp = getAlphaComposite();
        setAlphaComposite(AlphaComposite.SRC);
        blitCustomShader(bakeProgram, 0, 0, getWidth(), getHeight(), texture);
        setAlphaComposite(comp);
    }

    public void blitCustomShader(ShaderProgram program, Texture... textures) {
        blitCustomShader(program, 0, 0, getWidth(), getHeight(),textures);
    }

    public void blitCustomShader(ShaderProgram program, float x, float y, float width, float height, Texture... textures) {
        context.softFlush();
        if (ver == null) {
            ver = new VertexArray(context);
            ebo = new BufferObject(context, BufferType.Element, 6 * 4, UsageType.STATIC_DRAW);
            ebo.setData(0, new int[]{0, 1, 2, 0, 2, 3}, 0, 6);

            vbo = new BufferObject(context, BufferType.Array, 16 * 4, UsageType.STATIC_DRAW);
            vbo.setData(0, new float[]{
                     0,  0,  0,  0,
                     1,  0,  1,  0,
                     1,  1,  1,  1,
                     0,  1,  0,  1}, 0, 16);

            ver.setAttributeEnabled(vbo, 0, 2, AttributeType.FLOAT, false, 4 * 4, 0);
            ver.setAttributeEnabled(vbo, 1, 2, AttributeType.FLOAT, false, 4 * 4, 2 * 4);
            ver.setElements(ebo);
        }

        ShaderProgram current = context.getShaderProgram();
        context.setShaderProgram(program);
        program.set("area", new Vector4(x, y, width, height));
        program.set("view", new Vector2(getWidth(), getHeight()));
        context.setShaderTextures(textures);
        ver.drawElements(VertexMode.TRIANGLES, 0, 6, 1);
        context.setShaderTextures();
        context.setShaderProgram(current);
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

    AlphaComposite alphaComposite = AlphaComposite.SRC_OVER;

    public AlphaComposite getAlphaComposite() {
        return alphaComposite;
    }

    public void setAlphaComposite(AlphaComposite mode) {
        this.alphaComposite = mode;
        context.svgAlphaComposite(mode);

        switch (mode) {
            case SRC_OVER:
                context.setBlendFunction(
                        BlendFunction.ONE, BlendFunction.ONE_MINUS_SRC_ALPHA,
                        BlendFunction.ONE, BlendFunction.ONE_MINUS_SRC_ALPHA);
                break;
            case DST_OVER:
                context.setBlendFunction(
                        BlendFunction.ONE_MINUS_DST_ALPHA, BlendFunction.ONE,
                        BlendFunction.ONE_MINUS_DST_ALPHA, BlendFunction.ONE);
                break;
            case SRC_IN:
                context.setBlendFunction(
                        BlendFunction.DST_ALPHA, BlendFunction.ZERO,
                        BlendFunction.DST_ALPHA, BlendFunction.ZERO);
                break;
            case DST_IN:
                context.setBlendFunction(
                        BlendFunction.ZERO, BlendFunction.SRC_ALPHA,
                        BlendFunction.ZERO, BlendFunction.SRC_ALPHA);
                break;
            case SRC_OUT:
                context.setBlendFunction(
                        BlendFunction.ONE_MINUS_DST_ALPHA, BlendFunction.ZERO,
                        BlendFunction.ONE_MINUS_DST_ALPHA, BlendFunction.ZERO);
                break;
            case DST_OUT:
                context.setBlendFunction(
                        BlendFunction.ZERO, BlendFunction.ONE_MINUS_SRC_ALPHA,
                        BlendFunction.ZERO, BlendFunction.ONE_MINUS_SRC_ALPHA);
                break;
            case SRC_ATOP:
                context.setBlendFunction(
                        BlendFunction.DST_ALPHA, BlendFunction.ONE_MINUS_SRC_ALPHA,
                        BlendFunction.DST_ALPHA, BlendFunction.ONE_MINUS_SRC_ALPHA);
                break;
            case DST_ATOP:
                context.setBlendFunction(
                        BlendFunction.ONE_MINUS_DST_ALPHA, BlendFunction.SRC_ALPHA,
                        BlendFunction.ONE_MINUS_DST_ALPHA, BlendFunction.SRC_ALPHA);
                break;
            case XOR:
                context.setBlendFunction(
                        BlendFunction.ONE_MINUS_DST_ALPHA, BlendFunction.ONE_MINUS_SRC_ALPHA,
                        BlendFunction.ONE_MINUS_DST_ALPHA, BlendFunction.ONE_MINUS_SRC_ALPHA);
                break;
            case CLEAR:
                context.setBlendFunction(
                        BlendFunction.ZERO, BlendFunction.ZERO,
                        BlendFunction.ZERO, BlendFunction.ZERO);
                break;
            case SRC:
                context.setBlendFunction(
                        BlendFunction.ONE, BlendFunction.ZERO,
                        BlendFunction.ONE, BlendFunction.ZERO);
                break;
            case DST:
                context.setBlendFunction(
                        BlendFunction.ZERO, BlendFunction.ONE,
                        BlendFunction.ZERO, BlendFunction.ONE);
                break;
        }
    }

}
