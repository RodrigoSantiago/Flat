package flat.graphics.context.fonts;

import flat.graphics.symbols.FontPosture;
import flat.graphics.symbols.FontStyle;
import flat.graphics.symbols.FontWeight;

import java.io.File;
import java.util.Comparator;

public class FontDetail {
    private final File file;
    private final String family;
    private final FontPosture posture;
    private final FontWeight weight;
    private final FontStyle style;

    public FontDetail(File file, String family, FontPosture posture, FontWeight weight, FontStyle style) {
        this.file = file;
        this.family = family;
        this.posture = posture;
        this.weight = weight;
        this.style = style;
    }

    public File getFile() {
        return file;
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

    @Override
    public String toString() {
        return "{" +
                "'" + family + '\'' +
                ", " + style +
                ", " + posture +
                ", " + weight + '}';
    }

    public int compareTo(FontDetail o2) {
        int result;

        result = getFamily().compareToIgnoreCase(o2.getFamily());
        if (result != 0) return result;

        result = getStyle().compareTo(o2.getStyle());
        if (result != 0) return result;

        result = getPosture().compareTo(o2.getPosture());
        if (result != 0) return result;

        return getWeight().compareTo(o2.getWeight());
    }
}
