package flat.animations;

import flat.window.Activity;

public class PlayLaterTimer implements Animation {

    private final Activity activity;
    private final float duration;
    private final Runnable task;
    private boolean playing;
    private float time;

    public PlayLaterTimer(Activity activity, float duration, Runnable task) {
        this.activity = activity;
        this.duration = duration;
        this.task = task;
    }

    public void play() {
        playing = true;
        time = duration;
        activity.addAnimation(this);
    }

    public void stop() {
        playing = false;
        activity.removeAnimation(this);
    }

    @Override
    public Activity getSource() {
        return activity;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void handle(float seconds) {
        time -= seconds;
        if (time <= 0) {
            time = Math.max(0, time + duration);
            task.run();
            playing = false;
        }
    }
}
