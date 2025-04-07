package flat.graphics.context.fonts;

import flat.graphics.symbols.FontPosture;
import flat.graphics.symbols.FontStyle;
import flat.graphics.symbols.FontWeight;

import java.io.File;

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
}
