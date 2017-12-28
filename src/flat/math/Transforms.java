package flat.math;

/**
 * {@link Transform} related utility methods.
 */
public class Transforms {

    /**
     * Creates and returns a new shape that is the supplied shape transformed by this transform's
     * matrix.
     */
    public static IShape createTransformedShape (Transform t, IShape src) {
        if (src == null) {
            return null;
        }
        if (src instanceof Path) {
            return ((Path)src).createTransformedShape(t);
        }
        PathIterator path = src.pathIterator(t);
        Path dst = new Path(path.windingRule());
        dst.append(path, false);
        return dst;
    }

    /**
     * Multiplies the supplied two affine transforms, storing the result in {@code into}. {@code
     * into} may refer to the same instance as {@code a} or {@code b}.
     * @return {@code into} for chaining.
     */
    public static <T extends Transform> T multiply (Affine a, Affine b, T into) {
        return multiply(a.m00, a.m01, a.m10, a.m11, a.tx, a.ty,
                        b.m00, b.m01, b.m10, b.m11, b.tx, b.ty, into);
    }

    /**
     * Multiplies the supplied two affine transforms, storing the result in {@code into}. {@code
     * into} may refer to the same instance as {@code a}.
     * @return {@code into} for chaining.
     */
    public static <T extends Transform> T multiply (
            Affine a, float m00, float m01, float m10, float m11, float tx, float ty, T into) {
        return multiply(a.m00, a.m01, a.m10, a.m11, a.tx, a.ty, m00, m01, m10, m11, tx, ty, into);
    }

    /**
     * Multiplies the supplied two affine transforms, storing the result in {@code into}. {@code
     * into} may refer to the same instance as {@code b}.
     * @return {@code into} for chaining.
     */
    public static <T extends Transform> T multiply (
            float m00, float m01, float m10, float m11, float tx, float ty, Affine b, T into) {
        return multiply(m00, m01, m10, m11, tx, ty, b.m00, b.m01, b.m10, b.m11, b.tx, b.ty, into);
    }

    /**
     * Multiplies the supplied two affine transforms, storing the result in {@code into}.
     * @return {@code into} for chaining.
     */
    public static <T extends Transform> T multiply (
        float am00, float am01, float am10, float am11, float atx, float aty,
        float bm00, float bm01, float bm10, float bm11, float btx, float bty, T into) {
        into.setTransform(am00 * bm00 + am10 * bm01,
                          am01 * bm00 + am11 * bm01,
                          am00 * bm10 + am10 * bm11,
                          am01 * bm10 + am11 * bm11,
                          am00 *  btx + am10 *  bty + atx,
                          am01 *  btx + am11 *  bty + aty);
        return into;
    }
}
