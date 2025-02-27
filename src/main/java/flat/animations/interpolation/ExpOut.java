package flat.animations.interpolation;

public class ExpOut extends Exp {
    public ExpOut(float value, float power) {
        super(value, power);
    }

    public float apply(float a) {
        return 1 - ((float) Math.pow(value, -power * a) - min) * scale;
    }
}
