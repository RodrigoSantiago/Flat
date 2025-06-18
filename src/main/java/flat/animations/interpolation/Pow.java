package flat.animations.interpolation;

import flat.animations.Interpolation;

public class Pow implements Interpolation {
    final int power;

    public Pow (int power) {
        this.power = power;
    }

    public float apply (float a) {
        if (a <= 0.5f) return (float)Math.pow(a * 2, power) / 2;
        return (float)Math.pow((a - 1) * 2, power) / (power % 2 == 0 ? -2 : 2) + 1;
    }
}