package flat.math.stroke;

import flat.math.Vector2;

import java.util.Arrays;

public class CubicCurve {
    public float x1, y1, x2, y2;
    public float cx1, cy1, cx2, cy2;

    public CubicCurve(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.cx1 = cx1;
        this.cy1 = cy1;
        this.cx2 = cx2;
        this.cy2 = cy2;
        this.x2 = x2;
        this.y2 = y2;
    }

    public boolean isSimple() {
        Vector2 n1 = getNormal(0);
        Vector2 n2 = getNormal(1);
        float s = n1.x * n2.x + n1.y * n2.y;
        float angle = (float) Math.abs(Math.acos(s));
        return angle < Math.PI / 3f;
    }

    public CubicCurve[] split(float t) {
        final float pt1x = x1 * (1 - t) + cx1 * t;
        final float pt1y = y1 * (1 - t) + cy1 * t;
        final float pt2x = cx1 * (1 - t) + cx2 * t;
        final float pt2y = cy1 * (1 - t) + cy2 * t;
        final float pt3x = cx2 * (1 - t) + x2 * t;
        final float pt3y = cy2 * (1 - t) + y2 * t;
        final float pttx1 = pt1x * (1 - t) + pt2x * t;
        final float ptty1 = pt1y * (1 - t) + pt2y * t;
        final float pttx2 = pt2x * (1 - t) + pt3x * t;
        final float ptty2 = pt2y * (1 - t) + pt3y * t;
        final float ptttx = pttx1 * (1 - t) + pttx2 * t;
        final float pttty = ptty1 * (1 - t) + ptty2 * t;

        return new CubicCurve[]{
                new CubicCurve(x1, y1, pt1x, pt1y, pttx1, ptty1, ptttx, pttty),
                new CubicCurve(ptttx, pttty, pttx2, ptty2, pt3x, pt3y, x2, y2)};
    }

    public CubicCurve split(float t1, float t2) {
        // todo - Simplfy !
        if (t1 == 0) {
            return split(t2)[0];
        } else if (t2 == 1) {
            return split(t1)[1];
        } else {
            CubicCurve[] a = split(t1);
            t2 = (t2 - t1) / (1 - t1);

            return a[1].split(t2)[0];
        }
    }

    public Vector2 getDerivative(float t) {
        final float dx1 = 3 * (cx1 - x1);
        final float dy1 = 3 * (cy1 - y1);
        final float dx2 = 3 * (cx2 - cx1);
        final float dy2 = 3 * (cy2 - cy1);
        final float dx3 = 3 * (x2 - cx2);
        final float dy3 = 3 * (y2 - cy2);

        final float mt = 1 - t;
        final float a = mt * mt;
        final float b = mt * t * 2;
        final float c = t * t;
        return new Vector2(a * dx1 + b * dx2 + c * dx3, a * dy1 + b * dy2 + c * dy3);
    }

    public Vector2 getPoint(float t) {
        final float mt = 1 - t;
        final float mt2 = mt * mt;
        final float t2 = t * t;
        final float a = mt2 * mt;
        final float b = mt2 * t * 3;
        final float c = mt * t2 * 3;
        final float d = t * t2;
        return new Vector2(
                a * x1 + b * cx1 + c * cx2 + d * x2,
                a * y1 + b * cy1 + c * cy2 + d * y2);
    }

    public Vector2 getNormal(float t) {
        Vector2 d = getDerivative(t);
        final float q = (float) Math.sqrt(d.x * d.x + d.y * d.y);
        return new Vector2(-d.y / q, d.x / q);
    }

