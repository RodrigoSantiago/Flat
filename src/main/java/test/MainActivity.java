package test;

import flat.Flat;
import flat.animations.presets.Hide;
import flat.backend.GL;
import flat.backend.SVG;
import flat.events.ActionEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Context;
import flat.graphics.context.Font;
import flat.graphics.context.Texture2D;
import flat.graphics.text.FontPosture;
import flat.graphics.text.FontStyle;
import flat.graphics.text.FontWeight;
import flat.math.Affine;
import flat.math.stroke.BasicStroke;
import flat.widget.layout.LinearBox;
import flat.window.Activity;
import flat.window.Application;

public class MainActivity extends Activity {

    public MainActivity(Context context) {
        super(context);

        setTheme(Application.getResourcesManager().getResource("default/themes"));
        setScene(Application.getResourcesManager().getResource("default/screen_test/screen_test.uxml"));
    }

    @Override
    public void onShow() {

    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);

    }

    @Flat
    public void onAction(ActionEvent event) {
        Hide hide = new Hide(event.getSource());
        event.getSource().setClickable(false);
        hide.setDuration(1000);
        hide.play(this);
    }
}