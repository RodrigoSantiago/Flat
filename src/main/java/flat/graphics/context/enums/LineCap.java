package flat.graphics.context.enums;

import flat.backend.SVGEnums;

public enum LineCap {
    BUTT(SVGEnums.SVG_BUTT),
    ROUND(SVGEnums.SVG_ROUND),
    SQUARE(SVGEnums.SVG_SQUARE);

    private final int svgEnum;
    LineCap(int svgEnum) {
        this.svgEnum = svgEnum;
    }

    public int getInternalEnum() {
        return svgEnum;
    }
}
