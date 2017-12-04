package flat.math;

public class Matrix3 {

    public static final int M00 = 0;
    public static final int M10 = 1;
    public static final int M20 = 2;

    public static final int M01 = 3;
    public static final int M11 = 4;
    public static final int M21 = 5;

    public static final int M02 = 6;
    public static final int M12 = 7;
    public static final int M22 = 8;

    public final float[] val = new float[9];

    public Matrix3() {
        identity();
    }

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

    public boolean isIdentity() {
        return (val[M00] == 1 && val[M10] == 0 && val[M20] == 0 &&
                val[M01] == 0 && val[M11] == 1 && val[M21] == 0 &&
                val[M02] == 0 && val[M12] == 0 && val[M22] == 1);
    }

    public float determinant () {
        return val[M00] * val[M11] * val[M22] + val[M01] * val[M12] * val[M20] + val[M02] * val[M10] * val[M21] - val[M00]
                * val[M12] * val[M21] - val[M01] * val[M10] * val[M22] - val[M02] * val[M11] * val[M20];
    }

    public Matrix3 invert () {
        float det = determinant();
        if (det == 0) {
            throw new RuntimeException("Can't invert a singular matrix");
        }

        float inv_det = 1.0f / det;

        float _M00 = val[M11] * val[M22] - val[M21] * val[M12];
        float _M10 = val[M20] * val[M12] - val[M10] * val[M22];
        float _M20 = val[M10] * val[M21] - val[M20] * val[M11];
        float _M01 = val[M21] * val[M02] - val[M01] * val[M22];
        float _M11 = val[M00] * val[M22] - val[M20] * val[M02];
        float _M21 = val[M20] * val[M01] - val[M00] * val[M21];
        float _M02 = val[M01] * val[M12] - val[M11] * val[M02];
        float _M12 = val[M10] * val[M02] - val[M00] * val[M12];
        float _M22 = val[M00] * val[M11] - val[M10] * val[M01];

        val[M00] = inv_det * _M00;
        val[M10] = inv_det * _M10;
        val[M20] = inv_det * _M20;
        val[M01] = inv_det * _M01;
        val[M11] = inv_det * _M11;
        val[M21] = inv_det * _M21;
        val[M02] = inv_det * _M02;
        val[M12] = inv_det * _M12;
        val[M22] = inv_det * _M22;

        return this;
    }

    public Matrix3 set(Matrix3 other) {
        System.arraycopy(other.val, 0, val, 0, 9);
        return this;
    }

    public Matrix3 set(float[] data9) {
        System.arraycopy(data9, 0, val, 0, 9);
        return this;
    }

