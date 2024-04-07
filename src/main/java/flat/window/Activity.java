package flat.window;

import flat.animations.Animation;
import flat.events.FocusEvent;
import flat.events.KeyCode;
import flat.events.KeyEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Context;
import flat.resources.Dimension;
import flat.resources.DimensionStream;
import flat.uxml.*;
import flat.widget.Scene;
import flat.widget.Widget;

import java.util.ArrayList;
import java.util.Objects;

public class Activity extends Controller {

    private final Context context;
    private Scene scene;
    private ArrayList<Scene> menus = new ArrayList<>();
    private ArrayList<Animation> animations = new ArrayList<>();
    private ArrayList<Animation> animationsAdd = new ArrayList<>();
    private ArrayList<Animation> animationsRemove = new ArrayList<>();
    private Widget focus;

    private float width;
    private float height;
    private int color;

    private Scene nextScene;
    private Dimension dimension;
    private DimensionStream stream;
    private UXTheme theme;
    private boolean invalided, layoutInvalided, streamInvalided;

    private boolean hide;

    public Activity(Context context) {
        this.context = context;

        scene = new Scene();
        scene.setActivity(this);

        scene.applyAttributes(new UXStyleAttrs("attributes", (UXStyle)null), this);
        scene.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scene.setMinSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scene.setMaxSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);

        color = 0xDDDDDDFF;
    }

    public Context getContext() {
        return context;
    }

    public Window getWindow() {
        return context.getWindow();
    }

    @Override
    public boolean isListening() {
        return !hide;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        if (this.scene != scene && (this.stream != null || this.nextScene != scene)) {
            this.stream = null;
            this.nextScene = scene;
            invalidate(true);
        }
    }

    public void setSceneStream(DimensionStream stream) {
        if (this.stream != stream) {
            this.stream = stream;
            this.nextScene = null;
            streamInvalided = true;
            invalidate(true);
        }
    }

    public DimensionStream getSceneStream() {
        return stream;
    }

    public int getBackgroundColor() {
        return color;
    }

    public void setBackgroundColor(int color) {
        this.color = color;
    }

    public UXTheme getTheme() {
        return theme;
    }

    public void setTheme(UXTheme theme) {
        if (!Objects.equals(this.theme, theme)) {
            this.theme = theme;
            this.theme.setDimension(dimension);
            streamInvalided = true;
            invalidate(true);
        }
    }

    public void addAnimation(Animation animation) {
        animationsAdd.add(animation);
        animationsRemove.remove(animation);
    }

    public void removeAnimation(Animation animation) {
        animationsRemove.add(animation);
        animationsAdd.remove(animation);
    }

    /**
     * Called when activity is attached to Application, even before animations
     */
    public void onShow() {

    }

    /**
     * Called after activity transition
     */
    public void onStart() {

    }

    /**
     * Can be used to stop heavy process during transitions or before hiding
     */
    public void onPause() {

    }

    /**
     * Called when the activity is deatached from Application
     */
    public void onHide() {
        hide = true;
    }

    /**
     * Called Before SceneStream load a new Scene behavior
     */
    public void onSave() {

    }

    /**
     * Called After SceneStream load a new Scene or User has setted manually the screen (aways on onLayout event)
     */
    public void onLoad() {

    }

    public boolean onCloseRequest(boolean systemRequest) {

        return true;
    }

    /**
     * Called when the size or dpi changes. Called when a member has a significative size change
     *
     * @param width
     * @param height
     * @param dpi
     */
    public void onLayout(float width, float height, float dpi) {
        if (width != this.width || height != this.height) {
            Dimension dm;
            if (nextScene != null) {
                dm = new Dimension(width, height, dpi);
                theme.setDimension(dm);

                Scene newScene = nextScene;
                onSave();

                Scene oldScene = this.scene;
                this.scene = null;
                oldScene.setActivity(null);

                this.scene = newScene;
                newScene.setActivity(this);

                onLoad();
            } else if (stream != null) {
                dm = stream.getCloserDimension(width, height, dpi);
                theme.setDimension(dm);

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
                        Scene oldScene = this.scene;
                        this.scene = null;
                        oldScene.setActivity(null);

                        this.scene = newScene;
                        newScene.setActivity(this);
                        onLoad();
                    }
                }
            } else {
                dm = new Dimension(width, height, dpi);
                theme.setDimension(dm);
            }
            this.dimension = dm;
            this.width = width;
            this.height = height;
        }

        scene.onMeasure();
        for (Scene menu : menus) {
            menu.onMeasure();
        }

        scene.onLayout(width, height);
        for (Scene menu : menus) {
            menu.onLayout(Math.min(width, menu.mWidth()), Math.max(height, menu.mHeight()));
        }
    }

    /**
     * Called when rendering is needed
     *
     * @param context
     */
    public void onDraw(SmartContext context) {
        context.setAntialiasEnabled(true);
        context.setView(0, 0, (int) getWidth(), (int) getHeight());
        context.clear(color, 1, 0);
        context.clearClip();

        scene.onDraw(context);
        for (Scene menu : menus) {
            menu.onDraw(context);
        }
    }

    /**
     * Called when to atached activity when a key is pressed,released,typed
     * @param event
     */
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

    /**
     * Requested by focusables widgets, or, manually
     *
     * @param widget
     */
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

    public void showMenu(Scene menu, float x, float y) {
        // TODO - IMPLEMENT
        invalidate(true);
    }

    public void hideMenu(Scene menu) {
        // TODO - IMPLEMENT
        invalidate(false);
    }

    final boolean animate(float loopTime) {
        animations.removeAll(animationsRemove);
        animationsRemove.clear();

        animations.addAll(animationsAdd);
        animationsAdd.clear();

        boolean wasAnimated = !animations.isEmpty();

        for (int i = 0; i < animations.size(); i++) {
            Animation anim = animations.get(i);
            if (anim.isPlaying()) {
                anim.handle(loopTime);
            }
            if (!anim.isPlaying()) {
                animations.remove(i--);
            }
        }

        return wasAnimated;
    }

    final void layout(float width, float height, float dpi) {
        if (layoutInvalided) {
            invalided = true;
            layoutInvalided = false;
            onLayout(width, height, dpi);
        }
    }

    final boolean draw(SmartContext context) {
        if (invalided) {
            invalided = false;
            onDraw(context);
            return true;
        } else {
            return false;
        }
    }

    public void invalidate(boolean layout) {
        invalided = true;
        if (layout) {
            layoutInvalided = true;
        }
    }

    public Widget findById(String id) {
        for (int i = menus.size() - 1; i >= 0; i--) {
            Widget widget = menus.get(i).findById(id);
            if (widget != null) {
                return widget;
            }
        }
        return scene.findById(id);
    }

    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        for (int i = menus.size() - 1; i >= 0; i--) {
            Widget widget = menus.get(i).findByPosition(x, y, includeDisabled);
            if (widget != null) {
                return widget;
            }
        }
        Widget child = scene.findByPosition(x, y, includeDisabled);
        return child == null ? scene : child;
    }

    public Widget findFocused() {
        for (int i = menus.size() - 1; i >= 0; i--) {
            Widget widget = menus.get(i).findFocused();
            if (widget != null) {
                return widget;
            }
        }
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
