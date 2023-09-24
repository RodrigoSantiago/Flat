package flat.graphics.context;

import flat.backend.SVG;
import flat.graphics.text.FontPosture;
import flat.graphics.text.FontWeight;
import flat.widget.Application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public final class Font {
    private final String family;
    private final FontPosture posture;
    private final FontWeight weight;
    private float size, height, ascent, descent, lineGap;
    private boolean sdf;
    private boolean loaded;
    private byte[] data;

    private long fontID;

    private static final ArrayList<Font> fonts = new ArrayList<>();

    public static final Font DEFAULT;
    public static final Font DEFAULT_BOLD;
    public static final Font DEFAULT_ITALIC;
    public static final Font SERIF;
    public static final Font SANS_SERIF;
    public static final Font MONOSPACE;
    public static final Font CURSIVE;
    // CURSIVE

    static {
        DEFAULT = addFont("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, Font.class.getResourceAsStream("/fonts/Roboto-Regular.ttf"));
        DEFAULT_BOLD = addFont("Roboto", FontWeight.BOLD, FontPosture.REGULAR, Font.class.getResourceAsStream("/fonts/Roboto-Bold.ttf"));
        DEFAULT_ITALIC = addFont("Roboto", FontWeight.NORMAL, FontPosture.ITALIC, Font.class.getResourceAsStream("/fonts/Roboto-Italic.ttf"));
        SERIF = addFont("Serif", FontWeight.NORMAL, FontPosture.REGULAR, Font.class.getResourceAsStream("/fonts/DroidSerif-Regular.ttf"));
        SANS_SERIF = addFont("Sans", FontWeight.NORMAL, FontPosture.REGULAR, Font.class.getResourceAsStream("/fonts/DroidSans-Regular.ttf"));
        MONOSPACE = addFont("Mono", FontWeight.NORMAL, FontPosture.REGULAR, Font.class.getResourceAsStream("/fonts/DroidSans-Mono.ttf"));
        CURSIVE = addFont("Cursive", FontWeight.NORMAL, FontPosture.REGULAR, Font.class.getResourceAsStream("/fonts/DancingScript-Regular.ttf"));
    }

    public static Font findFont(String family) {
        return findFont(family, FontWeight.NORMAL);
    }

    public static Font findFont(String family, FontWeight weight) {
        return findFont(family,weight, FontPosture.REGULAR);
    }

    public static synchronized Font findFont(String family, FontWeight weight, FontPosture posture) {
        Font closer = null;
        for (Font font : fonts) {
            if (font.family.equalsIgnoreCase(family)) {
                if (closer == null) {
                    closer = font;
                } else if (closer.posture != posture && font.posture == posture) {
                    closer = font;
                } else if (font.posture == posture) {
                    if (Math.abs(font.weight.getWeight() - weight.getWeight()) <
                            Math.abs(closer.weight.getWeight() - weight.getWeight())) {
                        closer = font;
                    }
                }
            }
        }
        return closer;
    }

    public static Font addFont(String family, FontWeight weight, FontPosture posture, InputStream is) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return Font.addFont(family, weight, posture, buffer.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            // todo - add handler
        }
        return null;
    }

    public static synchronized Font addFont(String family, FontWeight weight, FontPosture posture, byte[] data) {
        Font found = findFont(family, weight, posture);
        Font font = new Font(family, weight, posture, data);

        if (found != null && found.weight == weight && found.posture == posture) {
            fonts.remove(found);
        }

        fonts.add(font);

        return font;
    }

    public static synchronized Font addFont(Font font) {
        Font found = findFont(font.getFamily(), font.getWeight(), font.getPosture());

        if (found != null && found.weight == font.getWeight() && found.posture == font.getPosture()) {
            fonts.remove(found);
        }

        fonts.add(font);

        return font;
    }

    public Font(String family, FontWeight weight, FontPosture posture, byte[] data) {
        this(family, weight, posture, data, 48, true);
    }

    public Font(String family, FontWeight weight, FontPosture posture, byte[] data, float size, boolean sdf) {
        this.family = family;
        this.weight = weight;
        this.posture = posture;
        this.size = size;
        this.sdf = sdf;
        this.data = data.clone();
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
        load();
        return SVG.FontGetTextWidth(fontID, text, size / this.size, spacing);
    }

    public float getWidth(ByteBuffer text, int offset, int length, float size, float spacing) {
        load();
        return SVG.FontGetTextWidthBuffer(fontID, text, offset, length, size / this.size, spacing);
    }

    public float getSize() {
        return size;
    }

    public float getHeight(float size) {
        load();
        return size * height / this.size;
    }

    public float getLineGap(float size) {
        load();
        return size * lineGap / this.size;
    }

    public float getAscent(float size) {
        load();
        return size * ascent / this.size;
    }

    public float getDescent(float size) {
        load();
        return size * descent / this.size;
    }

    public int getOffset(String text, float size, float spacing, float x, boolean half) {
        load();
        return SVG.FontGetOffset(fontID, text, size / this.size, spacing, x, half);
    }

    public int getOffset(ByteBuffer text, int offset, int length, float size, float spacing, float x, boolean half) {
        load();
        return SVG.FontGetOffsetBuffer(fontID, text, offset, length, size / this.size, spacing, x, half);
    }

    public long getInternalID() {
        load();
        return fontID;
    }

    private void load() {
        synchronized (Font.class) {
            if (!loaded) {
                this.fontID = SVG.FontCreate(data, size, sdf ? 1 : 0);
                this.height = SVG.FontGetHeight(fontID);
                this.ascent = SVG.FontGetAscent(fontID);
                this.descent = SVG.FontGetDescent(fontID);
                this.lineGap = SVG.FontGetLineGap(fontID);
                SVG.FontLoadAllGlyphs(fontID);

                this.data = null;
                loaded = true;
            }
        }
    }

    @Override
    protected void finalize() {
        if (loaded) {
            final long fontID = this.fontID;

            Application.runSync(() -> SVG.FontDestroy(fontID));
        }
    }

    @Override
    public int hashCode() {
        return family.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj instanceof Font) {
            Font other = (Font) obj;
            return other.family.equals(family) && other.weight == weight && other.posture == posture;
        } else {
            return false;
        }
    }
}