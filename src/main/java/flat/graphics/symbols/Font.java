package flat.graphics.symbols;

import flat.backend.GL;
import flat.backend.GLEnums;
import flat.backend.SVG;
import flat.exception.FlatException;
import flat.graphics.context.Context;
import flat.graphics.context.DisposeTask;
import flat.graphics.context.Glyph;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.context.fonts.SystemFonts;
import flat.graphics.image.ImageTexture;
import flat.math.shapes.Path;
import flat.window.Application;

import java.lang.ref.Cleaner;
import java.nio.Buffer;

public class Font {
    protected final static Cleaner cleaner = Cleaner.create();

    private static final float[] readGlyph = new float[5];

    private final String name;
    private final String family;
    private final FontPosture posture;
    private final FontWeight weight;
    private final FontStyle style;

    private final long fontId;

    private final float size, height, ascent, descent, lineGap;
    private final int glyphCount;
    private final boolean sdf;

    private final DisposeTask disposeTask;
    private boolean disposed;
    private int[] glyphs;

    public Font(String family, FontWeight weight, FontPosture posture, FontStyle style, byte[] data) {
        this(family, weight, posture, style, data,
                Application.getSystemQuality() == 1 ? 24 :
                Application.getSystemQuality() == 2 ? 32 : 48, true);
    }

    public Font(String family, FontWeight weight, FontPosture posture, FontStyle style, byte[] data, float size, boolean sdf) {
        if (data == null || data.length == 0) {
            throw new FlatException("Invalid font data");
        }

        final long fontId = SVG.FontLoad(data, size, sdf ? 1 : 0);
        if (fontId == 0) {
            throw new FlatException("Invalid font data");
        }

        this.fontId = fontId;

        this.size = size;
        this.sdf = sdf;

        this.name = SVG.FontGetName(fontId);
        this.height = SVG.FontGetHeight(fontId);
        this.ascent = SVG.FontGetAscent(fontId);
        this.descent = SVG.FontGetDescent(fontId);
        this.lineGap = SVG.FontGetLineGap(fontId);
        this.glyphCount = SVG.FontGetGlyphCount(fontId);

        this.family = family;
        this.weight = weight != null ? weight : FontWeight.parse(SVG.FontGetWeight(fontId));
        this.posture = posture != null ? posture : SVG.FontIsItalic(fontId) ? FontPosture.ITALIC : FontPosture.REGULAR;
        this.style = style != null ? style : SystemFonts.guessStyle(name);

        // Auto Removal
        cleaner.register(this, disposeTask = new DisposeTask(() -> SVG.FontUnload(fontId)));
    }

    public static Font getDefault() {
        return FontManager.getDefault();
    }

    public String getName() {
        return name;
    }

    public String getFamily() {
        return family;
    }

    public FontPosture getPosture() {
        return posture;
    }

    public FontWeight getWeight() {
        return weight;
    }

    public FontStyle getStyle() {
        return style;
    }

    public boolean isSdf() {
        return sdf;
    }

    public boolean isBold() {
        return weight.getWeight() >= FontWeight.BOLD.getWeight();
    }

    public boolean isItalic() {
        return posture == FontPosture.ITALIC;
    }

    public float getWidth(String text, float size, float spacing) {
        checkDisposed();
        return size <= 0 ? 0 : SVG.FontGetTextWidth(getInternalId(), text, size / this.size, spacing);
    }

    public float getWidth(Buffer text, int offset, int length, float size, float spacing) {
        checkDisposed();
        return size <= 0 ? 0 : SVG.FontGetTextWidthBuffer(getInternalId(), text, offset, length, size / this.size, spacing);
    }

    public CaretData getCaretOffset(String text, float size, float spacing, float x, boolean half) {
        checkDisposed();
        if (size < 0) {
            return new CaretData(0, 0);
        } else {
            float[] data = new float[2];
            SVG.FontGetOffset(getInternalId(), text, size / this.size, spacing, x, half, data);
            return new CaretData((int) data[0], data[1]);
        }
    }

    public CaretData getCaretOffset(Buffer text, int offset, int length, float size, float spacing, float x, boolean half) {
        checkDisposed();
        if (size < 0) {
            return new CaretData(x > 0 ? length : 0, 0);
        } else {
            float[] data = new float[2];
            SVG.FontGetOffsetBuffer(getInternalId(), text, offset, length, size / this.size, spacing, x, half, data);
            return new CaretData((int) data[0], data[1]);
        }
    }

    public CaretData getCaretOffsetSpace(String text, float size, float spacing, float x) {
        checkDisposed();
        if (size < 0) {
            return new CaretData(0, 0);
        } else {
            float[] data = new float[2];
            SVG.FontGetOffsetSpace(getInternalId(), text, size / this.size, spacing, x, data);
            return new CaretData((int) data[0], data[1]);
        }
    }

