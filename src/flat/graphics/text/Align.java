package flat.graphics.text;

import static flat.backend.SVGEnuns.*;

public final class Align {

    public enum Vertical {
        MIDDLE(SVG_ALIGN_MIDDLE),
        TOP(SVG_ALIGN_TOP),
        BOTTOM(SVG_ALIGN_BOTTOM),
        BASELINE(SVG_ALIGN_BASELINE);

        private final int svgEnum;

        Vertical(int svgEnum) {
            this.svgEnum = svgEnum;
        }

        public int getInternalEnum() {
            return svgEnum;
        }
    }

    public enum Horizontal {
        LEFT(SVG_ALIGN_LEFT),
        CENTER(SVG_ALIGN_CENTER),
        RIGHT(SVG_ALIGN_RIGHT);

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
