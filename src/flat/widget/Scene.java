package flat.widget;

import flat.graphics.SmartContext;
import flat.application.Activity;
import flat.uxml.Controller;
import flat.uxml.UXAttributes;
import flat.widget.layout.Box;

public class Scene extends Box {

    private Activity activity;

    public Scene(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void applyAttributes(Controller controller, UXAttributes attributes) {
        super.applyAttributes(controller, attributes);
        setPrefWidth(attributes.asSize("width", MATCH_PARENT));
        setPrefHeight(attributes.asSize("height", MATCH_PARENT));
        setMaxWidth(attributes.asSize("maxWidth", MATCH_PARENT));
        setMaxHeight(attributes.asSize("maxHeight", MATCH_PARENT));
        setMinWidth(attributes.asSize("minWidth", MATCH_PARENT));
        setMinHeight(attributes.asSize("minHeight", MATCH_PARENT));
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
    public void invalidate(boolean layout) {
        if (activity != null) {
            activity.invalidate(layout);
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        if (getVisibility() == VISIBLE) {
            super.onDraw(context);
        }
    }
}
