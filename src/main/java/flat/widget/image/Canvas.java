package flat.widget.image;

import flat.events.DrawEvent;
import flat.graphics.SmartContext;
import flat.math.shapes.Shape;
import flat.uxml.Controller;
import flat.uxml.UXBuilder;
import flat.uxml.UXListener;
import flat.uxml.UXTheme;
import flat.widget.Widget;

import java.lang.reflect.Method;

public class Canvas extends Widget {

    private UXListener<DrawEvent> drawListener;
    private boolean autorefresh;

    @Override
    public void applyAttributes(UXTheme theme, Controller controller, UXBuilder builder) {
        super.applyAttributes(theme, controller, builder);

        /*setAutorefresh(theme.asBool("autorefresh", isAutorefresh()));
        Method handle = theme.linkListener("on-draw", DrawEvent.class, controller);
        if (handle != null) {
            setDrawListener(new DrawListener.AutoDrawListener(controller, handle));
        }*/
    }

    @Override
    public void onDraw(SmartContext context) {
        if (drawListener != null) {
            Shape shape = context.getClip();

            context.clearClip();
            context.setTransform2D(getTransform());
            context.softFlush();

            drawListener.handle(new DrawEvent(this, context));

            context.softFlush();
            context.setTransform2D(null);
            context.setClip(shape);
        }
        if (autorefresh) {
            invalidate(false);
        }
    }

    public boolean isAutorefresh() {
        return autorefresh;
    }

    public void setAutorefresh(boolean autorefresh) {
        if (this.autorefresh != autorefresh) {
            this.autorefresh = autorefresh;
            invalidate(false);
        }
    }

    public UXListener<DrawEvent> getDrawListener() {
        return drawListener;
    }

    public void setDrawListener(UXListener<DrawEvent> drawListener) {
        this.drawListener = drawListener;
    }
}
