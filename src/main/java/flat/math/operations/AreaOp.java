package flat.math.operations;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

public abstract class AreaOp {
    public static abstract class CAGOp extends AreaOp {
        boolean inLeft;
        boolean inRight;
        boolean inResult;

        public void newRow() {
            inLeft = false;
            inRight = false;
            inResult = false;
        }

        public int classify(Edge e) {
            if (e.getCurveTag() == CTAG_LEFT) {
                inLeft = !inLeft;
            } else {
                inRight = !inRight;
            }
            boolean newClass = newClassification(inLeft, inRight);
            if (inResult == newClass) {
                return ETAG_IGNORE;
            }
            inResult = newClass;
            return (newClass ? ETAG_ENTER : ETAG_EXIT);
        }

        public int getState() {
            return (inResult ? RSTAG_INSIDE : RSTAG_OUTSIDE);
        }

        public abstract boolean newClassification(boolean inLeft, boolean inRight);
    }

    public static final class AddOp extends CAGOp {
        public boolean newClassification(boolean inLeft, boolean inRight) {
            return (inLeft || inRight);
        }
    }

    public static final class SubOp extends CAGOp {
        public boolean newClassification(boolean inLeft, boolean inRight) {
            return (inLeft && !inRight);
        }
    }

    public static final class IntOp extends CAGOp {
        public boolean newClassification(boolean inLeft, boolean inRight) {
            return (inLeft && inRight);
        }
    }

    public static final class XorOp extends CAGOp {
        public boolean newClassification(boolean inLeft, boolean inRight) {
            return (inLeft != inRight);
        }
    }

    public static final class NZWindOp extends AreaOp {
        private int count;

        public void newRow() {
            count = 0;
        }

        public int classify(Edge e) {
            // Note: the right curves should be an empty set with this op...
            // assert(e.getCurveTag() == CTAG_LEFT);
            int newCount = count;
            int type = (newCount == 0 ? ETAG_ENTER : ETAG_IGNORE);
            newCount += e.getCurve().getDirection();
            count = newCount;
            return (newCount == 0 ? ETAG_EXIT : type);
        }

        public int getState() {
            return ((count == 0) ? RSTAG_OUTSIDE : RSTAG_INSIDE);
        }
    }

    public static final class EOWindOp extends AreaOp {
        private boolean inside;

        public void newRow() {
            inside = false;
        }

        public int classify(Edge e) {
            // Note: the right curves should be an empty set with this op...
            // assert(e.getCurveTag() == CTAG_LEFT);
            boolean newInside = !inside;
            inside = newInside;
            return (newInside ? ETAG_ENTER : ETAG_EXIT);
        }

        public int getState() {
            return (inside ? RSTAG_INSIDE : RSTAG_OUTSIDE);
        }
    }

    private AreaOp() {
    }

    /* Constants to tag the left and right curves in the edge list */
    public static final int CTAG_LEFT = 0;
    public static final int CTAG_RIGHT = 1;

    /* Constants to classify edges */
    public static final int ETAG_IGNORE = 0;
    public static final int ETAG_ENTER = 1;
    public static final int ETAG_EXIT = -1;

    /* Constants used to classify result state */
    public static final int RSTAG_INSIDE = 1;
    public static final int RSTAG_OUTSIDE = -1;

    public abstract void newRow();

    public abstract int classify(Edge e);

    public abstract int getState();

    public Vector<Curve> calculate(Vector<Curve> left, Vector<Curve> right) {
        Vector<Edge> edges = new Vector<>();
        addEdges(edges, left, AreaOp.CTAG_LEFT);
        addEdges(edges, right, AreaOp.CTAG_RIGHT);
        return pruneEdges(edges);
    }

    private static void addEdges(Vector<Edge> edges, Vector<Curve> curves, int curvetag) {
        Enumeration<Curve> elements = curves.elements();
        while (elements.hasMoreElements()) {
            Curve c = elements.nextElement();
            if (c.getOrder() > 0) {
                edges.add(new Edge(c, curvetag));
            }
        }
    }

    private static Comparator<Edge> YXTopComparator = (o1, o2) -> {
        Curve c1 = o1.getCurve();
        Curve c2 = o2.getCurve();
        double v1, v2;
        if ((v1 = c1.getYTop()) == (v2 = c2.getYTop())) {
            if ((v1 = c1.getXTop()) == (v2 = c2.getXTop())) {
                return 0;
            }
        }
        if (v1 < v2) {
            return -1;
        }
        return 1;
    };

