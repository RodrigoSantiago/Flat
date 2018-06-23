package flat.animations;

import flat.widget.Application;

public abstract class Animation {

    private float delta = 1f;

    private boolean playing;
    private boolean paused;
    private long lastTime;
    private int loops;
    private Interpolation interpolation = Interpolation.linear;

    private long duration;
    private long _duration;
    private long _reaming;
    private int _loops;
    private Interpolation _interpolation;

    private Runnable onStop;

    public Interpolation getInterpolation() {
        return interpolation;
    }

    public void setInterpolation(Interpolation interpolation) {
        this.interpolation = interpolation == null ? Interpolation.linear : interpolation;
    }

    public Runnable getOnStop() {
        return onStop;
    }

    public void setOnStop(Runnable onStop) {
        this.onStop = onStop;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long milis) {
        this.duration = milis;
    }

    public int getLoops() {
        return loops;
    }

    public void setLoops(int loops) {
        this.loops = loops;
    }

    public float getDelta() {
        return delta;
    }

    public void setDelta(float delta) {
        this.delta = delta;
    }

    public void play() {
        play(0);
    }

    public void play(float position) {
        position = Math.max(Math.min(position, 1), 0);
        if (isPlaying()) {
            stop();
        }

        if (paused) {
            playing = true;
            paused = false;
            lastTime = -1;
        } else {
            evaluate();
            lastTime = -1;
            playing = true;
            paused = false;
        }
        _reaming = (long) (_duration * (1 - position));

        Application.runAnimation(this);
    }

    public void pause() {
        if (playing) {
            playing = false;
            paused = true;
        }
    }

    public void stop() {
        if (playing || paused) {
            playing = false;
            paused = false;
            if (onStop != null) onStop.run();
        }
    }

    public void stop(boolean performEnd) {
        if (performEnd && (playing || paused)) {
            compute(_interpolation.apply(1));
        }
        stop();
    }

    public boolean isPlaying() {
        return playing && !paused;
    }

    public boolean isPaused() {
        return !playing && paused;
    }

    public boolean isStopped() {
        return !playing && !paused;
    }

    public void jump(float position) {
        if (playing || paused) {
            position = Math.max(Math.min(position, 1), 0);
            _reaming = (long) (_duration * (1 - position));
        }
    }

    public float getT() {
        return _interpolation.apply(getPosition());
    }

    public float getPosition() {
        return playing || paused ? (1 - (_reaming / (float) _duration)) : 0;
    }

    public void handle(long time) {
        if (playing) {
            if (lastTime != -1) {
                _reaming -= (time - lastTime) * delta;
            }
            if (_reaming <= 0) {
                if (_reaming == 0 || _loops == 0) {
                    compute(_interpolation.apply(1));
                } else {
                    _reaming = _reaming % duration;
                    compute(_interpolation.apply(1 - (_reaming / (float) _duration)));
                }
                if (_loops == 0) {
                    stop();
                } else if (_loops > 0) {
                    _loops -= 1;
                }
            } else {
                compute(_interpolation.apply(1 - (_reaming / (float) _duration)));
            }
            lastTime = time;
        }
    }

    protected void evaluate() {
        if (isStopped()) {
            _duration = duration;
            _reaming = duration;
            _loops = loops;
            _interpolation = interpolation;
        }
    }

    protected abstract void compute(float t);

    public static float mix(float a, float b, float t) {
        return a * (1 - t) + b * t;
    }

    public static int mixColor(int a, int b, float t) {
        float ra = ((a >> 24) & 0xff) / 255f;
        float rr = ((a >> 16) & 0xff) / 255f;
        float rg = ((a >> 8) & 0xff) / 255f;
        float rb = (a & 0xff) / 255f;

        int sa = Math.min(255, Math.round(mix(ra, (((b >> 24) & 0xff) / 255f), t) * 255));
        int sr = Math.min(255, Math.round(mix(rr, (((b >> 16) & 0xff) / 255f), t) * 255));
        int sg = Math.min(255, Math.round(mix(rg, (((b >> 8) & 0xff) / 255f), t) * 255));
        int sb = Math.min(255, Math.round(mix(rb, ((b & 0xff) / 255f), t) * 255));

        return (sa << 24) | (sr << 16) | (sg << 8) | sb;
    }

    public static float angularMix(float a, float b, float t) {
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
