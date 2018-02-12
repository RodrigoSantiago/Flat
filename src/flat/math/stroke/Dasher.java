package flat.math.stroke;

import flat.math.shapes.PathConsumer;

/**
 * The <code>Dasher</code> class takes a series of linear commands
 * (<code>moveTo</code>, <code>lineTo</code>, <code>close</code> and
 * <code>end</code>) and breaks them into smaller segments according to a
 * dash pattern array and a starting dash phase.
 *
 * <p> Issues: in J2Se, a zero length dash segment as drawn as a very
 * short dash, whereas Pisces does not draw anything.  The PostScript
 * semantics are unclear.
 *
 */
public final class Dasher implements PathConsumer {

    private final PathConsumer out;
    private double[] dash;
    private double startPhase;
    private boolean startDashOn;
    private int startIdx;

    private boolean starting;
    private boolean needsMoveTo;

    private int idx;
    private boolean dashOn;
    private double phase;

    private double sx, sy;
    private double x0, y0;

    // temporary storage for the current curve
    private double[] curCurvepts;

    /**
     * Constructs a <code>Dasher</code>.
     *
     * @param out an output <code>PathConsumer2D</code>.
     * @param dash an array of <code>double</code>s containing the dash pattern
     * @param phase a <code>double</code> containing the dash phase
     */
    public Dasher(PathConsumer out, float[] dash, double phase) {
        this(out);
        reset(dash, phase);
    }

    public Dasher(PathConsumer out) {
        this.out = out;

        // we need curCurvepts to be able to contain 2 curves because when
        // dashing curves, we need to subdivide it
        curCurvepts = new double[8 * 2];
    }

    public void reset(float[] dash, double phase) {
        if (phase < 0) {
            throw new IllegalArgumentException("phase < 0 !");
        }

        // Normalize so 0 <= phase < dash[0]
        int sidx = 0;
        dashOn = true;
        double d;
        while (phase >= (d = dash[sidx])) {
            phase -= d;
            sidx = (sidx + 1) % dash.length;
            dashOn = !dashOn;
        }

        this.dash = new double[dash.length];
        for (int i = 0; i < dash.length; i++) {
            this.dash[i] = dash[i];
        }
        this.startPhase = this.phase = phase;
        this.startDashOn = dashOn;
        this.startIdx = sidx;
        this.starting = true;
    }

    public void moveTo(float x0, float y0) {
        if (firstSegidx > 0) {
            out.moveTo((float)sx, (float)sy);
            emitFirstSegments();
        }
        needsMoveTo = true;
        this.idx = startIdx;
        this.dashOn = this.startDashOn;
        this.phase = this.startPhase;
        this.sx = this.x0 = x0;
        this.sy = this.y0 = y0;
        this.starting = true;
    }

    private void emitSeg(double[] buf, int off, int type) {
        switch (type) {
        case 8:
            out.curveTo((float)buf[off+0], (float)buf[off+1],
                    (float)buf[off+2], (float)buf[off+3],
                    (float)buf[off+4], (float)buf[off+5]);
            break;
        case 6:
            out.quadTo((float)buf[off+0], (float)buf[off+1],
                    (float)buf[off+2], (float)buf[off+3]);
            break;
        case 4:
            out.lineTo((float)buf[off], (float)buf[off+1]);
        }
    }

    private void emitFirstSegments() {
        for (int i = 0; i < firstSegidx; ) {
            emitSeg(firstSegmentsBuffer, i+1, (int)firstSegmentsBuffer[i]);
            i += (((int)firstSegmentsBuffer[i]) - 1);
        }
        firstSegidx = 0;
    }

    // We don't emit the first dash right away. If we did, caps would be
    // drawn on it, but we need joins to be drawn if there's a closePath()
    // So, we store the path elements that make up the first dash in the
    // buffer below.
    private double[] firstSegmentsBuffer = new double[7];
    private int firstSegidx = 0;
    // precondition: pts must be in relative coordinates (relative to x0,y0)
    // fullCurve is true iff the curve in pts has not been split.
    private void goTo(double[] pts, int off, final int type) {
        double x = pts[off + type - 4];
        double y = pts[off + type - 3];
        if (dashOn) {
            if (starting) {
                firstSegmentsBuffer =
                    Helpers.widenArray(firstSegmentsBuffer,
                                       firstSegidx, type - 1);
                firstSegmentsBuffer[firstSegidx++] = type;
                System.arraycopy(pts, off, firstSegmentsBuffer, firstSegidx, type - 2);
                firstSegidx += type - 2;
            } else {
                if (needsMoveTo) {
                    out.moveTo((float)x0, (float)y0);
                    needsMoveTo = false;
                }
                emitSeg(pts, off, type);
            }
        } else {
            starting = false;
            needsMoveTo = true;
        }
        this.x0 = x;
        this.y0 = y;
    }

