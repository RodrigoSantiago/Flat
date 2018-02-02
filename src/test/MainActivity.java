package test;

import flat.graphics.SmartContext;
import flat.graphics.context.Paint;
import flat.screen.Activity;

public class MainActivity extends Activity {

    Paint paint = new Paint();
    public MainActivity() {

    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
        paint.setRadial(150, 150, 0, 50, new float[]{0, 1}, new int[]{0xFF0000FF, 0xFFFFFFFF});
        context.getContext().svgPaint(paint);
        context.getContext().svgDrawEllipse(100, 100, 100, 100, true);
    }
}
