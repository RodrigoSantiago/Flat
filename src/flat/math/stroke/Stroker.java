package flat.math.stroke;

import flat.math.shapes.PathConsumer;

import java.util.Arrays;

public final class Stroker implements PathConsumer {

    private static final int MOVE_TO = 0;
    private static final int DRAWING_OP_TO = 1; // ie. curve, line, or quad
    private static final int CLOSE = 2;

    /**
     * Constant value for join style.
     */
    public static final int JOIN_MITER = 0;

    /**
     * Constant value for join style.
     */
    public static final int JOIN_ROUND = 1;

    /**
     * Constant value for join style.
     */
    public static final int JOIN_BEVEL = 2;

    /**
     * Constant value for end cap style.
     */
    public static final int CAP_BUTT = 0;

    /**
     * Constant value for end cap style.
     */
    public static final int CAP_ROUND = 1;

    /**
     * Constant value for end cap style.
     */
    public static final int CAP_SQUARE = 2;

    private PathConsumer out;

    private int capStyle;
    private int joinStyle;

    private double lineWidth2;

    private final double[][] offset = new double[3][2];
    private final double[] miter = new double[2];
    private double miterLimitSq;

    private int prev;

    // The starting point of the path, and the slope there.
    private double sx0, sy0, sdx, sdy;
    // the current point and the slope there.
    private double cx0, cy0, cdx, cdy; // c stands for current
    // vectors that when added to (sx0,sy0) and (cx0,cy0) respectively yield the
    // first and last points on the left parallel path. Since this path is
    // parallel, it's slope at any point is parallel to the slope of the
    // original path (thought they may have different directions), so these
    // could be computed from sdx,sdy and cdx,cdy (and vice versa), but that
    // would be error prone and hard to read, so we keep these anyway.
    private double smx, smy, cmx, cmy;

    private final PolyStack reverse = new PolyStack();

    /**
     * Constructs a <code>Stroker</code>.
     *
     * @param pc2d an output <code>PathConsumer2D</code>.
     * @param lineWidth the desired line width in pixels
     * @param capStyle the desired end cap style, one of
     * <code>CAP_BUTT</code>, <code>CAP_ROUND</code> or
     * <code>CAP_SQUARE</code>.
     * @param joinStyle the desired line join style, one of
     * <code>JOIN_MITER</code>, <code>JOIN_ROUND</code> or
     * <code>JOIN_BEVEL</code>.
     * @param miterLimit the desired miter limit
     */
    public Stroker(PathConsumer pc2d,
                   double lineWidth,
                   int capStyle,
                   int joinStyle,
                   double miterLimit)
    {
        this(pc2d);

        reset(lineWidth, capStyle, joinStyle, miterLimit);
    }

    public Stroker(PathConsumer pc2d) {
        setConsumer(pc2d);
    }

    public void setConsumer(PathConsumer pc2d) {
        this.out = pc2d;
    }

    public void reset(double lineWidth, int capStyle, int joinStyle,
                      double miterLimit) {
        this.lineWidth2 = lineWidth / 2;
        this.capStyle = capStyle;
        this.joinStyle = joinStyle;

        double limit = miterLimit * lineWidth2;
        this.miterLimitSq = limit*limit;

        this.prev = CLOSE;
    }

    private static void computeOffset(final double lx, final double ly,
                                      final double w, final double[] m)
    {
        final double len = (double)Math.sqrt(lx*lx + ly*ly);
        if (len == 0) {
            m[0] = m[1] = 0;
        } else {
            m[0] = (ly * w)/len;
            m[1] = -(lx * w)/len;
        }
    }

    // Returns true if the vectors (dx1, dy1) and (dx2, dy2) are
    // clockwise (if dx1,dy1 needs to be rotated clockwise to close
    // the smallest angle between it and dx2,dy2).
    // This is equivalent to detecting whether a point q is on the right side
    // of a line passing through points p1, p2 where p2 = p1+(dx1,dy1) and
    // q = p2+(dx2,dy2), which is the same as saying p1, p2, q are in a
    // clockwise order.
    // NOTE: "clockwise" here assumes coordinates with 0,0 at the bottom left.
    private static boolean isCW(final double dx1, final double dy1,
                                final double dx2, final double dy2)
    {
        return dx1 * dy2 <= dy1 * dx2;
    }

    // pisces used to use fixed point arithmetic with 16 decimal digits. I
    // didn't want to change the values of the constant below when I converted
    // it to doubleing point, so that's why the divisions by 2^16 are there.
    private static final double ROUND_JOIN_THRESHOLD = 1000/65536f;

    private void drawRoundJoin(double x, double y,
                               double omx, double omy, double mx, double my,
                               boolean rev,
                               double threshold)
    {
        if ((omx == 0 && omy == 0) || (mx == 0 && my == 0)) {
            return;
        }

        double domx = omx - mx;
        double domy = omy - my;
        double len = domx*domx + domy*domy;
        if (len < threshold) {
            return;
        }

        if (rev) {
            omx = -omx;
            omy = -omy;
            mx = -mx;
            my = -my;
        }
        drawRoundJoin(x, y, omx, omy, mx, my, rev);
    }

