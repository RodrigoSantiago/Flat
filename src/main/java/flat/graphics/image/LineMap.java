package flat.graphics.image;

import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.context.Paint;
import flat.graphics.image.svg.SvgRoot;
import flat.graphics.image.svg.SvgShape;
import flat.math.Affine;
import flat.math.shapes.Path;
import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;
import flat.math.shapes.Stroke;
import flat.math.stroke.BasicStroke;
import flat.widget.enums.ImageFilter;

public class LineMap implements Drawable {

    private final SvgRoot root;
    private final Rectangle view;

    public LineMap(SvgRoot root) {
        this.root = root;
        this.view = root.getView();
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
    public void draw(Graphics graphics, float x, float y, float width, float height, int color, ImageFilter filter) {
        Affine affine = graphics.getTransform2D();
        Affine base = graphics.getTransform2D()
                .translate(x, y)
                .scale(width / view.width, height / view.height);

        graphics.setTransform2D(base);

        Paint paint = graphics.getPaint();
        Stroke stroke = graphics.getStroker();
        for (SvgShape svgPath : root.getAllShapes()) {

            Affine local = svgPath.getTransform();
            if (local != null) {
                graphics.setTransform2D(local.preMul(base));
            }

            if (svgPath.getFillPaint() != null) {
                graphics.setPaint(svgPath.getFillPaint().multiply(color));
                graphics.drawShape(svgPath.getShape(), true);
            }

            if (svgPath.getStrokePaint() != null && svgPath.getStroke() != null) {
                graphics.setPaint(svgPath.getStrokePaint().multiply(color));
                graphics.setStroker(svgPath.getStroke());
                graphics.drawShape(svgPath.getShape(), false);
            }

            if (svgPath.getFillPaint() == null && svgPath.getStrokePaint() == null) {
                graphics.setColor(color);
                graphics.drawShape(svgPath.getShape(), true);
            }

            if (local != null) {
                graphics.setTransform2D(base);
            }
        }
        graphics.setTransform2D(affine);
    }

    @Override
    public void draw(Graphics context, float x, float y, float frame, ImageFilter filter) {
        draw(context, x, y, getWidth(), getHeight(), 0xFFFFFFFF, filter);
    }
}
