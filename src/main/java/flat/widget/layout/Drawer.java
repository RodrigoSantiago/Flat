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
import flat.widget.enums.*;
import flat.window.Activity;

public class Drawer extends Parent {

    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;
    private Position slidePosition = Position.LEFT;
    private float slideAnimationDuration = 0;

    private boolean autoClose;
    private boolean blockEvents;
    private int blockColor = Color.transparent;

    private OverlayMode overlayMode = OverlayMode.FLOATING;

    protected final SlideAnimation slideAnimation = new SlideAnimation();
    private boolean shown;
    private float lerpPos;

    private Widget frontContent;
    private Widget backContent;

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        for (var child : children) {
            if (child.getAttributeBool("front-content", false)) {
                setFrontContent(child.getWidget());
            } else if (child.getAttributeBool("back-content", false)) {
                setBackContent(child.getWidget());
            }
        }
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();
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
        setOverlayMode(attrs.getConstant("overlay-mode", info, getOverlayMode()));
        setBlockEvents(attrs.getBool("block-events", info, isBlockEvents()));
        setAutoClose(attrs.getBool("auto-close", info, isAutoClose()));
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth = 0;
        float mHeight = 0;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        boolean overlap = getOverlayMode() == OverlayMode.OVERLAPPING;
        boolean measureFront = frontContent != null;
        boolean measureBack = backContent != null;

        float mFrontWidth = 0;
        float mFrontHeight = 0;

        if (measureFront) {
            if (overlap && !isShown() && !isAnimating()) {
                measureFront = false;
            } else {
                frontContent.onMeasure();
                mFrontWidth = getDefWidth(frontContent);
                mFrontHeight = getDefHeight(frontContent);
                if (getOverlayMode() == OverlayMode.SPLIT) {
                    if (isSlideHorizontal()) {
                        if (mFrontWidth != MATCH_PARENT) mFrontWidth = lerp(0, mFrontWidth);
                    } else {
                        if (mFrontHeight != MATCH_PARENT) mFrontHeight = lerp(0, mFrontHeight);
                    }
                }
            }
        }

        if (measureBack) {
            if (overlap && isShown() && !isAnimating()) {
                measureBack = false;
            } else {
                backContent.onMeasure();
            }
        }

