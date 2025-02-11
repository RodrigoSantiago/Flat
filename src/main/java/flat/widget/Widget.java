package flat.widget;

import flat.animations.StateAnimation;
import flat.animations.StateBitset;
import flat.animations.StateInfo;
import flat.events.*;
import flat.graphics.Color;
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
import flat.widget.enums.Visibility;
import flat.window.Activity;
import static flat.widget.State.*;

import java.util.*;

public class Widget {

    //---------------------
    //    Constants
    //---------------------
    public static final float WRAP_CONTENT = 0;
    public static final float MATCH_PARENT = Float.POSITIVE_INFINITY;

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
    private float layoutWidth, layoutHeight;
    private float minWidth, minHeight, maxWidth = MATCH_PARENT, maxHeight = MATCH_PARENT, prefWidth, prefHeight;
    private float weight = 1;
    private float measureWidth, measureHeight;

    private int visibility = Visibility.VISIBLE.ordinal();
    private Cursor cursor = Cursor.UNSET;

    private Menu contextMenu;

    //---------------------
    //    Family
    //---------------------
    Parent parent;
    Activity activity;
    Scene scene;
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
    private UXListener<PointerEvent> pointerListener;
    private UXListener<HoverEvent> hoverListener;
    private UXListener<ScrollEvent> scrollListener;
    private UXListener<KeyEvent> keyListener;
    private UXListener<DragEvent> dragListener;
    private UXListener<FocusEvent> focusListener;

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
        attrs = new UXAttrs(this, convertToKebabCase(getClass().getSimpleName()));
    }

    private static String convertToKebabCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }

        return camelCase
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2")
                .toLowerCase();
    }

    public void setAttributes(HashMap<Integer, UXValue> attributes, String style) {
        attrs.setAttributes(attributes);
        attrs.setName(style);
    }

    public void applyAttributes(Controller controller) {
        UXAttrs attrs = getAttrs();
        attrs.setTheme(getCurrentTheme());

        setEnabled(attrs.getAttributeBool("enabled", isEnabled()));

        setPointerListener(attrs.getAttributeListener("on-pointer", PointerEvent.class, controller));
        setHoverListener(attrs.getAttributeListener("on-hover", HoverEvent.class, controller));
        setScrollListener(attrs.getAttributeListener("on-scroll", ScrollEvent.class, controller));
        setKeyListener(attrs.getAttributeListener("on-key", KeyEvent.class, controller));
        setDragListener(attrs.getAttributeListener("on-drag", DragEvent.class, controller));
        setFocusListener(attrs.getAttributeListener("on-focus", FocusEvent.class, controller));

        setNextFocusId(attrs.getAttributeString("next-focus-id", getNextFocusId()));
        setPrevFocusId(attrs.getAttributeString("prev-focus-id", getPrevFocusId()));
    }

    public void applyStyle() {
        UXAttrs attrs = getAttrs();

        setTransitionDuration(attrs.getNumber("transition-duration", null, getTransitionDuration()));

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
                for (Widget child : getChildrenIterable()) {
                    child.applyStyle();
                }
            }
        }

        StateInfo info = getStateInfo();

        setVisibility(attrs.getConstant("visibility", info, getVisibility()));
        setCursor(attrs.getConstant("cursor", info, getCursor()));

        setFocusable(attrs.getBool("focusable", info, isFocusable()));
        setClickable(attrs.getBool("clickable", info, isClickable()));

        setPrefWidth(attrs.getSize("width", info, getPrefWidth()));
        setPrefHeight(attrs.getSize("height", info, getPrefHeight()));
        setMaxWidth(attrs.getSize("max-width", info, getMaxWidth()));
        setMaxHeight(attrs.getSize("max-height", info, getMaxHeight()));
        setMinWidth(attrs.getSize("min-width", info, getMinWidth()));
        setMinHeight(attrs.getSize("min-height", info, getMinHeight()));
        setWeight(attrs.getSize("weight", info, getWeight()));

        setTranslateX(attrs.getSize("translate-x", info, getTranslateX()));
        setTranslateY(attrs.getSize("translate-y", info, getTranslateY()));
        setCenterX(attrs.getNumber("center-x", info, getCenterX()));
        setCenterY(attrs.getNumber("center-y", info, getCenterY()));
        setScaleX(attrs.getNumber("scale-x", info, getScaleX()));
        setScaleY(attrs.getNumber("scale-y", info, getScaleY()));

        setRotate(attrs.getAngle("rotate", info, getRotate()));

        setElevation(attrs.getSize("elevation", info, getElevation()));
        setShadowEnabled(attrs.getBool("shadow-enabled", info, isShadowEnabled()));

        setRippleEnabled(attrs.getBool("ripple-enabled", info, isRippleEnabled()));
        setRippleColor(attrs.getColor("ripple-color", info, getRippleColor()));
        setRippleOverflow(attrs.getBool("ripple-overflow", info, isRippleOverflow()));

        setMarginTop(attrs.getSize("margin-top", info, getMarginTop()));
        setMarginRight(attrs.getSize("margin-right", info, getMarginRight()));
        setMarginBottom(attrs.getSize("margin-bottom", info, getMarginBottom()));
        setMarginLeft(attrs.getSize("margin-left", info, getMarginLeft()));

        setPaddingTop(attrs.getSize("padding-top", info, getPaddingTop()));
        setPaddingRight(attrs.getSize("padding-right", info, getPaddingRight()));
        setPaddingBottom(attrs.getSize("padding-bottom", info, getPaddingBottom()));
        setPaddingLeft(attrs.getSize("padding-left", info, getPaddingLeft()));

        setRadiusTop(attrs.getSize("radius-top", info, getRadiusTop()));
        setRadiusRight(attrs.getSize("radius-right", info, getRadiusRight()));
        setRadiusBottom(attrs.getSize("radius-bottom", info, getRadiusBottom()));
        setRadiusLeft(attrs.getSize("radius-left", info, getRadiusLeft()));

        setBackgroundColor(attrs.getColor("background-color", info, getBackgroundColor()));
        setBorderRound(attrs.getBool("border-round", info, isBorderRound()));
        setBorderColor(attrs.getColor("border-color", info, getBorderColor()));
        setBorderWidth(attrs.getSize("border-width", info, getBorderWidth()));
    }

    public void applyChildren(UXChildren children) {
        Menu menu = children.nextMenu();
        if (menu != null) {
            setContextMenu(menu);
        }
    }

    public void applyTheme() {
        attrs.setTheme(getCurrentTheme());
        applyStyle();

        for (Widget child : getChildrenIterable()) {
            child.applyTheme();
        }
        if (contextMenu != null) {
            contextMenu.applyTheme();
        }
    }

    public void onDraw(SmartContext context) {
        drawBackground(context);
        drawRipple(context);
        drawChildren(context);
    }

    protected void drawBackground(SmartContext context) {
        if (bg.width <= 0 || bg.height <= 0) {
            return;
        }

        float bgOpacity = Color.getOpacity(backgroundColor);
        float borderOpacity = Color.getOpacity(borderColor);

        float b = borderWidth * borderOpacity;
        float b2 = b / 2;

        // Draw Background Shadow
        if (bgOpacity > 0 && shadowEnabled) {
            context.setTransform2D(getTransform().preTranslate(0, Math.max(0, elevation)));
            context.drawRoundRectShadow(
                    bg.x - b, bg.y - b, bg.width + b * 2, bg.height + b * 2,
                    bg.arcTop + b, bg.arcRight + b, bg.arcBottom + b, bg.arcLeft + b,
                    elevation * 2, 0.55f * bgOpacity);
        }

        // Draw Background
        if (bgOpacity > 0) {
            context.setTransform2D(getTransform());
            context.setColor(backgroundColor);
            context.drawRoundRect(bg, true);
        }

        // Draw Border
        if (borderOpacity > 0 && borderWidth > 0) {
            context.setTransform2D(getTransform());
            context.setColor(borderColor);
            context.setStroker(new BasicStroke(borderWidth));
            context.drawRoundRect(
                    bg.x - b2, bg.y - b2, bg.width + b, bg.height + b,
                    bg.arcTop + b2, bg.arcRight + b2, bg.arcBottom + b2, bg.arcLeft + b2,
                    false);
        }
    }

    protected void drawRipple(SmartContext context) {
        float rippleOpacity = Color.getOpacity(rippleColor);

        if (rippleOpacity > 0 && rippleEnabled && ripple.isVisible()) {
            context.setTransform2D(getTransform());
            ripple.drawRipple(context, rippleOverflow ? null : bg, rippleColor);
        }
    }

    protected void drawChildren(SmartContext context) {
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
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        if (wrapWidth) {
            mWidth = Math.max(extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    /**
     * Define the best size (internal)
     *
     * @param width
     * @param height
     */
    public final void setMeasure(float width, float height) {
        measureWidth = width;
        measureHeight = height;
    }

    public float getMeasureWidth()  {
        return measureWidth;
    }

    public float getMeasureHeight()  {
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
     * @param layoutWidth
     * @param layoutHeight
     */
    public final void setLayout(float layoutWidth, float layoutHeight) {
        if (this.layoutWidth != layoutWidth || this.layoutHeight != layoutHeight) {
            this.layoutWidth = layoutWidth;
            this.layoutHeight = layoutHeight;
            updateRect();
        }
    }

    public float getLayoutPrefWidth() {
        return prefWidth == WRAP_CONTENT ? prefWidth : prefWidth + marginLeft + marginRight;
    }

    public float getLayoutPrefHeight() {
        return prefHeight == WRAP_CONTENT ? prefHeight : prefHeight + marginTop + marginBottom;
    }

    public float getLayoutMinWidth() {
        return Math.max(minWidth, paddingLeft + paddingRight) + marginLeft + marginRight;
    }

    public float getLayoutMinHeight() {
        return Math.max(minHeight, paddingTop + paddingBottom) + marginTop + marginBottom;
    }

    public float getLayoutMaxWidth() {
        return Math.max(getLayoutMinWidth(), maxWidth <= 0 ? MATCH_PARENT : maxWidth + marginLeft + marginRight);
    }

    public float getLayoutMaxHeight() {
        return Math.max(getLayoutMinHeight(), maxHeight <= 0 ? MATCH_PARENT : maxHeight + marginTop + marginBottom);
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

    protected void childInvalidate(Widget child, boolean source) {
        if (parent != null) {
            if (source) {
                if (getPrefWidth() == WRAP_CONTENT || getPrefHeight() == WRAP_CONTENT) {
                    parent.childInvalidate(this, true);
                } else {
                    parent.childInvalidate(this, false);
                }
            } else {
                parent.childInvalidate(child, false);
            }
        }
    }

    // TODO - Some widgets are using TRUE when the widget is NOT WRAP_CONTENT!
    public void invalidate(boolean layout) {
        if (parent != null) {
            if (layout) {
                parent.childInvalidate(this, true);
            } else {
                parent.invalidate(false);
            }
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

    protected boolean invalidateChildrenOrder(Widget child) {
        if (child == null) {
            invalidChildrenOrder = true;
            return true;
        }

        int index = children.indexOf(child);
        float el = child.getElevation();
        int indexPrev = index - 1;
        int indexNext = index + 1;
        boolean biggerThanPrevious = (indexPrev < 0 || children.get(indexPrev).getElevation() < el);
        boolean smallerThanNext = (indexNext >= children.size() || children.get(indexNext).getElevation() > el);
        if (!biggerThanPrevious || !smallerThanNext) {
            invalidChildrenOrder = true;
            return true;
        }
        return false;
    }

    public final String getId() {
        return id;
    }

    public void setId(String id) {
        if (!Objects.equals(this.id, id)) {
            String oldId = this.id;
            this.id = id;
            Scene scene = getScene();
            if (scene == this) {
                scene = scene.getScene();
            }
            if (scene != null) {
                scene.reassign(oldId, this);
            }
        }
    }

    protected Activity getCurrentActivity() {
        if (parent != null) {
            return parent.getCurrentActivity();
        } else {
            return null;
        }
    }

    public Activity getActivity() {
        return activity;
    }

    public void setTheme(UXTheme theme) {
        if (this.theme != theme) {
            this.theme = theme;

            invalidate(true);
        }
    }

    public UXTheme getTheme() {
        return this.theme;
    }

    public UXTheme getCurrentTheme() {
        return this.theme != null ? this.theme : parent != null ? parent.getCurrentTheme() : null;
    }

    /**
     * Return the current assigned scene
     *
     * @return
     */
    public Scene getScene() {
        return scene;
    }

    protected Scene getCurrentScene() {
        if (parent != null) {
            return parent.getCurrentScene();
        } else {
            return null;
        }
    }

    public Parent getParent() {
        return parent;
    }

    void setParent(Parent parent) {
        Scene sceneA = getCurrentScene();
        Activity activityA = sceneA == null ? null : sceneA.getActivity();

        this.parent = parent;

        Scene sceneB = getCurrentScene();
        Activity activityB = sceneB == null ? null : sceneB.getActivity();

        if (sceneA != sceneB) {
            onSceneChangeLocal(sceneA, sceneB);
        }

        if (activityA != activityB) {
            onActivityChangeLocal(activityA, activityB);
        }

        if (sceneA != sceneB) {
            onSceneChange(sceneA, sceneB);
        }

        if (activityA != activityB) {
            onActivityChange(activityA, activityB);
        }
    }

    public void onSceneChange(Scene prev, Scene scene) {
        if (children != null) {
            for (Widget widget : getChildrenIterable()) {
                widget.onSceneChange(prev, scene);
            }
        }

        if (contextMenu != null) {
            contextMenu.onSceneChange(prev, scene);
        }
    }

    void onSceneChangeLocal(Scene prev, Scene scene) {
        this.scene = scene;
        if (prev != null) {
            prev.unassign(this);
        }
        if (scene != null) {
            scene.assign(this);
        }
        if (children != null) {
            for (Widget widget : getChildrenIterable()) {
                widget.onSceneChangeLocal(prev, scene);
            }
        }

        if (contextMenu != null) {
            ((Widget)contextMenu).onSceneChangeLocal(prev, scene);
        }
    }

    public void onActivityChange(Activity prev, Activity activity) {
        if (children != null) {
            for (Widget widget : getChildrenIterable()) {
                widget.onActivityChange(prev, activity);
            }
        }

        if (contextMenu != null) {
            contextMenu.onActivityChange(prev, activity);
        }
    }

    void onActivityChangeLocal(Activity prev, Activity activity) {
        this.activity = activity;
        refreshFocus();

        if (children != null) {
            for (Widget widget : getChildrenIterable()) {
                widget.onActivityChangeLocal(prev, activity);
            }
        }

        if (contextMenu != null) {
            ((Widget)contextMenu).onActivityChangeLocal(prev, activity);
        }

        if (ripple != null) {
            ripple.stop();
        }

        if (stateAnimation != null && stateAnimation.isPlaying()) {
            stateAnimation.stop();
        }
    }

    public List<Widget> getUnmodifiableChildren() {
        if (children != null && unmodifiableChildren == null) {
            unmodifiableChildren = Collections.unmodifiableList(children);
        }
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
                boolean reverseX = contextMenu.getMeasureWidth() + x > act.getWidth();
                boolean reverseY = contextMenu.getMeasureHeight() + y > act.getHeight();
                contextMenu.show(act,
                        reverseX ? x - contextMenu.getMeasureWidth() : x,
                        reverseY ? y - contextMenu.getMeasureHeight() : y);
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
        attrs.unfollow(name);
    }

    public float getTransitionDuration() {
        return transitionDuration;
    }

    public void setTransitionDuration(float transitionDuration) {
        transitionDuration = Math.max(transitionDuration, 0);

        if (this.transitionDuration != transitionDuration) {
            this.transitionDuration = transitionDuration;

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
        return !isEnabled() || (parent != null && parent.isDisabled());
    }

    public boolean isEnabled() {
        return !DISABLED.contains(states);
    }

    public void setEnabled(boolean enabled) {
        if (isEnabled() != enabled) {
            setStates((byte) (!enabled ? states | DISABLED.bitset() : states & ~DISABLED.bitset()));
        }
    }

    public boolean isActivated() {
        return ACTIVATED.contains(states);
    }

    protected void setActivated(boolean actived) {
        if (isActivated() != actived) {
            setStates((byte) (actived ? states | ACTIVATED.bitset() : states & ~ACTIVATED.bitset()));
        }
    }

    public boolean isHovered() {
        return HOVERED.contains(states);
    }

    protected void setHovered(boolean hovered) {
        if (isHovered() != hovered) {
            setStates((byte) (hovered ? states | HOVERED.bitset() : states & ~HOVERED.bitset()));
        }
    }

    public boolean isPressed() {
        return PRESSED.contains(states);
    }

    protected void setPressed(boolean pressed) {
        if (isPressed() != pressed) {
            setStates((byte) (pressed ? states | PRESSED.bitset() : states & ~PRESSED.bitset()));
        }
    }

    public boolean isDragged() {
        return DRAGGED.contains(states);
    }

    protected void setDragged(boolean dragged) {
        if (isDragged() != dragged) {
            setStates((byte) (dragged ? states | DRAGGED.bitset() : states & ~DRAGGED.bitset()));
        }
    }

    public boolean isError() {
        return ERROR.contains(states);
    }

    protected void setError(boolean error) {
        if (isError() != error) {
            setStates((byte) (error ? states | ERROR.bitset() : states & ~ERROR.bitset()));
        }
    }

    public void refreshFocus() {
        Activity activity = getActivity();
        setFocused(activity != null && activity.getFocus() == this);
    }

    public boolean isFocused() {
        return FOCUSED.contains(states);
    }

    protected void setFocused(boolean focused) {
        if (isFocused() != focused) {
            Activity activity = getActivity();
            if (activity != null) {
                if (focused) {
                    if (focusable) {
                        setStates((byte) (states | FOCUSED.bitset()));
                        if (activity.getFocus() != this) {
                            activity.setFocus(this);
                        }
                    }
                } else {
                    setStates((byte) (states & ~FOCUSED.bitset()));
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

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
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

    public float getLayoutWidth() {
        return layoutWidth;
    }

    public float getLayoutHeight() {
        return layoutHeight;
    }

    public float getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(float marginTop) {
        if (this.marginTop != marginTop) {
            this.marginTop = marginTop;

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

            invalidate(true);
        }
    }

    public float getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(float paddingRight) {
        if (this.paddingRight != paddingRight) {
            this.paddingRight = paddingRight;

            invalidate(true);
        }
    }

    public float getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(float paddingBottom) {
        if (this.paddingBottom != paddingBottom) {
            this.paddingBottom = paddingBottom;

            invalidate(true);
        }
    }

    public float getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(float paddingLeft) {
        if (this.paddingLeft != paddingLeft) {
            this.paddingLeft = paddingLeft;

            invalidate(true);
        }
    }

    public void setPadding(float top, float right, float bottom , float left) {
        if (paddingTop != top || paddingRight != right || paddingBottom != bottom || paddingLeft != left) {
            paddingTop = top;
            paddingRight = right;
            paddingBottom = bottom;
            paddingLeft = left;

            invalidate(true);
        }
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        if (this.weight != weight) {
            this.weight = weight;

            invalidate(true);
        }
    }

    public float getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(float minWidth) {
        if (this.minWidth != minWidth) {
            this.minWidth = minWidth;

            invalidate(true);
        }
    }

    public float getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(float minHeight) {
        if (this.minHeight != minHeight) {
            this.minHeight = minHeight;

            invalidate(true);
        }
    }

    public void setMinSize(float minWidth, float minHeight) {
        if (this.minWidth != minWidth || this.minHeight != minHeight) {
            this.minWidth = minWidth;
            this.minHeight = minHeight;

            invalidate(true);
        }
    }

    public float getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(float maxWidth) {
        if (this.maxWidth != maxWidth) {
            this.maxWidth = maxWidth;

            invalidate(true);
        }
    }

    public float getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(float maxHeight) {
        if (this.maxHeight != maxHeight) {
            this.maxHeight = maxHeight;

            invalidate(true);
        }
    }

    public void setMaxSize(float maxWidth, float maxHeight) {
        if (this.maxWidth != maxWidth || this.maxHeight != maxHeight) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;

            invalidate(true);
        }
    }

    public float getPrefWidth() {
        return prefWidth;
    }

    public void setPrefWidth(float prefWidth) {
        if (this.prefWidth != prefWidth) {
            this.prefWidth = prefWidth;

            invalidate(true);
        }
    }

    public float getPrefHeight() {
        return prefHeight;
    }

    public void setPrefHeight(float prefHeight) {
        if (this.prefHeight != prefHeight) {
            this.prefHeight = prefHeight;

            invalidate(true);
        }
    }

    public void setPrefSize(float prefWidth, float prefHeight) {
        if (this.prefWidth != prefWidth || this.prefHeight != prefHeight) {
            this.prefWidth = prefWidth;
            this.prefHeight = prefHeight;

            invalidate(true);
        }
    }

    public float getCenterX() {
        return centerX;
    }

    public void setCenterX(float centerX) {
        if (this.centerX != centerX) {
            this.centerX = centerX;

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

            if (parent != null && parent.invalidateChildrenOrder(this)) {
                invalidate(true);
            } else {
                invalidate(false);
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
            var old = getVisibility();
            this.visibility = visibility.ordinal();

            invalidate(old == Visibility.GONE || visibility == Visibility.GONE);
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
        bg.x = marginLeft + marginRight > layoutWidth ? (marginLeft + layoutWidth - marginRight) / 2f : marginLeft;
        bg.y = marginTop + marginBottom > layoutHeight ? (marginTop + layoutHeight - marginBottom) / 2f : marginTop;
        bg.width = Math.max(0, layoutWidth - marginLeft - marginRight);
        bg.height = Math.max(0, layoutHeight - marginTop - marginBottom);

        float eLeft = marginLeft + paddingLeft;
        float eRight = marginRight + paddingRight;
        float eTop = marginTop + paddingTop;
        float eBot = marginBottom + paddingBottom;

        inx = eLeft + eRight > layoutWidth ? (eLeft + layoutWidth - eRight) / 2f : eLeft;
        iny = eTop + eBot > layoutHeight ? (eTop + layoutHeight - eBot) / 2f : eTop;
        inw = Math.max(0, layoutWidth - eLeft - eRight);
        inh = Math.max(0, layoutHeight - eTop - eBot);
        width = Math.max(0, layoutWidth - marginLeft - marginRight);
        height = Math.max(0, layoutHeight - marginTop - marginBottom);
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

            invalidate(false);
        }
    }

    public float getRadiusRight() {
        return bg.arcRight;
    }

    public void setRadiusRight(float radiusRight) {
        if (bg.arcRight != radiusRight) {
            bg.arcRight = radiusRight;

            invalidate(false);
        }
    }

    public float getRadiusBottom() {
        return bg.arcBottom;
    }

    public void setRadiusBottom(float radiusBottom) {
        if (bg.arcBottom != radiusBottom) {
            bg.arcBottom = radiusBottom;

            invalidate(false);
        }
    }

    public float getRadiusLeft() {
        return bg.arcLeft;
    }

    public void setRadiusLeft(float radiusLeft) {
        if (bg.arcLeft != radiusLeft) {
            bg.arcLeft = radiusLeft;

            invalidate(false);
        }
    }

    public void setRadius(float cTop, float cRight, float cBottom, float cLeft) {
        if (bg.arcTop != cTop || bg.arcRight != cRight || bg.arcBottom != cBottom || bg.arcLeft != cLeft) {
            bg.arcTop = cTop;
            bg.arcRight = cRight;
            bg.arcBottom = cBottom;
            bg.arcLeft = cLeft;

            invalidate(false);
        }
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int rgba) {
        if (this.backgroundColor != rgba) {
            this.backgroundColor = rgba;

            invalidate(false);
        }
    }

    public boolean isBorderRound() {
        return borderRound;
    }

    public void setBorderRound(boolean borderRound) {
        if (this.borderRound != borderRound) {
            this.borderRound = borderRound;

            invalidate(false);
        }
    }

    public int getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(int rgba) {
        if (this.borderColor != rgba) {
            this.borderColor = rgba;

            invalidate(false);
        }
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float width) {
        if (this.borderWidth != width) {
            this.borderWidth = width;

            invalidate(false);
        }
    }

    public boolean isShadowEnabled() {
        return shadowEnabled;
    }

    public void setShadowEnabled(boolean enable) {
        if (this.shadowEnabled != enable) {
            this.shadowEnabled = enable;

            invalidate(false);
        }
    }

    public boolean isRippleEnabled() {
        return rippleEnabled;
    }

    public void setRippleEnabled(boolean enable) {
        if (this.rippleEnabled != enable) {
            this.rippleEnabled = enable;

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

            invalidate(false);
        }
    }

    public boolean isRippleOverflow() {
        return rippleOverflow;
    }

    public void setRippleOverflow(boolean rippleOverflow) {
        if (this.rippleOverflow != rippleOverflow) {
            this.rippleOverflow = rippleOverflow;

            invalidate(false);
        }
    }

    public void fireRipple(float x, float y) {
        if (rippleEnabled) {
            transform();
            float ix;
            float iy;
            if (rippleOverflow) {
                ix = inx + inw * 0.5f;
                iy = iny + inh * 0.5f;
                float w = getLayoutWidth();
                float h = getLayoutHeight();
                ripple.setSize((float) Math.sqrt(w * w + h * h) * 0.5f);
                ripple.setSize(Math.max(getLayoutWidth(), getLayoutHeight()) * 0.5f);
            } else {
                ix = inverseTransform.pointX(x, y);
                iy = inverseTransform.pointY(x, y);
                ripple.setSize(Math.max(getWidth(), getHeight()));
            }
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

    public void setPointerListener(UXListener<PointerEvent> pointerListener) {
        this.pointerListener = pointerListener;
    }

    public UXListener<PointerEvent> getPointerListener() {
        return pointerListener;
    }

    public void setHoverListener(UXListener<HoverEvent> hoverListener) {
        this.hoverListener = hoverListener;
    }

    public UXListener<HoverEvent> getHoverListener() {
        return hoverListener;
    }

    public void setScrollListener(UXListener<ScrollEvent> scrollListener) {
        this.scrollListener = scrollListener;
    }

    public UXListener<ScrollEvent> getScrollListener() {
        return scrollListener;
    }

    public void setKeyListener(UXListener<KeyEvent> keyListener) {
        this.keyListener = keyListener;
    }

    public UXListener<KeyEvent> getKeyListener() {
        return keyListener;
    }

    public void setDragListener(UXListener<DragEvent> dragListener) {
        this.dragListener = dragListener;
    }

    public UXListener<DragEvent> getDragListener() {
        return dragListener;
    }

    public void setFocusListener(UXListener<FocusEvent> focusListener) {
        this.focusListener = focusListener;
    }

    public UXListener<FocusEvent> getFocusListener() {
        return focusListener;
    }

    public void firePointer(PointerEvent pointerEvent) {
        // -- Pressed -- //
        if (pointerEvent.getType() == PointerEvent.PRESSED) {
            setPressed(true);
            fireRipple(pointerEvent.getX(), pointerEvent.getY());
        }
        if (pointerEvent.getType() == PointerEvent.RELEASED) {
            setPressed(false);
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
