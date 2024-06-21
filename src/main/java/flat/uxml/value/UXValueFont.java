package flat.uxml.value;

import flat.graphics.context.Font;
import flat.graphics.text.FontPosture;
import flat.graphics.text.FontWeight;
import flat.uxml.UXTheme;
import flat.uxml.UXValue;

public class UXValueFont extends UXValue {
    private String generic;
    private String family;
    private FontWeight weight;
    private FontPosture posture;

    public UXValueFont(String generic, String family, FontWeight weight, FontPosture posture) {
        this.generic = generic;
        this.family = family;
        this.weight = weight;
        this.posture = posture;
    }

    @Override
    public Font asFont(UXTheme theme) {
        Font font;
        if (generic != null) {
            font = Font.findFont(generic, weight, posture);
            if (font != null) {
                return font;
            }
        }
        return Font.findFont(family, weight, posture);
    }
}
