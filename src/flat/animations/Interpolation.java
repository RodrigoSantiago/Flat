package flat.animations;

public abstract class Interpolation {
    public static final Interpolation linear = new Interpolation() {
        @Override
        public final float apply(float t) {
            return t;
        }
    };

    public static final Interpolation fade = new Interpolation() {
        @Override
        public final float apply(float t) {
            return t * t * t * (t * (t * 6 - 15) + 10);
        }
    };

    public static final Interpolation cubic = new Interpolation() {
        @Override
        public float apply(float t) {
            return ((t *= 2) <= 1 ? t * t * t : (t -= 2) * t * t + 2) / 2;
        }
    };

    public static final Interpolation cubicIn = new Interpolation() {
        @Override
        public float apply(float t) {
            return t * t * t;
        }
    };

    public static final Interpolation cubicOut = new Interpolation() {
        @Override
        public float apply(float t) {
            return --t * t * t + 1;
        }
    };

    public static final Interpolation quad = new Interpolation() {
        @Override
        public final float apply(float t) {
            return ((t *= 2) <= 1 ? t * t : --t * (2 - t) + 1) / 2;
        }
    };

    public static final Interpolation quadIn = new Interpolation() {
        @Override
        public final float apply(float t) {
            return t * t;
        }
    };

    public static final Interpolation quadOut = new Interpolation() {
        @Override
        public final float apply(float t) {
            return t * (2 - t);
        }
    };

    public static final Interpolation circle = new Interpolation() {
        @Override
        public final float apply(float t) {
            return (float) (((t *= 2) <= 1 ? 1 - Math.sqrt(1 - t * t) : Math.sqrt(1 - (t -= 2) * t) + 1) / 2);
        }
    };

    public static final Interpolation circleIn = new Interpolation() {
        @Override
        public final float apply(float t) {
            return 1 - (float) Math.sqrt(1 - t * t);
        }
    };

    public static final Interpolation circleOut = new Interpolation() {
        @Override
        public final float apply(float t) {
            return (float) Math.sqrt(1 - (t - 1) * (t - 1));
        }
    };

    public static final Interpolation acc = new Interpolation() {
        @Override
        public float apply(float t) {
            return t * t;
        }
    };

    public static final Interpolation decc = new Interpolation() {
        @Override
        public float apply(float t) {
            return (1.0f - (1.0f - t) * (1.0f - t));
        }
    };

    public static final Interpolation accDecc = new Interpolation() {
        @Override
        public float apply(float t) {
            return (float) (Math.cos((t + 1) * Math.PI) / 2.0f) + 0.5f;
        }
    };

    public static final Interpolation anticipate = new Interpolation() {
        @Override
        public float apply(float t) {
            return t * t * (3.0f * t - t);
        }
    };

    public static final Interpolation overshoot = new Interpolation() {
        @Override
        public float apply(float t) {
            t -= 1.0f;
            return t * t * (3f * t + 2f) + 1.0f;
        }
    };

    public static final Interpolation bounce = new Interpolation() {
        @Override
        public float apply(float t) {
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
        }
    };

    public static final Interpolation linearOutSlowIn = new Interpolation() {
        @Override
        public float apply(float t) {
            int index = Math.min((int) (t * (kernel1.length - 1)), kernel1.length - 2);
            float part = 1f / (kernel1.length - 1);
            return kernel1[index] + ((t - (index * part)) / part) * (kernel1[index + 1] - kernel1[index]);
        }
    };

    public static final Interpolation fastOutSlowIn = new Interpolation() {
        @Override
        public float apply(float t) {
            int index = Math.min((int) (t * (kernel2.length - 1)), kernel2.length - 2);
            float part = 1f / (kernel2.length - 1);
            return kernel2[index] + ((t - (index * part)) / part) * (kernel2[index + 1] - kernel2[index]);
        }
    };

    public static final Interpolation fastOutLinearIn = new Interpolation() {
        @Override
        public float apply(float t) {
            int index = Math.min((int) (t * (kernel3.length - 1)), kernel3.length - 2);
            float part = 1f / (kernel3.length - 1);
            return kernel3[index] + ((t - (index * part)) / part) * (kernel3[index + 1] - kernel3[index]);
        }
    };

    private Interpolation() {
    }

    public abstract float apply(float t);

