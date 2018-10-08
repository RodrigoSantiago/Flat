package flat.animations;

import flat.widget.Application;

public interface Animation {

    boolean isPlaying();

    void handle(long milis);

    static float mix(float a, float b, float t) {
        return a * (1 - t) + b * t;
    }

    static int mixColor(int a, int b, float t) {
        float rr = ((a >> 24) & 0xff) / 255f;
        float rg = ((a >> 16) & 0xff) / 255f;
        float rb = ((a >> 8) & 0xff) / 255f;
        float ra = (a & 0xff) / 255f;

        int sr = Math.min(255, Math.round(mix(rr, (((b >> 24) & 0xff) / 255f), t) * 255));
        int sg = Math.min(255, Math.round(mix(rg, (((b >> 16) & 0xff) / 255f), t) * 255));
        int sb = Math.min(255, Math.round(mix(rb, (((b >> 8) & 0xff) / 255f), t) * 255));
        int sa = Math.min(255, Math.round(mix(ra, ((b & 0xff) / 255f), t) * 255));

        return (sr << 24) | (sg << 16) | (sb << 8) | sa;
    }
}