    private Vector<Curve> pruneEdges(Vector<Edge> edges) {
        int numedges = edges.size();
        if (numedges < 2) {
            return new Vector<>();
        }
        Edge[] edgelist = edges.toArray(new Edge[numedges]);
        Arrays.sort(edgelist, YXTopComparator);
        Edge e;
        int left = 0;
        int right = 0;
        int cur, next;
        double yrange[] = new double[2];
        Vector<CurveLink> subcurves = new Vector<>();
        Vector<ChainEnd> chains = new Vector<>();
        Vector<CurveLink> links = new Vector<>();
        // Active edges are between left (inclusive) and right (exclusive)
        while (left < numedges) {
            double y = yrange[0];
            // Prune active edges that fall off the top of the active y range
            for (cur = next = right - 1; cur >= left; cur--) {
                e = edgelist[cur];
                if (e.getCurve().getYBot() > y) {
                    if (next > cur) {
                        edgelist[next] = e;
                    }
                    next--;
                }
            }
            left = next + 1;
            // Grab a new "top of Y range" if the active edges are empty
            if (left >= right) {
                if (right >= numedges) {
                    break;
                }
                y = edgelist[right].getCurve().getYTop();
                if (y > yrange[0]) {
                    finalizeSubCurves(subcurves, chains);
                }
                yrange[0] = y;
            }
            // Incorporate new active edges that enter the active y range
            while (right < numedges) {
                e = edgelist[right];
                if (e.getCurve().getYTop() > y) {
                    break;
                }
                right++;
            }
            // Sort the current active edges by their X values and
            // determine the maximum valid Y range where the X ordering
            // is correct
            yrange[1] = edgelist[left].getCurve().getYBot();
            if (right < numedges) {
                y = edgelist[right].getCurve().getYTop();
                if (yrange[1] > y) {
                    yrange[1] = y;
                }
            }
            // Note: We could start at left+1, but we need to make
            // sure that edgelist[left] has its equivalence set to 0.
            int nexteq = 1;
            for (cur = left; cur < right; cur++) {
                e = edgelist[cur];
                e.setEquivalence(0);
                for (next = cur; next > left; next--) {
                    Edge prevedge = edgelist[next - 1];
                    int ordering = e.compareTo(prevedge, yrange);
                    if (yrange[1] <= yrange[0]) {
                        throw new InternalError("backstepping to " + yrange[1] + " from " + yrange[0]);
                    }
                    if (ordering >= 0) {
                        if (ordering == 0) {
                            // If the curves are equal, mark them to be
                            // deleted later if they cancel each other
                            // out so that we avoid having extraneous
                            // curve segments.
                            int eq = prevedge.getEquivalence();
                            if (eq == 0) {
                                eq = nexteq++;
                                prevedge.setEquivalence(eq);
                            }
                            e.setEquivalence(eq);
                        }
                        break;
                    }
                    edgelist[next] = prevedge;
                }
                edgelist[next] = e;
            }
            // Now prune the active edge list.
            // For each edge in the list, determine its classification
            // (entering shape, exiting shape, ignore - no change) and
            // record the current Y range and its classification in the
            // Edge object for use later in constructing the new outline.
            newRow();
            double ystart = yrange[0];
            double yend = yrange[1];
            for (cur = left; cur < right; cur++) {
                e = edgelist[cur];
                int etag;
                int eq = e.getEquivalence();
                if (eq != 0) {
                    // Find one of the segments in the "equal" range
                    // with the right transition state and prefer an
                    // edge that was either active up until ystart
                    // or the edge that extends the furthest downward
                    // (i.e. has the most potential for continuation)
                    int origstate = getState();
                    etag = (origstate == AreaOp.RSTAG_INSIDE
                            ? AreaOp.ETAG_EXIT
                            : AreaOp.ETAG_ENTER);
                    Edge activematch = null;
                    Edge longestmatch = e;
                    double furthesty = yend;
                    do {
                        // Note: classify() must be called
                        // on every edge we consume here.
                        classify(e);
                        if (activematch == null && e.isActiveFor(ystart, etag)) {
                            activematch = e;
                        }
                        y = e.getCurve().getYBot();
                        if (y > furthesty) {
                            longestmatch = e;
                            furthesty = y;
                        }
                    } while (++cur < right && (e = edgelist[cur]).getEquivalence() == eq);
                    --cur;
                    if (getState() == origstate) {
                        etag = AreaOp.ETAG_IGNORE;
                    } else {
                        e = (activematch != null ? activematch : longestmatch);
                    }
                } else {
                    etag = classify(e);
                }
                if (etag != AreaOp.ETAG_IGNORE) {
                    e.record(yend, etag);
                    links.add(new CurveLink(e.getCurve(), ystart, yend, etag));
                }
            }
            // assert(getState() == AreaOp.RSTAG_OUTSIDE);
            resolveLinks(subcurves, chains, links);
            links.clear();
            // Finally capture the bottom of the valid Y range as the top
            // of the next Y range.
            yrange[0] = yend;
        }
        finalizeSubCurves(subcurves, chains);
        Vector<Curve> ret = new Vector<>();
        Enumeration<CurveLink> elements = subcurves.elements();
        while (elements.hasMoreElements()) {
            CurveLink link = elements.nextElement();
            ret.add(link.getMoveto());
            CurveLink nextlink = link;
            while ((nextlink = nextlink.getNext()) != null) {
                if (!link.absorb(nextlink)) {
                    ret.add(link.getSubCurve());
                    link = nextlink;
                }
            }
            ret.add(link.getSubCurve());
        }
        return ret;
    }