    private void drawRoundJoin(double cx, double cy,
                               double omx, double omy,
                               double mx, double my,
                               boolean rev)
    {
        // The sign of the dot product of mx,my and omx,omy is equal to the
        // the sign of the cosine of ext
        // (ext is the angle between omx,omy and mx,my).
        double cosext = omx * mx + omy * my;
        // If it is >=0, we know that abs(ext) is <= 90 degrees, so we only
        // need 1 curve to approximate the circle section that joins omx,omy
        // and mx,my.
        final int numCurves = cosext >= 0 ? 1 : 2;

        switch (numCurves) {
        case 1:
            drawBezApproxForArc(cx, cy, omx, omy, mx, my, rev);
            break;
        case 2:
            // we need to split the arc into 2 arcs spanning the same angle.
            // The point we want will be one of the 2 intersections of the
            // perpendicular bisector of the chord (omx,omy)->(mx,my) and the
            // circle. We could find this by scaling the vector
            // (omx+mx, omy+my)/2 so that it has length=lineWidth2 (and thus lies
            // on the circle), but that can have numerical problems when the angle
            // between omx,omy and mx,my is close to 180 degrees. So we compute a
            // normal of (omx,omy)-(mx,my). This will be the direction of the
            // perpendicular bisector. To get one of the intersections, we just scale
            // this vector that its length is lineWidth2 (this works because the
            // perpendicular bisector goes through the origin). This scaling doesn't
            // have numerical problems because we know that lineWidth2 divided by
            // this normal's length is at least 0.5 and at most sqrt(2)/2 (because
            // we know the angle of the arc is > 90 degrees).
            double nx = my - omy, ny = omx - mx;
            double nlen = (double)Math.sqrt(nx*nx + ny*ny);
            double scale = lineWidth2/nlen;
            double mmx = nx * scale, mmy = ny * scale;

            // if (isCW(omx, omy, mx, my) != isCW(mmx, mmy, mx, my)) then we've
            // computed the wrong intersection so we get the other one.
            // The test above is equivalent to if (rev).
            if (rev) {
                mmx = -mmx;
                mmy = -mmy;
            }
            drawBezApproxForArc(cx, cy, omx, omy, mmx, mmy, rev);
            drawBezApproxForArc(cx, cy, mmx, mmy, mx, my, rev);
            break;
        }
    }

    // the input arc defined by omx,omy and mx,my must span <= 90 degrees.
    private void drawBezApproxForArc(final double cx, final double cy,
                                     final double omx, final double omy,
                                     final double mx, final double my,
                                     boolean rev)
    {
        double cosext2 = (omx * mx + omy * my) / (2 * lineWidth2 * lineWidth2);
        // cv is the length of P1-P0 and P2-P3 divided by the radius of the arc
        // (so, cv assumes the arc has radius 1). P0, P1, P2, P3 are the points that
        // define the bezier curve we're computing.
        // It is computed using the constraints that P1-P0 and P3-P2 are parallel
        // to the arc tangents at the endpoints, and that |P1-P0|=|P3-P2|.
        double cv = (double)((4.0 / 3.0) * Math.sqrt(0.5-cosext2) /
                           (1.0 + Math.sqrt(cosext2+0.5)));
        // if clockwise, we need to negate cv.
        if (rev) { // rev is equivalent to isCW(omx, omy, mx, my)
            cv = -cv;
        }
        final double x1 = cx + omx;
        final double y1 = cy + omy;
        final double x2 = x1 - cv * omy;
        final double y2 = y1 + cv * omx;

        final double x4 = cx + mx;
        final double y4 = cy + my;
        final double x3 = x4 + cv * my;
        final double y3 = y4 - cv * mx;

        emitCurveTo(x1, y1, x2, y2, x3, y3, x4, y4, rev);
    }

    private void drawRoundCap(double cx, double cy, double mx, double my) {
        final double C = 0.5522847498307933f;
        // the first and second arguments of the following two calls
        // are really will be ignored by emitCurveTo (because of the false),
        // but we put them in anyway, as opposed to just giving it 4 zeroes,
        // because it's just 4 additions and it's not good to rely on this
        // sort of assumption (right now it's true, but that may change).
        emitCurveTo(cx+mx,      cy+my,
                    cx+mx-C*my, cy+my+C*mx,
                    cx-my+C*mx, cy+mx+C*my,
                    cx-my,      cy+mx,
                    false);
        emitCurveTo(cx-my,      cy+mx,
                    cx-my-C*mx, cy+mx-C*my,
                    cx-mx-C*my, cy-my+C*mx,
                    cx-mx,      cy-my,
                    false);
    }

    // Return the intersection point of the lines (x0, y0) -> (x1, y1)
    // and (x0p, y0p) -> (x1p, y1p) in m[0] and m[1]
    private void computeMiter(final double x0, final double y0,
                              final double x1, final double y1,
                              final double x0p, final double y0p,
                              final double x1p, final double y1p,
                              final double[] m, int off)
    {
        double x10 = x1 - x0;
        double y10 = y1 - y0;
        double x10p = x1p - x0p;
        double y10p = y1p - y0p;

        // if this is 0, the lines are parallel. If they go in the
        // same direction, there is no intersection so m[off] and
        // m[off+1] will contain infinity, so no miter will be drawn.
        // If they go in the same direction that means that the start of the
        // current segment and the end of the previous segment have the same
        // tangent, in which case this method won't even be involved in
        // miter drawing because it won't be called by drawMiter (because
        // (mx == omx && my == omy) will be true, and drawMiter will return
        // immediately).
        double den = x10*y10p - x10p*y10;
        double t = x10p*(y0-y0p) - y10p*(x0-x0p);
        t /= den;
        m[off++] = x0 + t*x10;
        m[off] = y0 + t*y10;
    }

    // Return the intersection point of the lines (x0, y0) -> (x1, y1)
    // and (x0p, y0p) -> (x1p, y1p) in m[0] and m[1]
    private void safecomputeMiter(final double x0, final double y0,
                                  final double x1, final double y1,
                                  final double x0p, final double y0p,
                                  final double x1p, final double y1p,
                                  final double[] m, int off)
    {
        double x10 = x1 - x0;
        double y10 = y1 - y0;
        double x10p = x1p - x0p;
        double y10p = y1p - y0p;

        // if this is 0, the lines are parallel. If they go in the
        // same direction, there is no intersection so m[off] and
        // m[off+1] will contain infinity, so no miter will be drawn.
        // If they go in the same direction that means that the start of the
        // current segment and the end of the previous segment have the same
        // tangent, in which case this method won't even be involved in
        // miter drawing because it won't be called by drawMiter (because
        // (mx == omx && my == omy) will be true, and drawMiter will return
        // immediately).
        double den = x10*y10p - x10p*y10;
        if (den == 0) {
            m[off++] = (x0 + x0p) / 2.0f;
            m[off] = (y0 + y0p) / 2.0f;
            return;
        }
        double t = x10p*(y0-y0p) - y10p*(x0-x0p);
        t /= den;
        m[off++] = x0 + t*x10;
        m[off] = y0 + t*y10;
    }