    public int getExtremas(float[] data) {
        final float dx1 = 3 * (cx1 - x1);
        final float dy1 = 3 * (cy1 - y1);
        final float dx2 = 3 * (cx2 - cx1);
        final float dy2 = 3 * (cy2 - cy1);
        final float dx3 = 3 * (x2 - cx2);
        final float dy3 = 3 * (y2 - cy2);

        final float d1x1 = 2 * (dx2 - dx1);
        final float d1y1 = 2 * (dy2 - dy1);
        final float d1x2 = 2 * (dx3 - dx2);
        final float d1y2 = 2 * (dy3 - dy2);

        int count = 0;
        data[count++] = 0;

        float d = dx1 - 2 * dx2 + dx3;
        if (d != 0) {
            float m1 = (float) -Math.sqrt(dx2 * dx2 - dx1 * dx3);
            float m2 = -dx1 + dx2;
            float v1 = -(m1 + m2) / d;
            float v2 = -(-m1 + m2) / d;
            if (v1 > 0 && v1 < 1 && !contains(data, v1)) data[count++] = v1;
            if (v2 > 0 && v2 < 1 && !contains(data, v1)) data[count++] = v2;
        } else if (dx2 != dx3) {
            float v1 = (2 * dx2 - dx3) / (2 * (dx2 - dx3));
            if (v1 > 0 && v1 < 1 && !contains(data, v1)) data[count++] = v1;
        }

        d = dy1 - 2 * dy2 + dy3;
        if (d != 0) {
            float m1 = (float) -Math.sqrt(dy2 * dy2 - dy1 * dy3);
            float m2 = -dy1 + dy2;
            float v1 = -(m1 + m2) / d;
            float v2 = -(-m1 + m2) / d;
            if (v1 > 0 && v1 < 1 && !contains(data, v1)) data[count++] = v1;
            if (v2 > 0 && v2 < 1 && !contains(data, v1)) data[count++] = v2;
        } else if (dy2 != dy3) {
            float v1 = (2 * dy2 - dy3) / (2 * (dy2 - dy3));
            if (v1 > 0 && v1 < 1 && !contains(data, v1)) data[count++] = v1;
        }

        float ex1 = d1x1 != d1x2 ? d1x1 / (d1x1 - d1x2) : -1;
        float ex2 = d1y1 != d1y2 ? d1y1 / (d1y1 - d1y2) : -1;
        if (ex1 > 0 && ex1 < 1 && !contains(data, ex1)) data[count ++] = ex1;
        if (ex2 > 0 && ex2 < 1 && !contains(data, ex2)) data[count ++] = ex2;

        data[count++] = 1;
        Arrays.sort(data, 0, count - 1);
        return count;
    }

    public CubicCurve[] offset(float d) {
        CubicCurve[] reduced = this.reduce();
        if (reduced.length == 0) {
            return new CubicCurve[]{scale(d)};
        } else {
            for (int i = 0; i < reduced.length; i++) {
                reduced[i] = reduced[i].scale(d);
            }
            return reduced;
        }
    }

    public CubicCurve[] reduceGranuality() {
        float[] extrema = CubicGranuality.getSections(x1, y1, cx1, cy1, cx2, cy2, x2, y2);
        int extremas = extrema.length;
        double t1 = extrema[0];
        CubicCurve[] curves = new CubicCurve[extremas - 1];
        double t2;
        for(int i = 1; i < extremas; i++) {
            t2 = extrema[i];
            curves[i - 1] = split((float)t1, (float)t2);
            t1 = t2;
        }
        return curves;
    }

    public CubicCurve[] reduceCount(int count) {
        CubicCurve[] curves = new CubicCurve[count];
        for(int i = 0; i < count; i++) {
            curves[i] = split((float)i / (count), (i + 1f) / (count));
        }
        return curves;
    }

    public CubicCurve[] reduceExtrema() {
        float[] extrema = new float[8];
        int extremas = getExtremas(extrema);
        double t1 = extrema[0];
        CubicCurve[] curves = new CubicCurve[extremas - 1];
        double t2;
        for(int i = 1; i < extremas; i++) {
            t2 = extrema[i];
            curves[i - 1] = split((float)t1, (float)t2);
            t1 = t2;
        }
        return curves;
    }

    public CubicCurve[] reduceSplit() {
        float[] extrema = new float[40];
        int extremas = getSplits(extrema, (float) Math.toRadians(15));
        double t1 = extrema[0];
        CubicCurve[] curves = new CubicCurve[extremas - 1];
        double t2;
        for(int i = 1; i < extremas; i++) {
            t2 = extrema[i];
            curves[i - 1] = split((float)t1, (float)t2);
            t1 = t2;
        }
        return curves;
    }

    public CubicCurve[] reduceSmart() {
        float[] extrema = CubicGranuality.getSections(x1, y1, cx1, cy1, cx2, cy2, x2, y2);
        int extremas = getSplits(extrema, extrema, 15f);
        double t1 = extrema[0];
        CubicCurve[] curves = new CubicCurve[extremas - 1];
        double t2;
        for(int i = 1; i < extremas; i++) {
            t2 = extrema[i];
            curves[i - 1] = split((float)t1, (float)t2);
            t1 = t2;
        }
        return curves;
    }

    public float getCups() {
        return (float) getCloserPoint((cx1 + cx2)/2f, (cy1 + cy2)/2f);
    }