    public void lineTo(float x1, float y1) {
        double dx = x1 - x0;
        double dy = y1 - y0;

        double len = (double) Math.sqrt(dx*dx + dy*dy);

        if (len == 0) {
            return;
        }

        // The scaling factors needed to get the dx and dy of the
        // transformed dash segments.
        double cx = dx / len;
        double cy = dy / len;

        while (true) {
            double leftInThisDashSegment = dash[idx] - phase;
            if (len <= leftInThisDashSegment) {
                curCurvepts[0] = x1;
                curCurvepts[1] = y1;
                goTo(curCurvepts, 0, 4);
                // Advance phase within current dash segment
                phase += len;
                if (len == leftInThisDashSegment) {
                    phase = 0f;
                    idx = (idx + 1) % dash.length;
                    dashOn = !dashOn;
                }
                return;
            }

            double dashdx = dash[idx] * cx;
            double dashdy = dash[idx] * cy;
            if (phase == 0) {
                curCurvepts[0] = x0 + dashdx;
                curCurvepts[1] = y0 + dashdy;
            } else {
                double p = leftInThisDashSegment / dash[idx];
                curCurvepts[0] = x0 + p * dashdx;
                curCurvepts[1] = y0 + p * dashdy;
            }

            goTo(curCurvepts, 0, 4);

            len -= leftInThisDashSegment;
            // Advance to next dash segment
            idx = (idx + 1) % dash.length;
            dashOn = !dashOn;
            phase = 0;
        }
    }

    private LengthIterator li = null;

    // preconditions: curCurvepts must be an array of length at least 2 * type,
    // that contains the curve we want to dash in the first type elements
    private void somethingTo(int type) {
        if (pointCurve(curCurvepts, type)) {
            return;
        }
        if (li == null) {
            li = new LengthIterator(4, 0.01f);
        }
        li.initializeIterationOnCurve(curCurvepts, type);

        int curCurveoff = 0; // initially the current curve is at curCurvepts[0...type]
        double lastSplitT = 0;
        double t = 0;
        double leftInThisDashSegment = dash[idx] - phase;
        while ((t = li.next(leftInThisDashSegment)) < 1) {
            if (t != 0) {
                Helpers.subdivideAt((t - lastSplitT) / (1 - lastSplitT),
                                    curCurvepts, curCurveoff,
                                    curCurvepts, 0,
                                    curCurvepts, type, type);
                lastSplitT = t;
                goTo(curCurvepts, 2, type);
                curCurveoff = type;
            }
            // Advance to next dash segment
            idx = (idx + 1) % dash.length;
            dashOn = !dashOn;
            phase = 0;
            leftInThisDashSegment = dash[idx];
        }
        goTo(curCurvepts, curCurveoff+2, type);
        phase += li.lastSegLen();
        if (phase >= dash[idx]) {
            phase = 0f;
            idx = (idx + 1) % dash.length;
            dashOn = !dashOn;
        }
    }

    private static boolean pointCurve(double[] curve, int type) {
        for (int i = 2; i < type; i++) {
            if (curve[i] != curve[i-2]) {
                return false;
            }
        }
        return true;
    }

    // Objects of this class are used to iterate through curves. They return
    // t values where the left side of the curve has a specified length.
    // It does this by subdividing the input curve until a certain error
    // condition has been met. A recursive subdivision procedure would
    // return as many as 1<<limit curves, but this is an iterator and we
    // don't need all the curves all at once, so what we carry out a
    // lazy inorder traversal of the recursion tree (meaning we only move
    // through the tree when we need the next subdivided curve). This saves
    // us a lot of memory because at any one time we only need to store
    // limit+1 curves - one for each level of the tree + 1.
    // NOTE: the way we do things here is not enough to traverse a general
    // tree; however, the trees we are interested in have the property that
    // every non leaf node has exactly 2 children
    private static class LengthIterator {
        private enum Side {LEFT, RIGHT};
        // Holds the curves at various levels of the recursion. The root
        // (i.e. the original curve) is at recCurveStack[0] (but then it
        // gets subdivided, the left half is put at 1, so most of the time
        // only the right half of the original curve is at 0)
        private double[][] recCurveStack;
        // sides[i] indicates whether the node at level i+1 in the path from
        // the root to the current leaf is a left or right child of its parent.
        private Side[] sides;
        private int curveType;
        private final int limit;
        private final double ERR;
        private final double minTincrement;
        // lastT and nextT delimit the current leaf.
        private double nextT;
        private double lenAtNextT;
        private double lastT;
        private double lenAtLastT;
        private double lenAtLastSplit;
        private double lastSegLen;
        // the current level in the recursion tree. 0 is the root. limit
        // is the deepest possible leaf.
        private int recLevel;
        private boolean done;

