package flat.math;

import flat.backend.ML;

public class Matrix4 {

    public static final int M00 = 0;
    public static final int M01 = 4;
    public static final int M02 = 8;
    public static final int M03 = 12;

    public static final int M10 = 1;
    public static final int M11 = 5;
    public static final int M12 = 9;
    public static final int M13 = 13;

    public static final int M20 = 2;
    public static final int M21 = 6;
    public static final int M22 = 10;
    public static final int M23 = 14;

    public static final int M30 = 3;
    public static final int M31 = 7;
    public static final int M32 = 11;
    public static final int M33 = 15;

    private static final float tmp[] = new float[16];
    public final float val[] = new float[16];

    public Matrix4 () {
        val[M00] = 1f;
        val[M11] = 1f;
        val[M22] = 1f;
        val[M33] = 1f;
    }

    public Matrix4 (Matrix4 matrix) {
        this.set(matrix);
    }

    public Matrix4 (float[] values) {
        this.set(values);
    }

    public Matrix4 (Quaternion quaternion) {
        this.set(quaternion);
    }

    public Matrix4 (Vector3 position, Quaternion rotation, Vector3 scale) {
        set(position, rotation, scale);
    }

    @Override
    public Matrix4 clone () {
        return new Matrix4(this);
    }

    public Matrix4 set (Matrix4 matrix) {
        return this.set(matrix.val);
    }

    public Matrix4 set (float[] values) {
        System.arraycopy(values, 0, val, 0, val.length);
        return this;
    }

