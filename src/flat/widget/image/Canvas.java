package flat.widget.image;

import flat.events.DrawEvent;
import flat.events.DrawListener;
import flat.graphics.SmartContext;
import flat.math.shapes.Shape;
import flat.uxml.Controller;
import flat.uxml.UXStyleAttrs;
import flat.widget.Widget;

import java.lang.reflect.Method;

public class Canvas extends Widget {

    private DrawListener drawListener;
    private boolean autorefresh;

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        setAutorefresh(style.asBool("autorefresh", isAutorefresh()));
        Method handle = style.asListener("on-draw", DrawEvent.class, controller);
        if (handle != null) {
            setDrawListener(new DrawListener.AutoDrawListener(controller, handle));
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        if (drawListener != null) {
            Shape shape = context.getClip();

            context.clearClip(false);
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

    public DrawListener getDrawListener() {
        return drawListener;
    }

    public void setDrawListener(DrawListener drawListener) {
        this.drawListener = drawListener;
    }
}
