package flat.graphics.context.enuns;

import flat.backend.SVGEnuns;

public enum LineCap {
    BUTT(SVGEnuns.SVG_BUTT),
    ROUND(SVGEnuns.SVG_ROUND),
    SQUARE(SVGEnuns.SVG_SQUARE);

    private final int svgEnum;
    LineCap(int svgEnum) {
        this.svgEnum = svgEnum;
    }

    public int getInternalEnum() {
        return svgEnum;
    }
}
