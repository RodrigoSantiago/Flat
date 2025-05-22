package flat.events;

import flat.graphics.Graphics;
import flat.math.Affine;
import flat.math.shapes.Rectangle;
import flat.math.shapes.RoundRectangle;
import flat.widget.Widget;

public class DrawEvent extends Event {
    public static final Type DRAW = new Type("DRAW");

    private final Graphics graphics;
    private final Rectangle bBox;
    private final RoundRectangle outBox;
    private final Rectangle inBox;
    private final Affine transform;

    public DrawEvent(Widget source, Graphics graphics, Rectangle bBox, RoundRectangle outBox, Rectangle inBox, Affine transform) {
        super(source, DRAW);
        this.graphics = graphics;
        this.bBox = bBox;
        this.outBox = outBox;
        this.inBox = inBox;
        this.transform = transform;
    }

    public Graphics getGraphics() {
        return graphics;
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
        return "(" + getSource() + ") DrawEvent [DRAW]";
    }

    public static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}
