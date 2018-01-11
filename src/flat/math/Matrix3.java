package flat.math;

import flat.math.util.NoninvertibleTransformException;
import flat.math.util.Platform;

import java.io.Serializable;

/**
 * A 3x3 matrix; useful for 2D transforms.
 */
public final class Matrix3 implements Serializable {

    private static final long serialVersionUID = 7907569533774959788L;

    public static final int M00 = 0;
    public static final int M01 = 3;
    public static final int M02 = 6;
    public static final int M10 = 1;
    public static final int M11 = 4;
    public static final int M12 = 7;
    public static final int M20 = 2;
    public static final int M21 = 5;
    public static final int M22 = 8;

    public float[] val = new float[9];

    /**
     * Constructs a identity matrix
     */
    public Matrix3() {
        identity();
    }

    /**
     * Constructs a matrix from the given matrix
     *
     * @param matrix The matrix
     */
    public Matrix3(Matrix3 matrix) {
        set(matrix);
    }

    /**
     * Constructs a matrix from the given float array with offset
     *
     * @param values The float array to copy
     */
    public Matrix3(float[] values, int offset) {
        this.set(values, offset);
    }

    /**
     * Sets this matrix to the identity matrix
     *
     * @return This matrix for chaining
     */
    public Matrix3 identity() {
        val[M00] = 1;
        val[M10] = 0;
        val[M20] = 0;
        val[M01] = 0;
        val[M11] = 1;
        val[M21] = 0;
        val[M02] = 0;
        val[M12] = 0;
        val[M22] = 1;
        return this;
    }

    /**
     * Copies the values from the provided matrix to this matrix.
     *
     * @param mat The matrix to copy.
     * @return This matrix for the purposes of chaining.
     */
    public Matrix3 set(Matrix3 mat) {
        System.arraycopy(mat.val, 0, val, 0, val.length);
        return this;
    }

    /**
     * Copies the values from the provided affine matrix to this matrix. The last row is set to (0, 0, 1).
     *
     * @param affine The affine matrix to copy.
     * @return This matrix for the purposes of chaining.
     */
    public Matrix3 set(Affine affine) {
        val[M00] = affine.m00;
        val[M10] = affine.m10;
        val[M20] = 0;
        val[M01] = affine.m01;
        val[M11] = affine.m11;
        val[M21] = 0;
        val[M02] = affine.m02;
        val[M12] = affine.m12;
        val[M22] = 1;

        return this;
    }

    /**
     * Sets this 3x3 matrix to the top left 3x3 corner of the provided 4x4 matrix.
     *
     * @param mat The matrix whose top left corner will be copied. This matrix will not be modified.
     * @return This matrix for the purpose of chaining operations.
     */
    public Matrix3 set(Matrix4 mat) {
        val[M00] = mat.val[Matrix4.M00];
        val[M10] = mat.val[Matrix4.M10];
        val[M20] = mat.val[Matrix4.M20];
        val[M01] = mat.val[Matrix4.M01];
        val[M11] = mat.val[Matrix4.M11];
        val[M21] = mat.val[Matrix4.M21];
        val[M02] = mat.val[Matrix4.M02];
        val[M12] = mat.val[Matrix4.M12];
        val[M22] = mat.val[Matrix4.M22];
        return this;
    }

    /**
     * Sets the matrix to the given matrix as a float array. The float array must have at least 9 elements; the first 9 will be
     * copied.
     *
     * @param values The matrix, in float form, that is to be copied.
     * @return This matrix for the purpose of chaining methods together.
     */
    public Matrix3 set(float[] values, int offset) {
        System.arraycopy(values, 0, val, offset, val.length);
        return this;
    }

    /**
     * Get the values in this matrix.
     */
    public void get(float[] data, int offset) {
        System.arraycopy(val, 0, data, offset, 9);
    }

