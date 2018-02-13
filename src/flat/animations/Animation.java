package flat.animations;

import flat.screen.Application;

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
            _reaming = (long) (_duration * position);
        }
    }

    public float getPosition() {
        return playing || paused ? _interpolation.apply(1 - (_reaming / (float) _duration)) : 0;
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
