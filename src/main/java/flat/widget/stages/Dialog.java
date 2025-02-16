package flat.widget.stages;

import flat.animations.Interpolation;
import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.DragEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.Group;
import flat.widget.Parent;
import flat.widget.Stage;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.window.Activity;
import flat.window.Application;

public class Dialog extends Stage {

    private VerticalAlign verticalAlign = VerticalAlign.MIDDLE;
    private HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;
    private float showupTransitionDuration = 0;

    private float targetX, targetY;
    private float dragX, dragY;
    private boolean show;
    private Controller controller;

    private final ShowupAnimation showupAnimation = new ShowupAnimation();

    public void build(String uxmlStream) {
        build(new ResourceStream(uxmlStream), null);
    }

    public void build(String uxmlStream, Controller controller) {
        build(new ResourceStream(uxmlStream), controller);
    }

    public void build(ResourceStream uxmlStream) {
        build(uxmlStream, null);
    }

    public void build(ResourceStream uxmlStream, Controller controller) {
        build(UXNode.parse(uxmlStream).instance(controller).build(getCurrentTheme()), controller);
    }

    public void build(Widget root) {
        build(root, null);
    }

    public void build(Widget root, Controller controller) {
        this.controller = controller;

        removeAll();
        if (root != null) {
            add(root);
        }
    }

    protected void setController(Controller controller) {
        this.controller = controller;
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
        setShowupTransitionDuration(attrs.getNumber("showup-transition-duration", info, getShowupTransitionDuration()));
    }

    @Override
    public void onMeasure() {
        performMeasureStack();
    }

    @Override
    public void onLayout(float width, float height) {
        performLayoutConstraints(width, height, verticalAlign, horizontalAlign);
        limitPosition();
        setLayoutPosition(targetX, targetY);
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);

        if (controller != null) {
            try {
                controller.onDraw(context);
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    @Override
    protected void onParentChange(Parent prev, Parent current) {
        super.onParentChange(prev, current);
        hide();
    }

    @Override
    protected void onGroupChange(Group prev, Group current) {
        super.onGroupChange(prev, current);
        hide();
    }

    @Override
    protected void onActivityChange(Activity prev, Activity current) {
        super.onActivityChange(prev, current);
        hide();
    }

    @Override
    public void firePointer(PointerEvent event) {
        super.firePointer(event);
        if (event.getType() == PointerEvent.PRESSED) {
            bringToFront();
        }
    }

    @Override
    public void fireDrag(DragEvent event) {
        super.fireDrag(event);
        if (!event.isConsumed() && event.getSource() == this) {
            if (contains(event.getX(), event.getY()) && event.getType() == DragEvent.STARTED) {
                if (!event.isCanceled()) {
                    event.accept(this);
                    dragX = event.getX();
                    dragY = event.getY();
                }
            } else if (event.getType() == DragEvent.OVER) {
                targetX += (event.getX() - dragX);
                targetY += (event.getY() - dragY);
                limitPosition();
                dragX = event.getX();
                dragY = event.getY();
                setLayoutPosition(targetX, targetY);
            }
        }
    }

    private void limitPosition() {
        if (getParent() == null) {
            targetX = Math.max(-getLayoutWidth() + getMarginRight() + getPaddingRight(), targetX);
            targetY = Math.max(-getLayoutHeight() + getMarginBottom() + getPaddingBottom(), targetY);
        } else {
            float avW = getParent().getWidth();
            float avH = getParent().getHeight();
            targetX = Math.max(-getLayoutWidth() + getMarginRight() + getPaddingRight()
                    , Math.min(avW - getMarginLeft() - getPaddingLeft(), targetX));
            targetY = Math.max(-getLayoutHeight() + getMarginBottom() + getPaddingBottom()
                    , Math.min(avH - getMarginTop() - getPaddingTop(), targetY));
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

    public float getShowupTransitionDuration() {
        return showupTransitionDuration;
    }

    public void setShowupTransitionDuration(float showupTransitionDuration) {
        if (this.showupTransitionDuration != showupTransitionDuration) {
            this.showupTransitionDuration = showupTransitionDuration;

            showupAnimation.stop();
        }
    }

    @Override
    public boolean isShown() {
        return show;
    }

    public void show(Activity activity) {
        show(activity, activity.getWidth() * 0.5f, activity.getHeight() * 0.5f);
    }

    public void show(Activity activity, float x, float y) {
        if (!isShown()) {
            setToShow(activity);
            show = true;
            onShow(activity, x, y);
            activity.addPointerFilter(this);
        }
    }

    @Override
    public void hide() {
        if (isShown()) {
            show = false;
            if (getParent() != null) {
                getParent().remove(this);
            }

            if (controller != null) {
                try {
                    controller.onHide();
                } catch (Exception e) {
                    Application.handleException(e);
                }
            }
        }
    }

    public void bringToFront() {
        if (isShown()) {
            setToShow(getActivity());
            getActivity().addPointerFilter(this);
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

        if (showupTransitionDuration > 0) {
            showupAnimation.setDuration(showupTransitionDuration);
            showupAnimation.play(act);
        }

        if (controller != null) {
            try {
                controller.setActivity(getActivity());
                controller.onShow();
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    private class ShowupAnimation extends NormalizedAnimation {
        private float scaleX, scaleY, centerX, centerY;
        private boolean followX, followY, followCX, followCY;

        public ShowupAnimation() {
            super(Interpolation.circleOut);
        }

        @Override
        public Activity getSource() {
            return getActivity();
        }

        @Override
        protected void compute(float t) {
            setScaleX(scaleX * 0.5f + (scaleX * 0.5f * t));
            setScaleY(scaleY * 0.5f + (scaleY * 0.5f * t));
            invalidate(false);
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
        }
    }
}
