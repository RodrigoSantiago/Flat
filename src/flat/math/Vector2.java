package flat.math;

public class Vector2 {

    public float x;
    public float y;

    public Vector2 () {
    }

    public Vector2 (float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 (Vector2 vector) {
        set(vector);
    }

    @Override
    public Vector2 clone () {
        return new Vector2(this);
    }

    public Vector2 set (float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2 set (float value) {
        return this.set(value, value);
    }

    public Vector2 set (Vector2 vector) {
        return this.set(vector.x, vector.y);
    }

    public Vector2 add (float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public Vector2 add (float value) {
        return this.add(value, value);
    }

    public Vector2 add (Vector2 vector) {
        return this.add(vector.x, vector.y);
    }

    public Vector2 sub (float x, float y) {
        this.x -= x;
        this.y -= y;
        return this;
    }
    public Vector2 sub (float value) {
        return this.sub(value, value);
    }

    public Vector2 sub (Vector2 vector) {
        return this.sub(vector.x, vector.y);
    }

    public Vector2 mul(float x, float y) {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public Vector2 mul(float value) {
        return this.mul(value, value);
    }

    public Vector2 mul(Vector2 vector) {
        return this.mul(vector.x, vector.y);
    }

    public Vector2 mul (Matrix3 mat) {
        // todo implementar
        return this;
    }

    public Vector2 lerp (Vector2 target, float alpha) {
        final float invAlpha = 1.0f - alpha;
        this.x = (x * invAlpha) + (target.x * alpha);
        this.y = (y * invAlpha) + (target.y * alpha);
        return this;
    }

    public float length() {
        return (float)Math.sqrt(x * x + y * y);
    }

    public Vector2 setLength (float len) {
        return setLengthSqr(len * len);
    }

    public float lengthSqr() {
        return x * x + y * y;
    }

    public Vector2 setLengthSqr(float lenSqr) {
        float oldLenSqr = lengthSqr();
        return (oldLenSqr == 0 || oldLenSqr == lenSqr) ? this : mul((float)Math.sqrt(lenSqr / oldLenSqr));
    }

    public Vector2 normalize() {
        float len = length();
        if (len != 0) {
            x /= len;
            y /= len;
        }
        return this;
    }

    public float dot (float x, float y) {
        return x * this.x + y * this.y;
    }

    public float dot (Vector2 vector) {
        return this.dot(vector.x, vector.y);
    }

    public float cross(float x, float y) {
        return this.x * y - this.y * x;
    }

    public float cross(Vector2 vector) {
        return this.cross(vector.x, vector.y);
    }

    public float distance(float x, float y) {
        final float x_d = x - this.x;
        final float y_d = y - this.y;
        return (float)Math.sqrt(x_d * x_d + y_d * y_d);
    }

    public float distance(Vector2 vector) {
        return this.distance(vector.x, vector.y);
    }

    public float distanceSqr(float x, float y) {
        final float x_d = x - this.x;
        final float y_d = y - this.y;
        return x_d * x_d + y_d * y_d;
    }

    public float distanceSqr(Vector2 vector) {
        return this.distanceSqr(vector.x, vector.y);
    }

    public static float lengthSqr(float x, float y) {
        return x * x + y * y;
    }

    public static float length(float x, float y) {
        return (float)Math.sqrt(x * x + y * y);
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        final float x_d = x2 - x1;
        final float y_d = y2 - y1;
        return (float)Math.sqrt(x_d * x_d + y_d * y_d);
    }

    public static float distanceSqr(float x1, float y1, float x2, float y2) {
        final float x_d = x2 - x1;
        final float y_d = y2 - y1;
        return x_d * x_d + y_d * y_d;
    }

    public static float dot (float x1, float y1, float x2, float y2) {
        return x1 * x2 + y1 * y2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if  (obj instanceof Vector2) {
            Vector2 other = (Vector2) obj;
            return other.x == x && other.y == y;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}