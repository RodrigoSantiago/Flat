package flat.window;

import flat.animations.Animation;
import flat.backend.WL;
import flat.events.FocusEvent;
import flat.events.KeyCode;
import flat.events.KeyEvent;
import flat.exception.FlatException;
import flat.graphics.SmartContext;
import flat.graphics.context.Context;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.Scene;
import flat.widget.Widget;

import java.util.ArrayList;

public class Activity extends Controller {

    private final Context context;
    private final ArrayList<Animation> animations = new ArrayList<>();
    private final ArrayList<Animation> animationsAdd = new ArrayList<>();
    private final ArrayList<Animation> animationsRemove = new ArrayList<>();
    private Widget focus;

    private float width;
    private float height;

    private Scene scene;
    private Scene nextScene;

    private UXTheme theme;
    private UXTheme nextTheme;

    private UXBuilder builder;
    private boolean invalided, invalidScene, invalidTheme;
    private Widget invalidWidget;

    private boolean hide = true;
    private boolean pause = true;

    private float lastDpi;

    public Activity(Context context) {
        this.context = context;
        this.width = context.getWidth();
        this.height = context.getHeight();
    }

    public Context getContext() {
        return context;
    }

    public Window getWindow() {
        return context.getWindow();
    }

    @Override
    public boolean isListening() {
        return !hide && !pause;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(String pathName) {
        setScene(new ResourceStream(pathName));
    }

    public void setScene(ResourceStream resourceStream) {
        this.builder = UXNode.parse(resourceStream).instance(this);
        this.nextScene = null;
    }

    public void setScene(Scene scene) {
        if (scene.getActivity() != null) {
            throw new FlatException("The scene is already assigned to an Activity");
        }

        if (this.scene != scene && this.nextScene != scene) {
            this.nextScene = scene;
            this.builder = null;
        }
    }

    public UXTheme getTheme() {
        return theme;
    }

    public void setTheme(String pathName) {
        setTheme(new ResourceStream(pathName));
    }

    public void setTheme(ResourceStream resourceStream) {
        setTheme(UXSheet.parse(resourceStream).instance());
    }

    public void setTheme(UXTheme theme) {
        if (this.theme != theme && this.nextTheme != theme) {
            this.nextTheme = theme;
        }
    }

    public void addAnimation(Animation animation) {
        if (!hide) {
            animationsAdd.add(animation);
            animationsRemove.remove(animation);
        }
    }

    public void removeAnimation(Animation animation) {
        if (!hide) {
            animationsRemove.add(animation);
            animationsAdd.remove(animation);
        }
    }

    private void updateDensity() {
        float dpi = getWindow() == null ? 160f : getWindow().getDpi();
        if (lastDpi != dpi) {
            lastDpi = dpi;
            invalidateTheme();
        }
    }

    private void buildTheme() {
        if (nextTheme != null) {
            theme = nextTheme;
            nextTheme = null;
            invalidateTheme();
        }
    }

    private void buildScene() {
        Scene old = scene;
        if (builder != null) {
            nextScene = builder.build(theme);
            builder = null;
        }
        if (scene == null && nextScene == null) {
            nextScene = new Scene();
        }
        if (nextScene != null) {
            clearAnimations();

            scene = nextScene;
            nextScene = null;

            if (old != null) {
                old.getActivityScene().setActivity(null);
            }
            scene.getActivityScene().setActivity(this);
            invalidateTheme();
        }
    }

    void refreshScene() {
        updateDensity();
        buildTheme();
        buildScene();

        if (invalidTheme) {
            invalidTheme = false;
            scene.setTheme(getTheme());
            scene.applyTheme();
            invalidateWidget(scene);
        }
    }

    void layout(float width, float height) {
        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            invalidateWidget(scene);
        }

        if (invalidWidget != null) {
            var wiget = invalidWidget;
            invalidWidget = null;
            if (wiget == scene) {
                wiget.onMeasure();
                wiget.onLayout(width, height);
            } else {
                wiget.onMeasure();
                wiget.onLayout(wiget.getLayoutWidth(), wiget.getLayoutHeight());
            }
        }
    }

