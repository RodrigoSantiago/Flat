package flat.graphics.context.enuns;

import static flat.backend.SVGEnuns.*;

public enum LineJoin {
    MITER(SVG_MITER),
    ROUND(SVG_ROUND),
    BEVEL(SVG_BEVEL);

    private final int svgEnum;
    LineJoin(int svgEnum) {
        this.svgEnum = svgEnum;
    }

    public int getInternalEnum() {
        return svgEnum;
    }
}
