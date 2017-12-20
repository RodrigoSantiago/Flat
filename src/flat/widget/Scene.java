package flat.widget;

import flat.backend.GL;
import flat.events.PointerEvent;
import flat.graphics.context.Context;
import flat.graphics.context.Shader;
import flat.graphics.context.ShaderProgram;
import flat.graphics.context.enuns.AttributeType;
import flat.graphics.context.enuns.BlendFunction;
import flat.graphics.context.enuns.ShaderType;
import flat.graphics.context.enuns.VertexMode;
import flat.graphics.smart.SmartContext;
import flat.graphics.smart.effects.RoundRectShadow;
import flat.graphics.smart.mesh.VertexData;
import flat.math.Affine;
import flat.screen.Activity;
import flat.widget.layout.Box;
import flat.widget.text.Label;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Scene extends Box {

    Activity activity;
    Box b;

    ShaderProgram shader;

    public Scene(Activity activity) {
        this.activity = activity;
        Label label = new Label();
        label.setPrefWidth(200);
        label.setBackgroundColor(0xFF0000FF);
        label.setText("Ola mundo !");

        b  = new Box();
        b.add(label);
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
    public void onDraw(SmartContext context) {
        context.setView(0, 0, (int) activity.getWidth(), (int) activity.getHeight());
        context.clear(0xDDDDDDFF);
        super.onDraw(context);
    }

    @Override
    public void invalidate(boolean layout) {
        activity.invalidate(layout);
    }
}