    boolean draw(SmartContext context) {
        if (invalided) {
            invalided = false;
            onDraw(context);
            return true;
        } else {
            return false;
        }
    }

    void show() {
        hide = false;
        refreshScene();
        layout(getWindow().getClientWidth(), getWindow().getClientHeight());
        onShow();

    }

    public void onShow() {

    }

    void start() {
        pause = false;
        onStart();
    }

    public void onStart() {


    }

    void pause() {
        pause = true;
        onPause();
    }

    public void onPause() {


    }

    void hide() {
        clearAnimations();
        hide = true;
        onHide();
    }

    public void onHide() {

    }

    boolean closeRequest(boolean systemRequest) {
        return onCloseRequest(systemRequest);
    }

    public boolean onCloseRequest(boolean systemRequest) {

        return true;
    }

    public void drawBackground(SmartContext context) {
        context.setAntialiasEnabled(true);
        context.setView(0, 0, (int) getWidth(), (int) getHeight());
        context.clear(scene == null ? 0x0 : scene.getBackgroundColor(), 1, 0);
        context.clearClip();
    }

    public void drawWidgets(SmartContext context) {
        if (scene != null) {
            scene.onDraw(context);
        }
    }

    public void onDraw(SmartContext context) {
        drawBackground(context);
        drawWidgets(context);
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
                    nextFocus = scene.findById(focusID);
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
            oldFocus.refreshFocus();
        }
        if (focus != null) {
            focus.refreshFocus();
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

    boolean animate(float loopTime) {
        animations.removeAll(animationsRemove);
        animationsRemove.clear();

        for (Animation anim : animationsAdd) {
            if (!animations.contains(anim)) {
                animations.add(anim);
            }
        }
        animationsAdd.clear();

        boolean wasAnimated = false;

        for (int i = 0; i < animations.size(); i++) {
            Animation anim = animations.get(i);
            if (anim.getSource() != this) {
                animations.remove(i--);
                continue;
            }

            if (anim.isPlaying()) {
                wasAnimated = true;
                anim.handle(loopTime);
            }
            if (!anim.isPlaying()) {
                animations.remove(i--);
            }
        }

        return wasAnimated;
    }

    void clearAnimations() {
        animationsAdd.clear();
        animationsRemove.clear();
        animations.clear();
    }

    public void invalidate() {
        if (!invalided) {
            invalided = true;
            WL.PostEmptyEvent();
        }
    }

    public void invalidateTheme() {
        invalidTheme = true;
        invalidate();
    }

    public void invalidateWidget(Widget widget) {
        invalidate();
        if (invalidWidget == scene) return;
        if (invalidWidget != null) {
            if (invalidWidget.isChildOf(widget)) {
                invalidWidget = widget;
            } else if (!widget.isChildOf(invalidWidget)) {
                invalidWidget = scene;
            }
        } else {
            invalidWidget = widget;
        }
    }

    public Widget findById(String id) {
        return scene == null ? null : scene.findById(id);
    }

    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if (scene == null) {
            return null;
        } else {
            Widget child = scene.findByPosition(x, y, includeDisabled);
            return child == null ? scene : child;
        }
    }

    public Widget findFocused() {
        if (scene == null) {
            return null;
        } else {
            Widget child = scene.findFocused();
            return child == null ? scene : child;
        }
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getDensity() {
        return lastDpi;
    }

    public static class Transition implements Animation {

        private Activity prev;
        private Activity next;

        public Transition(Activity next) {
            this.next = next;
        }

        public Activity getNext() {
            return next;
        }

        public Activity getPrev() {
            return prev;
        }

        public void start(Activity current) {
            this.prev = current;

            if (prev != null) {
                prev.pause();
            }
            if (next != null) {
                next.show();
            }
        }

        @Override
        public Activity getSource() {
            return null;
        }

        @Override
        public void handle(float time) {

        }

        public void end() {

        }

        public boolean draw(SmartContext context) {
            return false;
        }

        public void stop() {

        }

        @Override
        public boolean isPlaying() {
            return false;
        }
    }
}
