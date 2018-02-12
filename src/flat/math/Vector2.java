package flat.math;

import flat.math.util.Platform;

import java.io.Serializable;

/**
 * A 2D point class; useful for 2D translations, scales and geometric operations
 */
public final class Vector2 implements Serializable {

    public float x;
    public float y;

    /**
     * Constructs a vector at (0,0)
     */
    public Vector2() {
    }

    /**
     * Creates a vector with the given components
     *
     * @param x The x-component
     * @param y The y-component
     */
    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a vector from the given vector
     *
     * @param vector The vector
     */
    public Vector2(Vector2 vector) {
        set(vector);
    }

    @Override
    public Vector2 clone() {
        return new Vector2(this);
    }

    /**
     * Sets the vector to the given components
     *
     * @param x The x-component
     * @param y The y-component
     * @return This vector for chaining
     */
    public Vector2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Sets all components of the vector to the given value
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector2 set(float value) {
        return this.set(value, value);
    }

    /**
     * Sets the vector to the given vector
     *
     * @param vector The vector
     */
    public Vector2 set(Vector2 vector) {
        return this.set(vector.x, vector.y);
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
    }

    /**
     * Adds the given x,y values to this component
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @return This vector for chaining
     */
    public Vector2 add(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    /**
     * Adds the given value to all components of the vector.
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector2 add(float value) {
        return this.add(value, value);
    }

    /**
     * Adds the given vector to this component
     *
     * @param vector The vector
     * @return This vector for chaining
     */
    public Vector2 add(Vector2 vector) {
        return this.add(vector.x, vector.y);
    }

    /**
     * Subtracts the given x,y values to this component
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @return This vector for chaining
     */
    public Vector2 sub(float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }

    /**
     * Subtracts the given value from all components of this vector
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector2 sub(float value) {
        return this.sub(value, value);
    }

    /**
     * Subtracts the other vector from this vector.
     *
     * @param vector The vector
     * @return This vector for chaining
     */
    public Vector2 sub(Vector2 vector) {
        return this.sub(vector.x, vector.y);
    }

    /**
     * Scales this vector by the given x,y values
     *
     * @param x X value
     * @param y Y value
     * @return This vector for chaining
     */
    public Vector2 mul(float x, float y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    /**
     * Scales this vector by the the given value to all components of this vector
     *
     * @param value The value
     * @return This vector for chaining
     */
    public Vector2 mul(float value) {
        return this.mul(value, value);
    }

    /**
     * Scales this vector by the given vector.
     *
     * @param vector The vector
     * @return This vector for chaining
     */
    public Vector2 mul(Vector2 vector) {
        return this.mul(vector.x, vector.y);
    }

    /**
     * Left-multiplies the vector by the given matrix.
     *
     * @param matrix The matrix
     * @return This vector for chaining
     */
    public Vector2 mul(Matrix3 matrix) {
        return matrix.transform(this);
    }

    /**
     * The euclidean length
     */
    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Set the components to fit the euclidean length
     *
     * @param len Length
     * @return This vector for chaining
     */
    public Vector2 length(float len) {
        return lengthSqr(len < 0 ? -len * len : len * len);
    }

    /**
     * The squared euclidean length
     */
    public float lengthSqr() {
        return x * x + y * y;
    }

    /**
     * Set the components to fit the squared euclidean length
     *
     * @param len Squared Length
     * @return This vector for chaining
     */
    public Vector2 lengthSqr(float len) {
        float oldLenSqr = lengthSqr();
        if (len < 0) {
            len = -len;
            mul(-1);
        }
        return (oldLenSqr == 0 || oldLenSqr == len) ? this : mul((float) Math.sqrt(len / oldLenSqr));
    }

    /**
     * Scale this vector to the length one
     *
     * @return This vector for chaining
     */
    public Vector2 normalize() {
        float len = length();
        if (len != 0) {
            x /= len;
            y /= len;
        }
        return this;
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
     * @return The dot product
     */
    public float dot(float x, float y) {
        return x * this.x + y * this.y;
    }

    /**
     * Returns the dot product between this and the given vector.
     *
     * @return The dot product
     */
    public float dot(Vector2 vector) {
        return this.dot(vector.x, vector.y);
    }

    /**
     * Sets this vector to the cross product between it and the other vector.
     *
     * @param x The x-component of the other vector
     * @param y The y-component of the other vector
     * @return This vector for chaining
     */
    public float cross(float x, float y) {
        return this.x * y - this.y * x;
    }

    /**
     * Sets this vector to the cross product between it and the other vector.
     *
     * @param vector The other vector
     * @return This vector for chaining
     */
    public float cross(Vector2 vector) {
        return this.cross(vector.x, vector.y);
    }

    /**
     * Returns the distance between this point and the given point
     *
     * @param x The x-component of the other point
     * @param y The y-component of the other point
     * @return The distance
     */
    public float distance(float x, float y) {
        final float x_d = x - this.x;
        final float y_d = y - this.y;
        return (float) Math.sqrt(x_d * x_d + y_d * y_d);
    }

    /**
     * Returns the distance between this point and the given point
     *
     * @param vector The vector
     * @return The distance
     */
    public float distance(Vector2 vector) {
        return this.distance(vector.x, vector.y);
    }

    /**
     * Returns the squared distance between this point and the given point
     *
     * @param x The x-component of the other point
     * @param y The y-component of the other point
     * @return The squared distance
     */
    public float distanceSqr(float x, float y) {
        final float x_d = x - this.x;
        final float y_d = y - this.y;
        return x_d * x_d + y_d * y_d;
    }

    /**
     * Returns the squared distance between this point and the given point
     *
     * @param vector The vector
     * @return The squared distance
     */
    public float distanceSqr(Vector2 vector) {
        return this.distanceSqr(vector.x, vector.y);
    }

    /**
     * Returns the angle between this point and the given point
     *
     * @param x The x-component of the other point
     * @param y The y-component of the other point
     * @return The angle in degrees
     */
    public float angle(float x, float y) {
        return Mathf.toDegrees(Mathf.atan2(this.y - y, x - this.x));
    }

    /**
     * Returns the angle between this point and the given point
     *
     * @param vector The vector
     * @return The angle in degrees
     */
    public float angle(Vector2 vector) {
        return angle(vector.x, vector.y);
    }

    /**
     * Linearly interpolates between this vector and the target vector
     *
     * @param target The target vector
     * @param alpha  The interpolation coefficient
     * @return This vector for chaining
     */
    public Vector2 lerp(Vector2 target, float alpha) {
        final float invAlpha = 1.0f - alpha;
        this.x = (x * invAlpha) + (target.x * alpha);
        this.y = (y * invAlpha) + (target.y * alpha);
        return this;
    }

    public Vector2 moveTowards(Vector2 target, float maxDistance) {
        float distance = distanceSqr(target);
        if (distance > maxDistance * maxDistance) {
            return this.sub(target).normalize().mul(maxDistance);
        } else {
            return set(target);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Platform.hashCode(x);
        result = prime * result + Platform.hashCode(y);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (obj.getClass() != getClass()) return false;
        Vector2 other = (Vector2) obj;
        return other.x == x && other.y == y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    /**
     * Returns the euclidean length
     *
     * @param x The x-component of the point
     * @param y The y-component of the point
     * @return The euclidean length
     */
    public static float length(float x, float y) {
        return Mathf.sqrt(x * x + y * y);
    }

    /**
     * Returns the squared euclidean length
     *
     * @param x The x-component of the point
     * @param y The y-component of the point
     * @return The squared euclidean length
     */
    public static float lengthSqr(float x, float y) {
        return x * x + y * y;
    }

    /**
     * Returns the distance between the given points
     *
     * @param x1 The x-component of the point
     * @param y1 The y-component of the point
     * @param x2 The x-component of the other point
     * @param y2 The y-component of the other point
     * @return The distance
     */
    public static float distance(float x1, float y1, float x2, float y2) {
        final float x_d = x2 - x1;
        final float y_d = y2 - y1;
        return Mathf.sqrt(x_d * x_d + y_d * y_d);
    }

    /**
     * Returns the squared distance between the given points
     *
     * @param x1 The x-component of the point
     * @param y1 The y-component of the point
     * @param x2 The x-component of the other point
     * @param y2 The y-component of the other point
     * @return the squared distance
     */
    public static float distanceSqr(float x1, float y1, float x2, float y2) {
        final float x_d = x2 - x1;
        final float y_d = y2 - y1;
        return x_d * x_d + y_d * y_d;
    }

    /**
     * Returns the angle between the given points
     *
     * @param x1 The x-component of the point
     * @param y1 The y-component of the point
     * @param x2 The x-component of the other point
     * @param y2 The y-component of the other point
     * @return The angle in degrees
     */
    public static float angle(float x1, float y1, float x2, float y2) {
        return Mathf.toDegrees(Mathf.atan2(y1 - y2, x2 - x1));
    }

    /**
     * Returns the dot product between the two vectors
     *
     * @param x1 The x-component of the point
     * @param y1 The y-component of the point
     * @param x2 The x-component of the other point
     * @param y2 The y-component of the other point
     * @return The dot product
     */
    public static float dot(float x1, float y1, float x2, float y2) {
        return x1 * x2 + y1 * y2;
    }
}