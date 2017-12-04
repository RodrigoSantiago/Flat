package flat.math;

public final class Affine {

    public float m00, m01, m02, m10, m11, m12;

    public Affine() {
        identity();
    }

    public Affine(Affine other) {
        set(other);
    }

    public Affine identity() {
        m00 = 1;
        m01 = 0;
        m02 = 0;
        m10 = 0;
        m11 = 1;
        m12 = 0;
        return this;
    }

    public boolean isIdentity() {
        return (m00 == 1 && m10 == 0 &&
                m01 == 0 && m11 == 1 &&
                m02 == 0 && m12 == 0);
    }

    public float determinant() {
        return m00 * m11 - m01 * m10;
    }

    public Affine invert() {
        float det = determinant();
        if (det == 0) {
            throw  new RuntimeException("Can't invert a singular affine matrix");
        }

        float invDet = 1.0f / det;

        float tmp00 = m11;
        float tmp01 = -m01;
        float tmp02 = m01 * m12 - m11 * m02;
        float tmp10 = -m10;
        float tmp11 = m00;
        float tmp12 = m10 * m02 - m00 * m12;

        m00 = invDet * tmp00;
        m01 = invDet * tmp01;
        m02 = invDet * tmp02;
        m10 = invDet * tmp10;
        m11 = invDet * tmp11;
        m12 = invDet * tmp12;
        return this;
    }

    public Affine set(Affine other) {
        m00 = other.m00;
        m01 = other.m01;
        m02 = other.m02;
        m10 = other.m10;
        m11 = other.m11;
        m12 = other.m12;
        return this;
    }

    public Affine set(float[] data6) {
        m00 = data6[0];
        m01 = data6[1];
        m02 = data6[2];
        m10 = data6[3];
        m11 = data6[4];
        m12 = data6[5];
        return this;
    }

    public Affine set(float v00, float v10,
                      float v01, float v11,
                      float v02, float v12) {
        m00 = v00;
        m10 = v10;
        m01 = v01;
        m11 = v11;
        m02 = v02;
        m12 = v12;
        return this;
    }

    public Affine setAll(float x, float y, float scaleX, float scaleY, float rotation) {
        m02 = x;
        m12 = y;

        if (rotation == 0) {
            m00 = scaleX;
            m01 = 0;
            m10 = 0;
            m11 = scaleY;
        } else {
            float sin = (float) Math.sin(rotation);
            float cos = (float) Math.cos(rotation);

            m00 = cos * scaleX;
            m01 = -sin * scaleY;
            m10 = sin * scaleX;
            m11 = cos * scaleY;
        }
        return this;
    }

    public Affine translate(float x, float y) {
        m02 += x;
        m12 += y;
        return this;
    }

    public Affine toTranslation(float x, float y) {
        m00 = 1;
        m01 = 0;
        m02 = x;
        m10 = 0;
        m11 = 1;
        m12 = y;
        return this;
    }

    public Affine scale(float scaleX, float scaleY) {
        m00 *= scaleX;
        m01 *= scaleX;
        m02 *= scaleX;
        m10 *= scaleY;
        m11 *= scaleY;
        m12 *= scaleY;
        return this;
    }

    public Affine toScaling(float scaleX, float scaleY) {
        m00 = scaleX;
        m01 = 0;
        m02 = 0;
        m10 = 0;
        m11 = scaleY;
        m12 = 0;
        return this;
    }

    public Affine rotate(float angle) {
        if (angle == 0) return this;

        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        float tmp00 = cos * m00 - sin * m10;
        float tmp01 = cos * m01 - sin * m11;
        float tmp02 = cos * m02 - sin * m12;
        float tmp10 = sin * m00 + cos * m10;
        float tmp11 = sin * m01 + cos * m11;
        float tmp12 = sin * m02 + cos * m12;

        m00 = tmp00;
        m01 = tmp01;
        m02 = tmp02;
        m10 = tmp10;
        m11 = tmp11;
        m12 = tmp12;
        return this;
    }

    public Affine toRotation(float radians) {
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);

        m00 = cos;
        m01 = -sin;
        m02 = 0;
        m10 = sin;
        m11 = cos;
        m12 = 0;
        return this;
    }

    public Affine multiply(Affine other) {
        float tmp00 = m00 * other.m00 + m01 * other.m10;
        float tmp01 = m00 * other.m01 + m01 * other.m11;
        float tmp02 = m00 * other.m02 + m01 * other.m12 + m02;
        float tmp10 = m10 * other.m00 + m11 * other.m10;
        float tmp11 = m10 * other.m01 + m11 * other.m11;
        float tmp12 = m10 * other.m02 + m11 * other.m12 + m12;

        m00 = tmp00;
        m01 = tmp01;
        m02 = tmp02;
        m10 = tmp10;
        m11 = tmp11;
        m12 = tmp12;
        return this;
    }

    public Affine preMultiply(Affine other) {
        float tmp00 = other.m00 * m00 + other.m01 * m10;
        float tmp01 = other.m00 * m01 + other.m01 * m11;
        float tmp02 = other.m00 * m02 + other.m01 * m12 + other.m02;
        float tmp10 = other.m10 * m00 + other.m11 * m10;
        float tmp11 = other.m10 * m01 + other.m11 * m11;
        float tmp12 = other.m10 * m02 + other.m11 * m12 + other.m12;

        m00 = tmp00;
        m01 = tmp01;
        m02 = tmp02;
        m10 = tmp10;
        m11 = tmp11;
        m12 = tmp12;
        return this;
    }

    public float getTranslationX() {
        return m02;
    }

    public float getTranslationY() {
        return m12;
    }

    public float getScaleX() {
        return (float) Math.sqrt(m00 * m00 + m01 * m01);
    }

    public float getScaleY() {
        return (float) Math.sqrt(m10 * m10 + m11 * m11);
    }

    public float getRotation() {
        return (float) Math.atan2(m10, m00);
    }

    public float getPointX(float x, float y) {
        return m00 * x + m01 * y + m02;
    }

    public float getPointY(float x, float y) {
        return m10 * x + m11 * y + m12;
    }

}