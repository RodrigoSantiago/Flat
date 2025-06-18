package flat.widget.stages;

import flat.animations.Interpolation;
import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.DrawEvent;
import flat.events.KeyEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.Stage;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.widget.enums.Visibility;
import flat.window.Activity;
import flat.window.Application;

public class Dialog extends Stage {

    private VerticalAlign verticalAlign = VerticalAlign.MIDDLE;
    private HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;
    private float showTransitionDelay = 0;
    private float showTransitionDuration = 0;
    private float hideTransitionDuration = 0;
    private boolean blockEvents;
    private int blockColor = Color.transparent;

    private float targetX, targetY;
    private boolean show;
    private Controller controller;

    private final ShowAnimation showupAnimation = new ShowAnimation();
    private final HideAnimation hideAnimation = new HideAnimation();

    public Dialog build(String uxmlStream) {
        return build(new ResourceStream(uxmlStream), null);
    }

    public Dialog build(String uxmlStream, Controller controller) {
        return build(new ResourceStream(uxmlStream), controller);
    }

    public Dialog build(ResourceStream uxmlStream) {
        return build(uxmlStream, null);
    }

    public Dialog build(ResourceStream uxmlStream, Controller controller) {
        removeAll();
        UXNode.parse(uxmlStream).instance(controller).build(this::add);
        setController(controller);
        return this;
    }

    public Dialog build(Widget root) {
        return build(root, null);
    }

    public Dialog build(Widget root, Controller controller) {
        removeAll();
        if (root != null) {
            add(root);
        }
        setController(controller);
        return this;
    }
    
    public void setController(Controller controller) {
        if (this.controller != controller) {
            Controller old = this.controller;
            this.controller = controller;
            if (old != null) {
                old.setActivity(null);
            }
            if (this.controller != null) {
                this.controller.setActivity(getActivity());
            }
        }
    }
    
    public Controller getController() {
        return controller;
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
        setVerticalAlign(attrs.getConstant("vertical-align", info, getVerticalAlign()));
        setShowTransitionDuration(attrs.getNumber("show-transition-duration", info, getShowTransitionDuration()));
        setShowTransitionDelay(attrs.getNumber("show-transition-delay", info, getShowTransitionDelay()));
        setHideTransitionDuration(attrs.getNumber("hide-transition-duration", info, getHideTransitionDuration()));
        setBlockColor(attrs.getColor("block-color", info, getBlockColor()));
    }

