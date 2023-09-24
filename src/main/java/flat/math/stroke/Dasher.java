package flat.math.stroke;

import flat.math.shapes.Path;
import flat.math.shapes.PathIterator;

public class Dasher {

    float[] dash;
    public Path path;
    float dashPhase;
    float dashPhaseLimit;

    float px, py;
    float phase;
    float phaseNext;
    int phaseIndex;
    boolean phaseFill;
    boolean open;

    public Dasher(PathIterator it, float[] dash, float dashPhase) {
        path = new Path();
        if (dash.length % 2 == 0) {
            this.dash = new float[dash.length];
            for (int i = 0; i < dash.length; i++) {
                this.dash[i] = dash[i] <= 0 ? 0.01f : dash[i];
                this.dashPhaseLimit += dash[i];
            }
        } else {
            this.dash = new float[dash.length * 2];
            for (int i = 0; i < this.dash.length; i++) {
                int j = i >= dash.length ? i - dash.length : i;
                this.dash[i] = dash[j] <= 0 ? 0.01f : dash[j];
                this.dashPhaseLimit += dash[j];
            }
        }
        if (dashPhase >= 0) {
            this.dashPhase = dashPhase % dashPhaseLimit;
        } else {
            this.dashPhase = dashPhaseLimit + (dashPhase % dashPhaseLimit);
        }

        float sx, sy;
        float[] data = new float[6];
        while (!it.isDone()) {
            switch (it.currentSegment(data)) {
                case PathIterator.SEG_MOVETO:
                    moveTo(data[0], data[1]);
                    px = data[0];
                    py = data[1];
                    break;
                case PathIterator.SEG_LINETO:
                    lineTo(px, py, data[0], data[1]);
                    px = data[0];
                    py = data[1];
                    break;
                case PathIterator.SEG_QUADTO:
                    quadTo(px, py, data[0], data[1], data[2], data[3]);
                    px = data[2];
                    py = data[3];
                    break;
                case PathIterator.SEG_CUBICTO:
                    cubicTo(px, py, data[0], data[1], data[2], data[3], data[2], data[3]);
                    px = data[4];
                    py = data[5];
                    break;
                case PathIterator.SEG_CLOSE:
                    close();
                    px = py = 0;
                    break;
            }
            it.next();
        }
    }

    public void moveTo(float x, float y) {
        phase = this.dashPhase;
        phaseFill = true;
        phaseNext = phase;
        phaseIndex = 0;
        for (int i = 0; i < this.dash.length; i++) {
            if (phaseNext > this.dash[i]) {
                phaseNext -= this.dash[i];
                phaseIndex ++;
                phaseFill = !phaseFill;
            } else {
                phaseNext = this.dash[i] - phaseNext;
                break;
            }
        }
        open = false;
    }

    public void lineTo(float x, float y, float x2, float y2) {
        double vx = x2 - x;
        double vy = y2 - y;
        double len = Math.sqrt(vx * vx + vy * vy);
        if (len < 0.001) {
            if (!open) path.moveTo(x, y);
            path.lineTo(x2, y2);
            open = true;
            return;
        }
        vx /= len;
        vy /= len;

        px = x;
        py = y;
        double cLen = 0;
        while (len > phaseNext) {
            cLen += phaseNext;
            float nx = (float) (x + vx * cLen);
            float ny = (float) (y + vy * cLen);
            if (phaseFill) {
                if (!open) path.moveTo(px, py);
                path.lineTo(nx, ny);
                path.moveTo(nx, ny);
                open = false;
            }
            px = nx;
            py = ny;
            len -= phaseNext;

            if (++phaseIndex >= dash.length) phaseIndex = 0;
            phaseNext = dash[phaseIndex];
            phaseFill = !phaseFill;
        }
        if (len > 0.001) {
            phaseNext -= len;
            if (phaseFill) {
                if (!open) path.moveTo(px, py);
                path.lineTo(x2, y2);
                open = true;
            }
            px = x2;
            py = y2;
        }
    }

    public void quadTo(float x, float y, float cx, float cy, float x2, float y2) {

    }

    public void cubicTo(float x, float y, float c1x, float c1y, float c2x, float c2y, float x2, float y2) {

    }

    public void close() {
        if (open) path.closePath();
    }

    static abstract class DashIterator {

        static final double FLATNESS = 1.0;

        static class Line extends DashIterator {

            Line(double len) {
                length = len;
            }

            @Override
            double getNext(double dashPos) {
                return dashPos / length;
            }

        }

