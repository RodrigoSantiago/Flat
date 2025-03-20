package flat.graphics.image.svg;

import flat.math.shapes.Path;
import flat.math.shapes.PathIterator;
import flat.math.shapes.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class SvgRoot extends SvgNode {

    private final ArrayList<SvgShape> allShapes = new ArrayList<>();
    private float width;
    private float height;
    private Rectangle viewBox;
    private Rectangle boundingBox;

    public SvgRoot(String id, float width, float height, Rectangle viewBox) {
        super(null, id);
        this.width = width;
        this.height = height;
        this.viewBox = viewBox;
    }

    @Override
    void applyChildren(SvgChildren children) {
        for (var child : children) {
            child.getChild().getShapes(allShapes);
        }

        for (var shape : allShapes) {
            var bb = bounds(shape.getShape().pathIterator(shape.getTransform()));
            if (boundingBox == null) {
                boundingBox = bb;
            } else {
                boundingBox.add(bb);
            }
        }
        if (boundingBox == null) {
            boundingBox = viewBox == null ? new Rectangle(0, 0, 0, 0) : new Rectangle(viewBox);
        }
    }

    public Rectangle bounds(PathIterator it) {
        float rx1 = 0, ry1 = 0, rx2 = 0, ry2 = 0;
        boolean one = true;
        float[] coords = new float[6];
        while (!it.isDone()) {
            switch (it.currentSegment(coords)) {
                case PathIterator.SEG_CUBICTO:
                    rx1 = one ? coords[4] : Math.min(rx1, coords[4]);
                    ry1 = one ? coords[5] : Math.min(ry1, coords[5]);
                    rx2 = one ? coords[4] : Math.max(rx2, coords[4]);
                    ry2 = one ? coords[5] : Math.max(ry2, coords[5]);
                case PathIterator.SEG_QUADTO:
                    rx1 = one ? coords[2] : Math.min(rx1, coords[2]);
                    ry1 = one ? coords[3] : Math.min(ry1, coords[3]);
                    rx2 = one ? coords[2] : Math.max(rx2, coords[2]);
                    ry2 = one ? coords[3] : Math.max(ry2, coords[3]);
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_LINETO:
                    rx1 = one ? coords[0] : Math.min(rx1, coords[0]);
                    ry1 = one ? coords[1] : Math.min(ry1, coords[1]);
                    rx2 = one ? coords[0] : Math.max(rx2, coords[0]);
                    ry2 = one ? coords[1] : Math.max(ry2, coords[1]);
            }
            it.next();
            one = false;
        }
        return new Rectangle(rx1, ry1, rx2 - rx1, ry2 - ry1);
    }

    public List<SvgShape> getAllShapes() {
        return allShapes;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public Rectangle getView() {
        return new Rectangle(viewBox);
    }

    public Rectangle getBoundingBox() {
        return new Rectangle(boundingBox);
    }
}
