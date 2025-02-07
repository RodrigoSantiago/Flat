package flat.animations;

import flat.window.Activity;

public abstract class NormalizedAnimation implements Animation {

    private float delta = 1f;

    private boolean playing;
    private boolean paused;
    private int loops;
    private Interpolation interpolation = Interpolation.linear;

    private float duration;
    private float _duration;
    private float _reaming;
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

    public float getDuration() {
        return duration;
    }

    public void setDuration(float time) {
        this.duration = time;
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

    public void play(Activity activity) {
        play(activity, 0);
    }

    public void play(Activity activity, float position) {
        position = Math.max(Math.min(position, 1), 0);
        if (isPlaying()) {
            stop();
        }

        if (paused) {
            playing = true;
            paused = false;
        } else {
            evaluate();
            playing = true;
            paused = false;
        }
        _reaming = (_duration * (1 - position));

        if (activity != null) {
            activity.addAnimation(this);
        }
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
            _reaming = (_duration * (1 - position));
        }
    }

    public float getInterpolatedPosition() {
        return _interpolation == null ? interpolation.apply(getPosition()) : _interpolation.apply(getPosition());
    }

    public float getPosition() {
        return playing || paused ?  Math.max(Math.min(1 - (_reaming / _duration), 1), 0) : 0;
    }

    @Override
    public void handle(float time) {
        if (playing) {
            if (_reaming <= 0) {
                if (_reaming == 0 || _loops == 0) {
                    compute(_interpolation.apply(1));
                } else {
                    _reaming = _reaming % duration;
                    compute(_interpolation.apply(1 - (_reaming / _duration)));
                }
                if (_loops == 0) {
                    stop();
                } else if (_loops > 0) {
                    _loops -= 1;
                }
            } else {
                compute(_interpolation.apply(1 - (_reaming / _duration)));
            }
            _reaming -= time * delta;
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
}
