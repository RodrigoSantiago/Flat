package flat.widget;

import flat.animations.StateAnimation;
import flat.animations.StateBitset;
import flat.animations.StateInfo;
import flat.events.*;
import flat.graphics.SmartContext;
import flat.graphics.cursor.Cursor;
import flat.math.Affine;
import flat.math.Vector2;
import flat.math.shapes.RoundRectangle;
import flat.math.shapes.Shape;
import flat.math.stroke.BasicStroke;
import flat.uxml.*;
import flat.uxml.value.UXValue;
import flat.widget.effects.RippleEffect;
import flat.widget.enuns.Visibility;
import flat.window.Activity;

import java.lang.reflect.Method;
import java.util.*;

public class Widget implements Gadget {

    //---------------------
    //    Constants
    //---------------------
    public static final float WRAP_CONTENT = 0;
    public static final float MATCH_PARENT = Float.POSITIVE_INFINITY;

    public static final int ENABLED = 1 << 0;
    public static final int FOCUSED = 1 << 1;
    public static final int ACTIVATED = 1 << 2;
    public static final int HOVERED = 1 << 3;
    public static final int PRESSED = 1 << 4;
    public static final int DRAGGED = 1 << 5;
    public static final int ERROR = 1 << 6;
    public static final int DISABLED = 1 << 7;

    private static final Comparator<Widget> childComparator = (o1, o2) -> Float.compare(o1.elevation, o2.elevation);

    //---------------------
    //    Properties
    //---------------------
    private String id;
    private String nextFocusId, prevFocusId;
    private boolean focusable;

    private float marginTop, marginRight, marginBottom, marginLeft;
    private float paddingTop, paddingRight, paddingBottom, paddingLeft;

    private float width, height;
    private float minWidth, minHeight, maxWidth = MATCH_PARENT, maxHeight = MATCH_PARENT, prefWidth, prefHeight;
    private float measureWidth, measureHeight;
    private float layoutWidth, layoutHeight;
    private float offsetWidth, offsetHeight;

    private int visibility = Visibility.VISIBLE.ordinal();
    private Cursor cursor = Cursor.UNSET;

    private Menu contextMenu;

    //---------------------
    //    Family
    //---------------------
    Activity activity;
    Parent parent;
    ArrayList<Widget> children;
    List<Widget> unmodifiableChildren;
    boolean invalidChildrenOrder;

    //---------------------
    //    Transform
    //---------------------
    private float x, y, centerX, centerY, translateX, translateY, scaleX = 1, scaleY = 1, rotate, elevation;

    private final Affine transform = new Affine();
    private final Affine inverseTransform = new Affine();
    private boolean invalidTransform;

    //---------------------
    //    Events
    //---------------------
    private boolean clickable = true;
    private PointerListener pointerListener;
    private HoverListener hoverListener;
    private ScrollListener scrollListener;
    private KeyListener keyListener;
    private DragListener dragListener;
    private FocusListener focusListener;

    //---------------------
    //    Style
    //---------------------
    private UXAttrs attrs;
    private UXTheme theme;
    private byte states = 1;
    private StateAnimation stateAnimation;

    private final RoundRectangle bg = new RoundRectangle();
    private float inx, iny, inw, inh;

    private int backgroundColor;
    private boolean borderRound;
    private int borderColor;
    private float borderWidth;
    private float opacity = 1;

    private RippleEffect ripple;
    private int rippleColor;
    private boolean rippleOverflow;

    private boolean shadowEnabled;
    private boolean rippleEnabled;
    private float transitionDuration;

    public Widget() {
        attrs = new UXAttrs(getClass().getSimpleName().toLowerCase());
    }

    @Override
    public void setAttributes(HashMap<Integer, UXValue> attributes, String style) {
        attrs.setAttributes(attributes);
        attrs.setName(style);
    }

    @Override
    public void applyAttributes(UXTheme theme, Controller controller, UXBuilder builder) {
        UXAttrs attrs = getAttrs();
        attrs.setTheme(theme);

        String id = attrs.att("id").asString();
        if (id != null) {
            setId(id);
            if (controller != null) {
                controller.assign(id, this);
            }
        }
        setEnabled(attrs.att("enabled").asBool(null, isEnabled()));

        Method handle = attrs.linkListener("on-pointer", PointerEvent.class, controller);
        if (handle != null) {
            setPointerListener(new PointerListener.AutoPointerListener(controller, handle));
        }
        handle = attrs.linkListener("on-hover", HoverEvent.class, controller);
        if (handle != null) {
            setHoverListener(new HoverListener.AutoHoverListener(controller, handle));
        }
        handle = attrs.linkListener("on-scroll", ScrollEvent.class, controller);
        if (handle != null) {
            setScrollListener(new ScrollListener.AutoScrollListener(controller, handle));
        }
        handle = attrs.linkListener("on-key", KeyEvent.class, controller);
        if (handle != null) {
            setKeyListener(new KeyListener.AutoKeyListener(controller, handle));
        }
        handle = attrs.linkListener("on-drag", DragEvent.class, controller);
        if (handle != null) {
            setDragListener(new DragListener.AutoDragListener(controller, handle));
        }
        handle = attrs.linkListener("on-focus", FocusEvent.class, controller);
        if (handle != null) {
            setFocusListener(new FocusListener.AutoFocusListener(controller, handle));
        }

        setNextFocusId(attrs.att("next-focus-id").asString(null, getNextFocusId()));
        setPrevFocusId(attrs.att("prev-focus-id").asString(null, getPrevFocusId()));
    }

