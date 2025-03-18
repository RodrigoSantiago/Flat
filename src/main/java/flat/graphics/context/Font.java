package flat.graphics.context;

import flat.backend.SVG;
import flat.exception.FlatException;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.context.fonts.FontDetail;
import flat.graphics.context.fonts.WindowsSystemFonts;
import flat.graphics.image.PixelMap;
import flat.graphics.text.FontPosture;
import flat.graphics.text.FontStyle;
import flat.graphics.text.FontWeight;
import flat.math.shapes.Path;
import flat.window.Application;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.nio.Buffer;
import java.nio.file.Files;
import java.util.*;

public class Font {
    protected final static Cleaner cleaner = Cleaner.create();

    private static final ArrayList<Font> fonts = new ArrayList<>();
    private static final ArrayList<Font> defaultFonts = new ArrayList<>();
    private static final HashMap<String, ArrayList<Font>> fontFamilies = new HashMap<>();

    private static Font DefaultFont;
    private static final float[] readGlyph = new float[5];

    private final String family;
    private final FontPosture posture;
    private final FontWeight weight;
    private final FontStyle style;

    private final long fontId;
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
            throw new FlatException("Invalid font data");
        }

        this.family = family;
        this.weight = weight;
        this.posture = posture;
        this.style = style;
        this.size = size;
        this.sdf = sdf;

        final long fontID = SVG.FontLoad(data, size, sdf ? 1 : 0);
        if (fontID == 0) {
            throw new FlatException("Invalid font data");
        }
        this.fontId = fontID;
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
        return findFont(family, null, null, null);
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

        ArrayList<Font> fontFamily;
        if (family == null) {
            fontFamily = defaultFonts;
        } else {
            fontFamily = fontFamilies.get(family);
            if (fontFamily == null) {
                fontFamily = defaultFonts;
            }
        }
        if (weight == null) weight = FontWeight.NORMAL;
        if (posture == null) posture = FontPosture.REGULAR;
        if (style == null) style = FontStyle.SANS;

        Font closer = fontFamily.get(0);
        for (Font font : fontFamily) {
            if (getFontDifference(weight, posture, style,
                    closer.getWeight(), closer.getPosture(), closer.getStyle(),
                    font.getWeight(), font.getPosture(), font.getStyle()) == 2) {
                closer = font;
            }

        }
        return closer;
    }

    private static int getFontDifference(
            FontWeight weightT, FontPosture postureT, FontStyle styleT,
            FontWeight weightA, FontPosture postureA, FontStyle styleA,
            FontWeight weightB, FontPosture postureB, FontStyle styleB) {

        if (styleA != styleB) {
            if (styleA == styleT) return 1;
            if (styleB == styleT) return 2;
            if (styleA.ordinal() < styleB.ordinal()) return 1;
            return 2;
        }

        if (postureA != postureB) {
            if (postureA == postureT) return 1;
            if (postureB == postureT) return 2;
            if (postureA.ordinal() < postureB.ordinal()) return 1;
            return 2;
        }

        if (weightA != weightB) {
            if (weightA == weightT) return 1;
            if (weightB == weightT) return 2;
            int wA = Math.abs(weightA.getWeight() - weightT.getWeight());
            int wB = Math.abs(weightA.getWeight() - weightT.getWeight());
            if (wA != wB) return wA < wB ? 1 : 2;
            if (weightA.ordinal() < weightB.ordinal()) return 1;
            return 2;
        }

        return 1;
    }

    public static void install(Font font) {
        font.checkDisposed();

        if (fonts.contains(font)) return;

        fonts.add(font);

        var family = fontFamilies.get(font.family);
        if (family == null) {
            family = new ArrayList<>();
            fontFamilies.put(font.family, family);
        }
        family.add(font);
    }

    public static void installSystemFontFamily(String fontFamily) {
        readDefaultFonts();

        ArrayList<FontDetail> list = WindowsSystemFonts.listSystemFontFamilies().get(fontFamily);
        if (list == null) {
            return;
        }

        for (var detail : list) {
            ArrayList<Font> fontList = fontFamilies.get(fontFamily);
            if (fontList == null) {
                fontList = new ArrayList<>();
                fontFamilies.put(fontFamily, fontList);
            } else {
                Font instFont = findFont(fontFamily, detail.getWeight(), detail.getPosture(), detail.getStyle());
                if (instFont.getFamily().equals(fontFamily) &&
                        instFont.getWeight() == detail.getWeight() &&
                        instFont.getPosture() == detail.getPosture() &&
                        instFont.getStyle() == detail.getStyle()) {
                    continue;
                }
            }

            Font font = createSystemFont(detail);
            if (font != null) {
                install(font);
            }
        }
    }

    public static ArrayList<FontDetail> listSystemFonts() {
        readDefaultFonts();

        ArrayList<FontDetail> list = new ArrayList<>();
        for (var entry : WindowsSystemFonts.listSystemFontFamilies().values()) {
            list.addAll(entry);
        }
        return list;
    }

    public static Font createSystemFont(FontDetail fontDetail) {
        readDefaultFonts();

        try {
            byte[] data = Files.readAllBytes(fontDetail.getFile().toPath());
            return new Font(fontDetail.getFamily(), fontDetail.getWeight(), fontDetail.getPosture(), fontDetail.getStyle(), data);
        } catch (IOException e) {
            return null;
        }
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
        defaultFonts.addAll(List.of(sans, bold, italic, bolditalic, serif, mono, cursive));
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
        return size <= 0 ? 0 : SVG.FontGetTextWidth(fontId, text, size / this.size, spacing);
    }

    public float getWidth(Buffer text, int offset, int length, float size, float spacing) {
        checkDisposed();
        return size <= 0 ? 0 : SVG.FontGetTextWidthBuffer(fontId, text, offset, length, size / this.size, spacing);
    }

    public CaretData getCaretOffset(String text, float size, float spacing, float x, boolean half) {
        checkDisposed();
        if (size < 0) {
            return new CaretData(0, 0);
        } else {
            float[] data = new float[2];
            SVG.FontGetOffset(fontId, text, size / this.size, spacing, x, half, data);
            return new CaretData((int) data[0], data[1]);
        }
    }

    public CaretData getCaretOffset(Buffer text, int offset, int length, float size, float spacing, float x, boolean half) {
        checkDisposed();
        if (size < 0) {
            return new CaretData(x > 0 ? length : 0, 0);
        } else {
            float[] data = new float[2];
            SVG.FontGetOffsetBuffer(fontId, text, offset, length, size / this.size, spacing, x, half, data);
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
            SVG.FontGetAllCodePoints(fontId, glyphs);
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
        SVG.FontGetGlyph(fontId, codePoint, readGlyph);
        return new Glyph(readGlyph[0], readGlyph[1], readGlyph[2], readGlyph[3], readGlyph[4]);
    }

    public Path getGlyphPathByIndex(int glyphId) {
        return getGlyphPath(getGlyphCodePoint(glyphId));
    }

    public Path getGlyphPath(int codePoint) {
        checkDisposed();
        float[] polygon = SVG.FontGetGlyphShape(fontId, codePoint);
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
            tex.getData(0, imageData, 0);
            context.bindTexture(null);
            return new PixelMap(imageData, w, h, PixelFormat.RED);
        }
    }

    private void checkDisposed() {
        if (diposed) {
            throw new FlatException("The Font is disposed");
        }
    }

    public boolean isDisposed() {
        return diposed;
    }

    public void dispose() {
        if (defaultFonts.contains(this)) {
            throw new FlatException("A default font cannot be disposed");
        }

        if (!diposed) {
            diposed = true;
            fonts.remove(this);
            autoCleaner.run();
        }
    }

    public long getInternalID() {
        return fontId;
    }

    public long getInternalPaintID(final Context context) {
        if (context == null) return 0;

        FontRender render = contextInternal.get(context);
        if (render == null) {
            final long paintId = SVG.FontPaintCreate(context.svgId, fontId);

            FontRender fontRender = new FontRender();
            fontRender.paintId = paintId;
            fontRender.destroyTask = context.createSyncDestroyTask(() -> {
                SVG.FontPaintDestroy(paintId);
                contextInternal.remove(context);
            });

            contextInternal.put(context, fontRender);

            return paintId;
        }
        return render.paintId;
    }

    @Override
    public String toString() {
        return family
                + (posture == null ? "" : ", " + posture)
                + (weight == null ? "" : ", " + weight)
                + (style == null ? "" : ", " + style);
    }

    private static class FontRender {
        long paintId;
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
                Application.runOnContextSync(() -> {
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