        if (wrapWidth) {
            if (measureFront && measureBack) {
                if (getOverlayMode() == OverlayMode.SPLIT && isSlideHorizontal()) {
                    mWidth = mFrontWidth + getDefWidth(backContent);
                } else {
                    mWidth = Math.max(mFrontWidth, getDefWidth(backContent));
                }
            } else if (measureFront) {
                mWidth = mFrontWidth;
            } else if (measureBack) {
                mWidth = getDefWidth(backContent);
            }
            mWidth = Math.max(mWidth + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }

        if (wrapHeight) {
            if (measureFront && measureBack) {
                if (getOverlayMode() == OverlayMode.SPLIT && isSlideVertical()) {
                    mHeight = mFrontHeight + getDefHeight(backContent);
                } else {
                    mHeight = Math.max(mFrontHeight, getDefHeight(backContent));
                }
            } else if (measureFront) {
                mHeight = mFrontHeight;
            } else if (measureBack) {
                mHeight = getDefHeight(backContent);
            }
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

        boolean layoutFront = frontContent != null;
        boolean layoutBack = backContent != null;
        float mFrontWidth = 0;
        float mFrontHeight = 0;

        if (layoutFront && layoutBack) {
            if (getOverlayMode() == OverlayMode.SPLIT) {
                if (isSlideHorizontal()) {
                    layoutHorizontal();
                } else {
                    layoutVertical();
                }
            } else if (getOverlayMode() == OverlayMode.OVERLAPPING) {
                if (!isShown() || isAnimating()) {
                    layoutBack();
                }
                if (isShown() || isAnimating()) {
                    layoutFront();
                }
            } else {
                layoutBack();
                layoutFront();
            }
        } else if (layoutFront) {
            layoutFront();
        } else if (layoutBack) {
            layoutBack();
        }
        fireLayout();
    }

    private void layoutBack() {
        float lx = getInX();
        float ly = getInY();
        float lWidth = getInWidth();
        float lHeight = getInHeight();

        performSingleLayoutConstraints(lWidth, lHeight, lx, ly, backContent, getVerticalAlign(), getHorizontalAlign());
    }

    private void layoutFront() {
        float lx = getInX();
        float ly = getInY();
        float lWidth = getInWidth();
        float lHeight = getInHeight();

        float fw = Math.min(getDefWidth(frontContent), lWidth);
        float fh = Math.min(getDefHeight(frontContent), lHeight);

        frontContent.onLayout(fw, fh);
        if (getSlidePosition() == Position.LEFT) {
            float fyPos = off(ly, ly + lHeight, frontContent.getLayoutHeight(), verticalAlign);
            frontContent.setLayoutPosition(lx + lerp(-fw, 0), fyPos);
        } else if (getSlidePosition() == Position.RIGHT) {
            float fyPos = off(ly, ly + lHeight, frontContent.getLayoutHeight(), verticalAlign);
            frontContent.setLayoutPosition(lx + lWidth - lerp(0, fw), fyPos);
        } else if (getSlidePosition() == Position.TOP) {
            float fxPos = off(lx, lx + lWidth, frontContent.getLayoutWidth(), horizontalAlign);
            frontContent.setLayoutPosition(fxPos, ly + lerp(-fh, 0));
        } else {
            float fxPos = off(lx, lx + lWidth, frontContent.getLayoutWidth(), horizontalAlign);
            frontContent.setLayoutPosition(fxPos, ly + lHeight - lerp(0, fh));
        }
    }

    private void layoutHorizontal() {
        float lx = getInX();
        float ly = getInY();
        float lWidth = getInWidth();
        float lHeight = getInHeight();

        float bmin = backContent.getLayoutMinWidth();
        float bpre = backContent.getMeasureWidth();
        float bdef = bpre == MATCH_PARENT ? 0 : getDefWidth(backContent);
        float bmax = backContent.getLayoutMaxWidth();
        float bweight = backContent.getWeight();

        float fmin = lerp(0, frontContent.getLayoutMinWidth());
        float fpre = lerp(0, frontContent.getMeasureWidth());
        float fdef = fpre == MATCH_PARENT ? 0 : lerp(0, getDefWidth(frontContent));
        float fmax = lerp(0, frontContent.getLayoutMaxWidth() == MATCH_PARENT ? lWidth : frontContent.getLayoutMaxWidth());
        float fweight = lerp(0, frontContent.getWeight());

        float totalMinimum = bmin + fmin;
        float totalDefined = bdef + fdef;
        float minSpace = Math.min(totalMinimum, lWidth);
        float defSpace = Math.min(totalDefined - totalMinimum, lWidth - minSpace);
        float totalSpaceLeft = Math.max(lWidth - minSpace - defSpace, 0);
        float totalWeight = bweight + fweight;

        float bw = totalMinimum == 0 ? 0 : bmin / totalMinimum * minSpace;
        float fw = totalMinimum == 0 ? 0 : fmin / totalMinimum * minSpace;
        bw += totalDefined == 0 ? 0 : bdef / totalDefined * defSpace;
        fw += totalDefined == 0 ? 0 : fdef / totalDefined * defSpace;
        if (bpre == MATCH_PARENT && fpre == MATCH_PARENT) {
            float db = totalWeight == 0 ? totalSpaceLeft * 0.5f : totalSpaceLeft / totalWeight * bweight;
            float df = totalWeight == 0 ? totalSpaceLeft * 0.5f : totalSpaceLeft / totalWeight * fweight;
            if (bw + db > bmax) {
                df += (bw + db) - bmax;
            }
            if (fw + df > fmax) {
                db += (fw + df) - fmax;
            }
            bw = Math.min(bmax, bw + db);
            fw = Math.min(fmax, fw + df);
        } else if (bpre == MATCH_PARENT) {
            bw = Math.min(bw + totalSpaceLeft, bmax);
        } else if (fpre == MATCH_PARENT) {
            fw = Math.min(fw + totalSpaceLeft, fmax);
        }
        float bh = Math.min(getDefHeight(backContent), lHeight);
        float fh = Math.min(getDefHeight(frontContent), lHeight);
        float targetWidth = fw;
        /*float targetWidth = lerp(0, fw);
        if (bw < getDefWidth(backContent)) {
            bw = Math.min(bmax, bw + (fw - targetWidth));
        }*/

        backContent.onLayout(bw, bh);
        frontContent.onLayout(targetWidth, fh);

        if (getSlidePosition() == Position.LEFT) {
            float bxPos = off(lx + targetWidth, lx + lWidth, backContent.getLayoutWidth(), horizontalAlign);
            float byPos = off(ly, ly + lHeight, backContent.getLayoutHeight(), verticalAlign);
            backContent.setLayoutPosition(bxPos, byPos);

            float fyPos = off(ly, ly + lHeight, frontContent.getLayoutHeight(), verticalAlign);
            frontContent.setLayoutPosition(lx, fyPos);
        } else {
            float bxPos = off(lx, lx + lWidth - targetWidth, backContent.getLayoutWidth(), horizontalAlign);
            float byPos = off(ly, ly + lHeight, backContent.getLayoutHeight(), verticalAlign);
            backContent.setLayoutPosition(bxPos, byPos);

            float fyPos = off(ly, ly + lHeight, frontContent.getLayoutHeight(), verticalAlign);
            frontContent.setLayoutPosition(lx + lWidth - targetWidth, fyPos);
        }
    }

    private void layoutVertical() {
        float lx = getInX();
        float ly = getInY();
        float lWidth = getInWidth();
        float lHeight = getInHeight();

        float bmin = backContent.getLayoutMinHeight();
        float bpre = backContent.getMeasureHeight();
        float bdef = bpre == MATCH_PARENT ? 0 : getDefHeight(backContent);
        float bmax = backContent.getLayoutMaxHeight();
        float fmin = frontContent.getLayoutMinHeight();
        float fpre = frontContent.getMeasureHeight();
        float fdef = fpre == MATCH_PARENT ? 0 : getDefHeight(frontContent);
        float fmax = frontContent.getLayoutMaxHeight();
        float bweight = backContent.getWeight();
        float fweight = backContent.getWeight();

        float totalMinimum = bmin + fmin;
        float totalDefined = bdef + fdef;
        float minSpace = Math.min(totalMinimum, lHeight);
        float defSpace = Math.min(totalDefined - totalMinimum, lHeight - minSpace);
        float totalSpaceLeft = Math.max(lHeight - minSpace - defSpace, 0);
        float totalWeight = bweight + fweight;

        float bh = totalMinimum == 0 ? 0 : bmin / totalMinimum * minSpace;
        float fh = totalMinimum == 0 ? 0 : fmin / totalMinimum * minSpace;
        bh += totalDefined == 0 ? 0 : bdef / totalDefined * defSpace;
        fh += totalDefined == 0 ? 0 : fdef / totalDefined * defSpace;
        if (bpre == MATCH_PARENT && fpre == MATCH_PARENT) {
            float db = totalWeight == 0 ? totalSpaceLeft * 0.5f : totalSpaceLeft / totalWeight * bweight;
            float df = totalWeight == 0 ? totalSpaceLeft * 0.5f : totalSpaceLeft / totalWeight * fweight;
            if (bh + db > bmax) {
                df += (bh + db) - bmax;
            }
            if (fh + df > fmax) {
                db += (fh + df) - fmax;
            }
            bh = Math.min(bmax, bh + db);
            fh = Math.min(fmax, fh + df);
        } else if (bpre == MATCH_PARENT) {
            bh = Math.min(bh + totalSpaceLeft, bmax);
        } else if (fpre == MATCH_PARENT) {
            fh = Math.min(fh + totalSpaceLeft, fmax);
        }
        float bw = Math.min(getDefWidth(backContent), lWidth);
        float fw = Math.min(getDefWidth(frontContent), lWidth);
        float targetHeight = lerp(0, fh);
        if (bh < getDefHeight(backContent)) {
            bh = Math.min(bmax, bh + (fh - targetHeight));
        }

        backContent.onLayout(bw, bh);
        frontContent.onLayout(fw, fh);

        if (getSlidePosition() == Position.TOP) {
            float bxPos = off(lx, lx + lWidth, backContent.getLayoutWidth(), horizontalAlign);
            float byPos = off(ly + targetHeight, ly + lHeight, backContent.getLayoutHeight(), verticalAlign);
            backContent.setLayoutPosition(bxPos, byPos);

            float fxPos = off(lx, lx + lWidth, frontContent.getLayoutWidth(), horizontalAlign);
            frontContent.setLayoutPosition(fxPos, ly + lerp(-fh, 0));
        } else {
            float bxPos = off(lx, lx + lWidth, backContent.getLayoutWidth(), horizontalAlign);
            float byPos = off(ly, ly + lHeight - targetHeight, backContent.getLayoutHeight(), verticalAlign);
            backContent.setLayoutPosition(bxPos, byPos);

            float fxPos = off(lx, lx + lWidth, frontContent.getLayoutWidth(), horizontalAlign);
            frontContent.setLayoutPosition(fxPos, ly + lHeight - lerp(0, fh));
        }
    }

    private float lerp(float hide, float show) {
        if (lerpPos <= 0) return hide;
        if (lerpPos >= 1) return show;
        float t = Interpolation.fade.apply(lerpPos);
        return hide * (1 - t) + show * t;
    }

    @Override
    public void onDraw(Graphics graphics) {
        if (discardDraw(graphics)) return;

        drawBackground(graphics);
        drawRipple(graphics);

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        if (backContent != null && backContent.getVisibility() == Visibility.VISIBLE) {
            if (getOverlayMode() != OverlayMode.OVERLAPPING || !isShown() || isAnimating()) {
                backContent.onDraw(graphics);
            }
        }
        if (frontContent != null && frontContent.getVisibility() == Visibility.VISIBLE) {
            if (isShown() || isAnimating()) {
                graphics.setTransform2D(getTransform());
                if (isBlockEvents() && Color.getAlpha(getBlockColor()) > 0) {
                    graphics.setColor(Color.multiplyColorAlpha(getBlockColor(), lerp(0, 1)));
                    graphics.drawRect(x, y, width, height, true);
                }
                if (isAnimating()) {
                    graphics.pushClip(getBackgroundShape());
                }
                frontContent.onDraw(graphics);
                if (isAnimating()) {
                    graphics.popClip();
                }
            }
        }
    }

    @Override
    protected boolean detachChild(Widget child) {
        if (child == backContent || child == frontContent) {
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
        if (isShown() && frontContent != null) {
            Widget found = frontContent.findByPosition(x, y, includeDisabled);
            if (found != null) return found;
        }
        if ((!isBlockEvents() || getOverlayMode() == OverlayMode.SPLIT || !isShown()) && backContent != null) {
            Widget found = backContent.findByPosition(x, y, includeDisabled);
            if (found != null) return found;
        }
        return isHandlePointerEnabled() ? this : null;
    }

    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        if (isShown()
                && getOverlayMode() != OverlayMode.SPLIT
                && isAutoClose() && !event.isConsumed() && event.getPointerID() == 1
                && event.getType() == PointerEvent.RELEASED && event.getSource() == this) {
            hide();
        }
    }

    public OverlayMode getOverlayMode() {
        return overlayMode;
    }

    public void setOverlayMode(OverlayMode overlayMode) {
        if (overlayMode == null) overlayMode = OverlayMode.FLOATING;

        if (this.overlayMode != overlayMode) {
            this.overlayMode = overlayMode;
            invalidate(true);
        }
    }

    public Widget getBackContent() {
        return backContent;
    }

    public void setBackContent(Widget backContent) {
        if (this.backContent != backContent) {
            Widget old = this.backContent;
            if (backContent != null) {
                add(backContent);
                if (backContent.getParent() == this) {
                    this.backContent = backContent;
                    if (old != null) {
                        remove(old);
                    }
                }
            } else {
                this.backContent = null;
                remove(old);
            }
        }
    }

    public Widget getFrontContent() {
        return frontContent;
    }

    public void setFrontContent(Widget frontContent) {
        if (this.frontContent != frontContent) {
            Widget old = this.frontContent;
            if (frontContent != null) {
                add(frontContent);
                if (frontContent.getParent() == this) {
                    this.frontContent = frontContent;
                    if (old != null) {
                        remove(old);
                    }
                }
            } else {
                this.frontContent = null;
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

    public boolean isShown() {
        return shown;
    }

    public boolean isAnimating() {
        return (shown && lerpPos < 1) || (!shown && lerpPos > 0);
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
