package flat.backend;

public class SVG {
    static {
        System.loadLibrary("flat");
    }

    public static void load() {
        System.out.println("SVG Library loaded");
    }

    //---------------------------
    //         Context
    //---------------------------
    public static native boolean Init(int flags);
    public static native void Finish();

    //---------------------------
    //          Frame
    //---------------------------
    public static native void BeginFrame(int width, int height, float pixelRatio);
    public static native void CancelFrame();
    public static native void EndFrame();

    //---------------------------
    //     Composite operation
    //---------------------------
    public static native void SetGlobalCompositeOperation(int operation);
    public static native void SetGlobalCompositeBlendFunction(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha);

    //---------------------------
    //      State Handling
    //---------------------------
    public static native void StateSave();
    public static native void StateRestore();
    public static native void StateReset();

    //---------------------------
    //      Render styles
    //---------------------------
    public static native void SetShapeAntiAlias(int enable);
    public static native void SetStrokeColor(int color);
    public static native void SetFillColor(int color);
    public static native void SetMiterLimit(float limit);
    public static native void SetStrokeWidth(float size);
    public static native void SetLineCap(int cap);
    public static native void SetLineJoin(int join);
    public static native void SetGlobalAlpha(float alpha);

    //---------------------------
    //         Transforms
    //---------------------------
    public static native void TransformIdentity();
    public static native void TransformSet(float m00, float m01, float m02, float m10, float m11, float m12);
    public static native void TransformTranslate(float x, float y);
    public static native void TransformRotate(float angle);
    public static native void TransformSkewX(float angle);
    public static native void TransformSkewY(float angle);
    public static native void TransformScale(float x, float y);
    public static native void TransformGetCurrent(float[] data6);

    //---------------------------
    //          Scissoring
    //---------------------------
    public static native void SetScissor(float x, float y, float width, float height);
    public static native void SetIntersectScissor(float x, float y, float width, float height);
    public static native void ResetScissor();

    //---------------------------
    //           Paths
    //---------------------------
    public static native void BeginPath();
    public static native void MoveTo(float x, float y);
    public static native void LineTo(float x, float y);
    public static native void BezierTo(float c1x, float c1y, float c2x, float c2y, float x, float y);
    public static native void QuadTo(float cx, float cy, float x, float y);
    public static native void ArcTo(float x1, float y1, float x2, float y2, float radius);
    public static native void ClosePath();
    public static native void PathWinding(int dir);
    public static native void Arc(float cx, float cy, float radius, float a0, float a1, int dir);
    public static native void Rect(float x, float y, float width, float height);
    public static native void RoundedRect(float x, float y, float width, float height, float radTopLeft, float radTopRight, float radBottomRight, float radBottomLeft);
    public static native void Ellipse(float cx, float cy, float rx, float ry);
    public static native void Circle(float cx, float cy, float radius);
    public static native void Fill();
    public static native void Stroke();

    //---------------------------
    //           Text
    //---------------------------
    public static native int FontCreate(String name, byte[] data);
    public static native int FontFind(String name);
    public static native void FontDestroy(int id);
    public static native boolean FontAddFallbackFont(int baseFont, int fallbackFont);

    public static native void TextSetFont(int font);
    public static native void TextSetSize(float size);
    public static native void TextSetBlur(float blur);
    public static native void TextSetLetterSpacing(float spacing);
    public static native void TextSetLineHeight(float lineHeight);
    public static native void TextSetAlign(int align);

    public static native float DrawText(float x, float y, String string);
    public static native void DrawTextBox(float x, float y, float breakRowWidth, String string);
    public static native float DrawTextBounds(float x, float y, String string, float[] bounds4);
    public static native void DrawTextBoxBounds(float x, float y, float breakRowWidth, String string, float[] bounds4);
    public static native float TextMetricsGetAscender();
    public static native float TextMetricsGetDescender();
    public static native float TextMetricsGetLineHeight();
}