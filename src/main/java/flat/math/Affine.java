package flat.math;

/**
 * Implements an affine (3x2 matrix) transform. The transformation matrix has the form:
 * [ m00, m10, m02 ]
 * [ m01, m11, m12 ]
 * [   0,   0,   1 ]
 */
public final class Affine {

    /**
     * The scale, rotation and shear components of this transform.
     */
    public float m00, m01, m10, m11;

    /**
     * The translation components of this transform.
     */
    public float m02, m12;

    /**
     * Creates an affine transform configured with the identity transform.
     */
    public Affine() {
        identity();
    }

    /**
     * Creates an affine transform from the supplied scale, rotation and translation.
     */
    public Affine(float scaleX, float scaleY, float angle, float x, float y) {
        set(scaleX, scaleY, angle, x, y);
    }

    /**
     * Creates an affine transform with the specified transform matrix.
     */
    public Affine(float m00, float m01, float m10, float m11, float m02, float m12) {
        set(m00, m01, m10, m11, m02, m12);
    }

    /**
     * Creates an affine transform with the specified affine transform.
     */
    public Affine(Affine other) {
        set(other);
    }

    /**
     * Returns a copy of this transform.
     */
    public Affine clone() {
        return new Affine(m00, m01, m10, m11, m02, m12);
    }

    /**
     * Sets this matrix to the identity matrix
     *
     * @return this instance, for chaining.
     */
    public Affine identity() {
        return set(1, 0, 0, 1, 0, 0);
    }

    /**
     * Check if this is an indentity matrix.
     *
     * @return True if translation is 0, scale is 1 and rotation is 0.
     * */
    public boolean isIdentity() {
        return m00 == 1 && m01 == 0 && m10 == 0 && m11 == 1 && m02 == 0 && m12 == 0;
    }

    /**
     * Check if this is equals to other affine transform.
     *
     * @return True if translation has all values equals
     * */
    public boolean isEquals(Affine other) {
        return m00 == other.m00 && m01 == other.m01 &&
               m10 == other.m10 && m11 == other.m11 &&
               m02 == other.m02 && m12 == other.m12;
    }

    /**
     * Check if this is a translation only transform. Scale = 1. Shear = 0.
     *
     * @return True if translation is a translation only
     * */
    public boolean isTranslationOnly() {
        return m00 == 1.0f && m11 == 1.0f && m01 == 0.0f && m10 == 0.0f;
    }

    /**
     * Sets the affine transform values with the supplied scale, rotation and translation.
     * @param scaleX The scale in the x-axis.
     * @param scaleY The scale in the y-axis.
     * @param angle The angle in degrees
     * @param x The x-component of the translation vector.
     * @param y The y-component of the translation vector.
     * @return this instance, for chaining.
     */
    public Affine set(float scaleX, float scaleY, float angle, float x, float y) {
        angle = Mathf.toRadians(angle);

        float sin = Mathf.sin(angle);
        float cos = Mathf.cos(angle);
        this.m00 = cos * scaleX;
        this.m01 = sin * scaleY;
        this.m10 = -sin * scaleX;
        this.m11 = cos * scaleY;
        this.m02 = x;
        this.m12 = y;

        return this;
    }

