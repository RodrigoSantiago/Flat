package test;

import flat.graphics.SmartContext;
import flat.resources.ResourcesManager;
import flat.uxml.UXTheme;
import flat.widget.Activity;
import flat.resources.ResourceStream;

public class MainActivity extends Activity {

    public MainActivity() {
        setTheme(new UXTheme(ResourcesManager.getInput("material.theme")));
        setStream(new ResourceStream("screen_test"));
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
    }
}
