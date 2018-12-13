package test;

import flat.Flat;
import flat.backend.GL;
import flat.graphics.SmartContext;
import flat.graphics.context.Paint;
import flat.graphics.text.Align;
import flat.math.Affine;
import flat.math.operations.Area;
import flat.math.shapes.Arc;
import flat.math.shapes.Rectangle;
import flat.math.shapes.RoundRectangle;
import flat.resources.ResourcesManager;
import flat.uxml.UXTheme;
import flat.widget.Activity;
import flat.resources.ResourceStream;
import flat.widget.layout.Scroll;
import flat.widget.text.Button;

public class MainActivity extends Activity {

    @Flat
    public Scroll scroll;

    public MainActivity() {
        setTheme(new UXTheme(ResourcesManager.getInput("themes/material.uxss")));
        setStream(new ResourceStream("screen_test"));
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
        //context.drawShape(new RoundRectangle(0,0,100,100, 25,25,25,25), true);
    }

}
