package flat.graphics.image.svg;

import flat.graphics.context.Paint;
import flat.math.Affine;
import flat.math.shapes.Shape;
import flat.math.shapes.Stroke;
import flat.math.stroke.BasicStroke;

import java.util.ArrayList;

public class SvgShape extends SvgNode {
    private final Shape shape;
    private final Paint fillPaint;
    private final Paint strokePaint;
    private final Affine transform;
    private final Stroke stroke;

    public SvgShape(SvgNode parent, String id, Affine transform, Shape shape, Paint fillPaint, Paint strokePaint,
                    float strokeWidth, float strokeMiterLimit, int strokeCap, int strokeJoin) {
        super(parent, id);

        SvgGroup group = null;
        if (parent instanceof SvgGroup g) {
            group = g;
        }

        if (group != null && group.transform != null) {
            if (transform == null) {
                this.transform = group.transform;
            } else {
                this.transform = transform.preMul(group.transform);
            }
        } else {
            this.transform = transform;
        }

        this.shape = shape;
        this.fillPaint = fillPaint == null && group != null ? group.getFillPaint() : fillPaint;
        this.strokePaint = strokePaint == null && group != null ? group.getStrokePaint() : strokePaint;

        strokeWidth = strokeWidth == -1 && group != null ? group.getStrokeWidth() : strokeWidth;
        strokeCap = strokeCap == -1 && group != null ? group.getStrokeCap() : strokeCap;
        strokeJoin = strokeJoin == -1 && group != null ? group.getStrokeJoin() : strokeJoin;
        strokeMiterLimit = strokeMiterLimit == -1 && group != null ? group.getStrokeMiterLimit() : strokeMiterLimit;

        if (strokeWidth > 0) {
            stroke = new BasicStroke(
                    strokeWidth,
                    strokeCap == -1 ? 0 : strokeCap,
                    strokeJoin == -1 ? 0 : strokeCap,
                    strokeMiterLimit == -1 ? 10 : strokeMiterLimit
            );
        } else {
            stroke = null;
        }
    }

    @Override
    void getShapes(ArrayList<SvgShape> allShapes) {
        allShapes.add(this);
    }

    public Shape getShape() {
        return shape;
    }

    public Paint getFillPaint() {
        return fillPaint;
    }

    public Paint getStrokePaint() {
        return strokePaint;
    }

    public Affine getTransform() {
        return transform == null ? null : new Affine(transform);
    }

    public Stroke getStroke() {
        return stroke;
    }
}
