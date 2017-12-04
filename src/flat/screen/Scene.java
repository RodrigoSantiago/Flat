package flat.screen;

import flat.backend.WL;
import flat.events.PointerEvent;
import flat.graphics.Context;
import flat.widget.Widget;

public class Scene extends Widget {

    Activity activity;

    private float xx, yy;

    Scene(Activity activity) {
        this.activity = activity;
        setPointerListener(event -> {
            if (event.getType() == PointerEvent.MOVED) {
                xx = (float) event.getScreenX();
                yy = (float) event.getScreenY();
                return true;
            }

            return false;
        });
        WL.SetVsync(1);
        setBackgroundRadius(5000);
    }

    @Override
    public void onLayout(float width, float height) {
        setPrefWidth(MATH_PARENT);
        setPrefHeight(MATH_PARENT);
        setMinWidth(MATH_PARENT);
        setMinHeight(MATH_PARENT);
        setMaxWidth(MATH_PARENT);
        setMaxHeight(MATH_PARENT);

        super.onLayout(width, height);
    }

    @Override
    public void onDraw(Context context) {
        boolean inside = contains(xx, yy);

        context.clear();
        context.setColor(inside ? 0xFFFFFFFF : 0xFF0000FF);
        context.setTransform2D(getTransformView());
        context.drawRoundRect((-getCenterX() * getWidth()), (-getCenterY() * getHeight()), getWidth(), getHeight(), getBackgroundRadius(), true);
        context.setTransform2D(null);

        invalidate(false);
    }

    @Override
    public void invalidate(boolean layout) {
        activity.invalidate(layout);
    }
}
