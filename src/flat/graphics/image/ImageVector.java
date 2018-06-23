package flat.graphics.image;

import flat.graphics.SmartContext;
import flat.graphics.context.Paint;
import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;

public class ImageVector implements Image {

    private final Rectangle view;
    private final Shape[] paths;
    private final Paint[] paints;

    public ImageVector(Rectangle view, Shape[] paths, Paint[] paints) {
        this.view = view;
        this.paths = paths;
        this.paints = paints;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public float getWidth() {
        return view.width;
    }

    @Override
    public float getHeight() {
        return view.height;
    }

    @Override
    public void draw(SmartContext context, float x, float y, float width, float height, float frame) {
        context.setTransform2D(
                context.getTransform2D()
                        .translate(x, y)
                        .scale(width / view.width, height / view.height));

        for (int i = 0; i < paths.length; i++) {
            context.setPaint(paints[i]);
            context.drawShape(paths[i], true);
        }
    }
}