    /**
     * Postmultiplies this matrix with the provided matrix and stores the result in this matrix. For example:
     * <p>
     * <pre>
     * A.mul(B) results in A := AB
     * </pre>
     *
     * @param m Matrix to multiply by.
     * @return This matrix for the purpose of chaining operations together.
     */
    public Matrix3 mul(Matrix3 m) {
        float v00 = val[M00] * m.val[M00] + val[M01] * m.val[M10] + val[M02] * m.val[M20];
        float v01 = val[M00] * m.val[M01] + val[M01] * m.val[M11] + val[M02] * m.val[M21];
        float v02 = val[M00] * m.val[M02] + val[M01] * m.val[M12] + val[M02] * m.val[M22];

        float v10 = val[M10] * m.val[M00] + val[M11] * m.val[M10] + val[M12] * m.val[M20];
        float v11 = val[M10] * m.val[M01] + val[M11] * m.val[M11] + val[M12] * m.val[M21];
        float v12 = val[M10] * m.val[M02] + val[M11] * m.val[M12] + val[M12] * m.val[M22];

        float v20 = val[M20] * m.val[M00] + val[M21] * m.val[M10] + val[M22] * m.val[M20];
        float v21 = val[M20] * m.val[M01] + val[M21] * m.val[M11] + val[M22] * m.val[M21];
        float v22 = val[M20] * m.val[M02] + val[M21] * m.val[M12] + val[M22] * m.val[M22];

        val[M00] = v00;
        val[M10] = v10;
        val[M20] = v20;
        val[M01] = v01;
        val[M11] = v11;
        val[M21] = v21;
        val[M02] = v02;
        val[M12] = v12;
        val[M22] = v22;
        return this;
    }

    /**
     * Premultiplies this matrix with the provided matrix and stores the result in this matrix. For example:
     * <p>
     * <pre>
     * A.mulLeft(B) results in A := BA
     * </pre>
     *
     * @param m The other Matrix to multiply by
     * @return This matrix for the purpose of chaining operations.
     */
    public Matrix3 mulLeft(Matrix3 m) {
        float v00 = m.val[M00] * val[M00] + m.val[M01] * val[M10] + m.val[M02] * val[M20];
        float v01 = m.val[M00] * val[M01] + m.val[M01] * val[M11] + m.val[M02] * val[M21];
        float v02 = m.val[M00] * val[M02] + m.val[M01] * val[M12] + m.val[M02] * val[M22];

        float v10 = m.val[M10] * val[M00] + m.val[M11] * val[M10] + m.val[M12] * val[M20];
        float v11 = m.val[M10] * val[M01] + m.val[M11] * val[M11] + m.val[M12] * val[M21];
        float v12 = m.val[M10] * val[M02] + m.val[M11] * val[M12] + m.val[M12] * val[M22];

        float v20 = m.val[M20] * val[M00] + m.val[M21] * val[M10] + m.val[M22] * val[M20];
        float v21 = m.val[M20] * val[M01] + m.val[M21] * val[M11] + m.val[M22] * val[M21];
        float v22 = m.val[M20] * val[M02] + m.val[M21] * val[M12] + m.val[M22] * val[M22];

        val[M00] = v00;
        val[M10] = v10;
        val[M20] = v20;
        val[M01] = v01;
        val[M11] = v11;
        val[M21] = v21;
        val[M02] = v02;
        val[M12] = v12;
        val[M22] = v22;

        return this;
    }

    /**
     * Returns the x-coordinate of the translation component.
     */
    public float translateX() {
        return val[M02];
    }

    /**
     * Returns the y-coordinate of the translation component.
     */
    public float translateY() {
        return val[M12];
    }

    /**
     * Returns the x-component of the scale applied by this transform.
     */
    public float scaleX() {
        return Mathf.sqrt(val[M00] * val[M00] + val[M01] * val[M01]);
    }

    /**
     * Returns the y-component of the scale applied by this transform.
     */
    public float scaleY() {
        return Mathf.sqrt(val[M10] * val[M10] + val[M11] * val[M11]);
    }

    /**
     * Returns the rotation applied by this transform.
     */
    public float rotate() {
        return Mathf.toDegrees(Mathf.atan2(val[M10], val[M00]));
    }