    public static void finalizeSubCurves(Vector<CurveLink> subcurves, Vector<ChainEnd> chains) {
        int numchains = chains.size();
        if (numchains == 0) {
            return;
        }
        if ((numchains & 1) != 0) {
            throw new InternalError("Odd number of chains!");
        }
        ChainEnd[] endlist = new ChainEnd[numchains];
        chains.toArray(endlist);
        for (int i = 1; i < numchains; i += 2) {
            ChainEnd open = endlist[i - 1];
            ChainEnd close = endlist[i];
            CurveLink subcurve = open.linkTo(close);
            if (subcurve != null) {
                subcurves.add(subcurve);
            }
        }
        chains.clear();
    }

    private static CurveLink[] EmptyLinkList = new CurveLink[2];
    private static ChainEnd[] EmptyChainList = new ChainEnd[2];

    public static void resolveLinks(Vector<CurveLink> subcurves, Vector<ChainEnd> chains, Vector<CurveLink> links) {
        int numlinks = links.size();
        CurveLink[] linklist;
        if (numlinks == 0) {
            linklist = EmptyLinkList;
        } else {
            if ((numlinks & 1) != 0) {
                throw new InternalError("Odd number of new curves!");
            }
            linklist = new CurveLink[numlinks + 2];
            links.toArray(linklist);
        }
        int numchains = chains.size();
        ChainEnd[] endlist;
        if (numchains == 0) {
            endlist = EmptyChainList;
        } else {
            if ((numchains & 1) != 0) {
                throw new InternalError("Odd number of chains!");
            }
            endlist = new ChainEnd[numchains + 2];
            chains.toArray(endlist);
        }
        int curchain = 0;
        int curlink = 0;
        chains.clear();
        ChainEnd chain = endlist[0];
        ChainEnd nextchain = endlist[1];
        CurveLink link = linklist[0];
        CurveLink nextlink = linklist[1];
        while (chain != null || link != null) {
            /*
             * Strategy 1:
             * Connect chains or links if they are the only things left...
             */
            boolean connectchains = (link == null);
            boolean connectlinks = (chain == null);

            if (!connectchains && !connectlinks) {
                // assert(link != null && chain != null);
                /*
                 * Strategy 2:
                 * Connect chains or links if they close off an open area...
                 */
                connectchains = ((curchain & 1) == 0 &&
                        chain.getX() == nextchain.getX());
                connectlinks = ((curlink & 1) == 0 &&
                        link.getX() == nextlink.getX());

                if (!connectchains && !connectlinks) {
                    /*
                     * Strategy 3:
                     * Connect chains or links if their successor is
                     * between them and their potential connectee...
                     */
                    double cx = chain.getX();
                    double lx = link.getX();
                    connectchains =
                            (nextchain != null && cx < lx &&
                                    obstructs(nextchain.getX(), lx, curchain));
                    connectlinks =
                            (nextlink != null && lx < cx &&
                                    obstructs(nextlink.getX(), cx, curlink));
                }
            }
            if (connectchains) {
                CurveLink subcurve = chain.linkTo(nextchain);
                if (subcurve != null) {
                    subcurves.add(subcurve);
                }
                curchain += 2;
                chain = endlist[curchain];
                nextchain = endlist[curchain + 1];
            }
            if (connectlinks) {
                ChainEnd openend = new ChainEnd(link, null);
                ChainEnd closeend = new ChainEnd(nextlink, openend);
                openend.setOtherEnd(closeend);
                chains.add(openend);
                chains.add(closeend);
                curlink += 2;
                link = linklist[curlink];
                nextlink = linklist[curlink + 1];
            }
            if (!connectchains && !connectlinks) {
                // assert(link != null);
                // assert(chain != null);
                // assert(chain.getEtag() == link.getEtag());
                chain.addLink(link);
                chains.add(chain);
                curchain++;
                chain = nextchain;
                nextchain = endlist[curchain + 1];
                curlink++;
                link = nextlink;
                nextlink = linklist[curlink + 1];
            }
        }
        // assert ((chains.size() & 1) == 0);
    }

    /*
     * Does the position of the next edge at v1 "obstruct" the
     * connectivity between current edge and the potential
     * partner edge which is positioned at v2?
     *
     * Phase tells us whether we are testing for a transition
     * into or out of the interior part of the resulting area.
     *
     * Require 4-connected continuity if this edge and the partner
     * edge are both "entering into" type edges
     * Allow 8-connected continuity for "exiting from" type edges
     */
    public static boolean obstructs(double v1, double v2, int phase) {
        return (((phase & 1) == 0) ? (v1 <= v2) : (v1 < v2));
    }
}