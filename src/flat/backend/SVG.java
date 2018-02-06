package flat.backend;

import java.nio.Buffer;

public class SVG {

    //---------------------------
    //         Context2D
    //---------------------------
    public static native long Create(int flags);
    public static native void Destroy(long context);

    //---------------------------
    //          Frame
    //---------------------------
    public static native void BeginFrame(long context, int width, int height, float pixelRatio);
    public static native void CancelFrame(long context);
    public static native void EndFrame(long context);

    //---------------------------
    //     Composite operation
    //---------------------------
    public static native void SetGlobalCompositeOperation(long context, int operation);
    public static native void SetGlobalCompositeBlendFunction(long context, int srcRGB, int dstRGB, int srcAlpha, int dstAlpha);

    //---------------------------
    //      State Handling
    //---------------------------
    public static native void StateSave(long context);
    public static native void StateRestore(long context);
    public static native void StateReset(long context);

    //---------------------------
    //      Render styles
    //---------------------------
    public static native void SetShapeAntiAlias(long context, boolean enable);
    public static native void SetPaintColor(long context, int color);
    public static native void SetPaintLinearGradient(long context, float x1, float y1, float x2, float y2, int count, float[] stops, int[] colors, int cycleMethod, int interpolation);
    public static native void SetPaintRadialGradient(long context, float x1, float y1, float radiusIn, float radiusOut, int count, float[] stops, int[] colors, int cycleMethod, int interpolation);
    public static native void SetPaintBoxShadow(long context, float x, float y, float width, float height, float corners, float blur, float alpha, int interpolation);
    public static native void SetMiterLimit(long context, float limit);
    public static native void SetStrokeWidth(long context, float size);
    public static native void SetLineCap(long context, int cap);
    public static native void SetLineJoin(long context, int join);
    public static native void SetGlobalAlpha(long context, float alpha);

    //---------------------------
    //         Transforms
    //---------------------------
    public static native void TransformIdentity(long context);
    public static native void TransformSet(long context, float m00, float m01, float m02, float m10, float m11, float m12);
    public static native void TransformGet(long context, float[] data6);
    public static native void TransformTranslate(long context, float x, float y);
    public static native void TransformRotate(long context, float angle);
    public static native void TransformSkewX(long context, float angle);
    public static native void TransformSkewY(long context, float angle);
    public static native void TransformScale(long context, float x, float y);

    //---------------------------
    //          Scissoring
    //---------------------------
    public static native void SetScissor(long context, float x, float y, float width, float height);
    public static native void SetIntersectScissor(long context, float x, float y, float width, float height);
    public static native void ResetScissor(long context);

    //---------------------------
    //           Paths
    //---------------------------
    public static native void BeginPath(long context);
    public static native void MoveTo(long context, float x, float y);
    public static native void LineTo(long context, float x, float y);
    public static native void BezierTo(long context, float c1x, float c1y, float c2x, float c2y, float x, float y);
    public static native void QuadTo(long context, float cx, float cy, float x, float y);
    public static native void ArcTo(long context, float x1, float y1, float x2, float y2, float radius);
    public static native void ClosePath(long context);
    public static native void PathWinding(long context, int dir);
    public static native void Arc(long context, float cx, float cy, float radius, float a0, float a1, int dir);
    public static native void Rect(long context, float x, float y, float width, float height);
    public static native void RoundedRect(long context, float x, float y, float width, float height, float c1, float c2, float c3, float c4);
    public static native void Ellipse(long context, float cx, float cy, float rx, float ry);
    public static native void Circle(long context, float cx, float cy, float radius);
    public static native void Fill(long context);
    public static native void Stroke(long context);

    //---------------------------
    //           Text
    //---------------------------
    public static native int FontCreate(long context, String name, byte[] data);
    public static native int FontFind(long context, String name);
    public static native void FontDestroy(long context, int id);
    public static native boolean FontAddFallbackFont(long context, int baseFont, int fallbackFont);

    public static native void TextSetFont(long context, int font);
    public static native void TextSetSize(long context, float size);
    public static native void TextSetBlur(long context, float blur);
    public static native void TextSetLetterSpacing(long context, float spacing);
    public static native void TextSetLineHeight(long context, float lineHeight);
    public static native void TextSetAlign(long context, int align);

    public static native float DrawText(long context, float x, float y, String string);
    public static native void DrawTextBox(long context, float x, float y, float breakRowWidth, String string);
    public static native float DrawTextBounds(long context, float x, float y, String string, float[] bounds4);
    public static native void DrawTextBoxBounds(long context, float x, float y, float breakRowWidth, String string, float[] bounds4);

    public static native float DrawTextBuffer(long context, float x, float y, Buffer string, int offset, int length);
    public static native void DrawTextBoxBuffer(long context, float x, float y, float breakRowWidth, Buffer string, int offset, int length);
    public static native float DrawTextBoundsBuffer(long context, float x, float y, Buffer string, int offset, int length, float[] bounds4);
    public static native void DrawTextBoxBoundsBuffer(long context, float x, float y, float breakRowWidth, Buffer string, int offset, int length, float[] bounds4);

    public static native float TextGetWidth(long context, String string);
    public static native float TextGetWidthBuffer(long context, Buffer string, int offset, int length);
    public static native int TextGetLastGlyph(long context, String string, float breakRowWidth);
    public static native int TextGetLastGlyphBuffer(long context, Buffer string, int offset, int length, float breakRowWidth);

    public static native float TextMetricsGetAscender(long context);
    public static native float TextMetricsGetDescender(long context);
    public static native float TextMetricsGetLineHeight(long context);
}