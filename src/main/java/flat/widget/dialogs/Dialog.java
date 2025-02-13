package flat.widget.dialogs;

import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.math.shapes.Circle;
import flat.math.shapes.Shape;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXNode;
import flat.uxml.UXTheme;
import flat.widget.Group;
import flat.widget.Scene;
import flat.window.Activity;

public class Dialog extends Group {

    private float showupTransitionDuration = 0;

    private float targetX, targetY;
    private boolean show;

    private final Circle circle = new Circle();
    private final ShowupAnimation showupAnimation = new ShowupAnimation();

    public void build(ResourceStream stream, UXTheme theme, Controller controller) {
        var builder = UXNode.parse(stream).instance(controller);
        Scene scene = builder.build(theme);
        removeAll();
        add(scene);
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
        setShowupTransitionDuration(attrs.getNumber("showup-transition-duration", info, getShowupTransitionDuration()));
    }

    @Override
    public void onLayout(float width, float height) {
        super.onLayout(width, height);
        setPosition(targetX, targetY);
    }

    @Override
    public void onActivityChange(Activity prev, Activity current) {
        super.onActivityChange(prev, current);
        if (isShown()) {
            hide();
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);
        if (!pointerEvent.isConsumed() && pointerEvent.getType() == PointerEvent.FILTER) {
            if (!pointerEvent.getSource().isChildOf(this)) {
                hide();
            }
        }
    }

    @Override
    public void fireResize() {
        hide();
    }

    @Override
    public void onDraw(SmartContext context) {
        if (showupAnimation.isPlaying()) {
            float max = Math.max(getLayoutWidth(), getLayoutHeight());
            circle.radius = max * showupAnimation.getInterpolatedPosition();
            Shape oldClip = context.intersectClip(circle);

            drawBackground(context);
            drawRipple(context);
            drawChildren(context);

            context.setTransform2D(null);
            context.setClip(oldClip);
        } else {
            super.onDraw(context);
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

    public boolean isShown() {
        return show;
    }

    public void show(Activity activity, float x, float y) {
        Scene scene = activity.getScene();
        if (scene == null) return;

        if (!isShown()) {
            scene.add(this);
            show = true;
            onShow(activity, x, y);
            getActivity().addPointerFilter(this);
            getActivity().addResizeFilter(this);
        }
    }

    public void hide() {
        if (isShown()) {
            getActivity().removePointerFilter(this);
            getActivity().removeResizeFilter(this);
            show = false;
            remove(this);
        }
    }

    private void onShow(Activity act, float x, float y) {
        onMeasure();
        float mW = Math.min(Math.min(getMeasureWidth(), getLayoutMaxWidth()), act.getWidth());
        float mH = Math.min(Math.min(getMeasureHeight(), getLayoutMaxHeight()), act.getHeight());
        mW -= getMarginRight() + getMarginLeft();
        mH -= getMarginTop() + getMarginBottom();

        boolean reverseX = mW + x > act.getWidth();
        boolean reverseY = mH + y > act.getHeight();

        targetX = reverseX ? Math.max(0, x - mW) : x;
        targetY = reverseY ? Math.max(0, y - mH) : y;
        circle.x = x - targetX;
        circle.y = y - targetY;

        if (showupTransitionDuration > 0) {
            showupAnimation.setDuration(showupTransitionDuration);
            showupAnimation.play(act);
        }
    }

    private class ShowupAnimation extends NormalizedAnimation {
        @Override
        public Activity getSource() {
            return getActivity();
        }

        @Override
        protected void compute(float t) {
            invalidate(false);
        }
    }
}
