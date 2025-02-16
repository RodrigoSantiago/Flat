package flat.window;

import flat.animations.Animation;
import flat.events.FocusEvent;
import flat.events.KeyCode;
import flat.events.KeyEvent;
import flat.events.PointerEvent;
import flat.exception.FlatException;
import flat.graphics.SmartContext;
import flat.graphics.context.Context;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.Group;
import flat.widget.Parent;
import flat.widget.Scene;
import flat.widget.Widget;

import java.util.ArrayList;

public class Activity {

    private final Window window;
    private final Context context;
    private final ArrayList<Animation> animations = new ArrayList<>();
    private final ArrayList<Animation> animationsAdd = new ArrayList<>();
    private final ArrayList<Animation> animationsRemove = new ArrayList<>();
    private final ArrayList<Widget> pointerFilters = new ArrayList<>();
    private final ArrayList<Widget> keyFilters = new ArrayList<>();
    private final ArrayList<Widget> resizeFilters = new ArrayList<>();
    private final ArrayList<Widget> filtersTemp = new ArrayList<>();
    private Widget focus;

    private float width;
    private float height;

    private Scene scene;
    private Controller controller;
    private Scene nextScene;
    private Controller nextController;
    private WindowSettings initialSettings;

    private UXTheme theme;

    private boolean invalided, invalidScene, invalidDensity;
    private Widget invalidWidget;

    private float lastDpi;

    public static Activity create(Window window, WindowSettings settings) {
        if (window.getActivity() != null) {
            throw new RuntimeException("The Window already have a activity");
        } else {
            return new Activity(window, settings);
        }
    }

    private Activity(Window window, WindowSettings settings) {
        this.window = window;
        this.context = window.getContext();
        this.width = settings.getWidth();
        this.height = settings.getHeight();
        this.initialSettings = settings;
    }

    void initialize() {
        if (initialSettings.getController() != null) {
            controller = initialSettings.getController().build(this);
        }

        if (initialSettings.getThemeStream() != null) {
            theme = UXSheet.parse(initialSettings.getThemeStream()).instance();

        } else if (initialSettings.getTheme() != null) {
            theme = initialSettings.getTheme();
        }

        if (initialSettings.getLayoutStream() != null) {
            scene = UXNode.parse(initialSettings.getLayoutStream()).instance(controller).buildScene(theme);

        } else if (initialSettings.getLayout() != null) {
            scene = initialSettings.getLayout();
            scene.setTheme(theme);

        } else {
            scene = new Scene();
            scene.setTheme(theme);
        }

        initialSettings = null;

        scene.getActivityScene().setActivity(this);
        invalidateWidget(scene);
        invalidateDensity();
    }

    public Context getContext() {
        return context;
    }

    public Window getWindow() {
        return context.getWindow();
    }

    public Scene getScene() {
        return scene;
    }

    public Controller getController() {
        return controller;
    }

    public void setLayoutBuilder(String pathName, Controller controller) {
        setLayoutBuilder(new ResourceStream(pathName), controller);
    }

    public void setLayoutBuilder(ResourceStream resourceStream, Controller controller) {
        this.nextController = controller;
        this.nextScene = UXNode.parse(resourceStream).instance(controller).buildScene(getTheme());
    }

