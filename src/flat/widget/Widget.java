package flat.widget;

import flat.animations.StateAnimation;
import flat.animations.StateBitset;
import flat.animations.StateInfo;
import flat.events.*;
import flat.graphics.SmartContext;
import flat.graphics.cursor.Cursor;
import flat.math.*;
import flat.math.shapes.Rectangle;
import flat.math.shapes.RoundRectangle;
import flat.math.shapes.Shape;
import flat.math.stroke.BasicStroke;
import flat.uxml.*;
import flat.widget.effects.RippleEffect;
import flat.widget.enuns.Visibility;

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

    private int visibility = Visibility.Visible.ordinal();
    private Cursor cursor;
    private UXTheme theme;

    //---------------------
    //    Family
    //---------------------
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
    boolean invalidTransform;

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
    private UXStyle style;
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

    private boolean shadowEnabled;
    private boolean rippleEnabled;
    private long transitionDuration;

    public Widget() {

    }

    public Widget(UXStyleAttrs style) {
        this(style, null);
    }

    public Widget(UXStyleAttrs style, Controller controller) {
        applyAttributes(style, controller);
    }

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        if (style == null) return;

        String id = style.asString("id");
        if (id != null) {
            setId(id);
            if (controller != null) {
                controller.assign(id, this);
            }
        }

        Method handle = style.asListener("on-pointer", PointerEvent.class, controller);
        if (handle != null) {
            setPointerListener(new PointerListener.AutoPointerListener(controller, handle));
        }
        handle = style.asListener("on-hover", HoverEvent.class, controller);
        if (handle != null) {
            setHoverListener(new HoverListener.AutoHoverListener(controller, handle));
        }
        handle = style.asListener("on-scroll", ScrollEvent.class, controller);
        if (handle != null) {
            setScrollListener(new ScrollListener.AutoScrollListener(controller, handle));
        }
        handle = style.asListener("on-key", KeyEvent.class, controller);
        if (handle != null) {
            setKeyListener(new KeyListener.AutoKeyListener(controller, handle));
        }
        handle = style.asListener("on-drag", DragEvent.class, controller);
        if (handle != null) {
            setDragListener(new DragListener.AutoDragListener(controller, handle));
        }
        handle = style.asListener("on-focus", FocusEvent.class, controller);
        if (handle != null) {
            setFocusListener(new FocusListener.AutoFocusListener(controller, handle));
        }

        setNextFocusId(style.asString("next-focus-id", getNextFocusId()));
        setPrevFocusId(style.asString("prev-focus-id", getPrevFocusId()));
        setStyle(style);
    }

    @Override
    public void applyChildren(UXChildren children) {

    }

    public void applyStyle() {
        if (style == null) return;

        setTransitionDuration((long) style.asNumber("transition-duration", getTransitionDuration()));

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
                for (Widget child : children) {
                    child.applyStyle();
                }
            }
        }

        StateInfo info = getStateInfo();

        setEnabled(style.asBool("enabled", info, isEnabled()));
        setVisibility(style.asConstant("visibility", info, getVisibility()));

        switch (style.asString("cursor", info, String.valueOf(getCursor())).toLowerCase()) {
            case "arrow": setCursor(Cursor.ARROW); break;
            case "crosshair": setCursor(Cursor.CROSSHAIR);break;
            case "hand": setCursor(Cursor.HAND);break;
            case "ibeam": setCursor(Cursor.IBEAM);break;
            case "hresize": setCursor(Cursor.HRESIZE);break;
            case "vresize": setCursor(Cursor.VRESIZE);break;
            default: setCursor(null);
        }

        setFocusable(style.asBool("focusable", info, isFocusable()));
        setClickable(style.asBool("clickable", info, isClickable()));

        setPrefWidth(style.asSize("width", info, getPrefWidth()));
        setPrefHeight(style.asSize("height", info, getPrefHeight()));
        setMaxWidth(style.asSize("max-width", info, getMaxWidth()));
        setMaxHeight(style.asSize("max-height", info, getMaxHeight()));
        setMinWidth(style.asSize("min-width", info, getMinWidth()));
        setMinHeight(style.asSize("min-height", info, getMinHeight()));

        setTranslateX(style.asSize("x", info, getTranslateX()));
        setTranslateY(style.asSize("y", info, getTranslateY()));
        setCenterX(style.asNumber("centre-x", info, getCenterX()));
        setCenterY(style.asNumber("centre-y", info, getCenterY()));
        setScaleX(style.asNumber("scale-x", info, getScaleX()));
        setScaleY(style.asNumber("scale-y", info, getScaleY()));
        setOpacity(style.asNumber("opacity", info, getOpacity()));

        setRotate(style.asAngle("rotate", info, getRotate()));

        setElevation(style.asSize("elevation", info, getElevation()));
        setShadowEnabled(style.asBool("shadow", info, isShadowEnabled()));

        setRippleColor(style.asColor("ripple-color", info, getRippleColor()));
        setRippleEnabled(style.asBool("ripple", info, isRippleEnabled()));

        setMarginTop(style.asSize("margin-top", info, getMarginTop()));
        setMarginRight(style.asSize("margin-right", info, getMarginRight()));
        setMarginBottom(style.asSize("margin-bottom", info, getMarginBottom()));
        setMarginLeft(style.asSize("margin-left", info, getMarginLeft()));

        setPaddingTop(style.asSize("padding-top", info, getPaddingTop()));
        setPaddingRight(style.asSize("padding-right", info, getPaddingRight()));
        setPaddingBottom(style.asSize("padding-bottom", info, getPaddingBottom()));
        setPaddingLeft(style.asSize("padding-left", info, getPaddingLeft()));

        setRadiusTop(style.asSize("radius-top", info, getRadiusTop()));
        setRadiusRight(style.asSize("radius-right", info, getRadiusRight()));
        setRadiusBottom(style.asSize("radius-bottom", info, getRadiusBottom()));
        setRadiusLeft(style.asSize("radius-left", info, getRadiusLeft()));

        setBackgroundColor(style.asColor("background-color", info, getBackgroundColor()));
        setBorderRound(style.asBool("border-round", info, isBorderRound()));
        setBorderColor(style.asColor("border-color", info, getBorderColor()));
        setBorderWidth(style.asSize("border-width", info, getBorderWidth()));
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
                        elevation * 2, 0.28f * ((backgroundColor & 0xFF) / 255f));
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
                ripple.drawRipple(context, bg, rippleColor);
            }

            context.setTransform2D(null);
        }
    }

    protected void childrenDraw(SmartContext context) {
        if (children != null) {
            childSort();
            for (Widget child : children) {
                if (child.getVisibility() == Visibility.Visible) {
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
        setMeasure(Math.max(prefWidth + marginLeft + marginRight, getLayoutMinWidth()),
                Math.max(prefHeight + marginTop + marginBottom, getLayoutMinHeight()));
    }

    public float getMeasureWidth()  {
        return measureWidth;
    }

    public float getMeasureHeight()  {
        return measureHeight;
    }

    /**
     * Define the best size (internal)
     *
     * @param width
     * @param height
     */
    public final void setMeasure(float width, float height) {
        measureWidth = Math.max(width, getLayoutMinWidth());
        measureHeight = Math.max(height, getLayoutMinHeight());
    }

    /**
     * Define the position and computate the best size amoung multiples children (internal)
     * @param width
     * @param height
     */
    public void onLayout(float width, float height) {
        setLayout(Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
    }

    /**
     * Set the widget real size, based on parent's size (internal)
     * @param width
     * @param height
     */
    public final void setLayout(float width, float height) {
        if (parent != null) {
            if (width == MATCH_PARENT && (maxWidth == MATCH_PARENT || maxWidth == WRAP_CONTENT)) {
                setWidth(parent.getWidth());
            } else {
                setWidth(Math.min(width, getLayoutMaxWidth()));
            }
            if (height == MATCH_PARENT && (maxHeight == MATCH_PARENT || maxHeight == WRAP_CONTENT)) {
                setHeight(parent.getHeight());
            } else {
                setHeight(Math.min(height, getLayoutMaxHeight()));
            }
        } else {
            if (maxWidth != MATCH_PARENT && maxWidth != WRAP_CONTENT) {
                setWidth(Math.min(width, getLayoutMaxWidth()));
            } else {
                setWidth(width);
            }
            if (maxHeight != MATCH_PARENT && maxHeight != WRAP_CONTENT) {
                setHeight(Math.min(height, getLayoutMaxHeight()));
            } else {
                setHeight(height);
            }
        }
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
            for (Widget child : children) {
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
        Scene scene = getScene();
        if (scene != null) {
            return scene.getActivity();
        } else {
            return null;
        }
    }

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
        // todo - Cyclic parent bug
        if (this.parent != null && parent != null) {
            this.parent.remove(this);
        }
        Scene sceneA = getScene();
        this.parent = parent;
        Scene sceneB = getScene();
        if (sceneA != sceneB) {
            if (sceneA != null) {
                sceneA.deassign(this);
            }
            if (sceneB != null) {
                sceneB.assign(this);
            }
            onSceneChange();
        }
    }

    protected void onSceneChange() {
        if (children != null) {
            for (Widget widget : children) {
                widget.onSceneChange();
            }
        }
    }

    public List<Widget> getUnmodifiableChildren() {
        return unmodifiableChildren;
    }

    protected ArrayList<Widget> getChildren() {
        return children;
    }

    public Widget findById(String id) {
        if (id == null) return null;

        Scene scene = getScene();
        if (scene != null) {
            return scene.findById(id);
        } else {
            if (children != null) {
                for (Widget child : children) {
                    Widget found = child.findById(id);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }

    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if ((includeDisabled || isEnabled()) &&
                (getVisibility() == Visibility.Visible || getVisibility() == Visibility.Invisible)) {
            if (children != null) {
                childSort();
                for (int i = children.size() - 1; i >= 0; i--) {
                    Widget child = children.get(i);
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
                for (Widget child : children) {
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
        if (parent == widget) {
            return true;
        } else if (parent != null) {
            return parent.isChildOf(widget);
        } else {
            return false;
        }
    }

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    // ---- STATES ---- //
    protected void setStates(byte bitmask) {
        if (states != bitmask) {
            boolean applyStyle = getStyle() != null && getStyle().containsChange(states, bitmask);
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

    public UXStyle getStyle() {
        return this.style;
    }

    public void setStyle(UXStyle style) {
        if (this.style != style) {
            if (style == null) {
                this.style = null;
            } else {
                this.style = style instanceof UXStyleAttrs ?
                        (UXStyleAttrs) style : new UXStyleAttrs("attributes", style, null);
                applyStyle();
            }
        }
    }

    public void unfollowStyleProperty(String name) {
        if (style != null) {
            if (style.getClass() == UXStyle.class) {
                style = new UXStyleAttrs("attributes", style, null);
            }
            ((UXStyleAttrs) style).unfollow(name);
        }
    }

    public long getTransitionDuration() {
        return transitionDuration;
    }

    public void setTransitionDuration(long transitionDuration) {
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
                stateAnimation.setDuration((long) (transitionDuration));
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
            Application.runSync(() -> setFocused(focus));
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

    protected float getInWidth() {
        return inw;
    }

    protected float getInHeight() {
        return inh;
    }

    protected float getOutX() {
        return bg.x;
    }

    protected float getOutY() {
        return bg.y;
    }

    protected float getOutWidth() {
        return bg.width;
    }

    protected float getOutHeight() {
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

    public void setMinSize(float width, float height) {
        setMinWidth(width);
        setMinHeight(height);
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

    public void setMaxSize(float width, float height) {
        setMaxWidth(width);
        setMaxHeight(height);
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

    public void setPrefSize(float width, float height) {
        setPrefWidth(width);
        setPrefHeight(height);
    }

    public float getLayoutMinWidth() {
        return Math.max(minWidth, paddingLeft + paddingRight) + marginLeft + marginRight;
    }

    public float getLayoutMinHeight() {
        return Math.max(minHeight, paddingTop + paddingBottom) + marginTop + marginBottom;
    }

    public float getLayoutMaxWidth() {
        return maxWidth + marginLeft + marginRight;
    }

    public float getLayoutMaxHeight() {
        return maxHeight + marginTop + marginBottom;
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
            visibility = Visibility.Visible;
        }

        if (this.visibility != visibility.ordinal()) {
            this.visibility = visibility.ordinal();

            invalidate(true);
        }
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        if (this.cursor != cursor) {
            this.cursor = cursor;
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
        if (bg.arcTop != cTop ||
                bg.arcRight != cRight ||
                bg.arcBottom != cBottom ||
                bg.arcLeft != cLeft) {
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

    public void setRippleColor(int rippleColor) {
        if (this.rippleColor != rippleColor) {
            this.rippleColor = rippleColor;

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
        if (pointerEvent.getType() == PointerEvent.RELEASED) {
            if (!pointerEvent.isFocusConsumed() && isFocusable()) {
                pointerEvent.consumeFocus(true);
                requestFocus(true);
            }
        }

        if (pointerListener != null) {
            pointerListener.handle(pointerEvent);
        }
        if (!pointerEvent.isConsumed() && parent != null) {
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
        // Hover events are not consumables
        if (parent != null && hoverEvent.isRecyclable(parent)) {
            parent.fireHover(hoverEvent);
        }
        if (cursor != null) {
            Application.setCursor(cursor);
        }
    }

    public void fireScroll(ScrollEvent scrollEvent) {
        if (!scrollEvent.isConsumed() && scrollListener != null) {
            scrollListener.handle(scrollEvent);
            scrollEvent.consume();
        }
        if (!scrollEvent.isConsumed() && parent != null) {
            parent.fireScroll(scrollEvent);
        }
    }

    public void fireDrag(DragEvent dragEvent) {
        if (dragListener != null) {
            dragListener.handle(dragEvent);
        }
        if (!dragEvent.isConsumed() && parent != null && dragEvent.isRecyclable(parent)) {
            parent.fireDrag(dragEvent);
        }
    }

    public void fireKey(KeyEvent keyEvent) {
        if (keyListener != null) {
            keyListener.handle(keyEvent);
        }
        if (!keyEvent.isConsumed() && parent != null) {
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
