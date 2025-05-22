package flat.events;

import flat.graphics.Graphics;
import flat.math.Affine;
import flat.math.shapes.Rectangle;
import flat.math.shapes.RoundRectangle;
import flat.widget.Widget;

public class LayoutEvent extends Event {
    public static final Type LAYOUT = new Type("LAYOUT");

    private final Rectangle bBox;
    private final RoundRectangle outBox;
    private final Rectangle inBox;
    private final Affine transform;

    public LayoutEvent(Widget source, Rectangle bBox, RoundRectangle outBox, Rectangle inBox, Affine transform) {
        super(source, LAYOUT);
        this.bBox = bBox;
        this.outBox = outBox;
        this.inBox = inBox;
        this.transform = transform;
    }

    public Rectangle getBBox() {
        return bBox;
    }

    public RoundRectangle getOutBox() {
        return outBox;
    }

    public Rectangle getInBox() {
        return inBox;
    }

    public Affine getTransform() {
        return transform;
    }

    @Override
    public String toString() {
        return "(" + getSource() + ") LayoutEvent [LAYOUT]";
    }

    public static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}