    @Override
    public void onMeasure() {
        performMeasureStack();
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);
        performLayoutConstraints(getInWidth(), getInHeight(), getInX(), getInY(), verticalAlign, horizontalAlign);
        limitPosition();
        setLayoutPosition(targetX, targetY);
        fireLayout();
    }

    @Override
    public void onDraw(Graphics graphics) {
        if (getParent() != null && isBlockEvents() && Color.getAlpha(getBlockColor()) > 0) {
            float a;
            if (showupAnimation.isPlaying()) {
                float diff = getShowTransitionDelay() / (getShowTransitionDelay() + getShowTransitionDuration());
                a = Interpolation.circleOut.apply((showupAnimation.getPosition() - diff) / (1 - diff));
            } else if (hideAnimation.isPlaying()) {
                a = 1 - hideAnimation.getInterpolatedPosition();
            } else {
                a = 1;
            }
            if (a > 0) {
                graphics.setTransform2D(getParent().getTransform());
                graphics.setColor(Color.multiplyColorAlpha(getBlockColor(), a));
                graphics.drawRect(getParent().getInX(), getParent().getInY(),
                        getParent().getInWidth(), getParent().getInHeight(), true);
            }
        }

        if (discardDraw(graphics)) return;

        drawBackground(graphics);
        drawRipple(graphics);
        drawChildren(graphics);

        if (controller != null && controller.isListening()) {
            try {
                controller.onDraw(new DrawEvent(this, graphics));
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        if (event.getType() == PointerEvent.PRESSED) {
            bringToFront();
        }
    }

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if (!isCurrentHandleEventsEnabled()
                || getVisibility() == Visibility.GONE
                || (!includeDisabled && !isEnabled())
                || (!isBlockEvents() && !contains(x, y))) {
            return null;
        }
        for (Widget child : getChildrenIterableReverse()) {
            Widget found = child.findByPosition(x, y, includeDisabled);
            if (found != null) return found;
        }
        return isHandlePointerEnabled() ? this : null;
    }

    private void limitPosition() {
        if (getParent() == null) {
            targetX = Math.max(-getLayoutWidth() + getMarginRight() + getPaddingRight(), targetX);
            targetY = Math.max(-getLayoutHeight() + getMarginBottom() + getPaddingBottom(), targetY);
        } else {
            float avW = getParent().getWidth();
            float avH = getParent().getHeight();
            targetX = Math.max(-getLayoutWidth() / 2f + getMarginRight() + getPaddingRight()
                    , Math.min(avW - getMarginLeft() - getPaddingLeft() - getLayoutWidth() / 2f, targetX));
            targetY = Math.max(-getLayoutHeight() / 2f + getMarginBottom() + getPaddingBottom()
                    , Math.min(avH - getMarginTop() - getPaddingTop() - getLayoutHeight() / 2f, targetY));
        }
    }

    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(VerticalAlign verticalAlign) {
        if (verticalAlign == null) verticalAlign = VerticalAlign.MIDDLE;

        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(true);
        }
    }

    public HorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
        if (horizontalAlign == null) horizontalAlign = HorizontalAlign.CENTER;

        if (this.horizontalAlign != horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            invalidate(true);
        }
    }

    public float getShowTransitionDuration() {
        return showTransitionDuration;
    }

    public void setShowTransitionDuration(float showTransitionDuration) {
        if (this.showTransitionDuration != showTransitionDuration) {
            this.showTransitionDuration = showTransitionDuration;
        }
    }

    public float getShowTransitionDelay() {
        return showTransitionDelay;
    }

    public void setShowTransitionDelay(float showTransitionDelay) {
        if (this.showTransitionDelay != showTransitionDelay) {
            this.showTransitionDelay = showTransitionDelay;
        }
    }

    public float getHideTransitionDuration() {
        return hideTransitionDuration;
    }

    public void setHideTransitionDuration(float hideTransitionDuration) {
        if (this.hideTransitionDuration != hideTransitionDuration) {
            this.hideTransitionDuration = hideTransitionDuration;
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

    @Override
    public boolean isShown() {
        return getActivity() != null && getActivity().getScene() == getParent();
    }

    public void show(Activity activity) {
        show(activity, activity.getWidth() * 0.5f, activity.getHeight() * 0.5f);
    }

    public void show(Activity activity, float x, float y) {
        setToShow(activity);
        activity.addPointerFilter(this);
        onShow(activity, x, y);
        if (controller != null) {
            try {
                controller.setRoot(this);
                controller.setActivity(getActivity());
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    @Override
    public void hide() {
        setToHide();
        if (controller != null) {
            try {
                controller.setActivity(null);
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    public void smoothHide() {
        if (isShown() && getActivity() != null && hideTransitionDuration > 0) {
            setHandleEventsEnabled(false);
            showupAnimation.stop();
            hideAnimation.setDelta(1);
            hideAnimation.setDuration(hideTransitionDuration);
            hideAnimation.play(getActivity());
        } else {
            hide();
        }
    }

    public void bringToFront() {
        if (isShown()) {
            setToShow(getActivity());
            getActivity().addPointerFilter(this);
        }
    }

    public float getMoveCenterX() {
        return getLayoutX() + getOutWidth() / 2f + getOutX();
    }

    public float getMoveCenterY() {
        return getLayoutY() + getOutHeight() / 2f + getOutY();
    }

    public void move(float x, float y) {
        moveTo(getMoveCenterX() + x, getMoveCenterY() + y);
    }

    public void moveTo(float x, float y) {
        Activity act = getActivity();
        if (act != null) {
            onMeasure();
            float mW = Math.min(Math.min(getMeasureWidth(), getLayoutMaxWidth()), act.getWidth());
            float mH = Math.min(Math.min(getMeasureHeight(), getLayoutMaxHeight()), act.getHeight());
            onLayout(mW, mH);

            targetX = x - getOutWidth() / 2f - getOutX();
            targetY = y - getOutHeight() / 2f - getOutY();
            invalidate(true);
        }
    }

    private void onShow(Activity act, float x, float y) {
        refreshStyle();
        onMeasure();
        float mW = Math.min(Math.min(getMeasureWidth(), getLayoutMaxWidth()), act.getWidth());
        float mH = Math.min(Math.min(getMeasureHeight(), getLayoutMaxHeight()), act.getHeight());
        onLayout(mW, mH);

        targetX = x - getWidth() / 2f - getMarginLeft();
        targetY = y - getHeight() / 2f - getMarginTop();

        if (showTransitionDuration + showTransitionDelay > 0) {
            showupAnimation.setDelta(1);
            showupAnimation.setDuration(showTransitionDuration + showTransitionDelay);
            showupAnimation.play(act);
        }
    }

    @Override
    public void key(KeyEvent event) {
        super.key(event);
        if (controller != null && controller.isListening()) {
            try {
                if (event.getType() == KeyEvent.FILTER) {
                    controller.onKeyFilter(event);
                } else {
                    controller.onKey(event);
                }
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    private class ShowAnimation extends NormalizedAnimation {
        private float scaleX, scaleY, centerX, centerY;
        private boolean followX, followY, followCX, followCY, followVs;

        public ShowAnimation() {
            super(Interpolation.circleOut);
        }

        @Override
        public Activity getSource() {
            return getActivity();
        }

        @Override
        protected void compute(float t) {
            float diff = getShowTransitionDelay() / (getShowTransitionDelay() + getShowTransitionDuration());
            float p = (getPosition() - diff) / (1 - diff);
            if (p <= 0) {
                setVisibility(Visibility.INVISIBLE);
            } else {
                setVisibility(Visibility.VISIBLE);
                setScaleX(scaleX * 0.5f + (scaleX * 0.5f * getInterpolation().apply(p)));
                setScaleY(scaleY * 0.5f + (scaleY * 0.5f * getInterpolation().apply(p)));
            }
            if (getParent() != null) {
                getParent().invalidate(false);
            }
        }

        @Override
        protected void onStart() {
            scaleX = getScaleX();
            scaleY = getScaleY();
            centerX = getCenterX();
            centerY = getCenterY();
            followVs = isFollowStyleProperty("visibility");
            followX = isFollowStyleProperty("scale-x");
            followY = isFollowStyleProperty("scale-y");
            followCX = isFollowStyleProperty("center-x");
            followCY = isFollowStyleProperty("center-y");
            setFollowStyleProperty("visibility", false);
            setFollowStyleProperty("scale-x", false);
            setFollowStyleProperty("scale-y", false);
            setFollowStyleProperty("center-x", false);
            setFollowStyleProperty("center-y", false);
            setCenterX(0.5f);
            setCenterY(0.5f);
        }

        @Override
        protected void onStop() {
            setScaleX(scaleX);
            setScaleY(scaleY);
            setCenterX(centerX);
            setCenterY(centerY);
            setFollowStyleProperty("visibility", followVs);
            setFollowStyleProperty("scale-x", followX);
            setFollowStyleProperty("scale-y", followY);
            setFollowStyleProperty("scale-x", followCX);
            setFollowStyleProperty("scale-y", followCY);
        }
    }

    private class HideAnimation extends NormalizedAnimation {
        private float scaleX, scaleY, centerX, centerY;
        private boolean followX, followY, followCX, followCY;

        public HideAnimation() {
            super(Interpolation.circleIn);
        }

        @Override
        public Activity getSource() {
            return getActivity();
        }

        @Override
        protected void compute(float t) {
            setScaleX(scaleX * 0.5f + (scaleX * 0.5f * (1 - t)));
            setScaleY(scaleY * 0.5f + (scaleY * 0.5f * (1 - t)));
            if (getParent() != null) {
                getParent().invalidate(false);
            }
        }

        @Override
        protected void onStart() {
            scaleX = getScaleX();
            scaleY = getScaleY();
            centerX = getCenterX();
            centerY = getCenterY();
            followX = isFollowStyleProperty("scale-x");
            followY = isFollowStyleProperty("scale-y");
            followCX = isFollowStyleProperty("center-x");
            followCY = isFollowStyleProperty("center-y");
            setFollowStyleProperty("scale-x", false);
            setFollowStyleProperty("scale-y", false);
            setFollowStyleProperty("center-x", false);
            setFollowStyleProperty("center-y", false);
            setCenterX(0.5f);
            setCenterY(0.5f);
        }

        @Override
        protected void onStop() {
            setScaleX(scaleX);
            setScaleY(scaleY);
            setCenterX(centerX);
            setCenterY(centerY);
            setFollowStyleProperty("scale-x", followX);
            setFollowStyleProperty("scale-y", followY);
            setFollowStyleProperty("scale-x", followCX);
            setFollowStyleProperty("scale-y", followCY);
            hide();
        }
    }
}
