package flat.graphics.text;

import flat.backend.SVGEnuns;

public final class Align {

    public enum Vertical {
        TOP(SVGEnuns.SVG_TOP),
        MIDDLE(SVGEnuns.SVG_MIDDLE),
        BOTTOM(SVGEnuns.SVG_BASELINE),
        BASELINE(SVGEnuns.SVG_BOTTOM);

        private final int svgEnum;

        Vertical(int svgEnum) {
            this.svgEnum = svgEnum;
        }

        public int getInternalEnum() {
            return svgEnum;
        }
    }

    public enum Horizontal {
        LEFT(SVGEnuns.SVG_LEFT),
        CENTER(SVGEnuns.SVG_CENTER),
        RIGHT(SVGEnuns.SVG_RIGHT);

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
