package flat.math;

/**
 * Implements the identity transform.
 */
public class IdentityTransform extends AbstractTransform {

    /** Identifies the identity transform in {@link #generality}. */
    public static final int GENERALITY = 0;

    @Override // from Transform
    public float uniformScale () {
        return 1;
    }

    @Override // from Transform
    public float scaleX () {
        return 1;
    }

    @Override // from Transform
    public float scaleY () {
        return 1;
    }

    @Override // from Transform
    public float rotation () {
        return 0;
    }

    @Override // from Transform
    public float tx () {
        return 0;
    }

    @Override // from Transform
    public float ty () {
        return 0;
    }

    @Override // from Transform
    public void get (float[] matrix) {
        matrix[0] = 1; matrix[1] = 0;
        matrix[2] = 0; matrix[3] = 1;
        matrix[4] = 0; matrix[5] = 0;
    }

    @Override // from Transform
    public Transform invert () {
        return this;
    }

    @Override // from Transform
    public Transform concatenate (Transform other) {
        return other;
    }

    @Override // from Transform
    public Transform preConcatenate (Transform other) {
        return other;
    }

    @Override // from Transform
    public Transform lerp (Transform other, float t) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override // from Transform
    public Point transform (IPoint p, Point into) {
        return into.set(p);
    }

    @Override // from Transform
    public void transform (IPoint[] src, int srcOff, Point[] dst, int dstOff, int count) {
        for (int ii = 0; ii < count; ii++) {
            transform(src[srcOff++], dst[dstOff++]);
        }
    }

    @Override // from Transform
    public void transform (float[] src, int srcOff, float[] dst, int dstOff, int count) {
        for (int ii = 0; ii < count; ii++) {
            dst[dstOff++] = src[srcOff++];
        }
    }

    @Override // from Transform
    public Point inverseTransform (IPoint p, Point into) {
        return into.set(p);
    }

    @Override // from Transform
    public Vector transformPoint (IVector v, Vector into) {
        return into.set(v);
    }

    @Override // from Transform
    public Vector transform (IVector v, Vector into) {
        return into.set(v);
    }

    @Override // from Transform
    public Vector inverseTransform (IVector v, Vector into) {
        return into.set(v);
    }

    @Override // from Transform
    public IdentityTransform copy () {
        return this;
    }

    @Override // from Transform
    public int generality () {
        return GENERALITY;
    }

    @Override
    public String toString () {
        return "ident";
    }
}