    public Matrix3 set(float v00, float v10, float v20,
                       float v01, float v11, float v21,
                       float v02, float v12, float v22) {
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

    public Matrix3 setAll(float x, float y, float scaleX, float scaleY, float rotation) {
        val[M02] = x;
        val[M12] = y;
        val[M20] = 0;
        val[M21] = 0;
        val[M22] = 1;

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

    public Matrix3 translate(float x, float y) {
        float v00 = val[M00] * 1 + val[M01] * 0 + val[M02] * 0;
        float v01 = val[M00] * 0 + val[M01] * 1 + val[M02] * 0;
        float v02 = val[M00] * x + val[M01] * y + val[M02] * 1;

        float v10 = val[M10] * 1 + val[M11] * 0 + val[M12] * 0;
        float v11 = val[M10] * 0 + val[M11] * 1 + val[M12] * 0;
        float v12 = val[M10] * x + val[M11] * y + val[M12] * 1;

        float v20 = val[M20] * 1 + val[M21] * 0 + val[M22] * 0;
        float v21 = val[M20] * 0 + val[M21] * 1 + val[M22] * 0;
        float v22 = val[M20] * x + val[M21] * y + val[M22] * 1;

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

    public Matrix3 toTranslation(float x, float y) {
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

    public Matrix3 scale(float x, float y) {
        float v00 = val[M00] * x + val[M01] * 0 + val[M02] * 0;
        float v01 = val[M00] * 0 + val[M01] * y + val[M02] * 0;
        float v02 = val[M00] * 0 + val[M01] * 0 + val[M02] * 1;

        float v10 = val[M10] * x + val[M11] * 0 + val[M12] * 0;
        float v11 = val[M10] * 0 + val[M11] * y + val[M12] * 0;
        float v12 = val[M10] * 0 + val[M11] * 0 + val[M12] * 1;

        float v20 = val[M20] * x + val[M21] * 0 + val[M22] * 0;
        float v21 = val[M20] * 0 + val[M21] * y + val[M22] * 0;
        float v22 = val[M20] * 0 + val[M21] * 0 + val[M22] * 1;

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

    public Matrix3 toScaling(float x, float y) {
        val[M00] = x;
        val[M10] = 0;
        val[M20] = 0;
        val[M01] = 0;
        val[M11] = y;
        val[M21] = 0;
        val[M02] = 0;
        val[M12] = 0;
        val[M22] = 1;
        return this;
    }

    public Matrix3 rotate(float angle) {
        float c = (float)Math.cos(angle);
        float s = (float)Math.sin(angle);

        float v00 = val[M00] * c + val[M01] * s + val[M02] * 0;
        float v01 = val[M00] *-s + val[M01] * c + val[M02] * 0;
        float v02 = val[M00] * 0 + val[M01] * 0 + val[M02] * 1;

        float v10 = val[M10] * c + val[M11] * s + val[M12] * 0;
        float v11 = val[M10] *-s + val[M11] * c + val[M12] * 0;
        float v12 = val[M10] * 0 + val[M11] * 0 + val[M12] * 1;

        float v20 = val[M20] * c + val[M21] * s + val[M22] * 0;
        float v21 = val[M20] *-s + val[M21] * c + val[M22] * 0;
        float v22 = val[M20] * 0 + val[M21] * 0 + val[M22] * 1;

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

    public Matrix3 toRotation(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

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

    public Matrix3 multiply(Matrix3 other) {
        float[] oth = other.val;

        float v00 = val[M00] * oth[M00] + val[M01] * oth[M10] + val[M02] * oth[M20];
        float v01 = val[M00] * oth[M01] + val[M01] * oth[M11] + val[M02] * oth[M21];
        float v02 = val[M00] * oth[M02] + val[M01] * oth[M12] + val[M02] * oth[M22];

        float v10 = val[M10] * oth[M00] + val[M11] * oth[M10] + val[M12] * oth[M20];
        float v11 = val[M10] * oth[M01] + val[M11] * oth[M11] + val[M12] * oth[M21];
        float v12 = val[M10] * oth[M02] + val[M11] * oth[M12] + val[M12] * oth[M22];

        float v20 = val[M20] * oth[M00] + val[M21] * oth[M10] + val[M22] * oth[M20];
        float v21 = val[M20] * oth[M01] + val[M21] * oth[M11] + val[M22] * oth[M21];
        float v22 = val[M20] * oth[M02] + val[M21] * oth[M12] + val[M22] * oth[M22];

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

    public Matrix3 preMultiply(Matrix3 other) {
        float[] oth = other.val;

        float v00 = oth[M00] * val[M00] + oth[M01] * val[M10] + oth[M02] * val[M20];
        float v01 = oth[M00] * val[M01] + oth[M01] * val[M11] + oth[M02] * val[M21];
        float v02 = oth[M00] * val[M02] + oth[M01] * val[M12] + oth[M02] * val[M22];

        float v10 = oth[M10] * val[M00] + oth[M11] * val[M10] + oth[M12] * val[M20];
        float v11 = oth[M10] * val[M01] + oth[M11] * val[M11] + oth[M12] * val[M21];
        float v12 = oth[M10] * val[M02] + oth[M11] * val[M12] + oth[M12] * val[M22];

        float v20 = oth[M20] * val[M00] + oth[M21] * val[M10] + oth[M22] * val[M20];
        float v21 = oth[M20] * val[M01] + oth[M21] * val[M11] + oth[M22] * val[M21];
        float v22 = oth[M20] * val[M02] + oth[M21] * val[M12] + oth[M22] * val[M22];

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

    public float getTranslationX() {
        return val[M20];
    }

    public float getTranslationY() {
        return val[M21];
    }

    public float getScalingX() {
        return (float) Math.sqrt(val[M00] * val[M00] + val[M10] * val[M10]);
    }

    public float getScalingY() {
        return (float) Math.sqrt(val[M01] * val[M01] + val[M11] * val[M11]);
    }

    public float getRotation() {
        return (float) Math.atan2(val[M01], val[M00]);
    }

    public float getPointX(float x, float y) {
        return val[M00] * x + val[M01] * y + val[M02];
    }

    public float getPointY(float x, float y) {
        return val[M10] * x + val[M11] * y + val[M12];
    }
}