package flat.widget.layout;

import flat.animations.Animation;
import flat.animations.Interpolation;
import flat.animations.StateInfo;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXChildren;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.Position;
import flat.widget.enums.VerticalAlign;
import flat.widget.enums.Visibility;
import flat.window.Activity;

import java.util.List;

public class Drawer extends Parent {

    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;
    private Position slidePosition = Position.LEFT;
    private float slideAnimationDuration = 0;
    private boolean floating = true;
    private boolean autoClose;
    private boolean blockEvents;
    private int blockColor = Color.transparent;
    private Widget slideContent;

    protected final SlideAnimation slideAnimation = new SlideAnimation();
    private boolean shown;
    private float lerpPos;

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        for (var child : children) {
            if (child.getAttributeBool("slide-content", false)) {
                setSlideContent(child.getWidget());
            } else {
                add(child.getWidget());
            }
        }
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();
        setBlockEvents(attrs.getAttributeBool("block-events", isBlockEvents()));
        setAutoClose(attrs.getAttributeBool("auto-close", isAutoClose()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
        setVerticalAlign(attrs.getConstant("vertical-align", info, getVerticalAlign()));
        setSlidePosition(attrs.getConstant("slide-position", info, getSlidePosition()));
        setSlideAnimationDuration(attrs.getNumber("slide-animation-duration", info, getSlideAnimationDuration()));
        setBlockColor(attrs.getColor("block-color", info, getBlockColor()));
        setFloating(attrs.getBool("floating", info, isFloating()));
    }

    @Override
    public void onMeasure() {
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        if (isFloating() || slideContent == null
                || (isSlideHorizontal() && !wrapWidth)
                || (isSlideVertical() && !wrapHeight)) {
            performMeasureStack();
            return;
        }
        
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth = 0;
        float mHeight = 0;

        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE) continue;
            child.onMeasure();
        }
        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE || child == slideContent) continue;

            if (wrapWidth) {
                if (child.getMeasureWidth() == MATCH_PARENT) {
                    float mW = Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth());
                    if (mW > mWidth) {
                        mWidth = mW;
                    }
                } else if (child.getMeasureWidth() > mWidth) {
                    mWidth = child.getMeasureWidth();
                }
            }
            if (wrapHeight) {
                if (child.getMeasureHeight() == MATCH_PARENT) {
                    float mH = Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight());
                    if (mH > mHeight) {
                        mHeight = mH;
                    }
                } else if (child.getMeasureHeight() > mHeight) {
                    mHeight = child.getMeasureHeight();
                }
            }
        }

        if (wrapWidth && slideContent != null) {
            float mW;
            if (slideContent.getMeasureWidth() == MATCH_PARENT) {
                mW = Math.min(slideContent.getMeasureWidth(), slideContent.getLayoutMaxWidth());
            } else {
                mW = slideContent.getMeasureWidth();
            }
            if (isSlideHorizontal()) {
                mWidth += lerp(0, mW);
            } else if (mW > mWidth) {
                mWidth = mW;
            }
        }

        if (wrapHeight && slideContent != null) {
            float mH;
            if (slideContent.getMeasureHeight() == MATCH_PARENT) {
                mH = Math.min(slideContent.getMeasureHeight(), slideContent.getLayoutMaxHeight());
            } else {
                mH = slideContent.getMeasureHeight();
            }
            if (isSlideVertical()) {
                mHeight += lerp(0, mH);
            } else if (mH > mHeight) {
                mHeight = mH;
            }
        }

        if (wrapWidth) {
            mWidth = Math.max(mWidth + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(mHeight + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);

        float lx = getInX();
        float ly = getInY();
        float lWidth = getInWidth();
        float lHeight = getInHeight();

        if (slideContent == null || isFloating() || lerpPos <= 0) {
            for (Widget child : getChildrenIterable()) {
                if (child.getVisibility() == Visibility.GONE) continue;
                if (child == slideContent) {
                    float sWidth = Math.min(Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth()), lWidth);
                    float sHeight = Math.min(Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight()), lHeight);
                    performLayoutContent(slideContent, sWidth, sHeight);
                } else {
                    performSingleLayoutConstraints(getInWidth(), getInHeight(), getInX(), getInY(), child
                            , verticalAlign, horizontalAlign);
                }
            }
        } else {
            float slw = Math.min(slideContent.getMeasureWidth(), slideContent.getLayoutMaxWidth());
            float slh = Math.min(slideContent.getMeasureHeight(), slideContent.getLayoutMaxHeight());
            float cw = 0;
            float ch = 0;
            float screenW = 0;
            float screenH = 0;
            for (Widget child : getChildrenIterable()) {
                if (child.getVisibility() == Visibility.GONE || slideContent == child) continue;
                cw = Math.max(cw, Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth()));
                ch = Math.max(ch, Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight()));
            }
            if (isSlideHorizontal()) {
                if (cw == MATCH_PARENT && slw == MATCH_PARENT) {
                    slw = lWidth * 0.5f;
                } else if (slw == MATCH_PARENT) {
                    slw = lWidth - cw;
                } else if (slw + cw > lWidth) {
                    slw = slw / (slw + cw) * lWidth;
                }
                screenW = lerp(0, slw);
                cw = lWidth - screenW;
            } else {
                if (ch == MATCH_PARENT && slh == MATCH_PARENT) {
                    slh = lHeight * 0.5f;
                } else if (slh == MATCH_PARENT) {
                    slh = lHeight - ch;
                } else if (slh + ch > lHeight) {
                    slh = slh / (slh + ch) * lHeight;
                }
                screenH = lerp(0, slh);
                ch = lHeight - screenH;
            }
            cw = Math.min(cw, lWidth);
            ch = Math.min(ch, lHeight);
            slw = Math.min(slw, lWidth);
            slh = Math.min(slh, lHeight);
            for (Widget child : getChildrenIterable()) {
                if (child.getVisibility() == Visibility.GONE) continue;
                if (child == slideContent) {
                    performLayoutContent(slideContent, slw, slh);
                } else {
                    performSingleLayoutConstraints(cw, ch, screenW, screenH, child
                            , verticalAlign, horizontalAlign);
                }
            }
        }
    }

    private float lerp(float hide, float show) {
        float t = Interpolation.fade.apply(lerpPos);
        return hide * (1 - t) + show * t;
    }

    private void performLayoutContent(Widget child, float childWidth, float childHeight) {
        float lx = getInX();
        float ly = getInY();
        float lWidth = getInWidth();
        float lHeight = getInHeight();
        child.onLayout(childWidth, childHeight);

        float xPos = off(lx, lx + lWidth, child.getLayoutWidth(), horizontalAlign);
        float yPos = off(ly, ly + lHeight, child.getLayoutHeight(), verticalAlign);
        if (getSlidePosition() == Position.LEFT) {
            child.setLayoutPosition(getInX() + lerp(-childWidth, 0), yPos);
        } else if (getSlidePosition() == Position.RIGHT) {
            child.setLayoutPosition(getInX() + getInWidth() + lerp(0, -childWidth), yPos);
        } else if (getSlidePosition() == Position.TOP) {
            child.setLayoutPosition(xPos, getInY() + lerp(-childHeight, 0));
        } else if (getSlidePosition() == Position.BOTTOM) {
            child.setLayoutPosition(xPos, getInY() + getInHeight() + lerp(0, -childHeight));
        }
    }

    @Override
    public boolean onLayoutSingleChild(Widget child) {
        if (!isFloating()) return false;

        if (child == slideContent) {
            child.onMeasure();
            float lWidth = getInWidth();
            float lHeight = getInHeight();
            float sWidth = Math.min(Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth()), lWidth);
            float sHeight = Math.min(Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight()), lHeight);
            performLayoutContent(child, sWidth, sHeight);
            return true;
        }
        if (getChildren().contains(child)) {
            child.onMeasure();
            performSingleLayoutConstraints(getInWidth(), getInHeight(), getInX(), getInY(), child
                    , verticalAlign, horizontalAlign);
            return true;
        }
        return false;
    }

    @Override
    public void onDraw(Graphics graphics) {
        drawBackground(graphics);
        drawRipple(graphics);

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        for (Widget child : getChildrenIterable()) {
            if (child != getSlideContent() && child.getVisibility() == Visibility.VISIBLE) {
                child.onDraw(graphics);
            }
        }

        if (getSlideContent() != null && getSlideContent().getVisibility() == Visibility.VISIBLE && lerpPos > 0) {
            if (isBlockEvents() && Color.getAlpha(getBlockColor()) > 0) {
                graphics.setTransform2D(getTransform());
                graphics.setColor(Color.multiplyColorAlpha(getBlockColor(), lerp(0, 1)));
                graphics.drawRect(x, y, width, height, true);
            }
            boolean clip = (lerpPos != 0 && lerpPos != 1)
                    && (getActivity() != null && getParent() != getActivity().getScene());

            graphics.setTransform2D(getTransform());
            if (clip) graphics.pushClip(getBackgroundShape());
            getSlideContent().onDraw(graphics);
            if (clip) graphics.popClip();
        }
    }

    @Override
    public void add(Widget child) {
        super.add(child);
    }

    @Override
    public void add(Widget... children) {
        super.add(children);
    }

    @Override
    public void add(List<Widget> children) {
        super.add(children);
    }

    @Override
    protected boolean detachChild(Widget child) {
        if (child == slideContent) {
            return false;
        }
        return super.detachChild(child);
    }

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if (!isCurrentHandleEventsEnabled()
                || getVisibility() != Visibility.VISIBLE
                || (!includeDisabled && !isEnabled())
                || !contains(x, y)) {
            return null;
        }
        if (isShown() && getSlideContent() != null) {
            Widget found = getSlideContent().findByPosition(x, y, includeDisabled);
            if (found != null) return found;
        }
        if (!isBlockEvents() || !isShown()) {
            for (Widget child : getChildrenIterableReverse()) {
                Widget found = child.findByPosition(x, y, includeDisabled);
                if (found != null) return found;
            }
        }
        return this;
    }

    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        if (isShown() && isAutoClose() && !event.isConsumed() && event.getPointerID() == 1
                && event.getType() == PointerEvent.RELEASED && event.getSource() == this) {
            hide();
        }
    }

    public Widget getSlideContent() {
        return slideContent;
    }

    public void setSlideContent(Widget slideContent) {
        if (this.slideContent != slideContent) {
            Widget old = this.slideContent;
            if (slideContent != null) {
                add(slideContent);
                if (slideContent.getParent() == this) {
                    this.slideContent = slideContent;
                    if (old != null) {
                        remove(old);
                    }
                }
            } else {
                this.slideContent = null;
                remove(old);
            }
        }
    }

    public Position getSlidePosition() {
        return slidePosition;
    }

    public void setSlidePosition(Position slidePosition) {
        if (slidePosition == null) slidePosition = Position.LEFT;

        if (this.slidePosition != slidePosition) {
            this.slidePosition = slidePosition;
            invalidate(true);
        }
    }

    protected boolean isSlideHorizontal() {
        return slidePosition == Position.LEFT || slidePosition == Position.RIGHT;
    }


    protected boolean isSlideVertical() {
        return slidePosition == Position.TOP || slidePosition == Position.BOTTOM;
    }

    public float getSlideAnimationDuration() {
        return slideAnimationDuration;
    }

    public void setSlideAnimationDuration(float slideAnimationDuration) {
        if (this.slideAnimationDuration != slideAnimationDuration) {
            this.slideAnimationDuration = slideAnimationDuration;
        }
    }

    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(VerticalAlign verticalAlign) {
        if (verticalAlign == null) verticalAlign = VerticalAlign.TOP;

        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(true);
        }
    }

    public HorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
        if (horizontalAlign == null) horizontalAlign = HorizontalAlign.LEFT;

        if (this.horizontalAlign != horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            invalidate(true);
        }
    }

    public boolean isBlockEvents() {
        return blockEvents;
    }

    public void setBlockEvents(boolean blockEvents) {
        this.blockEvents = blockEvents;
    }

    public int getBlockColor() {
        return blockColor;
    }

    public void setBlockColor(int blockColor) {
        if (this.blockColor != blockColor) {
            this.blockColor = blockColor;
            invalidate(false);
        }
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public void setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
    }

    public boolean isFloating() {
        return floating;
    }

    public void setFloating(boolean floating) {
        if (this.floating != floating) {
            this.floating = floating;
            invalidate(true);
        }
    }

    public boolean isShown() {
        return shown;
    }

    private void setShown(boolean shown) {
        if (this.shown != shown) {
            this.shown = shown;
            if (slideAnimationDuration > 0) {
                slideAnimation.play();
            } else {
                lerpPos = isShown() ? 1 : 0;
            }
            invalidate(true);
        }
    }

    public void show() {
        setShown(true);
    }

    public void hide() {
        setShown(false);
    }

    public void toggle() {
        setShown(!isShown());
    }

    protected class SlideAnimation implements Animation {

        private boolean playing;

        public void play() {
            playing = true;
            if (getActivity() != null) {
                getActivity().addAnimation(this);
            }
        }

        @Override
        public boolean isPlaying() {
            return playing;
        }

        @Override
        public Activity getSource() {
            return getActivity();
        }

        @Override
        public void handle(float seconds) {
            float value = isShown() ? 1 : 0;
            if (slideAnimationDuration > 0 && !isDisabled()) {
                if (lerpPos > value) {
                    lerpPos = Math.max(value, lerpPos - seconds / slideAnimationDuration);
                } else if (lerpPos < value) {
                    lerpPos = Math.min(value, lerpPos + seconds / slideAnimationDuration);
                }
                if (lerpPos == value) {
                    playing = false;
                }
            } else {
                lerpPos = value;
                playing = false;
            }
            invalidate(true);
        }
    }
}