    private void drawMiter(final double pdx, final double pdy,
                           final double x0, final double y0,
                           final double dx, final double dy,
                           double omx, double omy, double mx, double my,
                           boolean rev)
    {
        if ((mx == omx && my == omy) ||
            (pdx == 0 && pdy == 0) ||
            (dx == 0 && dy == 0)) {
            return;
        }

        if (rev) {
            omx = -omx;
            omy = -omy;
            mx = -mx;
            my = -my;
        }

        computeMiter((x0 - pdx) + omx, (y0 - pdy) + omy, x0 + omx, y0 + omy,
                     (dx + x0) + mx, (dy + y0) + my, x0 + mx, y0 + my,
                     miter, 0);

        double lenSq = (miter[0]-x0)*(miter[0]-x0) + (miter[1]-y0)*(miter[1]-y0);

        if (lenSq < miterLimitSq) {
            emitLineTo(miter[0], miter[1], rev);
        }
    }

    public void moveTo(float x0, float y0) {
        if (prev == DRAWING_OP_TO) {
            finish();
        }
        this.sx0 = this.cx0 = x0;
        this.sy0 = this.cy0 = y0;
        this.cdx = this.sdx = 1;
        this.cdy = this.sdy = 0;
        this.prev = MOVE_TO;
    }

    public void lineTo(float x1, float y1) {
        double dx = x1 - cx0;
        double dy = y1 - cy0;
        if (dx == 0f && dy == 0f) {
            dx = 1;
        }
        computeOffset(dx, dy, lineWidth2, offset[0]);
        double mx = offset[0][0];
        double my = offset[0][1];

        drawJoin(cdx, cdy, cx0, cy0, dx, dy, cmx, cmy, mx, my);

        emitLineTo(cx0 + mx, cy0 + my);
        emitLineTo(x1 + mx, y1 + my);

        emitLineTo(cx0 - mx, cy0 - my, true);
        emitLineTo(x1 - mx, y1 - my, true);

        this.cmx = mx;
        this.cmy = my;
        this.cdx = dx;
        this.cdy = dy;
        this.cx0 = x1;
        this.cy0 = y1;
        this.prev = DRAWING_OP_TO;
    }

    public void closePath() {
        if (prev != DRAWING_OP_TO) {
            if (prev == CLOSE) {
                return;
            }
            emitMoveTo(cx0, cy0 - lineWidth2);
            this.cmx = this.smx = 0;
            this.cmy = this.smy = -lineWidth2;
            this.cdx = this.sdx = 1;
            this.cdy = this.sdy = 0;
            finish();
            return;
        }

        if (cx0 != sx0 || cy0 != sy0) {
            lineTo((float)sx0, (float)sy0);
        }

        drawJoin(cdx, cdy, cx0, cy0, sdx, sdy, cmx, cmy, smx, smy);

        emitLineTo(sx0 + smx, sy0 + smy);

        emitMoveTo(sx0 - smx, sy0 - smy);
        emitReverse();

        this.prev = CLOSE;
        emitClose();
    }

    private void emitReverse() {
        while(!reverse.isEmpty()) {
            reverse.pop(out);
        }
    }

    public void pathDone() {
        if (prev == DRAWING_OP_TO) {
            finish();
        }

        out.pathDone();
        // this shouldn't matter since this object won't be used
        // after the call to this method.
        this.prev = CLOSE;
    }

    private void finish() {
        if (capStyle == CAP_ROUND) {
            drawRoundCap(cx0, cy0, cmx, cmy);
        } else if (capStyle == CAP_SQUARE) {
            emitLineTo(cx0 - cmy + cmx, cy0 + cmx + cmy);
            emitLineTo(cx0 - cmy - cmx, cy0 + cmx - cmy);
        }

        emitReverse();

        if (capStyle == CAP_ROUND) {
            drawRoundCap(sx0, sy0, -smx, -smy);
        } else if (capStyle == CAP_SQUARE) {
            emitLineTo(sx0 + smy - smx, sy0 - smx - smy);
            emitLineTo(sx0 + smy + smx, sy0 - smx + smy);
        }

        emitClose();
    }

    private void emitMoveTo(final double x0, final double y0) {
        out.moveTo((float)x0, (float)y0);
    }

    private void emitLineTo(final double x1, final double y1) {
        out.lineTo((float)x1, (float)y1);
    }

    private void emitLineTo(final double x1, final double y1,
                            final boolean rev)
    {
        if (rev) {
            reverse.pushLine(x1, y1);
        } else {
            emitLineTo(x1, y1);
        }
    }

    private void emitQuadTo(final double x0, final double y0,
                            final double x1, final double y1,
                            final double x2, final double y2, final boolean rev)
    {
        if (rev) {
            reverse.pushQuad(x0, y0, x1, y1);
        } else {
            out.quadTo((float)x1, (float)y1, (float)x2, (float)y2);
        }
    }

    private void emitCurveTo(final double x0, final double y0,
                             final double x1, final double y1,
                             final double x2, final double y2,
                             final double x3, final double y3, final boolean rev)
    {
        if (rev) {
            reverse.pushCubic(x0, y0, x1, y1, x2, y2);
        } else {
            out.curveTo((float)x1, (float)y1, (float)x2, (float)y2, (float)x3, (float)y3);
        }
    }

    private void emitClose() {
        out.closePath();
    }

    private void drawJoin(double pdx, double pdy,
                          double x0, double y0,
                          double dx, double dy,
                          double omx, double omy,
                          double mx, double my)
    {
        if (prev != DRAWING_OP_TO) {
            emitMoveTo(x0 + mx, y0 + my);
            this.sdx = dx;
            this.sdy = dy;
            this.smx = mx;
            this.smy = my;
        } else {
            boolean cw = isCW(pdx, pdy, dx, dy);
            if (joinStyle == JOIN_MITER) {
                drawMiter(pdx, pdy, x0, y0, dx, dy, omx, omy, mx, my, cw);
            } else if (joinStyle == JOIN_ROUND) {
                drawRoundJoin(x0, y0,
                              omx, omy,
                              mx, my, cw,
                              ROUND_JOIN_THRESHOLD);
            }
            emitLineTo(x0, y0, !cw);
        }
        prev = DRAWING_OP_TO;
    }

