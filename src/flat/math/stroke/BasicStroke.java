package flat.math.stroke;

import flat.math.operations.Area;
import flat.math.shapes.Path;
import flat.math.shapes.PathConsumer;
import flat.math.shapes.PathIterator;
import flat.math.shapes.Shape;

public final class BasicStroke {
    public static final int CAP_BUTT = Stroker.CAP_BUTT;
    public static final int CAP_ROUND = Stroker.CAP_ROUND;
    public static final int CAP_SQUARE = Stroker.CAP_SQUARE;

    public static final int JOIN_MITER = Stroker.JOIN_MITER;
    public static final int JOIN_ROUND = Stroker.JOIN_ROUND;
    public static final int JOIN_BEVEL = Stroker.JOIN_BEVEL;

    public static final int TYPE_CENTERED = 0;
    public static final int TYPE_INNER = 1;
    public static final int TYPE_OUTER = 2;

    private final float width;
    private final int type;
    private final int cap;
    private final int join;
    private final float miterLimit;
    private final float dash[];
    private final float dashPhase;

    public BasicStroke() {
        this(1.0f);
    }

    public BasicStroke(BasicStroke other) {
        this(other.width, other.cap, other.join, other.miterLimit, other.dash, other.dashPhase, other.type);
    }

    public BasicStroke(float width) {
        this(width, CAP_SQUARE, JOIN_MITER);
    }

    public BasicStroke(float width, int cap, int join) {
        this(width, cap, join, 10.0f);
    }

    public BasicStroke(float width, int cap, int join, float miterLimit) {
        this(width, cap, join, miterLimit, null, 0);
    }

    public BasicStroke(float width, int cap, int join, float miterLimit, float[] dash, float dashPhase) {
        this(width, cap, join, miterLimit, dash, dashPhase, TYPE_CENTERED);
    }

    public BasicStroke(float width, int cap, int join, float miterLimit, float[] dash, float dashPhase, int type) {
        if (type != TYPE_CENTERED && type != TYPE_INNER && type != TYPE_OUTER) {
            throw new IllegalArgumentException("illegal type");
        }
        if (width < 0.0f) {
            throw new IllegalArgumentException("negative width");
        }
        if (cap != CAP_BUTT && cap != CAP_ROUND && cap != CAP_SQUARE) {
            throw new IllegalArgumentException("illegal end cap value");
        }
        if (join == JOIN_MITER) {
            if (miterLimit < 1.0f) {
                throw new IllegalArgumentException("miter limit < 1");
            }
        } else if (join != JOIN_ROUND && join != JOIN_BEVEL) {
            throw new IllegalArgumentException("illegal line join value");
        }
        this.type = type;
        this.width = width;
        this.cap = cap;
        this.join = join;
        this.miterLimit = miterLimit;

        if (dash != null) {
            if (dashPhase < 0.0f) {
                throw new IllegalArgumentException("negative dash phase");
            }
            boolean allzero = true;
            for (int i = 0; i < dash.length; i++) {
                float d = dash[i];
                if (d > 0.0) {
                    allzero = false;
                } else if (d < 0.0) {
                    throw new IllegalArgumentException("negative dash length");
                }
            }
            if (allzero) {
                throw new IllegalArgumentException("dash lengths all zero");
            }
        }
        this.dash = dash == null ? null : dash.clone();
        this.dashPhase = dashPhase;
    }

    /**
     * Returns the stroke type, one of {@code TYPE_CENTERED},
     * {@code TYPE_INNER}, or {@code TYPE_OUTER}.
     * @return the stroke type
     */
    public int getType() {
        return type;
    }

    /**
     * Returns the line width.  Line width is represented in user space,
     * which is the default-coordinate space used by Java 2D.  See the
     * <code>Graphics2D</code> class comments for more information on
     * the user space coordinate system.
     * @return the line width of this <code>BasicStroke</code>.
     */
    public float getLineWidth() {
        return width;
    }

    /**
     * Returns the end cap style.
     * @return the end cap style of this <code>BasicStroke</code> as one
     * of the static <code>int</code> values that define possible end cap
     * styles.
     */
    public int getEndCap() {
        return cap;
    }

    /**
     * Returns the line join style.
     * @return the line join style of the <code>BasicStroke</code> as one
     * of the static <code>int</code> values that define possible line
     * join styles.
     */
    public int getLineJoin() {
        return join;
    }

    /**
     * Returns the limit of miter joins.
     * @return the limit of miter joins of the <code>BasicStroke</code>.
     */
    public float getMiterLimit() {
        return miterLimit;
    }

