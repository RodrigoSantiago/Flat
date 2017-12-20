package flat.graphics.paint;

import static flat.backend.SVGEnuns.*;

public enum LineCap {
    BUTT(SVG_BUTT),
    ROUND(SVG_ROUND),
    SQUARE(SVG_SQUARE);

    private final int svgEnum;
    LineCap(int svgEnum) {
        this.svgEnum = svgEnum;
    }

    public int getInternalEnum() {
        return svgEnum;
    }
}