    public void applyStyle() {
        UXAttrs attrs = getAttrs();

        setTransitionDuration(attrs.value("transition-duration").asNumber(null, getTransitionDuration()));

        // Disabled State Overlay
        if (parent != null) {
            if (parent.isDisabled()) {
                if (stateAnimation == null) {
                    stateAnimation = new StateAnimation(this);
                    stateAnimation.set(states);
                }
                if (((Widget)parent).stateAnimation != null) {
                    stateAnimation.setDisabledOverlay(((Widget)parent).stateAnimation.getDisabled());
                } else {
                    stateAnimation.setDisabledOverlay(1);
                }
            } else {
                if (stateAnimation != null) {
                    stateAnimation.unsetDisabledOverlay();
                }
            }
        }

        if (isDisabled()) {
            if (children != null) {
                childSort();
                for (Widget child : getChildrenIterable()) {
                    child.applyStyle();
                }
            }
        }

        StateInfo info = getStateInfo();

        setVisibility(attrs.value("visibility").asConstant(info, getVisibility()));
        setCursor(attrs.value("cursor").asConstant(info, getCursor()));

        setFocusable(attrs.value("focusable").asBool(info, isFocusable()));
        setClickable(attrs.value("clickable").asBool(info, isClickable()));

        setPrefWidth(attrs.value("width").asSize(info, getPrefWidth()));
        setPrefHeight(attrs.value("height").asSize(info, getPrefHeight()));
        setMaxWidth(attrs.value("max-width").asSize(info, getMaxWidth()));
        setMaxHeight(attrs.value("max-height").asSize(info, getMaxHeight()));
        setMinWidth(attrs.value("min-width").asSize(info, getMinWidth()));
        setMinHeight(attrs.value("min-height").asSize(info, getMinHeight()));

        setTranslateX(attrs.value("x").asSize(info, getTranslateX()));
        setTranslateY(attrs.value("y").asSize(info, getTranslateY()));
        setCenterX(attrs.value("center-x").asNumber(info, getCenterX()));
        setCenterY(attrs.value("center-y").asNumber(info, getCenterY()));
        setScaleX(attrs.value("scale-x").asNumber(info, getScaleX()));
        setScaleY(attrs.value("scale-y").asNumber(info, getScaleY()));
        setOpacity(attrs.value("opacity").asNumber(info, getOpacity()));

        setRotate(attrs.value("rotate").asAngle(info, getRotate()));

        setElevation(attrs.value("elevation").asSize(info, getElevation()));
        setShadowEnabled(attrs.value("shadow").asBool(info, isShadowEnabled()));

        setRippleEnabled(attrs.value("ripple").asBool(info, isRippleEnabled()));
        setRippleColor(attrs.value("ripple-color").asColor(info, getRippleColor()));
        setRippleOverflow(attrs.value("ripple-overflow").asBool(info, isRippleOverflow()));

        setMarginTop(attrs.value("margin-top").asSize(info, getMarginTop()));
        setMarginRight(attrs.value("margin-right").asSize(info, getMarginRight()));
        setMarginBottom(attrs.value("margin-bottom").asSize(info, getMarginBottom()));
        setMarginLeft(attrs.value("margin-left").asSize(info, getMarginLeft()));

        setPaddingTop(attrs.value("padding-top").asSize(info, getPaddingTop()));
        setPaddingRight(attrs.value("padding-right").asSize(info, getPaddingRight()));
        setPaddingBottom(attrs.value("padding-bottom").asSize(info, getPaddingBottom()));
        setPaddingLeft(attrs.value("padding-left").asSize(info, getPaddingLeft()));

        setRadiusTop(attrs.value("radius-top").asSize(info, getRadiusTop()));
        setRadiusRight(attrs.value("radius-right").asSize(info, getRadiusRight()));
        setRadiusBottom(attrs.value("radius-bottom").asSize(info, getRadiusBottom()));
        setRadiusLeft(attrs.value("radius-left").asSize(info, getRadiusLeft()));

        setBackgroundColor(attrs.value("background-color").asColor(info, getBackgroundColor()));
        setBorderRound(attrs.value("border-round").asBool(info, isBorderRound()));
        setBorderColor(attrs.value("border-color").asColor(info, getBorderColor()));
        setBorderWidth(attrs.value("border-width").asSize(info, getBorderWidth()));
    }

    @Override
    public void applyChildren(UXChildren children) {
        Menu menu = children.nextMenu();
        if (menu != null) {
            setContextMenu(menu);
        }
    }

    public void applyTheme() {
        attrs.setTheme(getTheme());
        applyStyle();

        for (Widget child : getChildrenIterable()) {
            child.applyTheme();
        }
        if (contextMenu != null) {
            contextMenu.applyTheme();
        }
    }

    public void onDraw(SmartContext context) {
        backgroundDraw(backgroundColor, borderColor, rippleColor, context);
        childrenDraw(context);
    }

    protected void backgroundDraw(int backgroundColor, int borderColor, int rippleColor, SmartContext context) {
        if (getDisplayOpacity() > 0) {
            float b = borderWidth;
            float b2 = borderWidth / 2;

            if ((backgroundColor & 0xFF) > 0 && shadowEnabled) {
                context.setTransform2D(getTransform().preTranslate(0, Math.max(0, elevation)));
                context.drawRoundRectShadow(
                        bg.x - b, bg.y - b, bg.width + b * 2, bg.height + b * 2,
                        bg.arcTop + b, bg.arcRight + b, bg.arcBottom + b, bg.arcLeft + b,
                        elevation * 2, 0.55f * ((backgroundColor & 0xFF) / 255f));
            }

            context.setTransform2D(getTransform());

            if ((backgroundColor & 0xFF) > 0) {
                context.setColor(backgroundColor);
                context.drawRoundRect(bg, true);
            }

            if ((borderColor & 0xFF) > 0 && borderWidth > 0) {
                context.setColor(borderColor);
                context.setStroker(new BasicStroke(borderWidth));
                context.drawRoundRect(
                        bg.x - b2, bg.y - b2, bg.width + b, bg.height + b,
                        bg.arcTop + b2, bg.arcRight + b2, bg.arcBottom + b2, bg.arcLeft + b2,
                        false);
            }

            if ((rippleColor & 0xFF) > 0 && rippleEnabled && ripple.isVisible()) {
                ripple.drawRipple(context, isRippleOverflow() ? null : bg, rippleColor);
            }

            context.setTransform2D(null);
        }
    }

