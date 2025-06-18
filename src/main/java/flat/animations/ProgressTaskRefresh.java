package flat.animations;

import flat.concurrent.ProgressTask;
import flat.uxml.Controller;
import flat.uxml.UXListener;
import flat.window.Activity;

public class ProgressTaskRefresh implements Animation {

    private final Controller controller;
    private final ProgressTask<?> task;
    private final UXListener<Float> onProgress;
    private final Runnable onDone;
    private float prev;

    public ProgressTaskRefresh(Controller controller, ProgressTask<?> task, UXListener<Float> onProgress, Runnable onDone) {
        this.controller = controller;
        this.task = task;
        this.onProgress = onProgress;
        this.onDone = onDone;
    }

    @Override
    public Activity getSource() {
        return controller.getActivity();
    }

    @Override
    public boolean isPlaying() {
        return !task.isDone();
    }

    @Override
    public void handle(float seconds) {
        float p = task.getProgress();
        if (prev != p) {
            prev = p;
            onProgress.handle(p);
        }
    }

    @Override
    public void onRemoved() {
        if (onDone != null) {
            onDone.run();
        }
    }
}
