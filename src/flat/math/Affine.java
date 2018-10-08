package flat.math;

import flat.math.util.NoninvertibleTransformException;
import flat.math.util.Platform;

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
        this(1, 0, 0, 1, 0, 0);
    }

    /**
     * Creates an affine transform from the supplied scale, rotation and translation.
     */
    public Affine(float scaleX, float scaleY, float angle, float m02, float m12) {
        float sina = Mathf.sin(angle), cosa = Mathf.cos(angle);
        this.m00 = cosa * scaleX;
        this.m01 = sina * scaleY;
        this.m10 = -sina * scaleX;
        this.m11 = cosa * scaleY;
        this.m02 = m02;
        this.m12 = m12;
    }

    /**
     * Creates an affine transform with the specified transform matrix.
     */
    public Affine(float m00, float m01, float m10, float m11, float m02, float m12) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
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
        float sina = Mathf.sin(angle), cosa = Mathf.cos(angle);
        this.m00 = cosa * scaleX;
        this.m01 = sina * scaleY;
        this.m10 = -sina * scaleX;
        this.m11 = cosa * scaleY;
        this.m02 = x;
        this.m12 = y;
        return this;
    }

    /**
     * Sets the affine transform values with the specified transform matrix.
     *
     * @return this instance, for chaining.
     */
    public Affine set(float m00, float m01, float m10, float m11, float tx, float ty) {
        this.m00 = m00;
        this.m01 = m01;
        this.m10 = m10;
        this.m11 = m11;
        this.m02 = tx;
        this.m12 = ty;
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
        /*/ start with the contents of the upper 2x2 portion of the matrix
        float n00 = m00, n10 = m10;
        float n01 = m01, n11 = m11;
        for (int i = 0; i < 10; i++) {
            // store the results of the previous iteration
            float o00 = n00, o10 = n10;
            float o01 = n01, o11 = n11;

            // compute average of the matrix with its inverse transpose
            float det = o00 * o11 - o10 * o01;
            if (Math.abs(det) == 0f) {
                // determinant is zero; matrix is not invertible
                throw new NoninvertibleTransformException(this.toString());
            }
            float hrdet = 0.5f / det;
            n00 = +o11 * hrdet + o00 * 0.5f;
            n10 = -o01 * hrdet + o10 * 0.5f;

            n01 = -o10 * hrdet + o01 * 0.5f;
            n11 = +o00 * hrdet + o11 * 0.5f;

            // compute the difference; if it's small enough, we're done
            float d00 = n00 - o00, d10 = n10 - o10;
            float d01 = n01 - o01, d11 = n11 - o11;
            if (d00 * d00 + d10 * d10 + d01 * d01 + d11 * d11 < Mathf.EPSILON) {
                break;
            }
        }
        // now that we have a nice orthogonal matrix, we can extract the rotation
        return Mathf.atan2(n01, n00);*/
        return Mathf.atan2(m10, m00);
    }

    /**
     * Multiplies this matrix by a translation matrix.
     *
     * @param x The x-component of the translation vector.
     * @param y The y-component of the translation vector.
     * @return This matrix for the purpose of chaining.
     */
    public Affine translate(float x, float y) {
        m02 += m00 * x + m01 * y;
        m12 += m10 * x + m11 * y;
        return this;
    }

    public Affine preTranslate (float x, float y) {
        m02 += x;
        m12 += y;
        return this;
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
        m01 *= y;
        m10 *= x;
        m11 *= y;
        return this;
    }

    public Affine preScale (float scaleX, float scaleY) {
        m00 *= scaleX;
        m01 *= scaleX;
        m02 *= scaleX;
        m10 *= scaleY;
        m11 *= scaleY;
        m12 *= scaleY;
        return this;
    }

    /**
     * Multiplies this matrix by a shear matrix.
     *
     * @param x The shear in x direction.
     * @param y The shear in y direction.
     * @return This matrix for the purpose of chaining.
     */
    public Affine shear(float x, float y) {
        float tmp0 = m00 + y * m01;
        float tmp1 = m01 + x * m00;
        m00 = tmp0;
        m01 = tmp1;

        tmp0 = m10 + y * m11;
        tmp1 = m11 + x * m10;
        m10 = tmp0;
        m11 = tmp1;
        return this;
    }

    public Affine preShear (float shearX, float shearY) {
        float tmp00 = m00 + shearX * m10;
        float tmp01 = m01 + shearX * m11;
        float tmp02 = m02 + shearX * m12;
        float tmp10 = m10 + shearY * m00;
        float tmp11 = m11 + shearY * m01;
        float tmp12 = m12 + shearY * m02;

        m00 = tmp00;
        m01 = tmp01;
        m02 = tmp02;
        m10 = tmp10;
        m11 = tmp11;
        m12 = tmp12;
        return this;
    }

    /**
     * Multiplies this matrix with a (counter-clockwise) rotation matrix.
     *
     * @param angle The angle in degrees
     * @return This matrix for the purpose of chaining.
     */
    public Affine rotate(float angle) {
        if (angle == 0) return this;

        angle = Mathf.toRadians(angle);

        float cos = Mathf.cos(angle);
        float sin = Mathf.sin(angle);

        float tmp00 = m00 * cos + m01 * sin;
        float tmp01 = m00 * -sin + m01 * cos;
        float tmp10 = m10 * cos + m11 * sin;
        float tmp11 = m10 * -sin + m11 * cos;

        m00 = tmp00;
        m01 = tmp01;
        m10 = tmp10;
        m11 = tmp11;
        return this;
    }

    public Affine preRotate (float angle) {
        if (angle == 0) return this;

        angle = Mathf.toRadians(angle);

        float cos = Mathf.cos(angle);
        float sin = Mathf.sin(angle);

        float tmp00 = cos * m00 - sin * m10;
        float tmp01 = cos * m01 - sin * m11;
        float tmp02 = cos * m02 - sin * m12;
        float tmp10 = sin * m00 + cos * m10;
        float tmp11 = sin * m01 + cos * m11;
        float tmp12 = sin * m02 + cos * m12;

        m00 = tmp00;
        m01 = tmp01;
        m02 = tmp02;
        m10 = tmp10;
        m11 = tmp11;
        m12 = tmp12;
        return this;
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
        m02 = x;
        m10 = 0;
        m11 = 1;
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
        m02 = 0;
        m10 = 0;
        m11 = scaleY;
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
        m01 = shearX;
        m02 = 0;
        m10 = shearY;
        m11 = 1;
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
        m01 = -sin;
        m02 = 0;
        m10 = sin;
        m11 = cos;
        m12 = 0;
        return this;
    }

    /**
     * Set this transform to the inverse.
     *
     * @throws NoninvertibleTransformException if the transform is not invertible.
     */
    /*public Affine invert() {
        float det = m00 * m11 - m10 * m01;
        if (Math.abs(det) == 0f) {
            throw new NoninvertibleTransformException(this.toString());
        }
        float rdet = 1f / det;
        return set(m11 * rdet, -m10 * rdet, -m01 * rdet, m00 * rdet,
                (m10 * m12 - m11 * m02) * rdet, (m01 * m02 - m00 * m12) * rdet);
    }*/

    public Affine invert () {
        float det = m00 * m11 - m01 * m10;
        if (det == 0) {
            return this;
            //throw new NoninvertibleTransformException(this.toString());
        }

        float invDet = 1.0f / det;

        float tmp00 = m11;
        float tmp01 = -m01;
        float tmp02 = m01 * m12 - m11 * m02;
        float tmp10 = -m10;
        float tmp11 = m00;
        float tmp12 = m10 * m02 - m00 * m12;

        m00 = invDet * tmp00;
        m01 = invDet * tmp01;
        m02 = invDet * tmp02;
        m10 = invDet * tmp10;
        m11 = invDet * tmp11;
        m12 = invDet * tmp12;
        return this;
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
        float tmp00 = m00 * other.m00 + m01 * other.m10;
        float tmp01 = m00 * other.m01 + m01 * other.m11;
        float tmp02 = m00 * other.m02 + m01 * other.m12 + m02;
        float tmp10 = m10 * other.m00 + m11 * other.m10;
        float tmp11 = m10 * other.m01 + m11 * other.m11;
        float tmp12 = m10 * other.m02 + m11 * other.m12 + m12;

        m00 = tmp00;
        m01 = tmp01;
        m02 = tmp02;
        m10 = tmp10;
        m11 = tmp11;
        m12 = tmp12;
        return this;
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
        float tmp00 = other.m00 * m00 + other.m01 * m10;
        float tmp01 = other.m00 * m01 + other.m01 * m11;
        float tmp02 = other.m00 * m02 + other.m01 * m12 + other.m02;
        float tmp10 = other.m10 * m00 + other.m11 * m10;
        float tmp11 = other.m10 * m01 + other.m11 * m11;
        float tmp12 = other.m10 * m02 + other.m11 * m12 + other.m12;

        m00 = tmp00;
        m01 = tmp01;
        m02 = tmp02;
        m10 = tmp10;
        m11 = tmp11;
        m12 = tmp12;
        return this;
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
        point.x = m00 * x + m01 * y + m02;
        point.y = m10 * x + m11 * y + m12;
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
            dst[dstOff++] = m00 * x + m01 * y + m02;
            dst[dstOff++] = m10 * x + m11 * y + m12;
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
        return into.set(m00 * x + m01 * y + m02, m10 * x + m11 * y + m12);
    }

    /**
     * Transforms the supplied vector, writing the result into {@code into}.
     *
     * @param into a vector into which to store the result, may be the same object as {@code v}.
     * @return {@code into}, for chaining.
     */
    public Vector2 transformVector(Vector2 v, Vector2 into) {
        float x = v.x, y = v.y;
        return into.set(m00 * x + m01 * y, m10 * x + m11 * y).normalize();
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
        return Platform.hashCode(m00 + m01 + m02 + m10 + m11 + m12);
    }
}