    protected void childrenDraw(SmartContext context) {
        if (children != null) {
            childSort();
            for (Widget child : getChildrenIterable()) {
                if (child.getVisibility() == Visibility.VISIBLE) {
                    child.onDraw(context);
                }
            }
        }
    }

    protected Shape backgroundClip(SmartContext context) {
        context.setTransform2D(getTransform());
        return context.intersectClip(bg);
    }

    /**
     * Calculate the best size (internal)
     *
     * Equation : minsize < ([preferedSize || computedSize] + padding + margins) < maxsize
     */
    public void onMeasure() {
        setMeasure(Math.max(prefWidth + marginLeft + marginRight, lMinWidth()),
                Math.max(prefHeight + marginTop + marginBottom, lMinHeight()));
    }

    /**
     * Define the best size (internal)
     *
     * @param width
     * @param height
     */
    public final void setMeasure(float width, float height) {
        measureWidth = Math.max(width, lMinWidth());
        measureHeight = Math.max(height, lMinHeight());
    }

    public float mWidth()  {
        return measureWidth;
    }

    public float mHeight()  {
        return measureHeight;
    }

    /**
     * Define the position and computate the best size amoung multiples children (internal)
     * @param width
     * @param height
     */
    public void onLayout(float width, float height) {
        setLayout(width, height);
    }

    /**
     * Set the widget real size, based on parent's size (internal)
     * @param width
     * @param height
     */
    public final void setLayout(float width, float height) {
        this.layoutWidth = width;
        this.layoutHeight = height;

        setWidth(Math.max(0, width + offsetWidth));
        setHeight(Math.max(0, height + offsetHeight));
    }

    public float lWidth() {
        return layoutWidth;
    }

    public float lHeight() {
        return layoutHeight;
    }

    public float lMinWidth() {
        return Math.max(minWidth, paddingLeft + paddingRight) + marginLeft + marginRight;
    }

    public float lMinHeight() {
        return Math.max(minHeight, paddingTop + paddingBottom) + marginTop + marginBottom;
    }

    public float lMaxWidth() {
        return maxWidth + marginLeft + marginRight;
    }

    public float lMaxHeight() {
        return maxHeight + marginTop + marginBottom;
    }

    /**
     * Set the widget position (internal)
     * @param x
     * @param y
     */
    public final void setPosition(float x, float y) {
        if (this.x != x || this.y != y) {
            this.x = x;
            this.y = y;
            updateRect();
        }
    }

    public void invalidate(boolean layout) {
        if (parent != null) {
            parent.invalidate(layout);
        }
    }

    protected void invalidateTransform() {
        if (children != null) {
            for (Widget child : getChildrenIterable()) {
                child.invalidateTransform();
            }
        }
        invalidTransform = true;
    }

