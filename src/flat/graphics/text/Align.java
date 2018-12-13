package flat.graphics.text;

import static flat.backend.SVGEnuns.*;

public final class Align {

    public enum Vertical {
        TOP(SVG_TOP),
        MIDDLE(SVG_MIDDLE),
        BOTTOM(SVG_BASELINE),
        BASELINE(SVG_BOTTOM);

        private final int svgEnum;

        Vertical(int svgEnum) {
            this.svgEnum = svgEnum;
        }

        public int getInternalEnum() {
            return svgEnum;
        }
    }

    public enum Horizontal {
        LEFT(SVG_LEFT),
        CENTER(SVG_CENTER),
        RIGHT(SVG_RIGHT);

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
