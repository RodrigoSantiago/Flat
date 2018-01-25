package flat.math.stroke;

import flat.math.Mathf;
import flat.math.Vector2;

public class Intersections {
    public static boolean lineLine(float[] result,
                                   float ax1, float ay1, float ax2, float ay2,
                                   float bx1, float by1, float bx2, float by2) {
        float denominator = ((ax2 - ax1) * (by2 - by1)) - ((ay2 - ay1) * (bx2 - bx1));
        float numerator1 = ((ay1 - by1) * (bx2 - bx1)) - ((ax1 - bx1) * (by2 - by1));
        float numerator2 = ((ay1 - by1) * (ax2 - ax1)) - ((ax1 - bx1) * (ay2 - ay1));

        // TODO - COLLINEAR RESPONSE
        if (Mathf.isZero(denominator)) {
            /*if (numerator1 == 0 && numerator2 == 0) {
                result[0] = result[1] = 0;
                return true;
            } else {
                return false;
            }*/
            return false;
        }

        result[0] = numerator1 / denominator;
        result[1] = numerator2 / denominator;
        return true;
    }

    public static float lineQuad(
            float l1x1, float l1y1, float l1x2, float l1y2,
            float qx1, float qy1, float qcx1, float qcy1, float qx2, float qy2) {
        return 0;
    }

    public static boolean lineCubic(
            float[] result,
            float ax1, float ay1, float ax2, float ay2,
            float bx1, float by1, float bcx1, float bcy1, float bcx2, float bcy2, float bx2, float by2) {

        Object[] data = CubicGranuality.getAll(bx1, by1, bcx1, bcy1, bcx2, bcy2, bx2, by2);
        float[] fSections = (float[]) data[0];
        Vector2[] fPoints = (Vector2[]) data[1];
        for (int i = 1; i < fPoints.length; i++) {
            boolean it = Intersections.lineLine(result,
                    ax1, ay1, ax2, ay2,
                    fPoints[i - 1].x, fPoints[i - 1].y, fPoints[i].x, fPoints[i].y);
            if (it) {
                result[1] = (fSections[i] - fSections[i - 1]) * result[1] + fSections[i - 1];
                return true;
            }
        }
        return false;
    }

    public static float quadQuad(
            float q1x1, float q1y1, float q1cx1, float q1cy1, float q1x2, float q1y2,
            float q2x1, float q2y1, float q2cx1, float q2cy1, float q2x2, float q2y2) {
        return 0;
    }

    public static float quadCubic(
            float qx1, float qy1, float qcx1, float qcy1, float qx2, float qy2,
            float cx1, float cy1, float ccx1, float ccy1, float ccx2, float ccy2,float cx2, float cy2) {
        return 0;
    }

    public static float cubicCubic(
            float c1x1, float c1y1, float c1cx1, float c1cy1, float c1cx2, float c1cy2,float c1x2, float c1y2,
            float c2x1, float c2y1, float c2cx1, float c2cy1, float c2cx2, float c2cy2,float c2x2, float c2y2) {
        return 0;
    }


    public static float[] inflections(Vector2[] points) {
        if (points.length < 4) return new float[0];

        // FIXME: TODO: add in inflection abstraction for quartic+ curves?

        Vector2[] p = align(points, points[0].x, points[0].y, points[points.length - 1].x, points[points.length - 1].y);
        float a = p[2].x * p[1].y,
                b = p[3].x * p[1].y,
                c = p[1].x * p[2].y,
                d = p[3].x * p[2].y,
                v1 = 18 * (-3 * a + 2 * b + 3 * c - d),
                v2 = 18 * (3 * a - b - 3 * c),
                v3 = 18 * (c - a);

        if (Mathf.isZero(v1)) {
            if (!Mathf.isZero(v2)) {
                float t = -v3 / v2;
                if (0 <= t && t <= 1)
                    return new float[]{t};
            }
            return new float[0];
        }

        float trm = v2 * v2 - 4 * v1 * v3,
                sq = Mathf.sqrt(trm),
                d2 = 2 * v1;

        if (Mathf.isZero(d2)) return new float[0];

        float x1 = (sq - v2) / d2;
        float x2 = -(v2 + sq) / d2;
        if (x1 >= 0 && x1 <= 1) {
            if (x2 >= 0 && x1 <= 1) {
                return new float[]{x1, x2};
            } else {
                return new float[]{x1};
            }
        } else if (x2 >= 0 && x2 <= 1) {
            return new float[]{x2};
        }

        return null; // assert
    }


    static Vector2[] align(Vector2[] points, float x1, float y1, float x2, float y2) {
        double tx = x1;
        double ty = y1;
        double a = -Math.atan2(y2 - ty, x2 - tx);
        Vector2[] map = new Vector2[points.length];
        for (int i = 0; i < points.length; i++) {
            Vector2 p = points[i];
            map[i] = new Vector2(
                    (float)((p.x - tx) * Math.cos(a) - (p.y - ty) * Math.sin(a)),
                    (float)((p.x - tx) * Math.sin(a) + (p.y - ty) * Math.cos(a)));
        }
        return map;
    }
}
