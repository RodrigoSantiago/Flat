package flat.graphics.context.enums;

import flat.backend.SVGEnums;

public enum LineJoin {
    MITER(SVGEnums.SVG_MITER),
    ROUND(SVGEnums.SVG_ROUND),
    BEVEL(SVGEnums.SVG_BEVEL);

    private final int svgEnum;
    LineJoin(int svgEnum) {
        this.svgEnum = svgEnum;
    }

    public int getInternalEnum() {
        return svgEnum;
    }
}
