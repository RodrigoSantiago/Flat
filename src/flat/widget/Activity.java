package flat.widget;

import flat.Weak;
import flat.animations.Animation;
import flat.events.FocusEvent;
import flat.events.KeyCode;
import flat.events.KeyEvent;
import flat.graphics.SmartContext;
import flat.uxml.*;
import flat.resources.Dimension;
import flat.resources.DimensionStream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

public class Activity extends Controller {

    private Scene scene;
    private ArrayList<Scene> menus;
    private LinkedList<Weak<Animation>> animations;
    private LinkedList<Weak<Animation>> animationsCp;
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

    public Activity() {
        menus = new ArrayList<>();
        animations = new LinkedList<>();
        animationsCp = new LinkedList<>();

        scene = new Scene();
        scene.activity = this;
        scene.applyAttributes(new UXStyleAttrs("attributes", (UXStyle)null), this);
        scene.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scene.setMinSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scene.setMaxSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);

        color = 0xDDDDDDFF;
    }

    @Override
    public boolean isListening() {
        return !hide;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        if (this.stream != null || this.nextScene != scene) {
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

    public void addAnimation(final Animation animation) {
        Weak<Animation> w = new Weak<>(animation);
        if (!animations.contains(w)) {
            animations.add(w);
        }
    }

    public void removeAnimation(final Animation animation) {
        for (Iterator<Weak<Animation>> iterator = animations.iterator(); iterator.hasNext(); ) {
            if (iterator.next().get() == animation) {
                iterator.remove();
                break;
            }
        }
    }

    /**
     * Called when activity is attached to Application, even before animations
     */
    public void onShow() {

    }

    /**
     * Called after activity transition, or, imediatily when no animations is required
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
                this.scene.onActivityChange(this, null);
                this.scene = newScene;
                this.scene.activity = this;
                this.scene.onActivityChange(null, this);
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
                        this.scene.onActivityChange(this, null);
                        this.scene = newScene;
                        this.scene.activity = this;
                        this.scene.onActivityChange(null, this);
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
        context.clearClip(false);

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

    public void showMenu(Scene menu, float x, float y) {
        if (menu.activity != this) {
            Activity prev = menu.activity;
            if (menu.activity != null) {
                menu.activity.hideMenu(menu);
            }

            menu.setParent(null);
            menus.add(menu);
            menu.setPosition(x, y);
            menu.activity = this;
            menu.onActivityChange(prev, this);
        }
        invalidate(true);
    }

    public void hideMenu(Scene menu) {
        if (menu.activity == this) {
            menus.remove(menu);
            menu.activity = null;
            menu.onActivityChange(this, null);
        }
        invalidate(false);
    }

    final void animate(long loopTime) {
        ArrayList<Weak<Animation>> list = new ArrayList<>();

        for (Weak<Animation> w : animations) {
            Animation anim = w.get();
            if (anim != null) {
                if (anim.isPlaying()) {
                    anim.handle(loopTime);
                }
                if (!anim.isPlaying()) {
                    list.add(w);
                }
            } else {
                list.add(w);
            }
        }

        animations.removeAll(list);
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

    public final void invalidate(boolean layout) {
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
