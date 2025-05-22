package flat.widget.image;

import flat.events.DrawEvent;
import flat.graphics.Graphics;
import flat.math.shapes.Rectangle;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
import flat.widget.Widget;

public class Canvas extends Widget {

    private UXListener<DrawEvent> drawListener;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();
        setDrawListener(attrs.getAttributeListener("on-draw", DrawEvent.class, controller));
    }

    @Override
    public void onDraw(Graphics graphics) {
        super.onDraw(graphics);
        draw(graphics);
    }

    public UXListener<DrawEvent> getDrawListener() {
        return drawListener;
    }

    public void setDrawListener(UXListener<DrawEvent> drawListener) {
        this.drawListener = drawListener;
    }

    private void draw(Graphics graphics) {
        if (drawListener != null) {
            UXListener.safeHandle(drawListener, new DrawEvent(this, graphics,
                    new Rectangle(0, 0, getLayoutWidth(), getLayoutHeight()),
                    getBackgroundShape(),
                    new Rectangle(getInX(), getInY(), getInWidth(), getInHeight()),
                    getTransform())
            );
        }
    }
}
