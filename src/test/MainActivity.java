package test;

import flat.Flat;
import flat.events.DrawEvent;
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
import flat.widget.text.TextArea;

public class MainActivity extends Activity {

    @Flat
    TextArea text;

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

    TextArea.Style style = new TextArea.Style(Font.CURSIVE, 48, 0x0000FFFF);
    @Flat
    public void onClick(PointerEvent event) {
        if (event.getType() == PointerEvent.PRESSED) {
            text.setStyle(1, 10, style);
        }
    }

    float x = 0;
    @Flat
    public void onDraw(DrawEvent event) {
        SmartContext context = event.getSmartContext();
        context.setColor(0x00C000FF);
        context.drawRect(x++, 0, 100, 100, true);
    }
}