    private static boolean within(final double x1, final double y1,
                                  final double x2, final double y2,
                                  final double ERR)
    {
        assert ERR > 0 : "";
        // compare taxicab distance. ERR will always be small, so using
        // true distance won't give much benefit
        return (Helpers.within(x1, x2, ERR) &&  // we want to avoid calling Math.abs
                Helpers.within(y1, y2, ERR)); // this is just as good.
    }

    private void getLineOffsets(double x1, double y1,
                                double x2, double y2,
                                double[] left, double[] right) {
        computeOffset(x2 - x1, y2 - y1, lineWidth2, offset[0]);
        left[0] = x1 + offset[0][0];
        left[1] = y1 + offset[0][1];
        left[2] = x2 + offset[0][0];
        left[3] = y2 + offset[0][1];
        right[0] = x1 - offset[0][0];
        right[1] = y1 - offset[0][1];
        right[2] = x2 - offset[0][0];
        right[3] = y2 - offset[0][1];
    }

    private int computeOffsetCubic(double[] pts, final int off,
                                   double[] leftOff, double[] rightOff)
    {
        // if p1=p2 or p3=p4 it means that the derivative at the endpoint
        // vanishes, which creates problems with computeOffset. Usually
        // this happens when this stroker object is trying to winden
        // a curve with a cusp. What happens is that curveTo splits
        // the input curve at the cusp, and passes it to this function.
        // because of inaccuracies in the splitting, we consider points
        // equal if they're very close to each other.
        final double x1 = pts[off + 0], y1 = pts[off + 1];
        final double x2 = pts[off + 2], y2 = pts[off + 3];
        final double x3 = pts[off + 4], y3 = pts[off + 5];
        final double x4 = pts[off + 6], y4 = pts[off + 7];

        double dx4 = x4 - x3;
        double dy4 = y4 - y3;
        double dx1 = x2 - x1;
        double dy1 = y2 - y1;

        // if p1 == p2 && p3 == p4: draw line from p1->p4, unless p1 == p4,
        // in which case ignore if p1 == p2
        final boolean p1eqp2 = within(x1,y1,x2,y2, 6 * Math.ulp(y2));
        final boolean p3eqp4 = within(x3,y3,x4,y4, 6 * Math.ulp(y4));
        if (p1eqp2 && p3eqp4) {
            getLineOffsets(x1, y1, x4, y4, leftOff, rightOff);
            return 4;
        } else if (p1eqp2) {
            dx1 = x3 - x1;
            dy1 = y3 - y1;
        } else if (p3eqp4) {
            dx4 = x4 - x2;
            dy4 = y4 - y2;
        }

        // if p2-p1 and p4-p3 are parallel, that must mean this curve is a line
        double dotsq = (dx1 * dx4 + dy1 * dy4);
        dotsq = dotsq * dotsq;
        double l1sq = dx1 * dx1 + dy1 * dy1, l4sq = dx4 * dx4 + dy4 * dy4;
        if (Helpers.within(dotsq, l1sq * l4sq, 4 * Math.ulp(dotsq))) {
            getLineOffsets(x1, y1, x4, y4, leftOff, rightOff);
            return 4;
        }

//      What we're trying to do in this function is to approximate an ideal
//      offset curve (call it I) of the input curve B using a bezier curve Bp.
//      The constraints I use to get the equations are:
//
//      1. The computed curve Bp should go through I(0) and I(1). These are
//      x1p, y1p, x4p, y4p, which are p1p and p4p. We still need to find
//      4 variables: the x and y components of p2p and p3p (i.e. x2p, y2p, x3p, y3p).
//
//      2. Bp should have slope equal in absolute value to I at the endpoints. So,
//      (by the way, the operator || in the comments below means "aligned with".
//      It is defined on vectors, so when we say I'(0) || Bp'(0) we mean that
//      vectors I'(0) and Bp'(0) are aligned, which is the same as saying
//      that the tangent lines of I and Bp at 0 are parallel. Mathematically
//      this means (I'(t) || Bp'(t)) <==> (I'(t) = c * Bp'(t)) where c is some
//      nonzero constant.)
//      I'(0) || Bp'(0) and I'(1) || Bp'(1). Obviously, I'(0) || B'(0) and
//      I'(1) || B'(1); therefore, Bp'(0) || B'(0) and Bp'(1) || B'(1).
//      We know that Bp'(0) || (p2p-p1p) and Bp'(1) || (p4p-p3p) and the same
//      is true for any bezier curve; therefore, we get the equations
//          (1) p2p = c1 * (p2-p1) + p1p
//          (2) p3p = c2 * (p4-p3) + p4p
//      We know p1p, p4p, p2, p1, p3, and p4; therefore, this reduces the number
//      of unknowns from 4 to 2 (i.e. just c1 and c2).
//      To eliminate these 2 unknowns we use the following constraint:
//
//      3. Bp(0.5) == I(0.5). Bp(0.5)=(x,y) and I(0.5)=(xi,yi), and I should note
//      that I(0.5) is *the only* reason for computing dxm,dym. This gives us
//          (3) Bp(0.5) = (p1p + 3 * (p2p + p3p) + p4p)/8, which is equivalent to
//          (4) p2p + p3p = (Bp(0.5)*8 - p1p - p4p) / 3
//      We can substitute (1) and (2) from above into (4) and we get:
//          (5) c1*(p2-p1) + c2*(p4-p3) = (Bp(0.5)*8 - p1p - p4p)/3 - p1p - p4p
//      which is equivalent to
//          (6) c1*(p2-p1) + c2*(p4-p3) = (4/3) * (Bp(0.5) * 2 - p1p - p4p)
//
//      The right side of this is a 2D vector, and we know I(0.5), which gives us
//      Bp(0.5), which gives us the value of the right side.
//      The left side is just a matrix vector multiplication in disguise. It is
//
//      [x2-x1, x4-x3][c1]
//      [y2-y1, y4-y3][c2]
//      which, is equal to
//      [dx1, dx4][c1]
//      [dy1, dy4][c2]
//      At this point we are left with a simple linear system and we solve it by
//      getting the inverse of the matrix above. Then we use [c1,c2] to compute
//      p2p and p3p.

        double x = 0.125f * (x1 + 3 * (x2 + x3) + x4);
        double y = 0.125f * (y1 + 3 * (y2 + y3) + y4);
        // (dxm,dym) is some tangent of B at t=0.5. This means it's equal to
        // c*B'(0.5) for some constant c.
        double dxm = x3 + x4 - x1 - x2, dym = y3 + y4 - y1 - y2;

        // this computes the offsets at t=0, 0.5, 1, using the property that
        // for any bezier curve the vectors p2-p1 and p4-p3 are parallel to
        // the (dx/dt, dy/dt) vectors at the endpoints.
        computeOffset(dx1, dy1, lineWidth2, offset[0]);
        computeOffset(dxm, dym, lineWidth2, offset[1]);
        computeOffset(dx4, dy4, lineWidth2, offset[2]);
        double x1p = x1 + offset[0][0]; // start
        double y1p = y1 + offset[0][1]; // point
        double xi  = x + offset[1][0]; // interpolation
        double yi  = y + offset[1][1]; // point
        double x4p = x4 + offset[2][0]; // end
        double y4p = y4 + offset[2][1]; // point

        double invdet43 = 4f / (3f * (dx1 * dy4 - dy1 * dx4));

        double two_pi_m_p1_m_p4x = 2*xi - x1p - x4p;
        double two_pi_m_p1_m_p4y = 2*yi - y1p - y4p;
        double c1 = invdet43 * (dy4 * two_pi_m_p1_m_p4x - dx4 * two_pi_m_p1_m_p4y);
        double c2 = invdet43 * (dx1 * two_pi_m_p1_m_p4y - dy1 * two_pi_m_p1_m_p4x);

        double x2p, y2p, x3p, y3p;
        x2p = x1p + c1*dx1;
        y2p = y1p + c1*dy1;
        x3p = x4p + c2*dx4;
        y3p = y4p + c2*dy4;

        leftOff[0] = x1p; leftOff[1] = y1p;
        leftOff[2] = x2p; leftOff[3] = y2p;
        leftOff[4] = x3p; leftOff[5] = y3p;
        leftOff[6] = x4p; leftOff[7] = y4p;

        x1p = x1 - offset[0][0]; y1p = y1 - offset[0][1];
        xi = xi - 2 * offset[1][0]; yi = yi - 2 * offset[1][1];
        x4p = x4 - offset[2][0]; y4p = y4 - offset[2][1];

        two_pi_m_p1_m_p4x = 2*xi - x1p - x4p;
        two_pi_m_p1_m_p4y = 2*yi - y1p - y4p;
        c1 = invdet43 * (dy4 * two_pi_m_p1_m_p4x - dx4 * two_pi_m_p1_m_p4y);
        c2 = invdet43 * (dx1 * two_pi_m_p1_m_p4y - dy1 * two_pi_m_p1_m_p4x);

        x2p = x1p + c1*dx1;
        y2p = y1p + c1*dy1;
        x3p = x4p + c2*dx4;
        y3p = y4p + c2*dy4;

        rightOff[0] = x1p; rightOff[1] = y1p;
        rightOff[2] = x2p; rightOff[3] = y2p;
        rightOff[4] = x3p; rightOff[5] = y3p;
        rightOff[6] = x4p; rightOff[7] = y4p;
        return 8;
    }

