package flat.graphics.context.paints;

import flat.backend.SVG;
import flat.graphics.context.Paint;
import flat.math.Affine;
import flat.math.Mathf;

public class GaussianShadow extends Paint {

    private final float x;
    private final float y;
    private final float width;
    private final float height;
    private final float corners;
    private final float blur;
    private final float alpha;
    private final int color;
    private final Affine transform;
    private final float[] data;

    GaussianShadow(Builder builder) {
        x = builder.x;
        y = builder.y;
        width = builder.width;
        height = builder.height;
        corners = builder.corners;
        blur = builder.blur;
        alpha = Mathf.clamp(builder.alpha, 0, 1);
        color = builder.color;
        transform = builder.transform == null ? new Affine() : builder.transform;
        data = new float[]{
                transform.m00, transform.m10,
                transform.m01, transform.m11,
                transform.m02, transform.m12,
        };
    }

    @Override
    protected void setInternal(long svgId)  {
        SVG.SetPaintBoxGradient(svgId, x, y, width, height, corners, blur, alpha, color, data);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getCorners() {
        return corners;
    }

    public float getBlur() {
        return blur;
    }

    public float getAlpha() {
        return alpha;
    }

    public int getColor() {
        return color;
    }

    public Affine getTransform() {
        return new Affine(transform);
    }

    public static class Builder {
        private float x;
        private float y;
        private float width;
        private float height;
        private float corners;
        private float blur;
        private float alpha;
        private int color;
        private Affine transform;

        public Builder(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Builder transform(Affine transform) {
            this.transform = transform;
            return this;
        }

        public Builder corners(float corners) {
            this.corners = corners;
            return this;
        }

        public Builder blur(float blur) {
            this.blur = blur;
            return this;
        }

        public Builder alpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public GaussianShadow build() {
            return new GaussianShadow(this);
        }
    }
}
