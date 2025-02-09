package test;

import flat.Flat;
import flat.animations.presets.Hide;
import flat.backend.GL;
import flat.backend.SVG;
import flat.events.ActionEvent;
import flat.graphics.Color;
import flat.graphics.SmartContext;
import flat.graphics.context.Context;
import flat.graphics.context.Font;
import flat.graphics.context.Texture2D;
import flat.graphics.text.FontPosture;
import flat.graphics.text.FontStyle;
import flat.graphics.text.FontWeight;
import flat.math.Affine;
import flat.math.shapes.Path;
import flat.math.stroke.BasicStroke;
import flat.widget.layout.LinearBox;
import flat.widget.text.Button;
import flat.widget.text.Label;
import flat.window.Activity;
import flat.window.Application;

public class MainActivity extends Activity {

    public MainActivity(Context context) {
        super(context);

        setTheme(Application.getResourcesManager().getResource("default/themes"));
        setScene(Application.getResourcesManager().getResource("default/screen_test/screen_test.uxml"));
    }

    @Flat public Button button;
    @Flat public Label label;

    @Flat
    public void onButtonClick(ActionEvent actionEvent) {
        label.setText(label.getText() + ".");
        System.out.println("a");
    }

    @Override
    public void onShow() {
    }

    float t;
    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
        //t += 1 / 120f;
        //if (t > 1) t = 0;
        //context.setTransform2D(null);
        //context.setTextSize(64);
        //context.setTextFont(Font.getDefault());
        //context.setColor(Color.black);
        //context.setTextBlur(1);
        //context.drawText(32, 300, "Ola Mundo");

        /*context.setStroker(new BasicStroke(5.5f));
        context.setAntialiasEnabled(false);
        context.setColor(0x00000080);
        context.setAntialiasEnabled(false);
        context.drawLine(100, 100, 500, 600);
        context.setAntialiasEnabled(true);
        context.drawLine(100, 500, 500, 100);*/
        //context.setColor(Color.black);
        //context.setTextBlur(0);
        //context.drawText(32, 200, "Ola Mundo");
        context.setTransform2D(null);
        context.setColor(0xFFFFFFFF);
        context.drawRect(0, 0, 100, 100, true);
        context.setColor(0xFF000080);
        context.drawRect(0, 0, 100, 100, true);
    }

    @Flat
    public void onAction(ActionEvent event) {
        Hide hide = new Hide(event.getSource());
        event.getSource().setClickable(false);
        hide.setDuration(1000);
        hide.play(this);
    }
}