        // the lengths of the lines of the control polygon. Only its first
        // curveType/2 - 1 elements are valid. This is an optimization. See
        // next(double) for more detail.
        private double[] curLeafCtrlPolyLengths = new double[3];

        public LengthIterator(int reclimit, double err) {
            this.limit = reclimit;
            this.minTincrement = 1f / (1 << limit);
            this.ERR = err;
            this.recCurveStack = new double[reclimit+1][8];
            this.sides = new Side[reclimit];
            // if any methods are called without first initializing this object on
            // a curve, we want it to fail ASAP.
            this.nextT = Double.MAX_VALUE;
            this.lenAtNextT = Double.MAX_VALUE;
            this.lenAtLastSplit = Double.MIN_VALUE;
            this.recLevel = Integer.MIN_VALUE;
            this.lastSegLen = Double.MAX_VALUE;
            this.done = true;
        }

        public void initializeIterationOnCurve(double[] pts, int type) {
            System.arraycopy(pts, 0, recCurveStack[0], 0, type);
            this.curveType = type;
            this.recLevel = 0;
            this.lastT = 0;
            this.lenAtLastT = 0;
            this.nextT = 0;
            this.lenAtNextT = 0;
            goLeft(); // initializes nextT and lenAtNextT properly
            this.lenAtLastSplit = 0;
            if (recLevel > 0) {
                this.sides[0] = Side.LEFT;
                this.done = false;
            } else {
                // the root of the tree is a leaf so we're done.
                this.sides[0] = Side.RIGHT;
                this.done = true;
            }
            this.lastSegLen = 0;
        }

        // 0 == false, 1 == true, -1 == invalid cached value.
        private int cachedHaveLowAcceleration = -1;

        private boolean haveLowAcceleration(double err) {
            if (cachedHaveLowAcceleration == -1) {
                final double len1 = curLeafCtrlPolyLengths[0];
                final double len2 = curLeafCtrlPolyLengths[1];
                // the test below is equivalent to !within(len1/len2, 1, err).
                // It is using a multiplication instead of a division, so it
                // should be a bit faster.
                if (!Helpers.within(len1, len2, err*len2)) {
                    cachedHaveLowAcceleration = 0;
                    return false;
                }
                if (curveType == 8) {
                    final double len3 = curLeafCtrlPolyLengths[2];
                    // if len1 is close to 2 and 2 is close to 3, that probably
                    // means 1 is close to 3 so the second part of this test might
                    // not be needed, but it doesn't hurt to include it.
                    if (!(Helpers.within(len2, len3, err*len3) &&
                          Helpers.within(len1, len3, err*len3))) {
                        cachedHaveLowAcceleration = 0;
                        return false;
                    }
                }
                cachedHaveLowAcceleration = 1;
                return true;
            }

            return (cachedHaveLowAcceleration == 1);
        }

        // we want to avoid allocations/gc so we keep this array so we
        // can put roots in it,
        private double[] nextRoots = new double[4];

        // caches the coefficients of the current leaf in its flattened
        // form (see inside next() for what that means). The cache is
        // invalid when it's third element is negative, since in any
        // valid flattened curve, this would be >= 0.
        private double[] flatLeafCoefCache = new double[] {0, 0, -1, 0};
        // returns the t value where the remaining curve should be split in
        // order for the left subdivided curve to have length len. If len
        // is >= than the length of the uniterated curve, it returns 1.
        public double next(final double len) {
            final double targetLength = lenAtLastSplit + len;
            while(lenAtNextT < targetLength) {
                if (done) {
                    lastSegLen = lenAtNextT - lenAtLastSplit;
                    return 1;
                }
                goToNextLeaf();
            }
            lenAtLastSplit = targetLength;
            final double leaflen = lenAtNextT - lenAtLastT;
            double t = (targetLength - lenAtLastT) / leaflen;

            // cubicRootsInAB is a fairly expensive call, so we just don't do it
            // if the acceleration in this section of the curve is small enough.
            if (!haveLowAcceleration(0.05f)) {
                // We flatten the current leaf along the x axis, so that we're
                // left with a, b, c which define a 1D Bezier curve. We then
                // solve this to get the parameter of the original leaf that
                // gives us the desired length.

                if (flatLeafCoefCache[2] < 0) {
                    double x = 0+curLeafCtrlPolyLengths[0],
                          y = x+curLeafCtrlPolyLengths[1];
                    if (curveType == 8) {
                        double z = y + curLeafCtrlPolyLengths[2];
                        flatLeafCoefCache[0] = 3*(x - y) + z;
                        flatLeafCoefCache[1] = 3*(y - 2*x);
                        flatLeafCoefCache[2] = 3*x;
                        flatLeafCoefCache[3] = -z;
                    } else if (curveType == 6) {
                        flatLeafCoefCache[0] = 0f;
                        flatLeafCoefCache[1] = y - 2*x;
                        flatLeafCoefCache[2] = 2*x;
                        flatLeafCoefCache[3] = -y;
                    }
                }
                double a = flatLeafCoefCache[0];
                double b = flatLeafCoefCache[1];
                double c = flatLeafCoefCache[2];
                double d = t*flatLeafCoefCache[3];

                // we use cubicRootsInAB here, because we want only roots in 0, 1,
                // and our quadratic root finder doesn't filter, so it's just a
                // matter of convenience.
                int n = Helpers.cubicRootsInAB(a, b, c, d, nextRoots, 0, 0, 1);
                if (n == 1 && !Double.isNaN(nextRoots[0])) {
                    t = nextRoots[0];
                }
            }
            // t is relative to the current leaf, so we must make it a valid parameter
            // of the original curve.
            t = t * (nextT - lastT) + lastT;
            if (t >= 1) {
                t = 1;
                done = true;
            }
            // even if done = true, if we're here, that means targetLength
            // is equal to, or very, very close to the total length of the
            // curve, so lastSegLen won't be too high. In cases where len
            // overshoots the curve, this method will exit in the while
            // loop, and lastSegLen will still be set to the right value.
            lastSegLen = len;
            return t;
        }

