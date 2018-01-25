package flat.math.stroke;

import flat.math.Mathf;
import flat.math.Vector2;

import java.util.Arrays;

public class QuadCurve {
    public float x1, y1, x2, y2;
    public float cx1, cy1;

    public QuadCurve(float x1, float y1, float cx1, float cy1, float x2, float y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.cx1 = cx1;
        this.cy1 = cy1;
    }

    public boolean isSimple() {
        Vector2 n1 = getNormal(0);
        Vector2 n2 = getNormal(1);
        float s = n1.x * n2.x + n1.y * n2.y;
        float angle = (float) Math.abs(Math.acos(s));
        return angle < Math.PI / 3f;
    }

    public QuadCurve[] split(float t) {
        final float pt1x = x1 * (1 - t) + cx1 * t;
        final float pt1y = y1 * (1 - t) + cy1 * t;
        final float pt2x = cx1 * (1 - t) + x2 * t;
        final float pt2y = cy1 * (1 - t) + y2 * t;
        final float pttx = pt1x * (1 - t) + pt2x * t;
        final float ptty = pt1y * (1 - t) + pt2y * t;

        QuadCurve left = new QuadCurve(x1, y1, pt1x, pt1y, pttx, ptty);
        QuadCurve right = new QuadCurve(pttx, ptty, pt2x, pt2y, x2, y2);
        return new QuadCurve[]{left, right};
    }

    public QuadCurve split(float t1, float t2) {
        // todo - Simplfy !
        if (t1 == 0) {
            return split(t2)[0];
        } else if (t2 == 1) {
            return split(t1)[1];
        } else {
            QuadCurve[] a = split(t1);
            t2 = (t2 - t1) / (1 - t1);

            return a[1].split(t2)[0];
        }
    }

    public Vector2 getDerivative(float t) {
        final float d1x = 2 * (cx1 - x1);
        final float d1y = 2 * (cy1 - y1);
        final float d2x = 2 * (x2 - cx1);
        final float d2y = 2 * (y2 - cy1);

        final float x = (1 - t) * d1x + t * d2x;
        final float y = (1 - t) * d1y + t * d2y;

        return new Vector2(x, y);
    }

    public Vector2 getPoint(float t) {
        final float x = (1 - t) * (1 - t) * x1 + 2 * (1 - t) * t * cx1 + t * t * x2;
        final float y = (1 - t) * (1 - t) * y1 + 2 * (1 - t) * t * cy1 + t * t * y2;
        return new Vector2(x, y);
    }

    public Vector2 getNormal(float t) {
        Vector2 d = getDerivative(t); // todo - manually
        final float q = (float) Math.sqrt(d.x * d.x + d.y * d.y);
        final float x = -d.y / q;
        final float y = d.x / q;
        return new Vector2(x, y);
    }

    public int getExtremas(float[] data) {
        final float d1x = 2 * (cx1 - x1);
        final float d1y = 2 * (cy1 - y1);
        final float d2x = 2 * (x2 - cx1);
        final float d2y = 2 * (y2 - cy1);

        float ex1 = d1x != d2x ? d1x / (d1x - d2x) : -1;
        float ex2 = d1y != d2y ? d1y / (d1y - d2y) : -1;
        int count = 0;
        data[count++] = 0;
        if (ex1 > 0 && ex1 < 1 && !contains(data, ex1)) data[count ++] = ex1;
        if (ex2 > 0 && ex2 < 1 && !contains(data, ex2)) data[count ++] = ex2;
        data[count++] = 1;
        Arrays.sort(data, 0, count - 1);
        return count;
    }

