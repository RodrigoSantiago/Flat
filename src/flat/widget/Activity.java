package flat.widget;

import flat.events.FocusEvent;
import flat.events.KeyCode;
import flat.events.KeyEvent;
import flat.graphics.SmartContext;
import flat.uxml.*;
import flat.resources.Dimension;
import flat.resources.DimensionStream;

public class Activity extends Controller {

    private Scene scene;
    private Widget focus;

    private float width;
    private float height;
    private int color;

    private Dimension dimension;
    private DimensionStream stream;
    private UXTheme theme;
    private boolean invalided, layoutInvalided, streamInvalided;

    public Activity() {
        scene = new Scene();
        scene.activity = this;
        scene.applyAttributes(new UXStyleAttrs("attributes", null, null), this);
        scene.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scene.setMinSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scene.setMaxSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);

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

    public UXTheme getTheme() {
        return theme;
    }

    public void setTheme(UXTheme theme) {
        this.theme = theme;
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
                if ((dm != null && !dm.equals(dimension)) || dpi != dimension.dpi || streamInvalided) {
                    streamInvalided = false;
                    UXLoader loader = new UXLoader(stream, dm, theme, null, this);
                    Scene newScene = null;
                    try {
                        newScene = (Scene) loader.load(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (newScene != null) {
                        onSave();
                        this.scene = newScene;
                        this.scene.activity = this;
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

    public void onKeyPress(KeyEvent event) {
        if (event.getType() == KeyEvent.RELEASED || event.getType() == KeyEvent.REPEATED) {
            if (event.getKeycode() == KeyCode.KEY_TAB) {
                Widget nextFocus;
                if (getFocus() == null) {
                    String focusID = event.isShiftDown() ? scene.getPrevFocusId() : scene.getNextFocusId();
                    nextFocus = scene.findById(focusID);
                } else {
                    String focusID = event.isShiftDown() ? getFocus().getPrevFocusId() : getFocus().getNextFocusId();
                    nextFocus = getFocus().findById(focusID);
                }
                if (nextFocus != null) {
                    setFocus(nextFocus);
                }
            }
        }
    }

    public void setFocus(Widget widget) {
        if ((widget == focus) || (widget != null && widget.getActivity() != this)) {
            return;
        }

        Widget oldFocus = focus;
        this.focus = widget;

        if (oldFocus != null) {
            oldFocus.setFocused(false);
        }
        if (focus != null) {
            focus.setFocused(true);
        }

        if (oldFocus != null) {
            oldFocus.fireFocus(new FocusEvent(oldFocus, focus));
        }
        if (focus != null) {
            focus.fireFocus(new FocusEvent(oldFocus, focus));
        }
    }

    public Widget getFocus() {
        return focus;
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

    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        Widget child = scene.findByPosition(x , y, includeDisabled);
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
