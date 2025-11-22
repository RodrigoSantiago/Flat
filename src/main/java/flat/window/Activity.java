package flat.window;

import flat.animations.Animation;
import flat.animations.PlayLaterTimer;
import flat.events.*;
import flat.exception.FlatException;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.context.Context;
import flat.math.Mathf;
import flat.math.Vector2;
import flat.math.shapes.Rectangle;
import flat.math.stroke.BasicStroke;
import flat.resources.Dimension;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.uxml.value.UXValue;
import flat.widget.Parent;
import flat.widget.Scene;
import flat.widget.Widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class Activity {

    private final Window window;
    private final Context context;
    private final ArrayList<Animation> animations = new ArrayList<>();
    private final ArrayList<Animation> animationsAdd = new ArrayList<>();
    private final ArrayList<Animation> animationsRemove = new ArrayList<>();
    private final ArrayList<Animation> animationsAdded = new ArrayList<>();
    private final ArrayList<Animation> animationsRemoved = new ArrayList<>();
    private final ArrayList<Widget> focusListener = new ArrayList<>();
    private final ArrayList<Widget> keyListeners = new ArrayList<>();
    private final ArrayList<Widget> pointerFilters = new ArrayList<>();
    private final ArrayList<Widget> keyFilters = new ArrayList<>();
    private final ArrayList<Widget> resizeFilters = new ArrayList<>();
    private final ArrayList<Widget> filtersTemp = new ArrayList<>();
    private final HashSet<Widget> focusables = new HashSet<>();
    private Widget focus;
    private float focusAnim;
    private boolean renderPartial;

    private int width;
    private int height;

    private Scene scene;
    private Controller controller;
    private Scene nextScene;
    private Controller nextController;
    private WindowSettings initialSettings;

    private UXTheme theme;
    private float fontScale = 1f;
    private final HashMap<String, UXValue> themeVariables = new HashMap<>();

    private UXStringBundle stringBundle;

    private boolean invalidScene, invalidThemeStyle;
    private Widget invalidWidget;
    private boolean invalidLayout;
    private Rectangle invalidRect;
    private Rectangle invalidRectPrev;
    private final HashSet<Widget> invalidParents = new HashSet<>();

    private float lastDpi = 160f;
    private boolean continuousRendering;

    public static Activity create(Window window, WindowSettings settings) {
        if (window.getActivity() != null) {
            throw new FlatException("The Window already have an activity");
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
            controller = initialSettings.getController().build();
        }

        if (initialSettings.getStringBundleStream() != null) {
            stringBundle = UXStringBundle.parse(initialSettings.getStringBundleStream());

        } else if (initialSettings.getStringBundle() != null) {
            stringBundle = initialSettings.getStringBundle();
        }

        if (initialSettings.getThemeStream() != null) {
            theme = UXSheet.parse(initialSettings.getThemeStream()).instance(fontScale, lastDpi, stringBundle, themeVariables);

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
        invalidateWidget(scene, true);
        invalidateThemeStyle();
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

    public void setLayoutBuilder(Scene scene, Controller controller) {
        if (scene.getActivity() != null) {
            throw new FlatException("The scene is already assigned to an Activity");
        }

        if (this.nextScene != scene) {
            this.nextController = controller;
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
        invalidateThemeStyle();
    }

    public void setStringBundle(String pathName) {
        setStringBundle(new ResourceStream(pathName));
    }

    public void setStringBundle(ResourceStream resourceStream) {
        setStringBundle(UXStringBundle.parse(resourceStream));
    }

    public void setStringBundle(UXStringBundle stringBundle) {
        this.stringBundle = stringBundle;
        invalidateThemeStyle();
    }

    public void setThemeVariable(String name, UXValue value) {
        themeVariables.put(name, value);
        invalidateThemeStyle();
    }

    public UXValue getThemeVariable(String name) {
        return themeVariables.get(name);
    }

    public float getFontScale() {
        return fontScale;
    }

    public void setFontScale(float fontScale) {
        this.fontScale = fontScale;
    }

    public void addAnimation(Animation animation) {
        animationsRemove.remove(animation);
        if (!animations.contains(animation) && !animationsAdd.contains(animation)) {
            animationsAdd.add(animation);
        }
    }

    public void removeAnimation(Animation animation) {
        animationsAdd.remove(animation);
        if (animations.contains(animation) && !animationsRemove.contains(animation)) {
            animationsRemove.add(animation);
        }
    }
    
    public boolean isBlockedByDialog() {
        return getScene().isBlockedByDialog();
    }

    public void runLater(FutureTask<?> task) {
        window.runSync(task);
    }

    public <T> Future<T> runLater(Callable<T> task) {
        return window.runSync(task);
    }

    public <T> Future<T> runLater(Runnable task) {
        return window.runSync(task);
    }

    public void runLater(FutureTask<?> task, float seconds) {
        if (seconds <= 0) {
            runLater(task);
            return;
        }
        new PlayLaterTimer(this, seconds, () -> window.runSync(task)).play();
    }

    public void runLater(Callable<?> task, float seconds) {
        if (seconds <= 0) {
            runLater(task);
            return;
        }
        new PlayLaterTimer(this, seconds, () -> window.runSync(task)).play();
    }

    public void runLater(Runnable task, float seconds) {
        if (seconds <= 0) {
            runLater(task);
            return;
        }
        new PlayLaterTimer(this, seconds, () -> window.runSync(task)).play();
    }

    private void updateDensity() {
        float dpi = getWindow() == null ? 160f : getWindow().getDpi();
        if (lastDpi != dpi) {
            lastDpi = dpi;
            invalidateThemeStyle();
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
            invalidateWidget(scene, true);
            invalidateThemeStyle();
        }
    }

    void refreshScene() {
        updateDensity();
        buildScene();

        if (invalidThemeStyle) {
            invalidThemeStyle = false;
            theme = theme != null ? theme.createInstance(fontScale, lastDpi, stringBundle, themeVariables) : null;
            scene.setTheme(theme);
            scene.refreshStyle();
        }
        clearUnusedFilters();
        refreshFocus();
    }

    void layout(int w, int h) {
        if (w != 0 && h != 0 && (this.width != w || this.height != h)) {
            this.width = w;
            this.height = h;
            onResizeFilter();
            invalidateWidget(scene, true);
        }

        if (invalidWidget != null) {
            var widget = invalidWidget;
            invalidWidget = null;
            invalidParents.clear();
            if (invalidLayout) {
                invalidLayout = false;
                if (widget == scene) {
                    widget.onMeasure();
                    widget.onLayout(width, height);
                } else {
                    Parent parent = widget.getParent();
                    if (parent == null) {
                        widget.onMeasure();
                        widget.onLayout(widget.getLayoutWidth(), widget.getLayoutHeight());
                    } else if (parent.isWrapContent() || !parent.onLayoutSingleChild(widget)) {
                        parent.onMeasure();
                        parent.onLayout(parent.getLayoutWidth(), parent.getLayoutHeight());
                        widget = parent;
                    }
                }
            }
            if (widget.getActivity() != this) {
                invalidRect = new Rectangle(0, 0, getWidth(), getHeight());
            } else {
                float e = Dimension.dpPx(16f, getDensity()) * 2f;
                Vector2 p1 = widget.localToScreen(-e, -e);
                Vector2 p2 = widget.localToScreen(widget.getLayoutWidth() + e, -e);
                Vector2 p3 = widget.localToScreen(-e, widget.getLayoutHeight() + e);
                Vector2 p4 = widget.localToScreen(widget.getLayoutWidth() + e, widget.getLayoutHeight() + e);
                float minX = Mathf.floor(Math.min(p1.x, Math.min(p2.x, Math.min(p3.x, p4.x))));
                float minY = Mathf.floor(Math.min(p1.y, Math.min(p2.y, Math.min(p3.y, p4.y))));
                float maxX = Mathf.ceil(Math.max(p1.x, Math.max(p2.x, Math.max(p3.x, p4.x))));
                float maxY = Mathf.ceil(Math.max(p1.y, Math.max(p2.y, Math.max(p3.y, p4.y))));
                invalidRect = new Rectangle(Math.max(0, minX), Math.max(0, minY),
                        Math.min(getWidth(), maxX - minX), Math.min(getHeight(), maxY - minY));
            }
        }
        if (repaintRect != null) {
            if (invalidRect == null) {
                invalidRect = repaintRect;
            } else {
                invalidRect.add(repaintRect);
            }
            repaintRect = null;
            invalidRect.x = Math.max(0, invalidRect.x);
            invalidRect.y = Math.max(0, invalidRect.y);
            invalidRect.width = Math.min(getWidth(), invalidRect.width);
            invalidRect.height = Math.min(getHeight(), invalidRect.height);
        }
    }

    boolean draw(Graphics graphics) {
        if (renderPartial && !continuousRendering) {
            if (invalidRect != null) {
                onDraw(graphics);
                invalidRect = null;
                return true;
            }
        } else {
            if (invalidRect != null || continuousRendering) {
                onDrawDefault(graphics);
                invalidRect = null;
                return true;
            }
        }
        return false;
    }

    void show() {
        refreshScene();
        layout(getWindow().getClientWidth(), getWindow().getClientHeight());

        if (controller != null) {
            controller.setRoot(scene);
            controller.setActivity(this);
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
            controller.setActivity(null);
        }
    }

    private void drawBackground(Graphics graphics) {
        graphics.clear(scene.getBackgroundColor(), 1, 0);
    }

    private void drawWidgets(Graphics graphics) {
        scene.onDraw(graphics);
    }

    private void drawFocus(Graphics graphics) {
        if (focusAnim > 0) {
            if (focus != null && focus.isFocused() && focus.getActivity() == this) {
                float anim = Math.min(1, (1 - Math.abs((focusAnim - 0.5f) * 2f)) * 4f);
                float bb = focus.getFocusWidth() * 0.5f;
                var bg = focus.getBackgroundShape();
                int color = focus.getFocusColor();
                color = Color.multiplyColorAlpha(color, anim);
                graphics.setTransform2D(focus.getTransform());
                graphics.setColor(color);
                graphics.setStroke(new BasicStroke(bb * 2));
                graphics.drawRoundRect(
                        bg.x - bb, bg.y - bb, bg.width + bb * 2, bg.height + bb * 2,
                        bg.arcTop + bb, bg.arcRight + bb, bg.arcBottom + bb, bg.arcLeft + bb, false);
            }
            invalidateWidget(scene, false);
            focusAnim -= Application.getLoopTime() * 0.5f;
        }
    }

    private void drawController(Graphics graphics) {
        if (controller != null && controller.isListening()) {
            try {
                controller.onDraw(new DrawEvent(scene, graphics));
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    public void setRenderPartialEnabled(boolean renderPartial) {
        this.renderPartial = renderPartial;
    }

    public boolean isRenderPartialEnabled() {
        return renderPartial;
    }

    private void onDrawDefault(Graphics graphics) {
        drawBackground(graphics);
        drawWidgets(graphics);
        drawFocus(graphics);
        drawController(graphics);
    }

    private void onDraw(Graphics graphics) {
        Rectangle rect = invalidRect;
        if (invalidRectPrev != null) {
            rect = new Rectangle(invalidRect);
            rect.add(invalidRectPrev);
        }
        graphics.setScissor(
                Mathf.round(rect.x), Mathf.round(rect.y),
                Mathf.round(rect.width), Mathf.round(rect.height));
        drawBackground(graphics);
        drawWidgets(graphics);
        drawFocus(graphics);
        drawController(graphics);
        graphics.clearScissor();
        invalidRectPrev = invalidRect;
    }

    private void refreshFocus() {
        if (focus != null && focus.getActivity() != this) {
            setFocus(null);
        }
    }

    private void clearUnusedFilters() {
        pointerFilters.removeIf(widget -> widget.getActivity() != this);
        keyFilters.removeIf(widget -> widget.getActivity() != this);
        focusables.removeIf(widget -> widget.getActivity() != this);
        keyListeners.removeIf(widget -> widget.getActivity() != this);
        focusListener.removeIf(widget -> widget.getActivity() != this);
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
                widget.firePointer(event);
                if (event.isConsumed()) {
                    break;
                }
            }
        }
        filtersTemp.clear();
    }

    public void addFocusableWidget(Widget widget) {
        if (widget.getActivity() == this) {
            focusables.add(widget);
        }
    }

    public void removeFocusableWidget(Widget widget) {
        focusables.remove(widget);
    }

    public void addKeyListener(Widget widget) {
        if (widget.getActivity() == this && !keyListeners.contains(widget)) {
            keyListeners.remove(widget);
            keyListeners.add(widget);
        }
    }

    public void removeKeyListener(Widget widget) {
        keyListeners.remove(widget);
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
        if (controller != null && controller.isListening()) {
            try {
                controller.onKeyFilter(event);
            } catch (Exception e) {
                Application.handleException(e);
            }
        }

        filtersTemp.addAll(keyFilters);
        for (int i = filtersTemp.size() - 1; i >= 0; i--) {
            var widget = filtersTemp.get(i);
            if (widget.getActivity() == this) {
                widget.fireKey(event);
                if (event.isConsumed()) {
                    break;
                }
            }
        }
        filtersTemp.clear();
    }

    public void onKey(KeyEvent event) {
        if (controller != null && controller.isListening()) {
            try {
                controller.onKey(event);
            } catch (Exception e) {
                Application.handleException(e);
            }
        }

        filtersTemp.addAll(keyListeners);
        for (int i = keyListeners.size() - 1; i >= 0; i--) {
            var widget = keyListeners.get(i);
            if (widget.getActivity() == this) {
                widget.fireKey(event);
                if (event.isConsumed()) {
                    break;
                }
            }
        }
        filtersTemp.clear();

        if (event.isConsumed() || event.getType() != KeyEvent.PRESSED || event.getKeycode() != KeyCode.KEY_TAB) {
            return;
        }

        if (focus != null) {
            if (event.isShiftDown()) {
                if (focus.getPrevFocusId() != null) {
                    Widget prev = findById(focus.getPrevFocusId());
                    if (prev != null) {
                        setFocusByKeyboard(prev);
                        return;
                    }
                }
            } else {
                if (focus.getNextFocusId() != null) {
                    Widget next = findById(focus.getNextFocusId());
                    if (next != null) {
                        setFocusByKeyboard(next);
                        return;
                    }
                }
            }
        }

        int strideW = (int) Math.floor(getWidth() / 8) + 1;
        int strideH = (int) Math.floor(getHeight() / 8) + 1;
        Vector2 base = focus == null ? new Vector2(0, 0) : focus.localToScreen(0, 0);
        int baseX = (int) Math.floor(base.x / 8);
        int baseY = (int) Math.floor(base.y / 8);
        if (baseX < 0 || baseX > strideW) baseX = 0;
        if (baseY < 0 || baseY > strideH) baseY = 0;
        int baseI = focus == null ? -1 : baseX + baseY * strideW;

        Widget next = null;
        Widget prev = null;
        Widget first = null;
        Widget last = null;
        int nextI = -1;
        int prevI = -1;
        int firstI = -1;
        int lastI = -1;
        for (var widget : focusables) {
            if (!widget.isFocusable() || widget.getActivity() != this || widget == focus) continue;
            Vector2 current = widget.localToScreen(0, 0);
            int currentX = (int) Math.floor(current.x / 8);
            int currentY = (int) Math.floor(current.y / 8);
            int currentI = currentX + currentY * strideW;
            if (currentX < 0 || currentX > strideW) continue;
            if (currentY < 0 || currentY > strideH) continue;
            Vector2 current2 = widget.localToScreen(widget.getOutWidth() / 2, widget.getOutHeight() / 2);
            if (findByPosition(widget.getOutX() + current2.x, widget.getOutY() + current2.y, false) != widget) continue;

            if (currentI > baseI && (currentI < nextI || nextI == -1)) {
                next = widget;
                nextI = currentI;
            }
            if (currentI < baseI && (currentI > prevI || prevI == -1)) {
                prev = widget;
                prevI = currentI;
            }
            if (currentI < firstI || firstI == -1) {
                first = widget;
                firstI = currentI;
            }
            if (currentI > lastI || lastI == -1) {
                last = widget;
                lastI = currentI;
            }
        }

        if (event.isShiftDown()) {
            setFocusByKeyboard(prev == null ? last : prev);
        } else {
            setFocusByKeyboard(next == null ? first : next);
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
                widget.fireResize();
            }
        }
        filtersTemp.clear();
    }
    
    public void addFocusListener(Widget widget) {
        if (widget.getActivity() == this && !focusListener.contains(widget)) {
            focusListener.add(widget);
        }
    }
    
    public void removeFocusListener(Widget widget) {
        focusListener.remove(widget);
    }
    
    private void onFocusChanged(FocusEvent event) {
        filtersTemp.addAll(focusListener);
        for (int i = focusListener.size() - 1; i >= 0; i--) {
            var widget = focusListener.get(i);
            if (widget.getActivity() == this) {
                widget.fireFocus(event);
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
            oldFocus.fireFocus(new FocusEvent(oldFocus, focus, FocusEvent.LOST));
        }
        if (focus != null) {
            focus.fireFocus(new FocusEvent(focus, focus, FocusEvent.OWNED));
        }
        focusAnim = 0;
        onFocusChanged(new FocusEvent(oldFocus, focus, FocusEvent.OBSERVER));
    }

    public void setFocusByKeyboard(Widget widget) {
        setFocus(widget);
        focusAnim = 1f;
    }

    public Widget getFocus() {
        return focus;
    }

    public Widget getKeyFocus() {
        return focus == null ? scene : focus;
    }

    boolean animate(float loopTime) {
        animations.addAll(animationsAdd);
        animations.removeAll(animationsRemove);
        animationsAdded.addAll(animationsAdd);
        animationsRemoved.addAll(animationsRemove);

        animationsAdd.clear();
        animationsRemove.clear();

        for (var anim : animationsRemoved) {
            anim.onRemoved();
        }
        for (var anim : animationsAdded) {
            anim.onAdded();
        }
        animationsAdded.clear();
        animationsRemoved.clear();

        boolean wasAnimated = false;

        for (int i = 0; i < animations.size(); i++) {
            Animation anim = animations.get(i);
            if (anim.getSource() != this) {
                anim.onRemoved();
                animations.remove(i--);
                continue;
            }

            if (anim.isPlaying()) {
                wasAnimated = true;
                anim.handle(loopTime);
            }
            if (!anim.isPlaying()) {
                anim.onRemoved();
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

    public void repaint() {
        invalidateWidget(scene, false);
    }

    private Rectangle repaintRect;

    public void repaintRect(float x, float y, float width, float height) {
        if (repaintRect != null) {
            repaintRect.add(x, y, width, height);
        } else {
            repaintRect = new Rectangle(x, y, width, height);
        }
    }

    public void repaintRect(Rectangle rect) {
        if (repaintRect != null) {
            repaintRect.add(rect);
        } else {
            repaintRect = new Rectangle(rect);
        }
    }

    public void invalidateWidget(Widget widget, boolean layout) {
        if (invalidWidget != scene && widget.getActivity() == this) {
            if (invalidWidget != null) {
                Widget parent = null;
                var w = widget;
                while (w != null) {
                    if (invalidParents.contains(w)) {
                        parent = w;
                        break;
                    }
                    w = w.getParent();
                }
                if (parent != null) {
                    setInvalidWidget(parent);
                } else {
                    setInvalidWidget(scene);
                }
            } else {
                setInvalidWidget(widget);
            }
        }
        invalidLayout = invalidLayout || layout;
    }

    private void setInvalidWidget(Widget widget) {
        invalidParents.clear();
        var w = widget;
        while ((w = w.getParent()) != null) {
            invalidParents.add(w);
        }
        invalidWidget = widget;
    }

    private void invalidateThemeStyle() {
        invalidThemeStyle = true;
        invalidateWidget(scene, true);
    }

    public Widget findById(String id) {
        return scene.findById(id);
    }

    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        Widget child = scene.findByPosition(x, y, includeDisabled);
        return child == null ? scene : child;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getDensity() {
        return lastDpi;
    }

    public boolean isContinuousRendering() {
        return continuousRendering;
    }

    public void setContinuousRendering(boolean continuousRendering) {
        this.continuousRendering = continuousRendering;
    }
}
