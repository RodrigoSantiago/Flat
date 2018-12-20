package test;

import flat.Flat;
import flat.events.ActionEvent;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.math.shapes.Circle;
import flat.math.shapes.Ellipse;
import flat.math.shapes.RoundRectangle;
import flat.resources.ResourcesManager;
import flat.uxml.UXTheme;
import flat.widget.Activity;
import flat.resources.ResourceStream;
import flat.widget.layout.Box;
import flat.widget.layout.Grid;
import flat.widget.layout.ScrollBox;
import flat.widget.layout.Tab;
import flat.widget.text.Button;

public class MainActivity extends Activity {

    @Flat
    Tab tab;

    public MainActivity() {
        setTheme(new UXTheme(ResourcesManager.getInput("themes/material.uxss")));
        setStream(new ResourceStream("screen_test"));
    }

    Box b = new Box() {
        @Override
        public void onDraw(SmartContext context) {
            super.onDraw(context);
        }
    };

    @Override
    public void onLoad() {

    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
    }

    @Flat
    public void pagina_1(PointerEvent event) {
        if (event.getType() == PointerEvent.RELEASED) {
            tab.setActivePage(1);
        }
    }

    @Flat
    public void pagina_2(PointerEvent event) {
        if (event.getType() == PointerEvent.RELEASED) {
            tab.setActivePage(2);
        }
    }

    @Flat
    public void pagina_3(PointerEvent event) {
        if (event.getType() == PointerEvent.RELEASED) {
            tab.remove(tab.getPages().get(1));
        }
    }
}
