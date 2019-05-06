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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class Activity extends Controller {

    private Scene scene;
    private ArrayList<Menu> menus;
    private LinkedList<Weak<Animation>> animations;
    private Widget focus;

    private float width;
    private float height;
    private int color;

    private Dimension dimension;
    private DimensionStream stream;
    private UXTheme theme;
    private boolean invalided, layoutInvalided, streamInvalided;

    public Activity() {
        menus = new ArrayList<>();
        animations = new LinkedList<>();

        scene = new Scene();
        scene.activity = this;
        scene.applyAttributes(new UXStyleAttrs("attributes", (UXStyle)null), this);
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
        this.theme.setDimension(dimension);
        streamInvalided = true;
        invalidate(true);
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

    public void onAnimate(long loopTime) {
        for (Iterator<Weak<Animation>> iterator = animations.iterator(); iterator.hasNext(); ) {
            Weak<Animation> w = iterator.next();
            Animation anim = w.get();
            if (anim != null) {
                if (anim.isPlaying()) {
                    anim.handle(loopTime);
                }
                if (!anim.isPlaying()) {
                    iterator.remove();
                }
            } else {
                iterator.remove();
            }
        }
    }

    public void onShow() {

    }

    public void onStart() {

    }

    public void onPause() {

    }

    public void onHide() {

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
                        this.scene = newScene;
                        this.scene.activity = this;
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
        for (Menu menu : menus) {
            menu.onMeasure();
        }

        scene.onLayout(width, height);
        for (Menu menu : menus) {
            menu.onLayout(Math.min(width, menu.getMeasureWidth()), Math.max(height, menu.getMeasureHeight()));
        }
    }

    public void onDraw(SmartContext context) {
        context.setAntialiasEnabled(true);
        context.setView(0, 0, (int) getWidth(), (int) getHeight());
        context.clear(color, 1, 0);
        context.clearClip(false);
        scene.onDraw(context);

        for (Menu menu : menus) {
            menu.onDraw(context);
        }
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

    public void showMenu(Menu menu, float x, float y) {
        if (menu.activity != this) {
            if (menu.activity != null) {
                menu.activity.hideMenu(menu);
            }

            menu.setParent(null);
            menus.add(menu);
            menu.activity = this;
            menu.setPosition(x, y);
        }
        invalidate(true);
    }

    public void hideMenu(Menu menu) {
        if (menu.activity == this) {
            menus.remove(menu);
            menu.activity = null;
        }
        invalidate(false);
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

    final boolean layout() {
        if (layoutInvalided) {
            invalided = true;
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
        for (Menu menu : menus) {
            Widget widget = menu.findById(id);
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
        for (Menu menu : menus) {
            Widget widget = menu.findFocused();
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
