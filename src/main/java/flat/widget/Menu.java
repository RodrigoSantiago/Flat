package flat.widget;

import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.math.shapes.Circle;
import flat.math.shapes.Shape;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXChildren;
import flat.widget.enums.Direction;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.window.Activity;

import java.util.ArrayList;
import java.util.List;

public class Menu extends Group {

    private HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;
    private float showupTransitionDuration = 0;

    private float targetX, targetY;
    private final Circle circle = new Circle();
    private final ShowupAnimation showupAnimation = new ShowupAnimation();

    private final ArrayList<Widget> orderedList = new ArrayList<>();
    private float[] tempSize;

    private Menu parentMenu;
    private Menu childMenu;
    private boolean show;

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        Widget widget;
        while ((widget = children.next()) != null ) {
            add(widget);
        }
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
        setShowupTransitionDuration(attrs.getNumber("showup-transition-duration", info, getShowupTransitionDuration()));
    }

    @Override
    public void onMeasure() {
        performMeasureVertical();
    }

    @Override
    public void onLayout(float width, float height) {
        performLayoutVertical(width, height, orderedList, VerticalAlign.TOP, horizontalAlign, Direction.VERTICAL);
        setPosition(targetX, targetY);
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
    protected boolean attachChild(Widget child) {
        if (super.attachChild(child)) {
            orderedList.add(child);
            return true;
        }
        return false;
    }

    @Override
    protected boolean detachChild(Widget child) {
        if (super.detachChild(child)) {
            orderedList.remove(child);
            return true;
        }
        return false;
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
            context.setTransform2D(getTransform());
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

    public boolean isShown() {
        return show;
    }

    private boolean isChildMenuOf(Menu menu) {
        if (menu == this) return true;

        if (parentMenu != null) {
            if (parentMenu == menu) {
                return true;
            } else {
                return parentMenu.isChildOf(menu);
            }
        } else {
            return false;
        }
    }

    protected void showSubMenu(Menu menu, float x, float y) {
        if (childMenu != null) {
            hideSubMenu();
        }
        if (attachAndAddChild(menu)) {
            childMenu = menu;
            childMenu.parentMenu = this;
            childMenu.show = true;
            childMenu.getActivity().addPointerFilter(childMenu);
            childMenu.getActivity().addResizeFilter(childMenu);
            childMenu.onShow(childMenu.getActivity(), x, y);
        }
    }

    protected void hideSubMenu() {
        if (childMenu != null) {
            childMenu.getActivity().removePointerFilter(childMenu);
            childMenu.getActivity().removeResizeFilter(childMenu);
            childMenu.parentMenu = null;
            childMenu.show = false;
            childMenu.hideSubMenu();
            remove(childMenu);
            childMenu = null;
        }
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

    public void show(Menu menu, float x, float y) {
        if (menu.isShown() && !menu.isChildMenuOf(this)) {
            if (!isShown()) {
                menu.showSubMenu(this, x, y);
            }
        }
    }

    public void hide() {
        if (isShown()) {
            if (parentMenu != null) {
                if (parentMenu.childMenu == this) {
                    parentMenu.hideSubMenu();
                }
                parentMenu = null;

            } else if (getParent() instanceof Scene scene) {
                getActivity().removePointerFilter(this);
                getActivity().removeResizeFilter(this);
                show = false;
                hideSubMenu();
                scene.remove(this);

            }
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
