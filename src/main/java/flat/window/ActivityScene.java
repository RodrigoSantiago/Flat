package flat.window;

import flat.widget.SceneActivity;
import flat.widget.stages.Stage;

public class ActivityScene {

    private SceneActivity scene;
    private Activity activity;

    public ActivityScene(SceneActivity scene) {
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

    void addStage(Stage stage) {
        scene.addStage(stage);
    }
}
