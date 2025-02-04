package flat.graphics.context;

import flat.backend.SVG;
import flat.graphics.text.FontPosture;
import flat.graphics.text.FontStyle;
import flat.graphics.text.FontWeight;
import flat.window.Application;

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
    private int loadState;
    private boolean disposedAssigned;
    private boolean disposed;

    private HashMap<Context, FontRender> contextInternal = new HashMap<>();

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

        this.fontID = SVG.FontLoad(data, size, sdf ? 1 : 0);
        if (this.fontID == 0) {
            throw new RuntimeException("Invalid font data");
        }
        this.height = SVG.FontGetHeight(fontID);
        this.ascent = SVG.FontGetAscent(fontID);
        this.descent = SVG.FontGetDescent(fontID);
        this.lineGap = SVG.FontGetLineGap(fontID);
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
        checkInternalLoadState(text);
        return SVG.FontGetTextWidth(fontID, text, size / this.size, spacing);
    }

    public float getWidth(ByteBuffer text, int offset, int length, float size, float spacing) {
        loadAllGlyphs();
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
        checkInternalLoadState(text);
        return SVG.FontGetOffset(fontID, text, size / this.size, spacing, x, half);
    }

    public int getOffset(ByteBuffer text, int offset, int length, float size, float spacing, float x, boolean half) {
        loadAllGlyphs();
        return SVG.FontGetOffsetBuffer(fontID, text, offset, length, size / this.size, spacing, x, half);
    }

    private void checkDisposed() {
        if (disposed) {
            throw new RuntimeException("Font is disposed.");
        }
    }

    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        if (this == DefaultFont) {
            throw new RuntimeException("Default font cannot be disposed.");
        }

        if (!disposedAssigned) {
            disposedAssigned = true;
            Application.runVsync(() -> {
                disposed = true;
                fonts.remove(this);
                for (var render : contextInternal.values()) {
                    render.destroyTask.run();
                }
                contextInternal.clear();
                SVG.FontUnload(fontID);
            });
        }
    }

    public int getInternalLoadState() {
        return loadState;
    }

    public long getInternalID(Context context) {
        checkDisposed();

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

    private static String asc;

    private static String getASCI() {
        if (asc == null) {
            StringBuilder asciiChars = new StringBuilder();
            for (int i = 0; i < 128; i++) {
                asciiChars.append((char) i);
            }
            asc = asciiChars.toString();
        }
        return asc;
    }

    public void checkInternalLoadState(String text) {
        if (loadState < 3) {
            int state;
            if (text.length() == 0) {
                state = 0;
            } else if (text.equals("AaBbYyZz")) {
                state = 1;
            } else {
                state = 2;
                for (int i = 0; i < text.length(); i++) {
                    if (text.charAt(i) >= 128) {
                        state = 3;
                        break;
                    }
                }
            }
            if (state > loadState) {
                if (state == 1) {
                    SVG.FontLoadGlyphs(fontID, "AaBbYyZz", state);
                    loadState = 1;
                } else if (state == 2) {
                    Context.propagateHardFlush();
                    SVG.FontLoadGlyphs(fontID, getASCI(), state);
                    loadState = 2;
                } else {
                    Context.propagateHardFlush();
                    SVG.FontLoadAllGlyphs(fontID);
                    loadState = 3;
                }
            }
        }
    }

    public void loadAllGlyphs() {
        checkDisposed();

        if (loadState < 3) {
            Context.propagateHardFlush();
            SVG.FontLoadAllGlyphs(fontID);
            loadState = 3;
        }
    }

    @Override
    public String toString() {
        return family
                + (posture == null ? "" : ", " + posture)
                + (weight == null ? "" : ", " + weight)
                + (style == null ? "" : ", " + style);
    }

    private class FontRender {
        long paintID;
        Runnable destroyTask;
    }
}