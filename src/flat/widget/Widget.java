package flat.widget;

import flat.events.*;
import flat.math.RoundRect;
import flat.graphics.Context;
import flat.math.Affine;
import flat.math.Vector2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Widget {

    //---------------------
    //    Constants
    //---------------------
    public static final float WRAP_CONTENT = 0;
    public static final float MATH_PARENT = Float.POSITIVE_INFINITY;

    public static final int GONE = 0;
    public static final int VISIBLE = 1;
    public static final int HIDDEN = 2;

    private static final Comparator<Widget> childComparator = (o1, o2) -> Float.compare(o1.elevation, o2.elevation);

    //---------------------
    //    Properties
    //---------------------
    private String id;
    private boolean focus, focusTarget;
    private String nextFocusId, prevFocusId;
    private float width, height;
    private float marginTop, marginRight, marginBottom, marginLeft;
    private float paddingTop, paddingRight, paddingBottom, paddingLeft;
    private float minWidth = WRAP_CONTENT, minHeight = WRAP_CONTENT, maxWidth = MATH_PARENT, maxHeight = MATH_PARENT, prefWidth = 0, prefHeight = 0;
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
    private final RoundRect bg = new RoundRect();
    private int backgroundColor;
    private float opacity;

    private boolean shadowEffect;
    private boolean rippleEffect;

    //---------------------
    //    Events
    //---------------------
    private boolean clickable = true;
    private PointerListener pointerListener;
    private ScrollListener scrollListener;
    private KeyListener keyListener;
    private DragListener dragListener;
    private FocusListener focusListener;

    public void onDraw(Context context) {
        if (visibility == VISIBLE) {
            if (backgroundColor != 0) {
                float x = bg.getX();
                float y = bg.getY();
                float width = bg.getWidth();
                float height = bg.getHeight();
                float c1 = bg.getCornerTop();
                float c2 = bg.getCornerRight();
                float c3 = bg.getCornerBottom();
                float c4 = bg.getCornerLeft();
                float bgAlpha = (backgroundColor & 0x000000FF) / 255f;
                if (shadowEffect && bgAlpha > 0) {
                    context.setTransform(getTransformView().translate(0, Math.max(0, elevation)));
                    if (elevation <= 2f) {
                        context.setColor(0x000000FF);
                        context.setGlobalAlpha(0.28f);
                        context.drawRoundRect(x, y, width, height, c1, c2, c3, c4, true);
                        context.setGlobalAlpha(1);
                    } else if (elevation < 24) {
                        context.drawRoundRectShadow(x, y, width, height, c1, c2, c3, c4,
                                elevation * 2, 0.28f * bgAlpha);
                    } else if (elevation < 48) {
                        context.drawRoundRectShadow(x, y, width, height, c1, c2, c3, c4,
                                48, (0.28f - ((elevation - 24) / 100f)) * bgAlpha);
                    } else {
                        context.drawRoundRectShadow(x, y, width, height, c1, c2, c3, c4,
                                48, 0.04f * bgAlpha);
                    }
                }
                context.setTransform(getTransformView());
                context.setColor(backgroundColor);
                context.drawRoundRect(x, y, width, height, c1, c2, c3, c4, true);
                context.setTransform(null);
            }

            if (children != null) {
                childSort();
                for (Widget child : children) {
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
            if (width == MATH_PARENT && (maxWidth == MATH_PARENT || maxWidth == WRAP_CONTENT)) {
                setWidth(parent.getWidth());
            } else {
                setWidth(Math.min(width, maxWidth));
            }
            if (height == MATH_PARENT && (maxHeight == MATH_PARENT || maxHeight == WRAP_CONTENT)) {
                setHeight(parent.getHeight());
            } else {
                setHeight(Math.min(height, maxHeight));
            }
        } else {
            if (maxWidth != MATH_PARENT && maxWidth != WRAP_CONTENT) {
                setWidth(Math.min(width, maxWidth));
            } else {
                setWidth(width);
            }
            if (maxHeight != MATH_PARENT && maxHeight != WRAP_CONTENT) {
                setHeight(Math.min(height, maxHeight));
            } else {
                setHeight(height);
            }
        }
        setX(x);
        setY(y);
    }

    public void onMeasure() {
        setMeasure(prefWidth, prefHeight);
    }

    public final void setMeasure(float width, float height) {
        measureWidth = Math.max(width, minWidth);
        measureHeight = Math.max(height, minHeight);
    }

    public float getMeasureWidth()  {
        return measureWidth;
    }

    public float getMeasureHeight()  {
        return measureHeight;
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
        this.id = id;
    }

    public Parent getParent() {
        return parent;
    }

    void setParent(Parent parent) {
        if (this.parent != null && parent != null) {
            this.parent.childRemove(this);
        }
        this.parent = parent;
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
        return (visibility == VISIBLE || visibility == HIDDEN) && clickable && contains(x, y) ? this : null;
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

    public boolean isClickable() {
        return clickable;
    }

    public void setClickable(boolean clickable) {
        this.clickable = clickable;
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
        float x = transform.getPointX(point.x, point.y);
        float y = transform.getPointY(point.x, point.y);
        point.x = x;
        point.y = y;
    }

    public void screenToLocal(Vector2 point) {
        transform();
        float x = inverseTransform.getPointX(point.x, point.y);
        float y = inverseTransform.getPointY(point.x, point.y);
        point.x = x;
        point.y = y;
    }

    public boolean contains(float x, float y) {
        transform();
        float px = inverseTransform.getPointX(x, y);
        float py = inverseTransform.getPointY(x, y);
        return bg.contains(px, py);
    }

    public float getX() {
        return x;
    }

    void setX(float x) {
        if (this.x != x) {
            this.x = x;
            bg.setX(x + marginLeft);
            invalidateTransform();
        }
    }

    public float getY() {
        return y;
    }

    void setY(float y) {
        if (this.y != y) {
            this.y = y;
            bg.setY(y + marginTop);
            invalidateTransform();
        }
    }

    public float getWidth() {
        return width;
    }

    void setWidth(float width) {
        if (this.width != width) {
            this.width = width;
            bg.setWidth(width - marginLeft - marginRight);
        }
    }

    public float getHeight() {
        return height;
    }

    void setHeight(float height) {
        if (this.height != height) {
            this.height = height;
            bg.setHeight(height - marginTop - marginBottom);
        }
    }

    public float getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(float marginTop) {
        if (this.marginTop != marginTop) {
            this.marginTop = marginTop;
            invalidate(true);
        }
    }

    public float getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(float marginRight) {
        if (this.marginRight != marginRight) {
            this.marginRight = marginRight;
            invalidate(true);
        }
    }

    public float getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(float marginBottom) {
        if (this.marginBottom != marginBottom) {
            this.marginBottom = marginBottom;
            invalidate(true);
        }
    }

    public float getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(float marginLeft) {
        if (this.marginLeft != marginLeft) {
            this.marginLeft = marginLeft;
            invalidate(true);
        }
    }

    public void setMargins(float top, float right, float bottom , float left) {
        if (marginTop != top || marginRight != right || marginBottom != bottom || marginLeft != left) {
            marginTop = top;
            marginRight = right;
            marginBottom = bottom;
            marginLeft = left;
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

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
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
            transform.toTranslation(-centerX, -centerY)
                    .scale(scaleX, scaleY)
                    .rotate(rotate)
                    .translate(centerX + translateX + x, centerY + translateY + y);

            if (parent != null) {
                transform.preMultiply(parent.getTransformView());
            }
            inverseTransform.set(transform).invert();
        }
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
        return bg.getCornerTop();
    }

    public float getBackgroundCornerRight() {
        return bg.getCornerRight();
    }

    public float getBackgroundCornerBottom() {
        return bg.getCornerBottom();
    }

    public float getBackgroundCornerLeft() {
        return bg.getCornerLeft();
    }

    public void setBackgroundCorners(float cTop, float cRight, float cBottom, float cLeft) {
        if (bg.getCornerTop() != cTop ||
                bg.getCornerRight() != cRight ||
                bg.getCornerBottom() != cBottom ||
                bg.getCornerLeft() != cLeft) {
            bg.setCorners(cTop, cRight, cBottom, cLeft);
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
            invalidate(false);
        }
    }

    public void setPointerListener(PointerListener pointerListener) {
        this.pointerListener = pointerListener;
    }

    public PointerListener getPointerListener() {
        return pointerListener;
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
        boolean done = false;
        if (pointerListener != null) {
            done = pointerListener.handle(pointerEvent);
        }
        if (!done && parent != null) {
            parent.firePointer(pointerEvent.recycle(parent));
        }
    }

    public void fireScroll(ScrollEvent scrollEvent) {
        boolean done = false;
        if (scrollListener != null) {
            done = scrollListener.handle(scrollEvent);
        }
        if (!done && parent != null) {
            parent.fireScroll(scrollEvent.recycle(parent));
        }
    }

    public void fireDrag(DragEvent dragEvent) {
        boolean done = false;
        if (dragListener != null) {
            done = dragListener.handle(dragEvent);
        }
        if (!done && parent != null) {
            parent.fireDrag(dragEvent.recycle(parent));
        }
    }

    public void fireKey(KeyEvent keyEvent) {
        boolean done = false;
        if (keyListener != null) {
            done = keyListener.handle(keyEvent);
        }
        if (!done && parent != null) {
            parent.fireKey(keyEvent.recycle(parent));
        }
    }

    @Override
    public String toString() {
        return "[" + id + "]" + getClass().getSimpleName();
    }
}
