package flat.graphics.svg;

public class Path {
    boolean negative;
    Curve[] curves;
    private Curve[] curvers;

    public Curve[] getCurvers() {
        return curvers;
    }

    public boolean isHole() {
        return negative;
    }

    public static class Curve {
         public float cx1, cy1, cx2, cy2, x, y;
    }
}
