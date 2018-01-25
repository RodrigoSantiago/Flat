package flat.math;

import java.util.Arrays;

/**
 * Math utility methods.
 */
public final class Mathf {

    /**
     * A small number.
     */
    public static final float EPSILON = 0.00001f;

    /**
     * The circle constant
     */
    public static final float TAU = (float) (Math.PI * 2);

    /**
     * Twice Pi
     */
    public static final float TWO_PI = TAU;

    public static final float FOUR_PI = TWO_PI  * 2;

    /**
     * Pi times one half.
     */
    public static final float HALF_PI = (float) (Math.PI * 0.5);
    /**
     * The ratio of a circle's circumference to its diameter.
     */
    public static final float PI = (float) Math.PI;
    public static final float PI2 = PI * PI;
    /**
     * The base value of the natural logarithm.
     */
    public static final float E = (float) Math.E;

    /**
     * A cheaper version of {@link Math#round} that doesn't handle the special cases.
     */
    public static int round(float v) {
        return (v < 0f) ? (int) (v - 0.5f) : (int) (v + 0.5f);
    }

    /**
     * Returns the floor of v as an integer without calling the relatively expensive
     * {@link Math#floor}.
     */
    public static int ifloor(float v) {
        int iv = (int) v;
        return (v >= 0f || iv == v || iv == Integer.MIN_VALUE) ? iv : (iv - 1);
    }

    /**
     * Returns the ceiling of v as an integer without calling the relatively expensive
     * {@link Math#ceil}.
     */
    public static int iceil(float v) {
        int iv = (int) v;
        return (v <= 0f || iv == v || iv == Integer.MAX_VALUE) ? iv : (iv + 1);
    }

    /**
     * Clamps a value to the range [lower, upper].
     */
    public static float clamp(float v, float lower, float upper) {
        if (v < lower) return lower;
        else if (v > upper) return upper;
        else return v;
    }

    /**
     * Rounds a value to the nearest multiple of a target.
     */
    public static float roundNearest(float v, float target) {
        target = Math.abs(target);
        if (v >= 0) {
            return target * floor((v + 0.5f * target) / target);
        } else {
            return target * ceil((v - 0.5f * target) / target);
        }
    }

    /**
     * Checks whether the value supplied is in [lower, upper].
     */
    public static boolean isWithin(float v, float lower, float upper) {
        return v >= lower && v <= upper;
    }

    /**
     * Returns a random value according to the normal distribution with the provided mean and
     * standard deviation.
     *
     * @param normal a normally distributed random value.
     * @param mean   the desired mean.
     * @param stddev the desired standard deviation.
     */
    public static float normal(float normal, float mean, float stddev) {
        return stddev * normal + mean;
    }

    /**
     * Returns a random value according to the exponential distribution with the provided mean.
     *
     * @param random a uniformly distributed random value.
     * @param mean   the desired mean.
     */
    public static float exponential(float random, float mean) {
        return -log(1f - random) * mean;
    }

    /**
     * Linearly interpolates between two angles, taking the shortest path around the circle.
     * This assumes that both angles are in [-pi, +pi].
     */
    public static float lerpa(float a1, float a2, float t) {
        float ma1 = mirrorAngle(a1), ma2 = mirrorAngle(a2);
        float d = Math.abs(a2 - a1), md = Math.abs(ma1 - ma2);
        return (d <= md) ? lerp(a1, a2, t) : mirrorAngle(lerp(ma1, ma2, t));
    }

    /**
     * Linearly interpolates between v1 and v2 by the parameter t.
     */
    public static float lerp(float v1, float v2, float t) {
        return v1 + t * (v2 - v1);
    }

    /**
     * Determines whether two values are "close enough" to equal.
     */
    public static boolean epsilonEquals(float v1, float v2) {
        return Math.abs(v1 - v2) < EPSILON;
    }

    /**
     * Returns the (shortest) distance between two angles, assuming that both angles are in
     * [-pi, +pi].
     */
    public static float angularDistance(float a1, float a2) {
        float ma1 = mirrorAngle(a1), ma2 = mirrorAngle(a2);
        return Math.min(Math.abs(a1 - a2), Math.abs(ma1 - ma2));
    }

