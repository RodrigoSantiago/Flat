package flat.backend;

import java.nio.Buffer;

public class SVG {

    //---------------------------
    //         Context2D
    //---------------------------
    public static native long Create();
    public static native void Destroy(long context);
    public static native void SetDebug(boolean debug);

    //---------------------------
    //          Frame
    //---------------------------
    public static native void BeginFrame(long context, int width, int height);
    public static native void EndFrame(long context);
    public static native void Flush(long context);

    //---------------------------
    //      Render styles
    //---------------------------
    public static native void SetAntiAlias(long context, boolean aa);
    public static native void SetStroke(long context, float width, int cap, int join, float miter, float[] dash, float dashPhase);
    public static native void SetPaintColor(long context, int color);
    public static native void SetPaintLinearGradient(long context, float x1, float y1, float x2, float y2, int count, float[] data, int cycleMethod);
    public static native void SetPaintRadialGradient(long context, float x1, float y1, float fx, float fy, float rIn, float rOut, int count, float[] data, int cycleMethod);
    public static native void SetPaintBoxGradient(long context, float x, float y, float w, float h, float corners, float blur, float alpha, int color, float[] data);
    public static native void SetPaintImage(long context, int textureID, int color, float[] data, int cycleMethod, boolean nearest);
    public static native void SetBlendMode(long context, int blendMode);

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
    public static native void PathBegin(long context, int type, int rule);
    public static native void MoveTo(long context, float x, float y);
    public static native void LineTo(long context, float x, float y);
    public static native void QuadTo(long context, float cx, float cy, float x, float y);
    public static native void CubicTo(long context, float c1x, float c1y, float c2x, float c2y, float x, float y);
    public static native void Close(long context);
    public static native void PathEnd(long context);

    //---------------------------
    //     Economics Shapes
    //---------------------------
    public static native void Rect(long context, float x, float y, float width, float height, boolean fill);
    public static native void Ellipse(long context, float x, float y, float width, float height, boolean fill);
    public static native void RoundRect(long context, float x, float y, float width, float height, float c1, float c2, float c3, float c4, boolean fill);

    //---------------------------
    //           Text
    //---------------------------
    public static native long FontLoad(byte[] data, float size, int sdf);
    public static native void FontUnload(long font);
    public static native void FontSetEmojiEnabled(boolean enabled);
    public static native void FontCreateEmoji(long textureId, int[] sequence);
    public static native void FontDestroyEmoji();
    public static native long FontGetAtlas(long font, int[] size);

    public static native void FontGetAllCodePoints(long font, int[] codePoints);
    public static native void FontGetGlyph(long font, int codePoint, float[] data);
    public static native float[] FontGetGlyphShape(long font, int codePoints);
    public static native float FontGetKerning(long font, int codePointA , int codePointB);
    public static native String FontGetName(long font);
    public static native boolean FontIsBold(long font);
    public static native boolean FontIsItalic(long font);
    public static native int FontGetWeight(long font);
    public static native float FontGetHeight(long font);
    public static native float FontGetAscent(long font);
    public static native float FontGetDescent(long font);
    public static native float FontGetLineGap(long font);
    public static native int FontGetGlyphCount(long font);
    public static native float FontGetTextWidth(long font, String string, float size, float spacing);
    public static native float FontGetTextWidthBuffer(long font, Buffer string, int offset, int length, float size, float spacing);
    public static native void FontGetOffset(long font, String string, float size, float spacing, float x, boolean half, float[] cursor);
    public static native void FontGetOffsetBuffer(long font, Buffer string, int offset, int length, float size, float spacing, float x, boolean half, float[] cursor);
    public static native void FontGetOffsetSpace(long font, String string, float size, float spacing, float x, float[] cursor);
    public static native void FontGetOffsetSpaceBuffer(long font, Buffer string, int offset, int length, float size, float spacing, float x, float[] cursor);
    public static native int FontGetLineWrap(long font, String string, float size, float spacing, float x);
    public static native int FontGetLineWrapBuffer(long font, Buffer string, int offset, int length, float size, float spacing, float x);

    public static native void SetFont(long context, long fontPaint);
    public static native void SetFontScale(long context, float size);
    public static native void SetFontSpacing(long context, float spacing);
    public static native void SetFontBlur(long context, float blur);

    public static native int DrawText(long context, float x, float y, String string, float maxWidth, float maxHeight);
    public static native int DrawTextBuffer(long context, float x, float y, Buffer string, int offset, int length, float maxWidth, float maxHeight);

    //---------------------------
    //           Image
    //---------------------------
    public static native byte[] ReadImage(byte[] data, int[] imageData);
    public static native byte[] WriteImage(byte[] imageData, int width, int height, int channels, int format, int quality);
}