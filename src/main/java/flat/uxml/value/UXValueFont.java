package flat.uxml.value;

import flat.graphics.context.Font;
import flat.graphics.text.FontPosture;
import flat.graphics.text.FontStyle;
import flat.graphics.text.FontWeight;
import flat.uxml.UXTheme;

import java.util.Objects;

public class UXValueFont extends UXValue {
    private final String family;
    private final FontWeight weight;
    private final FontPosture posture;
    private final FontStyle style;

    public UXValueFont(String family, FontWeight weight, FontPosture posture, FontStyle style) {
        this.family = family;
        this.weight = weight;
        this.posture = posture;
        this.style = style;
    }

    @Override
    public Font asFont(UXTheme theme) {
        return Font.findFont(family, weight, posture, style);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UXValueFont that = (UXValueFont) o;
        return Objects.equals(family, that.family)
                && weight == that.weight
                && posture == that.posture
                && style == that.style;
    }

    @Override
    public int hashCode() {
        return Objects.hash(family, weight, posture, style);
    }

    @Override
    public String toString() {
        return "Font : " + family + " " + weight + " " + posture + " " + style;
    }
}