    /**
     * Postmultiplies this matrix by a translation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     *
     * @param x The x-component of the translation vector.
     * @param y The y-component of the translation vector.
     * @return This matrix for chaining
     */
    public Matrix3 translate(float x, float y) {
        final float t_00 = 1;
        final float t_10 = 0;
        final float t_20 = 0;

        final float t_01 = 0;
        final float t_11 = 1;
        final float t_21 = 0;

        final float t_02 = x;
        final float t_12 = y;
        final float t_22 = 1;

        //mul(val, tmp);

        float v00 = val[M00] * t_00 + val[M01] * t_10 + val[M02] * t_20;
        float v01 = val[M00] * t_01 + val[M01] * t_11 + val[M02] * t_21;
        float v02 = val[M00] * t_02 + val[M01] * t_12 + val[M02] * t_22;

        float v10 = val[M10] * 1 + val[M11] * t_10 + val[M12] * t_20;
        float v11 = val[M10] * t_01 + val[M11] * t_11 + val[M12] * t_21;
        float v12 = val[M10] * t_02 + val[M11] * t_12 + val[M12] * t_22;

        float v20 = val[M20] * 1 + val[M21] * t_10 + val[M22] * t_20;
        float v21 = val[M20] * t_01 + val[M21] * t_11 + val[M22] * t_21;
        float v22 = val[M20] * t_02 + val[M21] * t_12 + val[M22] * t_22;
        val[M00] = v00;
        val[M10] = v10;
        val[M20] = v20;
        val[M01] = v01;
        val[M11] = v11;
        val[M21] = v21;
        val[M02] = v02;
        val[M12] = v12;
        val[M22] = v22;

        return this;
    }

    /**
     * Postmultiplies this matrix with a scale matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     *
     * @param scaleX The scale in the x-axis.
     * @param scaleY The scale in the y-axis.
     * @return This matrix for chaining
     */
    public Matrix3 scale(float scaleX, float scaleY) {
        final float t_00 = scaleX;
        final float t_10 = 0;
        final float t_20 = 0;
        final float t_01 = 0;
        final float t_11 = scaleY;
        final float t_21 = 0;
        final float t_02 = 0;
        final float t_12 = 0;
        final float t_22 = 1;

        //mul(val, tmp);

        float v00 = val[M00] * t_00 + val[M01] * t_10 + val[M02] * t_20;
        float v01 = val[M00] * t_01 + val[M01] * t_11 + val[M02] * t_21;
        float v02 = val[M00] * t_02 + val[M01] * t_12 + val[M02] * t_22;

        float v10 = val[M10] * t_00 + val[M11] * t_10 + val[M12] * t_20;
        float v11 = val[M10] * t_01 + val[M11] * t_11 + val[M12] * t_21;
        float v12 = val[M10] * t_02 + val[M11] * t_12 + val[M12] * t_22;

        float v20 = val[M20] * t_00 + val[M21] * t_10 + val[M22] * t_20;
        float v21 = val[M20] * t_01 + val[M21] * t_11 + val[M22] * t_21;
        float v22 = val[M20] * t_02 + val[M21] * t_12 + val[M22] * t_22;
        val[M00] = v00;
        val[M10] = v10;
        val[M20] = v20;
        val[M01] = v01;
        val[M11] = v11;
        val[M21] = v21;
        val[M02] = v02;
        val[M12] = v12;
        val[M22] = v22;
        return this;
    }

