package test;

import flat.Flat;
import flat.animations.presets.Hide;
import flat.animations.presets.Show;
import flat.animations.property.Property;
import flat.events.ActionEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Context;
import flat.resources.Dimension;
import flat.resources.ResourceStream;
import flat.resources.ResourcesManager;
import flat.uxml.UXTheme;
import flat.widget.Activity;
import flat.widget.Application;
import flat.widget.layout.LinearBox;
import flat.widget.text.Button;

public class MainActivity extends Activity {

    @Flat public LinearBox box;
    @Flat public Button btn1;

    public MainActivity(Context context) {
        super(context);

        setTheme(new UXTheme(ResourcesManager.getInput("themes/material.uxss")));
        setSceneStream(new ResourceStream("screen_test"));
    }

    float mx, my;
    @Override
    public void onLoad() {
        getScene().setPointerListener(event -> {
            mx = (event.getX()-100) / 100;
            my = (event.getY()-100) / 100;
            if (event.getPointerID() == 1) f += 0.01f;
            else f -= 0.01;
            invalidate(true);
        });
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
        /*context.setAntialiasEnabled(true);
        context.setView(0, 0, (int) getWidth(), (int) getHeight());
        context.clear(0xFFFFFFFF, 1, 1);

        //context.getContext().svgBegin();
        context.clearClip();

        context.setColor(0x00000080);
        context.setTextBlur(0.1f);
        context.setTextSize(64);
        context.drawText(104, 104, "Ola Mundão" + f);
        context.setColor(0xEEEEEEFF);
        context.setTextBlur(1);
        context.setTextSize(64);
        context.drawText(100, 100, "Ola Mundão" + f);
        context.setColor(0x0000FF64);

        Paint radial = Paint.radial(100, 100, 0, 100, mx, my,
                new float[]{0, 1}, new int[]{0xFF0000FF, 0x00FF00FF}, Paint.CycleMethod.REFLECT);
        Paint linear = Paint.linear(0, 0, 200, 200, new float[]{0, 1}, new int[]{0xFF0000FF, 0x0000FFFF}, Paint.CycleMethod.REFLECT);
        context.setPaint(radial);
        context.drawCircle(100, 100, 100, true);*/

    }
    float f = 1;

    @Flat
    public void onAction(ActionEvent event) {
        Hide hide = new Hide(event.getSource());
        event.getSource().setClickable(false);
        hide.setDuration(1000);
        hide.play(this);
    }

    @Flat
    public void onAdd(ActionEvent event) {
        Button btn = new Button();
        btn.setStyle(getTheme().getStyle("button"));
        btn.applyStyle();
        btn.setText("ola");
        btn.setActionListener(this::onAction);
        box.add(btn);

        Show show = new Show(btn);
        show.setDimension(64);
        show.setDuration(1000);
        show.play(this);

        Property p = new Property<>(box::setPrefWidth, 0f, 100f);
    }
}