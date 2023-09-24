package flat.graphics.context.enuns;

import flat.backend.SVGEnuns;

public enum LineJoin {
    MITER(SVGEnuns.SVG_MITER),
    ROUND(SVGEnuns.SVG_ROUND),
    BEVEL(SVGEnuns.SVG_BEVEL);

    private final int svgEnum;
    LineJoin(int svgEnum) {
        this.svgEnum = svgEnum;
    }

    public int getInternalEnum() {
        return svgEnum;
    }
}
