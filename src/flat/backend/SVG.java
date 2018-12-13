package flat.backend;

import java.nio.Buffer;

public class SVG {

    //---------------------------
    //         Context2D
    //---------------------------
    public static native long Create();
    public static native void Destroy(long context);

    //---------------------------
    //          Frame
    //---------------------------
    public static native void BeginFrame(long context, int width, int height);
    public static native void EndFrame(long context);

    //---------------------------
    //      Render styles
    //---------------------------
    public static native void SetAntiAlias(long context, boolean aa);
    public static native void SetStroke(long context, float width, int cap, int join, float mitter);
    public static native void SetPaintColor(long context, int color);
    public static native void SetPaintLinearGradient(long context, float[] affine, float x1, float y1, float x2, float y2, int count, float[] stops, int[] colors, int cycleMethod);
    public static native void SetPaintRadialGradient(long context, float[] affine, float x1, float y1, float radiusIn, float radiusOut, int count, float[] stops, int[] colors, int cycleMethod);
    public static native void SetPaintBoxGradient(long context, float[] affine, float x, float y, float width, float height, float corners, float blur, int count, float[] stops, int[] colors, int cycleMethod);
    public static native void SetPaintImage(long context, int textureID, float[] affineImg, int color);
    public static native void SetPaintImageLinearGradient(long context, int textureID, float[] affineImg, float[] affine, float x1, float y1, float x2, float y2, int count, float[] stops, int[] colors, int cycleMethod);
    public static native void SetPaintImageRadialGradient(long context, int textureID, float[] affineImg, float[] affine, float x1, float y1, float radiusIn, float radiusOut, int count, float[] stops, int[] colors, int cycleMethod);
    public static native void SetPaintImageBoxGradient(long context, int textureID, float[] affineImg, float[] affine, float x, float y, float width, float height, float corners, float blur, int count, float[] stops, int[] colors, int cycleMethod);

    //---------------------------
    //         Transforms
    //---------------------------
    public static native void TransformIdentity(long context);
    public static native void TransformSet(long context, float m00, float m01, float m10, float m11, float m02, float m12);

    //---------------------------
    //          Clipping
    //---------------------------
    public static native void ClearClip(long context, int clip);

    //---------------------------
    //           Paths
    //---------------------------
    public static native void PathBegin(long context, int type);
    public static native void MoveTo(long context, float x, float y);
    public static native void LineTo(long context, float x, float y);
    public static native void QuadTo(long context, float cx, float cy, float x, float y);
    public static native void CubicTo(long context, float c1x, float c1y, float c2x, float c2y, float x, float y);
    public static native void Close(long context);
    public static native void PathEnd(long context);

    //---------------------------
    //     Economics Shapes
    //---------------------------
    public static native void Rect(long context, float x, float y, float width, float height);
    public static native void Ellipse(long context, float x, float y, float width, float height);
    public static native void RoundRect(long context, float x, float y, float width, float height, float c1, float c2, float c3, float c4);

    //---------------------------
    //           Text
    //---------------------------
    public static native long FontCreate(byte[] data, float size, int sdf);
    public static native void FontLoadAllGlyphs(long font);
    public static native void FontLoadGlyphs(long font, String characters);
    public static native void FontLoadGlyphsBuffer(long font, Buffer characters, int offset, int length);
    public static native int FontGetGlyphs(long font, String string, float[] data);
    public static native int FontGetGlyphsBuffer(long font, Buffer string, int offset, int length, float[] data);
    public static native float FontGetHeight(long font);
    public static native float FontGetAscent(long font);
    public static native float FontGetDescent(long font);
    public static native float FontGetTextWidth(long font, String string, float spacing);
    public static native float FontGetTextWidthBuffer(long font, Buffer string, int offset, int length, float spacing);
    public static native void FontDestroy(long font);

    public static native void SetFont(long context, long font);
    public static native void SetFontScale(long context, float size);
    public static native void SetFontSpacing(long context, float spacing);

    public static native float DrawText(long context, float x, float y, String string, float maxWidth, int hAlign, int vAlign);
    public static native float DrawTextBuffer(long context, float x, float y, Buffer string, int offset, int length, float maxWidth, int hAlign, int vAlign);

}