        static class Quad extends DashIterator {

            int valSize;
            int valPos;
            double curLen;
            double prevLen;
            double lastLen;
            double[] values;
            double step;

            Quad(double x1, double y1, double x2, double y2, double x3, double y3) {

                double nx = x1 + x3 - x2 - x2;
                double ny = y1 + y3 - y2 - y2;

                int n = (int)(1 + Math.sqrt(0.75 * (Math.abs(nx) + Math.abs(ny)) * FLATNESS));
                step = 1.0 / n;

                double ax = x1 + x3 - x2 - x2;
                double ay = y1 + y3 - y2 - y2;
                double bx = 2.0 * (x2 - x1);
                double by = 2.0 * (y2 - y1);

                double dx1 = step * (step * ax + bx);
                double dy1 = step * (step * ay + by);
                double dx2 = step * (step * ax * 2.0);
                double dy2 = step * (step * ay * 2.0);
                double vx = x1;
                double vy = y1;

                valSize = n;
                values = new double[valSize];
                double pvx = vx;
                double pvy = vy;
                length = 0.0;
                for(int i = 0; i < n; i++) {
                    vx += dx1;
                    vy += dy1;
                    dx1 += dx2;
                    dy1 += dy2;
                    double lx = vx - pvx;
                    double ly = vy - pvy;
                    values[i] = Math.sqrt(lx * lx + ly * ly);
                    length += values[i];
                    pvx = vx;
                    pvy = vy;
                }

                valPos = 0;
                curLen = 0.0;
                prevLen = 0.0;
            }

            @Override
            double getNext(double dashPos) {
                double t = 2.0;
                while (curLen <= dashPos && valPos < valSize) {
                    prevLen = curLen;
                    curLen += lastLen = values[valPos++];
                }
                if (curLen > dashPos) {
                    t = (valPos - 1 + (dashPos - prevLen) / lastLen) * step;
                }
                return t;
            }

        }

        static class Cubic extends DashIterator {

            int valSize;
            int valPos;
            double curLen;
            double prevLen;
            double lastLen;
            double[] values;
            double step;

            Cubic(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {

                double nx1 = x1 + x3 - x2 - x2;
                double ny1 = y1 + y3 - y2 - y2;
                double nx2 = x2 + x4 - x3 - x3;
                double ny2 = y2 + y4 - y3 - y3;

                double max = Math.max(Math.abs(nx1) + Math.abs(ny1), Math.abs(nx2) + Math.abs(ny2));
                int n = (int)(1 + Math.sqrt(0.75 * max) * FLATNESS);
                step = 1.0 / n;

                double ax = x4 - x1 + 3.0 * (x2 - x3);
                double ay = y4 - y1 + 3.0 * (y2 - y3);
                double bx = 3.0 * (x1 + x3 - x2 - x2);
                double by = 3.0 * (y1 + y3 - y2 - y2);
                double cx = 3.0 * (x2 - x1);
                double cy = 3.0 * (y2 - y1);

                double dx1 = step * (step * (step * ax + bx) + cx);
                double dy1 = step * (step * (step * ay + by) + cy);
                double dx2 = step * (step * (step * ax * 6.0 + bx * 2.0));
                double dy2 = step * (step * (step * ay * 6.0 + by * 2.0));
                double dx3 = step * (step * (step * ax * 6.0));
                double dy3 = step * (step * (step * ay * 6.0));
                double vx = x1;
                double vy = y1;

                valSize = n;
                values = new double[valSize];
                double pvx = vx;
                double pvy = vy;
                length = 0.0;
                for(int i = 0; i < n; i++) {
                    vx += dx1;
                    vy += dy1;
                    dx1 += dx2;
                    dy1 += dy2;
                    dx2 += dx3;
                    dy2 += dy3;
                    double lx = vx - pvx;
                    double ly = vy - pvy;
                    values[i] = Math.sqrt(lx * lx + ly * ly);
                    length += values[i];
                    pvx = vx;
                    pvy = vy;
                }

                valPos = 0;
                curLen = 0.0;
                prevLen = 0.0;
            }

            @Override
            double getNext(double dashPos) {
                double t = 2.0;
                while (curLen <= dashPos && valPos < valSize) {
                    prevLen = curLen;
                    curLen += lastLen = values[valPos++];
                }
                if (curLen > dashPos) {
                    t = (valPos - 1 + (dashPos - prevLen) / lastLen) * step;
                }
                return t;
            }

        }

        double length;

        abstract double getNext(double dashPos);

    }
}
