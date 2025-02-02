package flat.window;

import flat.widget.Scene;

import java.util.Objects;

public class ActivityScene {

    private Scene scene;
    private Activity activity;

    public ActivityScene(Scene scene) {
        this.scene = scene;
    }

    public Activity getActivity() {
        return activity;
    }

    void setActivity(Activity activity) {
        if (this.activity != activity) {
            Activity prev = this.activity;
            this.activity = activity;
            scene.onActivityChange(prev, this.activity);
        }
    }
}
