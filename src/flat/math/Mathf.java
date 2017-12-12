package flat.math;

public class Mathf {
    static public final float nanoToSec = 1 / 1000000000f;
    static public final float PI = 3.1415927f;
    static public final float PI2 = PI * 2;
    static public final float E = 2.7182818f;
    static public final float radDeg = 180f / PI;
    static public final float degRad = PI / 180;

    public static float epsilon = 0.000001f;

    public static boolean isEqual(float a, float b) {
        return a == b || ((a - epsilon < b) && (a + epsilon > b));
    }

    public static boolean isZero(float a) {
        return a == 0 || ((a - epsilon < 0) && (a + epsilon > 0));
    }

    public static float clamp(float number, float min, float max) {
        return number < min ? min : number > max ? max : number;
    }

    public static float atan2(float y, float x) {
        return (float) Math.atan2(y, x);
    }
}
