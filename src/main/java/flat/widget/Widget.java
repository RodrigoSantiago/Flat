package flat.widget;

import flat.animations.StateAnimation;
import flat.animations.StateBitset;
import flat.animations.StateInfo;
import flat.events.*;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.cursor.Cursor;
import flat.math.Affine;
import flat.math.Vector2;
import flat.math.shapes.RoundRectangle;
import flat.math.stroke.BasicStroke;
import flat.uxml.*;
import flat.uxml.value.UXValue;
import flat.widget.stages.Menu;
import flat.widget.effects.RippleEffect;
import flat.widget.enums.DropdownAlign;
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
    public static final Comparator<Widget> childComparator = (o1, o2) -> Float.compare(o1.elevation, o2.elevation);

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
    private Cursor currentCursor = Cursor.UNSET;

    private Menu contextMenu;

    //---------------------
    //    Family
    //---------------------
    Parent parent;
    Activity activity;
    Group group;
    final ArrayList<Widget> children = new ArrayList<>(0);
    final List<Widget> unmodifiableChildren = Collections.unmodifiableList(children);
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
    private boolean currentHandleEventsEnabled = true;
    private boolean handleEventsEnabled = true;
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
    private byte currentStateMask = 1;
    private StateAnimation stateAnimation;
    private boolean currentDisabled;

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
        attrs = new UXAttrs(this, UXAttrs.convertToKebabCase(getClass().getSimpleName()));
    }

    public void setAttributes(HashMap<Integer, UXValue> attributes, List<String> styles) {
        attrs.setAttributes(attributes);
        if (styles != null) {
            for (String style : styles) {
                attrs.addStyleName(style);
            }
        }
    }

    public void applyAttributes(Controller controller) {
        UXAttrs attrs = getAttrs();

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
        /*if (parent != null) {
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
            for (Widget child : getChildrenIterable()) {
                child.applyStyle();
            }
        }*/

        StateInfo info = getStateInfo();

        setVisibility(attrs.getConstant("visibility", info, getVisibility()));
        setCursor(attrs.getConstant("cursor", info, getCursor()));

        setFocusable(attrs.getBool("focusable", info, isFocusable()));
        setHandleEventsEnabled(attrs.getBool("handle-events-enabled", info, isHandleEventsEnabled()));

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
        Menu menu = children.getMenu();
        if (menu != null) {
            setContextMenu(menu);
        }
    }

    public void onDraw(Graphics graphics) {
        drawBackground(graphics);
        drawRipple(graphics);
        drawChildren(graphics);
    }

    protected void drawBackground(Graphics graphics) {
        if (bg.width <= 0 || bg.height <= 0) {
            return;
        }

        float bgOpacity = Color.getOpacity(backgroundColor);
        float borderOpacity = Color.getOpacity(borderColor);

        float b = borderWidth * borderOpacity;
        float b2 = b / 2;

        // Draw Background Shadow
        if (bgOpacity > 0 && shadowEnabled && elevation >= 1) {
            graphics.setTransform2D(getTransform().preTranslate(0, Math.max(0, elevation * 0.5f)));
            graphics.drawRoundRectShadow(
                    bg.x - b, bg.y - b, bg.width + b * 2, bg.height + b * 2,
                    bg.arcTop + b, bg.arcRight + b, bg.arcBottom + b, bg.arcLeft + b,
                    elevation, 0.55f * bgOpacity);
        }
        // Draw Background
        if (bgOpacity > 0) {
            graphics.setTransform2D(getTransform());
            graphics.setColor(backgroundColor);
            graphics.drawRoundRect(bg, true);
        }

        // Draw Border
        if (borderOpacity > 0 && borderWidth > 0) {
            graphics.setTransform2D(getTransform());
            graphics.setColor(borderColor);
            graphics.setStroker(new BasicStroke(borderWidth));
            graphics.drawRoundRect(
                    bg.x - b2, bg.y - b2, bg.width + b, bg.height + b,
                    bg.arcTop + b2, bg.arcRight + b2, bg.arcBottom + b2, bg.arcLeft + b2,
                    false);
        }
    }

    protected void drawRipple(Graphics graphics) {
        float rippleOpacity = Color.getOpacity(rippleColor);

        if (rippleOpacity > 0 && rippleEnabled && ripple.isVisible()) {
            graphics.setTransform2D(getTransform());
            ripple.drawRipple(graphics, rippleOverflow ? null : bg, rippleColor);
        }
    }

    protected void drawChildren(Graphics graphics) {
        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.VISIBLE) {
                child.onDraw(graphics);
            }
        }
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
    public void setLayoutPosition(float x, float y) {
        if (this.x != x || this.y != y) {
            this.x = x;
            this.y = y;
            updateRect();
            invalidate(false);
        }
    }

    protected void childInvalidate(Widget child) {
        if (activity != null) {
            if (getPrefWidth() == WRAP_CONTENT || getPrefHeight() == WRAP_CONTENT) {
                invalidate(true);
            } else {
                activity.invalidateWidget(child);
            }
        }
    }

    public void invalidate(boolean layout) {
        if (activity != null) {
            if (layout) {
                if (parent != null) {
                    parent.childInvalidate(this);
                } else {
                    activity.invalidateWidget(this);
                }
            } else {
                activity.invalidate();
            }
        }
    }

    protected void invalidateTransform() {
        for (Widget child : getChildrenIterable()) {
            child.invalidateTransform();
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
            if (group != null) {
                group.reassign(oldId, this);
            }
        }
    }

    public Activity getActivity() {
        return activity;
    }

    public void setTheme(UXTheme theme) {
        if (this.theme != theme) {
            this.theme = theme;
            onThemeChangeLocal();
        }
    }

    public UXTheme getTheme() {
        return this.theme;
    }

    public UXTheme getCurrentTheme() {
        return this.theme != null ? this.theme : parent != null ? parent.getCurrentTheme() : null;
    }

    public void refreshStyle() {
        applyStyle();
        for (var child : getChildrenIterable()) {
            child.refreshStyle();
        }
        if (contextMenu != null) {
            contextMenu.refreshStyle();
        }
    }

    /**
     * Return the current assigned group
     *
     * @return
     */
    public Group getGroup() {
        return group;
    }

    protected Group getCurrentOrGroup() {
        return group;
    }

    public Parent getParent() {
        return parent;
    }

    void setParent(Parent parent, TaskList tasks) {
        if (parent == this.parent) return;

        UXTheme themeA = getCurrentTheme();
        Group groupA = getGroup();
        Activity activityA = getActivity();

        Parent old = this.parent;
        this.parent = parent;

        Group groupB = parent == null ? null : parent.getCurrentOrGroup();
        Activity activityB = parent == null ? null : parent.getActivity();

        if (groupA != groupB) {
            onGroupChangeLocal(groupA, groupB);
        }

        if (activityA != activityB) {
            onActivityChangeLocal(activityA, activityB);
        }

        if (activityA != activityB) {
            onActivityChange(activityA, activityB, tasks);
        }

        UXTheme themeB = getCurrentTheme();
        if (themeA != themeB) {
            onThemeChangeLocal();
        }
        onDisabledChangeLocal();
        onCursorChangeLocal();
        onHandleEventsChangeLocal();
    }

    void onGroupChangeLocal(Group prev, Group current) {
        this.group = current;
        if (prev != null) {
            prev.unassign(this);
        }
        if (current != null) {
            current.assign(this);
        }
        if (getCurrentOrGroup() != this) {
            for (Widget widget : getChildrenIterable()) {
                widget.onGroupChangeLocal(prev, current);
            }
        }
    }

    void onActivityChangeLocal(Activity prev, Activity current) {
        this.activity = current;

        for (Widget widget : getChildrenIterable()) {
            widget.onActivityChangeLocal(prev, current);
        }
    }

    protected void onActivityChange(Activity prev, Activity current, TaskList tasks) {
        refreshFocus();

        if (contextMenu != null && contextMenu.isShown() && contextMenu.getActivity() != null) {
            Menu menu = this.contextMenu;
            tasks.add(() -> menu.hide());
        }

        if (ripple != null) {
            ripple.stop();
        }

        if (stateAnimation != null && stateAnimation.isPlaying()) {
            stateAnimation.stop();
        }

        for (Widget widget : getChildrenIterable()) {
            widget.onActivityChange(prev, current, tasks);
        }
    }

    void onThemeChangeLocal() {
        if (attrs.getTheme() != getCurrentTheme()) {
            attrs.setTheme(getCurrentTheme());
            if (getActivity() != null) {
                applyStyle();
            }

            if (contextMenu != null) {
                contextMenu.setTheme(getCurrentTheme());
            }

            for (Widget child : getChildrenIterable()) {
                child.onThemeChangeLocal();
            }
        }
    }

    void onCursorChangeLocal() {
        currentCursor = getShowCursor();
        for (Widget child : getChildrenIterable()) {
            child.onCursorChangeLocal();
        }
    }

    void onHandleEventsChangeLocal() {
        currentHandleEventsEnabled = getCurrentHandleEventsEnabled();
        for (Widget child : getChildrenIterable()) {
            child.onHandleEventsChangeLocal();
        }
    }

    void onDisabledChangeLocal() {
        currentDisabled = getCurrentDisabled();
        setStates(states);
        for (Widget child : getChildrenIterable()) {
            child.onDisabledChangeLocal();
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
        return new Children<>(getChildren());
    }

    public Children<Widget> getChildrenIterableReverse() {
        return new Children<>(getChildren(), true);
    }

    public Widget findById(String id) {
        if (id == null) return null;

        Group group = getGroup();
        if (group != null) {
            return group.findById(id);
        }
        return null;
    }

    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if (!isCurrentHandleEventsEnabled()
                || getVisibility() != Visibility.VISIBLE
                || (!includeDisabled && !isEnabled())
                || !contains(x, y)) {
            return null;
        }
        for (Widget child : getChildrenIterableReverse()) {
            Widget found = child.findByPosition(x, y, includeDisabled);
            if (found != null) return found;
        }
        return this;
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

    protected boolean getCurrentHandleEventsEnabled() {
        return handleEventsEnabled && (parent == null || parent.getCurrentHandleEventsEnabled());
    }

    protected boolean isCurrentHandleEventsEnabled() {
        return currentHandleEventsEnabled;
    }

    public boolean isHandleEventsEnabled() {
        return handleEventsEnabled;
    }

    public void setHandleEventsEnabled(boolean handleEventsEnabled) {
        if (this.handleEventsEnabled != handleEventsEnabled) {
            this.handleEventsEnabled = handleEventsEnabled;
            onHandleEventsChangeLocal();
        }
    }

    public Menu getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(Menu contextMenu) {
        this.contextMenu = contextMenu;
    }

    public void showContextMenu(float x, float y) {
        Activity act = getActivity();
        if (contextMenu != null && act != null) {
            contextMenu.show(act, x, y, DropdownAlign.TOP_LEFT_ADAPTATIVE);
        }
    }

    public void hideContextMenu() {
        if (contextMenu != null) {
            contextMenu.hide();
        }
    }

    // ---- STATES ---- //
    protected void setStates(byte bitmask) {
        states = bitmask;
        byte targetBitMask = (byte) (currentDisabled ? states | DISABLED.bitset() : states);

        if (currentStateMask != targetBitMask) {
            boolean applyStyle = getAttrs().containsChange(currentStateMask, targetBitMask);
            currentStateMask = targetBitMask;

            if (transitionDuration > 0) {
                if (applyStyle) {
                    if (stateAnimation == null) {
                        stateAnimation = new StateAnimation(this);
                    }
                    stateAnimation.play(currentStateMask);
                } else if (stateAnimation != null && stateAnimation.isPlaying()) {
                    stateAnimation.play(currentStateMask);
                } else if (stateAnimation != null) {
                    stateAnimation.set(currentStateMask);
                }
            } else if (applyStyle) {
                applyStyle();
            }
        }
    }

    protected byte getStateBitset() {
        return states;
    }

    protected StateInfo getStateInfo() {
        return stateAnimation != null ? stateAnimation : StateBitset.getState(currentStateMask);
    }

    protected UXAttrs getAttrs() {
        return this.attrs;
    }

    public List<String> getStyles() {
        return this.attrs.getStyleNames();
    }

    public void addStyle(String style) {
        if (attrs.addStyleName(style)) {
            applyStyle();
        }
    }

    public void addStyles(List<String> styles) {
        if (styles == null || styles.isEmpty()) {
            return;
        }

        boolean change = false;
        for (String style : styles) {
            change = attrs.addStyleName(style) || change;
        }
        if (change) {
            applyStyle();
        }
    }

    public void setStyles(List<String> styles) {
        attrs.cleatStyles();
        for (String style : styles) {
            attrs.addStyleName(style);
        }
        applyStyle();
    }

    public void clearStyles() {
        attrs.cleatStyles();
        applyStyle();
    }

    public void setFollowStyleProperty(String name, boolean follow) {
        if (follow) {
            attrs.clearUnfollow(name);
        } else {
            attrs.unfollow(name);
        }
    }

    public boolean isFollowStyleProperty(String name) {
        return !attrs.isUnfollow(name);
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

    public boolean isEnabled() {
        return !isDisabled();
    }

    public void setEnabled(boolean enabled) {
        setDisabled(!enabled);
    }

    public boolean isDisabled() {
        return currentDisabled;
    }

    public void setDisabled(boolean disabled) {
        if ((DISABLED.contains(states)) != disabled) {
            setStates((byte) (disabled ? states | DISABLED.bitset() : states & ~DISABLED.bitset()));
            onDisabledChangeLocal();
        }
    }

    protected boolean getCurrentDisabled() {
        return DISABLED.contains(states) || (parent != null && parent.getCurrentDisabled());
    }

    public boolean isActivated() {
        return ACTIVATED.contains(states);
    }

    protected void setActivated(boolean activated) {
        if (isActivated() != activated) {
            setStates((byte) (activated ? states | ACTIVATED.bitset() : states & ~ACTIVATED.bitset()));
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

    public boolean isUndefined() {
        return UNDEFINED.contains(states);
    }

    protected void setUndefined(boolean undefined) {
        if (isUndefined() != undefined) {
            setStates((byte) (undefined ? states | UNDEFINED.bitset() : states & ~UNDEFINED.bitset()));
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
                if (focused && focusable) {
                    setStates((byte) (states | FOCUSED.bitset()));
                    if (activity.getFocus() != this) {
                        activity.setFocus(this);
                    }
                } else {
                    setStates((byte) (states & ~FOCUSED.bitset()));
                    if (activity.getFocus() == this) {
                        activity.setFocus(null);
                    }
                }
            } else {
                if (focused && focusable) {
                    setStates((byte) (states | FOCUSED.bitset()));
                } else {
                    setStates((byte) (states & ~FOCUSED.bitset()));
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

    public float getLayoutX() {
        return x;
    }

    public float getLayoutY() {
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

            updateRect();
            invalidate(true);
        }
    }

    public float getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(float paddingRight) {
        if (this.paddingRight != paddingRight) {
            this.paddingRight = paddingRight;

            updateRect();
            invalidate(true);
        }
    }

    public float getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(float paddingBottom) {
        if (this.paddingBottom != paddingBottom) {
            this.paddingBottom = paddingBottom;

            updateRect();
            invalidate(true);
        }
    }

    public float getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(float paddingLeft) {
        if (this.paddingLeft != paddingLeft) {
            this.paddingLeft = paddingLeft;

            updateRect();
            invalidate(true);
        }
    }

    public void setPadding(float top, float right, float bottom , float left) {
        if (paddingTop != top || paddingRight != right || paddingBottom != bottom || paddingLeft != left) {
            paddingTop = top;
            paddingRight = right;
            paddingBottom = bottom;
            paddingLeft = left;

            updateRect();
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
        if (Math.abs(scaleX) < 0.001f) scaleX = 0.001f;

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
        if (Math.abs(scaleY) < 0.001f) scaleY = 0.001f;

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

            invalidate(parent != null && parent.invalidateChildrenOrder(this));
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

    private Cursor getShowCursor() {
        return cursor == Cursor.UNSET && parent != null ? ((Widget)parent).getShowCursor() : cursor;
    }

    public Cursor getCurrentCursor() {
        return currentCursor;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        if (this.cursor != cursor) {
            this.cursor = cursor;
            onCursorChangeLocal();
        }
    }

    private void childSort() {
        if (invalidChildrenOrder) {
            invalidChildrenOrder = false;
            sortChildren();
        }
    }

    protected void sortChildren() {
        children.sort(childComparator);
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

    public RoundRectangle getBackgroundShape() {
        return new RoundRectangle(bg);
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
                float w = getOutWidth();
                float h = getOutHeight();
                ripple.setSize(Math.min(w, h) * 0.5f);
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

    public void firePointer(PointerEvent event) {
        // -- Pressed -- //
        if (isCurrentHandleEventsEnabled()) {
            if (event.getType() == PointerEvent.PRESSED) {
                setPressed(true);
                fireRipple(event.getX(), event.getY());
            }
            if (event.getType() == PointerEvent.RELEASED) {
                setPressed(false);
                releaseRipple();

                if (event.getPointerID() == 2 && contextMenu != null) {
                    pointerMenu(event);
                }
                if (!event.isFocusConsumed() && isFocusable()) {
                    event.consumeFocus(true);
                    requestFocus(true);
                }
            }

            pointer(event);
        }

        if (event.getType() != PointerEvent.FILTER) {
            if (parent != null) {
                parent.firePointer(event);
            }
        }
    }

    public void pointerMenu(PointerEvent event) {
        showContextMenu(event.getX(), event.getY());
    }

    public void pointer(PointerEvent event) {
        UXListener.safeHandle(pointerListener, event);
    }

    public void fireHover(HoverEvent event) {
        // -- Hovered -- //
        if (isCurrentHandleEventsEnabled()) {
            if (event.getType() == HoverEvent.ENTERED) {
                setHovered(true);
            } else if (event.getType() == HoverEvent.EXITED) {
                setHovered(false);
            }

            hover(event);
        }

        if (parent != null) {
            parent.fireHover(event);
        }
    }

    public void hover(HoverEvent event) {
        UXListener.safeHandle(hoverListener, event);
    }

    public void fireScroll(ScrollEvent event) {
        if (isCurrentHandleEventsEnabled()) {
            scroll(event);
        }

        if (parent != null) {
            parent.fireScroll(event);
        }
    }

    public void scroll(ScrollEvent event) {
        UXListener.safeHandle(scrollListener, event);
    }

    public void fireDrag(DragEvent event) {
        if (isCurrentHandleEventsEnabled()) {
            drag(event);
        }

        if (parent != null && event.isRecyclable(parent)) {
            parent.fireDrag(event);
        }
    }

    public void drag(DragEvent event) {
        UXListener.safeHandle(dragListener, event);
    }

    public void fireKey(KeyEvent event) {
        if (isCurrentHandleEventsEnabled()) {
            key(event);
        }

        if (parent != null) {
            parent.fireKey(event);
        }
    }

    public void key(KeyEvent event) {
        UXListener.safeHandle(keyListener, event);
    }

    public void fireFocus(FocusEvent event) {
        if (isCurrentHandleEventsEnabled()) {
            focus(event);
        }
    }

    public void focus(FocusEvent event) {
        UXListener.safeHandle(focusListener, event);
    }

    public void fireResize() {
        if (isCurrentHandleEventsEnabled()) {
            resize();
        }
    }

    public void resize() {

    }

    @Override
    public String toString() {
        return "[" + id + "]" + getClass().getSimpleName() + "@" + Integer.toHexString(super.hashCode());
    }
}
