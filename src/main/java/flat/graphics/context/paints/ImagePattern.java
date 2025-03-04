package flat.graphics.context.paints;

import flat.backend.SVG;
import flat.graphics.Color;
import flat.graphics.context.enums.CycleMethod;
import flat.graphics.context.Paint;
import flat.graphics.context.Texture2D;
import flat.math.Affine;

public class ImagePattern extends Paint {

    private final float srcx1;
    private final float srcy1;
    private final float srcx2;
    private final float srcy2;
    private final float dstx1;
    private final float dsty1;
    private final float dstx2;
    private final float dsty2;
    private final Texture2D texture;
    private final int color;
    private final Affine transform;
    private final float[] data;
    private CycleMethod cycleMethod;

    ImagePattern(Builder builder) {
        texture = builder.texture;
        color = builder.color;
        srcx1 = builder.srcx1 / builder.texture.getWidth();
        srcy1 = builder.srcy1 / builder.texture.getHeight();
        srcx2 = builder.srcx2 / builder.texture.getWidth();
        srcy2 = builder.srcy2 / builder.texture.getHeight();
        dstx1 = builder.dstx1;
        dsty1 = builder.dsty1;
        dstx2 = builder.dstx2;
        dsty2 = builder.dsty2;
        cycleMethod = builder.cycleMethod == null ? CycleMethod.CLAMP : builder.cycleMethod;
        final float sw = srcx2 - srcx1;
        final float sh = srcy2 - srcy1;
        final float dw = dstx2 - dstx1;
        final float dh = dsty2 - dsty1;
        transform = new Affine()
                .translate(dstx1 - (dw / sw * srcx1), dsty1 - (dh / sh * srcy1))
                .scale(dw / sw, dh / sh);
        if (builder.transform != null) {
            transform.mul(builder.transform);
        }
        data = new float[]{
                transform.m00, transform.m10,
                transform.m01, transform.m11,
                transform.m02, transform.m12};
    }

    @Override
    protected void setInternal(long svgId)  {
        SVG.SetPaintImage(svgId, getTextureId(texture), color, data, cycleMethod.ordinal());
    }

    public float getSrcX1() {
        return srcx1;
    }

    public float getSrcY1() {
        return srcy1;
    }

    public float getSrcX2() {
        return srcx2;
    }

    public float getSrcY2() {
        return srcy2;
    }

    public float getDstX1() {
        return dstx1;
    }

    public float getDstY1() {
        return dsty1;
    }

    public float getDstX2() {
        return dstx2;
    }

    public float getDstY2() {
        return dsty2;
    }

    public Texture2D getTexture() {
        return texture;
    }

    public CycleMethod getCycleMethod() {
        return cycleMethod;
    }

    public int getColor() {
        return color;
    }

    public Affine getTransform() {
        return new Affine(transform);
    }

    public static class Builder {
        private float srcx1;
        private float srcy1;
        private float srcx2;
        private float srcy2;
        private float dstx1;
        private float dsty1;
        private float dstx2;
        private float dsty2;
        private Texture2D texture;
        private int color = Color.white;
        private Affine transform;
        private CycleMethod cycleMethod;

        public Builder(Texture2D texture) {
            this(texture, 0, 0, texture.getWidth(), texture.getHeight());
        }

        public Builder(Texture2D texture, float x, float y) {
            this(texture, x, y, texture.getWidth(), texture.getHeight());
        }

        public Builder(Texture2D texture, float x, float y, float width, float height) {
            this.texture = texture;
            srcx1 = 0;
            srcy1 = 0;
            srcx2 = texture.getWidth();
            srcy2 = texture.getHeight();
            dstx1 = x;
            dsty1 = y;
            dstx2 = x + width;
            dsty2 = y + height;
        }

        public Builder source(float x1, float y1, float x2, float y2) {
            srcx1 = x1;
            srcy1 = y1;
            srcx2 = x2;
            srcy2 = y2;
            return this;
        }

        public Builder destin(float x1, float y1, float x2, float y2) {
            dstx1 = x1;
            dsty1 = y1;
            dstx2 = x2;
            dsty2 = y2;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder cycleMethod(CycleMethod cycleMethod) {
            this.cycleMethod = cycleMethod;
            return this;
        }

        public Builder transform(Affine transform) {
            this.transform = transform;
            return this;
        }

        public ImagePattern build() {
            return new ImagePattern(this);
        }
    }
}
