package flat.math.util;

import flat.math.shapes.CubicCurves;
import flat.math.shapes.PathIterator;
import flat.math.shapes.QuadCurves;

import java.util.NoSuchElementException;

/**
 * The <code>FlatteningPathIterator</code> class returns a flattened view of
 * another {@link PathIterator} object.  Other {@link java.awt.Shape Shape}
 * classes can use this class to provide flattening behavior for their paths
 * without having to perform the interpolation calculations themselves.
 *
 * @author Jim Graham
 */
public class FlatteningPathIterator implements PathIterator {
    static final int GROW_SIZE = 24;    // Multiple of cubic & quad curve size

    PathIterator src;                   // The source iterator

    double squareflat;                  // Square of the flatness parameter
    // for testing against squared lengths

    int limit;                          // Maximum number of recursion levels

    float hold[] = new float[14];     // The cache of interpolated coords
    // Note that this must be long enough
    // to store a full cubic segment and
    // a relative cubic segment to avoid
    // aliasing when copying the coords
    // of a curve to the end of the array.
    // This is also serendipitously equal
    // to the size of a full quad segment
    // and 2 relative quad segments.

    double curx, cury;                  // The ending x,y of the last segment

    double movx, movy;                  // The x,y of the last move segment

    int holdType;                       // The type of the curve being held
    // for interpolation

    int holdEnd;                        // The index of the last curve segment
    // being held for interpolation

    int holdIndex;                      // The index of the curve segment
    // that was last interpolated.  This
    // is the curve segment ready to be
    // returned in the next call to
    // currentSegment().

    int levels[];                       // The recursion level at which
    // each curve being held in storage
    // was generated.

    int levelIndex;                     // The index of the entry in the
    // levels array of the curve segment
    // at the holdIndex

    boolean done;                       // True when iteration is done

    /**
     * Constructs a new <code>FlatteningPathIterator</code> object that
     * flattens a path as it iterates over it.  The iterator does not
     * subdivide any curve read from the source iterator to more than
     * 10 levels of subdivision which yields a maximum of 1024 line
     * segments per curve.
     * @param src the original unflattened path being iterated over
     * @param flatness the maximum allowable distance between the
     * control points and the flattened curve
     */
    public FlatteningPathIterator(PathIterator src, double flatness) {
        this(src, flatness, 10);
    }

    /**
     * Constructs a new <code>FlatteningPathIterator</code> object
     * that flattens a path as it iterates over it.
     * The <code>limit</code> parameter allows you to control the
     * maximum number of recursive subdivisions that the iterator
     * can make before it assumes that the curve is com.flat enough
     * without measuring against the <code>flatness</code> parameter.
     * The flattened iteration therefore never generates more than
     * a maximum of <code>(2^limit)</code> line segments per curve.
     * @param src the original unflattened path being iterated over
     * @param flatness the maximum allowable distance between the
     * control points and the flattened curve
     * @param limit the maximum number of recursive subdivisions
     * allowed for any curved segment
     * @exception <code>IllegalArgumentException</code> if
     *          <code>flatness</code> or <code>limit</code>
     *          is less than zero
     */
    public FlatteningPathIterator(PathIterator src, double flatness,
                                  int limit) {
        if (flatness < 0.0) {
            throw new IllegalArgumentException("flatness must be >= 0");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be >= 0");
        }
        this.src = src;
        this.squareflat = flatness * flatness;
        this.limit = limit;
        this.levels = new int[limit + 1];
        // prime the first path segment
        next(false);
    }

    /**
     * Returns the flatness of this iterator.
     * @return the flatness of this <code>FlatteningPathIterator</code>.
     */
    public double getFlatness() {
        return Math.sqrt(squareflat);
    }

    /**
     * Returns the recursion limit of this iterator.
     * @return the recursion limit of this
     * <code>FlatteningPathIterator</code>.
     */
    public int getRecursionLimit() {
        return limit;
    }

