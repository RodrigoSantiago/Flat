package flat.animations.interpolation;

public class ExpIn extends Exp {
    public ExpIn(float value, float power) {
        super(value, power);
    }

    public float apply(float a) {
        return ((float) Math.pow(value, power * (a - 1)) - min) * scale;
    }
}