    // compute offset curves using bezier spline through t=0.5 (i.e.
    // ComputedCurve(0.5) == IdealParallelCurve(0.5))
    // return the kind of curve in the right and left arrays.
    private int computeOffsetQuad(double[] pts, final int off,
                                  double[] leftOff, double[] rightOff)
    {
        final double x1 = pts[off + 0], y1 = pts[off + 1];
        final double x2 = pts[off + 2], y2 = pts[off + 3];
        final double x3 = pts[off + 4], y3 = pts[off + 5];

        double dx3 = x3 - x2;
        double dy3 = y3 - y2;
        double dx1 = x2 - x1;
        double dy1 = y2 - y1;

        // if p1=p2 or p3=p4 it means that the derivative at the endpoint
        // vanishes, which creates problems with computeOffset. Usually
        // this happens when this stroker object is trying to winden
        // a curve with a cusp. What happens is that curveTo splits
        // the input curve at the cusp, and passes it to this function.
        // because of inaccuracies in the splitting, we consider points
        // equal if they're very close to each other.

        // if p1 == p2 && p3 == p4: draw line from p1->p4, unless p1 == p4,
        // in which case ignore.
        final boolean p1eqp2 = within(x1,y1,x2,y2, 6 * Math.ulp(y2));
        final boolean p2eqp3 = within(x2,y2,x3,y3, 6 * Math.ulp(y3));
        if (p1eqp2 || p2eqp3) {
            getLineOffsets(x1, y1, x3, y3, leftOff, rightOff);
            return 4;
        }

        // if p2-p1 and p4-p3 are parallel, that must mean this curve is a line
        double dotsq = (dx1 * dx3 + dy1 * dy3);
        dotsq = dotsq * dotsq;
        double l1sq = dx1 * dx1 + dy1 * dy1, l3sq = dx3 * dx3 + dy3 * dy3;
        if (Helpers.within(dotsq, l1sq * l3sq, 4 * Math.ulp(dotsq))) {
            getLineOffsets(x1, y1, x3, y3, leftOff, rightOff);
            return 4;
        }

        // this computes the offsets at t=0, 0.5, 1, using the property that
        // for any bezier curve the vectors p2-p1 and p4-p3 are parallel to
        // the (dx/dt, dy/dt) vectors at the endpoints.
        computeOffset(dx1, dy1, lineWidth2, offset[0]);
        computeOffset(dx3, dy3, lineWidth2, offset[1]);
        double x1p = x1 + offset[0][0]; // start
        double y1p = y1 + offset[0][1]; // point
        double x3p = x3 + offset[1][0]; // end
        double y3p = y3 + offset[1][1]; // point

        safecomputeMiter(x1p, y1p, x1p+dx1, y1p+dy1, x3p, y3p, x3p-dx3, y3p-dy3, leftOff, 2);
        leftOff[0] = x1p; leftOff[1] = y1p;
        leftOff[4] = x3p; leftOff[5] = y3p;
        x1p = x1 - offset[0][0]; y1p = y1 - offset[0][1];
        x3p = x3 - offset[1][0]; y3p = y3 - offset[1][1];
        safecomputeMiter(x1p, y1p, x1p+dx1, y1p+dy1, x3p, y3p, x3p-dx3, y3p-dy3, rightOff, 2);
        rightOff[0] = x1p; rightOff[1] = y1p;
        rightOff[4] = x3p; rightOff[5] = y3p;
        return 6;
    }

