package flat.math.operations;

import flat.math.Affine;
import flat.math.Vector2;
import flat.math.shapes.PathIterator;
import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;
import flat.math.util.FlatteningPathIterator;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

public class Area implements Shape, Cloneable {
    private static Vector<Curve> EmptyCurves = new Vector<>();

    private Vector<Curve> curves;

    /**
     * Default constructor which creates an empty area
     */
    public Area() {
        curves = EmptyCurves;
    }

    /**
     * The <code>Area</code> class creates an area geometry from the
     * specified {@link Shape} object.  The geometry is explicitly
     * closed, if the <code>Shape</code> is not already closed.  The
     * fill rule (even-odd or winding) specified by the geometry of the
     * <code>Shape</code> is used to determine the resulting enclosed area.
     *
     * @param shape the <code>Shape</code> from which the area is constructed
     */
    public Area(Shape shape) {
        set(shape);
    }

    public Area(PathIterator shape) {
        curves = pathToCurves(shape);
    }

    private static Vector<Curve> pathToCurves(PathIterator pi) {
        Vector<Curve> curves = new Vector<>();
        int windingRule = pi.windingRule();
        double coords[] = new double[23];
        float tcoords[] = new float[6];
        double movx = 0, movy = 0;
        double curx = 0, cury = 0;
        double newx, newy;
        while (!pi.isDone()) {
            int type = pi.currentSegment(tcoords);
            coords[0] = tcoords[0];coords[1] = tcoords[1];
            coords[2] = tcoords[2];coords[3] = tcoords[3];
            coords[4] = tcoords[4];coords[5] = tcoords[5];
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    Curve.insertLine(curves, curx, cury, movx, movy);
                    curx = movx = coords[0];
                    cury = movy = coords[1];
                    Curve.insertMove(curves, movx, movy);
                    break;
                case PathIterator.SEG_LINETO:
                    newx = coords[0];
                    newy = coords[1];
                    Curve.insertLine(curves, curx, cury, newx, newy);
                    curx = newx;
                    cury = newy;
                    break;
                case PathIterator.SEG_QUADTO:
                    newx = coords[2];
                    newy = coords[3];
                    Curve.insertQuad(curves, curx, cury, coords);
                    curx = newx;
                    cury = newy;
                    break;
                case PathIterator.SEG_CUBICTO:
                    newx = coords[4];
                    newy = coords[5];
                    Curve.insertCubic(curves, curx, cury, coords);
                    curx = newx;
                    cury = newy;
                    break;
                case PathIterator.SEG_CLOSE:
                    Curve.insertLine(curves, curx, cury, movx, movy);
                    curx = movx;
                    cury = movy;
                    break;
            }
            pi.next();
        }
        Curve.insertLine(curves, curx, cury, movx, movy);
        AreaOp operator = (windingRule == PathIterator.WIND_EVEN_ODD) ? new AreaOp.EOWindOp() : new AreaOp.NZWindOp();
        return operator.calculate(curves, EmptyCurves);
    }

    public Area set(Shape shape) {
        if (shape instanceof Area) {
            curves = ((Area) shape).curves;
        } else {
            curves = pathToCurves(shape.pathIterator(null));
        }
        return this;
    }

    public Area set(PathIterator iterator) {
        curves = pathToCurves(iterator);
        return this;
    }

    /**
     * Adds the shape of the specified <code>Area</code> to the
     * shape of this <code>Area</code>.
     * The resulting shape of this <code>Area</code> will include
     * the union of both shapes, or all areas that were contained
     * in either this or the specified <code>Area</code>.
     * <pre>
     *     // Example:
     *     Area a1 = new Area([triangle 0,0 =&gt; 8,0 =&gt; 0,8]);
     *     Area a2 = new Area([triangle 0,0 =&gt; 8,0 =&gt; 8,8]);
     *     a1.add(a2);
     *
     *        a1(before)     +         a2         =     a1(after)
     *
     *     ################     ################     ################
     *     ##############         ##############     ################
     *     ############             ############     ################
     *     ##########                 ##########     ################
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     * @param rhs  the <code>Area</code> to be added to the current shape
     */
    public Area add(Area rhs) {
        if (isEmpty()) {
            this.curves = rhs.curves;
        } else {
            curves = new AreaOp.AddOp().calculate(this.curves, rhs.curves);
            invalidateBounds();
        }
        return this;
    }

    /**
     * Subtracts the shape of the specified <code>Area</code> from the
     * shape of this <code>Area</code>.
     * The resulting shape of this <code>Area</code> will include
     * areas that were contained only in this <code>Area</code>
     * and not in the specified <code>Area</code>.
     * <pre>
     *     // Example:
     *     Area a1 = new Area([triangle 0,0 =&gt; 8,0 =&gt; 0,8]);
     *     Area a2 = new Area([triangle 0,0 =&gt; 8,0 =&gt; 8,8]);
     *     a1.subtract(a2);
     *
     *        a1(before)     -         a2         =     a1(after)
     *
     *     ################     ################
     *     ##############         ##############     ##
     *     ############             ############     ####
     *     ##########                 ##########     ######
     *     ########                     ########     ########
     *     ######                         ######     ######
     *     ####                             ####     ####
     *     ##                                 ##     ##
     * </pre>
     * @param rhs the <code>Area</code> to be subtracted from the current shape
     */
    public Area subtract(Area rhs) {
        curves = new AreaOp.SubOp().calculate(this.curves, rhs.curves);
        invalidateBounds();
        return this;
    }

    /**
     * Sets the shape of this <code>Area</code> to the intersection of
     * its current shape and the shape of the specified <code>Area</code>.
     * The resulting shape of this <code>Area</code> will include
     * only areas that were contained in both this <code>Area</code>
     * and also in the specified <code>Area</code>.
     * <pre>
     *     // Example:
     *     Area a1 = new Area([triangle 0,0 =&gt; 8,0 =&gt; 0,8]);
     *     Area a2 = new Area([triangle 0,0 =&gt; 8,0 =&gt; 8,8]);
     *     a1.intersect(a2);
     *
     *      a1(before)   intersect     a2         =     a1(after)
     *
     *     ################     ################     ################
     *     ##############         ##############       ############
     *     ############             ############         ########
     *     ##########                 ##########           ####
     *     ########                     ########
     *     ######                         ######
     *     ####                             ####
     *     ##                                 ##
     * </pre>
     * @param rhs the <code>Area</code> to be intersected with this <code>Area</code>
     */
    public Area intersect(Area rhs) {
        curves = new AreaOp.IntOp().calculate(this.curves, rhs.curves);
        invalidateBounds();
        return this;
    }

    /**
     * Sets the shape of this <code>Area</code> to be the combined area
     * of its current shape and the shape of the specified <code>Area</code>,
     * minus their intersection.
     * The resulting shape of this <code>Area</code> will include
     * only areas that were contained in either this <code>Area</code>
     * or in the specified <code>Area</code>, but not in both.
     * <pre>
     *     // Example:
     *     Area a1 = new Area([triangle 0,0 =&gt; 8,0 =&gt; 0,8]);
     *     Area a2 = new Area([triangle 0,0 =&gt; 8,0 =&gt; 8,8]);
     *     a1.exclusiveOr(a2);
     *
     *        a1(before)    xor        a2         =     a1(after)
     *
     *     ################     ################
     *     ##############         ##############     ##            ##
     *     ############             ############     ####        ####
     *     ##########                 ##########     ######    ######
     *     ########                     ########     ################
     *     ######                         ######     ######    ######
     *     ####                             ####     ####        ####
     *     ##                                 ##     ##            ##
     * </pre>
     * @param rhs  the <code>Area</code> to be exclusive ORed with this <code>Area</code>
     */
    public Area exclusiveOr(Area rhs) {
        curves = new AreaOp.XorOp().calculate(this.curves, rhs.curves);
        invalidateBounds();
        return this;
    }

    /**
     * Removes all of the geometry from this <code>Area</code> and restores it to an empty area.
     */
    public void reset() {
        curves = EmptyCurves;
        invalidateBounds();
    }

    /**
     * Tests whether this <code>Area</code> consists entirely of
     * straight edged polygonal geometry.
     * @return    <code>true</code> if the geometry of this
     * <code>Area</code> consists entirely of line segments;
     * <code>false</code> otherwise.
     * @since 1.2
     */
    public boolean isPolygonal() {
        Enumeration<Curve> elements = curves.elements();
        while (elements.hasMoreElements()) {
            if (elements.nextElement().getOrder() > 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests whether this <code>Area</code> is rectangular in shape.
     *
     * @return <code>true</code> if the geometry of this <code>Area</code> is rectangular in shape
     */
    public boolean isRectangular() {
        int size = curves.size();
        if (size == 0) {
            return true;
        }
        if (size > 3) {
            return false;
        }
        Curve c1 = curves.get(1);
        Curve c2 = curves.get(2);
        if (c1.getOrder() != 1 || c2.getOrder() != 1) {
            return false;
        }
        if (c1.getXTop() != c1.getXBot() || c2.getXTop() != c2.getXBot()) {
            return false;
        }
        if (c1.getYTop() != c2.getYTop() || c1.getYBot() != c2.getYBot()) {
            return false;
        }
        return true;
    }

    /**
     * Tests whether this <code>Area</code> is comprised of a single
     * closed subpath.  This method returns <code>true</code> if the
     * path contains 0 or 1 subpaths, or <code>false</code> if the path
     * contains more than 1 subpath.  The subpaths are counted by the
     * number of {@link PathIterator#SEG_MOVETO SEG_MOVETO}  segments
     * that appear in the path.
     *
     * @return <code>true</code> if the <code>Area</code> is comprised of a single basic geometry
     */
    public boolean isSingular() {
        if (curves.size() < 3) {
            return true;
        }
        Enumeration<Curve> elements = curves.elements();
        elements.nextElement(); // First Order0 "moveto"
        while (elements.hasMoreElements()) {
            if ((elements.nextElement()).getOrder() == 0) {
                return false;
            }
        }
        return true;
    }

    private Rectangle cachedBounds;
    private void invalidateBounds() {
        cachedBounds = null;
    }
    private Rectangle getCachedBounds() {
        if (cachedBounds != null) {
            return cachedBounds;
        }
        Rectangle r = new Rectangle();
        if (curves.size() > 0) {
            Curve c = curves.get(0);
            // First point is always an order 0 curve (moveto)
            r.set((float)c.getX0(), (float)c.getY0(), 0, 0);
            for (int i = 1; i < curves.size(); i++) {
                (curves.get(i)).enlarge(r);
            }
        }
        return (cachedBounds = r);
    }

    @Override
    public boolean isEmpty() {
        return (curves.size() == 0);
    }

    @Override
    public Rectangle bounds() {
        return getCachedBounds().bounds();
    }

    @Override
    public Area clone() {
        return new Area(this);
    }

    /**
     * Tests whether the geometries of the two <code>Area</code> objects
     * are equal.
     * This method will return false if the argument is null.
     * @param other the <code>Area</code> to be compared to this <code>Area</code>
     * @return  <code>true</code> if the two geometries are equal
     */
    public boolean equals(Area other) {
        // REMIND: A *much* simpler operation should be possible...
        // Should be able to do a curve-wise comparison since all Areas
        // should evaluate their curves in the same top-down order.
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        return false;
        //return new AreaOp.XorOp().calculate(this.curves, other.curves).isEmpty();
    }

    /**
     * Transforms the geometry of this <code>Area</code> using the specified
     * {@link Affine}.  The geometry is transformed in place, which
     * permanently changes the enclosed area defined by this object.
     * @param t  the transformation used to transform the area
     * @throws NullPointerException if <code>t</code> is null
     * @since 1.2
     */
    public void transform(Affine t) {
        if (t == null) {
            throw new NullPointerException("transform must not be null");
        }
        // REMIND: A simpler operation can be performed for some types
        // of transform.
        curves = pathToCurves(pathIterator(t));
        invalidateBounds();
    }

    /**
     * Creates a new <code>Area</code> object that contains the same
     * geometry as this <code>Area</code> transformed by the specified
     * <code>Affine</code>.  This <code>Area</code> object
     * is unchanged.
     * @param t  the specified <code>Affine</code> used to transform
     *           the new <code>Area</code>
     * @throws NullPointerException if <code>t</code> is null
     * @return   a new <code>Area</code> object representing the transformed
     *           geometry.
     * @since 1.2
     */
    public Area createTransformedArea(Affine t) {
        Area a = new Area(this);
        a.transform(t);
        return a;
    }

    @Override
    public boolean contains(float x, float y) {
        if (!getCachedBounds().contains(x, y)) {
            return false;
        }
        Enumeration enum_ = curves.elements();
        int crossings = 0;
        while (enum_.hasMoreElements()) {
            Curve c = (Curve) enum_.nextElement();
            crossings += c.crossingsFor(x, y);
        }
        return ((crossings & 1) == 1);
    }

    @Override
    public boolean contains(Vector2 p) {
        return contains(p.x, p.y);
    }

    @Override
    public boolean contains(float x, float y, float w, float h) {
        if (w < 0 || h < 0) {
            return false;
        }
        if (!getCachedBounds().contains(x, y, w, h)) {
            return false;
        }
        Crossings c = Crossings.findCrossings(curves, x, y, x+w, y+h);
        return (c != null && c.covers(y, y+h));
    }

    @Override
    public boolean contains(Rectangle r) {
        return contains(r.x, r.y, r.width, r.height);
    }

    @Override
    public boolean intersects(float x, float y, float w, float h) {
        if (w < 0 || h < 0) {
            return false;
        }
        if (!getCachedBounds().intersects(x, y, w, h)) {
            return false;
        }
        Crossings c = Crossings.findCrossings(curves, x, y, x+w, y+h);
        return (c == null || !c.isEmpty());
    }

    @Override
    public boolean intersects(Rectangle rectangle) {
        return intersects(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
    }

    @Override
    public PathIterator pathIterator(Affine at) {
        return new AreaIterator(curves, at);
    }

    @Override
    public PathIterator pathIterator(Affine at, float flatness) {
        return new FlatteningPathIterator(pathIterator(at), flatness);
    }

    static class AreaIterator implements PathIterator {
        private Affine transform;
        private Vector<Curve> curves;
        private int index;
        private Curve prevcurve;
        private Curve thiscurve;

        public AreaIterator(Vector<Curve> curves, Affine at) {
            this.curves = curves;
            this.transform = at;
            if (curves.size() >= 1) {
                thiscurve = curves.get(0);
            }
        }

        public int windingRule() {
            // REMIND: Which is better, EVEN_ODD or NON_ZERO?
            //         The paths calculated could be classified either way.
            //return WIND_EVEN_ODD;
            return WIND_NON_ZERO;
        }

        public boolean isDone() {
            return (prevcurve == null && thiscurve == null);
        }

        public void next() {
            if (prevcurve != null) {
                prevcurve = null;
            } else {
                prevcurve = thiscurve;
                index++;
                if (index < curves.size()) {
                    thiscurve = curves.get(index);
                    if (thiscurve.getOrder() != 0 &&
                            prevcurve.getX1() == thiscurve.getX0() &&
                            prevcurve.getY1() == thiscurve.getY0()) {
                        prevcurve = null;
                    }
                } else {
                    thiscurve = null;
                }
            }
        }

        public int currentSegment(float coords[]) {
            int segtype;
            int numpoints;
            if (prevcurve != null) {
                // Need to finish off junction between curves
                if (thiscurve == null || thiscurve.getOrder() == 0) {
                    return SEG_CLOSE;
                }
                coords[0] = (float) thiscurve.getX0();
                coords[1] = (float) thiscurve.getY0();
                segtype = SEG_LINETO;
                numpoints = 1;
            } else if (thiscurve == null) {
                throw new NoSuchElementException("area iterator out of bounds");
            } else {
                segtype = thiscurve.getSegment(coords);
                numpoints = thiscurve.getOrder();
                if (numpoints == 0) {
                    numpoints = 1;
                }
            }
            if (transform != null) {
                transform.transform(coords, 0, coords, 0, numpoints);
            }
            return segtype;
        }
    }
}