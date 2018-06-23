package test;

import flat.graphics.SmartContext;
import flat.widget.Activity;
import flat.resources.ResourceStream;

public class MainActivity extends Activity {

    public MainActivity() {
        setStream(new ResourceStream("screen_test"));
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
    }
}
