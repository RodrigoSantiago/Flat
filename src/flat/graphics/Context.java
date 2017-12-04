package flat.graphics;

import flat.backend.GL;
import flat.backend.SVG;
import flat.graphics.image.Image;
import flat.graphics.paint.LineCap;
import flat.graphics.paint.LineJoin;
import flat.graphics.paint.Paint;
import flat.graphics.svg.Svg;
import flat.graphics.text.Font;
import flat.graphics.text.TextAligment;
import flat.math.Affine;

import static flat.backend.GLEnuns.*;
import static flat.backend.SVGEnuns.*;

public final class Context {

    private static Context context;
    private Context(Thread thread) {
        this.thread = thread;
    }
    public static Context getContext() {
        if (context == null) {
            context = new Context(Thread.currentThread());
        }
        return context;
    }

    private final Thread thread;
    private int mode;
    public int width, height;

    private int color;
    private Paint paint;
    private float strokeSize;
    private LineCap strokeCap;
    private LineJoin strokeJoin;

    private Affine idt = new Affine();
    private Affine tr2 = new Affine();

    public void clear() {
        GL.Clear(CB_COLOR_BUFFER_BIT | CB_DEPTH_BUFFER_BIT | CB_STENCIL_BUFFER_BIT);
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        if (mode == 1) {
            SVG.EndFrame();
            mode = 0;
            setModeSvg();
        }
        GL.SetViewport(0, 0, width, height);
    }
    
    public void setModeClear() {
        if (mode != 0) {
            if (mode == 1) {
                SVG.EndFrame();
            }
            mode = 0;
            setTransform2D(null);
        }
    }

    public void setModeSvg() {
        if (mode != 1) {
            mode = 1;
            SVG.BeginFrame(width, height, 1);
            SVG.SetLineCap(strokeCap == LineCap.BUTT ? SVG_BUTT : strokeCap == LineCap.SQUARE ? SVG_SQUARE : SVG_ROUND);
            SVG.SetLineJoin(strokeJoin == LineJoin.BEVEL ? SVG_BEVEL : strokeJoin == LineJoin.MITER ? SVG_MITER : SVG_ROUND);
            SVG.SetStrokeWidth(strokeSize);
            SVG.SetStrokeColor(color);
            SVG.SetFillColor(color);
            SVG.TransformSet(tr2.m00, tr2.m10, tr2.m01, tr2.m11, tr2.m02, tr2.m12);
        }
    }

    public void setMode2D() {
        if (mode != 2) {
            if (mode == 1) {
                float[] d9 = new float[9];
                SVG.TransformGetCurrent(d9);
                tr2.set(d9);
                SVG.EndFrame();
            }
            mode = 2;

            // todo - Ir adicionando imagens ate uma certa quantidade ( contanto que sejam da mesma textura )
            // desenhar todas no final de uma lapada so
        }
    }
    public void setMode3D() {
        if (mode != 3) {
            if (mode == 1) {
                float[] d9 = new float[9];
                SVG.TransformGetCurrent(d9);
                tr2.set(d9);
                SVG.EndFrame();
            }
            mode = 3;
        }
    }

    public void setTransform2D(Affine transform2D) {
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

    public void drawImage(Image image, float x, float y) {

    }

    public void drawImage(Image image, float srcX, float srcY, float srcW, float srcH, float dstX, float dstY, float dstW, float dstH) {

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

    public void setStrokeSize(float size) {
        strokeSize = Math.max(0.0001f, size);
        if (mode == 1) {
            SVG.SetStrokeWidth(strokeSize);
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
        setModeSvg();
        SVG.BeginPath();
        SVG.MoveTo(x1, y1);
        SVG.LineTo(x2, y2);
        SVG.Stroke();
    }

    public void drawQuad(float x1, float y1, float x2, float y2, float cx, float cy) {
        setModeSvg();
        SVG.BeginPath();
        SVG.MoveTo(x1, y1);
        SVG.QuadTo(cx, cy, x2, y2);
        SVG.Stroke();
    }

    public void drawBezier(float x1, float y1, float x2, float y2, float cx1, float cy1, float cx2, float cy2) {
        setModeSvg();
        SVG.BeginPath();
        SVG.MoveTo(x1, y1);
        SVG.BezierTo(cx1, cy1, cx2, cy2,x2, y2);
        SVG.Stroke();
    }

    public void drawCircle(float x, float y, float r, boolean fill) {
        setModeSvg();
        SVG.BeginPath();
        SVG.Circle(x, y, r);
        if (fill) {
            SVG.Fill();
        } else {
            SVG.Stroke();
        }
    }

    public void drawEllipse(float x, float y, float width, float height, boolean fill) {
        setModeSvg();
        SVG.BeginPath();
        SVG.Ellipse(x, y, width, height);
        if (fill) {
            SVG.Fill();
        } else {
            SVG.Stroke();
        }
    }

    public void drawRect(float x, float y, float width, float height, boolean fill) {
        setModeSvg();
        SVG.BeginPath();
        SVG.Rect(x, y, width, height);
        if (fill) {
            SVG.Fill();
        } else {
            SVG.Stroke();
        }
    }

    public void drawRoundRect(float x, float y, float width, float height, float corners, boolean fill) {
        setModeSvg();
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

    public void drawText(String text, float x, float y, float width) {

    }

    public void beginShadow() {

    }

    public void flushShadow(int color, float xoffset, float yoffset, float radius) {

    }
}
