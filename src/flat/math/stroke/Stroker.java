package flat.math.stroke;

import flat.graphics.context.enuns.LineCap;
import flat.graphics.context.enuns.LineJoin;
import flat.math.Mathf;
import flat.math.PathList;
import flat.math.Vector2;
import flat.math.shapes.Path;
import flat.math.shapes.PathIterator;
import flat.math.shapes.Shape;

public class Stroker {
    private static int[] pointShift = {
            2,  // MOVETO
            2,  // LINETO
            4,  // QUADTO
            6,  // CUBICTO
            0   // CLOSE
    };

    private LineCap cap;
    private LineJoin join;
    private float miterLimit;
    private float width = 20;

    private float innerWeight = 20;
    private float sx, sy;
    private float px, py;
    private boolean closed;
    private PathList pathList = new PathList();

    public Stroker() {
        this(1);
    }

    public Stroker(float width) {
        this(width, LineCap.BUTT, LineJoin.MITER, 1.0f);
    }

    public Stroker(float width, LineCap cap, LineJoin join, float miterLimit) {
        setWidth(width);
        setCap(cap);
        setJoin(join);
        setMiterLimit(miterLimit);
    }

    public LineCap getCap() {
        return cap;
    }

    public void setCap(LineCap cap) {
        this.cap = cap;
    }

    public LineJoin getJoin() {
        return join;
    }

    public void setJoin(LineJoin join) {
        this.join = join;
    }

    public float getMiterLimit() {
        return miterLimit;
    }

    public void setMiterLimit(float miterLimit) {
        this.miterLimit = miterLimit;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public Path getStrokedShape(Shape shape) {
        innerWeight = Math.max(Math.abs(width), 0.00001f);
        closed = false;
        pathList.clear();
        Path path = new Path(Path.WIND_NON_ZERO);
        strokeIterator(path, shape.pathIterator(null));
        path.reverse();

        closed = false;
        pathList.clear();
        innerWeight = -innerWeight;
        strokeIterator(path, shape.pathIterator(null));
        return path;
    }

    private void strokeIterator(Path path, PathIterator pi) {
        float[] coords = new float[6];
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    moveTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    lineTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    cubicTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    close();
                    break;
            }
            pi.next();
        }

        float[] back = new float[8];
        float[] line = new float[8];
        float[] result = new float[2];

        // Join Loop
        for (int i = 2; i < pathList.size() + (closed ? 1 : 0); i++) {
            final int iVal = i == pathList.size() ? 0 : i;
            pathList.read(i - 2, back);
            int bType = pathList.read(i - 1, back, 2);
            pathList.read(iVal, line);
            int lType = pathList.read(iVal + 1, line, 2);
            i++;

            if (Mathf.isEqual(back[pointShift[bType]], line[0])
                    && Mathf.isEqual(back[pointShift[bType] + 1], line[1])) {
                pathList.remove(iVal);
                i--;
            } else {
                if (join == LineJoin.BEVEL) {           // Simple link
                    if (iVal != 0) pathList.setAsLineTo(iVal, line[0], line[1]);
                } else if (join == LineJoin.MITER) {   // Add mitter point
                    // line line > collision point
                    // curve - use last or first normal point
                    // set BACK to COLISIOPOINT
                    // remove LINE (iVal)
                } else {
                    // ROUND
                    // *** pathList.setAsCubicTo(iVal, line[0], line[1]); IF 0 add move to !!!!
                }
            }
        }

        float[] data  = new float[6];
        for (int i = 0; i < pathList.size(); i++) {
            int type = pathList.read(i, data);
            if (Float.isNaN(data[0])) data[0] = 0;
            if (Float.isNaN(data[1])) data[1] = 0;
            if (Float.isNaN(data[2])) data[2] = 0;
            if (Float.isNaN(data[3])) data[3] = 0;
            if (Float.isNaN(data[4])) data[4] = 0;
            if (Float.isNaN(data[5])) data[5] = 0;
            switch (type) {
                case PathList.MOVE :
                    path.moveTo(data[0], data[1]);
                    break;
                case PathList.LINE :
                    path.lineTo(data[0], data[1]);
                    break;
                case PathList.QUAD :
                    path.quadTo(data[0],data[1],data[2],data[3]);
                    break;
                case PathList.CUBIC :
                    path.curveTo(data[0],data[1],data[2],data[3],data[4],data[5]);
                    break;
                case PathList.CLOSE :
                    path.closePath();
                    break;
            }
        }
        path.closePath();
    }

    private void moveTo(float x, float y) {
        px = sx = x;
        py = sy = y;
        closed = false;
    }

    private void lineTo(float x, float y) {
        if (x == px && y == py) return;

        // Normal by weight
        Vector2 n = new Vector2(y - py, (px - x)).length(innerWeight);

        // Line
        float x1 = px + n.x;
        float y1 = py + n.y;
        float x2 = x + n.x;
        float y2 = y + n.y;
        pathList.moveTo(x1, y1);
        pathList.lineTo(x2, y2);

        px = x;
        py = y;
    }

    private void quadTo(float cx1, float cy1, float x2, float y2) {
        QuadCurve quadCurve = new QuadCurve(px, py, cx1, cy1, x2, y2);
        QuadCurve[] scaled = quadCurve.offset(-innerWeight);
        for (QuadCurve curve : scaled) {
            pathList.moveTo(curve.x1, curve.y1);
            pathList.quadTo(curve.cx1, curve.cy1, curve.x2, curve.y2);
        }
        px = x2;
        py = y2;
    }

    private void cubicTo(float cx1, float cy1, float cx2, float cy2,float x2, float y2) {
        CubicCurve cubicCurve = new CubicCurve(px, py, cx1, cy1, cx2, cy2, x2, y2);
        CubicCurve[] scaled = cubicCurve.offset(-innerWeight);
        for (CubicCurve curve : scaled) {
            pathList.moveTo(curve.x1, curve.y1);
            pathList.cubicTo(curve.cx1, curve.cy1, curve.cx2, curve.cy2, curve.x2, curve.y2);
            if (Float.isNaN(curve.cx1) ||
                    Float.isNaN(curve.cy1) ||
                    Float.isNaN(curve.cx2) ||
                    Float.isNaN(curve.cy2) ||
                    Float.isNaN(curve.x2) ||
                    Float.isNaN(curve.y2) || Float.isNaN(curve.x1) || Float.isNaN(curve.y1)) {
                System.out.println("here");
            }
        }
        px = x2;
        py = y2;
    }

    private void close() {
        if (!closed) {
            lineTo(sx, sy);
        }
        closed = true;
    }
}
