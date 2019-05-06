package flat.widget.layout;

import flat.animations.Interpolation;
import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.ActionListener;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.math.Mathf;
import flat.math.Vector2;
import flat.resources.Resource;
import flat.uxml.Controller;
import flat.uxml.UXChildren;
import flat.uxml.UXStyle;
import flat.uxml.UXStyleAttrs;
import flat.widget.Activity;
import flat.widget.Gadget;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enuns.Visibility;
import flat.widget.text.Button;

public class Drawer extends Parent {

    private float frontWidth;
    private float frontHeight;

    private float frontPos;

    private float hideWidth;

    private float slideGestureArea;
    private long slideAnimDuration;

    private int color;
    private boolean shown;

    private Button toggleButton;
    private Drawable showIconImage;
    private Drawable hideIconImage;

    private Widget front, back;

    private int gesPressID = -1;
    private float gesPress;
    private float gesOffset;
    private long gesTimer;

    private boolean hideEffect;

    private SlideAnimation anim = new SlideAnimation(this);
    private ActionListener actionListener = (event) -> setShown(!isShown());

    public Drawer() {
        anim.setInterpolation(Interpolation.quadOut);
    }

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        setFrontWidth(style.asSize("front-width", getFrontWidth()));
        setFrontHeight(style.asSize("front-height", getFrontHeight()));
        setHideWidth(style.asSize("hide-width", getHideWidth()));
        setSlideGestureArea(style.asSize("slide-gesture-area", getSlideGestureArea()));
        setSlideAnimDuration((long) style.asNumber("slide-anim-duration", getSlideAnimDuration()));

