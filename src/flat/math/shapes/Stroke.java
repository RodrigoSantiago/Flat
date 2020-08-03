package flat.math.shapes;

public interface Stroke {

    float getLineWidth();

    int getEndCap();

    int getLineJoin();

    float getMiterLimit();

    Shape createStrokedShape(Shape s);

    float getDashPhase();

    float[] getDashArray();
}
