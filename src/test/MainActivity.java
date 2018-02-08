package test;

import flat.animations.TransformAnimation;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.graphics.text.Align;
import flat.screen.Activity;
import flat.widget.layout.*;
import flat.widget.text.Button;

public class MainActivity extends Activity {

    VBox vBox = new VBox();
    Button btn = new Button();
    public MainActivity() {
        vBox.setBackgroundColor(0xEFEFEFFF);
        vBox.setTranslateX(100);
        vBox.setTranslateY(100);
        btn.setText("BUTTON");
        btn.setMargins(10, 10, 10, 10);
        btn.setHorizontalAlign(Align.Horizontal.CENTER);
        btn.setTextColor(0xFFFFFFFF);
        btn.setBackgroundColor(0x2196F3FF);
        btn.setPadding(8, 16, 8, 16);
        btn.setBackgroundCorners(2, 2, 2, 2);
        btn.setElevation(4);
        btn.setShadowEffectEnabled(true);
        btn.setVerticalAlign(Align.Vertical.MIDDLE);
        btn.setCenterX(0.5f);
        btn.setCenterY(0.5f);
        btn.setFont(Font.DEFAULT_BOLD);
        //btn.setRotate(45);
        btn.setRippleEffectEnabled(true);
        vBox.setPadding(10, 10, 10, 10);

        TransformAnimation animation = new TransformAnimation(btn);
        animation.setLinearAngularMix(true);
        animation.setLoops(-1);
        animation.setFrom(0, 0, 1, 1, 0)
                .setTo(0, 0, 1, 1, 360);
        animation.setDuration(2000);
        animation.play();

        vBox.add(btn);
        getScene().add(vBox);
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
    }

    /*
     float m = Math.min(ext.x, ext.y);
     float d = pt.length();
     float r = vec2(pt.x * (ext.y / ext.x), pt.y).nor().mul(ext.x, ext.y).length();
     float t = (d - (r - m)) / m;
     */
}
