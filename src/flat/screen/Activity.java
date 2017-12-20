package flat.screen;

import flat.graphics.smart.SmartContext;
import flat.widget.Scene;
import flat.widget.Widget;

public class Activity {
    private Scene scene;
    private boolean invalided, layoutInvalidaded;
    private float width;
    private float height;

    public Activity() {
        scene = new Scene(this);
    }

    public void onSave() {

    }

    public void onLoad() {

    }

    public void onLayout(float width, float height) {
        this.width = width;
        this.height = height;
        scene.onMeasure();
        scene.onLayout(0, 0, width, height);
    }

    public void onDraw(SmartContext context) {
        scene.onDraw(context);
    }

    public Scene getScene() {
        return scene;
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

    public final void invalidate(boolean layout) {
        invalided = true;
        if (layout) {
            layoutInvalidaded = true;
        }
    }

    public Widget findById(String id) {
        return scene.findById(id);
    }

    public Widget findByPosition(float x, float y) {
        Widget child = scene.findByPosition(x , y);
        return child == null ? scene : child;
    }

    public Widget findFocused() {
        Widget child = scene.findFocused();
        return child == null ? scene : child;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
