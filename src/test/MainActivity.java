package test;

import flat.Flat;
import flat.graphics.SmartContext;
import flat.screen.Activity;
import flat.uxml.data.ResourceStream;
import flat.widget.image.ImageView;

public class MainActivity extends Activity {

    @Flat ImageView img;

    public MainActivity() {
        setStream(new ResourceStream("screen_test"));
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }
}
