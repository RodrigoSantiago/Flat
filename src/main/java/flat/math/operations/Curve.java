package flat.math.operations;

import flat.math.shapes.Rectangle;

import java.util.Vector;

public abstract class Curve {
    public static final int INCREASING = 1;
    public static final int DECREASING = -1;

    protected final int direction;

    public static void insertMove(Vector<Curve> curves, double x, double y) {
        curves.add(new Order0(x, y));
    }

    public static void insertLine(Vector<Curve> curves, double x0, double y0, double x1, double y1) {
        if (y0 < y1) {
            curves.add(new Order1(x0, y0, x1, y1, INCREASING));
        } else if (y0 > y1) {
            curves.add(new Order1(x1, y1, x0, y0, DECREASING));
        } else {
            // Do not add horizontal lines
        }
    }

    public static void insertQuad(Vector<Curve> curves, double x0, double y0, double coords[]) {
        double y1 = coords[3];
        if (y0 > y1) {
            Order2.insert(curves, coords, coords[2], y1, coords[0], coords[1], x0, y0, DECREASING);
        } else if (y0 != y1 || y0 != coords[1]) {
            Order2.insert(curves, coords, x0, y0, coords[0], coords[1], coords[2], y1, INCREASING);
        } else {
            // Do not add horizontal lines
        }
    }

    public static void insertCubic(Vector<Curve> curves, double x0, double y0, double coords[]) {
        double y1 = coords[5];
        if (y0 > y1) {
            Order3.insert(curves, coords, coords[4], y1, coords[2], coords[3], coords[0], coords[1], x0, y0, DECREASING);
        } else if (y0 != y1 || y0 != coords[1] || y0 != coords[3]) {
            Order3.insert(curves, coords, x0, y0, coords[0], coords[1], coords[2], coords[3], coords[4], y1, INCREASING);
        } else {
            // Do not add horizontal lines
        }
    }

    public Curve(int direction) {
        this.direction = direction;
    }

    public final int getDirection() {
        return direction;
    }

    public final Curve getWithDirection(int direction) {
        return (this.direction == direction ? this : getReversedCurve());
    }

    public static int orderof(double x1, double x2) {
        return Double.compare(x1, x2);
    }

    public abstract int getOrder();

    public abstract double getXTop();
    public abstract double getYTop();
    public abstract double getXBot();
    public abstract double getYBot();

    public abstract double getXMin();
    public abstract double getXMax();

    public abstract double getX0();
    public abstract double getY0();
    public abstract double getX1();
    public abstract double getY1();

    public abstract double XforY(double y);
    public abstract double TforY(double y);
    public abstract double XforT(double t);
    public abstract double YforT(double t);
    public abstract double dXforT(double t, int deriv);
    public abstract double dYforT(double t, int deriv);

    public abstract double nextVertical(double t0, double t1);

    public int crossingsFor(double x, double y) {
        if (y >= getYTop() && y < getYBot()) {
            if (x < getXMax() && (x < getXMin() || x < XforY(y))) {
                return 1;
            }
        }
        return 0;
    }

    public boolean accumulateCrossings(Crossings c) {
        double xhi = c.getXHi();
        if (getXMin() >= xhi) {
            return false;
        }
        double xlo = c.getXLo();
        double ylo = c.getYLo();
        double yhi = c.getYHi();
        double y0 = getYTop();
        double y1 = getYBot();
        double tstart, ystart, tend, yend;
        if (y0 < ylo) {
            if (y1 <= ylo) {
                return false;
            }
            ystart = ylo;
            tstart = TforY(ylo);
        } else {
            if (y0 >= yhi) {
                return false;
            }
            ystart = y0;
            tstart = 0;
        }
        if (y1 > yhi) {
            yend = yhi;
            tend = TforY(yhi);
        } else {
            yend = y1;
            tend = 1;
        }
        boolean hitLo = false;
        boolean hitHi = false;
        while (true) {
            double x = XforT(tstart);
            if (x < xhi) {
                if (hitHi || x > xlo) {
                    return true;
                }
                hitLo = true;
            } else {
                if (hitLo) {
                    return true;
                }
                hitHi = true;
            }
            if (tstart >= tend) {
                break;
            }
            tstart = nextVertical(tstart, tend);
        }
        if (hitLo) {
            c.record(ystart, yend, direction);
        }
        return false;
    }

    public abstract void enlarge(Rectangle r);

    public abstract Curve getReversedCurve();

    public abstract Curve getSubCurve(double ystart, double yend, int dir);

