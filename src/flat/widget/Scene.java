package flat.widget;

import flat.screen.Context;

public class Scene extends Widget {

    @Override
    public void onLayout(double width, double height) {
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

    }

    @Override
    public void invalidate(boolean layout) {

    }

    @Override
    public void invalidadeOrder() {

    }
}
