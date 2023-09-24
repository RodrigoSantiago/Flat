package flat.math;

import java.io.Serializable;

/**
 * A 3D point class; useful for 3D translations, scales, rotation, and geometric operations
 */
public final class Vector3 implements Serializable {
    private static final long serialVersionUID = 3840054589595372522L;

    /**
     * the x-component of this vector
     **/
    public float x;
    /**
     * the y-component of this vector
     **/
    public float y;
    /**
     * the z-component of this vector
     **/
    public float z;

    public final static Vector3 X = new Vector3(1, 0, 0);
    public final static Vector3 Y = new Vector3(0, 1, 0);
    public final static Vector3 Z = new Vector3(0, 0, 1);
    public final static Vector3 Zero = new Vector3(0, 0, 0);

    private final static Matrix4 tmpMat = new Matrix4();

    /**
     * Constructs a vector at (0,0,0)
     */
    public Vector3() {
    }

    /**
     * Creates a vector with the given components
     *
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     */
    public Vector3(float x, float y, float z) {
        this.set(x, y, z);
    }

    /**
     * Creates a vector from the given vector
     *
     * @param vector The vector
     */
    public Vector3(Vector3 vector) {
        this.set(vector);
    }

    /**
     * Creates a vector with the given components
     *
     * @param values The data to read
     * @param offset The start index
     */
    public Vector3(float[] values, int offset) {
        this.set(values, offset);
    }

    /**
     * Creates a vector from the given vector2 and z-component
     *
     * @param vector The vector
     * @param z      The z-component
     */
    public Vector3(Vector2 vector, float z) {
        this.set(vector.x, vector.y, z);
    }

    @Override
    public Vector3 clone() {
        return new Vector3(this);
    }

    /**
     * Sets the vector to the given components
     *
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     * @return This vector for chaining
     */
    public Vector3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /**
     * Sets all components of the vector to the given value
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector3 set(float value) {
        return this.set(value, value, value);
    }

    /**
     * Sets the vector to the given vector
     *
     * @param vector The vector
     */
    public Vector3 set(Vector3 vector) {
        return this.set(vector.x, vector.y, vector.z);
    }

    /**
     * Sets the vectors components
     *
     * @param values The data to read
     * @param offset The start index
     */
    public void set(float[] values, int offset) {
        x = values[offset];
        y = values[offset + 1];
        z = values[offset + 2];
    }

    /**
     * Gets the vectors components
     *
     * @param values The data to store
     * @param offset The start index
     */
    public void get(float[] values, int offset) {
        values[offset] = x;
        values[offset + 1] = y;
        values[offset + 2] = z;
    }

    /**
     * Adds the given x,y,z values to this component
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @return This vector for chaining
     */
    public Vector3 add(float x, float y, float z) {
        return this.set(this.x + x, this.y + y, this.z + z);
    }

    /**
     * Adds the given value to all components of the vector.
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector3 add(float value) {
        return this.add(value, value, value);
    }

    /**
     * Adds the given vector to this component
     *
     * @param vector The vector
     * @return This vector for chaining
     */
    public Vector3 add(Vector3 vector) {
        return this.add(vector.x, vector.y, vector.z);
    }

    /**
     * Subtracts the given x,y,z values to this component
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @return This vector for chaining
     */
    public Vector3 sub(float x, float y, float z) {
        return this.set(this.x - x, this.y - y, this.z - z);
    }

    /**
     * Subtracts the given value from all components of this vector
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector3 sub(float value) {
        return this.sub(value, value, value);
    }

    /**
     * Subtracts the other vector from this vector.
     *
     * @param vector The vector
     * @return This vector for chaining
     */
    public Vector3 sub(Vector3 vector) {
        return this.sub(vector.x, vector.y, vector.z);
    }