    /**
     * Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     *
     * @param angle The angle in degrees
     * @return This matrix for chaining
     */
    public Matrix3 rotate(float angle) {
        if (angle == 0) return this;

        angle = Mathf.toRadians(angle);

        float cos = Mathf.cos(angle);
        float sin = Mathf.sin(angle);

        final float t_00 = cos;
        final float t_10 = sin;
        final float t_20 = 0;

        final float t_01 = -sin;
        final float t_11 = cos;
        final float t_21 = 0;

        final float t_02 = 0;
        final float t_12 = 0;
        final float t_22 = 1;

        //mul(val, tmp);

        float v00 = val[M00] * t_00 + val[M01] * t_10 + val[M02] * t_20;
        float v01 = val[M00] * t_01 + val[M01] * t_11 + val[M02] * t_21;
        float v02 = val[M00] * t_02 + val[M01] * t_12 + val[M02] * t_22;

        float v10 = val[M10] * t_00 + val[M11] * t_10 + val[M12] * t_20;
        float v11 = val[M10] * t_01 + val[M11] * t_11 + val[M12] * t_21;
        float v12 = val[M10] * t_02 + val[M11] * t_12 + val[M12] * t_22;

        float v20 = val[M20] * t_00 + val[M21] * t_10 + val[M22] * t_20;
        float v21 = val[M20] * t_01 + val[M21] * t_11 + val[M22] * t_21;
        float v22 = val[M20] * t_02 + val[M21] * t_12 + val[M22] * t_22;
        val[M00] = v00;
        val[M10] = v10;
        val[M20] = v20;
        val[M01] = v01;
        val[M11] = v11;
        val[M21] = v21;
        val[M02] = v02;
        val[M12] = v12;
        val[M22] = v22;
        return this;
    }

    /**
     * Sets this matrix to a rotation matrix (z-axis)
     *
     * @param angle the angle in degrees.
     * @return This matrix for chaining
     */
    public Matrix3 setToRotation(float angle) {
        angle = Mathf.toRadians(angle);

        float cos =  Mathf.cos(angle);
        float sin =  Mathf.sin(angle);
        val[M00] = cos;
        val[M10] = sin;
        val[M20] = 0;

        val[M01] = -sin;
        val[M11] = cos;
        val[M21] = 0;

        val[M02] = 0;
        val[M12] = 0;
        val[M22] = 1;
        return this;
    }

    /**
     * Sets this matrix to a translation matrix.
     *
     * @param x the translation in x
     * @param y the translation in y
     * @return This matrix for chaining
     */
    public Matrix3 setToTranslation(float x, float y) {
        val[M00] = 1;
        val[M10] = 0;
        val[M20] = 0;

        val[M01] = 0;
        val[M11] = 1;
        val[M21] = 0;

        val[M02] = x;
        val[M12] = y;
        val[M22] = 1;
        return this;
    }

    /**
     * Sets this matrix to a scaling matrix.
     *
     * @param scaleX the scale in x
     * @param scaleY the scale in y
     * @return This matrix for chaining
     */
    public Matrix3 setToScaling(float scaleX, float scaleY) {
        val[M00] = scaleX;
        val[M10] = 0;
        val[M20] = 0;
        val[M01] = 0;
        val[M11] = scaleY;
        val[M21] = 0;
        val[M02] = 0;
        val[M12] = 0;
        val[M22] = 1;
        return this;
    }

    /**
     * @return The determinant of this matrix
     */
    public float determinant() {
        return val[M00] * val[M11] * val[M22] + val[M01] * val[M12] * val[M20] + val[M02] * val[M10] * val[M21]
                - val[M00] * val[M12] * val[M21] - val[M01] * val[M10] * val[M22] - val[M02] * val[M11] * val[M20];
    }

    /**
     * Inverts this matrix given that the determinant is != 0.
     *
     * @return This matrix for chaining
     * @throws flat.math.util.NoninvertibleTransformException if the matrix is singular (not invertible)
     */
    public Matrix3 invert() {
        float det = determinant();
        if (det == 0) throw new NoninvertibleTransformException(this.toString());

        float inv_det = 1.0f / det;

        final float t_00 = val[M11] * val[M22] - val[M21] * val[M12];
        final float t_10 = val[M20] * val[M12] - val[M10] * val[M22];
        final float t_20 = val[M10] * val[M21] - val[M20] * val[M11];
        final float t_01 = val[M21] * val[M02] - val[M01] * val[M22];
        final float t_11 = val[M00] * val[M22] - val[M20] * val[M02];
        final float t_21 = val[M20] * val[M01] - val[M00] * val[M21];
        final float t_02 = val[M01] * val[M12] - val[M11] * val[M02];
        final float t_12 = val[M10] * val[M02] - val[M00] * val[M12];
        final float t_22 = val[M00] * val[M11] - val[M10] * val[M01];

        val[M00] = inv_det * t_00;
        val[M10] = inv_det * t_10;
        val[M20] = inv_det * t_20;
        val[M01] = inv_det * t_01;
        val[M11] = inv_det * t_11;
        val[M21] = inv_det * t_21;
        val[M02] = inv_det * t_02;
        val[M12] = inv_det * t_12;
        val[M22] = inv_det * t_22;

        return this;
    }

