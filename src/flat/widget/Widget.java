package flat.widget;

import flat.events.*;
import flat.graphics.SmartContext;
import flat.math.*;
import flat.math.shapes.Rectangle;
import flat.math.shapes.RoundRectangle;
import flat.math.shapes.Shape;
import flat.uxml.Controller;
import flat.uxml.UXAttributes;
import flat.uxml.UXChildren;

import java.lang.reflect.Method;
import java.util.*;

public class Widget {

    //---------------------
    //    Constants
    //---------------------
    public static final float WRAP_CONTENT = 0;
    public static final float MATCH_PARENT = Float.POSITIVE_INFINITY;

    public static final int GONE = 0;
    public static final int VISIBLE = 1;
    public static final int INVISIBLE = 2;

    protected static final HashMap<String, Integer> visibilities = UXAttributes.atts(
            "GONE", GONE,
            "VISIBLE", VISIBLE,
            "INVISIBLE", INVISIBLE
    );

    private static final Comparator<Widget> childComparator = (o1, o2) -> Float.compare(o1.elevation, o2.elevation);
    //---------------------
    //    Properties
    //---------------------
    private String id;
    private boolean enabled = true, focus, focusTarget;
    private String nextFocusId, prevFocusId;
    private float width, height;
    private float marginTop, marginRight, marginBottom, marginLeft;
    private float paddingTop, paddingRight, paddingBottom, paddingLeft;
    private float minWidth, minHeight, maxWidth = MATCH_PARENT, maxHeight = MATCH_PARENT, prefWidth, prefHeight;
    private int visibility = VISIBLE;
    private float measureWidth, measureHeight;

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
    private final Affine tmpTransform = new Affine();
    boolean invalidTransform;

    //---------------------
    //    Drawing
    //---------------------
    private final RoundRectangle bg = new RoundRectangle();
    private final Rectangle pd = new Rectangle();
    private int backgroundColor;
    private float opacity = 1;

    private RippleEffect ripple;
    private int rippleColor;

    private Shape clip;

    private boolean shadowEffect;
    private boolean rippleEffect;

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

    public Widget() {

    }

    public Widget(UXAttributes attributes) {
        this(null, attributes);
    }

    public Widget(Controller controller, UXAttributes attributes) {
        applyAttributes(controller, attributes);
    }

    public void applyAttributes(Controller controller, UXAttributes attributes) {
        String id = attributes.asString("id", null);
        setId(id);
        if (id != null && controller != null) {
            controller.assign(id, this);
        }

        setNextFocusId(attributes.asString("nextFocusId", null));
        setPrefWidth(attributes.asSize("width", WRAP_CONTENT));
        setPrefHeight(attributes.asSize("height", WRAP_CONTENT));
        setMaxWidth(attributes.asSize("maxWidth", MATCH_PARENT));
        setMaxHeight(attributes.asSize("maxHeight", MATCH_PARENT));
        setMinWidth(attributes.asSize("minWidth", WRAP_CONTENT));
        setMinHeight(attributes.asSize("minHeight", WRAP_CONTENT));

        setMargins(attributes.asSize("marginTop", 0), attributes.asSize("marginRight", 0),
                attributes.asSize("marginBottom", 0), attributes.asSize("marginLeft", 0));

        setPadding(attributes.asSize("paddingTop", 0), attributes.asSize("paddingRight", 0),
                attributes.asSize("paddingBottom", 0), attributes.asSize("paddingLeft", 0));

        setTranslateX(attributes.asSize("translateX", 0));
        setTranslateY(attributes.asSize("translateY", 0));
        setElevation(attributes.asSize("elevation", 0));
        setShadowEffectEnabled(attributes.asBoolean("shadowEffect", false));
        setRippleEffectEnabled(attributes.asBoolean("rippleEffect", false));
        setCenterX(attributes.asNumber("centreX", 0.5f));
        setCenterY(attributes.asNumber("centreY", 0.5f));
        setScaleX(attributes.asNumber("scaleX", 1));
        setScaleY(attributes.asNumber("scaleY", 1));
        setRotate(attributes.asNumber("rotate", 0));
        setOpacity(attributes.asNumber("opacity", 1));
        setFocusTarget(attributes.asBoolean("focusTarget", true));
        setEnabled(attributes.asBoolean("enabled", true));
        setClickable(attributes.asBoolean("clickable", true));

        setBackgroundColor(attributes.asColor("backgroundColor", 0));
        setBackgroundCorners(attributes.asNumber("backgroundCornerTop", 0), attributes.asNumber("backgroundCornerRight", 0),
                attributes.asNumber("backgroundCornerBottom", 0), attributes.asNumber("backgroundCornerLeft", 0));

        setRippleColor(attributes.asColor("rippleColor", 0x00000030));

        setVisibility(attributes.asConstant("visibility", visibilities, VISIBLE));

        Method handle = attributes.asListener("onPointer", PointerEvent.class, controller);
        if (handle != null) {
            setPointerListener(new PointerListener.AutoPointerListener(controller, handle));
        }
        handle = attributes.asListener("onHover", HoverEvent.class, controller);
        if (handle != null) {
            setHoverListener(new HoverListener.AutoHoverListener(controller, handle));
        }
        handle = attributes.asListener("onScroll", ScrollEvent.class, controller);
        if (handle != null) {
            setScrollListener(new ScrollListener.AutoScrollListener(controller, handle));
        }
        handle = attributes.asListener("onKey", KeyEvent.class, controller);
        if (handle != null) {
            setKeyListener(new KeyListener.AutoKeyListener(controller, handle));
        }
        handle = attributes.asListener("onDrag", DragEvent.class, controller);
        if (handle != null) {
            setDragListener(new DragListener.AutoDragListener(controller, handle));
        }
        handle = attributes.asListener("onFocus", FocusEvent.class, controller);
        if (handle != null) {
            setFocusListener(new FocusListener.AutoFocusListener(controller, handle));
        }
    }