    public int compareTo(Curve that, double yrange[]) {
        double y0 = yrange[0];
        double y1 = yrange[1];
        y1 = Math.min(Math.min(y1, this.getYBot()), that.getYBot());
        if (y1 <= yrange[0]) {
            throw new InternalError("target range = " + yrange[0] + "=>" + yrange[1] + "\nbackstepping from " + yrange[0] + " to " + y1);
        }
        yrange[1] = y1;
        if (this.getXMax() <= that.getXMin()) {
            if (this.getXMin() == that.getXMax()) {
                return 0;
            }
            return -1;
        }
        if (this.getXMin() >= that.getXMax()) {
            return 1;
        }
        // Parameter s for thi(s) curve and t for tha(t) curve
        // [st]0 = parameters for top of current section of interest
        // [st]1 = parameters for bottom of valid range
        // [st]h = parameters for hypothesis point
        // [d][xy]s = valuations of thi(s) curve at sh
        // [d][xy]t = valuations of tha(t) curve at th
        double s0 = this.TforY(y0);
        double ys0 = this.YforT(s0);
        if (ys0 < y0) {
            s0 = refineTforY(s0, ys0, y0);
            ys0 = this.YforT(s0);
        }
        double s1 = this.TforY(y1);
        if (this.YforT(s1) < y0) {
            s1 = refineTforY(s1, this.YforT(s1), y0);
        }
        double t0 = that.TforY(y0);
        double yt0 = that.YforT(t0);
        if (yt0 < y0) {
            t0 = that.refineTforY(t0, yt0, y0);
            yt0 = that.YforT(t0);
        }
        double t1 = that.TforY(y1);
        if (that.YforT(t1) < y0) {
            t1 = that.refineTforY(t1, that.YforT(t1), y0);
        }
        double xs0 = this.XforT(s0);
        double xt0 = that.XforT(t0);
        double scale = Math.max(Math.abs(y0), Math.abs(y1));
        double ymin = Math.max(scale * 1E-14, 1E-300);
        if (fairlyClose(xs0, xt0)) {
            double bump = ymin;
            double maxbump = Math.min(ymin * 1E13, (y1 - y0) * .1);
            double y = y0 + bump;
            while (y <= y1) {
                if (fairlyClose(this.XforY(y), that.XforY(y))) {
                    if ((bump *= 2) > maxbump) {
                        bump = maxbump;
                    }
                } else {
                    y -= bump;
                    while (true) {
                        bump /= 2;
                        double newy = y + bump;
                        if (newy <= y) {
                            break;
                        }
                        if (fairlyClose(this.XforY(newy), that.XforY(newy))) {
                            y = newy;
                        }
                    }
                    break;
                }
                y += bump;
            }
            if (y > y0) {
                if (y < y1) {
                    yrange[1] = y;
                }
                return 0;
            }
        }

        // assert (ymin > 0);

        while (s0 < s1 && t0 < t1) {
            double sh = this.nextVertical(s0, s1);
            double xsh = this.XforT(sh);
            double ysh = this.YforT(sh);
            double th = that.nextVertical(t0, t1);
            double xth = that.XforT(th);
            double yth = that.YforT(th);
        try {
            if (findIntersect(that, yrange, ymin, 0, 0,
                              s0, xs0, ys0, sh, xsh, ysh,
                              t0, xt0, yt0, th, xth, yth)) {
                break;
            }
        } catch (Throwable t) {
            return 0;
        }
            if (ysh < yth) {
                if (ysh > yrange[0]) {
                    if (ysh < yrange[1]) {
                        yrange[1] = ysh;
                    }
                    break;
                }
                s0 = sh;
                xs0 = xsh;
                ys0 = ysh;
            } else {
                if (yth > yrange[0]) {
                    if (yth < yrange[1]) {
                        yrange[1] = yth;
                    }
                    break;
                }
                t0 = th;
                xt0 = xth;
                yt0 = yth;
            }
        }
        double ymid = (yrange[0] + yrange[1]) / 2;
        return orderof(this.XforY(ymid), that.XforY(ymid));
    }

    public static final double TMIN = 1E-3;

