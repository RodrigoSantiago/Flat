package flat.widget;

import flat.events.*;
import flat.graphics.RoundRect;
import flat.graphics.Context;
import flat.math.Affine;
import flat.math.Vector2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Widget {
    public static final float WRAP_CONTENT = Float.NEGATIVE_INFINITY;
    public static final float MATH_PARENT = Float.POSITIVE_INFINITY;

    public static final int GONE = 0;
    public static final int VISIBLE = 1;
    public static final int HIDDEN = 2;

    Widget parent;

    private String id;
    private boolean focus;
    private String nextFocusId, prevFocusId;
    private float width, height;
    private float marginTop, marginRight, marginBottom, marginLeft;
    private float paddingTop, paddingRight, paddingBottom, paddingLeft;
    private float minWidth, minHeight, maxWidth, maxHeight, prefWidth, prefHeight;
    private int visibility;

    private final Affine transform = new Affine(), tmpTransform = new Affine(), inverseTransform = new Affine();
    private boolean invTransform;

    private float centerX, centerY, translateX, translateY, elevation, scaleX = 1, scaleY = 1, rotate;

    private final RoundRect background = new RoundRect();
    private float backgroundRadius;
    private int backgroundColor;

    private float opacity;

    private float mWidth, mHeight;
    private boolean clickable;

    private PointerListener pointerListener;
    private KeyListener keyListener;
    private DragListener dragListener;
    private FocusListener focusListener;

    ArrayList<Widget> children;
    ArrayList<Widget> childrenDraw;

    private static Comparator<Widget> comparator = (o1, o2) -> Float.compare(o1.elevation, o2.elevation);
    boolean childSorted;

    public void onDraw(Context context) {
        // DRAW BACK GROUND

        if  (children != null) {
            if (!childSorted) {
                childSorted = true;
                childrenDraw.sort(comparator);
            }
            for (Widget child : childrenDraw) {
                child.onDraw(context);
            }
        }
    }

    public void onLayout(float width, float height) {
        float mMinWidth, mMinHeight, mMaxWidth, mMaxHeight, mPrefWidth, mPrefHeight;

        // MAX
        if (maxWidth == MATH_PARENT) {
            mMaxWidth = width;
        } else if (maxWidth == WRAP_CONTENT) {
            mMaxWidth = Float.MAX_VALUE;
        } else {
            mMaxWidth = maxWidth;
        }
        if (maxHeight == MATH_PARENT) {
            mMaxHeight = height;
        } else if (maxHeight == WRAP_CONTENT) {
            mMaxHeight = Float.MAX_VALUE;
        } else {
            mMaxHeight = maxHeight;
        }

        // MIN
        if (minWidth == MATH_PARENT) {
            mMinWidth = width;
        } else if (minWidth == WRAP_CONTENT) {
            mMinWidth = 0;
        } else {
            mMinWidth = minWidth;
        }
        if (minHeight == MATH_PARENT) {
            mMinHeight = height;
        } else if (minHeight == WRAP_CONTENT) {
            mMinHeight = 0;
        } else {
            mMinHeight = minHeight;
        }

        // PREF
        if (prefWidth == MATH_PARENT) {
            mPrefWidth = width;
        } else if (prefWidth == WRAP_CONTENT) {
            mPrefWidth = width;
        } else {
            mPrefWidth = prefWidth;
        }
        if (prefHeight == MATH_PARENT) {
            mPrefHeight = height;
        } else if (prefHeight == WRAP_CONTENT) {
            mPrefHeight = height;
        } else {
            mPrefHeight = prefHeight;
        }

        mPrefWidth = Math.max(mMinWidth, Math.min(mMaxWidth, mPrefWidth));
        mPrefHeight = Math.max(mMinHeight, Math.min(mMaxHeight, mPrefHeight));

        onMeasure(mPrefWidth, mPrefHeight);

        if (maxWidth == WRAP_CONTENT) mMaxWidth = mWidth;
        if (maxHeight == WRAP_CONTENT) mMaxHeight = mHeight;
        if (minWidth == WRAP_CONTENT) mMinWidth = mWidth;
        if (minHeight == WRAP_CONTENT) mMinHeight = mHeight;
        if (prefWidth == WRAP_CONTENT) mPrefWidth = mWidth;
        if (prefHeight == WRAP_CONTENT) mPrefHeight = mHeight;

        setWidth(Math.max(mMinWidth, Math.min(mMaxWidth, mPrefWidth)));
        setHeight(Math.max(mMinHeight, Math.min(mMaxHeight, mPrefHeight)));
    }

    public void onMeasure(float width, float height) {
        if (children != null) {
            for (Widget child : children) {
                child.onLayout(width, height);
            }
        }
        setMeasure(width, height);
    }

    public void setMeasure(float width, float height) {
        mWidth = width;
        mHeight = height;
    }

    public void invalidate(boolean layout) {
        if (parent != null)
            parent.invalidate(layout);
    }

    public void invalidateTransform() {
        if (children != null) {
            for (Widget child : children) {
                child.invalidateTransform();
            }
            invTransform = true;
        }
    }

    public void invalidadeOrder() {
        if (parent != null)
            parent.childSorted = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Widget> getChildren() {
        return null;
    }

    public Widget findById(String id) {
        return null;
    }

    public Widget findByPosition(float x, float y) {
        if (children != null) {
            for (int i = children.size() - 1; i >= 0; i--) {
                Widget child = children.get(i);
                Widget found = child.findByPosition(x, y);
                if (found != null) return found;
            }
        }
        return visibility != GONE && clickable && contains(x, y) ? this : null;
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

    public boolean contains(Vector2 point) {
        return contains(point.x, point.y);
    }

    public boolean contains(float x, float y) {
        transform();
        float px = -(centerX * width) + inverseTransform.getPointX(x, y);
        float py = -(centerY * height) + inverseTransform.getPointY(x, y);
        return background.contains(px, py);
    }

    public float getWidth() {
        return width;
    }

    void setWidth(float width) {
        this.width = width;
        background.setWidth(width);
    }

    public float getHeight() {
        return height;
    }

    void setHeight(float height) {
        this.height = height;
        background.setHeight(height);
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

    public float getCenterX() {
        return centerX;
    }

    public void setCenterX(float centerX) {
        if (this.centerX != centerX) {
            this.centerX = centerX;
            invalidate(false);
        }
    }

    public float getCenterY() {
        return centerY;
    }

    public void setCenterY(float centerY) {
        if (this.centerY != centerY) {
            this.centerY = centerY;
            invalidate(false);
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
            invalidadeOrder();
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

    private void transform() {
        if (invTransform) {
            invTransform = false;
            transform.setAll(translateX, translateY, scaleX, scaleY, rotate);
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

    public float getBackgroundRadius() {
        return backgroundRadius;
    }

    public void setBackgroundRadius(float radius) {
        if (backgroundRadius != radius) {
            this.backgroundRadius = radius;
            background.setRadius(radius);
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

    public void setPointerListener(PointerListener pointerListener) {
        this.pointerListener = pointerListener;
    }

    public PointerListener getPointerListener() {
        return pointerListener;
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
}
