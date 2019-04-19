package test;

import flat.Flat;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
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

    long t = 0;
    float mean = 0;
    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
        long n = System.nanoTime();
        context.setTransform2D(null);
        if (mean == 0) {
            mean = (1000000000f/(n - t));
        } else {
            mean = mean * 0.9f + (1000000000f/(n - t)) * 0.1f;
        }
        context.setTextSize(64);
        context.drawText(300, 0, ""+ ((int)mean));
        invalidate(true);
        t = n;

        /*context.setAntialiasEnabled(true);
        context.setView(0, 0, (int) getWidth(), (int) getHeight());
        context.clear(0, 1, 0);
        context.clearClip(false);
        context.setColor(0xFF0000FF);
        context.drawRect(0, 0, 100, 100, true);*/
    }

    @Flat
    public void onClick(PointerEvent event) {
        if (event.getType() == PointerEvent.PRESSED) {
            text.setSingleLine(!text.isSingleLine());
        }
    }
}
