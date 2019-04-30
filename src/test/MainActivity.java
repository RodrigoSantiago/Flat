package test;

import flat.Flat;
import flat.events.ActionEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.resources.ResourcesManager;
import flat.uxml.UXTheme;
import flat.widget.Activity;
import flat.resources.ResourceStream;
import flat.widget.layout.Drawer;
import flat.widget.text.TextArea;

public class MainActivity extends Activity {

    @Flat
    Drawer nav;

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
    public void onAction(ActionEvent event) {
        nav.setShown(!nav.isShown());
    }
}
