package test;

import flat.Flat;
import flat.animations.presets.Hide;
import flat.animations.presets.Show;
import flat.animations.property.Property;
import flat.events.ActionEvent;
import flat.graphics.SmartContext;
import flat.resources.Dimension;
import flat.resources.ResourcesManager;
import flat.uxml.UXTheme;
import flat.widget.Activity;
import flat.resources.ResourceStream;
import flat.widget.layout.LinearBox;
import flat.widget.text.Button;

public class MainActivity extends Activity {

    @Flat public LinearBox box;
    @Flat public Button btn1;

    public MainActivity(int n) {
        setTheme(new UXTheme(ResourcesManager.getInput("themes/material.uxss")));
        setSceneStream(new ResourceStream("screen_test"));
    }

    @Override
    public void onLoad() {

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

    @Flat
    public void onAdd(ActionEvent event) {
        Button btn = new Button();
        btn.setStyle(getTheme().getStyle("button"));
        btn.applyStyle();
        btn.setText("ola");
        btn.setActionListener(this::onAction);
        box.add(btn);

        Show show = new Show(btn);
        show.setDimension(Dimension.dpPx(64));
        show.setDuration(1000);
        show.play(this);

        Property p = new Property<>(box::setPrefWidth, 0f, 100f);
    }
}