        style.link("toogle-button", (gadget) -> setToggleButton((Button) gadget.getWidget()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        UXStyle style = getStyle();
        if (style == null) return;

        StateInfo info = getStateInfo();

        setColor(style.asColor("color", info, getColor()));

        Resource res = getStyle().asResource("show-icon-image", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setShowIconImage(drawable);
            }
        }
        res = getStyle().asResource("hide-icon-image", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setHideIconImage(drawable);
            }
        }
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        boolean first = true;
        Gadget child;
        while ((child = children.next()) != null ) {
            Widget widget = child.getWidget();
            if (widget != null) {
                if (first) {
                    setFront(widget);
                    first = false;
                } else {
                    setBack(widget);
                    break;
                }
            }
        }
    }

    @Override
    protected void onActivityChange(Activity prev, Activity activity) {
        super.onActivityChange(prev, activity);

        if (anim.isPlaying()) {
            if (prev != null) prev.removeAnimation(anim);
            if (activity != null) activity.addAnimation(anim);
        }
    }

    @Override
    public void onMeasure() {
        final float offWidth = getPaddingLeft() + getPaddingRight();
        final float offHeight = getPaddingTop() + getPaddingBottom();
        float mWidth = Math.max(getPrefWidth(), Math.max(getMinWidth(), offWidth));
        float mHeight = Math.max(getPrefHeight(), Math.max(getMinHeight(), offHeight));

        float childrenWidth = 0, childrenMinWidth = hideEffect ? 0 : frontWidth;
        float childrenHeight = 0, childrenMinHeight = 0;
        if (back != null) {
            back.onMeasure();
            if (back.getVisibility() != Visibility.Gone) {
                if (back.getMeasureWidth() > childrenWidth) {
                    childrenWidth = back.getMeasureWidth();
                }
                if (back.getLayoutMinWidth() > childrenMinWidth) {
                    childrenMinWidth = back.getLayoutMinWidth();
                }
                if (back.getMeasureHeight() > childrenHeight) {
                    childrenHeight = back.getMeasureHeight();
                }
                if (back.getLayoutMinHeight() > childrenMinHeight) {
                    childrenMinHeight = back.getLayoutMinHeight();
                }
            }
        }
        if (front != null) {
            front.onMeasure();
        }
        if (getPrefWidth() == WRAP_CONTENT) {
            mWidth = childrenWidth + offWidth;
        } else if (mWidth < childrenMinWidth + offWidth) {
            mWidth = childrenMinWidth + offWidth;
        }
        if (getPrefHeight() == WRAP_CONTENT) {
            mHeight = childrenHeight + offHeight;
        } else if (mHeight < childrenMinHeight + offHeight) {
            mHeight = childrenMinHeight + offHeight;
        }
        setMeasure(mWidth + getMarginLeft() + getMarginRight(), mHeight + getMarginTop() + getMarginBottom());
    }


    @Override
    public void onLayout(float width, float height) {
        setLayout(Math.min(width, getMeasureWidth()), Math.min(getMeasureHeight(), height));
        setHideEffect((getInWidth() <= hideWidth));

        if (hideEffect) {
            if (back != null && back.getVisibility() != Visibility.Gone) {
                back.onLayout(getInWidth(), getInHeight());
                back.setPosition(0, 0);
            }
            if (front != null && front.getVisibility() != Visibility.Gone) {
                front.onLayout(frontWidth, getInHeight());
                front.setPosition(Mathf.clamp(frontPos + gesOffset, -frontWidth, 0), 0);
            }
        } else {
            if (front != null && front.getVisibility() != Visibility.Gone) {
                front.onLayout(frontWidth, getInHeight());
                front.setPosition(0, 0);
            }
            if (back != null && back.getVisibility() != Visibility.Gone) {
                back.onLayout(getInWidth() - frontWidth, getInHeight());
                back.setPosition(frontWidth, 0);
            }
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(getBackgroundColor(), getBorderColor(), getRippleColor(), context);

        context.setTransform2D(getTransform());
        if (back != null && back.getVisibility() == Visibility.Visible) {
            back.onDraw(context);
        }
        if (hideEffect) {
            if ((shown || anim.isPlaying() || gesOffset != 0) && front != null && front.getVisibility() == Visibility.Visible) {
                int alpha = (int) ((color & 0xFF) * (1 + Mathf.clamp(frontPos + gesOffset, -frontWidth, 0) / frontWidth));

                context.setTransform2D(getTransform());
                context.setColor((color & 0xFFFFFF00) | (alpha));
                context.drawRect(getInX(), getInY(), getInWidth(), getInHeight(), true);

                front.onDraw(context);
            }
        } else {
            if (front != null && front.getVisibility() == Visibility.Visible) {
                front.onDraw(context);
            }
        }
    }

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if ((includeDisabled || isEnabled()) &&
                (getVisibility() == Visibility.Visible || getVisibility() == Visibility.Invisible)) {
            if (hideEffect) {
                if (isShown()) {
                    if (front != null) {
                        Widget widget = front.findByPosition(x, y, includeDisabled);
                        if (widget != null) {
                            return widget;
                        }
                    }
                } else if (back != null) {
                    Widget widget = back.findByPosition(x, y, includeDisabled);
                    if (widget != null) {
                        return widget;
                    }
                }
                return isClickable() && contains(x, y) ? this : null;
            } else {
                if (front != null) {
                    Widget widget = front.findByPosition(x, y, includeDisabled);
                    if (widget != null) {
                        return widget;
                    }
                }
                if (back != null) {
                    Widget widget = back.findByPosition(x, y, includeDisabled);
                    if (widget != null) {
                        return widget;
                    }
                }
                return isClickable() && contains(x, y) ? this : null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void remove(Widget widget) {
        if (widget != null) {
            if (widget == front) {
                front = null;
            }
            if (widget == back) {
                back = null;
            }
            super.remove(widget);
        }
    }

    @Override
    protected void onSceneChange() {
        super.onSceneChange();

        if (getScene() == null) {
            if (toggleButton != null && toggleButton.getActionListener() == actionListener) {
                toggleButton.setActionListener(null);
            }
        } else {
            if (toggleButton != null && toggleButton.getActionListener() == null) {
                toggleButton.setActionListener(actionListener);
            }
        }
    }

    @Override
    public void firePointer(PointerEvent event) {
        super.firePointer(event);

        if (gesPressID == -1 && event.getType() == PointerEvent.PRESSED) {
            Vector2 point = screenToLocal(event.getX(), event.getY());
            if ((!shown && point.x < slideGestureArea) || (shown && point.x < frontWidth)) {
                gesPressID = event.getPointerID();
                gesPress = point.x;
                gesTimer = System.currentTimeMillis();
            }
        }

        if (gesPressID == event.getPointerID() && event.getType() == PointerEvent.DRAGGED) {
            Vector2 point = screenToLocal(event.getX(), event.getY());
            float gesPreOffset = point.x - gesPress;
            if (Math.abs(gesPreOffset) > slideGestureArea || gesOffset != 0) {
                gesOffset = gesPreOffset;
                invalidate(true);
            }
        }

        if (event.getType() == PointerEvent.RELEASED) {
            long t = System.currentTimeMillis();

            if (gesPressID == event.getPointerID()) {
                if (isShown() && (gesOffset < -frontWidth * 0.3 || (t - gesTimer < 300 && gesOffset < -frontWidth / 8))) {
                    setShown(false);
                } else if (!isShown() && (gesOffset > frontWidth / 4 || (t - gesTimer < 300 && gesOffset > frontWidth / 8))) {
                    setShown(true);
                } else if (event.getSource() == this && event.getType() == PointerEvent.RELEASED && shown) {
                    setShown(false);
                } else {
                    playAnim(this.shown);
                }
                gesPressID = -1;
                gesOffset = 0;
                gesTimer = 0;
                invalidate(true);
            } else if (event.getSource() == this && event.getType() == PointerEvent.RELEASED && shown) {
                setShown(false);
            }
        }
    }

    public Widget getFront() {
        return front;
    }

    public void setFront(Widget front) {
        if (this.front != front) {
            if (this.front != null) remove(this.front);
            this.front = front;
            add(front);
            invalidate(true);
        }
    }

    public Widget getBack() {
        return back;
    }

    public void setBack(Widget back) {
        if (this.back != back) {
            if (this.back != null) remove(this.back);
            this.back = back;
            add(back);
            invalidate(true);
        }
    }

    public boolean isShown() {
        return shown;
    }

    public void setShown(boolean shown) {
        if (!hideEffect) shown = true;

        if (this.shown != shown) {
            this.shown = shown;

            if (hideEffect) {
                playAnim(this.shown);
            }

            if (toggleButton != null) {
                toggleButton.setIconImage(shown ? hideIconImage : showIconImage);
            }
            invalidate(true);
        }
    }

    public float getFrontWidth() {
        return frontWidth;
    }

    public void setFrontWidth(float frontWidth) {
        if (this.frontWidth != frontWidth) {
            if (Mathf.epsilonEquals(frontPos, -this.frontWidth)) {
                frontPos = -frontWidth;
            }
            this.frontWidth = frontWidth;
            invalidate(true);
        }
    }

    public float getFrontHeight() {
        return frontHeight;
    }

    public void setFrontHeight(float frontHeight) {
        if (this.frontHeight != frontHeight) {
            this.frontHeight = frontHeight;
            invalidate(true);
        }
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        if (this.color != color) {
            this.color = color;
            invalidate(true);
        }
    }

    public long getSlideAnimDuration() {
        return slideAnimDuration;
    }

    public void setSlideAnimDuration(long milis) {
        if (this.slideAnimDuration != milis) {
            this.slideAnimDuration = milis;
            anim.setDuration(slideAnimDuration);
        }
    }

    public float getSlideGestureArea() {
        return slideGestureArea;
    }

    public void setSlideGestureArea(float slideGestureArea) {
        if (this.slideGestureArea != slideGestureArea) {
            this.slideGestureArea = slideGestureArea;
        }
    }

    public Drawable getShowIconImage() {
        return showIconImage;
    }

    public void setShowIconImage(Drawable showIconImage) {
        if (this.showIconImage != showIconImage) {
            this.showIconImage = showIconImage;
            if (toggleButton != null && !isShown()) {
                toggleButton.setIconImage(showIconImage);
            }
        }
    }

    public Drawable getHideIconImage() {
        return hideIconImage;
    }

    public void setHideIconImage(Drawable hideIconImage) {
        if (this.hideIconImage != hideIconImage) {
            this.hideIconImage = hideIconImage;
            if (toggleButton != null && isShown()) {
                toggleButton.setIconImage(hideIconImage);
            }
        }
    }

    public float getHideWidth() {
        return hideWidth;
    }

    public void setHideWidth(float hideWidth) {
        if (this.hideWidth != hideWidth) {
            this.hideWidth = hideWidth;
            invalidate(true);
        }
    }

    public Button getToggleButton() {
        return toggleButton;
    }

    public void setToggleButton(Button toggleButton) {
        if (this.toggleButton != toggleButton) {
            if (this.toggleButton != null && this.toggleButton.getActionListener() == actionListener) {
                toggleButton.setActionListener(null);
            }
            this.toggleButton = toggleButton;
            if (toggleButton != null) {
                toggleButton.setIconImage(isShown() ? hideIconImage : showIconImage);
                if (toggleButton.getActionListener() == null) {
                    toggleButton.setActionListener(actionListener);
                }
            }
        }
    }

    void setHideEffect(boolean hideEffect) {
        if (this.hideEffect != hideEffect) {
            this.hideEffect = hideEffect;

            gesPress = 0;
            gesOffset = 0;
            gesPressID = -1;
            gesTimer = 0;
            anim.stop(true);
            if (!hideEffect) {
                shown = true;
                frontPos = 0;
                if (toggleButton != null) {
                    toggleButton.setIconImage(hideIconImage);
                }
            }
            invalidate(true);
        }
    }

    void setFrontPos(float pos) {
        this.frontPos = pos;
        invalidate(true);
    }

    void playAnim(boolean toShow) {
        anim.stop(false);
        anim.setDuration(slideAnimDuration);
        anim.setValues(Mathf.clamp(frontPos + gesOffset, -frontWidth, 0), toShow ? 0 : -frontWidth);

        anim.play(getActivity());
    }

    static class SlideAnimation extends NormalizedAnimation {

        public final Drawer drawer;

        private float pos, toPos;
        private float _pos, _toPos;

        public SlideAnimation(Drawer drawer) {
            this.drawer = drawer;
        }

        public void setValues(float pos, float toPos) {
            this.pos = pos;
            this.toPos = toPos;
        }

        @Override
        protected void evaluate() {
            super.evaluate();
            if (isStopped()) {
                _pos = pos;
                _toPos = toPos;
            }
        }

        @Override
        protected void compute(float t) {
            drawer.setFrontPos(Interpolation.mix(_pos, _toPos, t));
        }
    }
}
