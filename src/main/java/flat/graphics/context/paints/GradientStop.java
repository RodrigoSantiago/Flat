package flat.graphics.context.paints;

public class GradientStop {
    private final float step;
    private final int color;

    public GradientStop(float step, int color) {
        this.step = step;
        this.color = color;
    }

    public float getStep() {
        return step;
    }

    public int getColor() {
        return color;
    }
}