    /**
     * Scales this vector by the given x,y,z values
     *
     * @param x X value
     * @param y Y value
     * @param z Z value
     * @return This vector for chaining
     */
    public Vector3 mul(float x, float y, float z) {
        return this.set(this.x * x, this.y * y, this.z * z);
    }

    /**
     * Scales this vector by the the given value to all components of this vector
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector3 mul(float value) {
        return this.mul(value, value, value);
    }

    /**
     * Scales this vector by the given vector.
     *
     * @param vector The vector
     * @return This vector for chaining
     */
    public Vector3 mul(Vector3 vector) {
        return this.mul(vector.x, vector.y, vector.z);
    }

    /**
     * Left-multiplies the vector by the given matrix.
     *
     * @param matrix The matrix
     * @return This vector for chaining
     */
    public Vector3 mul(Matrix3 matrix) {
        final float l_mat[] = matrix.val;
        return set(
                x * l_mat[Matrix3.M00] + y * l_mat[Matrix3.M01] + z * l_mat[Matrix3.M02],
                x * l_mat[Matrix3.M10] + y * l_mat[Matrix3.M11] + z * l_mat[Matrix3.M12],
                x * l_mat[Matrix3.M20] + y * l_mat[Matrix3.M21] + z * l_mat[Matrix3.M22]);
    }

