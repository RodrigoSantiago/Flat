package flat.math.shapes;

import flat.math.Affine;
import flat.math.Mathf;
import flat.math.Vector2;
import flat.math.operations.Area;
import flat.math.util.FlatteningPathIterator;
import flat.math.util.IllegalPathStateException;

import java.util.NoSuchElementException;

/**
 * Represents a path constructed from lines and curves and which can contain subpaths.
 */
public class Path implements PathConsumer, Shape, Cloneable {

    /**
     * Specifies the even/odd rule for determining the interior of a path.
     */
    public static final int WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD;

    /**
     * Specifies the non-zero rule for determining the interior of a path.
     */
    public static final int WIND_NON_ZERO = PathIterator.WIND_NON_ZERO;

    public Path() {
        this(WIND_NON_ZERO, BUFFER_SIZE);
    }

    public Path(int rule) {
        this(rule, BUFFER_SIZE);
    }

    public Path(int rule, int initialCapacity) {
        setWindingRule(rule);
        types = new byte[initialCapacity];
        points = new float[initialCapacity * 2];
    }

    public Path(Shape shape) {
        this(WIND_NON_ZERO, BUFFER_SIZE);
        PathIterator p = shape.pathIterator(null);
        setWindingRule(p.windingRule());
        append(p, false);
    }

    public Path(PathIterator iterator) {
        this(WIND_NON_ZERO, BUFFER_SIZE);
        setWindingRule(iterator.windingRule());
        append(iterator, false);
    }

    @Override
    public Path clone() {
        return new Path(rule, types.clone(), points.clone(), typeSize, pointSize);
    }

    public void setWindingRule(int rule) {
        if (rule != WIND_EVEN_ODD && rule != WIND_NON_ZERO) {
            throw new IllegalArgumentException("Invalid winding rule value");
        }
        this.rule = rule;
    }

    public int windingRule() {
        return rule;
    }

    public void moveTo(float x, float y) {
        optimized = false;

        if (typeSize > 0 && types[typeSize - 1] == PathIterator.SEG_MOVETO) {
            points[pointSize - 2] = x;
            points[pointSize - 1] = y;
        } else {
            checkBuf(2, false);
            types[typeSize++] = PathIterator.SEG_MOVETO;
            points[pointSize++] = x;
            points[pointSize++] = y;
        }
    }

    public void lineTo(float x, float y) {
        optimized = false;

        checkBuf(2, true);
        types[typeSize++] = PathIterator.SEG_LINETO;
        points[pointSize++] = x;
        points[pointSize++] = y;
    }

    public void quadTo(float x1, float y1, float x2, float y2) {
        optimized = false;

        checkBuf(4, true);
        types[typeSize++] = PathIterator.SEG_QUADTO;
        points[pointSize++] = x1;
        points[pointSize++] = y1;
        points[pointSize++] = x2;
        points[pointSize++] = y2;
    }

    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
        optimized = false;

