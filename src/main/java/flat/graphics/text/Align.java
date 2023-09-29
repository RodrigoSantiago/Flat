package flat.graphics.text;

import flat.backend.SVGEnums;

public final class Align {

    public enum Vertical {
        TOP(SVGEnums.SVG_TOP),
        MIDDLE(SVGEnums.SVG_MIDDLE),
        BOTTOM(SVGEnums.SVG_BASELINE),
        BASELINE(SVGEnums.SVG_BOTTOM);

        private final int svgEnum;

        Vertical(int svgEnum) {
            this.svgEnum = svgEnum;
        }

        public int getInternalEnum() {
            return svgEnum;
        }
    }

    public enum Horizontal {
        LEFT(SVGEnums.SVG_LEFT),
        CENTER(SVGEnums.SVG_CENTER),
        RIGHT(SVGEnums.SVG_RIGHT);

        private final int svgEnum;

        Horizontal(int svgEnum) {
            this.svgEnum = svgEnum;
        }

        public int getInternalEnum() {
            return svgEnum;
        }
    }

    private Align() {
    }
}
