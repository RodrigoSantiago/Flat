package flat.animations;

import flat.animations.interpolation.*;

public interface Interpolation {

    float apply(float t);

    Interpolation linear = t -> t;

    Interpolation fade = t -> t * t * t * (t * (t * 6 - 15) + 10);

    Interpolation cubic = t -> ((t *= 2) <= 1 ? t * t * t : (t -= 2) * t * t + 2) / 2;

    Interpolation cubicIn = t -> t * t * t;

    Interpolation cubicOut = t -> --t * t * t + 1;

    Interpolation quad = t -> ((t *= 2) <= 1 ? t * t : --t * (2 - t) + 1) / 2;

    Interpolation quadIn = t -> t * t;

    Interpolation quadOut = t -> t * (2 - t);

    Interpolation circle = t -> (float) (((t *= 2) <= 1 ? 1 - Math.sqrt(1 - t * t) : Math.sqrt(1 - (t -= 2) * t) + 1) / 2);

    Interpolation circleIn = t -> 1 - (float) Math.sqrt(1 - t * t);

    Interpolation circleOut = t -> (float) Math.sqrt(1 - (t - 1) * (t - 1));

    Interpolation acc = t -> t * t;

    Interpolation decc = t -> (1.0f - (1.0f - t) * (1.0f - t));

    Interpolation accDecc = t -> (float) (Math.cos((t + 1) * Math.PI) / 2.0f) + 0.5f;

    Interpolation anticipate = t -> t * t * (3.0f * t - t);

    Interpolation overshoot = t -> {
        t -= 1.0f;
        return t * t * (3f * t + 2f) + 1.0f;
    };

    Interpolation bounce = t -> {
        t *= 1.1226f;
        if (t < 0.3535f) {
            return t * t * 8.0f;
        } else if (t < 0.7408f) {
            t = (t - 0.54719f);
            return t * t * 8.0f + 0.7f;
        } else if (t < 0.9644f) {
            t = (t - 0.8526f);
            return t * t * 8.0f + 0.9f;
        } else {
            t = (t - 1.0435f);
            return t * t * 8.0f + 0.95f;
        }
    };

    Interpolation linearOutSlowIn = t -> {
        int index = Math.min((int) (t * (AnimationKernel.kernel1.length - 1)), AnimationKernel.kernel1.length - 2);
        float part = 1f / (AnimationKernel.kernel1.length - 1);
        return AnimationKernel.kernel1[index] + ((t - (index * part)) / part) * (AnimationKernel.kernel1[index + 1] - AnimationKernel.kernel1[index]);
    };

    Interpolation fastOutSlowIn = t -> {
        int index = Math.min((int) (t * (AnimationKernel.kernel2.length - 1)), AnimationKernel.kernel2.length - 2);
        float part = 1f / (AnimationKernel.kernel2.length - 1);
        return AnimationKernel.kernel2[index] + ((t - (index * part)) / part) * (AnimationKernel.kernel2[index + 1] - AnimationKernel.kernel2[index]);
    };

    Interpolation fastOutLinearIn = t -> {
        int index = Math.min((int) (t * (AnimationKernel.kernel3.length - 1)), AnimationKernel.kernel3.length - 2);
        float part = 1f / (AnimationKernel.kernel3.length - 1);
        return AnimationKernel.kernel3[index] + ((t - (index * part)) / part) * (AnimationKernel.kernel3[index + 1] - AnimationKernel.kernel3[index]);
    };

    Interpolation exp10 = new Exp(2, 10);
    Interpolation exp10In = new ExpIn(2, 10);
    Interpolation exp10Out = new ExpOut(2, 10);

    Interpolation exp8 = new Exp(2, 8);
    Interpolation exp8In = new ExpIn(2, 8);
    Interpolation exp8Out = new ExpOut(2, 8);

    Interpolation exp5 = new Exp(2, 5);
    Interpolation exp5In = new ExpIn(2, 5);
    Interpolation exp5Out = new ExpOut(2, 5);

    Interpolation pow2 = new Pow(2);
    Interpolation pow2In = new PowIn(2);
    Interpolation pow2Out = new PowOut(2);

    Interpolation pow3 = new Pow(3);
    Interpolation pow3In = new PowIn(3);
    Interpolation pow3Out = new PowOut(3);

    Interpolation pow4 = new Pow(4);
    Interpolation pow4In = new PowIn(4);
    Interpolation pow4Out = new PowOut(4);

    static float mix(float a, float b, float t) {
        return a * (1 - t) + b * t;
    }

    static float mixAngle(float a, float b, float t) {
        float difference = Math.abs(b - a);
        if (difference > 180) {
            if (b > a) {
                a += 360;
            } else {
                b += 360;
            }
        }

        float value = (a + ((b - a) * t));

        if (value >= 0 && value <= 360) {
            return value;
        } else {
            return (value % 360);
        }
    }
}