    public void applyChildren(UXChildren children) {

    }

    public void onDraw(SmartContext context) {
        backgroundDraw(context);
        childrenDraw(context);
    }

    protected void backgroundDraw(SmartContext context) {
        float bgAlpha = (backgroundColor & 0xFF) / 255f * getDisplayOpacity();
        if (backgroundColor != 0) {
            if (shadowEffect && bgAlpha > 0) {
                context.setTransform2D(getTransformView().preTranslate(0, Math.max(0, elevation)));
                context.drawRoundRectShadow(bg, elevation * 2, 0.28f * bgAlpha);
            }
            context.setTransform2D(getTransformView());
            context.setColor(backgroundColor);
            context.setAlpha(getDisplayOpacity());
            context.drawRoundRect(bg, true);
            if (rippleEffect && ripple.isVisible()) {
                ripple.drawRipple(context, bg, rippleColor);
            }
            context.setTransform2D(null);
        } else if (rippleEffect && ripple.isVisible()) {
            context.setTransform2D(getTransformView());
            ripple.drawRipple(context, bg, rippleColor);
            context.setTransform2D(null);
        }
    }

    protected void childrenDraw(SmartContext context) {
        if (children != null) {
            childSort();
            for (Widget child : children) {
                if (child.getVisibility() == VISIBLE) {
                    child.onDraw(context);
                }
            }
        }
    }


