package flat.image;

public class Svg {
    Node[] nodes;

    public static class Node {
        boolean hole;
        Curve[] curves;
    }

    public static class Curve {
        float x1, y1, x2, y2, rx, ry, lx, ly;
    }
}
