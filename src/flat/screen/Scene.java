package flat.screen;

import flat.events.PointerEvent;
import flat.graphics.context.Context;
import flat.widget.layout.Box;

public class Scene extends Box {

    Activity activity;
    Box b;
    public Scene(Activity activity) {
        this.activity = activity;
        b  = new Box();
        b.setBackgroundColor(-1);
        b.setPrefSize(100, 100);
        b.setShadowEffectEnabled(true);
        b.setBackgroundCorners(10, 10, 10, 10);
        setPointerListener(event -> {
            if (event.getType() == PointerEvent.DRAGGED) {
                if (event.getMouseButton() == 1) {
                    b.setElevation(b.getElevation() + 1);
                } else {
                    b.setElevation(b.getElevation() - 1);
                }
            }
            b.setTranslateX(event.getX());
            b.setTranslateY(event.getY());
            return false;
        });
        add(b);
    }

    @Override
    public void onLayout(float x, float y, float width, float height) {
        super.onLayout(0, 0, activity.getWidth(), activity.getHeight());
    }

    @Override
    public void onMeasure() {
        setPrefHeight(activity.getHeight());
        setPrefWidth(activity.getWidth());
        setMinHeight(activity.getHeight());
        setMinWidth(activity.getWidth());
        setMaxHeight(activity.getHeight());
        setMaxWidth(activity.getWidth());
        super.onMeasure();
    }

    @Override
    public void onDraw(Context context) {
        context.setView(0, 0, (int) activity.getWidth(), (int) activity.getHeight());
        context.clear(0xDDDDDDFF);
        super.onDraw(context);
        context.setTransform(b.getTransformView().translate(0, b.getElevation()));
        context.drawRoundRectShadow(-1, -1, 100, 100, 15, 15, 0.5F);
    }

    @Override
    public void invalidate(boolean layout) {
        activity.invalidate(layout);
    }
}