    /**
     * Sets the affine transform values with the specified transform matrix.
     *
     * @return this instance, for chaining.
     */
    public Affine set(float m00, float m01, float m10, float m11, float m02, float m12) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
        return this;
    }

    /**
     * Sets this affine transform matrix to {@code other}.
     *
     * @return this instance, for chaining.
     */
    public Affine set(Affine other) {
        return set(other.m00, other.m01, other.m10, other.m11, other.m02, other.m12);
    }

    /**
     * Returns the x-coordinate of the translation component.
     */
    public float translateX() {
        return this.m02;
    }

    /**
     * Returns the y-coordinate of the translation component.
     */
    public float translateY() {
        return this.m12;
    }

    /**
     * Returns the x-component of the scale applied by this transform.
     */
    public float scaleX() {
        return Mathf.sqrt(m00 * m00 + m01 * m01);
    }

    /**
     * Returns the y-component of the scale applied by this transform.
     */
    public float scaleY() {
        return Mathf.sqrt(m10 * m10 + m11 * m11);
    }

    /**
     * Returns the x-coordinate shearing element of the transform.
     */
    public float shearX() {
        return m01;
    }

    /**
     * Returns the y-coordinate shearing element of the transform.
     */
    public float shearY() {
        return m10;
    }

    /**
     * Returns the rotation applied by this transform.
     */
    public float rotate() {
        float n00 = m00, n10 = m10;
        float n01 = m01, n11 = m11;
        for (int ii = 0; ii < 10; ii++) {
            float o00 = n00, o10 = n10;
            float o01 = n01, o11 = n11;

            float det = o00 * o11 - o10 * o01;
            if (Math.abs(det) == 0f) {
                return 0;
            }
            float hrdet = 0.5f / det;
            n00 = +o11 * hrdet + o00 * 0.5f;
            n10 = -o01 * hrdet + o10 * 0.5f;

            n01 = -o10 * hrdet + o01 * 0.5f;
            n11 = +o00 * hrdet + o11 * 0.5f;

            float d00 = n00 - o00, d10 = n10 - o10;
            float d01 = n01 - o01, d11 = n11 - o11;
            if (d00 * d00 + d10 * d10 + d01 * d01 + d11 * d11 < Mathf.EPSILON) {
                break;
            }
        }
        return Mathf.atan2(n01, n00);
    }

    /**
     * Multiplies this matrix by a translation matrix.
     *
     * @param x The x-component of the translation vector.
     * @param y The y-component of the translation vector.
     * @return This matrix for the purpose of chaining.
     */
    public Affine translate(float x, float y) {
        this.m02 += m00 * x + m10 * y;
        this.m12 += m11 * y + m01 * x;
        return this;
    }

    public Affine preTranslate (float x, float y) {
        return multiply(1, 0, 0, 1, x, y, this, this);
    }

    /**
     * Multiplies this matrix with a scale matrix.
     *
     * @param x The scale in the x-axis.
     * @param y The scale in the y-axis.
     * @return This matrix for the purpose of chaining.
     */
    public Affine scale(float x, float y) {
        m00 *= x;
        m01 *= x;
        m10 *= y;
        m11 *= y;
        return this;
    }

    public Affine preScale (float scaleX, float scaleY) {
        return multiply(scaleX, 0, 0, scaleY, 0, 0, this, this);
    }

    /**
     * Multiplies this matrix by a shear matrix.
     *
     * @param shearX The shear in x direction.
     * @param shearY The shear in y direction.
     * @return This matrix for the purpose of chaining.
     */
    public Affine shear(float shearX, float shearY) {
        return multiply(this, 1, shearY, shearX, 1, 0, 0, this);
    }

    public Affine preShear (float shearX, float shearY) {
        return multiply(1, shearY, shearX, 1, 0, 0, this, this);
    }

    /**
     * Multiplies this matrix with a (counter-clockwise) rotation matrix.
     *
     * @param angle The angle in degrees
     * @return This matrix for the purpose of chaining.
     */
    public Affine rotate(float angle) {
        angle = Mathf.toRadians(angle);
        float sin = Mathf.sin(angle);
        float cos = Mathf.cos(angle);
        return multiply(this, cos, sin, -sin, cos, 0, 0, this);
    }

    public Affine preRotate (float angle) {
        angle = Mathf.toRadians(angle);
        float sin = Mathf.sin(angle);
        float cos = Mathf.cos(angle);
        return multiply(cos, sin, -sin, cos, 0, 0, this, this);
    }

    /**
     * Copies the affine transform matrix into the supplied array.
     *
     * @param matrix the array which receives {@code m00, m01, m10, m11, translateX, translateY}.
     */
    public void get(float[] matrix, int offset) {
        matrix[offset] = m00;
        matrix[offset + 1] = m01;
        matrix[offset + 2] = m10;
        matrix[offset + 3] = m11;
        matrix[offset + 4] = m02;
        matrix[offset + 5] = m12;
    }

    /**
     * Sets this matrix to a translation matrix.
     *
     * @param x The translation in x
     * @param y The translation in y
     * @return This matrix for the purpose of chaining operations.
     */
    public Affine setToTranslation(float x, float y) {
        m00 = 1;
        m01 = 0;
        m10 = 0;
        m11 = 1;
        m02 = x;
        m12 = y;
        return this;
    }

    /**
     * Sets this matrix to a scaling matrix.
     *
     * @param scaleX The scale in x.
     * @param scaleY The scale in y.
     * @return This matrix for the purpose of chaining operations.
     */
    public Affine setToScaling(float scaleX, float scaleY) {
        m00 = scaleX;
        m01 = 0;
        m10 = 0;
        m11 = scaleY;
        m02 = 0;
        m12 = 0;
        return this;
    }

    /**
     * Sets this matrix to a shearing matrix.
     *
     * @param shearX The shear in x direction.
     * @param shearY The shear in y direction.
     * @return This matrix for the purpose of chaining operations.
     */
    public Affine setToShearing(float shearX, float shearY) {
        m00 = 1;
        m01 = shearY;
        m10 = shearX;
        m11 = 1;
        m02 = 0;
        m12 = 0;
        return this;
    }

    /**
     * Sets this matrix to a rotation matrix that will rotate any vector in counter-clockwise direction around the z-axis.
     *
     * @param angle The angle in degrees.
     * @return This matrix for the purpose of chaining operations.
     */
    public Affine setToRotation(float angle) {
        angle = Mathf.toRadians(angle);

        float cos = Mathf.cos(angle);
        float sin = Mathf.sin(angle);

        m00 = cos;
        m01 = sin;
        m10 = -sin;
        m11 = cos;
        m02 = 0;
        m12 = 0;
        return this;
    }

    /**
     * Set this transform to the inverse.
     *
     */
    public Affine invert() {
        float det = m00 * m11 - m10 * m01;
        if (Math.abs(det) < Mathf.EPSILON) {
            return identity();
        }
        float rdet = 1f / det;
        return set(m11 * rdet, -m10 * rdet, -m01 * rdet, m00 * rdet,
                (m10 * m12 - m11 * m02) * rdet,
                (m01 * m02 - m00 * m12) * rdet);
    }

    /**
     * Postmultiplies this matrix with the provided matrix and stores the result in this matrix. For example:
     * <p>
     * <pre>
     * A.mul(B) results in A := AB
     * </pre>
     *
     * @param other Matrix to multiply by.
     * @return This matrix for the purpose of chaining operations together.
     */
    public Affine mul(Affine other) {
        return multiply(this, other, this);
    }

    /**
     * Premultiplies this matrix with the provided matrix and stores the result in this matrix. For example:
     *
     * <pre>
     * A.preMul(B) results in A := BA
     * </pre>
     *
     * @param other The other Matrix to multiply by
     * @return This matrix for the purpose of chaining operations.
     */
    public Affine preMul(Affine other) {
        return multiply(other, this, this);
    }

    /**
     * Set the transform with the linear interpolation between this transform and the specified other.
     */
    public Affine lerp(Affine other, float t) {
        return set(
                m00 + t * (other.m00 - m00), m01 + t * (other.m01 - m01),
                m10 + t * (other.m10 - m10), m11 + t * (other.m11 - m11),
                m02 + t * (other.m02 - m02), m12 + t * (other.m12 - m12));
    }

    /**
     * Transforms the supplied point.
     *
     * @return the x-coordinate
     */
    public float pointX(float x, float y) {
        return m00 * x + m01 * y + m02;
    }

    /**
     * Transforms the supplied point.
     *
     * @return the y-coordinate
     */
    public float pointY(float x, float y) {
        return m10 * x + m11 * y + m12;
    }

    /**
     * Transforms the supplied point.
     *
     * @return the point supplied
     */
    public Vector2 transform(Vector2 point) {
        float x = point.x, y = point.y;
        point.x = m00 * x + m10 * y + m02;
        point.y = m01 * x + m11 * y + m12;
        return point;
    }

    /**
     * Transforms the supplied points.
     *
     * @param src    the points to be transformed (as {@code [x, y, x, y, ...]}).
     * @param srcOff the offset into the {@code src} array at which to start.
     * @param dst    the points into which to store the transformed points. May be {@code src}.
     * @param dstOff the offset into the {@code dst} array at which to start.
     * @param count  the number of points to transform.
     */
    public void transform(float[] src, int srcOff, float[] dst, int dstOff, int count) {
        for (int i = 0; i < count; i++) {
            float x = src[srcOff++], y = src[srcOff++];
            dst[dstOff++] = m00 * x + m10 * y + m02;
            dst[dstOff++] = m01 * x + m11 * y + m12;
        }
    }

    /**
     * Transforms the supplied point (accounting for translation), writing the result
     * into {@code into}.
     *
     * @param into a vector into which to store the result, may be the same object as {@code v}.
     * @return {@code into}, for chaining.
     */
    public Vector2 transformPoint(Vector2 v, Vector2 into) {
        float x = v.x, y = v.y;
        return into.set(m00 * x + m10 * y + m02, m01 * x + m11 * y + m12);
    }

    /**
     * Transforms the supplied vector, writing the result into {@code into}.
     *
     * @param into a vector into which to store the result, may be the same object as {@code v}.
     * @return {@code into}, for chaining.
     */
    public Vector2 transformVector(Vector2 v, Vector2 into) {
        float x = v.x, y = v.y;
        return into.set(m00 * x + m10 * y, m01 * x + m11 * y).normalize();
    }

    @Override
    public String toString() {
        if (m00 != 1 || m01 != 0 || m10 != 0 || m11 != 1) {
            return "affine [" +
                    Mathf.toString(m00) + " " + Mathf.toString(m01) + " " +
                    Mathf.toString(m10) + " " + Mathf.toString(m11) + " " + translateX() + ", " + translateY() + "]";
        } else if (m02 != 0 || m12 != 0) {
            return "transform " + translateX() + ", " + translateY();
        } else {
            return "identity";
        }
    }

    @Override
    public int hashCode() {
        return Float.hashCode(m00 + m01 + m02 + m10 + m11 + m12);
    }

    /**
     * Multiplies the supplied two affine transforms, storing the result in {@code into}. {@code
     * into} may refer to the same instance as {@code a} or {@code b}.
     * @return {@code into} for chaining.
     */
    public static Affine multiply (Affine a, Affine b, Affine into) {
        return multiply(a.m00, a.m01, a.m10, a.m11, a.m02, a.m12,
                b.m00, b.m01, b.m10, b.m11, b.m02, b.m12, into);
    }

    /**
     * Multiplies the supplied two affine transforms, storing the result in {@code into}. {@code
     * into} may refer to the same instance as {@code a}.
     * @return {@code into} for chaining.
     */
    public static Affine multiply (
            Affine a, float m00, float m01, float m10, float m11, float tx, float ty, Affine into) {
        return multiply(a.m00, a.m01, a.m10, a.m11, a.m02, a.m12, m00, m01, m10, m11, tx, ty, into);
    }

    /**
     * Multiplies the supplied two affine transforms, storing the result in {@code into}. {@code
     * into} may refer to the same instance as {@code b}.
     * @return {@code into} for chaining.
     */
    public static Affine multiply (
            float m00, float m01, float m10, float m11, float tx, float ty, Affine b, Affine into) {
        return multiply(m00, m01, m10, m11, tx, ty, b.m00, b.m01, b.m10, b.m11, b.m02, b.m12, into);
    }

    /**
     * Multiplies the supplied two affine transforms, storing the result in {@code into}.
     * @return {@code into} for chaining.
     */
    public static Affine multiply (
            float am00, float am01, float am10, float am11, float atx, float aty,
            float bm00, float bm01, float bm10, float bm11, float btx, float bty, Affine into) {
        into.set(am00 * bm00 + am10 * bm01,
                am01 * bm00 + am11 * bm01,
                am00 * bm10 + am10 * bm11,
                am01 * bm10 + am11 * bm11,
                am00 *  btx + am10 *  bty + atx,
                am01 *  btx + am11 *  bty + aty);
        return into;
    }
}