    /**
     * Returns the winding rule for determining the interior of the
     * path.
     * @return the winding rule of the original unflattened path being
     * iterated over.
     * @see PathIterator#WIND_EVEN_ODD
     * @see PathIterator#WIND_NON_ZERO
     */
    public int windingRule() {
        return src.windingRule();
    }

    /**
     * Tests if the iteration is complete.
     * @return <code>true</code> if all the segments have
     * been read; <code>false</code> otherwise.
     */
    public boolean isDone() {
        return done;
    }

    /*
     * Ensures that the hold array can hold up to (want) more values.
     * It is currently holding (hold.length - holdIndex) values.
     */
    void ensureHoldCapacity(int want) {
        if (holdIndex - want < 0) {
            int have = hold.length - holdIndex;
            int newsize = hold.length + GROW_SIZE;
            float newhold[] = new float[newsize];
            System.arraycopy(hold, holdIndex,
                    newhold, holdIndex + GROW_SIZE,
                    have);
            hold = newhold;
            holdIndex += GROW_SIZE;
            holdEnd += GROW_SIZE;
        }
    }

    /**
     * Moves the iterator to the next segment of the path forwards
     * along the primary direction of traversal as long as there are
     * more points in that direction.
     */
    public void next() {
        next(true);
    }

    private void next(boolean doNext) {
        int level;

        if (holdIndex >= holdEnd) {
            if (doNext) {
                src.next();
            }
            if (src.isDone()) {
                done = true;
                return;
            }
            holdType = src.currentSegment(hold);
            levelIndex = 0;
            levels[0] = 0;
        }

        switch (holdType) {
            case SEG_MOVETO:
            case SEG_LINETO:
                curx = hold[0];
                cury = hold[1];
                if (holdType == SEG_MOVETO) {
                    movx = curx;
                    movy = cury;
                }
                holdIndex = 0;
                holdEnd = 0;
                break;
            case SEG_CLOSE:
                curx = movx;
                cury = movy;
                holdIndex = 0;
                holdEnd = 0;
                break;
            case SEG_QUADTO:
                if (holdIndex >= holdEnd) {
                    // Move the coordinates to the end of the array.
                    holdIndex = hold.length - 6;
                    holdEnd = hold.length - 2;
                    hold[holdIndex + 0] = (float) curx;
                    hold[holdIndex + 1] = (float) cury;
                    hold[holdIndex + 2] = hold[0];
                    hold[holdIndex + 3] = hold[1];
                    hold[holdIndex + 4] = (float) (curx = hold[2]);
                    hold[holdIndex + 5] = (float) (cury = hold[3]);
                }

                level = levels[levelIndex];
                while (level < limit) {
                    if (QuadCurves.flatnessSq(hold, holdIndex) < squareflat) {
                        break;
                    }

                    ensureHoldCapacity(4);
                    QuadCurves.subdivide(hold, holdIndex,
                            hold, holdIndex - 4,
                            hold, holdIndex);
                    holdIndex -= 4;

                    // Now that we have subdivided, we have constructed
                    // two curves of one depth lower than the original
                    // curve.  One of those curves is in the place of
                    // the former curve and one of them is in the next
                    // set of held coordinate slots.  We now set both
                    // curves level values to the next higher level.
                    level++;
                    levels[levelIndex] = level;
                    levelIndex++;
                    levels[levelIndex] = level;
                }

                // This curve segment is com.flat enough, or it is too deep
                // in recursion levels to try to flatten any more.  The
                // two coordinates at holdIndex+4 and holdIndex+5 now
                // contain the endpoint of the curve which can be the
                // endpoint of an approximating line segment.
                holdIndex += 4;
                levelIndex--;
                break;
            case SEG_CUBICTO:
                if (holdIndex >= holdEnd) {
                    // Move the coordinates to the end of the array.
                    holdIndex = hold.length - 8;
                    holdEnd = hold.length - 2;
                    hold[holdIndex + 0] = (float) curx;
                    hold[holdIndex + 1] = (float) cury;
                    hold[holdIndex + 2] = hold[0];
                    hold[holdIndex + 3] = hold[1];
                    hold[holdIndex + 4] = hold[2];
                    hold[holdIndex + 5] = hold[3];
                    hold[holdIndex + 6] = (float) (curx = hold[4]);
                    hold[holdIndex + 7] = (float) (cury = hold[5]);
                }

                level = levels[levelIndex];
                while (level < limit) {
                    if (CubicCurves.flatnessSq(hold, holdIndex) < squareflat) {
                        break;
                    }

                    ensureHoldCapacity(6);
                    CubicCurves.subdivide(hold, holdIndex,
                            hold, holdIndex - 6,
                            hold, holdIndex);
                    holdIndex -= 6;

                    // Now that we have subdivided, we have constructed
                    // two curves of one depth lower than the original
                    // curve.  One of those curves is in the place of
                    // the former curve and one of them is in the next
                    // set of held coordinate slots.  We now set both
                    // curves level values to the next higher level.
                    level++;
                    levels[levelIndex] = level;
                    levelIndex++;
                    levels[levelIndex] = level;
                }

                // This curve segment is com.flat enough, or it is too deep
                // in recursion levels to try to flatten any more.  The
                // two coordinates at holdIndex+6 and holdIndex+7 now
                // contain the endpoint of the curve which can be the
                // endpoint of an approximating line segment.
                holdIndex += 6;
                levelIndex--;
                break;
        }
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     * The return value is the path segment type:
     * SEG_MOVETO, SEG_LINETO, or SEG_CLOSE.
     * A float array of length 6 must be passed in and can be used to
     * store the coordinates of the point(s).
     * Each point is stored as a pair of float x,y coordinates.
     * SEG_MOVETO and SEG_LINETO types return one point,
     * and SEG_CLOSE does not return any points.
     * @param coords an array that holds the data returned from
     * this method
     * @return the path segment type of the current path segment.
     * @exception <code>NoSuchElementException</code> if there
     *          are no more elements in the flattening path to be
     *          returned.
     * @see PathIterator#SEG_MOVETO
     * @see PathIterator#SEG_LINETO
     * @see PathIterator#SEG_CLOSE
     */
    public int currentSegment(float[] coords) {
        if (isDone()) {
            throw new NoSuchElementException("flattening iterator out of bounds");
        }
        int type = holdType;
        if (type != SEG_CLOSE) {
            coords[0] = (float) hold[holdIndex + 0];
            coords[1] = (float) hold[holdIndex + 1];
            if (type != SEG_MOVETO) {
                type = SEG_LINETO;
            }
        }
        return type;
    }

    /**
     * Returns the coordinates and type of the current path segment in
     * the iteration.
     * The return value is the path segment type:
     * SEG_MOVETO, SEG_LINETO, or SEG_CLOSE.
     * A double array of length 6 must be passed in and can be used to
     * store the coordinates of the point(s).
     * Each point is stored as a pair of double x,y coordinates.
     * SEG_MOVETO and SEG_LINETO types return one point,
     * and SEG_CLOSE does not return any points.
     * @param coords an array that holds the data returned from
     * this method
     * @return the path segment type of the current path segment.
     * @exception <code>NoSuchElementException</code> if there
     *          are no more elements in the flattening path to be
     *          returned.
     * @see PathIterator#SEG_MOVETO
     * @see PathIterator#SEG_LINETO
     * @see PathIterator#SEG_CLOSE
     */
    public int currentSegment(double[] coords) {
        if (isDone()) {
            throw new NoSuchElementException("flattening iterator out of bounds");
        }
        int type = holdType;
        if (type != SEG_CLOSE) {
            coords[0] = hold[holdIndex + 0];
            coords[1] = hold[holdIndex + 1];
            if (type != SEG_MOVETO) {
                type = SEG_LINETO;
            }
        }
        return type;
    }
}