    /**
     * Returns true if this stroke object will apply dashing attributes
     * to the path.
     * @return whether the stroke has dashes
     */
    public boolean isDashed() {
        return (dash != null);
    }

    /**
     * Returns the array representing the lengths of the dash segments.
     * Alternate entries in the array represent the user space lengths
     * of the opaque and transparent segments of the dashes.
     * As the pen moves along the outline of the <code>Shape</code>
     * to be stroked, the user space
     * distance that the pen travels is accumulated.  The distance
     * value is used to index into the dash array.
     * The pen is opaque when its current cumulative distance maps
     * to an even element of the dash array and transparent otherwise.
     * @return the dash array.
     */
    public float[] getDashArray() {
        return dash.clone();
    }

    /**
     * Returns the current dash phase.
     * The dash phase is a distance specified in user coordinates that
     * represents an offset into the dashing pattern. In other words, the dash
     * phase defines the point in the dashing pattern that will correspond to
     * the beginning of the stroke.
     * @return the dash phase as a <code>float</code> value.
     */
    public float getDashPhase() {
        return dashPhase;
    }

    public Shape createStrokedShape(Shape s) {
        Shape ret = createCenteredStrokedShape(s);

        if (type == TYPE_INNER) {
            ret = makeIntersectedShape(ret, s);
        } else if (type == TYPE_OUTER) {
            ret = makeSubtractedShape(ret, s);
        }
        return ret;
    }

    private boolean isCW(final float dx1, final float dy1,
                         final float dx2, final float dy2)
    {
        return dx1 * dy2 <= dy1 * dx2;
    }

    private void computeOffset(final float lx, final float ly,
                               final float w, final float[] m, int off) {
        final float len = (float) Math.sqrt(lx * lx + ly * ly);
        if (len == 0) {
            m[off + 0] = m[off + 1] = 0;
        } else {
            m[off + 0] = (ly * w) / len;
            m[off + 1] = -(lx * w) / len;
        }
    }

    private void computeMiter(final float x0, final float y0,
                              final float x1, final float y1,
                              final float x0p, final float y0p,
                              final float x1p, final float y1p,
                              final float[] m, int off)
    {
        float x10 = x1 - x0;
        float y10 = y1 - y0;
        float x10p = x1p - x0p;
        float y10p = y1p - y0p;

        // if this is 0, the lines are parallel. If they go in the
        // same direction, there is no intersection so m[off] and
        // m[off+1] will contain infinity, so no miter will be drawn.
        // If they go in the same direction that means that the start of the
        // current segment and the end of the previous segment have the same
        // tangent, in which case this method won't even be involved in
        // miter drawing because it won't be called by drawMiter (because
        // (mx == omx && my == omy) will be true, and drawMiter will return
        // immediately).
        float den = x10*y10p - x10p*y10;
        float t = x10p*(y0-y0p) - y10p*(x0-x0p);
        t /= den;
        m[off++] = x0 + t*x10;
        m[off] = y0 + t*y10;
    }

    private void accumulateQuad(float bbox[], int off,
                               float v0, float vc, float v1, float w)
    {
        // Breaking this quad down into a polynomial:
        // eqn[0] = v0;
        // eqn[1] = vc + vc - v0 - v0;
        // eqn[2] = v0 - vc - vc + v1;
        // Deriving the polynomial:
        // eqn'[0] = 1*eqn[1] = 2*(vc-v0)
        // eqn'[1] = 2*eqn[2] = 2*((v1-vc)-(vc-v0))
        // Solving for zeroes on the derivative:
        // e1*t + e0 = 0
        // t = -e0/e1;
        // t = -2(vc-v0) / 2((v1-vc)-(vc-v0))
        // t = (v0-vc) / (v1-vc+v0-vc)
        float num = v0 - vc;
        float den = v1 - vc + num;
        if (den != 0f) {
            float t = num / den;
            if (t > 0 && t < 1) {
                float u = 1f - t;
                float v = v0 * u * u + 2 * vc * t * u + v1 * t * t;
                if (bbox[off] > v - w) bbox[off] = v - w;
                if (bbox[off+2] < v + w) bbox[off+2] = v + w;
            }
        }
    }

    private void accumulateCubic(float bbox[], int off, float t,
                                float v0, float vc0, float vc1, float v1, float w)
    {
        if (t > 0 && t < 1) {
            float u = 1f - t;
            float v =        v0 * u * u * u
                      + 3 * vc0 * t * u * u
                      + 3 * vc1 * t * t * u
                      +      v1 * t * t * t;
            if (bbox[off] > v - w) bbox[off] = v - w;
            if (bbox[off+2] < v + w) bbox[off+2] = v + w;
        }
    }