    // This is where the curve to be processed is put. We give it
    // enough room to store 2 curves: one for the current subdivision, the
    // other for the rest of the curve.
    private double[] middle = new double[MAX_N_CURVES*8];
    private double[] lp = new double[8];
    private double[] rp = new double[8];
    private static final int MAX_N_CURVES = 11;
    private double[] subdivTs = new double[MAX_N_CURVES - 1];

    // If this class is compiled with ecj, then Hotspot crashes when OSR
    // compiling this function. See bugs 7004570 and 6675699
    // TODO: until those are fixed, we should work around that by
    // manually inlining this into curveTo and quadTo.
/******************************* WORKAROUND **********************************
    private void somethingTo(final int type) {
        // need these so we can update the state at the end of this method
        final double xf = middle[type-2], yf = middle[type-1];
        double dxs = middle[2] - middle[0];
        double dys = middle[3] - middle[1];
        double dxf = middle[type - 2] - middle[type - 4];
        double dyf = middle[type - 1] - middle[type - 3];
        switch(type) {
        case 6:
            if ((dxs == 0f && dys == 0f) ||
                (dxf == 0f && dyf == 0f)) {
               dxs = dxf = middle[4] - middle[0];
               dys = dyf = middle[5] - middle[1];
            }
            break;
        case 8:
            boolean p1eqp2 = (dxs == 0f && dys == 0f);
            boolean p3eqp4 = (dxf == 0f && dyf == 0f);
            if (p1eqp2) {
                dxs = middle[4] - middle[0];
                dys = middle[5] - middle[1];
                if (dxs == 0f && dys == 0f) {
                    dxs = middle[6] - middle[0];
                    dys = middle[7] - middle[1];
                }
            }
            if (p3eqp4) {
                dxf = middle[6] - middle[2];
                dyf = middle[7] - middle[3];
                if (dxf == 0f && dyf == 0f) {
                    dxf = middle[6] - middle[0];
                    dyf = middle[7] - middle[1];
                }
            }
        }
        if (dxs == 0f && dys == 0f) {
            // this happens iff the "curve" is just a point
            lineTo(middle[0], middle[1]);
            return;
        }
        // if these vectors are too small, normalize them, to avoid future
        // precision problems.
        if (Math.abs(dxs) < 0.1f && Math.abs(dys) < 0.1f) {
            double len = (double)Math.sqrt(dxs*dxs + dys*dys);
            dxs /= len;
            dys /= len;
        }
        if (Math.abs(dxf) < 0.1f && Math.abs(dyf) < 0.1f) {
            double len = (double)Math.sqrt(dxf*dxf + dyf*dyf);
            dxf /= len;
            dyf /= len;
        }

        computeOffset(dxs, dys, lineWidth2, offset[0]);
        final double mx = offset[0][0];
        final double my = offset[0][1];
        drawJoin(cdx, cdy, cx0, cy0, dxs, dys, cmx, cmy, mx, my);

        int nSplits = findSubdivPoints(middle, subdivTs, type, lineWidth2);

        int kind = 0;
        Iterator<Integer> it = Curve.breakPtsAtTs(middle, type, subdivTs, nSplits);
        while(it.hasNext()) {
            int curCurveOff = it.next();

            kind = 0;
            switch (type) {
            case 8:
                kind = computeOffsetCubic(middle, curCurveOff, lp, rp);
                break;
            case 6:
                kind = computeOffsetQuad(middle, curCurveOff, lp, rp);
                break;
            }
            if (kind != 0) {
                emitLineTo(lp[0], lp[1]);
                switch(kind) {
                case 8:
                    emitCurveTo(lp[0], lp[1], lp[2], lp[3], lp[4], lp[5], lp[6], lp[7], false);
                    emitCurveTo(rp[0], rp[1], rp[2], rp[3], rp[4], rp[5], rp[6], rp[7], true);
                    break;
                case 6:
                    emitQuadTo(lp[0], lp[1], lp[2], lp[3], lp[4], lp[5], false);
                    emitQuadTo(rp[0], rp[1], rp[2], rp[3], rp[4], rp[5], true);
                    break;
                case 4:
                    emitLineTo(lp[2], lp[3]);
                    emitLineTo(rp[0], rp[1], true);
                    break;
                }
                emitLineTo(rp[kind - 2], rp[kind - 1], true);
            }
        }

        this.cmx = (lp[kind - 2] - rp[kind - 2]) / 2;
        this.cmy = (lp[kind - 1] - rp[kind - 1]) / 2;
        this.cdx = dxf;
        this.cdy = dyf;
        this.cx0 = xf;
        this.cy0 = yf;
        this.prev = DRAWING_OP_TO;
    }
****************************** END WORKAROUND *******************************/

