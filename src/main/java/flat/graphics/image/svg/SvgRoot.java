package flat.graphics.image.svg;

import flat.math.shapes.Path;
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
            var bb = new Path(shape.getShape().pathIterator(shape.getTransform())).bounds();
            if (boundingBox == null) {
                boundingBox = bb;
            } else {
                boundingBox.add(bb);
            }
        }
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