    public QuadCurve[] offset(float t) {
        float cusp = getCups();
        float[] cps = new float[2];
        QuadCurve[] sub = split(cusp);
        cps[0] = cusp * sub[0].getCups();
        cps[1] = (1 - cusp) * sub[1].getCups() + cusp;

        Vector2 n0 = getNormal(0);
        Vector2 n1 = getNormal(cps[0]);
        Vector2 n2 = getNormal(cusp);
        Vector2 n3 = getNormal(cps[1]);
        Vector2 n4 = getNormal(1);

        Vector2 p0 = new Vector2(n0).mul(t).add(getPoint(0f));
        Vector2 p1 = new Vector2(n1).mul(t).add(getPoint(cps[0]));
        Vector2 p2 = new Vector2(n2).mul(t).add(getPoint(cusp));
        Vector2 p3 = new Vector2(n3).mul(t).add(getPoint(cps[1]));
        Vector2 p4 = new Vector2(n4).mul(t).add(getPoint(1f));

        float[] col = new float[2];
        float x1 = p0.x;
        float y1 = p0.y;
        float x2 = p0.x + n0.y;
        float y2 = p0.y - n0.x;
        Intersections.lineLine(col,
                x1, y1, x2, y2,
                p1.x, p1.y, p1.x + n1.y, p1.y - n1.x);
        QuadCurve q0 = new QuadCurve(p0.x, p0.y, (x2 - x1) * col[0] + x1, (y2 - y1) * col[0] + y1,p1.x, p1.y);

        x1 = p1.x;
        y1 = p1.y;
        x2 = p1.x + n1.y;
        y2 = p1.y - n1.x;
        Intersections.lineLine(col,
                x1, y1, x2, y2,
                p2.x, p2.y, p2.x + n2.y, p2.y - n2.x);
        QuadCurve q1 = new QuadCurve(p1.x, p1.y, (x2 - x1) * col[0] + x1, (y2 - y1) * col[0] + y1, p2.x, p2.y);

        x1 = p2.x;
        y1 = p2.y;
        x2 = p2.x + n2.y;
        y2 = p2.y - n2.x;
        Intersections.lineLine(col,
                x1, y1, x2, y2,
                p3.x, p3.y, p3.x + n3.y, p3.y - n3.x);
        QuadCurve q2 = new QuadCurve(p2.x, p2.y, (x2 - x1) * col[0] + x1, (y2 - y1) * col[0] + y1, p3.x, p3.y);

        x1 = p3.x;
        y1 = p3.y;
        x2 = p3.x + n3.y;
        y2 = p3.y - n3.x;
        Intersections.lineLine(col,
                x1, y1, x2, y2,
                p4.x, p4.y, p4.x + n4.y, p4.y - n4.x);
        QuadCurve q3 = new QuadCurve(p3.x, p3.y, (x2 - x1) * col[0] + x1, (y2 - y1) * col[0] + y1, p4.x, p4.y);
        return new QuadCurve[] {q0, q1, q2, q3};
    }

    public float getCups() {
        Vector2 V0 = new Vector2(cx1 - x1, cy1 - y1);
        Vector2 V1 = new Vector2(x2 - cx1, y2 - cy1);
        double a =  new Vector2(V1).sub(V0).dot(new Vector2(V1).sub(V0));
        double b = 3 * (new Vector2(V1).dot(V0) - new Vector2(V0).dot(V0));
        double c = new Vector2(V0).mul(3).dot(V0) - new Vector2(V1).dot(V0);
        double d = -new Vector2(V0).dot(V0);
        double[] result = new double[3];
        int count = Mathf.solve(result, a, b, c, d);
        for (int i = 0; i < count; i++) {
            if (result[i] >= 0 && result[i] <= 1) {
                return (float) result[i];
            }
        }
        return 0.5f;
    }

    public QuadCurve scale(float d) {
        Vector2 n0 = getNormal(0).mul(d).add(x1, y1);
        Vector2 n05 = getNormal(0.5f).mul(d).add(cx1, cy1);
        Vector2 n1 = getNormal(1).mul(d).add(x2, y2);
        return new QuadCurve(n0.x, n0.y, n05.x, n05.y, n1.x, n1.y);
    }

    private boolean contains(float[] data, float val) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == val) return true;
        }
        return false;
    }
}