    // finds values of t where the curve in pts should be subdivided in order
    // to get good offset curves a distance of w away from the middle curve.
    // Stores the points in ts, and returns how many of them there were.
    private static Curve c = new Curve();
    private static int findSubdivPoints(double[] pts, double[] ts,
                                        final int type, final double w)
    {
        final double x12 = pts[2] - pts[0];
        final double y12 = pts[3] - pts[1];
        // if the curve is already parallel to either axis we gain nothing
        // from rotating it.
        if (y12 != 0f && x12 != 0f) {
            // we rotate it so that the first vector in the control polygon is
            // parallel to the x-axis. This will ensure that rotated quarter
            // circles won't be subdivided.
            final double hypot = (double)Math.sqrt(x12 * x12 + y12 * y12);
            final double cos = x12 / hypot;
            final double sin = y12 / hypot;
            final double x1 = cos * pts[0] + sin * pts[1];
            final double y1 = cos * pts[1] - sin * pts[0];
            final double x2 = cos * pts[2] + sin * pts[3];
            final double y2 = cos * pts[3] - sin * pts[2];
            final double x3 = cos * pts[4] + sin * pts[5];
            final double y3 = cos * pts[5] - sin * pts[4];
            switch(type) {
            case 8:
                final double x4 = cos * pts[6] + sin * pts[7];
                final double y4 = cos * pts[7] - sin * pts[6];
                c.set((float)x1, (float)y1, (float)x2, (float)y2, (float)x3, (float)y3, (float)x4, (float)y4);
                break;
            case 6:
                c.set((float)x1, (float)y1, (float)x2, (float)y2, (float)x3, (float)y3);
                break;
            }
        } else {
            c.set(pts, type);
        }

        int ret = 0;
        // we subdivide at values of t such that the remaining rotated
        // curves are monotonic in x and y.
        ret += c.dxRoots(ts, ret);
        ret += c.dyRoots(ts, ret);
        // subdivide at inflection points.
        if (type == 8) {
            // quadratic curves can't have inflection points
            ret += c.infPoints(ts, ret);
        }

        // now we must subdivide at points where one of the offset curves will have
        // a cusp. This happens at ts where the radius of curvature is equal to w.
        ret += c.rootsOfROCMinusW(ts, ret, w, 0.0001f);

        ret = Helpers.filterOutNotInAB(ts, 0, ret, 0.0001f, 0.9999f);
        Helpers.isort(ts, 0, ret);
        return ret;
    }

    @Override public void curveTo(float x1, float y1,
                                  float x2, float y2,
                                  float x3, float y3)
    {
        middle[0] = cx0; middle[1] = cy0;
        middle[2] = x1;  middle[3] = y1;
        middle[4] = x2;  middle[5] = y2;
        middle[6] = x3;  middle[7] = y3;

        // inlined version of somethingTo(8);
        // See the TODO on somethingTo
        // (JDK-6675699)

        // need these so we can update the state at the end of this method
        final double xf = middle[6], yf = middle[7];
        double dxs = middle[2] - middle[0];
        double dys = middle[3] - middle[1];
        double dxf = middle[6] - middle[4];
        double dyf = middle[7] - middle[5];

        boolean p1eqp2 = (dxs == 0f && dys == 0f);
        boolean p3eqp4 = (dxf == 0f && dyf == 0f);
        if (p1eqp2) {
            dxs = middle[4] - middle[0];
            dys = middle[5] - middle[1];
            if (dxs == 0f && dys == 0f) {
                dxs = middle[6] - middle[0];
                dys = middle[7] - middle[1];
            }
        }
        if (p3eqp4) {
            dxf = middle[6] - middle[2];
            dyf = middle[7] - middle[3];
            if (dxf == 0f && dyf == 0f) {
                dxf = middle[6] - middle[0];
                dyf = middle[7] - middle[1];
            }
        }
        if (dxs == 0f && dys == 0f) {
            // this happens iff the "curve" is just a point
            lineTo((float)middle[0], (float)middle[1]);
            return;
        }

        // if these vectors are too small, normalize them, to avoid future
        // precision problems.
        if (Math.abs(dxs) < 0.1f && Math.abs(dys) < 0.1f) {
            double len = Math.sqrt(dxs*dxs + dys*dys);
            dxs /= len;
            dys /= len;
        }
        if (Math.abs(dxf) < 0.1f && Math.abs(dyf) < 0.1f) {
            double len = Math.sqrt(dxf*dxf + dyf*dyf);
            dxf /= len;
            dyf /= len;
        }

        computeOffset(dxs, dys, lineWidth2, offset[0]);
        final double mx = offset[0][0];
        final double my = offset[0][1];
        drawJoin(cdx, cdy, cx0, cy0, dxs, dys, cmx, cmy, mx, my);

        int nSplits = findSubdivPoints(middle, subdivTs, 8, lineWidth2);
        double prevT = 0f;
        for (int i = 0; i < nSplits; i++) {
            double t = subdivTs[i];
            Helpers.subdivideCubicAt((t - prevT) / (1 - prevT),
                                     middle, i*6,
                                     middle, i*6,
                                     middle, i*6+6);
            prevT = t;
        }

        int kind = 0;
        for (int i = 0; i <= nSplits; i++) {
            kind = computeOffsetCubic(middle, i*6, lp, rp);
            if (kind != 0) {
                emitLineTo(lp[0], lp[1]);
                switch(kind) {
                case 8:
                    emitCurveTo(lp[0], lp[1], lp[2], lp[3], lp[4], lp[5], lp[6], lp[7], false);
                    emitCurveTo(rp[0], rp[1], rp[2], rp[3], rp[4], rp[5], rp[6], rp[7], true);
                    break;
                case 4:
                    emitLineTo(lp[2], lp[3]);
                    emitLineTo(rp[0], rp[1], true);
                    break;
                }
                emitLineTo(rp[kind - 2], rp[kind - 1], true);
            }
        }

        this.cmx = (lp[kind - 2] - rp[kind - 2]) / 2;
        this.cmy = (lp[kind - 1] - rp[kind - 1]) / 2;
        this.cdx = dxf;
        this.cdy = dyf;
        this.cx0 = xf;
        this.cy0 = yf;
        this.prev = DRAWING_OP_TO;
    }

