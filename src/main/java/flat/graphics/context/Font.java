package flat.graphics.context;

import flat.backend.SVG;
import flat.graphics.text.FontPosture;
import flat.graphics.text.FontStyle;
import flat.graphics.text.FontWeight;
import flat.window.Application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class Font {

    private final String family;
    private final FontPosture posture;
    private final FontWeight weight;
    private final FontStyle style;

    private final long fontID;
    private final float size, height, ascent, descent, lineGap;
    private final boolean sdf;
    private final byte[] data;

    private HashMap<Context, Long> contextInternal = new HashMap<>();

    private static final ArrayList<Font> fonts = new ArrayList<>();

    private static Font DefaultFont;

    public Font(String family, FontWeight weight, FontPosture posture, FontStyle style, byte[] data) {
        this(family, weight, posture, style, data, 48, true);
    }

    public Font(String family, FontWeight weight, FontPosture posture, FontStyle style, byte[] data, float size, boolean sdf) {
        this.family = family;
        this.weight = weight;
        this.posture = posture;
        this.style = style;
        this.size = size;
        this.sdf = sdf;
        this.data = data.clone();

        this.fontID = SVG.FontCreate(data, size, sdf ? 1 : 0);
        if (this.fontID == 0) {
            throw new RuntimeException("Invalid font data");
        }

        this.height = SVG.FontGetHeight(fontID);
        this.ascent = SVG.FontGetAscent(fontID);
        this.descent = SVG.FontGetDescent(fontID);
        this.lineGap = SVG.FontGetLineGap(fontID);
        SVG.FontLoadAllGlyphs(fontID);
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

    public static synchronized Font findFont(String family, FontWeight weight, FontPosture posture, FontStyle style) {
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
        int f = family == null ? 0 : this.family.equalsIgnoreCase(family) ? 1 : 0;
        int s = style == null ? 0 : this.style == style ? 1 : 0;
        int p = posture == null ? 0 : this.posture == posture ? 1 : 0;
        int w = weight == null ? 0 : Math.abs(this.weight.getWeight() - weight.getWeight());

        return f * 8000 + s * 4000 + p * 2000 + w;
    }

    public static synchronized void install(Font font) {
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

        var sans = new Font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.SANS, res.getData("/fonts/Roboto-Regular.ttf"));
        install(sans);
        var bold = new Font("Roboto", FontWeight.BOLD, FontPosture.REGULAR, FontStyle.SANS, res.getData("/fonts/Roboto-Bold.ttf"));
        install(bold);
        var italic = new Font("Roboto", FontWeight.NORMAL, FontPosture.ITALIC, FontStyle.SANS, res.getData("/fonts/Roboto-Italic.ttf"));
        install(italic);
        var bolditalic = new Font("Roboto", FontWeight.BOLD, FontPosture.ITALIC, FontStyle.SANS, res.getData("/fonts/Roboto-BoldItalic.ttf"));
        install(bolditalic);

        var serif = new Font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.SERIF, res.getData("/fonts/RobotoSerif-Regular.ttf"));
        install(serif);
        var mono = new Font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.MONO, res.getData("/fonts/RobotoMono-Regular.ttf"));
        install(mono);
        var cursive = new Font("DancingScript", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.CURSIVE, res.getData("/fonts/DancingScript-Regular.ttf"));
        install(cursive);

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
        return SVG.FontGetTextWidth(fontID, text, size / this.size, spacing);
    }

    public float getWidth(ByteBuffer text, int offset, int length, float size, float spacing) {
        return SVG.FontGetTextWidthBuffer(fontID, text, offset, length, size / this.size, spacing);
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

    public int getOffset(String text, float size, float spacing, float x, boolean half) {
        return SVG.FontGetOffset(fontID, text, size / this.size, spacing, x, half);
    }

    public int getOffset(ByteBuffer text, int offset, int length, float size, float spacing, float x, boolean half) {
        return SVG.FontGetOffsetBuffer(fontID, text, offset, length, size / this.size, spacing, x, half);
    }

    public long getInternalID(Context context) {
        Long internalId = contextInternal.get(context);
        if (internalId == null) {
            final long id = SVG.FontCreate(data, size, sdf ? 1 : 0);
            SVG.FontLoadAllGlyphs(id);

            contextInternal.put(context, id);
            context.createSyncDestroyTask(() -> {
                SVG.FontDestroy(id);
                contextInternal.remove(context);
            });
            return id;
        }
        return internalId;
    }
}