package flat.widget;

import flat.uxml.TaskList;
import flat.window.Activity;

public class SceneActivity {
    Scene scene;

    SceneActivity(Scene scene) {
        this.scene = scene;
    }

    public void onActivityChange(Activity prev, Activity activity) {
        scene.onActivityChangeLocal(prev, activity);
        TaskList tasks = new TaskList();
        scene.onActivityChange(prev, activity, tasks);
        tasks.run();
    }
}