    public Matrix4 set (Quaternion quaternion) {
        return set(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
    }

    public Matrix4 set (float quaternionX, float quaternionY, float quaternionZ, float quaternionW) {
        return set(0f, 0f, 0f, quaternionX, quaternionY, quaternionZ, quaternionW);
    }

    public Matrix4 set (Vector3 position, Quaternion orientation) {
        return set(position.x, position.y, position.z, orientation.x, orientation.y, orientation.z, orientation.w);
    }

    public Matrix4 set (float translationX, float translationY, float translationZ, float quaternionX, float quaternionY,
                        float quaternionZ, float quaternionW) {
        final float xs = quaternionX * 2f, ys = quaternionY * 2f, zs = quaternionZ * 2f;
        final float wx = quaternionW * xs, wy = quaternionW * ys, wz = quaternionW * zs;
        final float xx = quaternionX * xs, xy = quaternionX * ys, xz = quaternionX * zs;
        final float yy = quaternionY * ys, yz = quaternionY * zs, zz = quaternionZ * zs;

        val[M00] = (1.0f - (yy + zz));
        val[M01] = (xy - wz);
        val[M02] = (xz + wy);
        val[M03] = translationX;

        val[M10] = (xy + wz);
        val[M11] = (1.0f - (xx + zz));
        val[M12] = (yz - wx);
        val[M13] = translationY;

        val[M20] = (xz - wy);
        val[M21] = (yz + wx);
        val[M22] = (1.0f - (xx + yy));
        val[M23] = translationZ;

        val[M30] = 0.f;
        val[M31] = 0.f;
        val[M32] = 0.f;
        val[M33] = 1.0f;
        return this;
    }

    public Matrix4 set (Vector3 position, Quaternion orientation, Vector3 scale) {
        return set(position.x, position.y, position.z, orientation.x, orientation.y, orientation.z, orientation.w, scale.x,
                scale.y, scale.z);
    }

    public Matrix4 set (float translationX, float translationY, float translationZ, float quaternionX, float quaternionY,
                        float quaternionZ, float quaternionW, float scaleX, float scaleY, float scaleZ) {
        final float xs = quaternionX * 2f, ys = quaternionY * 2f, zs = quaternionZ * 2f;
        final float wx = quaternionW * xs, wy = quaternionW * ys, wz = quaternionW * zs;
        final float xx = quaternionX * xs, xy = quaternionX * ys, xz = quaternionX * zs;
        final float yy = quaternionY * ys, yz = quaternionY * zs, zz = quaternionZ * zs;

        val[M00] = scaleX * (1.0f - (yy + zz));
        val[M01] = scaleY * (xy - wz);
        val[M02] = scaleZ * (xz + wy);
        val[M03] = translationX;

        val[M10] = scaleX * (xy + wz);
        val[M11] = scaleY * (1.0f - (xx + zz));
        val[M12] = scaleZ * (yz - wx);
        val[M13] = translationY;

        val[M20] = scaleX * (xz - wy);
        val[M21] = scaleY * (yz + wx);
        val[M22] = scaleZ * (1.0f - (xx + yy));
        val[M23] = translationZ;

        val[M30] = 0.f;
        val[M31] = 0.f;
        val[M32] = 0.f;
        val[M33] = 1.0f;
        return this;
    }

    public Matrix4 set (Vector3 xAxis, Vector3 yAxis, Vector3 zAxis, Vector3 pos) {
        val[M00] = xAxis.x;
        val[M01] = xAxis.y;
        val[M02] = xAxis.z;
        val[M10] = yAxis.x;
        val[M11] = yAxis.y;
        val[M12] = yAxis.z;
        val[M20] = zAxis.x;
        val[M21] = zAxis.y;
        val[M22] = zAxis.z;
        val[M03] = pos.x;
        val[M13] = pos.y;
        val[M23] = pos.z;
        val[M30] = 0;
        val[M31] = 0;
        val[M32] = 0;
        val[M33] = 1;
        return this;
    }

    /** Adds a translational component to the matrix in the 4th column. The other columns are untouched.
     *
     * @param vector The translation vector to add to the current matrix. (This vector is not modified)
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 trn (Vector3 vector) {
        val[M03] += vector.x;
        val[M13] += vector.y;
        val[M23] += vector.z;
        return this;
    }

    /** Adds a translational component to the matrix in the 4th column. The other columns are untouched.
     *
     * @param x The x-component of the translation vector.
     * @param y The y-component of the translation vector.
     * @param z The z-component of the translation vector.
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 trn (float x, float y, float z) {
        val[M03] += x;
        val[M13] += y;
        val[M23] += z;
        return this;
    }

    /** @return the backing float array */
    public float[] getValues () {
        return val;
    }

    /** Postmultiplies this matrix with the given matrix, storing the result in this matrix. For example:
     *
     * <pre>
     * A.mul(B) results in A := AB.
     * </pre>
     *
     * @param matrix The other matrix to multiply by.
     * @return This matrix for the purpose of chaining operations together. */
    public Matrix4 mul (Matrix4 matrix) {
        ML.mul(val, matrix.val);
        return this;
    }

    /** Premultiplies this matrix with the given matrix, storing the result in this matrix. For example:
     *
     * <pre>
     * A.mulLeft(B) results in A := BA.
     * </pre>
     *
     * @param matrix The other matrix to multiply by.
     * @return This matrix for the purpose of chaining operations together. */
    public Matrix4 mulLeft (Matrix4 matrix) {
        tmpMat.set(matrix);
        ML.mul(tmpMat.val, this.val);
        return set(tmpMat);
    }

    /** Transposes the matrix.
     *
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 tra () {
        tmp[M00] = val[M00];
        tmp[M01] = val[M10];
        tmp[M02] = val[M20];
        tmp[M03] = val[M30];
        tmp[M10] = val[M01];
        tmp[M11] = val[M11];
        tmp[M12] = val[M21];
        tmp[M13] = val[M31];
        tmp[M20] = val[M02];
        tmp[M21] = val[M12];
        tmp[M22] = val[M22];
        tmp[M23] = val[M32];
        tmp[M30] = val[M03];
        tmp[M31] = val[M13];
        tmp[M32] = val[M23];
        tmp[M33] = val[M33];
        return set(tmp);
    }

    public Matrix4 identity() {
        val[M00] = 1;
        val[M01] = 0;
        val[M02] = 0;
        val[M03] = 0;
        val[M10] = 0;
        val[M11] = 1;
        val[M12] = 0;
        val[M13] = 0;
        val[M20] = 0;
        val[M21] = 0;
        val[M22] = 1;
        val[M23] = 0;
        val[M30] = 0;
        val[M31] = 0;
        val[M32] = 0;
        val[M33] = 1;
        return this;
    }

    public Matrix4 inv () {
        float l_det = val[M30] * val[M21] * val[M12] * val[M03] - val[M20] * val[M31] * val[M12] * val[M03] - val[M30] * val[M11]
                * val[M22] * val[M03] + val[M10] * val[M31] * val[M22] * val[M03] + val[M20] * val[M11] * val[M32] * val[M03] - val[M10]
                * val[M21] * val[M32] * val[M03] - val[M30] * val[M21] * val[M02] * val[M13] + val[M20] * val[M31] * val[M02] * val[M13]
                + val[M30] * val[M01] * val[M22] * val[M13] - val[M00] * val[M31] * val[M22] * val[M13] - val[M20] * val[M01] * val[M32]
                * val[M13] + val[M00] * val[M21] * val[M32] * val[M13] + val[M30] * val[M11] * val[M02] * val[M23] - val[M10] * val[M31]
                * val[M02] * val[M23] - val[M30] * val[M01] * val[M12] * val[M23] + val[M00] * val[M31] * val[M12] * val[M23] + val[M10]
                * val[M01] * val[M32] * val[M23] - val[M00] * val[M11] * val[M32] * val[M23] - val[M20] * val[M11] * val[M02] * val[M33]
                + val[M10] * val[M21] * val[M02] * val[M33] + val[M20] * val[M01] * val[M12] * val[M33] - val[M00] * val[M21] * val[M12]
                * val[M33] - val[M10] * val[M01] * val[M22] * val[M33] + val[M00] * val[M11] * val[M22] * val[M33];

        if (l_det == 0f) throw new RuntimeException("non-invertible matrix");

        float inv_det = 1.0f / l_det;
        tmp[M00] = val[M12] * val[M23] * val[M31] - val[M13] * val[M22] * val[M31] + val[M13] * val[M21] * val[M32] - val[M11]
                * val[M23] * val[M32] - val[M12] * val[M21] * val[M33] + val[M11] * val[M22] * val[M33];
        tmp[M01] = val[M03] * val[M22] * val[M31] - val[M02] * val[M23] * val[M31] - val[M03] * val[M21] * val[M32] + val[M01]
                * val[M23] * val[M32] + val[M02] * val[M21] * val[M33] - val[M01] * val[M22] * val[M33];
        tmp[M02] = val[M02] * val[M13] * val[M31] - val[M03] * val[M12] * val[M31] + val[M03] * val[M11] * val[M32] - val[M01]
                * val[M13] * val[M32] - val[M02] * val[M11] * val[M33] + val[M01] * val[M12] * val[M33];
        tmp[M03] = val[M03] * val[M12] * val[M21] - val[M02] * val[M13] * val[M21] - val[M03] * val[M11] * val[M22] + val[M01]
                * val[M13] * val[M22] + val[M02] * val[M11] * val[M23] - val[M01] * val[M12] * val[M23];
        tmp[M10] = val[M13] * val[M22] * val[M30] - val[M12] * val[M23] * val[M30] - val[M13] * val[M20] * val[M32] + val[M10]
                * val[M23] * val[M32] + val[M12] * val[M20] * val[M33] - val[M10] * val[M22] * val[M33];
        tmp[M11] = val[M02] * val[M23] * val[M30] - val[M03] * val[M22] * val[M30] + val[M03] * val[M20] * val[M32] - val[M00]
                * val[M23] * val[M32] - val[M02] * val[M20] * val[M33] + val[M00] * val[M22] * val[M33];
        tmp[M12] = val[M03] * val[M12] * val[M30] - val[M02] * val[M13] * val[M30] - val[M03] * val[M10] * val[M32] + val[M00]
                * val[M13] * val[M32] + val[M02] * val[M10] * val[M33] - val[M00] * val[M12] * val[M33];
        tmp[M13] = val[M02] * val[M13] * val[M20] - val[M03] * val[M12] * val[M20] + val[M03] * val[M10] * val[M22] - val[M00]
                * val[M13] * val[M22] - val[M02] * val[M10] * val[M23] + val[M00] * val[M12] * val[M23];
        tmp[M20] = val[M11] * val[M23] * val[M30] - val[M13] * val[M21] * val[M30] + val[M13] * val[M20] * val[M31] - val[M10]
                * val[M23] * val[M31] - val[M11] * val[M20] * val[M33] + val[M10] * val[M21] * val[M33];
        tmp[M21] = val[M03] * val[M21] * val[M30] - val[M01] * val[M23] * val[M30] - val[M03] * val[M20] * val[M31] + val[M00]
                * val[M23] * val[M31] + val[M01] * val[M20] * val[M33] - val[M00] * val[M21] * val[M33];
        tmp[M22] = val[M01] * val[M13] * val[M30] - val[M03] * val[M11] * val[M30] + val[M03] * val[M10] * val[M31] - val[M00]
                * val[M13] * val[M31] - val[M01] * val[M10] * val[M33] + val[M00] * val[M11] * val[M33];
        tmp[M23] = val[M03] * val[M11] * val[M20] - val[M01] * val[M13] * val[M20] - val[M03] * val[M10] * val[M21] + val[M00]
                * val[M13] * val[M21] + val[M01] * val[M10] * val[M23] - val[M00] * val[M11] * val[M23];
        tmp[M30] = val[M12] * val[M21] * val[M30] - val[M11] * val[M22] * val[M30] - val[M12] * val[M20] * val[M31] + val[M10]
                * val[M22] * val[M31] + val[M11] * val[M20] * val[M32] - val[M10] * val[M21] * val[M32];
        tmp[M31] = val[M01] * val[M22] * val[M30] - val[M02] * val[M21] * val[M30] + val[M02] * val[M20] * val[M31] - val[M00]
                * val[M22] * val[M31] - val[M01] * val[M20] * val[M32] + val[M00] * val[M21] * val[M32];
        tmp[M32] = val[M02] * val[M11] * val[M30] - val[M01] * val[M12] * val[M30] - val[M02] * val[M10] * val[M31] + val[M00]
                * val[M12] * val[M31] + val[M01] * val[M10] * val[M32] - val[M00] * val[M11] * val[M32];
        tmp[M33] = val[M01] * val[M12] * val[M20] - val[M02] * val[M11] * val[M20] + val[M02] * val[M10] * val[M21] - val[M00]
                * val[M12] * val[M21] - val[M01] * val[M10] * val[M22] + val[M00] * val[M11] * val[M22];
        val[M00] = tmp[M00] * inv_det;
        val[M01] = tmp[M01] * inv_det;
        val[M02] = tmp[M02] * inv_det;
        val[M03] = tmp[M03] * inv_det;
        val[M10] = tmp[M10] * inv_det;
        val[M11] = tmp[M11] * inv_det;
        val[M12] = tmp[M12] * inv_det;
        val[M13] = tmp[M13] * inv_det;
        val[M20] = tmp[M20] * inv_det;
        val[M21] = tmp[M21] * inv_det;
        val[M22] = tmp[M22] * inv_det;
        val[M23] = tmp[M23] * inv_det;
        val[M30] = tmp[M30] * inv_det;
        val[M31] = tmp[M31] * inv_det;
        val[M32] = tmp[M32] * inv_det;
        val[M33] = tmp[M33] * inv_det;
        return this;
    }

    /** @return The determinant of this matrix */
    public float det () {
        return val[M30] * val[M21] * val[M12] * val[M03] - val[M20] * val[M31] * val[M12] * val[M03] - val[M30] * val[M11]
                * val[M22] * val[M03] + val[M10] * val[M31] * val[M22] * val[M03] + val[M20] * val[M11] * val[M32] * val[M03] - val[M10]
                * val[M21] * val[M32] * val[M03] - val[M30] * val[M21] * val[M02] * val[M13] + val[M20] * val[M31] * val[M02] * val[M13]
                + val[M30] * val[M01] * val[M22] * val[M13] - val[M00] * val[M31] * val[M22] * val[M13] - val[M20] * val[M01] * val[M32]
                * val[M13] + val[M00] * val[M21] * val[M32] * val[M13] + val[M30] * val[M11] * val[M02] * val[M23] - val[M10] * val[M31]
                * val[M02] * val[M23] - val[M30] * val[M01] * val[M12] * val[M23] + val[M00] * val[M31] * val[M12] * val[M23] + val[M10]
                * val[M01] * val[M32] * val[M23] - val[M00] * val[M11] * val[M32] * val[M23] - val[M20] * val[M11] * val[M02] * val[M33]
                + val[M10] * val[M21] * val[M02] * val[M33] + val[M20] * val[M01] * val[M12] * val[M33] - val[M00] * val[M21] * val[M12]
                * val[M33] - val[M10] * val[M01] * val[M22] * val[M33] + val[M00] * val[M11] * val[M22] * val[M33];
    }

    /** @return The determinant of the 3x3 upper left matrix */
    public float det3x3 () {
        return val[M00] * val[M11] * val[M22] + val[M01] * val[M12] * val[M20] + val[M02] * val[M10] * val[M21] - val[M00]
                * val[M12] * val[M21] - val[M01] * val[M10] * val[M22] - val[M02] * val[M11] * val[M20];
    }

    /** Sets the matrix to a projection matrix with a near- and far plane, a field of view in degrees and an aspect ratio. Note that
     * the field of view specified is the angle in degrees for the height, the field of view for the width will be calculated
     * according to the aspect ratio.
     *
     * @param near The near plane
     * @param far The far plane
     * @param fovy The field of view of the height in degrees
     * @param aspectRatio The "width over height" aspect ratio
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 setToProjection (float near, float far, float fovy, float aspectRatio) {
        identity();
        float l_fd = (float)(1.0 / Math.tan((fovy * (Math.PI / 180)) / 2.0));
        float l_a1 = (far + near) / (near - far);
        float l_a2 = (2 * far * near) / (near - far);
        val[M00] = l_fd / aspectRatio;
        val[M10] = 0;
        val[M20] = 0;
        val[M30] = 0;
        val[M01] = 0;
        val[M11] = l_fd;
        val[M21] = 0;
        val[M31] = 0;
        val[M02] = 0;
        val[M12] = 0;
        val[M22] = l_a1;
        val[M32] = -1;
        val[M03] = 0;
        val[M13] = 0;
        val[M23] = l_a2;
        val[M33] = 0;

        return this;
    }

    /** Sets the matrix to a projection matrix with a near/far plane, and left, bottom, right and top specifying the points on the
     * near plane that are mapped to the lower left and upper right corners of the viewport. This allows to create projection
     * matrix with off-center vanishing point.
     *
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @param near The near plane
     * @param far The far plane
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 setToProjection (float left, float right, float bottom, float top, float near, float far) {
        float x = 2.0f * near / (right - left);
        float y = 2.0f * near / (top - bottom);
        float a = (right + left) / (right - left);
        float b = (top + bottom) / (top - bottom);
        float l_a1 = (far + near) / (near - far);
        float l_a2 = (2 * far * near) / (near - far);
        val[M00] = x;
        val[M10] = 0;
        val[M20] = 0;
        val[M30] = 0;
        val[M01] = 0;
        val[M11] = y;
        val[M21] = 0;
        val[M31] = 0;
        val[M02] = a;
        val[M12] = b;
        val[M22] = l_a1;
        val[M32] = -1;
        val[M03] = 0;
        val[M13] = 0;
        val[M23] = l_a2;
        val[M33] = 0;

        return this;
    }

    /** Sets this matrix to an orthographic projection matrix with the origin at (x,y) extending by width and height. The near plane
     * is set to 0, the far plane is set to 1.
     *
     * @param x The x-coordinate of the origin
     * @param y The y-coordinate of the origin
     * @param width The width
     * @param height The height
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 setToOrtho2D (float x, float y, float width, float height) {
        setToOrtho(x, x + width, y, y + height, 0, 1);
        return this;
    }

    /** Sets this matrix to an orthographic projection matrix with the origin at (x,y) extending by width and height, having a near
     * and far plane.
     *
     * @param x The x-coordinate of the origin
     * @param y The y-coordinate of the origin
     * @param width The width
     * @param height The height
     * @param near The near plane
     * @param far The far plane
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 setToOrtho2D (float x, float y, float width, float height, float near, float far) {
        setToOrtho(x, x + width, y, y + height, near, far);
        return this;
    }

    /** Sets the matrix to an orthographic projection like glOrtho (http://www.opengl.org/sdk/docs/man/xhtml/glOrtho.xml) following
     * the OpenGL equivalent
     *
     * @param left The left clipping plane
     * @param right The right clipping plane
     * @param bottom The bottom clipping plane
     * @param top The top clipping plane
     * @param near The near clipping plane
     * @param far The far clipping plane
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 setToOrtho (float left, float right, float bottom, float top, float near, float far) {

        this.identity();
        float x_orth = 2 / (right - left);
        float y_orth = 2 / (top - bottom);
        float z_orth = -2 / (far - near);

        float tx = -(right + left) / (right - left);
        float ty = -(top + bottom) / (top - bottom);
        float tz = -(far + near) / (far - near);

        val[M00] = x_orth;
        val[M10] = 0;
        val[M20] = 0;
        val[M30] = 0;
        val[M01] = 0;
        val[M11] = y_orth;
        val[M21] = 0;
        val[M31] = 0;
        val[M02] = 0;
        val[M12] = 0;
        val[M22] = z_orth;
        val[M32] = 0;
        val[M03] = tx;
        val[M13] = ty;
        val[M23] = tz;
        val[M33] = 1;

        return this;
    }

    /** Sets the 4th column to the translation vector.
     *
     * @param vector The translation vector
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 setTranslation (Vector3 vector) {
        val[M03] = vector.x;
        val[M13] = vector.y;
        val[M23] = vector.z;
        return this;
    }

    /** Sets the 4th column to the translation vector.
     *
     * @param x The X coordinate of the translation vector
     * @param y The Y coordinate of the translation vector
     * @param z The Z coordinate of the translation vector
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 setTranslation (float x, float y, float z) {
        val[M03] = x;
        val[M13] = y;
        val[M23] = z;
        return this;
    }

    /** Sets this matrix to a translation matrix, overwriting it first by an identity matrix and then setting the 4th column to the
     * translation vector.
     *
     * @param vector The translation vector
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 setToTranslation (Vector3 vector) {
        identity();
        val[M03] = vector.x;
        val[M13] = vector.y;
        val[M23] = vector.z;
        return this;
    }

    /** Sets this matrix to a translation matrix, overwriting it first by an identity matrix and then setting the 4th column to the
     * translation vector.
     *
     * @param x The x-component of the translation vector.
     * @param y The y-component of the translation vector.
     * @param z The z-component of the translation vector.
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 setToTranslation (float x, float y, float z) {
        identity();
        val[M03] = x;
        val[M13] = y;
        val[M23] = z;
        return this;
    }

    /** Sets this matrix to a translation and scaling matrix by first overwriting it with an identity and then setting the
     * translation vector in the 4th column and the scaling vector in the diagonal.
     *
     * @param translation The translation vector
     * @param scaling The scaling vector
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 setToTranslationAndScaling (Vector3 translation, Vector3 scaling) {
        identity();
        val[M03] = translation.x;
        val[M13] = translation.y;
        val[M23] = translation.z;
        val[M00] = scaling.x;
        val[M11] = scaling.y;
        val[M22] = scaling.z;
        return this;
    }

    /** Sets this matrix to a translation and scaling matrix by first overwriting it with an identity and then setting the
     * translation vector in the 4th column and the scaling vector in the diagonal.
     *
     * @param translationX The x-component of the translation vector
     * @param translationY The y-component of the translation vector
     * @param translationZ The z-component of the translation vector
     * @param scalingX The x-component of the scaling vector
     * @param scalingY The x-component of the scaling vector
     * @param scalingZ The x-component of the scaling vector
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 setToTranslationAndScaling (float translationX, float translationY, float translationZ, float scalingX,
                                               float scalingY, float scalingZ) {
        identity();
        val[M03] = translationX;
        val[M13] = translationY;
        val[M23] = translationZ;
        val[M00] = scalingX;
        val[M11] = scalingY;
        val[M22] = scalingZ;
        return this;
    }

    static Quaternion quat = new Quaternion();
    static Quaternion quat2 = new Quaternion();

    /** Sets the matrix to a rotation matrix around the given axis.
     *
     * @param axis The axis
     * @param degrees The angle in degrees
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 setToRotation (Vector3 axis, float degrees) {
        if (degrees == 0) {
            identity();
            return this;
        }
        return set(quat.set(axis, degrees));
    }

    /** Sets the matrix to a rotation matrix around the given axis.
     *
     * @param axisX The x-component of the axis
     * @param axisY The y-component of the axis
     * @param axisZ The z-component of the axis
     * @param degrees The angle in degrees
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 setToRotation (float axisX, float axisY, float axisZ, float degrees) {
        if (degrees == 0) {
            identity();
            return this;
        }
        return set(quat.setFromAxis(axisX, axisY, axisZ, degrees));
    }

    /** Set the matrix to a rotation matrix between two vectors.
     * @param v1 The base vector
     * @param v2 The target vector
     * @return This matrix for the purpose of chaining methods together */
    public Matrix4 setToRotation (final Vector3 v1, final Vector3 v2) {
        return set(quat.setFromCross(v1, v2));
    }

    /** Set the matrix to a rotation matrix between two vectors.
     * @param x1 The base vectors x value
     * @param y1 The base vectors y value
     * @param z1 The base vectors z value
     * @param x2 The target vector x value
     * @param y2 The target vector y value
     * @param z2 The target vector z value
     * @return This matrix for the purpose of chaining methods together */
    public Matrix4 setToRotation (final float x1, final float y1, final float z1, final float x2, final float y2, final float z2) {
        return set(quat.setFromCross(x1, y1, z1, x2, y2, z2));
    }

    /** Sets this matrix to a rotation matrix from the given euler angles.
     * @param yaw the yaw in degrees
     * @param pitch the pitch in degrees
     * @param roll the roll in degrees
     * @return This matrix */
    public Matrix4 setFromEulerAngles (float yaw, float pitch, float roll) {
        quat.setEulerAngles(yaw, pitch, roll);
        return set(quat);
    }

    /** Sets this matrix to a scaling matrix
     *
     * @param vector The scaling vector
     * @return This matrix for chaining. */
    public Matrix4 setToScaling (Vector3 vector) {
        identity();
        val[M00] = vector.x;
        val[M11] = vector.y;
        val[M22] = vector.z;
        return this;
    }

    /** Sets this matrix to a scaling matrix
     *
     * @param x The x-component of the scaling vector
     * @param y The y-component of the scaling vector
     * @param z The z-component of the scaling vector
     * @return This matrix for chaining. */
    public Matrix4 setToScaling (float x, float y, float z) {
        identity();
        val[M00] = x;
        val[M11] = y;
        val[M22] = z;
        return this;
    }

    static final Vector3 l_vez = new Vector3();
    static final Vector3 l_vex = new Vector3();
    static final Vector3 l_vey = new Vector3();

    /** Sets the matrix to a look at matrix with a direction and an up vector. Multiply with a translation matrix to get a camera
     * model view matrix.
     *
     * @param direction The direction vector
     * @param up The up vector
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 setToLookAt (Vector3 direction, Vector3 up) {
        l_vez.set(direction).normalize();
        l_vex.set(direction).normalize();
        l_vex.cross(up).normalize();
        l_vey.set(l_vex).cross(l_vez).normalize();
        identity();
        val[M00] = l_vex.x;
        val[M01] = l_vex.y;
        val[M02] = l_vex.z;
        val[M10] = l_vey.x;
        val[M11] = l_vey.y;
        val[M12] = l_vey.z;
        val[M20] = -l_vez.x;
        val[M21] = -l_vez.y;
        val[M22] = -l_vez.z;

        return this;
    }

    static final Vector3 tmpVec = new Vector3();
    static final Matrix4 tmpMat = new Matrix4();

    /** Sets this matrix to a look at matrix with the given position, target and up vector.
     *
     * @param position the position
     * @param target the target
     * @param up the up vector
     * @return This matrix */
    public Matrix4 setToLookAt (Vector3 position, Vector3 target, Vector3 up) {
        tmpVec.set(target).sub(position);
        setToLookAt(tmpVec, up);
        this.mul(tmpMat.setToTranslation(-position.x, -position.y, -position.z));

        return this;
    }

    static final Vector3 right = new Vector3();
    static final Vector3 tmpForward = new Vector3();
    static final Vector3 tmpUp = new Vector3();

    public Matrix4 setToWorld (Vector3 position, Vector3 forward, Vector3 up) {
        tmpForward.set(forward).normalize();
        right.set(tmpForward).cross(up).normalize();
        tmpUp.set(right).cross(tmpForward).normalize();

        this.set(right, tmpUp, tmpForward.mul(-1), position);
        return this;
    }

    public String toString () {
        return "[" + val[M00] + "|" + val[M01] + "|" + val[M02] + "|" + val[M03] + "]\n" + "[" + val[M10] + "|" + val[M11] + "|"
                + val[M12] + "|" + val[M13] + "]\n" + "[" + val[M20] + "|" + val[M21] + "|" + val[M22] + "|" + val[M23] + "]\n" + "["
                + val[M30] + "|" + val[M31] + "|" + val[M32] + "|" + val[M33] + "]\n";
    }

    /** Linearly interpolates between this matrix and the given matrix mixing by alpha
     * @param matrix the matrix
     * @param alpha the alpha value in the range [0,1]
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 lerp (Matrix4 matrix, float alpha) {
        for (int i = 0; i < 16; i++)
            this.val[i] = this.val[i] * (1 - alpha) + matrix.val[i] * alpha;
        return this;
    }

    /** Averages the given transform with this one and stores the result in this matrix. Translations and scales are lerped while
     * rotations are slerped.
     * @param other The other transform
     * @param w Weight of this transform; weight of the other transform is (1 - w)
     * @return This matrix for chaining */
    public Matrix4 avg (Matrix4 other, float w) {
        getScale(tmpVec);
        other.getScale(tmpForward);

        getRotation(quat);
        other.getRotation(quat2);

        getTranslation(tmpUp);
        other.getTranslation(right);

        setToScaling(tmpVec.mul(w).add(tmpForward.mul(1 - w)));
        rotate(quat.slerp(quat2, 1 - w));
        setTranslation(tmpUp.mul(w).add(right.mul(1 - w)));

        return this;
    }

    /** Averages the given transforms and stores the result in this matrix. Translations and scales are lerped while rotations are
     * slerped. Does not destroy the data contained in t.
     * @param t List of transforms
     * @return This matrix for chaining */
    public Matrix4 avg (Matrix4[] t) {
        final float w = 1.0f / t.length;

        tmpVec.set(t[0].getScale(tmpUp).mul(w));
        quat.set(t[0].getRotation(quat2).exp(w));
        tmpForward.set(t[0].getTranslation(tmpUp).mul(w));

        for (int i = 1; i < t.length; i++) {
            tmpVec.add(t[i].getScale(tmpUp).mul(w));
            quat.mul(t[i].getRotation(quat2).exp(w));
            tmpForward.add(t[i].getTranslation(tmpUp).mul(w));
        }
        quat.normalize();

        setToScaling(tmpVec);
        rotate(quat);
        setTranslation(tmpForward);

        return this;
    }

    /** Averages the given transforms with the given weights and stores the result in this matrix. Translations and scales are
     * lerped while rotations are slerped. Does not destroy the data contained in t or w; Sum of w_i must be equal to 1, or
     * unexpected results will occur.
     * @param t List of transforms
     * @param w List of weights
     * @return This matrix for chaining */
    public Matrix4 avg (Matrix4[] t, float[] w) {
        tmpVec.set(t[0].getScale(tmpUp).mul(w[0]));
        quat.set(t[0].getRotation(quat2).exp(w[0]));
        tmpForward.set(t[0].getTranslation(tmpUp).mul(w[0]));

        for (int i = 1; i < t.length; i++) {
            tmpVec.add(t[i].getScale(tmpUp).mul(w[i]));
            quat.mul(t[i].getRotation(quat2).exp(w[i]));
            tmpForward.add(t[i].getTranslation(tmpUp).mul(w[i]));
        }
        quat.normalize();

        setToScaling(tmpVec);
        rotate(quat);
        setTranslation(tmpForward);

        return this;
    }

    /** Sets this matrix to the given 3x3 matrix. The third column of this matrix is set to (0,0,1,0).
     * @param mat the matrix */
    public Matrix4 set (Matrix3 mat) {
        val[0] = mat.val[0];
        val[1] = mat.val[1];
        val[2] = mat.val[2];
        val[3] = 0;
        val[4] = mat.val[3];
        val[5] = mat.val[4];
        val[6] = mat.val[5];
        val[7] = 0;
        val[8] = 0;
        val[9] = 0;
        val[10] = 1;
        val[11] = 0;
        val[12] = mat.val[6];
        val[13] = mat.val[7];
        val[14] = 0;
        val[15] = mat.val[8];
        return this;
    }

    /** Sets this matrix to the given affine matrix. The values are mapped as follows:
     *
     * <pre>
     *      [  M00  M01   0   M02  ]
     *      [  M10  M11   0   M12  ]
     *      [   0    0    1    0   ]
     *      [   0    0    0    1   ]
     * </pre>
     * @param affine the affine matrix
     * @return This matrix for chaining */
    public Matrix4 set (Affine affine) {
        val[M00] = affine.m00;
        val[M10] = affine.m10;
        val[M20] = 0;
        val[M30] = 0;
        val[M01] = affine.m01;
        val[M11] = affine.m11;
        val[M21] = 0;
        val[M31] = 0;
        val[M02] = 0;
        val[M12] = 0;
        val[M22] = 1;
        val[M32] = 0;
        val[M03] = affine.m02;
        val[M13] = affine.m12;
        val[M23] = 0;
        val[M33] = 1;
        return this;
    }

    /** Assumes that this matrix is a 2D affine transformation, copying only the relevant components. The values are mapped as
     * follows:
     *
     * <pre>
     *      [  M00  M01   _   M02  ]
     *      [  M10  M11   _   M12  ]
     *      [   _    _    _    _   ]
     *      [   _    _    _    _   ]
     * </pre>
     * @param affine the source matrix
     * @return This matrix for chaining */
    public Matrix4 setAsAffine (Affine affine) {
        val[M00] = affine.m00;
        val[M10] = affine.m10;
        val[M01] = affine.m01;
        val[M11] = affine.m11;
        val[M03] = affine.m02;
        val[M13] = affine.m12;
        return this;
    }

    /** Assumes that both matrices are 2D affine transformations, copying only the relevant components. The copied values are:
     *
     * <pre>
     *      [  M00  M01   _   M03  ]
     *      [  M10  M11   _   M13  ]
     *      [   _    _    _    _   ]
     *      [   _    _    _    _   ]
     * </pre>
     * @param mat the source matrix
     * @return This matrix for chaining */
    public Matrix4 setAsAffine (Matrix4 mat) {
        val[M00] = mat.val[M00];
        val[M10] = mat.val[M10];
        val[M01] = mat.val[M01];
        val[M11] = mat.val[M11];
        val[M03] = mat.val[M03];
        val[M13] = mat.val[M13];
        return this;
    }

    public Matrix4 scl (Vector3 scale) {
        val[M00] *= scale.x;
        val[M11] *= scale.y;
        val[M22] *= scale.z;
        return this;
    }

    public Matrix4 scl (float x, float y, float z) {
        val[M00] *= x;
        val[M11] *= y;
        val[M22] *= z;
        return this;
    }

    public Matrix4 scl (float scale) {
        val[M00] *= scale;
        val[M11] *= scale;
        val[M22] *= scale;
        return this;
    }

    public Vector3 getTranslation (Vector3 position) {
        position.x = val[M03];
        position.y = val[M13];
        position.z = val[M23];
        return position;
    }

    /** Gets the rotation of this matrix.
     * @param rotation The {@link Quaternion} to receive the rotation
     * @param normalizeAxes True to normalize the axes, necessary when the matrix might also include scaling.
     * @return The provided {@link Quaternion} for chaining. */
    public Quaternion getRotation (Quaternion rotation, boolean normalizeAxes) {
        return rotation.setFromMatrix(normalizeAxes, this);
    }

    /** Gets the rotation of this matrix.
     * @param rotation The {@link Quaternion} to receive the rotation
     * @return The provided {@link Quaternion} for chaining. */
    public Quaternion getRotation (Quaternion rotation) {
        return rotation.setFromMatrix(this);
    }

    /** @return the squared scale factor on the X axis */
    public float getScaleXSquared () {
        return val[Matrix4.M00] * val[Matrix4.M00] + val[Matrix4.M01] * val[Matrix4.M01] + val[Matrix4.M02] * val[Matrix4.M02];
    }

    /** @return the squared scale factor on the Y axis */
    public float getScaleYSquared () {
        return val[Matrix4.M10] * val[Matrix4.M10] + val[Matrix4.M11] * val[Matrix4.M11] + val[Matrix4.M12] * val[Matrix4.M12];
    }

    /** @return the squared scale factor on the Z axis */
    public float getScaleZSquared () {
        return val[Matrix4.M20] * val[Matrix4.M20] + val[Matrix4.M21] * val[Matrix4.M21] + val[Matrix4.M22] * val[Matrix4.M22];
    }

    /** @return the scale factor on the X axis (non-negative) */
    public float getScaleX () {
        return (Mathf.isZero(val[Matrix4.M01]) && Mathf.isZero(val[Matrix4.M02])) ? Math.abs(val[Matrix4.M00])
                : (float)Math.sqrt(getScaleXSquared());
    }

    /** @return the scale factor on the Y axis (non-negative) */
    public float getScaleY () {
        return (Mathf.isZero(val[Matrix4.M10]) && Mathf.isZero(val[Matrix4.M12])) ? Math.abs(val[Matrix4.M11])
                : (float)Math.sqrt(getScaleYSquared());
    }

    /** @return the scale factor on the X axis (non-negative) */
    public float getScaleZ () {
        return (Mathf.isZero(val[Matrix4.M20]) && Mathf.isZero(val[Matrix4.M21])) ? Math.abs(val[Matrix4.M22])
                : (float)Math.sqrt(getScaleZSquared());
    }

    /** @param scale The vector which will receive the (non-negative) scale components on each axis.
     * @return The provided vector for chaining. */
    public Vector3 getScale (Vector3 scale) {
        return scale.set(getScaleX(), getScaleY(), getScaleZ());
    }

    /** removes the translational part and transposes the matrix. */
    public Matrix4 toNormalMatrix () {
        val[M03] = 0;
        val[M13] = 0;
        val[M23] = 0;
        return inv().tra();
    }

    /** Postmultiplies this matrix by a translation matrix. Postmultiplication is also used by OpenGL ES'
     * glTranslate/glRotate/glScale
     * @param translation
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 translate (Vector3 translation) {
        return translate(translation.x, translation.y, translation.z);
    }

    /** Postmultiplies this matrix by a translation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     * @param x Translation in the x-axis.
     * @param y Translation in the y-axis.
     * @param z Translation in the z-axis.
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 translate (float x, float y, float z) {
        tmp[M00] = 1;
        tmp[M01] = 0;
        tmp[M02] = 0;
        tmp[M03] = x;
        tmp[M10] = 0;
        tmp[M11] = 1;
        tmp[M12] = 0;
        tmp[M13] = y;
        tmp[M20] = 0;
        tmp[M21] = 0;
        tmp[M22] = 1;
        tmp[M23] = z;
        tmp[M30] = 0;
        tmp[M31] = 0;
        tmp[M32] = 0;
        tmp[M33] = 1;

        ML.mul(val, tmp);
        return this;
    }

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     *
     * @param axis The vector axis to rotate around.
     * @param degrees The angle in degrees.
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 rotate (Vector3 axis, float degrees) {
        if (degrees == 0) return this;
        quat.set(axis, degrees);
        return rotate(quat);
    }

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale
     * @param axisX The x-axis component of the vector to rotate around.
     * @param axisY The y-axis component of the vector to rotate around.
     * @param axisZ The z-axis component of the vector to rotate around.
     * @param degrees The angle in degrees
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 rotate (float axisX, float axisY, float axisZ, float degrees) {
        if (degrees == 0) return this;
        quat.setFromAxis(axisX, axisY, axisZ, degrees);
        return rotate(quat);
    }

    /** Postmultiplies this matrix with a (counter-clockwise) rotation matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     *
     * @param rotation
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 rotate (Quaternion rotation) {
        rotation.toMatrix(tmp);
        ML.mul(val, tmp);
        return this;
    }

    /** Postmultiplies this matrix by the rotation between two vectors.
     * @param v1 The base vector
     * @param v2 The target vector
     * @return This matrix for the purpose of chaining methods together */
    public Matrix4 rotate (final Vector3 v1, final Vector3 v2) {
        return rotate(quat.setFromCross(v1, v2));
    }

    /** Postmultiplies this matrix with a scale matrix. Postmultiplication is also used by OpenGL ES' 1.x
     * glTranslate/glRotate/glScale.
     * @param scaleX The scale in the x-axis.
     * @param scaleY The scale in the y-axis.
     * @param scaleZ The scale in the z-axis.
     * @return This matrix for the purpose of chaining methods together. */
    public Matrix4 scale (float scaleX, float scaleY, float scaleZ) {
        tmp[M00] = scaleX;
        tmp[M01] = 0;
        tmp[M02] = 0;
        tmp[M03] = 0;
        tmp[M10] = 0;
        tmp[M11] = scaleY;
        tmp[M12] = 0;
        tmp[M13] = 0;
        tmp[M20] = 0;
        tmp[M21] = 0;
        tmp[M22] = scaleZ;
        tmp[M23] = 0;
        tmp[M30] = 0;
        tmp[M31] = 0;
        tmp[M32] = 0;
        tmp[M33] = 1;

        ML.mul(val, tmp);
        return this;
    }

    /** Copies the 4x3 upper-left sub-matrix into float array. The destination array is supposed to be a column major matrix.
     * @param dst the destination matrix */
    public void extract4x3Matrix (float[] dst) {
        dst[0] = val[M00];
        dst[1] = val[M10];
        dst[2] = val[M20];
        dst[3] = val[M01];
        dst[4] = val[M11];
        dst[5] = val[M21];
        dst[6] = val[M02];
        dst[7] = val[M12];
        dst[8] = val[M22];
        dst[9] = val[M03];
        dst[10] = val[M13];
        dst[11] = val[M23];
    }

    /** @return True if this matrix has any rotation or scaling, false otherwise */
    public boolean hasRotationOrScaling () {
        return !(Mathf.isEqual(val[M00], 1) && Mathf.isEqual(val[M11], 1) && Mathf.isEqual(val[M22], 1)
                && Mathf.isZero(val[M01]) && Mathf.isZero(val[M02]) && Mathf.isZero(val[M10]) && Mathf.isZero(val[M12])
                && Mathf.isZero(val[M20]) && Mathf.isZero(val[M21]));
    }
}
