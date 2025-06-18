package flat.animations.interpolation;

public class PowIn extends Pow {
    public PowIn (int power) {
        super(power);
    }

    public float apply (float a) {
        return (float)Math.pow(a, power);
    }
}