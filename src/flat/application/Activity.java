package flat.application;

import flat.graphics.SmartContext;
import flat.uxml.Controller;
import flat.uxml.UXAttributes;
import flat.uxml.UXLoader;
import flat.uxml.data.Dimension;
import flat.uxml.data.DimensionStream;
import flat.widget.Scene;
import flat.widget.Widget;

public class Activity extends Controller {

    private Scene scene;
    private Widget root;

    private float width;
    private float height;
    private int color;

    private Dimension dimension;
    private DimensionStream stream;
    private boolean invalided, layoutInvalided, streamInvalided;

    public Activity() {
        scene = new Scene(this);
        scene.applyAttributes(this, new UXAttributes(null));
        color = 0xDDDDDDFF;
    }

    public void setBackgroundColor(int color) {
        this.color = color;
    }

    public DimensionStream getStream() {
        return stream;
    }

    public void setStream(DimensionStream stream) {
        this.stream = stream;
        streamInvalided = true;
        invalidate(true);
    }

    public void onSave() {

    }

    public void onLoad() {

    }

    public void onLayout(float width, float height, float dpi) {
        if (width != this.width || height != this.height) {
            Dimension dm;
            if (stream != null) {
                dm = stream.getCloserDimension(width, height, dpi);
                if (dm != null && !dm.equals(dimension) || dpi != dimension.dpi || streamInvalided) {
                    UXLoader loader = new UXLoader(stream, dm, null, this);
                    Widget widget = null;
                    try {
                        widget = loader.load();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (widget != null) {
                        onSave();
                        if (root != null) {
                            scene.childRemove(root);
                        }
                        scene.add(root = widget);
                        onLoad();
                    }
                }
            } else {
                dm = new Dimension(width, height, dpi);
            }
            this.dimension = dm;
            this.width = width;
            this.height = height;
        }
        scene.onMeasure();
        scene.onLayout(0, 0, width, height);
    }

    public void onDraw(SmartContext context) {
        context.setView(0, 0, (int) getWidth(), (int) getHeight());
        context.clear(color, 1, 0);
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
        if (layoutInvalided) {
            layoutInvalided = false;
            return true;
        } else {
            return false;
        }
    }

    public final void invalidate(boolean layout) {
        invalided = true;
        if (layout) {
            layoutInvalided = true;
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