        public double lastSegLen() {
            return lastSegLen;
        }

        // go to the next leaf (in an inorder traversal) in the recursion tree
        // preconditions: must be on a leaf, and that leaf must not be the root.
        private void goToNextLeaf() {
            // We must go to the first ancestor node that has an unvisited
            // right child.
            recLevel--;
            while(sides[recLevel] == Side.RIGHT) {
                if (recLevel == 0) {
                    done = true;
                    return;
                }
                recLevel--;
            }

            sides[recLevel] = Side.RIGHT;
            System.arraycopy(recCurveStack[recLevel], 0,
                             recCurveStack[recLevel+1], 0, curveType);
            recLevel++;
            goLeft();
        }

        // go to the leftmost node from the current node. Return its length.
        private void goLeft() {
            double len = onLeaf();
            if (len >= 0) {
                lastT = nextT;
                lenAtLastT = lenAtNextT;
                nextT += (1 << (limit - recLevel)) * minTincrement;
                lenAtNextT += len;
                // invalidate caches
                flatLeafCoefCache[2] = -1;
                cachedHaveLowAcceleration = -1;
            } else {
                Helpers.subdivide(recCurveStack[recLevel], 0,
                                  recCurveStack[recLevel+1], 0,
                                  recCurveStack[recLevel], 0, curveType);
                sides[recLevel] = Side.LEFT;
                recLevel++;
                goLeft();
            }
        }

        // this is a bit of a hack. It returns -1 if we're not on a leaf, and
        // the length of the leaf if we are on a leaf.
        private double onLeaf() {
            double[] curve = recCurveStack[recLevel];
            double polyLen = 0;

            double x0 = curve[0], y0 = curve[1];
            for (int i = 2; i < curveType; i += 2) {
                final double x1 = curve[i], y1 = curve[i+1];
                final double len = Helpers.linelen(x0, y0, x1, y1);
                polyLen += len;
                curLeafCtrlPolyLengths[i/2 - 1] = len;
                x0 = x1;
                y0 = y1;
            }

            final double lineLen =
                Helpers.linelen(curve[0], curve[1], curve[curveType-2], curve[curveType-1]);
            if (polyLen - lineLen < ERR || recLevel == limit) {
                return (polyLen + lineLen)/2;
            }
            return -1;
        }
    }

    @Override
    public void curveTo(float x1, float y1,
                        float x2, float y2,
                        float x3, float y3)
    {
        curCurvepts[0] = x0;        curCurvepts[1] = y0;
        curCurvepts[2] = x1;        curCurvepts[3] = y1;
        curCurvepts[4] = x2;        curCurvepts[5] = y2;
        curCurvepts[6] = x3;        curCurvepts[7] = y3;
        somethingTo(8);
    }

    @Override
    public void quadTo(float x1, float y1, float x2, float y2) {
        curCurvepts[0] = x0;        curCurvepts[1] = y0;
        curCurvepts[2] = x1;        curCurvepts[3] = y1;
        curCurvepts[4] = x2;        curCurvepts[5] = y2;
        somethingTo(6);
    }

    public void closePath() {
        lineTo((float)sx, (float)sy);
        if (firstSegidx > 0) {
            if (!dashOn || needsMoveTo) {
                out.moveTo((float)sx, (float)sy);
            }
            emitFirstSegments();
        }
        moveTo((float)sx, (float)sy);
    }

    public void pathDone() {
        if (firstSegidx > 0) {
            out.moveTo((float)sx, (float)sy);
            emitFirstSegments();
        }
        out.pathDone();
    }
}

