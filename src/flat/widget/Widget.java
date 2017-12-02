package flat.widget;

import flat.events.*;
import flat.screen.Context;
import flat.screen.Window;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Widget {
    public static final double WRAP_CONTENT = Double.NEGATIVE_INFINITY;
    public static final double MATH_PARENT = Double.POSITIVE_INFINITY;

    Widget parent;

    private String id;
    private boolean focus, focusable;
    private String nextFocusId, prevFocusId;
    private double x, y, width, height;
    private double marginTop, marginRight, marginBottom, marginLeft;
    private double paddingTop, paddingRight, paddingBottom, paddingLeft;
    private double minWidth, minHeight, maxWidth, maxHeight, prefWidth, prefHeight;
    private double offsetX, offsetY, translateX, translateY, elevation, scaleX, scaleY, rotate;
    private double opacity;
    private int visibility;

    private double mWidth, mHeight;
    private boolean clickable;

    private PointerListener pointerListener;
    private KeyListener keyListener;
    private DragListener dragListener;
    private FocusListener focusListener;

    ArrayList<Widget> children;
    ArrayList<Widget> childrenDraw;

    Comparator<Widget> comparator = (o1, o2) -> Double.compare(o1.elevation, o2.elevation);
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

    public void onLayout(double width, double height) {
        double mMinWidth, mMinHeight, mMaxWidth, mMaxHeight, mPrefWidth, mPrefHeight;

        // MAX
        if (maxWidth == MATH_PARENT) {
            mMaxWidth = width;
        } else if (maxWidth == WRAP_CONTENT) {
            mMaxWidth = Double.MAX_VALUE;
        } else {
            mMaxWidth = maxWidth;
        }
        if (maxHeight == MATH_PARENT) {
            mMaxHeight = height;
        } else if (maxHeight == WRAP_CONTENT) {
            mMaxHeight = Double.MAX_VALUE;
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

        this.width = Math.max(mMinWidth, Math.min(mMaxWidth, mPrefWidth));
        this.height = Math.max(mMinHeight, Math.min(mMaxHeight, mPrefHeight));
    }

    public void onMeasure(double width, double height) {
        if (children != null) {
            for (Widget child : children) {
                child.onLayout(width, height);
            }
        }
        setMeasure(width, height);
    }

    public void setMeasure(double width, double height) {
        mWidth = width;
        mHeight = height;
    }

    public void invalidate(boolean layout) {
        if (parent != null)
            parent.invalidate(layout);
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

    public Widget findByPosition(double x, double y) {
        return this;
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

    public boolean isFocusable() {
        return focusable;
    }

    public void setFocusable(boolean focusable) {
        this.focusable = focusable;
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

    public double getX() {
        return x;
    }

    public void setX(double x) {
        if (this.x != x) {
            this.x = x;
            invalidate(true);
        }
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        if (this.y != y) {
            this.y = y;
            invalidate(true);
        }
    }

    public double getScreenX() {
        return parent == null ? x : parent.getScreenX() + x;
    }

    public double getScreenY() {
        return parent == null ? y : parent.getScreenY() + y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(double marginTop) {
        if (this.marginTop != marginTop) {
            this.marginTop = marginTop;
            invalidate(true);
        }
    }

    public double getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(double marginRight) {
        if (this.marginRight != marginRight) {
            this.marginRight = marginRight;
            invalidate(true);
        }
    }

    public double getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(double marginBottom) {
        if (this.marginBottom != marginBottom) {
            this.marginBottom = marginBottom;
            invalidate(true);
        }
    }

    public double getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(double marginLeft) {
        if (this.marginLeft != marginLeft) {
            this.marginLeft = marginLeft;
            invalidate(true);
        }
    }

    public void setMargins(double top, double right, double bottom , double left) {
        if (marginTop != top || marginRight != right || marginBottom != bottom || marginLeft != left) {
            marginTop = top;
            marginRight = right;
            marginBottom = bottom;
            marginLeft = left;
            invalidate(true);
        }
    }

    public double getPaddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(double paddingTop) {
        if (this.paddingTop != paddingTop) {
            this.paddingTop = paddingTop;
            invalidate(true);
        }
    }

    public double getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(double paddingRight) {
        if (this.paddingRight != paddingRight) {
            this.paddingRight = paddingRight;
            invalidate(true);
        }
    }

    public double getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(double paddingBottom) {
        if (this.paddingBottom != paddingBottom) {
            this.paddingBottom = paddingBottom;
            invalidate(true);
        }
    }

    public double getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(double paddingLeft) {
        if (this.paddingLeft != paddingLeft) {
            this.paddingLeft = paddingLeft;
            invalidate(true);
        }
    }

    public void setPadding(double top, double right, double bottom , double left) {
        if (paddingTop != top || paddingRight != right || paddingBottom != bottom || paddingLeft != left) {
            paddingTop = top;
            paddingRight = right;
            paddingBottom = bottom;
            paddingLeft = left;
            invalidate(true);
        }
    }

    public double getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(double minWidth) {
        if (this.minWidth != minWidth) {
            this.minWidth = minWidth;
            invalidate(true);
        }
    }

    public double getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(double minHeight) {
        if (this.minHeight != minHeight) {
            this.minHeight = minHeight;
            invalidate(true);
        }
    }

    public double getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(double maxWidth) {
        if (this.maxWidth != maxWidth) {
            this.maxWidth = maxWidth;
            invalidate(true);
        }
    }

    public double getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(double maxHeight) {
        if (this.maxHeight != maxHeight) {
            this.maxHeight = maxHeight;
            invalidate(true);
        }
    }

    public double getPrefWidth() {
        return prefWidth;
    }

    public void setPrefWidth(double prefWidth) {
        if (this.prefWidth != prefWidth) {
            this.prefWidth = prefWidth;
            invalidate(true);
        }
    }

    public double getPrefHeight() {
        return prefHeight;
    }

    public void setPrefHeight(double prefHeight) {
        if (this.prefHeight != prefHeight) {
            this.prefHeight = prefHeight;
            invalidate(true);
        }
    }

    public double getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(double offsetX) {
        if (this.translateX != translateX) {
            this.offsetX = offsetX;
            invalidate(true);
        }
    }

    public double getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(double offsetY) {
        if (this.translateX != translateX) {
            this.offsetY = offsetY;
            invalidate(true);
        }
    }

    public double getTranslateX() {
        return translateX;
    }

    public void setTranslateX(double translateX) {
        if (this.translateX != translateX) {
            this.translateX = translateX;
            invalidate(true);
        }
    }

    public double getTranslateY() {
        return translateY;
    }

    public void setTranslateY(double translateY) {
        if (this.translateY != translateY) {
            this.translateY = translateY;
            invalidate(true);
        }
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        if (this.elevation != elevation) {
            this.elevation = elevation;
            invalidate(true);
            invalidadeOrder();
        }
    }

    public double getScaleX() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        if (this.scaleX != scaleX) {
            this.scaleX = scaleX;
            invalidate(true);
        }
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        if (this.scaleY != scaleY) {
            this.scaleY = scaleY;
            invalidate(true);
        }
    }

    public double getRotate() {
        return rotate;
    }

    public void setRotate(double rotate) {
        if (this.rotate != rotate) {
            this.rotate = rotate;
            invalidate(true);
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

    public double getOpacity() {
        return opacity;
    }

    public void setOpacity(double opacity) {
        if (this.opacity != opacity) {
            this.opacity = opacity;
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
