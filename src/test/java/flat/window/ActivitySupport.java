package flat.window;

import flat.widget.Scene;

public class ActivitySupport {
    public static void setActivity(Scene scene, Activity activity) {
        scene.getActivityScene().setActivity(activity);
    }
}