    public void setLayoutBuilder(Scene scene) {
        if (scene.getActivity() != null) {
            throw new FlatException("The scene is already assigned to an Activity");
        }

        if (this.nextScene != scene) {
            this.nextController = null;
            this.nextScene = scene;
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
        this.theme = theme;
        invalidateDensity();
    }

    public void addAnimation(Animation animation) {
        animationsAdd.add(animation);
        animationsRemove.remove(animation);
    }

    public void removeAnimation(Animation animation) {
        animationsRemove.add(animation);
        animationsAdd.remove(animation);
    }

    private void updateDensity() {
        float dpi = getWindow() == null ? 160f : getWindow().getDpi();
        if (lastDpi != dpi) {
            lastDpi = dpi;
            invalidateDensity();
        }
    }

    private void buildScene() {
        if (nextScene != null) {
            clearAnimations();

            Scene old = scene;

            this.controller = nextController;
            this.scene = nextScene;

            nextController = null;
            nextScene = null;

            if (old != null) {
                old.getActivityScene().setActivity(null);
            }
            scene.getActivityScene().setActivity(this);
            invalidateWidget(scene);
            invalidateDensity();
        }
    }

    void refreshScene() {
        updateDensity();
        buildScene();

        if (invalidDensity) {
            invalidDensity = false;
            scene.refreshStyle();
        }
        clearUnusedFilters();
    }

    void layout(float width, float height) {
        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            onResizeFilter();
            invalidateWidget(scene);
        }

        if (invalidWidget != null) {
            var wiget = invalidWidget;
            invalidWidget = null;
            if (wiget == scene) {
                wiget.onMeasure();
                wiget.onLayout(width, height);
            } else {
                Parent parent = wiget.getParent();
                if (parent == null) {
                    wiget.onMeasure();
                    wiget.onLayout(wiget.getLayoutWidth(), wiget.getLayoutHeight());
                } else if (!parent.onLayoutSingleChild(wiget)) {
                    parent.onMeasure();
                    parent.onLayout(parent.getLayoutWidth(), parent.getLayoutHeight());
                }
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
        refreshScene();
        layout(getWindow().getClientWidth(), getWindow().getClientHeight());

        if (controller != null) {
            try {
                controller.onShow();
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    boolean closeRequest(boolean systemRequest) {
        if (controller != null) {
            try {
                return controller.onCloseRequest(systemRequest);
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
        return true;
    }

    void close() {
        if (controller != null) {
            try {
                controller.onHide();
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    private void drawBackground(SmartContext context) {
        context.setAntialiasEnabled(true);
        context.setView(0, 0, (int) getWidth(), (int) getHeight());
        context.clear(scene.getBackgroundColor(), 1, 0);
        context.clearClip();
    }

    private void drawWidgets(SmartContext context) {
        scene.onDraw(context);
    }

    private void onDraw(SmartContext context) {
        drawBackground(context);
        drawWidgets(context);

        if (controller != null) {
            try {
                controller.onDraw(context);
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    private void clearUnusedFilters() {
        filtersTemp.addAll(pointerFilters);
        for (var widget : filtersTemp) {
            if (widget.getActivity() != this) {
                removePointerFilter(widget);
            }
        }
        filtersTemp.clear();
        filtersTemp.addAll(keyFilters);
        for (var widget : filtersTemp) {
            if (widget.getActivity() != this) {
                removePointerFilter(widget);
            }
        }
        filtersTemp.clear();
    }

    public void addPointerFilter(Widget widget) {
        if (widget.getActivity() == this) {
            pointerFilters.remove(widget);
            pointerFilters.add(widget);
        }
    }

    public void removePointerFilter(Widget widget) {
        pointerFilters.remove(widget);
    }

    public void onPointerFilter(PointerEvent event) {
        filtersTemp.addAll(pointerFilters);
        for (int i = filtersTemp.size() - 1; i >= 0; i--) {
            var widget = filtersTemp.get(i);
            if (widget.getActivity() == this) {
                try {
                    widget.firePointer(event);
                } catch (Exception e) {
                    Application.handleException(e);
                }
                if (event.isConsumed()) {
                    break;
                }
            }
        }
        filtersTemp.clear();
    }

    public void addKeyFilter(Widget widget) {
        if (widget.getActivity() == this && !keyFilters.contains(widget)) {
            keyFilters.remove(widget);
            keyFilters.add(widget);
        }
    }

    public void removeKeyFilter(Widget widget) {
        keyFilters.remove(widget);
    }

    public void onKeyFilter(KeyEvent event) {
        filtersTemp.addAll(keyFilters);
        for (int i = filtersTemp.size() - 1; i >= 0; i--) {
            var widget = filtersTemp.get(i);
            if (widget.getActivity() == this) {
                try {
                    widget.fireKey(event);
                } catch (Exception e) {
                    Application.handleException(e);
                }
                if (event.isConsumed()) {
                    break;
                }
            }
        }
        filtersTemp.clear();

        if (!event.isConsumed() && event.getKeycode() == KeyCode.KEY_TAB) {
            Widget nextFocus = getFocus() == null ? scene : getFocus();
            do {
                if (nextFocus instanceof Group group) {
                    if (group.getInitialFocusId() != null) {
                        nextFocus = nextFocus.findById(group.getInitialFocusId());
                    } else if (nextFocus.getGroup() != null) {
                        nextFocus = nextFocus.getGroup().findById(nextFocus.getNextFocusId());
                    } else {
                        nextFocus = nextFocus.findById(nextFocus.getNextFocusId());
                    }
                } else {
                    nextFocus = nextFocus.findById(nextFocus.getNextFocusId());
                }
            } while (nextFocus instanceof Group group && group.getInitialFocusId() != null);

            if (nextFocus != null && nextFocus.isFocusable()) {
                setFocus(nextFocus);
            }
        }
    }

    public void addResizeFilter(Widget widget) {
        if (widget.getActivity() == this && !resizeFilters.contains(widget)) {
            resizeFilters.add(widget);
        }
    }

    public void removeResizeFilter(Widget widget) {
        resizeFilters.remove(widget);
    }

    public void onResizeFilter() {
        filtersTemp.addAll(resizeFilters);
        for (int i = filtersTemp.size() - 1; i >= 0; i--) {
            var widget = filtersTemp.get(i);
            if (widget.getActivity() == this) {
                try {
                    widget.fireResize();
                } catch (Exception e) {
                    Application.handleException(e);
                }
            }
        }
        filtersTemp.clear();
    }

    public void setFocus(Widget widget) {
        if ((widget == focus) ||
                (widget != null && widget.getActivity() != this) ||
                (widget != null && !widget.isFocusable())) {
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
            try {
                oldFocus.fireFocus(new FocusEvent(oldFocus, focus));
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
        if (focus != null) {
            try {
                focus.fireFocus(new FocusEvent(oldFocus, focus));
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    public Widget getFocus() {
        return focus;
    }

    public Widget getKeyFocus() {
        return focus == null ? scene : focus;
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
        }
    }

    public void invalidateWidget(Widget widget) {
        invalidate();
        if (invalidWidget != scene && widget.getActivity() == this) {
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
    }

    private void invalidateDensity() {
        invalidDensity = true;
        invalidate();
    }

    public Widget findById(String id) {
        return scene.findById(id);
    }

    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        Widget child = scene.findByPosition(x, y, includeDisabled);
        return child == null ? scene : child;
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
}
