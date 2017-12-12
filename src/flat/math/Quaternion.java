package flat.math;

public class Quaternion {

    private static Quaternion tmp1 = new Quaternion(0, 0, 0, 0);
    private static Quaternion tmp2 = new Quaternion(0, 0, 0, 0);

    public float x;
    public float y;
    public float z;
    public float w;

    public Quaternion () {
        idtentity();
    }

    public Quaternion (float x, float y, float z, float w) {
        this.set(x, y, z, w);
    }

    public Quaternion (Quaternion quaternion) {
        this.set(quaternion);
    }

    public Quaternion (Vector3 axis, float angle) {
        this.set(axis, angle);
    }

    @Override
    public Quaternion clone () {
        return new Quaternion(this);
    }

    public Quaternion set (float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Quaternion set (Quaternion quaternion) {
        return this.set(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
    }

    public Quaternion set (Vector3 axis, float angle) {
        return setFromAxis(axis.x, axis.y, axis.z, angle);
    }

    public Quaternion setEulerAngles (float yaw, float pitch, float roll) {
        yaw *= Mathf.degRad;
        pitch *= Mathf.degRad;
        roll *= Mathf.degRad;

        final float hr = roll * 0.5f;
        final float shr = (float) Math.sin(hr);
        final float chr = (float) Math.cos(hr);
        final float hp = pitch * 0.5f;
        final float shp = (float) Math.sin(hp);
        final float chp = (float) Math.cos(hp);
        final float hy = yaw * 0.5f;
        final float shy = (float) Math.sin(hy);
        final float chy = (float) Math.cos(hy);
        final float chy_shp = chy * shp;
        final float shy_chp = shy * chp;
        final float chy_chp = chy * chp;
        final float shy_shp = shy * shp;

        x = (chy_shp * chr) + (shy_chp * shr); // cos(yaw/2) * sin(pitch/2) * cos(roll/2) + sin(yaw/2) * cos(pitch/2) * sin(roll/2)
        y = (shy_chp * chr) - (chy_shp * shr); // sin(yaw/2) * cos(pitch/2) * cos(roll/2) - cos(yaw/2) * sin(pitch/2) * sin(roll/2)
        z = (chy_chp * shr) - (shy_shp * chr); // cos(yaw/2) * cos(pitch/2) * sin(roll/2) - sin(yaw/2) * sin(pitch/2) * cos(roll/2)
        w = (chy_chp * chr) + (shy_shp * shr); // cos(yaw/2) * cos(pitch/2) * cos(roll/2) + sin(yaw/2) * sin(pitch/2) * sin(roll/2)
        return this;
    }

    public int getGimbalPole () {
        final float t = y * x + z * w;
        return t > 0.499f ? 1 : (t < -0.499f ? -1 : 0);
    }

    public float getRoll () {
        final int pole = getGimbalPole();
        return (pole == 0 ?
                Mathf.atan2(2f * (w * z + y * x), 1f - 2f * (x * x + z * z)) :
                (float)pole * 2f * Mathf.atan2(y, w)) * Mathf.radDeg;
    }

    public float getPitch () {
        final int pole = getGimbalPole();
        return (pole == 0 ?
                (float) Math.asin(Mathf.clamp(2f * (w * x - z * y), -1f, 1f)) :
                (float) pole * Mathf.PI * 0.5f) * Mathf.radDeg;
    }

    public float getYaw () {
        return (getGimbalPole() == 0 ? Mathf.atan2(2f * (y * w + x * z), 1f - 2f * (y * y + x * x)) : 0f) * Mathf.radDeg;
    }

    public Quaternion add (Quaternion quaternion) {
        this.x += quaternion.x;
        this.y += quaternion.y;
        this.z += quaternion.z;
        this.w += quaternion.w;
        return this;
    }

    public Quaternion add (float x, float y, float z, float w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    public Quaternion mul (float x, float y, float z, float w) {
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

    public Quaternion mul (final Quaternion other) {
        final float newX = this.w * other.x + this.x * other.w + this.y * other.z - this.z * other.y;
        final float newY = this.w * other.y + this.y * other.w + this.z * other.x - this.x * other.z;
        final float newZ = this.w * other.z + this.z * other.w + this.x * other.y - this.y * other.x;
        final float newW = this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    public Quaternion mulLeft (Quaternion other) {
        final float newX = other.w * this.x + other.x * this.w + other.y * this.z - other.z * this.y;
        final float newY = other.w * this.y + other.y * this.w + other.z * this.x - other.x * this.z;
        final float newZ = other.w * this.z + other.z * this.w + other.x * this.y - other.y * this.x;
        final float newW = other.w * this.w - other.x * this.x - other.y * this.y - other.z * this.z;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    public Quaternion mulLeft (float x, float y, float z, float w) {
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

    public Quaternion scale(float value) {
        this.x *= value;
        this.y *= value;
        this.z *= value;
        this.w *= value;
        return this;
    }

    public Quaternion idtentity() {
        return this.set(0, 0, 0, 1);
    }

    public boolean isIdentity () {
        return Mathf.isZero(x) && Mathf.isZero(y) && Mathf.isZero(z) && Mathf.isEqual(w, 1f);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z + w * w);
    }

    public float lengthSqr() {
        return x * x + y * y + z * z + w * w;
    }

    public Quaternion normalize() {
        float len = lengthSqr();
        if (len != 0.f && !Mathf.isEqual(len, 1f)) {
            len = (float)Math.sqrt(len);
            w /= len;
            x /= len;
            y /= len;
            z /= len;
        }
        return this;
    }

    public Quaternion conjugate () {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }

    public Vector3 transform (Vector3 vector) {
        tmp2.set(this);
        tmp2.conjugate();
        tmp2.mulLeft(tmp1.set(vector.x, vector.y, vector.z, 0)).mulLeft(this);

        vector.x = tmp2.x;
        vector.y = tmp2.y;
        vector.z = tmp2.z;
        return vector;
    }

    public void toMatrix (float[] mat) {
        final float xx = x * x;
        final float xy = x * y;
        final float xz = x * z;
        final float xw = x * w;
        final float yy = y * y;
        final float yz = y * z;
        final float yw = y * w;
        final float zz = z * z;
        final float zw = z * w;

        mat[Matrix4.M00] = 1 - 2 * (yy + zz);
        mat[Matrix4.M01] = 2 * (xy - zw);
        mat[Matrix4.M02] = 2 * (xz + yw);
        mat[Matrix4.M03] = 0;
        mat[Matrix4.M10] = 2 * (xy + zw);
        mat[Matrix4.M11] = 1 - 2 * (xx + zz);
        mat[Matrix4.M12] = 2 * (yz - xw);
        mat[Matrix4.M13] = 0;
        mat[Matrix4.M20] = 2 * (xz - yw);
        mat[Matrix4.M21] = 2 * (yz + xw);
        mat[Matrix4.M22] = 1 - 2 * (xx + yy);
        mat[Matrix4.M23] = 0;
        mat[Matrix4.M30] = 0;
        mat[Matrix4.M31] = 0;
        mat[Matrix4.M32] = 0;
        mat[Matrix4.M33] = 1;
    }

    public Quaternion setFromAxis (float x, float y, float z, float angle) {
        angle *= Mathf.degRad;

        float d = Vector3.length(x, y, z);
        if (d == 0f) return idtentity();
        d = 1f / d;
        float l_ang = angle < 0 ? Mathf.PI2 - (-angle % Mathf.PI2) : angle % Mathf.PI2;
        float l_sin = (float)Math.sin(l_ang / 2);
        float l_cos = (float)Math.cos(l_ang / 2);
        return this.set(d * x * l_sin, d * y * l_sin, d * z * l_sin, l_cos).normalize();
    }

    public Quaternion setFromAxis (Vector3 axis, final float degrees) {
        return setFromAxis(axis.x, axis.y, axis.z, degrees);
    }

    public Quaternion setFromMatrix (boolean normalizeAxes, Matrix4 matrix) {
        return setFromAxes(normalizeAxes,
                matrix.val[Matrix4.M00], matrix.val[Matrix4.M01], matrix.val[Matrix4.M02],
                matrix.val[Matrix4.M10], matrix.val[Matrix4.M11], matrix.val[Matrix4.M12],
                matrix.val[Matrix4.M20], matrix.val[Matrix4.M21], matrix.val[Matrix4.M22]);
    }

    public Quaternion setFromMatrix (Matrix4 matrix) {
        return setFromMatrix(false, matrix);
    }

    public Quaternion setFromMatrix (boolean normalizeAxes, Matrix3 matrix) {
        return setFromAxes(normalizeAxes,
                matrix.val[Matrix3.M00], matrix.val[Matrix3.M01], matrix.val[Matrix3.M02],
                matrix.val[Matrix3.M10], matrix.val[Matrix3.M11], matrix.val[Matrix3.M12],
                matrix.val[Matrix3.M20], matrix.val[Matrix3.M21], matrix.val[Matrix3.M22]);
    }

    public Quaternion setFromMatrix (Matrix3 matrix) {
        return setFromMatrix(false, matrix);
    }

    public Quaternion setFromAxes (float xx, float xy, float xz, float yx, float yy, float yz, float zx, float zy, float zz) {
        return setFromAxes(false, xx, xy, xz, yx, yy, yz, zx, zy, zz);
    }

    public Quaternion setFromAxes (boolean normalizeAxes, float xx, float xy, float xz, float yx, float yy, float yz, float zx,
                                   float zy, float zz) {
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
            float s = (float)Math.sqrt(t + 1); // |s|>=1 ...
            w = 0.5f * s;
            s = 0.5f / s; // so this division isn't bad
            x = (zy - yz) * s;
            y = (xz - zx) * s;
            z = (yx - xy) * s;
        } else if ((xx > yy) && (xx > zz)) {
            float s = (float)Math.sqrt(1.0 + xx - yy - zz); // |s|>=1
            x = s * 0.5f; // |x| >= .5
            s = 0.5f / s;
            y = (yx + xy) * s;
            z = (xz + zx) * s;
            w = (zy - yz) * s;
        } else if (yy > zz) {
            float s = (float)Math.sqrt(1.0 + yy - xx - zz); // |s|>=1
            y = s * 0.5f; // |y| >= .5
            s = 0.5f / s;
            x = (yx + xy) * s;
            z = (zy + yz) * s;
            w = (xz - zx) * s;
        } else {
            float s = (float)Math.sqrt(1.0 + zz - xx - yy); // |s|>=1
            z = s * 0.5f; // |z| >= .5
            s = 0.5f / s;
            x = (xz + zx) * s;
            y = (zy + yz) * s;
            w = (yx - xy) * s;
        }

        return this;
    }

    public Quaternion setFromCross (Vector3 v1, Vector3 v2) {
        return setFromCross(v1.x, v1.y,v1.z, v2.x,v2.y,v2.z);
    }

    public Quaternion setFromCross (float x1, float y1, float z1, float x2, float y2, float z2) {
        final float dot = Mathf.clamp(Vector3.dot(x1, y1, z1, x2, y2, z2), -1f, 1f);
        final float angle = (float)Math.acos(dot);
        return setFromAxis(y1 * z2 - z1 * y2, z1 * x2 - x1 * z2, x1 * y2 - y1 * x2, angle * Mathf.radDeg);
    }

    public Quaternion slerp (Quaternion end, float alpha) {
        final float d = this.x * end.x + this.y * end.y + this.z * end.z + this.w * end.w;
        float absDot = d < 0.f ? -d : d;

        float scale0 = 1f - alpha;
        float scale1 = alpha;

        if ((1 - absDot) > 0.1) {
            final float angle = (float)Math.acos(absDot);
            final float invSinTheta = 1f / (float)Math.sin(angle);

            scale0 = ((float)Math.sin((1f - alpha) * angle) * invSinTheta);
            scale1 = ((float)Math.sin((alpha * angle)) * invSinTheta);
        }

        if (d < 0.f) scale1 = -scale1;

        x = (scale0 * x) + (scale1 * end.x);
        y = (scale0 * y) + (scale1 * end.y);
        z = (scale0 * z) + (scale1 * end.z);
        w = (scale0 * w) + (scale1 * end.w);
        return this;
    }

    public Quaternion slerp (Quaternion[] q) {
        final float w = 1.0f / q.length;
        set(q[0]).exp(w);
        for (int i = 1; i < q.length; i++)
            mul(tmp1.set(q[i]).exp(w));
        normalize();
        return this;
    }

    public Quaternion slerp (Quaternion[] q, float[] w) {
        set(q[0]).exp(w[0]);
        for (int i = 1; i < q.length; i++)
            mul(tmp1.set(q[i]).exp(w[i]));
        normalize();
        return this;
    }

    public Quaternion exp (float alpha) {
        float norm = length();
        float normExp = (float)Math.pow(norm, alpha);

        float theta = (float)Math.acos(w / norm);

        float coeff = 0;
        if (Math.abs(theta) < 0.001)
            coeff = normExp * alpha / norm;
        else
            coeff = (float)(normExp * Math.sin(alpha * theta) / (norm * Math.sin(theta)));

        w = (float)(normExp * Math.cos(alpha * theta));
        x *= coeff;
        y *= coeff;
        z *= coeff;

        normalize();

        return this;
    }

    public float dot (final float x, final float y, final float z, final float w) {
        return this.x * x + this.y * y + this.z * z + this.w * w;
    }

    public float dot (Quaternion other) {
        return this.dot(other.x, other.y, other.z, other.w);
    }

    public float getAxisAngle (Vector3 axis) {
        if (this.w > 1) this.normalize();
        float angle = (float)(2.0 * Math.acos(this.w));
        double s = Math.sqrt(1 - this.w * this.w);
        if (s < Mathf.epsilon) {
            axis.x = this.x;
            axis.y = this.y;
            axis.z = this.z;
        } else {
            axis.x = (float)(this.x / s);
            axis.y = (float)(this.y / s);
            axis.z = (float)(this.z / s);
        }

        return angle * Mathf.radDeg;
    }

    public float getAngle () {
        return (float)(2.0 * Math.acos((this.w > 1) ? (this.w / length()) : this.w) * Mathf.radDeg);
    }

    public void getSwingTwist (float axisX, float axisY, float axisZ, Quaternion swing, Quaternion twist) {
        final float d = Vector3.dot(this.x, this.y, this.z, axisX, axisY, axisZ);
        twist.set(axisX * d, axisY * d, axisZ * d, this.w).normalize();
        if (d < 0) twist.scale(-1f);
        swing.set(twist).conjugate().mulLeft(this);
    }

    public void getSwingTwist (Vector3 axis, Quaternion swing, Quaternion twist) {
        getSwingTwist(axis.x, axis.y, axis.z, swing, twist);
    }

    public float getAngleAround (final float axisX, final float axisY, final float axisZ) {
        final float d = Vector3.dot(this.x, this.y, this.z, axisX, axisY, axisZ);
        final float l2 = Quaternion.lengthSqr(axisX * d, axisY * d, axisZ * d, this.w);
        return Mathf.isZero(l2) ? 0f : (float)(2.0 * Math.acos(Mathf.clamp(
                (float)((d < 0 ? -this.w : this.w) / Math.sqrt(l2)), -1f, 1f))) * Mathf.radDeg;
    }

    public float getAngleAround (final Vector3 axis) {
        return getAngleAround(axis.x, axis.y, axis.z);
    }

    @Override
    public String toString () {
        return "[" + x + "|" + y + "|" + z + "|" + w + "]";
    }

    public static float length(final float x, final float y, final float z, final float w) {
        return (float) Math.sqrt(x * x + y * y + z * z + w * w);
    }

    public static float lengthSqr(final float x, final float y, final float z, final float w) {
        return x * x + y * y + z * z + w * w;
    }

    public static float dot (float x1, float y1, float z1, float w1, float x2, float y2, float z2, float w2) {
        return x1 * x2 + y1 * y2 + z1 * z2 + w1 * w2;
    }
}