    protected void invalidateChildrenOrder() {
        invalidChildrenOrder = true;
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final Widget getWidget() {
        return this;
    }

    public void setId(String id) {
        if (!Objects.equals(this.id, id)) {
            String oldId = this.id;
            this.id = id;
            Scene scene = getScene();
            if (scene != null) {
                scene.reassign(oldId, this);
            }
        }
    }

    public Activity getActivity() {
        if (parent != null) {
            return parent.getActivity();
        } else {
            return null;
        }
    }

    public void setTheme(UXTheme theme) {
        if (this.theme != theme) {
            this.theme = theme;

            invalidate(true);
        }
    }

    public UXTheme getTheme() {
        return this.theme != null ? this.theme : parent != null ? parent.getTheme() : null;
    }

    /**
     * Return the top-most scene, direct assigned to an activity
     *
     * @return
     */
    public Scene getScene() {
        Scene scene = null;
        if (parent != null) {
            if (parent.isScene()) {
                scene = (Scene) parent;
            } else {
                scene = parent.getScene();
            }
        }
        return scene;
    }

    boolean isScene() {
        return false;
    }

    public Parent getParent() {
        return parent;
    }

    void setParent(Parent parent) {
        if (parent == this) parent = null;

        if (this.parent != null && parent != null) {
            this.parent.remove(this);
        }

        if (parent != null && parent.isChildOf(this)) {
            parent.getParent().remove(parent);
        }

        Scene sceneA = getScene();
        Activity activityA = sceneA == null ? null : sceneA.getActivity();

        this.parent = parent;

        Scene sceneB = getScene();
        Activity activityB = sceneB == null ? null : sceneB.getActivity();

        if (sceneA != sceneB) {
            if (sceneA != null) {
                sceneA.unassign(this);
            }
            if (sceneB != null) {
                sceneB.assign(this);
            }
            onSceneChange();
        }

        if (activityA != activityB) {
            onActivityChange(activityA, activityB);
        }
    }

    protected void onSceneChange() {
        if (children != null) {
            for (Widget widget : getChildrenIterable()) {
                widget.onSceneChange();
            }
        }
    }

    protected void onActivityChange(Activity prev, Activity activity) {
        refreshFocus();

        if (children != null) {
            for (Widget widget : getChildrenIterable()) {
                widget.onActivityChange(prev, activity);
            }
        }

        if (ripple != null) {
            ripple.onActivityChange(prev, activity);
        }

        if (stateAnimation != null && stateAnimation.isPlaying()) {
            if (prev != null) prev.removeAnimation(stateAnimation);
            if (activity != null) activity.addAnimation(stateAnimation);
        }
    }

    public List<Widget> getUnmodifiableChildren() {
        return unmodifiableChildren;
    }

    protected ArrayList<Widget> getChildren() {
        childSort();
        return children;
    }

    public Children<Widget> getChildrenIterable() {
        childSort();
        return new Children<>(children);
    }

    public Children<Widget> getChildrenIterableReverse() {
        childSort();
        return new Children<>(children, true);
    }

    public Widget findById(String id) {
        if (id == null) return null;

        Scene scene = getScene();
        if (scene != null) {
            return scene.findById(id);
        } else {
            if (children != null) {
                for (Widget child : getChildrenIterable()) {
                    Widget found = child.findById(id);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }

    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        // TODO - reverse order {child -> contains to contains -> child on cliping }

        if ((includeDisabled || isEnabled()) &&
                (getVisibility() == Visibility.VISIBLE || getVisibility() == Visibility.INVISIBLE)) {
            if (children != null) {
                childSort();
                for (Widget child : getChildrenIterableReverse()) {
                    Widget found = child.findByPosition(x, y, includeDisabled);
                    if (found != null) return found;
                }
            }
            return clickable && contains(x, y) ? this : null;
        } else {
            return null;
        }
    }

    public Widget findFocused() {
        if (isFocused()) {
            if (children != null) {
                for (Widget child : getChildrenIterable()) {
                    Widget focus = child.findFocused();
                    if (focus != null) return focus;
                }
            }
            return this;
        } else {
            return null;
        }
    }

    public boolean isChildOf(Widget widget) {
        if (parent != null) {
            if (parent == widget) {
                return true;
            } else {
                return parent.isChildOf(widget);
            }
        } else {
            return false;
        }
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        if (this.clickable != clickable) {
            this.clickable = clickable;
            attrs.unfollow("clickable", clickable);
        }
    }

    public Menu getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(Menu contextMenu) {
        this.contextMenu = contextMenu;
    }

    public void showContextMenu(float x, float y) {
        if (contextMenu != null) {
            Activity act = getActivity();
            if (act != null) {
                contextMenu.onMeasure();
                boolean reverseX = contextMenu.mWidth() + x > act.getWidth();
                boolean reverseY = contextMenu.mHeight() + y > act.getHeight();
                contextMenu.show(act,
                        reverseX ? x - contextMenu.mWidth() : x,
                        reverseY ? y - contextMenu.mHeight() : y);
            }
        }
    }

    public void hideContextMenu() {
        if (contextMenu != null) {
            contextMenu.hide();
        }
    }

    // ---- STATES ---- //
    protected void setStates(byte bitmask) {
        if (states != bitmask) {
            boolean applyStyle = getAttrs() != null && getAttrs().containsChange(states, bitmask);
            states = bitmask;

            if (transitionDuration > 0) {
                if (applyStyle) {
                    if (stateAnimation == null) {
                        stateAnimation = new StateAnimation(this);
                    }
                    stateAnimation.play(bitmask);
                } else if (stateAnimation != null && stateAnimation.isPlaying()) {
                    stateAnimation.play(bitmask);
                } else if (stateAnimation != null) {
                    stateAnimation.set(bitmask);
                }
            } else if (applyStyle) {
                applyStyle();
                invalidate(false);
            }
        }
    }

    protected byte getStateBitset() {
        return states;
    }

    protected StateInfo getStateInfo() {
        return stateAnimation != null ? stateAnimation : StateBitset.getState(states);
    }

    protected UXAttrs getAttrs() {
        return this.attrs;
    }

    public String getStyle() {
        return this.attrs.getName();
    }

    public void setStyle(String style) {
        if (!Objects.equals(this.attrs.getName(), style)) {
            attrs.setName(style);
            applyStyle();
        }
    }

    public void unfollowStyleProperty(String name) {
        if (attrs != null) {
            attrs.unfollow(name);
        }
    }

    public float getTransitionDuration() {
        return transitionDuration;
    }

    public void setTransitionDuration(float transitionDuration) {
        transitionDuration = Math.max(transitionDuration, 0);

        if (this.transitionDuration != transitionDuration) {
            this.transitionDuration = transitionDuration;
            attrs.unfollow("transition-duration", transitionDuration);

            if (transitionDuration == 0) {
                if (stateAnimation != null) {
                    stateAnimation.set(states);
                }
            } else {
                if (stateAnimation == null) {
                    stateAnimation = new StateAnimation(this);
                    stateAnimation.set(states);
                }
                stateAnimation.setDuration(transitionDuration);
            }
        }
    }

    public boolean isDisabled() {
        return parent == null ? !isEnabled() : parent.isDisabled() || !isEnabled();
    }

    public boolean isEnabled() {
        return (states & DISABLED) != DISABLED;
    }

    public void setEnabled(boolean enabled) {
        if (isEnabled() != enabled) {
            setStates((byte) (enabled ? states | DISABLED : states & ~DISABLED));
        }
    }

    public boolean isActivated() {
        return (states & ACTIVATED) == ACTIVATED;
    }

    protected void setActivated(boolean actived) {
        if (isActivated() != actived) {
            setStates((byte) (actived ? states | ACTIVATED : states & ~ACTIVATED));
        }
    }

    public boolean isHovered() {
        return (states & HOVERED) == HOVERED;
    }

    protected void setHovered(boolean hovered) {
        if (isHovered() != hovered) {
            setStates((byte) (hovered ? states | HOVERED : states & ~HOVERED));
        }
    }

    public boolean isPressed() {
        return (states & PRESSED) == PRESSED;
    }

    protected void setPressed(boolean pressed) {
        if (isPressed() != pressed) {
            setStates((byte) (pressed ? states | PRESSED : states & ~PRESSED));
        }
    }

    public boolean isDragged() {
        return (states & DRAGGED) == DRAGGED;
    }

    protected void setDragged(boolean dragged) {
        if (isDragged() != dragged) {
            setStates((byte) (dragged ? states | DRAGGED : states & ~DRAGGED));
        }
    }

    public boolean isError() {
        return (states & ERROR) == ERROR;
    }

    protected void setError(boolean error) {
        if (isError() != error) {
            setStates((byte) (error ? states | ERROR : states & ~ERROR));
        }
    }

    public void refreshFocus() {
        Activity activity = getActivity();
        setFocused(activity != null && activity.getFocus() == this);
    }

    public boolean isFocused() {
        return (states & FOCUSED) == FOCUSED;
    }

    protected void setFocused(boolean focused) {
        if (isFocused() != focused) {
            Activity activity = getActivity();
            if (activity != null) {
                if (focused) {
                    if (focusable) {
                        setStates((byte) (states | FOCUSED));
                        if (activity.getFocus() != this) {
                            activity.setFocus(this);
                        }
                    }
                } else {
                    setStates((byte) (states & ~FOCUSED));
                    if (activity.getFocus() == this) {
                        activity.setFocus(null);
                    }
                }
            }
        }
    }

    public void requestFocus(boolean focus) {
        if (focusable) {
            Activity activity = getActivity();
            if (activity != null && activity.getWindow() != null) {
                activity.getWindow().runSync(() -> setFocused(focus));
            }
        }
    }

    public boolean isFocusable() {
        return focusable;
    }

    public void setFocusable(boolean focusable) {
        if (this.focusable != focusable) {
            this.focusable = focusable;
            attrs.unfollow("focusable", focusable);

            if (!focusable) {
                setFocused(false);
            }
        }
    }

    public String getNextFocusId() {
        return nextFocusId;
    }

    public void setNextFocusId(String nextFocusId) {
        this.nextFocusId = nextFocusId;
    }

    public String getPrevFocusId() {
        return prevFocusId;
    }

    public void setPrevFocusId(String prevFocusId) {
        this.prevFocusId = prevFocusId;
    }

    public Vector2 localToScreen(float x, float y) {
        Vector2 point = new Vector2(x, y);
        localToScreen(point);
        return point;
    }

    public void localToScreen(Vector2 point) {
        transform();
        float x = transform.pointX(point.x, point.y);
        float y = transform.pointY(point.x, point.y);
        point.x = x;
        point.y = y;
    }

    public Vector2 screenToLocal(float x, float y) {
        Vector2 point = new Vector2(x, y);
        screenToLocal(point);
        return point;
    }

    public void screenToLocal(Vector2 point) {
        transform();
        float x = inverseTransform.pointX(point.x, point.y);
        float y = inverseTransform.pointY(point.x, point.y);
        point.x = x;
        point.y = y;
    }

    public boolean contains(float x, float y) {
        transform();
        float px = inverseTransform.pointX(x, y);
        float py = inverseTransform.pointY(x, y);
        return bg.contains(px, py);
    }

    protected float getInX() {
        return inx;
    }

    protected float getInY() {
        return iny;
    }

    public float getInWidth() {
        return inw;
    }

    public float getInHeight() {
        return inh;
    }

    protected float getOutX() {
        return bg.x;
    }

    protected float getOutY() {
        return bg.y;
    }

    public float getOutWidth() {
        return bg.width;
    }

    public float getOutHeight() {
        return bg.height;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    void setWidth(float width) {
        if (this.width != width) {
            this.width = width;
            updateRect();
        }
    }

    public float getHeight() {
        return height;
    }

    void setHeight(float height) {
        if (this.height != height) {
            this.height = height;
            updateRect();
        }
    }

    public float getOffsetWidth() {
        return offsetWidth;
    }

    public void setOffsetWidth(float offsetWidth) {
        if (this.offsetWidth != offsetWidth) {
            this.offsetWidth = offsetWidth;
            invalidate(true);
        }
    }

    public float getOffsetHeight() {
        return offsetHeight;
    }

    public void setOffsetHeight(float offsetHeight) {
        if (this.offsetHeight != offsetHeight) {
            this.offsetHeight = offsetHeight;
            invalidate(true);
        }
    }

    public float getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(float marginTop) {
        if (this.marginTop != marginTop) {
            this.marginTop = marginTop;
            attrs.unfollow("margin-top", marginTop);

            updateRect();
            invalidate(true);
        }
    }

    public float getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(float marginRight) {
        if (this.marginRight != marginRight) {
            this.marginRight = marginRight;
            attrs.unfollow("margin-right", marginRight);

            updateRect();
            invalidate(true);
        }
    }

    public float getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(float marginBottom) {
        if (this.marginBottom != marginBottom) {
            this.marginBottom = marginBottom;
            attrs.unfollow("margin-bottom", marginBottom);

            updateRect();
            invalidate(true);
        }
    }

    public float getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(float marginLeft) {
        if (this.marginLeft != marginLeft) {
            this.marginLeft = marginLeft;
            attrs.unfollow("margin-left", marginLeft);

            updateRect();
            invalidate(true);
        }
    }

    public void setMargins(float top, float right, float bottom , float left) {
        if (marginTop != top || marginRight != right || marginBottom != bottom || marginLeft != left) {
            marginTop = top;
            marginRight = right;
            marginBottom = bottom;
            marginLeft = left;
            attrs.unfollow("margin-top", marginTop);
            attrs.unfollow("margin-right", marginRight);
            attrs.unfollow("margin-bottom", marginBottom);
            attrs.unfollow("margin-left", marginLeft);

            updateRect();
            invalidate(true);
        }
    }

    public float getPaddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(float paddingTop) {
        if (this.paddingTop != paddingTop) {
            this.paddingTop = paddingTop;
            attrs.unfollow("padding-top", paddingTop);

            invalidate(true);
        }
    }

    public float getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(float paddingRight) {
        if (this.paddingRight != paddingRight) {
            this.paddingRight = paddingRight;
            attrs.unfollow("padding-right", paddingRight);

            invalidate(true);
        }
    }

    public float getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(float paddingBottom) {
        if (this.paddingBottom != paddingBottom) {
            this.paddingBottom = paddingBottom;
            attrs.unfollow("padding-bottom", paddingBottom);

            invalidate(true);
        }
    }

    public float getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(float paddingLeft) {
        if (this.paddingLeft != paddingLeft) {
            this.paddingLeft = paddingLeft;
            attrs.unfollow("padding-left", paddingLeft);

            invalidate(true);
        }
    }

    public void setPadding(float top, float right, float bottom , float left) {
        if (paddingTop != top || paddingRight != right || paddingBottom != bottom || paddingLeft != left) {
            paddingTop = top;
            paddingRight = right;
            paddingBottom = bottom;
            paddingLeft = left;
            attrs.unfollow("padding-top", paddingTop);
            attrs.unfollow("padding-right", paddingRight);
            attrs.unfollow("padding-bottom", paddingBottom);
            attrs.unfollow("padding-left", paddingLeft);

            invalidate(true);
        }
    }

    public float getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(float minWidth) {
        if (this.minWidth != minWidth) {
            this.minWidth = minWidth;
            attrs.unfollow("min-width", minWidth);

            invalidate(true);
        }
    }

    public float getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(float minHeight) {
        if (this.minHeight != minHeight) {
            this.minHeight = minHeight;
            attrs.unfollow("min-height", minHeight);

            invalidate(true);
        }
    }

    public void setMinSize(float minWidth, float minHeight) {
        if (this.minWidth != minWidth || this.minHeight != minHeight) {
            this.minWidth = minWidth;
            this.minHeight = minHeight;
            attrs.unfollow("min-width", minWidth);
            attrs.unfollow("min-height", minHeight);

            invalidate(true);
        }
    }

    public float getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(float maxWidth) {
        if (this.maxWidth != maxWidth) {
            this.maxWidth = maxWidth;
            attrs.unfollow("max-width", maxWidth);

            invalidate(true);
        }
    }

    public float getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(float maxHeight) {
        if (this.maxHeight != maxHeight) {
            this.maxHeight = maxHeight;
            attrs.unfollow("max-height", maxHeight);

            invalidate(true);
        }
    }

    public void setMaxSize(float maxWidth, float maxHeight) {
        if (this.maxWidth != maxWidth || this.maxHeight != maxHeight) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            attrs.unfollow("max-width", maxWidth);
            attrs.unfollow("max-height", maxHeight);

            invalidate(true);
        }
    }

    public float getPrefWidth() {
        return prefWidth;
    }

    public void setPrefWidth(float prefWidth) {
        if (this.prefWidth != prefWidth) {
            this.prefWidth = prefWidth;
            attrs.unfollow("width", prefWidth);

            invalidate(true);
        }
    }

    public float getPrefHeight() {
        return prefHeight;
    }

    public void setPrefHeight(float prefHeight) {
        if (this.prefHeight != prefHeight) {
            this.prefHeight = prefHeight;
            attrs.unfollow("height", prefHeight);

            invalidate(true);
        }
    }

    public void setPrefSize(float prefWidth, float prefHeight) {
        if (this.prefWidth != prefWidth || this.prefHeight != prefHeight) {
            this.prefWidth = prefWidth;
            this.prefHeight = prefHeight;
            attrs.unfollow("width", prefWidth);
            attrs.unfollow("height", prefHeight);

            invalidate(true);
        }
    }

    public float getCenterX() {
        return centerX;
    }

    public void setCenterX(float centerX) {
        if (this.centerX != centerX) {
            this.centerX = centerX;
            attrs.unfollow("center-x", centerX);

            invalidate(false);
            invalidateTransform();
        }
    }

    public float getCenterY() {
        return centerY;
    }

    public void setCenterY(float centerY) {
        if (this.centerY != centerY) {
            this.centerY = centerY;
            attrs.unfollow("center-y", centerY);

            invalidate(false);
            invalidateTransform();
        }
    }

    public float getTranslateX() {
        return translateX;
    }

    public void setTranslateX(float translateX) {
        if (this.translateX != translateX) {
            this.translateX = translateX;
            attrs.unfollow("translate-x", translateX);

            invalidate(false);
            invalidateTransform();
        }
    }

    public float getTranslateY() {
        return translateY;
    }

    public void setTranslateY(float translateY) {
        if (this.translateY != translateY) {
            this.translateY = translateY;
            attrs.unfollow("translate-y", translateY);

            invalidate(false);
            invalidateTransform();
        }
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        if (this.scaleX != scaleX) {
            this.scaleX = scaleX;
            attrs.unfollow("scale-x", scaleX);

            invalidate(false);
            invalidateTransform();
        }
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        if (this.scaleY != scaleY) {
            this.scaleY = scaleY;
            attrs.unfollow("scale-y", scaleY);

            invalidate(false);
            invalidateTransform();
        }
    }

    public float getRotate() {
        return rotate;
    }

    public void setRotate(float rotate) {
        if (rotate < 0 || rotate > 360) rotate = rotate % 360;

        if (this.rotate != rotate) {
            this.rotate = rotate;
            attrs.unfollow("rotate", rotate);

            invalidate(false);
            invalidateTransform();
        }
    }

    public float getElevation() {
        return elevation;
    }

    public void setElevation(float elevation) {
        if (this.elevation != elevation) {
            this.elevation = elevation;
            attrs.unfollow("elevation", elevation);

            invalidate(true);
            if (parent != null) {
                parent.invalidateChildrenOrder();
            }
        }
    }

    public Visibility getVisibility() {
        return Visibility.values()[visibility];
    }

    public void setVisibility(Visibility visibility) {
        if (visibility == null) {
            visibility = Visibility.VISIBLE;
        }

        if (this.visibility != visibility.ordinal()) {
            this.visibility = visibility.ordinal();
            attrs.unfollow("visibility", visibility);

            invalidate(true);
        }
    }

    public Cursor getShowCursor() {
        return cursor == Cursor.UNSET && parent != null ? parent.getShowCursor() : cursor;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        if (this.cursor != cursor) {
            this.cursor = cursor;
            attrs.unfollow("cursor", cursor);
        }
    }

    public float getDisplayOpacity() {
        return parent == null ? opacity : parent.getDisplayOpacity() * opacity;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        opacity = Math.max(0, Math.min(1, opacity));
        if (this.opacity != opacity) {
            this.opacity = opacity;
            attrs.unfollow("opacity", opacity);

            invalidate(false);
        }
    }

    private void childSort() {
        if (invalidChildrenOrder) {
            invalidChildrenOrder = false;
            if (children != null) {
                children.sort(childComparator);
            }
        }
    }

    private void transform() {
        if (invalidTransform) {
            invalidTransform = false;
            float cx = centerX * bg.width + bg.x + x;
            float cy = centerY * bg.height + bg.y + y;
            transform.identity()
                    .translate(cx, cy)
                    .scale(scaleX, scaleY)
                    .rotate(rotate)
                    .translate(translateX + x - cx, translateY + y - cy);

            if (parent != null) {
                transform.preMul(parent.getTransform()); // multiply
            }
            inverseTransform.set(transform).invert();
        }
    }

    private void updateRect() {
        bg.x = marginLeft + marginRight > width ? (marginLeft + width - marginRight) / 2f : marginLeft;
        bg.y = marginTop + marginBottom > height ? (marginTop + height - marginBottom) / 2f : marginTop;
        bg.width = Math.max(0, width - marginLeft - marginRight);
        bg.height = Math.max(0, height - marginTop - marginBottom);

        float lm = marginLeft + paddingLeft;
        float rm = marginRight + paddingRight;
        float tm = marginTop + paddingTop;
        float bm = marginBottom + paddingBottom;

        inx = lm + rm > getWidth() ? (lm + getWidth() - rm) / 2f : lm;
        iny = tm + bm > getHeight() ? (tm + getHeight() - bm) / 2f : tm;
        inw = Math.max(0, getWidth() - lm - rm);
        inh = Math.max(0, getHeight() - tm - bm);
        invalidateTransform();
    }

    public Affine getTransform() {
        transform();
        return new Affine(transform);
    }

    public float getRadiusTop() {
        return bg.arcTop;
    }

    public void setRadiusTop(float radiusTop) {
        if (bg.arcTop != radiusTop) {
            bg.arcTop = radiusTop;
            attrs.unfollow("radius-top", radiusTop);

            invalidate(false);
        }
    }

    public float getRadiusRight() {
        return bg.arcRight;
    }

    public void setRadiusRight(float radiusRight) {
        if (bg.arcRight != radiusRight) {
            bg.arcRight = radiusRight;
            attrs.unfollow("radius-right", radiusRight);

            invalidate(false);
        }
    }

    public float getRadiusBottom() {
        return bg.arcBottom;
    }

    public void setRadiusBottom(float radiusBottom) {
        if (bg.arcBottom != radiusBottom) {
            bg.arcBottom = radiusBottom;
            attrs.unfollow("radius-bottom", radiusBottom);

            invalidate(false);
        }
    }

    public float getRadiusLeft() {
        return bg.arcLeft;
    }

    public void setRadiusLeft(float radiusLeft) {
        if (bg.arcLeft != radiusLeft) {
            bg.arcLeft = radiusLeft;
            attrs.unfollow("radius-left", radiusLeft);

            invalidate(false);
        }
    }

    public void setRadius(float cTop, float cRight, float cBottom, float cLeft) {
        if (bg.arcTop != cTop || bg.arcRight != cRight || bg.arcBottom != cBottom || bg.arcLeft != cLeft) {
            bg.arcTop = cTop;
            bg.arcRight = cRight;
            bg.arcBottom = cBottom;
            bg.arcLeft = cLeft;
            attrs.unfollow("radius-top", cTop);
            attrs.unfollow("radius-right", cRight);
            attrs.unfollow("radius-bottom", cBottom);
            attrs.unfollow("radius-left", cLeft);

            invalidate(false);
        }
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int rgba) {
        if (this.backgroundColor != rgba) {
            this.backgroundColor = rgba;
            attrs.unfollow("background-color", backgroundColor);

            invalidate(false);
        }
    }

    public boolean isBorderRound() {
        return borderRound;
    }

    public void setBorderRound(boolean borderRound) {
        if (this.borderRound != borderRound) {
            this.borderRound = borderRound;
            attrs.unfollow("border-round", borderRound);

            invalidate(false);
        }
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int rgba) {
        if (this.borderColor != rgba) {
            this.borderColor = rgba;
            attrs.unfollow("border-color", borderColor);

            invalidate(false);
        }
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float width) {
        if (this.borderWidth != width) {
            this.borderWidth = width;
            attrs.unfollow("border-width", borderWidth);

            invalidate(false);
        }
    }

    public boolean isShadowEnabled() {
        return shadowEnabled;
    }

    public void setShadowEnabled(boolean enable) {
        if (this.shadowEnabled != enable) {
            this.shadowEnabled = enable;
            attrs.unfollow("shadow-enabled", shadowEnabled);

            invalidate(false);
        }
    }

    public boolean isRippleEnabled() {
        return rippleEnabled;
    }

    public void setRippleEnabled(boolean enable) {
        if (this.rippleEnabled != enable) {
            this.rippleEnabled = enable;
            attrs.unfollow("ripple-enabled", rippleEnabled);

            ripple = enable ? new RippleEffect(this) : null;
            invalidate(false);
        }
    }

    public int getRippleColor() {
        return rippleColor;
    }

    public void setRippleColor(int rgba) {
        if (this.rippleColor != rgba) {
            this.rippleColor = rgba;
            attrs.unfollow("ripple-color", rippleColor);

            invalidate(false);
        }
    }

    public boolean isRippleOverflow() {
        return rippleOverflow;
    }

    public void setRippleOverflow(boolean rippleOverflow) {
        if (this.rippleOverflow != rippleOverflow) {
            this.rippleOverflow = rippleOverflow;
            attrs.unfollow("ripple-overflow", rippleOverflow);

            invalidate(false);
        }
    }

    public void fireRipple(float x, float y) {
        if (rippleEnabled) {
            transform();
            float ix = inverseTransform.pointX(x, y);
            float iy = inverseTransform.pointY(x, y);
            ripple.fire(ix, iy);
        }
    }

    public void releaseRipple() {
        if (rippleEnabled) {
            ripple.release();
        }
    }

    protected RippleEffect getRipple() {
        return ripple;
    }

    public void setPointerListener(PointerListener pointerListener) {
        this.pointerListener = pointerListener;
    }

    public PointerListener getPointerListener() {
        return pointerListener;
    }

    public void setHoverListener(HoverListener hoverListener) {
        this.hoverListener = hoverListener;
    }

    public HoverListener getHoverListener() {
        return hoverListener;
    }

    public void setScrollListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    public ScrollListener getScrollListener() {
        return scrollListener;
    }

    public void setKeyListener(KeyListener keyListener) {
        this.keyListener = keyListener;
    }

    public KeyListener getKeyListener() {
        return keyListener;
    }

    public void setDragListener(DragListener dragListener) {
        this.dragListener = dragListener;
    }

    public DragListener getDragListener() {
        return dragListener;
    }

    public void setFocusListener(FocusListener focusListener) {
        this.focusListener = focusListener;
    }

    public FocusListener getFocusListener() {
        return focusListener;
    }

    public void firePointer(PointerEvent pointerEvent) {
        // -- Pressed -- //
        if (pointerEvent.getType() == PointerEvent.PRESSED) {
            fireRipple(pointerEvent.getX(), pointerEvent.getY());
        }
        if (pointerEvent.getType() == PointerEvent.RELEASED) {
            releaseRipple();

            if (pointerEvent.getPointerID() == 2 && contextMenu != null) {
                showContextMenu(pointerEvent.getX(), pointerEvent.getY());
            }
            if (!pointerEvent.isFocusConsumed() && isFocusable()) {
                pointerEvent.consumeFocus(true);
                requestFocus(true);
            }
        }

        if (pointerListener != null) {
            pointerListener.handle(pointerEvent);
        }
        if (parent != null) {
            parent.firePointer(pointerEvent);
        }
    }

    public void fireHover(HoverEvent hoverEvent) {
        // -- Hovered -- //
        if (hoverEvent.getType() == HoverEvent.ENTERED) {
            setHovered(true);
        } else if (hoverEvent.getType() == HoverEvent.EXITED) {
            setHovered(false);
        }

        if (hoverListener != null) {
            hoverListener.handle(hoverEvent);
        }
        if (parent != null && hoverEvent.isRecyclable(parent)) {
            parent.fireHover(hoverEvent);
        }
    }

    public void fireScroll(ScrollEvent scrollEvent) {
        if (scrollListener != null) {
            scrollListener.handle(scrollEvent);
        }
        if (parent != null) {
            parent.fireScroll(scrollEvent);
        }
    }

    public void fireDrag(DragEvent dragEvent) {
        if (dragListener != null) {
            dragListener.handle(dragEvent);
        }
        if (parent != null && dragEvent.isRecyclable(parent)) {
            parent.fireDrag(dragEvent);
        }
    }

    public void fireKey(KeyEvent keyEvent) {
        if (keyListener != null) {
            keyListener.handle(keyEvent);
        }
        if (parent != null) {
            parent.fireKey(keyEvent);
        }
    }

    public void fireFocus(FocusEvent focusEvent) {
        if (focusListener != null) {
            focusListener.handle(focusEvent);
        }
    }

    @Override
    public String toString() {
        return "[" + id + "]" + getClass().getSimpleName();
    }
}
