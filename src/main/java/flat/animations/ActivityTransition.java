package flat.animations;

import flat.graphics.SmartContext;
import flat.widget.Activity;

public class ActivityTransition {

    protected Activity prev;
    protected Activity next;

    public ActivityTransition() {

    }

    public Activity getNext() {
        return next;
    }

    public Activity getPrev() {
        return prev;
    }

    public void start(Activity prev, Activity next) {
        this.prev = prev;
        this.next = next;

        if (prev != null) {
            prev.onPause();
        }
        if (next != null) {
            next.onShow();
        }
    }

    public void handle(long time) {

    }

    public void end() {
        if (prev != null) {
            prev.onHide();
        }
        if (next != null) {
            next.onStart();
            next.invalidate(true);
        }
    }

    public boolean draw(SmartContext context) {
        return false;
    }

    public void stop() {

    }

    public boolean isPlaying() {
        return false;
    }
}
