package flat.animations.interpolation;

public class PowOut extends Pow {
    public PowOut (int power) {
        super(power);
    }

    public float apply (float a) {
        return (float)Math.pow(a - 1, power) * (power % 2 == 0 ? -1 : 1) + 1;
    }
}
