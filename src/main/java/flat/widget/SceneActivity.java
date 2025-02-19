package flat.widget;

import flat.window.Activity;

public class SceneActivity {
    Scene scene;

    SceneActivity(Scene scene) {
        this.scene = scene;
    }

    public void onActivityChange(Activity prev, Activity activity) {
        scene.onActivityChangeLocal(prev, activity);
    }
}
