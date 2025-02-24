package flat.graphics.context;

import flat.backend.SVG;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.image.PixelMap;
import flat.graphics.text.FontPosture;
import flat.graphics.text.FontStyle;
import flat.graphics.text.FontWeight;
import flat.math.shapes.Path;
import flat.window.Application;

import java.lang.ref.Cleaner;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;

public class Font {
    protected final static Cleaner cleaner = Cleaner.create();

    private static final ArrayList<Font> fonts = new ArrayList<>();
    private static Font DefaultFont;
    private static final float[] readGlyph = new float[5];

    private final String family;
    private final FontPosture posture;
    private final FontWeight weight;
    private final FontStyle style;

    private final long fontID;
    private final float size, height, ascent, descent, lineGap;
    private final int glyphCount;
    private final boolean sdf;
    private boolean diposed;

    private HashMap<Context, FontRender> contextInternal;
    private AutoCleaner autoCleaner;
    private int[] glyphs;

    public Font(String family, FontWeight weight, FontPosture posture, FontStyle style, byte[] data) {
        this(family, weight, posture, style, data, 48, true);
    }

    public Font(String family, FontWeight weight, FontPosture posture, FontStyle style, byte[] data, float size, boolean sdf) {
        if (data == null || data.length == 0) {
            throw new RuntimeException("Invalid font data");
        }

        this.family = family;
        this.weight = weight;
        this.posture = posture;
        this.style = style;
        this.size = size;
        this.sdf = sdf;

        final long fontID = SVG.FontLoad(data, size, sdf ? 1 : 0);
        if (fontID == 0) {
            throw new RuntimeException("Invalid font data");
        }
        this.fontID = fontID;
        this.height = SVG.FontGetHeight(fontID);
        this.ascent = SVG.FontGetAscent(fontID);
        this.descent = SVG.FontGetDescent(fontID);
        this.lineGap = SVG.FontGetLineGap(fontID);
        this.glyphCount = SVG.FontGetGlyphCount(fontID);

        final HashMap<Context, FontRender> fontRenderer = new HashMap<>();
        this.contextInternal = fontRenderer;

        this.autoCleaner = new AutoCleaner(fontRenderer, fontID);

        // Auto Removal
        cleaner.register(this, this.autoCleaner);
    }

    public static Font findFont(String family) {
        return findFont(family, null);
    }

    public static Font findFont(FontWeight weight) {
        return findFont(null, weight);
    }

    public static Font findFont(FontPosture posture) {
        return findFont(null, null, posture);
    }

    public static Font findFont(FontStyle style) {
        return findFont(null, null, null, style);
    }

    public static Font findFont(String family, FontWeight weight) {
        return findFont(family, weight, null, null);
    }

    public static Font findFont(String family, FontWeight weight, FontPosture posture) {
        return findFont(family, weight, posture, null);
    }

    public static Font findFont(String family, FontWeight weight, FontPosture posture, FontStyle style) {
        readDefaultFonts();

        Font closer = null;
        int minDiff = 0;
        for (Font font : fonts) {
            int diff = font.getFontDifference(family, weight, posture, style);
            if (closer == null || diff < minDiff) {
                closer = font;
                minDiff = diff;
            } else if (diff == minDiff) {
                // Style
                if (font.style.ordinal() < closer.style.ordinal()) {
                    closer = font;
                } else if (font.style.ordinal() == closer.style.ordinal()) {
                    // Posture
                    if (font.posture.ordinal() < closer.posture.ordinal()) {
                        closer = font;
                    } else if (font.posture.ordinal() == closer.posture.ordinal()) {
                        // Weight
                        if (font.weight.ordinal() < closer.weight.ordinal()) {
                            closer = font;
                        } else if (font.weight.ordinal() == closer.weight.ordinal()) {
                            // Internal random fixed value
                            if (font.fontID < closer.fontID) {
                                closer = font;
                            }
                        }
                    }
                }
            }
        }
        return closer;
    }

    private int getFontDifference(String family, FontWeight weight, FontPosture posture, FontStyle style) {
        int f = family == null ? 1 : this.family.equalsIgnoreCase(family) ? 0 : 1;
        int s = style == null ? 1 : this.style == style ? 0 : 1;
        int p = posture == null ? 1 : this.posture == posture ? 0 : 1;
        int w = weight == null ? 400 : Math.abs(this.weight.getWeight() - weight.getWeight());

        return f * 8000 + s * 4000 + p * 2000 + w;
    }

    public static void install(Font font) {
        font.checkDisposed();
        fonts.add(font);
    }

    public static Font getDefault() {
        readDefaultFonts();
        return DefaultFont;
    }

