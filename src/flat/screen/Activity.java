package flat.screen;

import flat.widget.*;

public class Activity {
    Scene scene;
    private boolean invalided, layoutInvalidaded;

    public Activity() {
        scene = new Scene();
    }

    public void onSave() {

    }

    public void onLoad() {

    }

    public void onLayout(double width, double height) {
        scene.onLayout(width, height);
    }

    public void onDraw(Context glContext) {
        scene.onDraw(glContext);
    }

    final boolean draw() {
        if (invalided) {
            invalided = false;
            return true;
        } else {
            return false;
        }
    }

    final boolean layout() {
        if (layoutInvalidaded) {
            layoutInvalidaded = false;
            return true;
        } else {
            return false;
        }
    }

    public void invalidate(boolean layout) {
        invalided = true;
        if (layout) {
            layoutInvalidaded = true;
        }
    }

    public Widget findById(String id) {
        return scene.findById(id);
    }

    public Widget findByPosition(double x, double y) {
        Widget child = scene.findByPosition(x , y);
        return child == null ? scene : child;
    }

    public Widget findFocused() {
        Widget child = scene.findFocused();
        return child == null ? scene : child;
    }
}