    @Override public void quadTo(float x1, float y1, float x2, float y2) {
        middle[0] = cx0; middle[1] = cy0;
        middle[2] = x1;  middle[3] = y1;
        middle[4] = x2;  middle[5] = y2;

        // inlined version of somethingTo(8);
        // See the TODO on somethingTo
        // (JDK-6675699)

        // need these so we can update the state at the end of this method
        final double xf = middle[4], yf = middle[5];
        double dxs = middle[2] - middle[0];
        double dys = middle[3] - middle[1];
        double dxf = middle[4] - middle[2];
        double dyf = middle[5] - middle[3];
        if ((dxs == 0f && dys == 0f) || (dxf == 0f && dyf == 0f)) {
            dxs = dxf = middle[4] - middle[0];
            dys = dyf = middle[5] - middle[1];
        }
        if (dxs == 0f && dys == 0f) {
            // this happens iff the "curve" is just a point
            lineTo((float)middle[0], (float)middle[1]);
            return;
        }
        // if these vectors are too small, normalize them, to avoid future
        // precision problems.
        if (Math.abs(dxs) < 0.1f && Math.abs(dys) < 0.1f) {
            double len = Math.sqrt(dxs*dxs + dys*dys);
            dxs /= len;
            dys /= len;
        }
        if (Math.abs(dxf) < 0.1f && Math.abs(dyf) < 0.1f) {
            double len = Math.sqrt(dxf*dxf + dyf*dyf);
            dxf /= len;
            dyf /= len;
        }

        computeOffset(dxs, dys, lineWidth2, offset[0]);
        final double mx = offset[0][0];
        final double my = offset[0][1];
        drawJoin(cdx, cdy, cx0, cy0, dxs, dys, cmx, cmy, mx, my);

        int nSplits = findSubdivPoints(middle, subdivTs, 6, lineWidth2);
        double prevt = 0f;
        for (int i = 0; i < nSplits; i++) {
            double t = subdivTs[i];
            Helpers.subdivideQuadAt((t - prevt) / (1 - prevt),
                                    middle, i*4,
                                    middle, i*4,
                                    middle, i*4+4);
            prevt = t;
        }

        int kind = 0;
        for (int i = 0; i <= nSplits; i++) {
            kind = computeOffsetQuad(middle, i*4, lp, rp);
            if (kind != 0) {
                emitLineTo(lp[0], lp[1]);
                switch(kind) {
                case 6:
                    emitQuadTo(lp[0], lp[1], lp[2], lp[3], lp[4], lp[5], false);
                    emitQuadTo(rp[0], rp[1], rp[2], rp[3], rp[4], rp[5], true);
                    break;
                case 4:
                    emitLineTo(lp[2], lp[3]);
                    emitLineTo(rp[0], rp[1], true);
                    break;
                }
                emitLineTo(rp[kind - 2], rp[kind - 1], true);
            }
        }

        this.cmx = (lp[kind - 2] - rp[kind - 2]) / 2;
        this.cmy = (lp[kind - 1] - rp[kind - 1]) / 2;
        this.cdx = dxf;
        this.cdy = dyf;
        this.cx0 = xf;
        this.cy0 = yf;
        this.prev = DRAWING_OP_TO;
    }

    // a stack of polynomial curves where each curve shares endpoints with
    // adjacent ones.
    private static final class PolyStack {
        double[] curves;
        int end;
        int[] curveTypes;
        int numCurves;

        private static final int INIT_SIZE = 50;

        PolyStack() {
            curves = new double[8 * INIT_SIZE];
            curveTypes = new int[INIT_SIZE];
            end = 0;
            numCurves = 0;
        }

        public boolean isEmpty() {
            return numCurves == 0;
        }

        private void ensureSpace(int n) {
            if (end + n >= curves.length) {
                int newSize = (end + n) * 2;
                curves = Arrays.copyOf(curves, newSize);
            }
            if (numCurves >= curveTypes.length) {
                int newSize = numCurves * 2;
                curveTypes = Arrays.copyOf(curveTypes, newSize);
            }
        }

        public void pushCubic(double x0, double y0,
                              double x1, double y1,
                              double x2, double y2)
        {
            ensureSpace(6);
            curveTypes[numCurves++] = 8;
            // assert(x0 == lastX && y0 == lastY)

            // we reverse the coordinate order to make popping easier
            curves[end++] = x2;    curves[end++] = y2;
            curves[end++] = x1;    curves[end++] = y1;
            curves[end++] = x0;    curves[end++] = y0;
        }

        public void pushQuad(double x0, double y0,
                             double x1, double y1)
        {
            ensureSpace(4);
            curveTypes[numCurves++] = 6;
            // assert(x0 == lastX && y0 == lastY)
            curves[end++] = x1;    curves[end++] = y1;
            curves[end++] = x0;    curves[end++] = y0;
        }

        public void pushLine(double x, double y) {
            ensureSpace(2);
            curveTypes[numCurves++] = 4;
            // assert(x0 == lastX && y0 == lastY)
            curves[end++] = x;    curves[end++] = y;
        }

        @SuppressWarnings("unused")
        public int pop(double[] pts) {
            int ret = curveTypes[numCurves - 1];
            numCurves--;
            end -= (ret - 2);
            System.arraycopy(curves, end, pts, 0, ret - 2);
            return ret;
        }

        public void pop(PathConsumer io) {
            numCurves--;
            int type = curveTypes[numCurves];
            end -= (type - 2);
            switch(type) {
            case 8:
                io.curveTo((float)curves[end+0], (float)curves[end+1],
                        (float)curves[end+2], (float)curves[end+3],
                        (float)curves[end+4], (float)curves[end+5]);
                break;
            case 6:
                io.quadTo((float)curves[end+0], (float)curves[end+1],
                        (float)curves[end+2], (float)curves[end+3]);
                 break;
            case 4:
                io.lineTo((float)curves[end], (float)curves[end+1]);
            }
        }

        @Override
        public String toString() {
            String ret = "";
            int nc = numCurves;
            int last = this.end;
            while (nc > 0) {
                nc--;
                int type = curveTypes[numCurves];
                last -= (type - 2);
                switch(type) {
                case 8:
                    ret += "cubic: ";
                    break;
                case 6:
                    ret += "quad: ";
                    break;
                case 4:
                    ret += "line: ";
                    break;
                }
                ret += Arrays.toString(Arrays.copyOfRange(curves, last, last+type-2)) + "\n";
            }
            return ret;
        }
    }
}
