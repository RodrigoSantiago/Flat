package flat.math;

public class Vector3 {

    public float x;
    public float y;
    public float z;

    public final static Vector3 X = new Vector3(1, 0, 0);
    public final static Vector3 Y = new Vector3(0, 1, 0);
    public final static Vector3 Z = new Vector3(0, 0, 1);
    public final static Vector3 Zero = new Vector3(0, 0, 0);

    private final static Matrix4 tmpMat = new Matrix4();

    public Vector3() {
    }

    public Vector3(float x, float y, float z) {
        this.set(x, y, z);
    }

    public Vector3(float[] values) {
        this.set(values[0], values[1], values[2]);
    }

    public Vector3(float value) {
        this.set(value);
    }

    public Vector3(Vector3 vector) {
        this.set(vector);
    }

    public Vector3(Vector2 vector, float z) {
        this.set(vector.x, vector.y, z);
    }

    public Vector3(Vector2 vector) {
        this.set(vector.x, vector.y, 0);
    }

    @Override
    public Vector3 clone() {
        return new Vector3(this);
    }

    public Vector3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3 set(float value) {
        return this.set(value, value, value);
    }

    public Vector3 set(Vector3 vector) {
        return this.set(vector.x, vector.y, vector.z);
    }

    public Vector3 add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vector3 add(float values) {
        return this.add(values, values, values);
    }

    public Vector3 add(Vector3 vector) {
        return this.add(vector.x, vector.y, vector.z);
    }

    public Vector3 sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vector3 sub(float value) {
        return this.sub(value, value, value);
    }

    public Vector3 sub(Vector3 vector) {
        return this.sub(vector.x, vector.y, vector.z);
    }

    public Vector3 mul(float x, float y, float z) {
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return this;
    }

    public Vector3 mul(float value) {
        return this.mul(value, value, value);
    }

    public Vector3 mul(Vector3 vector) {
        return this.mul(vector.x, vector.y, vector.z);
    }

    public Vector3 mul(Matrix4 mat) {
        return this.set(
                x * mat.val[Matrix4.M00] + y * mat.val[Matrix4.M01] + z * mat.val[Matrix4.M02] + mat.val[Matrix4.M03],
                x * mat.val[Matrix4.M10] + y * mat.val[Matrix4.M11] + z * mat.val[Matrix4.M12] + mat.val[Matrix4.M13],
                x * mat.val[Matrix4.M20] + y * mat.val[Matrix4.M21] + z * mat.val[Matrix4.M22] + mat.val[Matrix4.M23]);
    }

    public Vector3 mul(Matrix3 mat) {
        return set(
                x * mat.val[Matrix3.M00] + y * mat.val[Matrix3.M01] + z * mat.val[Matrix3.M02],
                x * mat.val[Matrix3.M10] + y * mat.val[Matrix3.M11] + z * mat.val[Matrix3.M12],
                x * mat.val[Matrix3.M20] + y * mat.val[Matrix3.M21] + z * mat.val[Matrix3.M22]);
    }

    public Vector3 mul(Quaternion quat) {
        return quat.transform(this);
    }

    public Vector3 prj(Matrix4 matrix) {
        final float tmp[] = matrix.val;
        final float l_w = 1f / (x * tmp[Matrix4.M30] + y * tmp[Matrix4.M31] + z * tmp[Matrix4.M32] + tmp[Matrix4.M33]);
        return this.set(
                (x * tmp[Matrix4.M00] + y * tmp[Matrix4.M01] + z * tmp[Matrix4.M02] + tmp[Matrix4.M03]) * l_w,
                (x * tmp[Matrix4.M10] + y * tmp[Matrix4.M11] + z * tmp[Matrix4.M12] + tmp[Matrix4.M13]) * l_w,
                (x * tmp[Matrix4.M20] + y * tmp[Matrix4.M21] + z * tmp[Matrix4.M22] + tmp[Matrix4.M23]) * l_w);
    }

    public Vector3 setLength(float len) {
        return setLengthSqr(len * len);
    }

    public Vector3 setLengthSqr(float len2) {
        float oldLen2 = lengthSqr();
        return (oldLen2 == 0 || oldLen2 == len2) ? this : mul((float) Math.sqrt(len2 / oldLen2));
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public float lengthSqr() {
        return x * x + y * y + z * z;
    }

    public float distance(float x, float y, float z) {
        final float a = x - this.x;
        final float b = y - this.y;
        final float c = z - this.z;
        return (float) Math.sqrt(a * a + b * b + c * c);
    }

    public float distance(Vector3 vector) {
        return this.distance(vector.x, vector.y, vector.z);
    }

    public float distanceSqr(float x, float y, float z) {
        final float a = x - this.x;
        final float b = y - this.y;
        final float c = z - this.z;
        return a * a + b * b + c * c;
    }

    public float distanceSqr(Vector3 vector) {
        return this.distanceSqr(vector.x, vector.y, vector.z);
    }

    public Vector3 normalize() {
        final float lenSqr = this.lengthSqr();
        if (lenSqr == 0f || lenSqr == 1f) return this;
        return this.mul(1f / (float) Math.sqrt(lenSqr));
    }

    public float dot(float x, float y, float z) {
        return this.x * x + this.y * y + this.z * z;
    }

    public float dot(Vector3 vector) {
        return this.dot(vector.x, vector.y, vector.z);
    }

    public Vector3 cross(float x, float y, float z) {
        return this.set(this.y * z - this.z * y, this.z * x - this.x * z, this.x * y - this.y * x);
    }

    public Vector3 cross(Vector3 vector) {
        return this.cross(vector.x, vector.y, vector.z);
    }

    public Vector3 lerp(final Vector3 target, float alpha) {
        x += alpha * (target.x - x);
        y += alpha * (target.y - y);
        z += alpha * (target.z - z);
        return this;
    }

    public Vector3 slerp(final Vector3 target, float alpha) {
        final float dot = dot(target);

        if (dot > 0.9995 || dot < -0.9995) return lerp(target, alpha);

        final float theta0 = (float) Math.acos(dot);
        final float theta = theta0 * alpha;

        final float st = (float) Math.sin(theta);
        final float tx = target.x - x * dot;
        final float ty = target.y - y * dot;
        final float tz = target.z - z * dot;
        final float l2 = tx * tx + ty * ty + tz * tz;
        final float dl = st * ((l2 < 0.0001f) ? 1f : 1f / (float) Math.sqrt(l2));

        return mul((float) Math.cos(theta)).add(tx * dl, ty * dl, tz * dl).normalize();
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }

    public static float length(final float x, final float y, final float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public static float lengthSqr(final float x, final float y, final float z) {
        return x * x + y * y + z * z;
    }

    public static float distance(final float x1, final float y1, final float z1, final float x2, final float y2, final float z2) {
        final float a = x2 - x1;
        final float b = y2 - y1;
        final float c = z2 - z1;
        return (float) Math.sqrt(a * a + b * b + c * c);
    }

    public static float distanceSqr(final float x1, final float y1, final float z1, final float x2, final float y2, final float z2) {
        final float a = x2 - x1;
        final float b = y2 - y1;
        final float c = z2 - z1;
        return a * a + b * b + c * c;
    }

    public static float dot(float x1, float y1, float z1, float x2, float y2, float z2) {
        return x1 * x2 + y1 * y2 + z1 * z2;
    }
}