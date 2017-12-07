package flat.screen;

import flat.events.PointerEvent;
import flat.graphics.*;
import flat.graphics.image.Image;
import flat.widget.containers.Box;

public class Scene extends Box {

    Activity activity;

    float corners;
    public Scene(Activity activity) {
        this.activity = activity;

        setBackgroundColor(0xFFFFFFFF);

        Box box = new Box();
        box.setBackgroundColor(0xFF0000FF);
        box.setPrefSize(100, 100);
        add(box);

        Shader shader = new Shader();
        shader.setFragmentSorce(Context.getContext().fragment);
        shader.setVertexSorce(Context.getContext().vertex);
        shader.compile();

        VertexArray vertexArray = new VertexArray();
        vertexArray.setData(new float[] {
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.0f,  0.5f, 0.0f
        });
        vertexArray.setAttributes(0, 3, 3, 0);

        setPointerListener(event -> {
            if (event.getType() == PointerEvent.DRAGGED) {
                corners += 1;
                invalidate(true);
            }
            return false;
        });
    }

    @Override
    public void onLayout(float width, float height) {
        super.onLayout(activity.getWidth(), activity.getHeight());
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
        context.clear();
        context.setView(0, 0, (int) activity.getWidth(), (int) activity.getHeight());

        context.setColor(0xDDDDDDFF);
        context.drawRect(0, 0, (int) activity.getWidth(), (int) activity.getHeight(), true);

        context.drawRoundRectShadow(10, 12, 50, 100, corners, 10.0f, 1f);
    }

    @Override
    public void invalidate(boolean layout) {
        activity.invalidate(layout);
    }
}