    public boolean findIntersect(Curve that, double yrange[], double ymin,
                                 int slevel, int tlevel,
                                 double s0, double xs0, double ys0,
                                 double s1, double xs1, double ys1,
                                 double t0, double xt0, double yt0,
                                 double t1, double xt1, double yt1) {
        if (ys0 > yt1 || yt0 > ys1) {
            return false;
        }
        if (Math.min(xs0, xs1) > Math.max(xt0, xt1) ||
            Math.max(xs0, xs1) < Math.min(xt0, xt1))
        {
            return false;
        }
        // Bounding boxes intersect - back off the larger of
        // the two subcurves by half until they stop intersecting
        // (or until they get small enough to switch to a more
        //  intensive algorithm).
        if (s1 - s0 > TMIN) {
            double s = (s0 + s1) / 2;
            double xs = this.XforT(s);
            double ys = this.YforT(s);
            if (s == s0 || s == s1) {
                throw new InternalError("s0 = " + s0 + "\ns1 = " + s1 + "\nno s progress!");
            }
            if (t1 - t0 > TMIN) {
                double t = (t0 + t1) / 2;
                double xt = that.XforT(t);
                double yt = that.YforT(t);
                if (t == t0 || t == t1) {
                    throw new InternalError("t0 = " + t0 + "\nt1 = " + t1 + "\nno t progress!");
                }
                if (ys >= yt0 && yt >= ys0) {
                    if (findIntersect(that, yrange, ymin, slevel+1, tlevel+1,
                                      s0, xs0, ys0, s, xs, ys,
                                      t0, xt0, yt0, t, xt, yt)) {
                        return true;
                    }
                }
                if (ys >= yt) {
                    if (findIntersect(that, yrange, ymin, slevel+1, tlevel+1,
                                      s0, xs0, ys0, s, xs, ys,
                                      t, xt, yt, t1, xt1, yt1)) {
                        return true;
                    }
                }
                if (yt >= ys) {
                    if (findIntersect(that, yrange, ymin, slevel+1, tlevel+1,
                                      s, xs, ys, s1, xs1, ys1,
                                      t0, xt0, yt0, t, xt, yt)) {
                        return true;
                    }
                }
                if (ys1 >= yt && yt1 >= ys) {
                    if (findIntersect(that, yrange, ymin, slevel+1, tlevel+1,
                                      s, xs, ys, s1, xs1, ys1,
                                      t, xt, yt, t1, xt1, yt1)) {
                        return true;
                    }
                }
            } else {
                if (ys >= yt0) {
                    if (findIntersect(that, yrange, ymin, slevel+1, tlevel,
                                      s0, xs0, ys0, s, xs, ys,
                                      t0, xt0, yt0, t1, xt1, yt1)) {
                        return true;
                    }
                }
                if (yt1 >= ys) {
                    if (findIntersect(that, yrange, ymin, slevel+1, tlevel,
                                      s, xs, ys, s1, xs1, ys1,
                                      t0, xt0, yt0, t1, xt1, yt1)) {
                        return true;
                    }
                }
            }
        } else if (t1 - t0 > TMIN) {
            double t = (t0 + t1) / 2;
            double xt = that.XforT(t);
            double yt = that.YforT(t);
            if (t == t0 || t == t1) {
                throw new InternalError("t0 = " + t0 + "\nt1 = " + t1 + "\nno t progress!");
            }
            if (yt >= ys0) {
                if (findIntersect(that, yrange, ymin, slevel, tlevel+1,
                                  s0, xs0, ys0, s1, xs1, ys1,
                                  t0, xt0, yt0, t, xt, yt)) {
                    return true;
                }
            }
            if (ys1 >= yt) {
                if (findIntersect(that, yrange, ymin, slevel, tlevel+1,
                                  s0, xs0, ys0, s1, xs1, ys1,
                                  t, xt, yt, t1, xt1, yt1)) {
                    return true;
                }
            }
        } else {
            // No more subdivisions
            double xlk = xs1 - xs0;
            double ylk = ys1 - ys0;
            double xnm = xt1 - xt0;
            double ynm = yt1 - yt0;
            double xmk = xt0 - xs0;
            double ymk = yt0 - ys0;
            double det = xnm * ylk - ynm * xlk;
            if (det != 0) {
                double detinv = 1 / det;
                double s = (xnm * ymk - ynm * xmk) * detinv;
                double t = (xlk * ymk - ylk * xmk) * detinv;
                if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
                    s = s0 + s * (s1 - s0);
                    t = t0 + t * (t1 - t0);
                    // assert (s >= 0 && s <= 1 && t >= 0 && t <= 1);
                    double y = (this.YforT(s) + that.YforT(t)) / 2;
                    if (y <= yrange[1] && y > yrange[0]) {
                        yrange[1] = y;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public double refineTforY(double t0, double yt0, double y0) {
        double t1 = 1;
        while (true) {
            double th = (t0 + t1) / 2;
            if (th == t0 || th == t1) {
                return t1;
            }
            double y = YforT(th);
            if (y < y0) {
                t0 = th;
                yt0 = y;
            } else if (y > y0) {
                t1 = th;
            } else {
                return t1;
            }
        }
    }

    public boolean fairlyClose(double v1, double v2) {
        return (Math.abs(v1 - v2) < Math.max(Math.abs(v1), Math.abs(v2)) * 1E-10);
    }

    public abstract int getSegment(float coords[]);
}
