package flat.animations;

import flat.graphics.SmartContext;
import flat.widget.Activity;

public class ActivityTransition {

    Activity prev;
    Activity next;

    public ActivityTransition() {

    }

    public Activity getNext() {
        return next;
    }

    public Activity getPrev() {
        return prev;
    }

    public void setActivities(Activity prev, Activity next) {
        this.prev = prev;
        this.next = next;
    }

    public void handle(long time) {

    }

    public boolean draw(SmartContext context) {
        return true;
    }

    public void stop() {

    }

    public boolean isPlaying() {
        return false;
    }
}