        checkBuf(6, true);
        types[typeSize++] = PathIterator.SEG_CUBICTO;
        points[pointSize++] = x1;
        points[pointSize++] = y1;
        points[pointSize++] = x2;
        points[pointSize++] = y2;
        points[pointSize++] = x3;
        points[pointSize++] = y3;
    }

    public void arcTo(float rx, float ry, float xAxisRotation, int largeArcFlag, int sweepFlag, float cx, float cy) {
        optimized = false;

        if (typeSize == 0) {
            throw new IllegalPathStateException("First segment must be a SEG_MOVETO");
        }
        float px, py;
        int j = pointSize - 2;
        if (types[typeSize - 1] == PathIterator.SEG_CLOSE) {
            for (int i = typeSize - 2; i > 0; i--) {
                int type = types[i];
                if (type == PathIterator.SEG_MOVETO) {
                    break;
                }
                j -= pointShift[type];
            }
        }
        px = points[j];
        py = points[j + 1];

        //-------------------------------------------
        //              Init
        //-------------------------------------------
        if (rx == 0 || ry == 0) {
            return;
        }

        final float sinphi = Mathf.sin(xAxisRotation * Mathf.TAU / 360f);
        final float cosphi = Mathf.cos(xAxisRotation * Mathf.TAU / 360f);

        final float pxp = cosphi * (px - cx) / 2f + sinphi * (py - cy) / 2f;
        final float pyp = -sinphi * (px - cx) / 2f + cosphi * (py - cy) / 2f;

        if (pxp == 0 && pyp == 0) {
            return;
        }

        rx = Math.abs(rx);
        ry = Math.abs(ry);

        final float lambda = Mathf.pow(pxp, 2f) / Mathf.pow(rx, 2f) + Mathf.pow(pyp, 2f) / Mathf.pow(ry, 2f);

        if (lambda > 1) {
            rx *= Math.sqrt(lambda);
            ry *= Math.sqrt(lambda);
        }

        //-------------------------------------------
        //              Center Angles
        //-------------------------------------------
        final float rxsq = Mathf.pow(rx, 2);
        final float rysq = Mathf.pow(ry, 2);
        final float pxpsq = Mathf.pow(pxp, 2);
        final float pypsq = Mathf.pow(pyp, 2);

        float radicant = (rxsq * rysq) - (rxsq * pypsq) - (rysq * pxpsq);

        if (radicant < 0) {
            radicant = 0;
        }

        radicant /= (rxsq * pypsq) + (rysq * pxpsq);
        radicant = Mathf.sqrt(radicant) * (largeArcFlag == sweepFlag ? -1 : 1);

        final float centerxp = radicant * rx / ry * pyp;
        final float centeryp = radicant * -ry / rx * pxp;

        final float centerx = cosphi * centerxp - sinphi * centeryp + (px + cx) / 2f;
        final float centery = sinphi * centerxp + cosphi * centeryp + (py + cy) / 2f;

        final float vx1 = (pxp - centerxp) / rx;
        final float vy1 = (pyp - centeryp) / ry;
        final float vx2 = (-pxp - centerxp) / rx;
        final float vy2 = (-pyp - centeryp) / ry;

        float ang1 = angle(1, 0, vx1, vy1);
        float ang2 = angle(vx1, vy1, vx2, vy2);

        if (sweepFlag == 0 && ang2 > 0) {
            ang2 -= Mathf.TAU;
        }

        if (sweepFlag == 1 && ang2 < 0) {
            ang2 += Mathf.TAU;
        }
        //-------------------------------------------
        //              Segments
        //-------------------------------------------
        final float segments = Math.max(Mathf.ceil(Math.abs(ang2) / (Mathf.TAU / 4f)), 1f);

        ang2 /= segments;

        final float a = 4f / 3f * Mathf.tan(ang2 / 4f);
        for (int i = 0; i < segments; i++) {
            final float x1 = Mathf.cos(ang1);
            final float y1 = Mathf.sin(ang1);
            final float x2 = Mathf.cos(ang1 + ang2);
            final float y2 = Mathf.sin(ang1 + ang2);

            float xp = (x1 - y1 * a) * rx;
            float yp = (y1 + x1 * a) * ry;
            final float p1x = (cosphi * xp - sinphi * yp) + centerx;
            final float p1y = (sinphi * xp + cosphi * yp) + centery;

            xp = (x2 + y2 * a) * rx;
            yp = (y2 - x2 * a) * ry;
            final float p2x = (cosphi * xp - sinphi * yp) + centerx;
            final float p2y = (sinphi * xp + cosphi * yp) + centery;

            xp = x2 * rx;
            yp = y2 * ry;
            final float p3x = (cosphi * xp - sinphi * yp) + centerx;
            final float p3y = (sinphi * xp + cosphi * yp) + centery;

            curveTo(p1x, p1y, p2x, p2y, p3x, p3y);
            ang1 += ang2;
        }
    }

    private float angle(float ux, float uy, float vx, float vy) {
        final float sign = (ux * vy - uy * vx < 0) ? -1 : 1;
        final float umag = Mathf.sqrt(ux * ux + uy * uy);
        final float vmag = Mathf.sqrt(ux * ux + uy * uy);
        final float dot = ux * vx + uy * vy;

        float div = dot / (umag * vmag);

        if (div > 1) {
            div = 1;
        } else if (div < -1) {
            div = -1;
        }
        return sign * Mathf.acos(div);
    }

    public void closePath() {
        if (typeSize > 0 && types[typeSize - 1] == PathIterator.SEG_MOVETO) {
            return;
        }
        if (typeSize == 0 || types[typeSize - 1] != PathIterator.SEG_CLOSE) {
            checkBuf(0, true);
            types[typeSize++] = PathIterator.SEG_CLOSE;
        }
    }

    public void pathDone() {

    }

    public void append(Shape shape, boolean connect) {
        PathIterator p = shape.pathIterator(null);
        append(p, connect);
    }

    public void append(PathIterator path, boolean connect) {
        while (!path.isDone()) {
            float[] coords = new float[6];
            switch (path.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    if (!connect || typeSize == 0) {
                        moveTo(coords[0], coords[1]);
                    } else if (types[typeSize - 1] != PathIterator.SEG_CLOSE &&
                            points[pointSize - 2] == coords[0] &&
                            points[pointSize - 1] == coords[1]) {
                        // we're already here
                    } else {
                        lineTo(coords[0], coords[1]);
                    }
                    break;
                case PathIterator.SEG_LINETO:
                    lineTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    closePath();
                    break;
            }
            path.next();
            connect = false;
        }
    }

    public void reset() {
        optimized = false;

        typeSize = 0;
        pointSize = 0;
    }

    public void reverse() {
        optimized = false;

        for (int i = 0, len = pointSize / 2; i < len; i++) {
            final int index = i;
            final float x = points[i];
            final float y = points[++i];
            points[index] = points[pointSize - index - 2];
            points[index + 1] = points[pointSize- index - 1];
            points[pointSize - index - 2] = x;
            points[pointSize - index - 1] = y;
        }
        for (int i = 0; i < typeSize / 2; i++) {
            final byte type = types[i];
            final byte type2 = types[typeSize - i - 1];
            types[i] = (type2 == PathIterator.SEG_MOVETO ? PathIterator.SEG_CLOSE :
                    type2 == PathIterator.SEG_CLOSE ? PathIterator.SEG_MOVETO : type2);
            types[typeSize - i - 1] = (type == PathIterator.SEG_MOVETO ? PathIterator.SEG_CLOSE :
                    type == PathIterator.SEG_CLOSE ? PathIterator.SEG_MOVETO : type);
        }
    }

    public int length() {
        return pointSize / 2;
    }

    public void transform(Affine t) {
        optimized = false;

        t.transform(points, 0, points, 0, pointSize / 2);
    }

    public Shape createTransformedShape(Affine t) {
        Path p = clone();
        if (t != null) {
            p.transform(t);
        }
        return p;
    }

    public Vector2 currentPoint() {
        if (typeSize == 0) {
            return null;
        }
        int j = pointSize - 2;
        if (types[typeSize - 1] == PathIterator.SEG_CLOSE) {
            for (int i = typeSize - 2; i > 0; i--) {
                int type = types[i];
                if (type == PathIterator.SEG_MOVETO) {
                    break;
                }
                j -= pointShift[type];
            }
        }
        return new Vector2(points[j], points[j + 1]);
    }

    public void optimize() {
        if (!optimized) {
            Area area = new Area(this);
            reset();
            append(area, false);
            optimized = true;
        }
    }

    @Override
    public Rectangle bounds() {
        float rx1, ry1, rx2, ry2;
        if (pointSize == 0) {
            rx1 = ry1 = rx2 = ry2 = 0f;
        } else {
            int i = pointSize - 1;
            ry1 = ry2 = points[i--];
            rx1 = rx2 = points[i--];
            while (i > 0) {
                float y = points[i--];
                float x = points[i--];
                if (x < rx1) {
                    rx1 = x;
                } else if (x > rx2) {
                    rx2 = x;
                }
                if (y < ry1) {
                    ry1 = y;
                } else if (y > ry2) {
                    ry2 = y;
                }
            }
        }
        return new Rectangle(rx1, ry1, rx2 - rx1, ry2 - ry1);
    }

    @Override
    public boolean isEmpty() {
        float rx1, ry1, rx2, ry2;
        if (pointSize == 0) {
            rx1 = ry1 = rx2 = ry2 = 0f;
        } else {
            int i = pointSize - 1;
            ry1 = ry2 = points[i--];
            rx1 = rx2 = points[i--];
            while (i > 0) {
                float y = points[i--];
                float x = points[i--];
                if (x < rx1) {
                    rx1 = x;
                } else if (x > rx2) {
                    rx2 = x;
                }
                if (y < ry1) {
                    ry1 = y;
                } else if (y > ry2) {
                    ry2 = y;
                }
            }
        }
        return rx2 - rx1 <= 0 || ry2 - ry1 <= 0;
    }

    @Override
    public boolean contains(float px, float py) {
        return isInside(Crossing.crossShape(this, px, py));
    }

    @Override
    public boolean contains(float rx, float ry, float rw, float rh) {
        int cross = Crossing.intersectShape(this, rx, ry, rw, rh);
        return cross != Crossing.CROSSING && isInside(cross);
    }

    @Override
    public boolean intersects(float rx, float ry, float rw, float rh) {
        int cross = Crossing.intersectShape(this, rx, ry, rw, rh);
        return cross == Crossing.CROSSING || isInside(cross);
    }

    @Override
    public boolean contains(Vector2 p) {
        return contains(p.x, p.y);
    }

    @Override
    public boolean contains(Rectangle rectangle) {
        return contains(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public boolean intersects(Rectangle rectangle) {
        return intersects(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public PathIterator pathIterator(Affine t) {
        return new Iterator(this, t);
    }

    @Override
    public PathIterator pathIterator(Affine t, float flatness) {
        return new FlatteningPathIterator(pathIterator(t), flatness);
    }

    /**
     * Checks points and types buffer size to add pointCount points. If necessary realloc buffers
     * to enlarge size.
     *
     * @param pointCount the point count to be added in buffer
     */
    protected void checkBuf(int pointCount, boolean checkMove) {
        if (checkMove && typeSize == 0) {
            throw new IllegalPathStateException("First segment must be a SEG_MOVETO");
        }
        if (typeSize == types.length) {
            byte[] tmp = new byte[typeSize + BUFFER_CAPACITY];
            System.arraycopy(types, 0, tmp, 0, typeSize);
            types = tmp;
        }
        if (pointSize + pointCount > points.length) {
            float[] tmp = new float[pointSize + Math.max(BUFFER_CAPACITY * 2, pointCount)];
            System.arraycopy(points, 0, tmp, 0, pointSize);
            points = tmp;
        }
    }

    /**
     * Checks cross count according to path rule to define is it point inside shape or not.
     *
     * @param cross the point cross count.
     * @return true if point is inside path, or false otherwise.
     */
    protected boolean isInside(int cross) {
        return (rule == WIND_NON_ZERO) ? Crossing.isInsideNonZero(cross) :
                Crossing.isInsideEvenOdd(cross);
    }

    private Path(int rule, byte[] types, float[] points, int typeSize, int pointSize) {
        this.rule = rule;
        this.types = types;
        this.points = points;
        this.typeSize = typeSize;
        this.pointSize = pointSize;
    }

    /**
     * An iterator over a {@link Path}.
     */
    protected static class Iterator implements PathIterator {
        private int typeIndex;
        private int pointIndex;
        private Path p;
        private Affine t;

        Iterator(Path path) {
            this(path, null);
        }

        Iterator(Path path, Affine at) {
            this.p = path;
            this.t = at;
        }

        @Override
        public int windingRule() {
            return p.windingRule();
        }

        @Override
        public boolean isDone() {
            return typeIndex >= p.typeSize;
        }

        @Override
        public void next() {
            typeIndex++;
        }

        @Override
        public int currentSegment(float[] coords) {
            if (isDone()) {
                throw new NoSuchElementException("Iterator out of bounds");
            }
            int type = p.types[typeIndex];
            int count = Path.pointShift[type];
            System.arraycopy(p.points, pointIndex, coords, 0, count);
            if (t != null) {
                t.transform(coords, 0, coords, 0, count / 2);
            }
            pointIndex += count;
            return type;
        }
    }

    /**
     * The point's types buffer.
     */
    protected byte[] types;

    /**
     * The points buffer.
     */
    protected float[] points;

    /**
     * The point's type buffer size.
     */
    protected int typeSize;

    /**
     * The points buffer size.
     */
    protected int pointSize;

    /* The path rule. */
    protected int rule;

    /* The path rule. */
    protected boolean optimized;

    /**
     * The space required in points buffer for different segment types.
     */
    protected static int[] pointShift = {
            2,  // MOVETO
            2,  // LINETO
            4,  // QUADTO
            6,  // CUBICTO
            0   // CLOSE
    };

    /**
     * The default initial buffer size.
     */
    protected static final int BUFFER_SIZE = 10;

    /**
     * The amount by which to expand the buffer capacity.
     */
    protected static final int BUFFER_CAPACITY = 10;
}