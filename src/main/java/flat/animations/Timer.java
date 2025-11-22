package flat.animations;

import flat.uxml.Controller;
import flat.widget.Widget;
import flat.window.Activity;

public class Timer implements Animation {

    private Controller controller;
    private Widget widget;
    private float duration;
    private boolean loop;
    private Runnable task;
    private boolean playing;
    private float time;

    public Timer(Controller controller, float duration, Runnable task) {
        this(controller, duration, false, task);
    }

    public Timer(Controller controller, float duration, boolean loop, Runnable task) {
        this.controller = controller;
        this.duration = duration;
        this.loop = loop;
        this.task = task;
    }
    
    public Timer(Widget widget, float duration, Runnable task) {
        this(widget, duration, false, task);
    }
    
    public Timer(Widget widget, float duration, boolean loop, Runnable task) {
        this.widget = widget;
        this.duration = duration;
        this.loop = loop;
        this.task = task;
    }

    public void play() {
        if (getSource() != null) {
            playing = true;
            time = duration;
            getSource().addAnimation(this);
        }
    }

    public void stop() {
        playing = false;
        if (getSource() != null) {
            getSource().removeAnimation(this);
        }
    }

    @Override
    public Activity getSource() {
        return controller == null ? widget.getActivity() : controller.getActivity();
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
            if (!loop) {
                playing = false;
            }
        }
    }
}