    private void accumulateCubic(float bbox[], int off,
                                float v0, float vc0, float vc1, float v1, float w)
    {
        // Breaking this cubic down into a polynomial:
        // eqn[0] = v0;
        // eqn[1] = (vc0 - v0) * 3f;
        // eqn[2] = (vc1 - vc0 - vc0 + v0) * 3f;
        // eqn[3] = v1 + (vc0 - vc1) * 3f - v0;
        // Deriving the polynomial:
        // eqn'[0] = 1*eqn[1] = 3(vc0-v0)
        // eqn'[1] = 2*eqn[2] = 6((vc1-vc0)-(vc0-v0))
        // eqn'[2] = 3*eqn[3] = 3((v1-vc1)-2(vc1-vc0)+(vc0-v0))
        // Solving for zeroes on the derivative:
        // e2*t*t + e1*t + e0 = a*t*t + b*t + c = 0
        // Note that in solving for 0 we can divide all e0,e1,e2 by 3
        // t = (-b +/- sqrt(b*b-4ac))/2a
        float c = vc0 - v0;
        float b = 2f * ((vc1 - vc0) - c);
        float a = (v1 - vc1) - b - c;
        if (a == 0f) {
            // The quadratic parabola has degenerated to a line.
            if (b == 0f) {
                // The line has degenerated to a constant.
                return;
            }
            accumulateCubic(bbox, off, -c/b, v0, vc0, vc1, v1, w);
        } else {
            // From Numerical Recipes, 5.6, Quadratic and Cubic Equations
            float d = b * b - 4f * a * c;
            if (d < 0f) {
                // If d < 0.0, then there are no roots
                return;
            }
            d = (float) Math.sqrt(d);
            // For accuracy, calculate one root using:
            //     (-b +/- d) / 2a
            // and the other using:
            //     2c / (-b +/- d)
            // Choose the sign of the +/- so that b+d gets larger in magnitude
            if (b < 0f) {
                d = -d;
            }
            float q = (b + d) / -2f;
            // We already tested a for being 0 above
            accumulateCubic(bbox, off, q/a, v0, vc0, vc1, v1, w);
            if (q != 0f) {
                accumulateCubic(bbox, off, c/q, v0, vc0, vc1, v1, w);
            }
        }
    }

    private void accumulate(float o0, float o1, float o2, float o3, float[] bbox) {
        if (o0 <= o2) {
            if (o0 < bbox[0]) bbox[0] = o0;
            if (o2 > bbox[2]) bbox[2] = o2;
        } else {
            if (o2 < bbox[0]) bbox[0] = o2;
            if (o0 > bbox[2]) bbox[2] = o0;
        }
        if (o1 <= o3) {
            if (o1 < bbox[1]) bbox[1] = o1;
            if (o3 > bbox[3]) bbox[3] = o3;
        } else {
            if (o3 < bbox[1]) bbox[1] = o3;
            if (o1 > bbox[3]) bbox[3] = o1;
        }
    }

    private void accumulateOrdered(float o0, float o1, float o2, float o3, float[] bbox) {
        if (o0 < bbox[0]) bbox[0] = o0;
        if (o2 > bbox[2]) bbox[2] = o2;
        if (o1 < bbox[1]) bbox[1] = o1;
        if (o3 > bbox[3]) bbox[3] = o3;
    }


    private void accumulateJoin(float pdx, float pdy, float dx, float dy, float x0, float y0,
                                float pox, float poy, float ox, float oy, float[] bbox, float w) {

        if (join == JOIN_BEVEL) {
            accumulateBevel(x0, y0, pox, poy, ox, oy, bbox);
        } else if (join == JOIN_MITER) {
            accumulateMiter(pdx, pdy, dx, dy, pox, poy, ox, oy, x0, y0, bbox, w);
        } else { // JOIN_ROUND
            accumulateOrdered(x0 - w, y0 - w, x0 + w, y0 + w, bbox);
        }


    }

    private void accumulateCap(float dx, float dy, float x0, float y0,
                               float ox, float oy, float[] bbox, float w) {
        if (cap == CAP_SQUARE) {
            accumulate(x0 + ox - oy, y0 + oy + ox, x0 - ox - oy, y0 - oy + ox, bbox);
        } else if (cap == CAP_BUTT) {
            accumulate(x0 + ox, y0 + oy, x0 - ox, y0 - oy, bbox);
        } else { //cap == CAP_ROUND
            accumulateOrdered(x0 - w, y0 - w, x0 + w, y0 + w, bbox);
        }

    }

