package flat.animations;

import flat.graphics.SmartContext;
import flat.window.Activity;

public class ActivityTransition {

    private Activity prev;
    private Activity next;

    public ActivityTransition(Activity next) {
        this.next = next;
    }

    public Activity getNext() {
        return next;
    }

    public Activity getPrev() {
        return prev;
    }

    public void start(Activity current) {
        this.prev = current;

        if (prev != null) {
            prev.onPause();
        }
        if (next != null) {
            next.onShow();
        }
    }

    public void handle(float time) {

    }

    public void end() {

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
