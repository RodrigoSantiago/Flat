package flat.math;

import java.io.Serializable;

/**
 * A 4D point class; useful for Matrices and 3D transforms
 */
public final class Vector4 implements Serializable {

    private static final long serialVersionUID = -775706366125314150L;

    public float x, y, z, w;

    /**
     * Constructs a vector at (0,0,0,0)
     */
    public Vector4() {
    }

    /**
     * Creates a vector with the given components
     *
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     * @param w The w-component
     */
    public Vector4(float x, float y, float z, float w) {
        set(x, y, z, w);
    }

    /**
     * Creates a vector from the given vector
     *
     * @param vector The vector
     */
    public Vector4(Vector4 vector) {
        set(vector);
    }

    /**
     * Creates a vector with the given components
     *
     * @param values The data to read
     * @param offset The start index
     */
    public Vector4(float[] values, int offset) {
        set(values, offset);
    }

    /**
     * Sets the vector to the given components
     *
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     * @param w The w-component
     * @return this vector for chaining
     */
    public Vector4 set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    /**
     * Sets all components of the vector to the given value
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector4 set(float value) {
        return set(value, value, value, value);
    }

    /**
     * Sets the vector to the given vector
     *
     * @param vector This vector for chaining
     */
    public Vector4 set(Vector4 vector) {
        return set(vector.x, vector.y, vector.z, vector.w);
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
        w = values[offset + 3];
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
        values[offset + 3] = w;
    }

    /**
     * Adds the given values to this component
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @param w The w-component of the other vector
     * @return This vector for chaining
     */
    public Vector4 add(float x, float y, float z, float w) {
        return this.set(this.x + x, this.y + y, this.z + z, this.w + w);
    }

    /**
     * Adds the given value to all components of the vector.
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector4 add(float value) {
        return this.add(value, value, value, value);
    }

    /**
     * Adds the given vector to this component
     *
     * @param vector The vector
     * @return This vector for chaining
     */
    public Vector4 add(Vector4 vector) {
        return this.add(vector.x, vector.y, vector.z, vector.w);
    }


    /**
     * Subtracts the given x,y,z values to this component
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @param z The z-component of the other vector
     * @param w The w-component of the other vector
     * @return This vector for chaining
     */
    public Vector4 sub(float x, float y, float z, float w) {
        return this.set(this.x - x, this.y - y, this.z - z, this.w - w);
    }

    /**
     * Subtracts the given value from all components of this vector
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector4 sub(float value) {
        return this.sub(value, value, value, value);
    }

    /**
     * Subtracts the other vector from this vector.
     *
     * @param vector The vector
     * @return This vector for chaining
     */
    public Vector4 sub(Vector4 vector) {
        return this.sub(vector.x, vector.y, vector.z, vector.w);
    }

    /**
     * Scales this vector by the given x,y,z values
     *
     * @param x X value
     * @param y Y value
     * @param z Z value
     * @return This vector for chaining
     */
    public Vector4 mul(float x, float y, float z, float w) {
        return this.set(this.x * x, this.y * y, this.z * z, this.w * w);
    }

    /**
     * Scales this vector by the the given value to all components of this vector
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector4 mul(float value) {
        return this.mul(value, value, value, value);
    }

    /**
     * Scales this vector by the given vector.
     *
     * @param vector The vector
     * @return This vector for chaining
     */
    public Vector4 mul(Vector4 vector) {
        return this.mul(vector.x, vector.y, vector.z, vector.w);
    }

    /**
     * The euclidean length
     */
    public float length() {
        return Mathf.sqrt(x * x + y * y + z * z + w * w);
    }

    /**
     * Set the components to fit the euclidean length
     *
     * @param len Length
     * @return This vector for chaining
     */
    public Vector4 length(float len) {
        return lengthSqr(len * len);
    }

    /**
     * The squared euclidean length
     */
    public float lengthSqr() {
        return x * x + y * y + z * z + w * w;
    }

    /**
     * Set the components to fit the squared euclidean length
     *
     * @param len Squared Length
     * @return This vector for chaining
     */
    public Vector4 lengthSqr(float len) {
        float oldLen2 = lengthSqr();
        return (oldLen2 == 0 || oldLen2 == len) ? this : mul(Mathf.sqrt(len / oldLen2));
    }

    /**
     * Multiplies this vector by a matrix (V * M) and stores the result in the object provided.
     *
     * @return This vector for chaining
     */
    public Vector4 mul(Matrix4 mat) {
        return set(
                mat.val[Matrix4.M00] * x + mat.val[Matrix4.M01] * y + mat.val[Matrix4.M02] * z + mat.val[Matrix4.M03] * w,
                mat.val[Matrix4.M10] * x + mat.val[Matrix4.M11] * y + mat.val[Matrix4.M12] * z + mat.val[Matrix4.M13] * w,
                mat.val[Matrix4.M20] * x + mat.val[Matrix4.M21] * y + mat.val[Matrix4.M22] * z + mat.val[Matrix4.M23] * w,
                mat.val[Matrix4.M30] * x + mat.val[Matrix4.M31] * y + mat.val[Matrix4.M32] * z + mat.val[Matrix4.M33] * w);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        Vector4 other = (Vector4) obj;
        return (x == other.x && y == other.y && z == other.z && w == other.w);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + ", " + w + "]";
    }

    @Override
    public int hashCode() {
        return Float.hashCode(x) ^ Float.hashCode(y) ^ Float.hashCode(z) ^ Float.hashCode(w);
    }
}