    /**
     * Transposes the current matrix.
     *
     * @return This matrix for chaining
     */
    public Matrix3 transpose() {
        float v01 = val[M10];
        float v02 = val[M20];
        float v10 = val[M01];
        float v12 = val[M21];
        float v20 = val[M02];
        float v21 = val[M12];
        val[M01] = v01;
        val[M02] = v02;
        val[M10] = v10;
        val[M12] = v12;
        val[M20] = v20;
        val[M21] = v21;
        return this;
    }

    /**
     * Transforms the supplied point.
     *
     * @return The point supplied
     */
    public Vector2 transform(Vector2 point) {
        float x = point.x, y = point.y;
        point.x = val[M00] * x + val[M10] * y + val[M02];
        point.y = val[M01] * x + val[M11] * y + val[M12];
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
            dst[dstOff++] = val[M00] * x + val[M10] * y + val[M02];
            dst[dstOff++] = val[M01] * x + val[M11] * y + val[M12];
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        Matrix3 other = (Matrix3) obj;
        return val[M00] == other.val[M00] && val[M01] == other.val[M01] && val[M02] == other.val[M02] &&
                val[M10] == other.val[M10] && val[M11] == other.val[M11] && val[M12] == other.val[M12] &&
                val[M20] == other.val[M20] && val[M21] == other.val[M12] && val[M22] == other.val[M22];
    }

    @Override
    public String toString() {
        return "[" + val[M00] + "|" + val[M01] + "|" + val[M02] + "]\n" +
                "[" + val[M10] + "|" + val[M11] + "|" + val[M12] + "]\n" +
                "[" + val[M20] + "|" + val[M21] + "|" + val[M22] + "]";
    }

    @Override
    public int hashCode() {
        return Platform.hashCode(val[M00] + val[M01] + val[M02] + val[M10] + val[M11] + val[M12] + val[M20] + val[M21] + val[M22]);
    }

    /**
     * Multiplies matrix a with matrix b in the following manner:
     * <p>
     * <pre>
     * mul(A, B) => A := AB
     * </pre>
     *
     * @param mata The float array representing the first matrix. Must have at least 9 elements.
     * @param matb The float array representing the second matrix. Must have at least 9 elements.
     */
    public static void mul(float[] mata, float[] matb) {
        float v00 = mata[M00] * matb[M00] + mata[M01] * matb[M10] + mata[M02] * matb[M20];
        float v01 = mata[M00] * matb[M01] + mata[M01] * matb[M11] + mata[M02] * matb[M21];
        float v02 = mata[M00] * matb[M02] + mata[M01] * matb[M12] + mata[M02] * matb[M22];

        float v10 = mata[M10] * matb[M00] + mata[M11] * matb[M10] + mata[M12] * matb[M20];
        float v11 = mata[M10] * matb[M01] + mata[M11] * matb[M11] + mata[M12] * matb[M21];
        float v12 = mata[M10] * matb[M02] + mata[M11] * matb[M12] + mata[M12] * matb[M22];

        float v20 = mata[M20] * matb[M00] + mata[M21] * matb[M10] + mata[M22] * matb[M20];
        float v21 = mata[M20] * matb[M01] + mata[M21] * matb[M11] + mata[M22] * matb[M21];
        float v22 = mata[M20] * matb[M02] + mata[M21] * matb[M12] + mata[M22] * matb[M22];

        mata[M00] = v00;
        mata[M10] = v10;
        mata[M20] = v20;
        mata[M01] = v01;
        mata[M11] = v11;
        mata[M21] = v21;
        mata[M02] = v02;
        mata[M12] = v12;
        mata[M22] = v22;
    }
}