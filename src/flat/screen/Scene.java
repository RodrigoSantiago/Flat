package flat.screen;

import flat.graphics.*;
import flat.widget.layout.Box;
import flat.widget.text.Label;

public class Scene extends Box {

    Activity activity;
    public Scene(Activity activity) {
        this.activity = activity;
        Label label = new Label();
        label.setText("Eaaeee !!!!!");
        add(label);
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
    }

    @Override
    public void invalidate(boolean layout) {
        activity.invalidate(layout);
    }
}
