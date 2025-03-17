package flat.graphics.context.paints;

import flat.backend.SVG;
import flat.exception.FlatException;
import flat.graphics.Color;
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
        if (builder.transform != null) {
            data = new float[]{
                    builder.transform.m00, builder.transform.m10,
                    builder.transform.m01, builder.transform.m11,
                    builder.transform.m02, builder.transform.m12,
            };
        } else {
            data = null;
        }
    }

    public GaussianShadow(GaussianShadow other, int color) {
        this.x = other.x;
        this.y = other.y;
        this.width = other.width;
        this.height = other.height;
        this.corners = other.corners;
        this.blur = other.blur;
        this.alpha = other.alpha;
        this.data = other.data;
        this.color = color;
    }

    @Override
    protected void setInternal(long svgId)  {
        SVG.SetPaintBoxGradient(svgId, x, y, width, height, corners, blur, alpha, color, data);
    }

    @Override
    public Paint multiply(int color) {
        return color == -1 ? this : new GaussianShadow(this, Color.multiply(this.color, color));
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
        return data == null ? new Affine() : new Affine(data[0], data[2], data[1], data[3], data[4], data[5]);
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
        private boolean readOnly;

        public Builder(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        private void checkReadOnly() {
            if (readOnly) {
                throw new FlatException("The builder is read only after building");
            }
        }

        public Builder transform(Affine transform) {
            checkReadOnly();
            this.transform = transform;
            return this;
        }

        public Builder corners(float corners) {
            checkReadOnly();
            this.corners = corners;
            return this;
        }

        public Builder blur(float blur) {
            checkReadOnly();
            this.blur = blur;
            return this;
        }

        public Builder alpha(float alpha) {
            checkReadOnly();
            this.alpha = alpha;
            return this;
        }

        public Builder color(int color) {
            checkReadOnly();
            this.color = color;
            return this;
        }

        public GaussianShadow build() {
            checkReadOnly();
            readOnly = true;
            return new GaussianShadow(this);
        }
    }
}