    /**
     * Returns the (shortest) difference between two angles, assuming that both angles are in
     * [-pi, +pi].
     */
    public static float angularDifference(float a1, float a2) {
        float ma1 = mirrorAngle(a1), ma2 = mirrorAngle(a2);
        float diff = a1 - a2, mdiff = ma2 - ma1;
        return (Math.abs(diff) < Math.abs(mdiff)) ? diff : mdiff;
    }

    /**
     * Returns an angle in the range [-pi, pi).
     */
    public static float normalizeAngle(float a) {
        while (a < -PI) {
            a += TWO_PI;
        }
        while (a >= PI) {
            a -= TWO_PI;
        }
        return a;
    }

    /**
     * Returns an angle in the range [0, 2pi).
     */
    public static float normalizeAnglePositive(float a) {
        while (a < 0f) {
            a += TWO_PI;
        }
        while (a >= TWO_PI) {
            a -= TWO_PI;
        }
        return a;
    }

    /**
     * Returns the mirror angle of the specified angle (assumed to be in [-pi, +pi]). The angle is
     * mirrored around the PI/2 if it is positive, and -PI/2 if it is negative. One can visualize
     * this as mirroring around the "y-axis".
     */
    public static float mirrorAngle(float a) {
        return (a > 0f ? PI : -PI) - a;
    }

    /**
     * Sets the number of decimal places to show when formatting values. By default, they are
     * formatted to three decimal places.
     */
    public static void setToStringDecimalPlaces(int places) {
        if (places < 0) throw new IllegalArgumentException("Decimal places must be >= 0.");
        TO_STRING_DECIMAL_PLACES = places;
    }

    /**
     * Formats the supplied value, truncated to the currently configured number of decimal places.
     * The value is also always preceded by a sign (e.g. +1.0 or -0.5).
     */
    public static String toString(float value) {
        return toString(value, TO_STRING_DECIMAL_PLACES);
    }

    /**
     * Formats the supplied floating point value, truncated to the given number of decimal places.
     * The value is also always preceded by a sign (e.g. +1.0 or -0.5).
     */
    public static String toString(float value, int decimalPlaces) {
        if (Float.isNaN(value)) return "NaN";

        StringBuilder buf = new StringBuilder();
        if (value >= 0) buf.append("+");
        else {
            buf.append("-");
            value = -value;
        }
        int ivalue = (int) value;
        buf.append(ivalue);
        if (decimalPlaces > 0) {
            buf.append(".");
            for (int ii = 0; ii < decimalPlaces; ii++) {
                value = (value - ivalue) * 10;
                ivalue = (int) value;
                buf.append(ivalue);
            }
            // trim trailing zeros
            for (int ii = 0; ii < decimalPlaces - 1; ii++) {
                if (buf.charAt(buf.length() - 1) == '0') {
                    buf.setLength(buf.length() - 1);
                }
            }
        }
        return buf.toString();
    }

    protected static int TO_STRING_DECIMAL_PLACES = 3;

    /**
     * Computes and returns the sine of the given angle.
     *
     * @see Math#sin
     */
    public static float sin(float a) {
        return (float) Math.sin(a);
    }

    /**
     * Computes and returns the cosine of the given angle.
     *
     * @see Math#cos
     */
    public static float cos(float a) {
        return (float) Math.cos(a);
    }

    /**
     * Computes and returns the tangent of the given angle.
     *
     * @see Math#tan
     */
    public static float tan(float a) {
        return (float) Math.tan(a);
    }

    /**
     * Computes and returns the arc sine of the given value.
     *
     * @see Math#asin
     */
    public static float asin(float a) {
        return (float) Math.asin(a);
    }

    /**
     * Computes and returns the arc cosine of the given value.
     *
     * @see Math#acos
     */
    public static float acos(float a) {
        return (float) Math.acos(a);
    }

    /**
     * Computes and returns the arc tangent of the given value.
     *
     * @see Math#atan
     */
    public static float atan(float a) {
        return (float) Math.atan(a);
    }

    /**
     * Computes and returns the arc tangent of the given values.
     *
     * @see Math#atan2
     */
    public static float atan2(float y, float x) {
        return (float) Math.atan2(y, x);
    }

    /**
     * Converts from radians to degrees.
     *
     * @see Math#toDegrees
     */
    public static float toDegrees(float a) {
        return a * (180f / PI);
    }