    private float[] tmpMiter = new float[2];

    private void accumulateMiter(float pdx, float pdy, float dx, float dy,
                                    float pox, float poy, float ox, float oy,
                                    float x0, float y0, float[] bbox, float w) {
        // Always accumulate bevel for cases of degenerate miters...
        accumulateBevel(x0, y0, pox, poy, ox, oy, bbox);

        boolean cw = isCW(pdx, pdy, dx, dy);

        if (cw) {
            pox = -pox;
            poy = -poy;
            ox = -ox;
            oy = -oy;
        }

        computeMiter((x0 - pdx) + pox, (y0 - pdy) + poy, x0 + pox, y0 + poy,
                     (x0 + dx) + ox, (y0 + dy) + oy, x0 + ox, y0 + oy,
                     tmpMiter, 0);
        float lenSq = (tmpMiter[0] - x0) * (tmpMiter[0] - x0) + (tmpMiter[1] - y0) * (tmpMiter[1] - y0);

        float miterLimitWidth = miterLimit * w;
        if (lenSq < miterLimitWidth * miterLimitWidth) {
            accumulateOrdered(tmpMiter[0], tmpMiter[1], tmpMiter[0], tmpMiter[1], bbox);
        }
    }


    private void accumulateBevel(float x0, float y0, float pox, float poy, float ox, float oy, float[] bbox) {
        accumulate(x0 + pox, y0 + poy, x0 - pox, y0 - poy, bbox);
        accumulate(x0 + ox, y0 + oy, x0 - ox, y0 - oy, bbox);
    }

    public Shape createCenteredStrokedShape(Shape s) {
        Path p2d = new Path(Path.WIND_NON_ZERO);
        float lw = (type == TYPE_CENTERED) ? width : width * 2.0f;
        PathConsumer pc2d = new Stroker(p2d, lw, cap, join, miterLimit);
        if (dash != null) {
            pc2d = new Dasher(pc2d, dash, dashPhase);
        }
        feedConsumer(s.pathIterator(null), pc2d);
        return p2d;
    }

    public static void feedConsumer(PathIterator pi, PathConsumer pc) {
        float[] coords = new float[6];
        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    pc.moveTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    pc.lineTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    pc.quadTo(coords[0], coords[1],
                            coords[2], coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    pc.curveTo(coords[0], coords[1],
                            coords[2], coords[3],
                            coords[4], coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    pc.closePath();
                    break;
            }
            pi.next();
        }
        pc.pathDone();
    }

    protected Shape makeIntersectedShape(Shape outer, Shape inner) {
        return new Area(outer).intersect(new Area(inner));
    }

    protected Shape makeSubtractedShape(Shape outer, Shape inner) {
        return new Area(outer).subtract(new Area(inner));
    }

    /**
     * Returns the hashcode for this stroke.
     * @return      a hash code for this stroke.
     */
    @Override
    public int hashCode() {
        int hash = Float.floatToIntBits(width);
        hash = hash * 31 + join;
        hash = hash * 31 + cap;
        hash = hash * 31 + Float.floatToIntBits(miterLimit);
        if (dash != null) {
            hash = hash * 31 + Float.floatToIntBits(dashPhase);
            for (int i = 0; i < dash.length; i++) {
                hash = hash * 31 + Float.floatToIntBits(dash[i]);
            }
        }
        return hash;
    }

    /**
     * Tests if a specified object is equal to this <code>BasicStroke</code>
     * by first testing if it is a <code>BasicStroke</code> and then comparing
     * its width, join, cap, miter limit, dash, and dash phase attributes with
     * those of this <code>BasicStroke</code>.
     * @param  obj the specified object to compare to this
     *              <code>BasicStroke</code>
     * @return <code>true</code> if the width, join, cap, miter limit, dash, and
     *            dash phase are the same for both objects;
     *            <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BasicStroke)) {
            return false;
        }
        BasicStroke bs = (BasicStroke) obj;
        if (width != bs.width) {
            return false;
        }
        if (join != bs.join) {
            return false;
        }
        if (cap != bs.cap) {
            return false;
        }
        if (miterLimit != bs.miterLimit) {
            return false;
        }
        if (dash != null) {
            if (dashPhase != bs.dashPhase) {
                return false;
            }
            if (!java.util.Arrays.equals(dash, bs.dash)) {
                return false;
            }
        }
        else if (bs.dash != null) {
            return false;
        }

        return true;
    }

    @Override
    public BasicStroke clone() {
        return new BasicStroke(width, cap, join, miterLimit, dash, dashPhase, type);
    }
}
