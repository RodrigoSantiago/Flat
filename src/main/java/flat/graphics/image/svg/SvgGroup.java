package flat.graphics.image.svg;

import flat.graphics.context.Paint;
import flat.math.Affine;

import java.util.ArrayList;

public class SvgGroup extends SvgNode {
    private final Paint fillPaint;
    private final Paint strokePaint;
    private final float strokeWidth;
    private final float strokeMiterLimit;
    private final int strokeCap;
    private final int strokeJoin;
    protected final Affine transform;

    private final ArrayList<SvgNode> children = new ArrayList<>();

    public SvgGroup(SvgNode parent, String id, Affine transform, Paint fillPaint, Paint strokePaint,
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

        this.fillPaint = fillPaint == null && group != null ? group.getFillPaint() : fillPaint;
        this.strokePaint = strokePaint == null && group != null ? group.getStrokePaint() : strokePaint;
        this.strokeWidth = strokeWidth == -1 && group != null ? group.getStrokeWidth() : strokeWidth;
        this.strokeMiterLimit = strokeMiterLimit == -1 && group != null ? group.getStrokeMiterLimit() : strokeMiterLimit;
        this.strokeCap = strokeCap == -1 && group != null ? group.getStrokeCap() : strokeCap;
        this.strokeJoin = strokeJoin == -1 && group != null ? group.getStrokeJoin() : strokeJoin;
    }

    @Override
    void applyChildren(SvgChildren children) {
        for (var child : children) {
            this.children.add(child.getChild());
        }
    }

    @Override
    void getShapes(ArrayList<SvgShape> allShapes) {
        for (var child : children) {
            child.getShapes(allShapes);
        }
    }

    public Paint getFillPaint() {
        return fillPaint;
    }

    public Paint getStrokePaint() {
        return strokePaint;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public float getStrokeMiterLimit() {
        return strokeMiterLimit;
    }

    public int getStrokeCap() {
        return strokeCap;
    }

    public int getStrokeJoin() {
        return strokeJoin;
    }

    public Affine getTransform() {
        return transform == null ? null : new Affine(transform);
    }
}