    public CaretData getCaretOffsetSpace(Buffer text, int offset, int length, float size, float spacing, float x) {
        checkDisposed();
        if (size < 0) {
            return new CaretData(x > 0 ? length : 0, 0);
        } else {
            float[] data = new float[2];
            SVG.FontGetOffsetSpaceBuffer(getInternalId(), text, offset, length, size / this.size, spacing, x, data);
            return new CaretData((int) data[0], data[1]);
        }
    }

    public int getLineWrap(String text, float size, float spacing, float x) {
        checkDisposed();
        if (size < 0) {
            return 1;
        } else {
            return SVG.FontGetLineWrap(getInternalId(), text, size / this.size, spacing, x);
        }
    }

    public int getLineWrap(Buffer text, int offset, int length, float size, float spacing, float x) {
        checkDisposed();
        if (size < 0) {
            return 1;
        } else {
            return SVG.FontGetLineWrapBuffer(getInternalId(), text, offset, length, size / this.size, spacing, x);
        }
    }

    public float getSize() {
        return size;
    }

    public float getHeight(float size) {
        return size * height / this.size;
    }

    public float getLineGap(float size) {
        return size * lineGap / this.size;
    }

    public float getAscent(float size) {
        return size * ascent / this.size;
    }

    public float getDescent(float size) {
        return size * descent / this.size;
    }

    private int[] getGlyphs() {
        if (glyphs == null) {
            glyphs = new int[glyphCount];
            SVG.FontGetAllCodePoints(getInternalId(), glyphs);
        }
        return glyphs;
    }

    public int getGlyphCount() {
        return glyphCount;
    }

    public int getGlyphCodePoint(int glyphId) {
        checkDisposed();
        if (glyphId < glyphCount) {
            return getGlyphs()[glyphId];
        }
        return 0;
    }

    public Glyph getGlyphDataByIndex(int glyphId) {
        return getGlyphData(getGlyphCodePoint(glyphId));
    }

    public Glyph getGlyphData(int codePoint) {
        checkDisposed();
        SVG.FontGetGlyph(getInternalId(), codePoint, readGlyph);
        return new Glyph(readGlyph[0], readGlyph[1], readGlyph[2], readGlyph[3], readGlyph[4]);
    }

    public Path getGlyphPathByIndex(int glyphId) {
        return getGlyphPath(getGlyphCodePoint(glyphId));
    }

    public Path getGlyphPath(int codePoint) {
        checkDisposed();
        float[] polygon = SVG.FontGetGlyphShape(getInternalId(), codePoint);
        if (polygon != null) {
            Path path = new Path();
            float sx = 0, sy = 0;
            float cx = 0, cy = 0;
            for (int i = 0; i < polygon.length; ) {
                float type = polygon[i++];
                if (type == 0) {
                    if (i > 1) path.closePath();
                    path.moveTo(polygon[i++], polygon[i++]);
                } else if (type == 1) {
                    path.lineTo(polygon[i++], polygon[i++]);
                } else if (type == 2) {
                    path.quadTo(polygon[i++], polygon[i++], polygon[i++], polygon[i++]);
                } else if (type == 3) {
                    path.curveTo(polygon[i++], polygon[i++], polygon[i++], polygon[i++], polygon[i++], polygon[i++]);
                }
            }
            if (polygon.length > 0) path.closePath();
            return path;
        } else {
            return new Path();
        }
    }
    public float getGlyphKerning(int codePointA, int codePointB) {
        checkDisposed();
        return SVG.FontGetKerning(fontId, codePointA, codePointB);
    }

    public ImageTexture createImageFromAtlas(Context context) {
        int[] data = new int[4];
        int imageId = (int) SVG.FontGetAtlas(getInternalId(), data);
        int w = data[0];
        int h = data[1];
        byte[] imageData = new byte[w * h];
        if (imageId == 0) {
            return new ImageTexture(imageData, w, h, PixelFormat.RED);
        } else {

            int oldId = GL.TextureGetBound(GLEnums.TT_TEXTURE_2D);
            GL.TextureBind(GLEnums.TT_TEXTURE_2D, imageId);
            GL.TexGetImageB(GLEnums.TT_TEXTURE_2D, 0, PixelFormat.RED.getInternalEnum(), imageData, 0);
            GL.TextureBind(GLEnums.TT_TEXTURE_2D, oldId);

            return new ImageTexture(imageData, w, h, PixelFormat.RED);
        }
    }

    void checkDisposed() {
        if (disposed) {
            throw new FlatException("The Font is disposed");
        }
    }

    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        if (!disposed) {
            FontManager.uninstall(this);
            disposed = true;
            disposeTask.run();
        }
    }

    public long getInternalId() {
        return fontId;
    }

    @Override
    public String toString() {
        return family
                + (posture == null ? "" : ", " + posture)
                + (weight == null ? "" : ", " + weight)
                + (style == null ? "" : ", " + style);
    }

    private static class FontIdPair {
        private long fontId;
        private long fontRenderId;
    }

    public static class CaretData {
        public final int index;
        public final float width;

        public CaretData(int index, float width) {
            this.index = index;
            this.width = width;
        }

        public int getIndex() {
            return index;
        }

        public float getWidth() {
            return width;
        }
    }
}