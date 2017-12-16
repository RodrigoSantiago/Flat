package flat.math;

public final class Affine {

    public static final int M00 = 0, M10 = 1;
    public static final int M01 = 2, M11 = 3;
    public static final int M02 = 4, M12 = 5;

    public final float[] val = new float[6];

    public Affine() {
        identity();
    }

    public Affine(Affine other) {
        set(other);
    }

    public Affine identity() {
        val[M00] = 1;
        val[M01] = 0;
        val[M02] = 0;
        val[M10] = 0;
        val[M11] = 1;
        val[M12] = 0;
        return this;
    }

    public boolean isIdentity() {
        return (val[M00] == 1 && val[M10] == 0 &&
                val[M01] == 0 && val[M11] == 1 &&
                val[M02] == 0 && val[M12] == 0);
    }

    public float determinant() {
        return val[M00] * val[M11] - val[M01] * val[M10];
    }

    public Affine invert() {
        float det = determinant();
        if (det == 0) {
            throw  new RuntimeException("Can't invert a singular affine matrix");
        }

        float invDet = 1.0f / det;

        float tmp00 = val[M11];
        float tmp01 = -val[M01];
        float tmp02 = val[M01] * val[M12] - val[M11] * val[M02];
        float tmp10 = -val[M10];
        float tmp11 = val[M00];
        float tmp12 = val[M10] * val[M02] - val[M00] * val[M12];

        val[M00] = invDet * tmp00;
        val[M01] = invDet * tmp01;
        val[M02] = invDet * tmp02;
        val[M10] = invDet * tmp10;
        val[M11] = invDet * tmp11;
        val[M12] = invDet * tmp12;
        return this;
    }

    public Affine set(Affine other) {
        val[M00] = other.val[M00];
        val[M01] = other.val[M01];
        val[M02] = other.val[M02];
        val[M10] = other.val[M10];
        val[M11] = other.val[M11];
        val[M12] = other.val[M12];
        return this;
    }

    public Affine set(float[] data6) {
        val[M00] = data6[0];
        val[M01] = data6[1];
        val[M02] = data6[2];
        val[M10] = data6[3];
        val[M11] = data6[4];
        val[M12] = data6[5];
        return this;
    }

    public Affine set(float v00, float v10,
                      float v01, float v11,
                      float v02, float v12) {
        val[M00] = v00;
        val[M10] = v10;
        val[M01] = v01;
        val[M11] = v11;
        val[M02] = v02;
        val[M12] = v12;
        return this;
    }

    public Affine setAll(float x, float y, float scaleX, float scaleY, float rotation) {
        val[M02] = x;
        val[M12] = y;

        if (rotation == 0) {
            val[M00] = scaleX;
            val[M01] = 0;
            val[M10] = 0;
            val[M11] = scaleY;
        } else {
            float sin = (float) Math.sin(rotation);
            float cos = (float) Math.cos(rotation);

            val[M00] = cos * scaleX;
            val[M01] = -sin * scaleY;
            val[M10] = sin * scaleX;
            val[M11] = cos * scaleY;
        }
        return this;
    }

    public Affine translate(float x, float y) {
        val[M02] += x;
        val[M12] += y;
        return this;
    }

    public Affine toTranslation(float x, float y) {
        val[M00] = 1;
        val[M01] = 0;
        val[M02] = x;
        val[M10] = 0;
        val[M11] = 1;
        val[M12] = y;
        return this;
    }

    public Affine scale(float scaleX, float scaleY) {
        val[M00] *= scaleX;
        val[M01] *= scaleX;
        val[M02] *= scaleX;
        val[M10] *= scaleY;
        val[M11] *= scaleY;
        val[M12] *= scaleY;
        return this;
    }

    public Affine toScaling(float scaleX, float scaleY) {
        val[M00] = scaleX;
        val[M01] = 0;
        val[M02] = 0;
        val[M10] = 0;
        val[M11] = scaleY;
        val[M12] = 0;
        return this;
    }

    public Affine rotate(float angle) {
        if (angle == 0) return this;

        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        float tmp00 = cos * val[M00] - sin * val[M10];
        float tmp01 = cos * val[M01] - sin * val[M11];
        float tmp02 = cos * val[M02] - sin * val[M12];
        float tmp10 = sin * val[M00] + cos * val[M10];
        float tmp11 = sin * val[M01] + cos * val[M11];
        float tmp12 = sin * val[M02] + cos * val[M12];

        val[M00] = tmp00;
        val[M01] = tmp01;
        val[M02] = tmp02;
        val[M10] = tmp10;
        val[M11] = tmp11;
        val[M12] = tmp12;
        return this;
    }

    public Affine toRotation(float radians) {
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);

        val[M00] = cos;
        val[M01] = -sin;
        val[M02] = 0;
        val[M10] = sin;
        val[M11] = cos;
        val[M12] = 0;
        return this;
    }

    public Affine multiply(Affine other) {
        float tmp00 = val[M00] * other.val[M00] + val[M01] * other.val[M10];
        float tmp01 = val[M00] * other.val[M01] + val[M01] * other.val[M11];
        float tmp02 = val[M00] * other.val[M02] + val[M01] * other.val[M12] + val[M02];
        float tmp10 = val[M10] * other.val[M00] + val[M11] * other.val[M10];
        float tmp11 = val[M10] * other.val[M01] + val[M11] * other.val[M11];
        float tmp12 = val[M10] * other.val[M02] + val[M11] * other.val[M12] + val[M12];

        val[M00] = tmp00;
        val[M01] = tmp01;
        val[M02] = tmp02;
        val[M10] = tmp10;
        val[M11] = tmp11;
        val[M12] = tmp12;
        return this;
    }

    public Affine preMultiply(Affine other) {
        float tmp00 = other.val[M00] * val[M00] + other.val[M01] * val[M10];
        float tmp01 = other.val[M00] * val[M01] + other.val[M01] * val[M11];
        float tmp02 = other.val[M00] * val[M02] + other.val[M01] * val[M12] + other.val[M02];
        float tmp10 = other.val[M10] * val[M00] + other.val[M11] * val[M10];
        float tmp11 = other.val[M10] * val[M01] + other.val[M11] * val[M11];
        float tmp12 = other.val[M10] * val[M02] + other.val[M11] * val[M12] + other.val[M12];

        val[M00] = tmp00;
        val[M01] = tmp01;
        val[M02] = tmp02;
        val[M10] = tmp10;
        val[M11] = tmp11;
        val[M12] = tmp12;
        return this;
    }

    public float getTranslationX() {
        return val[M02];
    }

    public float getTranslationY() {
        return val[M12];
    }

    public float getScaleX() {
        return (float) Math.sqrt(val[M00] * val[M00] + val[M01] * val[M01]);
    }

    public float getScaleY() {
        return (float) Math.sqrt(val[M10] * val[M10] + val[M11] * val[M11]);
    }

    public float getRotation() {
        return (float) Math.atan2(val[M10], val[M00]);
    }

    public float getPointX(float x, float y) {
        return val[M00] * x + val[M01] * y + val[M02];
    }

    public float getPointY(float x, float y) {
        return val[M10] * x + val[M11] * y + val[M12];
    }

}