    private static void readDefaultFonts() {
        if (DefaultFont != null) {
            return;
        }
        var res = Application.getResourcesManager();

        var sans = new Font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.SANS
                , res.getData("default/fonts/Roboto-Regular.ttf"));
        install(sans);
        var bold = new Font("Roboto", FontWeight.BOLD, FontPosture.REGULAR, FontStyle.SANS
                , res.getData("default/fonts/Roboto-Bold.ttf"));
        install(bold);
        var italic = new Font("Roboto", FontWeight.NORMAL, FontPosture.ITALIC, FontStyle.SANS
                , res.getData("default/fonts/Roboto-Italic.ttf"));
        install(italic);
        var bolditalic = new Font("Roboto", FontWeight.BOLD, FontPosture.ITALIC, FontStyle.SANS
                , res.getData("default/fonts/Roboto-BoldItalic.ttf"));
        install(bolditalic);
        var serif = new Font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.SERIF
                , res.getData("default/fonts/RobotoSerif-Regular.ttf"));
        install(serif);
        var mono = new Font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.MONO
                , res.getData("default/fonts/RobotoMono-Regular.ttf"));
        install(mono);
        var cursive = new Font("DancingScript", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.CURSIVE
                , res.getData("default/fonts/DancingScript-Regular.ttf"));
        install(cursive);
        var emoji = new Font("NotoEmoji", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.EMOJI
                , res.getData("default/fonts/NotoEmoji-Regular.ttf"));
        install(emoji);

        DefaultFont = sans;
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
        return size <= 0 ? 0 : SVG.FontGetTextWidth(fontID, text, size / this.size, spacing);
    }

    public float getWidth(Buffer text, int offset, int length, float size, float spacing) {
        checkDisposed();
        return size <= 0 ? 0 : SVG.FontGetTextWidthBuffer(fontID, text, offset, length, size / this.size, spacing);
    }

    public CaretData getCaretOffset(String text, float size, float spacing, float x, boolean half) {
        checkDisposed();
        if (size < 0) {
            return new CaretData(0, 0);
        } else {
            float[] data = new float[2];
            SVG.FontGetOffset(fontID, text, size / this.size, spacing, x, half, data);
            return new CaretData((int) data[0], data[1]);
        }
    }

    public CaretData getCaretOffset(Buffer text, int offset, int length, float size, float spacing, float x, boolean half) {
        checkDisposed();
        if (size < 0) {
            return new CaretData(0, 0);
        } else {
            float[] data = new float[2];
            SVG.FontGetOffsetBuffer(fontID, text, offset, length, size / this.size, spacing, x, half, data);
            return new CaretData((int) data[0], data[1]);
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
            SVG.FontGetAllCodePoints(fontID, glyphs);
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
        SVG.FontGetGlyph(fontID, codePoint, readGlyph);
        return new Glyph(readGlyph[0], readGlyph[1], readGlyph[2], readGlyph[3], readGlyph[4]);
    }

    public Path getGlyphPathByIndex(int glyphId) {
        return getGlyphPath(getGlyphCodePoint(glyphId));
    }

    public Path getGlyphPath(int codePoint) {
        checkDisposed();
        float[] polygon = SVG.FontGetGlyphShape(fontID, codePoint);
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

    public PixelMap createImageFromAtlas(Context context) {
        int[] data = new int[4];
        int imageId = (int) SVG.FontPaintGetAtlas(getInternalPaintID(context), data);
        int w = data[0];
        int h = data[1];
        byte[] imageData = new byte[w * h];
        if (imageId == 0) {
            return new PixelMap(imageData, w, h, PixelFormat.RED);
        } else {
            Texture2D tex = new Texture2D(context, imageId, w, h, 0, PixelFormat.RED);
            tex.begin(0);
            tex.getData(0, imageData, 0);
            tex.end();
            return new PixelMap(imageData, w, h, PixelFormat.RED);
        }
    }

    private void checkDisposed() {
        if (diposed) {
            throw new RuntimeException("Font is disposed.");
        }
    }

    public boolean isDisposed() {
        return diposed;
    }

    public void dispose() {
        if (this == DefaultFont) {
            throw new RuntimeException("Default font cannot be disposed.");
        }

        if (!diposed) {
            diposed = true;
            fonts.remove(this);
            autoCleaner.run();
        }
    }

    public long getInternalID() {
        return fontID;
    }

    public long getInternalPaintID(final Context context) {
        if (context == null) return 0;

        FontRender render = contextInternal.get(context);
        if (render == null) {
            final long paintID = SVG.FontPaintCreate(fontID);

            FontRender fontRender = new FontRender();
            fontRender.paintID = paintID;
            fontRender.destroyTask = context.createSyncDestroyTask(() -> {
                SVG.FontPaintDestroy(paintID);
                contextInternal.remove(context);
            });

            contextInternal.put(context, fontRender);

            return paintID;
        }
        return render.paintID;
    }

    @Override
    public String toString() {
        return family
                + (posture == null ? "" : ", " + posture)
                + (weight == null ? "" : ", " + weight)
                + (style == null ? "" : ", " + style);
    }

    private static class FontRender {
        long paintID;
        Runnable destroyTask;
    }

    private static class AutoCleaner implements Runnable {
        private final long fontID;
        private HashMap<Context, FontRender> fontRenderer;
        private boolean consumed;

        public AutoCleaner(HashMap<Context, FontRender> fontRenderer, long fontID) {
            this.fontRenderer = fontRenderer;
            this.fontID = fontID;
        }

        @Override
        public void run() {
            if (!consumed) {
                consumed = true;
                Application.runVsync(() -> {
                    var values = new ArrayList<>(fontRenderer.values());
                    for (var render : values) {
                        render.destroyTask.run();
                    }
                    fontRenderer.clear();
                    fontRenderer = null;
                    SVG.FontUnload(fontID);
                });
            }
        }
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