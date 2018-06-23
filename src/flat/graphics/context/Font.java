package flat.graphics.context;

import flat.backend.SVG;
import flat.graphics.text.FontPosture;
import flat.graphics.text.FontWeight;
import flat.widget.Application;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public final class Font {
    private final String family;
    private final FontPosture posture;
    private final FontWeight weight;

    private HashMap<Thread, Integer> fontIds = new HashMap<>();
    private byte[] data;

    private static final ArrayList<Font> fonts = new ArrayList<>();

    public static final Font DEFAULT;
    public static final Font DEFAULT_BOLD;
    public static final Font DEFAULT_ITALIC;
    public static final Font SERIF;
    public static final Font SANS_SERIF;
    public static final Font MONOSPACE;

    static {
        DEFAULT = addFont("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, Font.class.getResourceAsStream("/resources/fonts/Roboto-Regular.ttf"));
        DEFAULT_BOLD = addFont("Roboto", FontWeight.BOLD, FontPosture.REGULAR, Font.class.getResourceAsStream("/resources/fonts/Roboto-Bold.ttf"));
        DEFAULT_ITALIC = addFont("Roboto", FontWeight.NORMAL, FontPosture.ITALIC, Font.class.getResourceAsStream("/resources/fonts/Roboto-Italic.ttf"));
        SERIF = addFont("Serif", FontWeight.NORMAL, FontPosture.REGULAR, Font.class.getResourceAsStream("/resources/fonts/DroidSerif-Regular.ttf"));
        SANS_SERIF = addFont("Sans", FontWeight.NORMAL, FontPosture.REGULAR, Font.class.getResourceAsStream("/resources/fonts/DroidSans-Regular.ttf"));
        MONOSPACE = addFont("Mono", FontWeight.NORMAL, FontPosture.REGULAR, Font.class.getResourceAsStream("/resources/fonts/DroidSans-Mono.ttf"));
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
                } else if (closer.posture != posture) {
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

    private Font(String family, FontWeight weight, FontPosture posture, byte[] data) {
        this.family = family;
        this.weight = weight;
        this.posture = posture;
        this.data = data.clone();
    }

    public int getInternalID() {
        Integer index;
        synchronized (Font.class) {
            index = fontIds.get(Thread.currentThread());
        }
        if (index == null) {
            long svgId = Application.getCurrentContext().svgId;
            index = SVG.FontCreate(svgId, family + "-" + posture + "-" + weight, data);
            synchronized (Font.class) {
                fontIds.put(Thread.currentThread(), index);
            }
        }
        return index;
    }

    static void dispose(Thread thread) {
        synchronized (Font.class) {
            for (Font font : fonts) {
                font.fontIds.remove(thread);
            }
        }
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