package test;

import flat.Flat;
import flat.events.ActionEvent;
import flat.events.DragEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.resources.ResourcesManager;
import flat.uxml.UXTheme;
import flat.widget.Activity;
import flat.resources.ResourceStream;
import flat.widget.Application;
import flat.widget.layout.Drawer;
import flat.widget.text.TextArea;

public class MainActivity extends Activity {

    static int a ;
    @Flat
    Drawer nav;

    public MainActivity() {
        a++;
        setTheme(new UXTheme(ResourcesManager.getInput("themes/material.uxss")));
        setStream(new ResourceStream("screen_test"));
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
        context.setTransform2D(null);

        if (a % 2 == 0) {
            context.setColor(0xFF0000FF);
        } else {
            context.setColor(0x00FF00FF);
        }
        context.drawRect(100, 100, 100, 100, true);
    }

    TextArea.Style style = new TextArea.Style(Font.CURSIVE, 48, 0x0000FFFF);

    @Flat
    public void onAction(ActionEvent event) {
        //nav.setShown(!nav.isShown());
        //Application.setActivity(new MainActivity());
    }

    @Flat
    public void onPointer(PointerEvent event) {
        //System.out.println(event);
    }

    @Flat
    public void onDrag1(DragEvent event) {
        System.out.println(event);
        if (event.getType() == DragEvent.STARTED) {
            event.dragStart();
        }
    }

    @Flat
    public void onDrag2(DragEvent event) {
        System.out.println(event);
        if (event.getType() == DragEvent.DROPPED) {
            event.dragComplete(true);
        }
    }
}
