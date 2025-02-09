package flat.graphics.context;

import flat.math.Affine;
import flat.math.Mathf;

public final class Paint {
    public enum CycleMethod {CLAMP, REPEAT, REFLECT}

    private static final float[] identity = new float[]{1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f};

    protected int type;
    protected float[] transform;
    protected float[] transformImage;
    protected CycleMethod cycleMethod;

    protected int color;

    protected float[] stops;
    protected int[] colors;

    protected float x1, y1, x2, y2, fx, fy;

    protected float corners, blur;

    protected Texture2D texture;

    public static Paint color(int color) {
        Paint paint = new Paint(0);
        paint.color = color;
        paint.transform = identity;
        return paint;
    }

    public static Paint linear(float x1, float y1, float x2, float y2, float[] stops, int[] colors) {
        return linear(x1, y1, x2, y2, stops, colors, CycleMethod.CLAMP);
    }

    public static Paint linear(float x1, float y1, float x2, float y2, float[] stops, int[] colors, CycleMethod cycleMethod) {
        return linear(x1, y1, x2, y2, stops, colors, cycleMethod, null);
    }

    public static Paint linear(float x1, float y1, float x2, float y2, float[] stops, int[] colors, CycleMethod cycleMethod, Affine transform) {
        Paint paint = new Paint(1);
        paint.x1 = x1;
        paint.y1 = y1;
        paint.x2 = x2;
        paint.y2 = y2;
        paint.stops = stops.clone();
        paint.colors = colors.clone();
        paint.cycleMethod = cycleMethod;
        paint.transform = transform == null ? identity :
                new float[]{
                        transform.m00, transform.m10,
                        transform.m01, transform.m11,
                        transform.m02, transform.m12};
        return paint;
    }

    public static Paint radial(float x1, float y1, float radiusIn, float radiusOut, float[] stops, int[] colors) {
        return radial(x1, y1, radiusIn, radiusOut, 0, 0, stops, colors, CycleMethod.CLAMP);
    }

    public static Paint radial(float x1, float y1, float radiusIn, float radiusOut, float fx, float fy, float[] stops, int[] colors, CycleMethod cycleMethod) {
        return radial(x1, y1, radiusIn, radiusOut, fx, fy, stops, colors, cycleMethod, null);
    }

    public static Paint radial(float x1, float y1, float radiusIn, float radiusOut, float fx, float fy, float[] stops, int[] colors, CycleMethod cycleMethod, Affine transform) {
        Paint paint = new Paint(2);
        paint.x1 = x1;
        paint.y1 = y1;
        paint.x2 = radiusIn;
        paint.y2 = radiusOut;
        paint.fx = fx;
        paint.fy = fy;
        paint.stops = stops.clone();
        paint.colors = colors.clone();
        paint.cycleMethod = cycleMethod;
        paint.transform = transform == null ? identity :
                new float[]{
                        transform.m00, transform.m10,
                        transform.m01, transform.m11,
                        transform.m02, transform.m12};
        return paint;
    }

    public static Paint shadow(float x1, float y1, float x2, float y2, float corners, float blur, float alpha) {
        return shadow(x1, y1, x2, y2, corners, blur, alpha, null);
    }

    public static Paint shadow(float x1, float y1, float x2, float y2, float corners, float blur, float alpha, Affine transform) {
        alpha = Mathf.clamp(alpha, 0, 1);

        Paint paint = new Paint(3);
        paint.x1 = Math.min(x1, x2);
        paint.y1 = Math.min(y1, y2);
        paint.x2 = Math.abs(x1 - x2);
        paint.y2 = Math.abs(y1 - y2);
        paint.corners = corners;
        paint.blur = blur;
        paint.stops = new float[]{0.0f, 0.6f, 1.0f};
        paint.colors = new int[]{0x000000FF & (int) (alpha * 255), 0x000000FF & (int) (alpha * 24), 0};
        paint.cycleMethod = CycleMethod.CLAMP;
        paint.transform = transform == null ? identity :
                new float[]{
                        transform.m00, transform.m10,
                        transform.m01, transform.m11,
                        transform.m02, transform.m12};
        return paint;
    }

    public static Paint image(float srcx1, float srcy1, float srcx2, float srcy2,
                              float dstx1, float dsty1, float dstx2, float dsty2,
                              Texture2D texture, int color, Affine transform) {
        srcx1 = srcx1 / texture.getWidth();
        srcy1 = srcy1 / texture.getHeight();
        srcx2 = srcx2 / texture.getWidth();
        srcy2 = srcy2 / texture.getHeight();
        final float sw = srcx2 - srcx1;
        final float sh = srcy2 - srcy1;
        final float dw = dstx2 - dstx1;
        final float dh = dsty2 - dsty1;
        Affine inner = new Affine()
                .translate(dstx1 - (dw / sw * srcx1), dsty1 - (dh / sh * srcy1))
                .scale(dw / sw, dh / sh);
        if (transform != null) {
            inner.mul(transform);
        }
        Paint paint = new Paint(4);
        paint.texture = texture;
        paint.color = color;
        paint.transform = identity;
        paint.transformImage =
                new float[]{
                        inner.m00, inner.m10,
                        inner.m01, inner.m11,
                        inner.m02, inner.m12};
        return paint;
    }

    public static Paint image(float x, float y, float width, float height, Texture2D texture) {
        return image(x, y, width, height, texture, 0xFFFFFFFF);
    }

    public static Paint image(float x, float y, float width, float height, Texture2D texture, int color) {
        return image(x, y, width, height, texture, color, null);
    }

    public static Paint image(float x, float y, float width, float height, Texture2D texture, int color, Affine transform) {
        return image(0, 0, texture.getWidth(), texture.getHeight(), x, y, width, height, texture, color, transform);
    }

    private Paint(int type) {
        this.type = type;
    }

    public boolean isColor() {
        return type == 0;
    }

    public boolean isLinearGradient() {
        return type == 1;
    }

    public boolean isRadialGradient() {
        return type == 2;
    }

    public boolean isBoxShadow() {
        return type == 3;
    }

    public boolean isImagePattern() {
        return type == 4;
    }
}