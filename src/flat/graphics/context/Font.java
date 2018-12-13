package flat.graphics.context;

import flat.backend.SVG;
import flat.graphics.text.FontPosture;
import flat.graphics.text.FontWeight;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public final class Font {
    private final String family;
    private final FontPosture posture;
    private final FontWeight weight;

    private long fontID;

    private static final ArrayList<Font> fonts = new ArrayList<>();

    public static final Font DEFAULT;
    public static final Font DEFAULT_BOLD;
    public static final Font DEFAULT_ITALIC;
    public static final Font SERIF;
    public static final Font SANS_SERIF;
    public static final Font MONOSPACE;

    static {
        DEFAULT = addFont("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, Font.class.getResourceAsStream("/fonts/Roboto-Regular.ttf"));
        DEFAULT_BOLD = addFont("Roboto", FontWeight.BOLD, FontPosture.REGULAR, Font.class.getResourceAsStream("/fonts/Roboto-Bold.ttf"));
        DEFAULT_ITALIC = addFont("Roboto", FontWeight.NORMAL, FontPosture.ITALIC, Font.class.getResourceAsStream("/fonts/Roboto-Italic.ttf"));
        SERIF = addFont("Serif", FontWeight.NORMAL, FontPosture.REGULAR, Font.class.getResourceAsStream("/fonts/DroidSerif-Regular.ttf"));
        SANS_SERIF = addFont("Sans", FontWeight.NORMAL, FontPosture.REGULAR, Font.class.getResourceAsStream("/fonts/DroidSans-Regular.ttf"));
        MONOSPACE = addFont("Mono", FontWeight.NORMAL, FontPosture.REGULAR, Font.class.getResourceAsStream("/fonts/DroidSans-Mono.ttf"));
    }

    public static Font findFont(String family) {
        return findFont(family, FontWeight.NORMAL);
    }

    public static Font findFont(String family, FontWeight weight) {
        return findFont(family,weight, FontPosture.REGULAR);
    }

    public static Font findFont(String family, FontWeight weight, FontPosture posture) {
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
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        try {
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return Font.addFont(family,  weight, posture, buffer.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                buffer.close();
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    public static Font addFont(String family, FontWeight weight, FontPosture posture, byte[] data) {
        Font found = findFont(family, weight, posture);
        if (found == null || found.weight != weight || found.posture != posture) {
            Font font = new Font(family, weight, posture, data);
            synchronized (Font.class) {
                fonts.add(font);
            }
            return font;
        } else  {
            return found;
        }
    }

    public Font(String family, FontWeight weight, FontPosture posture, byte[] data) {
        this.family = family;
        this.weight = weight;
        this.posture = posture;
        fontID = SVG.FontCreate(data, 48, 1);
        SVG.FontLoadAllGlyphs(fontID);
    }

    public long getInternalID() {
        return fontID;
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

    public boolean isBold() {
        return weight.getWeight() >= FontWeight.BOLD.getWeight();
    }

    public boolean isItalic() {
        return posture == FontPosture.ITALIC;
    }

    @Override
    protected void finalize() {
        SVG.FontDestroy(fontID);
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