    private static float[] kernel1 = new float[] { 0.0000f, 0.0222f,
            0.0424f, 0.0613f, 0.0793f, 0.0966f, 0.1132f, 0.1293f, 0.1449f,
            0.1600f, 0.1747f, 0.1890f, 0.2029f, 0.2165f, 0.2298f, 0.2428f,
            0.2555f, 0.2680f, 0.2802f, 0.2921f, 0.3038f, 0.3153f, 0.3266f,
            0.3377f, 0.3486f, 0.3592f, 0.3697f, 0.3801f, 0.3902f, 0.4002f,
            0.4100f, 0.4196f, 0.4291f, 0.4385f, 0.4477f, 0.4567f, 0.4656f,
            0.4744f, 0.4831f, 0.4916f, 0.5000f, 0.5083f, 0.5164f, 0.5245f,
            0.5324f, 0.5402f, 0.5479f, 0.5555f, 0.5629f, 0.5703f, 0.5776f,
            0.5847f, 0.5918f, 0.5988f, 0.6057f, 0.6124f, 0.6191f, 0.6257f,
            0.6322f, 0.6387f, 0.6450f, 0.6512f, 0.6574f, 0.6635f, 0.6695f,
            0.6754f, 0.6812f, 0.6870f, 0.6927f, 0.6983f, 0.7038f, 0.7093f,
            0.7147f, 0.7200f, 0.7252f, 0.7304f, 0.7355f, 0.7406f, 0.7455f,
            0.7504f, 0.7553f, 0.7600f, 0.7647f, 0.7694f, 0.7740f, 0.7785f,
            0.7829f, 0.7873f, 0.7917f, 0.7959f, 0.8002f, 0.8043f, 0.8084f,
            0.8125f, 0.8165f, 0.8204f, 0.8243f, 0.8281f, 0.8319f, 0.8356f,
            0.8392f, 0.8429f, 0.8464f, 0.8499f, 0.8534f, 0.8568f, 0.8601f,
            0.8634f, 0.8667f, 0.8699f, 0.8731f, 0.8762f, 0.8792f, 0.8823f,
            0.8852f, 0.8882f, 0.8910f, 0.8939f, 0.8967f, 0.8994f, 0.9021f,
            0.9048f, 0.9074f, 0.9100f, 0.9125f, 0.9150f, 0.9174f, 0.9198f,
            0.9222f, 0.9245f, 0.9268f, 0.9290f, 0.9312f, 0.9334f, 0.9355f,
            0.9376f, 0.9396f, 0.9416f, 0.9436f, 0.9455f, 0.9474f, 0.9492f,
            0.9510f, 0.9528f, 0.9545f, 0.9562f, 0.9579f, 0.9595f, 0.9611f,
            0.9627f, 0.9642f, 0.9657f, 0.9672f, 0.9686f, 0.9700f, 0.9713f,
            0.9726f, 0.9739f, 0.9752f, 0.9764f, 0.9776f, 0.9787f, 0.9798f,
            0.9809f, 0.9820f, 0.9830f, 0.9840f, 0.9849f, 0.9859f, 0.9868f,
            0.9876f, 0.9885f, 0.9893f, 0.9900f, 0.9908f, 0.9915f, 0.9922f,
            0.9928f, 0.9934f, 0.9940f, 0.9946f, 0.9951f, 0.9956f, 0.9961f,
            0.9966f, 0.9970f, 0.9974f, 0.9977f, 0.9981f, 0.9984f, 0.9987f,
            0.9989f, 0.9992f, 0.9994f, 0.9995f, 0.9997f, 0.9998f, 0.9999f,
            0.9999f, 1.0000f, 1.0000f };
    private static float[] kernel2 = new float[] { 0.0000f, 0.0001f,
            0.0002f, 0.0005f, 0.0009f, 0.0014f, 0.0020f, 0.0027f, 0.0036f,
            0.0046f, 0.0058f, 0.0071f, 0.0085f, 0.0101f, 0.0118f, 0.0137f,
            0.0158f, 0.0180f, 0.0205f, 0.0231f, 0.0259f, 0.0289f, 0.0321f,
            0.0355f, 0.0391f, 0.0430f, 0.0471f, 0.0514f, 0.0560f, 0.0608f,
            0.0660f, 0.0714f, 0.0771f, 0.0830f, 0.0893f, 0.0959f, 0.1029f,
            0.1101f, 0.1177f, 0.1257f, 0.1339f, 0.1426f, 0.1516f, 0.1610f,
            0.1707f, 0.1808f, 0.1913f, 0.2021f, 0.2133f, 0.2248f, 0.2366f,
            0.2487f, 0.2611f, 0.2738f, 0.2867f, 0.2998f, 0.3131f, 0.3265f,
            0.3400f, 0.3536f, 0.3673f, 0.3810f, 0.3946f, 0.4082f, 0.4217f,
            0.4352f, 0.4485f, 0.4616f, 0.4746f, 0.4874f, 0.5000f, 0.5124f,
            0.5246f, 0.5365f, 0.5482f, 0.5597f, 0.5710f, 0.5820f, 0.5928f,
            0.6033f, 0.6136f, 0.6237f, 0.6335f, 0.6431f, 0.6525f, 0.6616f,
            0.6706f, 0.6793f, 0.6878f, 0.6961f, 0.7043f, 0.7122f, 0.7199f,
            0.7275f, 0.7349f, 0.7421f, 0.7491f, 0.7559f, 0.7626f, 0.7692f,
            0.7756f, 0.7818f, 0.7879f, 0.7938f, 0.7996f, 0.8053f, 0.8108f,
            0.8162f, 0.8215f, 0.8266f, 0.8317f, 0.8366f, 0.8414f, 0.8461f,
            0.8507f, 0.8551f, 0.8595f, 0.8638f, 0.8679f, 0.8720f, 0.8760f,
            0.8798f, 0.8836f, 0.8873f, 0.8909f, 0.8945f, 0.8979f, 0.9013f,
            0.9046f, 0.9078f, 0.9109f, 0.9139f, 0.9169f, 0.9198f, 0.9227f,
            0.9254f, 0.9281f, 0.9307f, 0.9333f, 0.9358f, 0.9382f, 0.9406f,
            0.9429f, 0.9452f, 0.9474f, 0.9495f, 0.9516f, 0.9536f, 0.9556f,
            0.9575f, 0.9594f, 0.9612f, 0.9629f, 0.9646f, 0.9663f, 0.9679f,
            0.9695f, 0.9710f, 0.9725f, 0.9739f, 0.9753f, 0.9766f, 0.9779f,
            0.9791f, 0.9803f, 0.9815f, 0.9826f, 0.9837f, 0.9848f, 0.9858f,
            0.9867f, 0.9877f, 0.9885f, 0.9894f, 0.9902f, 0.9910f, 0.9917f,
            0.9924f, 0.9931f, 0.9937f, 0.9944f, 0.9949f, 0.9955f, 0.9960f,
            0.9964f, 0.9969f, 0.9973f, 0.9977f, 0.9980f, 0.9984f, 0.9986f,
            0.9989f, 0.9991f, 0.9993f, 0.9995f, 0.9997f, 0.9998f, 0.9999f,
            0.9999f, 1.0000f, 1.0000f };
    private static float[] kernel3 = new float[] { 0.0000f, 0.0001f,
            0.0002f, 0.0005f, 0.0008f, 0.0013f, 0.0018f, 0.0024f, 0.0032f,
            0.0040f, 0.0049f, 0.0059f, 0.0069f, 0.0081f, 0.0093f, 0.0106f,
            0.0120f, 0.0135f, 0.0151f, 0.0167f, 0.0184f, 0.0201f, 0.0220f,
            0.0239f, 0.0259f, 0.0279f, 0.0300f, 0.0322f, 0.0345f, 0.0368f,
            0.0391f, 0.0416f, 0.0441f, 0.0466f, 0.0492f, 0.0519f, 0.0547f,
            0.0574f, 0.0603f, 0.0632f, 0.0662f, 0.0692f, 0.0722f, 0.0754f,
            0.0785f, 0.0817f, 0.0850f, 0.0884f, 0.0917f, 0.0952f, 0.0986f,
            0.1021f, 0.1057f, 0.1093f, 0.1130f, 0.1167f, 0.1205f, 0.1243f,
            0.1281f, 0.1320f, 0.1359f, 0.1399f, 0.1439f, 0.1480f, 0.1521f,
            0.1562f, 0.1604f, 0.1647f, 0.1689f, 0.1732f, 0.1776f, 0.1820f,
            0.1864f, 0.1909f, 0.1954f, 0.1999f, 0.2045f, 0.2091f, 0.2138f,
            0.2184f, 0.2232f, 0.2279f, 0.2327f, 0.2376f, 0.2424f, 0.2473f,
            0.2523f, 0.2572f, 0.2622f, 0.2673f, 0.2723f, 0.2774f, 0.2826f,
            0.2877f, 0.2929f, 0.2982f, 0.3034f, 0.3087f, 0.3141f, 0.3194f,
            0.3248f, 0.3302f, 0.3357f, 0.3412f, 0.3467f, 0.3522f, 0.3578f,
            0.3634f, 0.3690f, 0.3747f, 0.3804f, 0.3861f, 0.3918f, 0.3976f,
            0.4034f, 0.4092f, 0.4151f, 0.4210f, 0.4269f, 0.4329f, 0.4388f,
            0.4448f, 0.4508f, 0.4569f, 0.4630f, 0.4691f, 0.4752f, 0.4814f,
            0.4876f, 0.4938f, 0.5000f, 0.5063f, 0.5126f, 0.5189f, 0.5252f,
            0.5316f, 0.5380f, 0.5444f, 0.5508f, 0.5573f, 0.5638f, 0.5703f,
            0.5768f, 0.5834f, 0.5900f, 0.5966f, 0.6033f, 0.6099f, 0.6166f,
            0.6233f, 0.6301f, 0.6369f, 0.6436f, 0.6505f, 0.6573f, 0.6642f,
            0.6710f, 0.6780f, 0.6849f, 0.6919f, 0.6988f, 0.7059f, 0.7129f,
            0.7199f, 0.7270f, 0.7341f, 0.7413f, 0.7484f, 0.7556f, 0.7628f,
            0.7700f, 0.7773f, 0.7846f, 0.7919f, 0.7992f, 0.8066f, 0.8140f,
            0.8214f, 0.8288f, 0.8363f, 0.8437f, 0.8513f, 0.8588f, 0.8664f,
            0.8740f, 0.8816f, 0.8892f, 0.8969f, 0.9046f, 0.9124f, 0.9201f,
            0.9280f, 0.9358f, 0.9437f, 0.9516f, 0.9595f, 0.9675f, 0.9755f,
            0.9836f, 0.9918f, 1.0000f };
}