    public CubicCurve[] reduce() {
        CubicCurve[] cvs = new CubicCurve[8];
        CubicCurve[] sub1 = split(getCups());
        CubicCurve[] sub10 = sub1[0].split(sub1[0].getCups());
        CubicCurve[] sub100 = sub10[0].split(sub10[0].getCups());
        CubicCurve[] sub101 = sub10[1].split(sub10[1].getCups());

        CubicCurve[] sub11 = sub1[1].split(sub1[1].getCups());
        CubicCurve[] sub110 = sub11[0].split(sub11[0].getCups());
        CubicCurve[] sub111 = sub11[1].split(sub11[1].getCups());

        cvs[0] = sub100[0];
        cvs[1] = sub100[1];
        cvs[2] = sub101[0];
        cvs[3] = sub101[1];
        cvs[4] = sub110[0];
        cvs[5] = sub110[1];
        cvs[6] = sub111[0];
        cvs[7] = sub111[1];
        return cvs;
    }

    public boolean isLinear() {
        return (Math.abs((y1 - cy1) * (x1 - x2) - (y1 - y2) * (x1 - cx1)) < 0.1f &&
                Math.abs((y1 - cy2) * (x1 - x2) - (y1 - y2) * (x1 - cx2)) < 0.1f);
    }
    public CubicCurve scale(float d) {
        Vector2 vec0 = getDerivative(0);
        Vector2 vec1 = getDerivative(1);
        if (isLinear() || (vec0.x == 0 && vec0.y == 0) || (vec1.x == 0 && vec1.y == 0)) {
            Vector2 normal = new Vector2(y1 - y2, -(x1 - x2));
            Vector2 p1 = new Vector2(normal).length(d).add(x1, y1);
            Vector2 p2 = new Vector2(normal).length(d).add(cx1, cy1);
            Vector2 p3 = new Vector2(normal).length(d).add(cx2, cy2);
            Vector2 p4 = new Vector2(normal).length(d).add(x2, y2);
            return new CubicCurve(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
        } else {
            Vector2 n1 = new Vector2(y1 - cy1, -(x1 - cx1)).length(d);
            Vector2 n2 = new Vector2(cy1 - cy2, -(cx1 - cx2)).length(d);
            Vector2 n3 = new Vector2(cy2 - y2, -(cx2 - x2)).length(d);

            final float q0 = (float) Math.sqrt(vec0.x * vec0.x + vec0.y * vec0.y);
            final float q1 = (float) Math.sqrt(vec1.x * vec1.x + vec1.y * vec1.y);

            Vector2 p1 = new Vector2(-vec0.y / q0, vec0.x / q0).length(d).add(x1, y1);
            Vector2 p4 = new Vector2(-vec1.y / q1, vec1.x / q1).length(d).add(x2, y2);

            float[] result = new float[2];
            float l1_x1 = x1 + n1.x;
            float l1_y1 = y1 + n1.y;
            float l1_x2 = cx1 + n1.x;
            float l1_y2 = cy1 + n1.y;
            float l2_x1 = cx1 + n2.x;
            float l2_y1 = cy1 + n2.y;
            float l2_x2 = cx2 + n2.x;
            float l2_y2 = cy2 + n2.y;
            boolean it = Intersections.lineLine(result, l1_x1, l1_y1, l1_x2, l1_y2, l2_x1, l2_y1, l2_x2, l2_y2);
            Vector2 p2 = new Vector2(
                    (l1_x2 - l1_x1) * result[0] + l1_x1,
                    (l1_y2 - l1_y1) * result[0] + l1_y1);

            float dist = Vector2.distance(p2.x, p2.y, (l1_x2 + l2_x1) / 2f, (l1_y2 + l2_y1) / 2f);
            if (dist > Math.abs(d) || !it) {
                Vector2 n = new Vector2(
                        (l1_x2 + l2_x1) / 2f - cx1,
                        (l1_y2 + l2_y1) / 2f - cy1).normalize().mul(Math.abs(d));
                p2.set((l1_x2 + l2_x1) / 2f, (l1_y2 + l2_y1) / 2f).add(n);
            }

            l1_x1 = cx1 + n2.x;
            l1_y1 = cy1 + n2.y;
            l1_x2 = cx2 + n2.x;
            l1_y2 = cy2 + n2.y;
            l2_x1 = cx2 + n3.x;
            l2_y1 = cy2 + n3.y;
            l2_x2 = x2 + n3.x;
            l2_y2 = y2 + n3.y;
            it = Intersections.lineLine(result, l1_x1, l1_y1, l1_x2, l1_y2, l2_x1, l2_y1, l2_x2, l2_y2);
            Vector2 p3 = new Vector2(
                    (l1_x2 - l1_x1) * result[0] + l1_x1,
                    (l1_y2 - l1_y1) * result[0] + l1_y1);

            dist = Vector2.distance(p3.x, p3.y, (l1_x2 + l2_x1) / 2f, (l1_y2 + l2_y1) / 2f);
            if (dist > Math.abs(d) || !it) {
                Vector2 n = new Vector2(
                        (l1_x2 + l2_x1) / 2f - cx2,
                        (l1_y2 + l2_y1) / 2f - cy2).normalize().mul(Math.abs(d));
                p3.set((l1_x2 + l2_x1) / 2f, (l1_y2 + l2_y1) / 2f).add(n);
            }
            return new CubicCurve(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
        }
    }

    public int getSplits(float[] ts, float[] data, float angleLimit) {
        int count = 0;
        data[count++] = 0;

        Vector2 last = getPoint(0);
        Vector2 point;
        float angle = 0;
        float sangle = 0;
        boolean difType = false;
        for (int i = 1; i < ts.length - 1; i++) {
            point = getPoint(ts[i]);
            float angle2 = getAngle(last.x, last.y, point.x, point.y);
            float dif = Math.abs(getDifference(angle, angle2));
            boolean type = getSide(sangle, angle2);
            if (i > 1 && (dif > angleLimit || difType != type)) {
                angle = angle2;
                data[count++] = ts[i - 1];
            }
            sangle = angle2;
            difType = type;
            last = point;
        }
        data[count++] = 1;
        return count;
    }

    public int getSplits(float[] data, float angleLimit) {
        int count = 0;
        data[count++] = 0;

        Vector2 last = getPoint(0);
        Vector2 point;
        float angle = 0;
        float sangle = 0;
        boolean difType = false;
        for (int i = 1; i < data.length - 2; i++) {
            point = getPoint((i + 1) / (data.length - 1));
            float angle2 = getAngle(last.x, last.y, point.x, point.y);
            float dif = Math.abs(getDifference(angle, angle2));
            boolean type = getSide(sangle, angle2);
            if (i > 1 && (dif > angleLimit || difType != type)) {
                angle = angle2;
                data[count++] = i / (data.length - 1);
            }
            sangle = angle2;
            difType = type;
            last = point;
        }
        data[count++] = 1;
        return count;
    }

    private static  boolean contains(float[] data, float val) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == val) return true;
        }
        return false;
    }

    private static float getAngle(float x, float y, float x2, float y2) {
        float angle = (float) Math.toDegrees(Math.atan2(y2 - y, x2 - x));

        if(angle < 0){
            angle += 360;
        }

        return angle;
    }

    private static float getDifference(float a1, float a2) {
        return Math.min((a1 - a2) < 0 ? a1 - a2 + 360 : a1 - a2, (a2 - a1) < 0 ? a2 - a1 + 360 : a2 - a1);
    }

    private static boolean getSide(float a1, float a2) {
        if (a1 < 180 && a2 < a1 + 180) {
            return true; // CCW
        }
        if (a1 > 180 && (a2 > a1 || a2 < a1 - 180)) {
            return true; // CCW
        }
        return false;
    }

    public double getCloserPoint(double fx, double fy) {
        return getClosestPointToCubicBezier(5, fx, fy, 0, 1d, 20, x1, y1, cx1, cy1, cx2, cy2, x2, y2);
    }

    private static double getClosestPointToCubicBezier(int iterations, double fx, double fy, double start, double end, int slices, double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
        if (iterations <= 0) return (start + end) / 2;
        double tick = (end - start) / (double) slices;
        double x, y, dx, dy;
        double best = 0;
        double bestDistance = Double.POSITIVE_INFINITY;
        double currentDistance;
        double t = start;
        while (t <= end) {
            //B(t) = (1-t)**3 p0 + 3(1 - t)**2 t P1 + 3(1-t)t**2 P2 + t**3 P3
            x = (1 - t) * (1 - t) * (1 - t) * x0 + 3 * (1 - t) * (1 - t) * t * x1 + 3 * (1 - t) * t * t * x2 + t * t * t * x3;
            y = (1 - t) * (1 - t) * (1 - t) * y0 + 3 * (1 - t) * (1 - t) * t * y1 + 3 * (1 - t) * t * t * y2 + t * t * t * y3;

            dx = x - fx;
            dy = y - fy;
            dx *= dx;
            dy *= dy;
            currentDistance = dx + dy;
            if (currentDistance < bestDistance) {
                bestDistance = currentDistance;
                best = t;
            }
            t += tick;
        }
        return getClosestPointToCubicBezier(iterations - 1, fx, fy, Math.max(best - tick, 0d), Math.min(best + tick, 1d), slices, x0, y0, x1, y1, x2, y2, x3, y3);
    }

}
