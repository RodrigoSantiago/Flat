package flat.math.operations;

final class Edge {

    private final Curve curve;

    private int ctag;
    private int etag;
    private double activey;
    private int equivalence;

    private Edge lastEdge;
    private int lastResult;
    private double lastLimit;

    public Edge(Curve c, int ctag) {
        this.curve = c;
        this.ctag = ctag;
        this.etag = AreaOp.ETAG_IGNORE;
    }

    public Curve getCurve() {
        return curve;
    }

    public int getCurveTag() {
        return ctag;
    }

    public int getEquivalence() {
        return equivalence;
    }

    public void setEquivalence(int eq) {
        equivalence = eq;
    }

    public int compareTo(Edge other, double yrange[]) {
        if (other == lastEdge && yrange[0] < lastLimit) {
            if (yrange[1] > lastLimit) {
                yrange[1] = lastLimit;
            }
            return lastResult;
        }
        if (this == other.lastEdge && yrange[0] < other.lastLimit) {
            if (yrange[1] > other.lastLimit) {
                yrange[1] = other.lastLimit;
            }
            return 0 - other.lastResult;
        }
        int ret = curve.compareTo(other.curve, yrange);
        lastEdge = other;
        lastLimit = yrange[1];
        lastResult = ret;
        return ret;
    }

    public void record(double yend, int etag) {
        this.activey = yend;
        this.etag = etag;
    }

    public boolean isActiveFor(double y, int etag) {
        return (this.etag == etag && this.activey >= y);
    }
}