    /**
     * Left-multiplies the vector by the given matrix, assuming the fourth (w) component of the vector is 1.
     *
     * @param matrix The matrix
     * @return This vector for chaining
     */
    public Vector3 mul(Matrix4 matrix) {
        final float l_mat[] = matrix.val;
        return this.set(
                x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M01] + z * l_mat[Matrix4.M02] + l_mat[Matrix4.M03],
                x * l_mat[Matrix4.M10] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M12] + l_mat[Matrix4.M13],
                x * l_mat[Matrix4.M20] + y * l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22] + l_mat[Matrix4.M23]);
    }

    /**
     * Multiplies the vector by the given {@link Quaternion}.
     *
     * @return This vector for chaining
     */
    public Vector3 mul(Quaternion quat) {
        return quat.transform(this);
    }

    /**
     * The euclidean length
     */
    public float length() {
        return Mathf.sqrt(x * x + y * y + z * z);
    }

    /**
     * Set the components to fit the euclidean length
     *
     * @param len Length
     * @return This vector for chaining
     */
    public Vector3 length(float len) {
        return lengthSqr(len * len);
    }

    /**
     * The squared euclidean length
     */
    public float lengthSqr() {
        return x * x + y * y + z * z;
    }

    /**
     * Set the components to fit the squared euclidean length
     *
     * @param len Squared Length
     * @return This vector for chaining
     */
    public Vector3 lengthSqr(float len) {
        float oldLen2 = lengthSqr();
        return (oldLen2 == 0 || oldLen2 == len) ? this : mul(Mathf.sqrt(len / oldLen2));
    }

    /**
     * Scale this vector to the length one
     *
     * @return This vector for chaining.
     */
    public Vector3 normalize() {
        final float len2 = this.lengthSqr();
        if (len2 == 0f || len2 == 1f) return this;
        return this.mul(1f / (float) Math.sqrt(len2));
    }

    /**
     * Returns true if this vector has length equals to one
     *
     * @return Returns true if this vector has length equals to one
     */
    public boolean isNormalized() {
        return Mathf.isEqual(length(), 1f);
    }

    /**
     * Returns the dot product between this and the given vector.
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @return The dot product
     */
    public float dot(float x, float y, float z) {
        return this.x * x + this.y * y + this.z * z;
    }

    /**
     * Returns the dot product between this and the given vector.
     *
     * @return The dot product
     */
    public float dot(Vector3 vector) {
        return x * vector.x + y * vector.y + z * vector.z;
    }

    /**
     * Sets this vector to the cross product between it and the other vector.
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @return This vector for chaining
     */
    public Vector3 cross(float x, float y, float z) {
        return this.set(this.y * z - this.z * y, this.z * x - this.x * z, this.x * y - this.y * x);
    }

    /**
     * Sets this vector to the cross product between it and the other vector.
     *
     * @param vector The other vector
     * @return This vector for chaining
     */
    public Vector3 cross(Vector3 vector) {
        return this.cross(vector.x, vector.y, vector.z);
    }


    /**
     * Returns the euclidean distance between this point and the given point
     *
     * @param x The x-component of the other point
     * @param y The y-component of the other point
     * @param z The z-component of the other point
     * @return the distance between this point and the given point
     */
    public float distance(float x, float y, float z) {
        final float a = x - this.x;
        final float b = y - this.y;
        final float c = z - this.z;
        return Mathf.sqrt(a * a + b * b + c * c);
    }

    /**
     * Returns the euclidean distance between this point and the given point
     *
     * @param vector The vector
     * @return The distance
     */
    public float distance(Vector3 vector) {
        return distance(vector.x, vector.y, vector.z);
    }

    /**
     * Returns the squared distance between this point and the given point
     *
     * @param x The x-component of the other point
     * @param y The y-component of the other point
     * @param z The z-component of the other point
     * @return The squared distance
     */
    public float distanceSqr(float x, float y, float z) {
        final float a = x - this.x;
        final float b = y - this.y;
        final float c = z - this.z;
        return a * a + b * b + c * c;
    }

    /**
     * Returns the squared euclidean distance between this point and the given point
     *
     * @param vector The vector
     * @return The squared distance
     */
    public float distanceSqr(Vector3 vector) {
        return distanceSqr(vector.x, vector.y, vector.z);
    }

    /**
     * Multiplies this vector by the given matrix dividing by w, assuming the fourth (w) component of the vector is 1. This is
     * mostly used to project/unproject vectors via a perspective projection matrix.
     *
     * @param matrix The matrix.
     * @return This vector for chaining
     */
    public Vector3 prj(Matrix4 matrix) {
        final float l_mat[] = matrix.val;
        final float l_w = 1f / (x * l_mat[Matrix4.M30] + y * l_mat[Matrix4.M31] + z * l_mat[Matrix4.M32] + l_mat[Matrix4.M33]);
        return this.set(
                (x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M01] + z * l_mat[Matrix4.M02] + l_mat[Matrix4.M03]) * l_w,
                (x * l_mat[Matrix4.M10] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M12] + l_mat[Matrix4.M13]) * l_w,
                (x * l_mat[Matrix4.M20] + y * l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22] + l_mat[Matrix4.M23]) * l_w);
    }

    /**
     * Multiplies this vector by the first three columns of the matrix, essentially only applying rotation and scaling.
     *
     * @param matrix The matrix
     * @return This vector for chaining
     */
    public Vector3 rot(final Matrix4 matrix) {
        final float l_mat[] = matrix.val;
        return this.set(
                x * l_mat[Matrix4.M00] + y * l_mat[Matrix4.M01] + z * l_mat[Matrix4.M02],
                x * l_mat[Matrix4.M10] + y * l_mat[Matrix4.M11] + z * l_mat[Matrix4.M12],
                x * l_mat[Matrix4.M20] + y * l_mat[Matrix4.M21] + z * l_mat[Matrix4.M22]);
    }

    /**
     * Linearly interpolates between this vector and the target vector by alpha
     *
     * @param target The target vector
     * @param alpha  The interpolation coefficient
     * @return This vector for chaining
     */
    public Vector3 lerp(Vector3 target, float alpha) {
        x += alpha * (target.x - x);
        y += alpha * (target.y - y);
        z += alpha * (target.z - z);
        return this;
    }

    /**
     * Spherically interpolates between this vector and the target vector by alpha which is in the range [0,1]. The result is
     * stored in this vector.
     *
     * @param target The target vector
     * @param alpha  The interpolation coefficient
     * @return This vector for chaining
     */
    public Vector3 slerp(Vector3 target, float alpha) {
        final float dot = dot(target);
        // If the inputs are too close for comfort, simply linearly interpolate.
        if (dot > 0.9995 || dot < -0.9995) return lerp(target, alpha);

        // theta0 = angle between input vectors
        final float theta0 = Mathf.acos(dot);
        // theta = angle between this vector and result
        final float theta = theta0 * alpha;

        final float st = Mathf.sin(theta);
        final float tx = target.x - x * dot;
        final float ty = target.y - y * dot;
        final float tz = target.z - z * dot;
        final float l2 = tx * tx + ty * ty + tz * tz;
        final float dl = st * ((l2 < 0.0001f) ? 1f : 1f / (float) Math.sqrt(l2));

        return mul(Mathf.cos(theta)).add(tx * dl, ty * dl, tz * dl).normalize();
    }

    public Vector3 moveTowards(Vector3 target, float maxDistance) {
        float distance = distanceSqr(target);
        if (distance > maxDistance * maxDistance) {
            return this.sub(target).normalize().mul(maxDistance);
        } else {
            return set(target);
        }
    }

    /**
     * Clamp this vector to be between max and min
     *
     * @param min min value
     * @param max max value
     * @return This vector for chaining
     */
    public Vector3 clamp(float min, float max) {
        final float len2 = lengthSqr();
        if (len2 == 0f) return this;
        float max2 = max * max;
        if (len2 > max2) return mul((float) Math.sqrt(max2 / len2));
        float min2 = min * min;
        if (len2 < min2) return mul((float) Math.sqrt(min2 / len2));
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.hashCode(x);
        result = prime * result + Float.hashCode(y);
        result = prime * result + Float.hashCode(z);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Vector3 other = (Vector3) obj;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }

    /**
     * Returns the euclidean length
     *
     * @param x The x-component of the point
     * @param y The y-component of the point
     * @param z The z-component of the point
     * @return The euclidean length
     */
    public static float length(float x, float y, float z) {
        return Mathf.sqrt(x * x + y * y + z * z);
    }

    /**
     * Returns the squared euclidean length
     *
     * @param x The x-component of the point
     * @param y The y-component of the point
     * @param z The z-component of the point
     * @return The squared euclidean length
     */
    public static float lengthSqr(float x, float y, float z) {
        return x * x + y * y + z * z;
    }

    /**
     * Returns the distance between the given points
     *
     * @param x1 The x-component of the point
     * @param y1 The y-component of the point
     * @param z1 The z-component of the point
     * @param x2 The x-component of the other point
     * @param y2 The y-component of the other point
     * @param z2 The z-component of the other point
     * @return The distance
     */
    public static float distance(float x1, float y1, float z1, float x2, float y2, float z2) {
        final float a = x2 - x1;
        final float b = y2 - y1;
        final float c = z2 - z1;
        return Mathf.sqrt(a * a + b * b + c * c);
    }

    /**
     * Returns the squared distance between the given points
     *
     * @param x1 The x-component of the point
     * @param y1 The y-component of the point
     * @param z1 The z-component of the point
     * @param x2 The x-component of the other point
     * @param y2 The y-component of the other point
     * @param z2 The z-component of the other point
     * @return The squared distance
     */
    public static float distanceSqr(float x1, float y1, float z1, float x2, float y2, float z2) {
        final float a = x2 - x1;
        final float b = y2 - y1;
        final float c = z2 - z1;
        return a * a + b * b + c * c;
    }

    /**
     * Returns the dot product between the two vectors
     *
     * @param x1 The x-component of the point
     * @param y1 The y-component of the point
     * @param z1 The z-component of the point
     * @param x2 The x-component of the other point
     * @param y2 The y-component of the other point
     * @param z2 The z-component of the other point
     * @return The dot product
     */
    public static float dot(float x1, float y1, float z1, float x2, float y2, float z2) {
        return x1 * x2 + y1 * y2 + z1 * z2;
    }
}