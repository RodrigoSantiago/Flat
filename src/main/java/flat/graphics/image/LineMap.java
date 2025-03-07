package flat.graphics.image;

import flat.graphics.Graphics;
import flat.graphics.context.Paint;
import flat.math.Affine;
import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;
import flat.math.shapes.Stroke;
import flat.widget.enums.ImageFilter;

public class LineMap implements Drawable {

    private final Rectangle view;
    private final SVGPath[] svgPaths;

    public LineMap(Rectangle view, SVGPath[] svgPaths) {
        this.view = view;
        this.svgPaths = svgPaths;
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
    public void draw(Graphics context, float x, float y, float width, float height, int color, ImageFilter filter) {
        Affine affine = context.getTransform2D();
        context.setTransform2D(
                context.getTransform2D()
                        .translate(x, y)
                        .scale(width / view.width, height / view.height));

        Paint paint = context.getPaint();
        Stroke stroke = context.getStroker();
        for (SVGPath svgPath : svgPaths) {
            if (svgPath.observeFill) {
                context.setColor(color);
                context.drawShape(svgPath.shape, true);
            } else if (svgPath.fillPaint != null) {
                context.setPaint(svgPath.fillPaint);
                context.drawShape(svgPath.shape, true);
            }
            if (svgPath.observeStroke) {
                context.setColor(color);
                context.setStroker(stroke);
                context.drawShape(svgPath.shape, false);
            } else if (svgPath.strokePaint != null) {
                context.setStroker(svgPath.stroke);
                context.setPaint(svgPath.strokePaint);
                context.drawShape(svgPath.shape, false);
            }
        }
        context.setTransform2D(affine);
    }

    @Override
    public void draw(Graphics context, float x, float y, float frame, ImageFilter filter) {
        draw(context, x, y, getWidth(), getHeight(), 0xFFFFFFFF, filter);
    }

    public static class SVGPath {
        public final String id;
        public final Shape shape;
        public final Stroke stroke;
        public final Paint fillPaint, strokePaint;
        public final boolean observeFill, observeStroke;

        public SVGPath(String id, Shape shape, Stroke stroke, Paint fillPaint, Paint strokePaint,
                       boolean observeFill, boolean observeStroke) {
            this.id = id;
            this.shape = shape;
            this.stroke = stroke;
            this.fillPaint = fillPaint;
            this.strokePaint = strokePaint;
            this.observeFill = observeFill;
            this.observeStroke = observeStroke;
        }

    }
}
