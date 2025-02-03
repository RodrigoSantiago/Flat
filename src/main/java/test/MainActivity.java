package test;

import flat.Flat;
import flat.animations.presets.Hide;
import flat.events.ActionEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Context;
import flat.widget.layout.LinearBox;
import flat.window.Activity;
import flat.window.Application;

public class MainActivity extends Activity {

    public MainActivity(Context context) {
        super(context);

        setTheme(Application.getResourcesManager().getResource("default/themes"));
        setScene(Application.getResourcesManager().getResource("default/screen_test/screen_test.uxml"));
    }

    float mx, my;

    @Flat
    private LinearBox layout;

    @Override
    public void onShow() {
        System.out.println(layout);
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
}