    /**
     * Converts from degrees to radians.
     *
     * @see Math#toRadians
     */
    public static float toRadians(float a) {
        return a * (PI / 180f);
    }

    /**
     * Returns the square root of the supplied value.
     *
     * @see Math#sqrt
     */
    public static float sqrt(float v) {
        return (float) Math.sqrt(v);
    }

    /**
     * Returns the cube root of the supplied value.
     *
     * @see Math#cbrt
     */
    public static float cbrt(float v) {
        return (float) Math.cbrt(v);
    }

    /**
     * Computes and returns sqrt(x*x + y*y).
     *
     * @see Math#hypot
     */
    public static float hypot(float x, float y) {
        return (float) Math.hypot(x, y);
    }

    /**
     * Returns e to the power of the supplied value.
     *
     * @see Math#exp
     */
    public static float exp(float v) {
        return (float) Math.exp(v);
    }

    /**
     * Returns the natural logarithm of the supplied value.
     *
     * @see Math#log
     */
    public static float log(float v) {
        return (float) Math.log(v);
    }

    /**
     * Returns the base 10 logarithm of the supplied value.
     *
     * @see Math#log10
     */
    public static float log10(float v) {
        return (float) Math.log10(v);
    }

    /**
     * Returns v to the power of e.
     *
     * @see Math#pow
     */
    public static float pow(float v, float e) {
        return (float) Math.pow(v, e);
    }

    /**
     * Returns the floor of v.
     *
     * @see Math#floor
     */
    public static float floor(float v) {
        return (float) Math.floor(v);
    }

    /**
     * Returns the ceiling of v.
     *
     * @see Math#ceil
     */
    public static float ceil(float v) {
        return (float) Math.ceil(v);
    }

    public static int floorInt(double d) {
        int id = (int) d;
        return d == id || d > 0 ? id : id - 1;
    }

    public static int ceilInt(double d) {
        int id = (int) d;
        return d == id || d < 0 ? id : -((int) (-d)) + 1;
    }

    public static boolean isZero(float value) {
        return value < EPSILON && value > -EPSILON;
    }

    public static boolean isEqual(float valueA, float valueB) {
        float dif = valueA - valueB;
        return dif < EPSILON && dif > -EPSILON;
    }

    public static int solve(double[] result, double a, double b, double c, double d) {
        int count = 0;
        if (a == 0.0) {
            if (b == 0.0) {
                if (c == 0.0) {
                    result[count++] = d;
                    return count;
                } else {
                    result[count++] = d / c;
                    return count;
                }
            } else {
                double delta = Math.sqrt(c * c - 4 * b * d);
                result[count++] = (-c + delta) / (2 * b);
                result[count++] = (-c - delta) / (2 * b);
                Arrays.sort(result, 0, count);
                return count;
            }
        } else {
            double denom = a;
            a = b / denom;
            b = c / denom;
            c = d / denom;

            double a_over_3 = a / 3.0;
            double Q = (3 * b - a * a) / 9.0;
            double Q_CUBE = Q * Q * Q;
            double R = (9 * a * b - 27 * c - 2 * a * a * a) / 54.0;
            double R_SQR = R * R;
            double D = Q_CUBE + R_SQR;

            if (D < 0.0) {
                double theta = Math.acos(R / Math.sqrt(-Q_CUBE));
                double SQRT_Q = Math.sqrt(-Q);
                result[count++] = 2.0 * SQRT_Q * Math.cos(theta / 3.0) - a_over_3;
                result[count++] = 2.0 * SQRT_Q * Math.cos((theta + TWO_PI) / 3.0) - a_over_3;
                result[count++] = 2.0 * SQRT_Q * Math.cos((theta + FOUR_PI) / 3.0) - a_over_3;
            } else if (D > 0.0) {
                double SQRT_D = Math.sqrt(D);
                double S = Math.cbrt(R + SQRT_D);
                double T = Math.cbrt(R - SQRT_D);
                result[count++] = (S + T) - a_over_3;
            } else {
                double CBRT_R = Math.cbrt(R);
                result[count++] = 2 * CBRT_R - a_over_3;
                result[count++] = CBRT_R - a_over_3;
            }
            Arrays.sort(result, 0, count);
            return count;
        }
    }
}