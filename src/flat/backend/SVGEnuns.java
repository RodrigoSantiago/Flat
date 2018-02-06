package flat.backend;

public class SVGEnuns {

    // create flags
    public static final int SVG_ANTIALIAS = 1;
    public static final int SVG_STENCIL_STROKES = 1 << 1;
    public static final int SVG_DEBUG = 1 << 2;

    // winding
    public static final int SVG_CCW = 1;                    // Winding for solid shapes
    public static final int SVG_CW = 2;                     // Winding for holes

    // solidity
    public static final int SVG_SOLID = 1;
    public static final int SVG_HOLE = 2;

    // line stroker
    public static final int SVG_BUTT = 0;
    public static final int SVG_ROUND = 1;
    public static final int SVG_SQUARE = 2;
    public static final int SVG_BEVEL = 3;
    public static final int SVG_MITER = 4;

    // Horizontal Align
    public static final int SVG_ALIGN_LEFT = 1;             // Default, align text horizontally to left
    public static final int SVG_ALIGN_CENTER = 1 << 1;      // Align text horizontally to center
    public static final int SVG_ALIGN_RIGHT = 1 << 2;       // Align text horizontally to right

    // Vertical Align
    public static final int SVG_ALIGN_TOP = 1 << 3;         // Align text vertically to top
    public static final int SVG_ALIGN_MIDDLE = 1 << 4;      // Align text vertically to middle
    public static final int SVG_ALIGN_BOTTOM = 1 << 5;      // Align text vertically to bottom
    public static final int SVG_ALIGN_BASELINE = 1 << 6;    // Default, align text vertically to baseline

    // Blend Factor
    public static final int SVG_ZERO = 1;
    public static final int SVG_ONE = 1 << 1;
    public static final int SVG_SRC_COLOR = 1 << 2;
    public static final int SVG_ONE_MINUS_SRC_COLOR = 1 << 3;
    public static final int SVG_DST_COLOR = 1 << 4;
    public static final int SVG_ONE_MINUS_DST_COLOR = 1 << 5;
    public static final int SVG_SRC_ALPHA = 1 << 6;
    public static final int SVG_ONE_MINUS_SRC_ALPHA = 1 << 7;
    public static final int SVG_DST_ALPHA = 1 << 8;
    public static final int SVG_ONE_MINUS_DST_ALPHA = 1 << 9;
    public static final int SVG_SRC_ALPHA_SATURATE = 1 << 10;

    // Composite Operation
    public static final int SVG_SOURCE_OVER = 0;
    public static final int SVG_SOURCE_IN = 1;
    public static final int SVG_SOURCE_OUT = 2;
    public static final int SVG_ATOP = 3;
    public static final int SVG_DESTINATION_OVER = 4;
    public static final int SVG_DESTINATION_IN = 5;
    public static final int SVG_DESTINATION_OUT = 6;
    public static final int SVG_DESTINATION_ATOP = 7;
    public static final int SVG_LIGHTER = 8;
    public static final int SVG_COPY = 9;
    public static final int SVG_XOR = 10;
}
