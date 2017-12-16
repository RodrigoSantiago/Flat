package flat.widget;

import flat.events.PointerEvent;
import flat.graphics.context.Context;
import flat.screen.Activity;
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
        context.setViewPort(0, 0, (int) activity.getWidth(), (int) activity.getHeight());
        context.setClearColor(0xDDDDDDFF);
        context.clear(true, true, true);
        super.onDraw(context);
    }

    @Override
    public void invalidate(boolean layout) {
        activity.invalidate(layout);
    }
}
