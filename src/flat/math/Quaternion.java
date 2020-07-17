package flat.math;

import flat.math.util.NoninvertibleTransformException;

import java.io.Serializable;

/**
 * A Quaternion; useful for 3D rotations.
 */
public final class Quaternion implements Serializable {

    private static final long serialVersionUID = -7661875440774897168L;

    public float x;
    public float y;
    public float z;
    public float w;

    /**
     * Constructor, identity quaternion
     */
    public Quaternion() {
        identity();
    }

    /**
     * Constructor, sets the four components of the quaternion.
     *
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     * @param w The w-component
     */
    public Quaternion(float x, float y, float z, float w) {
        this.set(x, y, z, w);
    }

    /**
     * Constructor, sets the quaternion components from the given quaternion.
     *
     * @param quaternion The quaternion to copy.
     */
    public Quaternion(Quaternion quaternion) {
        this.set(quaternion);
    }

    /**
     * Constructor, sets the quaternion from the given axis vector and the angle around that axis in degrees.
     *
     * @param axis  The axis
     * @param angle The angle in degrees.
     */
    public Quaternion(Vector3 axis, float angle) {
        this.setFromAxis(axis, angle);
    }

    @Override
    public Quaternion clone() {
        return new Quaternion(this);
    }

    /**
     * Sets the components of the quaternion
     *
     * @param x The x-component
     * @param y The y-component
     * @param z The z-component
     * @param w The w-component
     * @return This quaternion for chaining
     */
    public Quaternion set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    /**
     * Sets the quaternion components from the given quaternion.
     *
     * @param quaternion The quaternion.
     * @return This quaternion for chaining
     */
    public Quaternion set(Quaternion quaternion) {
        return this.set(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
    }

    /**
     * Sets the quaternion components from the given axis and angle around that axis.
     *
     * @param x     X direction of the axis
     * @param y     Y direction of the axis
     * @param z     Z direction of the axis
     * @param angle The angle in degrees
     * @return This quaternion for chaining
     */
    public Quaternion setFromAxis(float x, float y, float z, float angle) {
        angle = Mathf.toRadians(angle);

        float d = Vector3.length(x, y, z);
        if (d == 0f) return identity();
        d = 1f / d;
        float l_ang = angle < 0 ? Mathf.PI2 - (-angle % Mathf.PI2) : angle % Mathf.PI2;
        float l_sin = (float) Math.sin(l_ang / 2);
        float l_cos = (float) Math.cos(l_ang / 2);
        return this.set(d * x * l_sin, d * y * l_sin, d * z * l_sin, l_cos).normalize();
    }

    /**
     * Sets the quaternion components from the given axis and angle around that axis.
     *
     * @param axis  The axis
     * @param angle The angle in degrees
     * @return This quaternion for chaining
     */
    public Quaternion setFromAxis(Vector3 axis, float angle) {
        return setFromAxis(axis.x, axis.y, axis.z, angle);
    }

    /**
     * Get the axis-angle representation of the rotation in degrees
     *
     * @param axis vector which will receive the axis
     * @return The angle in degrees
     */
    public float getAxisAngle(Vector3 axis) {
        if (this.w > 1)
            this.normalize(); // if w>1 acos and sqrt will produce errors, this cant happen if quaternion is normalised
        float angle = 2 * Mathf.acos(this.w);
        double s = Math.sqrt(1 - this.w * this.w); // assuming quaternion normalised then w is less than 1, so term always positive.
        if (s < Mathf.EPSILON) { // test to avoid divide by zero, s is always positive due to sqrt
            // if s close to zero then direction of axis not important
            axis.x = this.x; // if it is important that axis is normalised then replace with x=1; y=z=0;
            axis.y = this.y;
            axis.z = this.z;
        } else {
            axis.x = (float) (this.x / s); // normalise axis
            axis.y = (float) (this.y / s);
            axis.z = (float) (this.z / s);
        }

        return Mathf.toDegrees(angle);
    }

    /**
     * Get the angle in degrees of the rotation this quaternion represents. Use {@link #getAxisAngle(Vector3)} to get both the axis
     * and the angle of this rotation
     *
     * @return The angle in degrees
     */
    public float getAngle() {
        return Mathf.toDegrees((float) (2.0 * Math.acos((this.w > 1) ? (this.w / length()) : this.w)));
    }

    /**
     * Sets the quaternion to the given euler angles in degrees.
     *
     * @param x the rotation around the x axis in degrees
     * @param y the rotation around the y axis in degrees
     * @param z the rotation around the z axis in degrees
     * @return This quaternion for chaining
     */
    public Quaternion setEuler(float x, float y, float z) {
        x = Mathf.toRadians(x);
        y = Mathf.toRadians(y);
        z = Mathf.toRadians(z);
        final float hr = z * 0.5f;
        final float shr = (float) Math.sin(hr);
        final float chr = (float) Math.cos(hr);
        final float hp = x * 0.5f;
        final float shp = (float) Math.sin(hp);
        final float chp = (float) Math.cos(hp);
        final float hy = y * 0.5f;
        final float shy = (float) Math.sin(hy);
        final float chy = (float) Math.cos(hy);
        final float chy_shp = chy * shp;
        final float shy_chp = shy * chp;
        final float chy_chp = chy * chp;
        final float shy_shp = shy * shp;

        this.x = (chy_shp * chr) + (shy_chp * shr); // cos(y/2) * sin(x/2) * cos(z/2) + sin(y/2) * cos(x/2) * sin(z/2)
        this.y = (shy_chp * chr) - (chy_shp * shr); // sin(y/2) * cos(x/2) * cos(z/2) - cos(y/2) * sin(x/2) * sin(z/2)
        this.z = (chy_chp * shr) - (shy_shp * chr); // cos(y/2) * cos(x/2) * sin(z/2) - sin(y/2) * sin(x/2) * cos(z/2)
        w = (chy_chp * chr) + (shy_shp * shr); // cos(y/2) * cos(x/2) * cos(z/2) + sin(y/2) * sin(x/2) * sin(z/2)
        return this;
    }

    /**
     * Get the pitch euler angle in degrees, which is the rotation around the x axis. Requires that this quaternion is normalized.
     *
     * @return The rotation around the x axis in degrees (between -90 and +90)
     */
    public float getEulerX() {
        final int pole = getGimbalPole();
        return Mathf.toDegrees(pole == 0 ? (float) Math.asin(Mathf.clamp(2f * (w * x - z * y), -1f, 1f)) : (float) pole * Mathf.PI * 0.5f);
    }

    /**
     * Get the yaw euler angle in degrees, which is the rotation around the y axis. Requires that this quaternion is normalized.
     *
     * @return The rotation around the y axis in degrees (between -180 and +180)
     */
    public float getEulerY() {
        final int pole = getGimbalPole();
        return Mathf.toDegrees(pole == 0 ? Mathf.atan2(2f * (y * w + x * z), 1f - 2f * (y * y + x * x)) : 0f);
    }

    /**
     * Get the roll euler angle in degrees, which is the rotation around the z axis. Requires that this quaternion is normalized.
     *
     * @return The rotation around the z axis in degrees (between -180 and +180)
     */
    public float getEulerZ() {
        final int pole = getGimbalPole();
        return Mathf.toDegrees(pole == 0 ? Mathf.atan2(2f * (w * z + y * x), 1f - 2f * (x * x + z * z)) : (float) pole * 2f * Mathf.atan2(y, w));
    }

    /**
     * Get the pole of the gimbal lock, if any.
     *
     * @return Positive (+1) for north pole, negative (-1) for south pole, zero (0) when no gimbal lock
     */
    public int getGimbalPole() {
        final float t = y * x + z * w;
        return t > 0.499f ? 1 : (t < -0.499f ? -1 : 0);
    }

    /**
     * Set this quaternion to the inverse.
     *
     * @throws NoninvertibleTransformException if the transform is not invertible.
     * @return This quaternion for chaining
     */
    public Quaternion invert() {
        float d = x * x + y * y + z * z + w * w;
        if (d == 0) throw new NoninvertibleTransformException(this.toString());
        return set(x / d, -y / d, -z / d, -w / d);
    }

    /**
     * Multiplies the components of this quaternion with the given scalar.
     *
     * @param scalar the scalar.
     * @return This quaternion for chaining
     */
    public Quaternion mul(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        this.w *= scalar;
        return this;
    }

    /**
     * Multiplies this quaternion with another one in the form of this = this * other
     *
     * @param x the x component of the other quaternion to multiply with
     * @param y the y component of the other quaternion to multiply with
     * @param z the z component of the other quaternion to multiply with
     * @param w the w component of the other quaternion to multiply with
     * @return This quaternion for chaining
     */
    public Quaternion mul(float x, float y, float z, float w) {
        final float newX = this.w * x + this.x * w + this.y * z - this.z * y;
        final float newY = this.w * y + this.y * w + this.z * x - this.x * z;
        final float newZ = this.w * z + this.z * w + this.x * y - this.y * x;
        final float newW = this.w * w - this.x * x - this.y * y - this.z * z;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    /**
     * Multiplies this quaternion with another one in the form of this = this * other
     *
     * @param other Quaternion to multiply with
     * @return This quaternion for chaining
     */
    public Quaternion mul(Quaternion other) {
        return mul(other.x, other.y, other.z, other.w);
    }

    /**
     * Multiplies this quaternion with another one in the form of this = other * this
     *
     * @param x the x component of the other quaternion to multiply with
     * @param y the y component of the other quaternion to multiply with
     * @param z the z component of the other quaternion to multiply with
     * @param w the w component of the other quaternion to multiply with
     * @return This quaternion for chaining
     */
    public Quaternion mulLeft(final float x, final float y, final float z, final float w) {
        final float newX = w * this.x + x * this.w + y * this.z - z * this.y;
        final float newY = w * this.y + y * this.w + z * this.x - x * this.z;
        final float newZ = w * this.z + z * this.w + x * this.y - y * this.x;
        final float newW = w * this.w - x * this.x - y * this.y - z * this.z;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    /**
     * Multiplies this quaternion with another one in the form of this = other * this
     *
     * @param other Quaternion to multiply with
     * @return This quaternion for chaining
     */
    public Quaternion mulLeft(Quaternion other) {
        return mulLeft(other.x, other.y, other.z, other.w);
    }

    /**
     * Sets the quaternion to an identity Quaternion
     *
     * @return This quaternion for chaining
     */
    public Quaternion identity() {
        return this.set(0, 0, 0, 1);
    }

    /**
     * @return If this quaternion is an identity Quaternion
     */
    public boolean isIdentity() {
        return Mathf.isZero(x) && Mathf.isZero(y) && Mathf.isZero(z) && Mathf.isEqual(w, 1f);
    }

    /**
     * Returns the euclidean length of this quaternion
     *
     * @return the euclidean length of this quaternion
     */
    public float length() {
        return Mathf.sqrt(x * x + y * y + z * z + w * w);
    }

    /**
     * Returns the squared euclidean length of this quaternion
     *
     * @return the squared euclidean length of this quaternion
     */
    public float lengthSqr() {
        return x * x + y * y + z * z + w * w;
    }

    /**
     * Normalizes this quaternion to unit length
     *
     * @return This quaternion for chaining
     */
    public Quaternion normalize() {
        float len = lengthSqr();
        if (len != 0.f && !Mathf.isEqual(len, 1f)) {
            len = Mathf.sqrt(len);
            w /= len;
            x /= len;
            y /= len;
            z /= len;
        }
        return this;
    }

    /**
     * Conjugate the quaternion.
     *
     * @return This quaternion for chaining
     */
    public Quaternion conjugate() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    /**
     * Get the dot product between this and the other quaternion (commutative).
     *
     * @param x the x component of the other quaternion
     * @param y the y component of the other quaternion
     * @param z the z component of the other quaternion
     * @param w the w component of the other quaternion
     * @return The dot product of this and the other quaternion.
     */
    public float dot(float x, float y, float z, float w) {
        return this.x * x + this.y * y + this.z * z + this.w * w;
    }

    /**
     * Get the dot product between this and the other quaternion (commutative).
     *
     * @param other the other quaternion.
     * @return The dot product of this and the other quaternion.
     */
    public float dot(Quaternion other) {
        return dot(other.x, other.y, other.z, other.w);
    }

    /**
     * Spherical linear interpolation between this quaternion and the other quaternion, based on the alpha value in the range
     * [0,1]. Taken from Bones framework for JPCT, see http://www.aptalkarga.com/bones/
     *
     * @param end   the end quaternion
     * @param alpha alpha in the range [0,1]
     * @return This quaternion for chaining
     */
    public Quaternion slerp(Quaternion end, float alpha) {
        final float d = this.x * end.x + this.y * end.y + this.z * end.z + this.w * end.w;
        float absDot = d < 0.f ? -d : d;

        // Set the first and second scale for the interpolation
        float scale0 = 1f - alpha;
        float scale1 = alpha;

        // Check if the angle between the 2 quaternions was big enough to
        // warrant such calculations
        if ((1 - absDot) > 0.1) {// Get the angle between the 2 quaternions,
            // and then store the sin() of that angle
            final float angle = (float) Math.acos(absDot);
            final float invSinTheta = 1f / (float) Math.sin(angle);

            // Calculate the scale for q1 and q2, according to the angle and
            // it's sine value
            scale0 = ((float) Math.sin((1f - alpha) * angle) * invSinTheta);
            scale1 = ((float) Math.sin((alpha * angle)) * invSinTheta);
        }

        if (d < 0.f) scale1 = -scale1;

        // Calculate the x, y, z and w values for the quaternion by using a
        // special form of linear interpolation for quaternions.
        x = (scale0 * x) + (scale1 * end.x);
        y = (scale0 * y) + (scale1 * end.y);
        z = (scale0 * z) + (scale1 * end.z);
        w = (scale0 * w) + (scale1 * end.w);

        // Return the interpolated quaternion
        return this;
    }

    /**
     * Transforms the given vector using this quaternion
     *
     * @param v Vector to transform
     */
    Vector3 transform(Vector3 v) {
        final float newX = v.x * -this.w + v.y * -this.z - v.z * -this.y;
        final float newY = v.y * -this.w + v.z * -this.x - v.x * -this.z;
        final float newZ = v.z * -this.w + v.x * -this.y - v.y * -this.x;
        final float newW = v.x * -this.x - v.y * -this.y - v.z * -this.z;

        v.x = w * newX + x * newW + y * newZ - z * newY;
        v.y = w * newY + y * newW + z * newX - x * newZ;
        v.z = w * newZ + z * newW + x * newY - y * newX;
        return v;
    }

    Quaternion setFromMatrix(boolean normalizeAxes, Matrix4 matrix) {
        return setFromAxes(normalizeAxes,
                matrix.val[Matrix4.M00], matrix.val[Matrix4.M01], matrix.val[Matrix4.M02],
                matrix.val[Matrix4.M10], matrix.val[Matrix4.M11], matrix.val[Matrix4.M12],
                matrix.val[Matrix4.M20], matrix.val[Matrix4.M21], matrix.val[Matrix4.M22]);
    }

    Quaternion setFromAxes(boolean normalizeAxes, float xx, float xy, float xz, float yx, float yy, float yz, float zx, float zy, float zz) {
        if (normalizeAxes) {
            final float lx = 1f / Vector3.length(xx, xy, xz);
            final float ly = 1f / Vector3.length(yx, yy, yz);
            final float lz = 1f / Vector3.length(zx, zy, zz);
            xx *= lx;
            xy *= lx;
            xz *= lx;
            yx *= ly;
            yy *= ly;
            yz *= ly;
            zx *= lz;
            zy *= lz;
            zz *= lz;
        }
        // the trace is the sum of the diagonal elements; see
        // http://mathworld.wolfram.com/MatrixTrace.html
        final float t = xx + yy + zz;

        // we protect the division by s by ensuring that s>=1
        if (t >= 0) { // |w| >= .5
            float s = (float) Math.sqrt(t + 1); // |s|>=1 ...
            w = 0.5f * s;
            s = 0.5f / s; // so this division isn't bad
            x = (zy - yz) * s;
            y = (xz - zx) * s;
            z = (yx - xy) * s;
        } else if ((xx > yy) && (xx > zz)) {
            float s = (float) Math.sqrt(1.0 + xx - yy - zz); // |s|>=1
            x = s * 0.5f; // |x| >= .5
            s = 0.5f / s;
            y = (yx + xy) * s;
            z = (xz + zx) * s;
            w = (zy - yz) * s;
        } else if (yy > zz) {
            float s = (float) Math.sqrt(1.0 + yy - xx - zz); // |s|>=1
            y = s * 0.5f; // |y| >= .5
            s = 0.5f / s;
            x = (yx + xy) * s;
            z = (zy + yz) * s;
            w = (xz - zx) * s;
        } else {
            float s = (float) Math.sqrt(1.0 + zz - xx - yy); // |s|>=1
            z = s * 0.5f; // |z| >= .5
            s = 0.5f / s;
            x = (xz + zx) * s;
            y = (zy + yz) * s;
            w = (yx - xy) * s;
        }

        return this;
    }

    Quaternion setFromCross(Vector3 v1, Vector3 v2) {
        return setFromCross(v1.x, v1.y, v1.z, v2.x, v2.y, v2.z);
    }

    Quaternion setFromCross(float x1, float y1, float z1, float x2, float y2, float z2) {
        final float dot = Mathf.clamp(Vector3.dot(x1, y1, z1, x2, y2, z2), -1f, 1f);
        final float angle = (float) Math.acos(dot);
        return setFromAxis(y1 * z2 - z1 * y2, z1 * x2 - x1 * z2, x1 * y2 - y1 * x2, angle);
    }

    Quaternion exp(float alpha) {

        // Calculate |q|^alpha
        float norm = length();
        float normExp = (float) Math.pow(norm, alpha);

        // Calculate theta
        float theta = (float) Math.acos(w / norm);

        // Calculate coefficient of basis elements
        float coeff = 0;
        if (Math.abs(theta) < 0.001) // If theta is small enough, use the limit of sin(alpha*theta) / sin(theta) instead of actual value
            coeff = normExp * alpha / norm;
        else
            coeff = (float) (normExp * Math.sin(alpha * theta) / (norm * Math.sin(theta)));

        // Write results
        w = (float) (normExp * Math.cos(alpha * theta));
        x *= coeff;
        y *= coeff;
        z *= coeff;

        // Fix any possible discrepancies
        normalize();

        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.hashCode(w);
        result = prime * result + Float.hashCode(x);
        result = prime * result + Float.hashCode(y);
        result = prime * result + Float.hashCode(z);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        Quaternion other = (Quaternion) obj;
        return x == other.x && y == other.y && z == other.z && w == other.w;
    }

    @Override
    public String toString() {
        return "[" + x + "|" + y + "|" + z + "|" + w + "]";
    }

    /**
     * Get the dot product between the two quaternions (commutative).
     *
     * @param x1 the x component of the first quaternion
     * @param y1 the y component of the first quaternion
     * @param z1 the z component of the first quaternion
     * @param w1 the w component of the first quaternion
     * @param x2 the x component of the second quaternion
     * @param y2 the y component of the second quaternion
     * @param z2 the z component of the second quaternion
     * @param w2 the w component of the second quaternion
     * @return The dot product between the first and second quaternion.
     */
    public static float dot(float x1, float y1, float z1, float w1, float x2, float y2, float z2, float w2) {
        return x1 * x2 + y1 * y2 + z1 * z2 + w1 * w2;
    }

    /**
     * Returns the euclidean length of a quaternion
     *
     * @param x the x component of the quaternion
     * @param y the y component of the quaternion
     * @param z the z component of the quaternion
     * @param w the w component of the quaternion
     * @return The euclidean length of this quaternion
     */
    public static float length(float x, float y, float z, float w) {
        return Mathf.sqrt(x * x + y * y + z * z + w * w);
    }

    /**
     * Returns the squared euclidean length of a quaternion
     *
     * @param x the x component of the quaternion
     * @param y the y component of the quaternion
     * @param z the z component of the quaternion
     * @param w the w component of the quaternion
     * @return The squared euclidean length of this quaternion
     */
    public static float lengthSqr(float x, float y, float z, float w) {
        return x * x + y * y + z * z + w * w;
    }

    // todo ... hum should test
    public Quaternion LookAt(Vector3 position, Vector3 lookVector) {
        // assert(lookVector != position);

        Vector3 direction = new Vector3(lookVector).sub(position).normalize();
        float dot = Vector3.dot(0, 0, 1, direction.x, direction.y, direction.z);
        if (Math.abs(dot + 1.0f) < 0.000001f) {
            return setFromAxis(0, 1, 0, 180);
        } else if (Math.abs(dot - 1.0f) < 0.000001f) {
            return identity();
        }

        float angle = -Mathf.toDegrees(Mathf.acos(dot));
        Vector3 cross = new Vector3(0, 0, 1).cross(direction).normalize();

        return setFromAxis(cross, angle).normalize();
    }
}