    public void onLayout(float x, float y, float width, float height) {
        setLayout(x, y, Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
    }

    public final void setLayout(float x, float y, float width, float height) {
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
        setX(x);
        setY(y);
    }

    public void onMeasure() {
        setMeasure(getLayoutPrefWidth(), getLayoutPrefHeight());
    }

    public final void setMeasure(float width, float height) {
        measureWidth = Math.max(width, getLayoutMinWidth());
        measureHeight = Math.max(height, getLayoutMinHeight());
    }

    public float getMeasureWidth()  {
        return measureWidth;
    }

    public float getMeasureHeight()  {
        return measureHeight;
    }

    public void requestRender() {
        invalidate(false);
    }

    public void requestLayout() {
        invalidate(true);
    }

    protected void invalidate(boolean layout) {
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

    public String getId() {
        return id;
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
        if (this.parent != null && parent != null) {
            this.parent.childRemove(this);
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
        }

    }

    public List<Widget> getUnmodifiableChildren() {
        return unmodifiableChildren;
    }

    protected ArrayList<Widget> getChildren() {
        return children;
    }

    public Widget findById(String id) {
        return null;
    }

    public Widget findByPosition(float x, float y) {
        if (children != null) {
            childSort();
            for (int i = children.size() - 1; i >= 0; i--) {
                Widget child = children.get(i);
                Widget found = child.findByPosition(x, y);
                if (found != null) return found;
            }
        }
        return (visibility == VISIBLE || visibility == INVISIBLE) && clickable && contains(x, y) ? this : null;
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

    public boolean isDisabled() {
        return parent == null ? !enabled : parent.isDisabled() || !enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFocused() {
        return focus;
    }

    public void setFocus(boolean focus) {
        this.focus = focus;
    }

    public boolean isFocusTarget() {
        return focusTarget;
    }

    public void setFocusTarget(boolean focusTarget) {
        this.focusTarget = focusTarget;
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
        return pd.x;
    }

    protected float getInY() {
        return pd.y;
    }

    protected float getInWidth() {
        return pd.width;
    }

    protected float getInHeight() {
        return pd.height;
    }

    public float getX() {
        return x;
    }

    void setX(float x) {
        if (this.x != x) {
            this.x = x;
            updateRect();
        }
    }

    public float getY() {
        return y;
    }

    void setY(float y) {
        if (this.y != y) {
            this.y = y;
            updateRect();
        }
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
        if (this.minWidth != width ||  this.minHeight != height) {
            this.minWidth = width;
            this.minHeight = height;
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

    public void setMaxSize(float width, float height) {
        if (this.maxWidth != width ||  this.maxHeight != height) {
            this.maxWidth = width;
            this.maxHeight = height;
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

    public void setPrefSize(float width, float height) {
        if (this.prefWidth != width ||  this.prefHeight != height) {
            this.prefWidth = width;
            this.prefHeight = height;
            invalidate(true);
        }
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

    public float getLayoutPrefWidth() {
        return prefWidth + paddingLeft + paddingRight + marginLeft + marginRight;
    }

    public float getLayoutPrefHeight() {
        return prefHeight + paddingTop + paddingBottom + marginTop + marginBottom;
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
        if (this.rotate != rotate) {
            if (rotate < 0 || rotate > 360) rotate = rotate % 360;
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

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        if (this.visibility != visibility) {
            this.visibility = visibility;
            invalidate(true);
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
                transform.preMul(parent.getTransformView()); // multiply
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

        pd.x = lm + rm > getWidth() ? (lm + getWidth() - rm) / 2f : lm;
        pd.y = tm + bm > getHeight() ? (tm + getHeight() - bm) / 2f : tm;
        pd.width = Math.max(0, getWidth() - lm - rm);
        pd.height = Math.max(0, getHeight() - tm - bm);
        invalidateTransform();
    }

    public Affine getTransformView() {
        transform();
        return tmpTransform.set(transform);
    }

    public Affine getInverseTransformView() {
        transform();
        return tmpTransform.set(inverseTransform);
    }

    public float getBackgroundCornerTop() {
        return bg.arcTop;
    }

    public float getBackgroundCornerRight() {
        return bg.arcRight;
    }

    public float getBackgroundCornerBottom() {
        return bg.arcBottom;
    }

    public float getBackgroundCornerLeft() {
        return bg.arcLeft;
    }

    public void setBackgroundCorners(float cTop, float cRight, float cBottom, float cLeft) {
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

    public boolean isShadowEffectEnabled() {
        return shadowEffect;
    }

    public void setShadowEffectEnabled(boolean enable) {
        if (this.shadowEffect != enable) {
            this.shadowEffect = enable;
            invalidate(false);
        }
    }

    public boolean isRippleEffectEnabled() {
        return rippleEffect;
    }

    public void setRippleEffectEnabled(boolean enable) {
        if (this.rippleEffect != enable) {
            this.rippleEffect = enable;
            ripple = enable ? new RippleEffect(this) : null;
            invalidate(false);
        }
    }

    public int getRippleColor() {
        return rippleColor;
    }

    public void setRippleColor(int rippleColor) {
        this.rippleColor = rippleColor;
    }

    public void fireRipple(float x, float y) {
        if (rippleEffect) {
            transform();
            float ix = inverseTransform.pointX(x, y);
            float iy = inverseTransform.pointY(x, y);
            ripple.fire(ix, iy);
        }
    }

    public void releaseRipple() {
        if (rippleEffect) {
            ripple.release();
        }
    }

    protected RippleEffect getRipple() {
        return ripple;
    }

    public void setClip(Shape clip) {
        if (this.clip != clip) {
            this.clip = clip;
            invalidate(true);
        }
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
        if (pointerEvent.getType() == PointerEvent.PRESSED) {
            fireRipple(pointerEvent.getX(), pointerEvent.getY());
        } else if (pointerEvent.getType() == PointerEvent.RELEASED) {
            releaseRipple();
        }
        if (pointerListener != null) {
            pointerListener.handle(pointerEvent);
        }
        if (!pointerEvent.isConsumed() && parent != null) {
            parent.firePointer(pointerEvent.recycle(parent));
        }
    }

    public void fireHover(HoverEvent hoverEvent) {
        if (hoverListener != null) {
            hoverListener.handle(hoverEvent);
        }
        // Hover events are not consumables
        if (parent != null && hoverEvent.isRecyclable(parent)) {
            parent.fireHover(hoverEvent.recycle(parent));
        }
    }

    public void fireScroll(ScrollEvent scrollEvent) {
        if (scrollListener != null) {
            scrollListener.handle(scrollEvent);
        }
        if (!scrollEvent.isConsumed() && parent != null) {
            parent.fireScroll(scrollEvent.recycle(parent));
        }
    }

    public void fireDrag(DragEvent dragEvent) {
        if (dragListener != null) {
            dragListener.handle(dragEvent);
        }
        if (!dragEvent.isConsumed() && parent != null && dragEvent.isRecyclable(parent)) {
            parent.fireDrag(dragEvent.recycle(parent));
        }
    }

    public void fireKey(KeyEvent keyEvent) {
        if (keyListener != null) {
            keyListener.handle(keyEvent);
        }
        if (!keyEvent.isConsumed() && parent != null) {
            parent.fireKey(keyEvent.recycle(parent));
        }
    }

    @Override
    public String toString() {
        return "[" + id + "]" + getClass().getSimpleName();
    }
}
