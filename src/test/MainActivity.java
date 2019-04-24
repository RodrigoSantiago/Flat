package test;

import flat.Flat;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.math.Affine;
import flat.math.stroke.BasicStroke;
import flat.resources.ResourcesManager;
import flat.uxml.UXTheme;
import flat.widget.Activity;
import flat.resources.ResourceStream;
import flat.widget.layout.ScrollBox;
import flat.widget.layout.Tab;
import flat.widget.text.TextField;

public class MainActivity extends Activity {

    @Flat
    TextField text;

    public MainActivity() {
        setTheme(new UXTheme(ResourcesManager.getInput("themes/material.uxss")));
        setStream(new ResourceStream("screen_test"));
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
    }

    @Flat
    public void onClick(PointerEvent event) {
        if (event.getType() == PointerEvent.PRESSED) {
            text.setSingleLine(!text.isSingleLine());
        }
    }
}
