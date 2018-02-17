package flat.graphics.context;

import flat.math.Affine;

public final class Paint {
    public enum CycleMethod {CLAMP, REPEATE, REFLECT}

    private static final Affine identity = new Affine();

    protected int type;
    protected Affine transform;
    protected CycleMethod cycleMethod;

    protected int color;

    protected float[] stops;
    protected int[] colors;

    protected float x1, y1, x2, y2;

    protected float corners, alpha, blur;

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
        paint.transform = transform == null ? identity : new Affine(transform);
        return paint;
    }

    public static Paint radial(float x1, float y1, float radiusIn, float radiusOut, float[] stops, int[] colors) {
        return radial(x1, y1, radiusIn, radiusOut, stops, colors, CycleMethod.CLAMP);
    }

    public static Paint radial(float x1, float y1, float radiusIn, float radiusOut, float[] stops, int[] colors, CycleMethod cycleMethod) {
        return radial(x1, y1, radiusIn, radiusOut, stops, colors, cycleMethod, null);
    }

    public static Paint radial(float x1, float y1, float radiusIn, float radiusOut, float[] stops, int[] colors, CycleMethod cycleMethod, Affine transform) {
        Paint paint = new Paint(2);
        paint.x1 = x1;
        paint.y1 = y1;
        paint.x2 = radiusIn;
        paint.y2 = radiusOut;
        paint.stops = stops.clone();
        paint.colors = colors.clone();
        paint.cycleMethod = cycleMethod;
        paint.transform = transform == null ? identity : new Affine(transform);
        return paint;
    }

    public static Paint shadow(float x1, float y1, float x2, float y2, float corners, float blur, float alpha) {
        return shadow(x1, y1, x2, y2, corners, blur, alpha, null);
    }

    public static Paint shadow(float x1, float y1, float x2, float y2, float corners, float blur, float alpha, Affine transform) {
        Paint paint = new Paint(3);
        paint.x1 = Math.min(x1, x2);
        paint.y1 = Math.min(y1, y2);
        paint.x2 = Math.abs(x1 - x2);
        paint.y2 = Math.abs(y1 - y2);
        paint.corners = corners;
        paint.blur = blur;
        paint.alpha = alpha;
        paint.transform = transform == null ? identity : new Affine(transform);
        return paint;
    }

    public static Paint image(float srcx1, float srcy1, float srcx2, float srcy2,
                              float dstx1, float dsty1, float dstx2, float dsty2,
                              Texture2D texture, Affine transform) {
        final float sw = srcx2 - srcx1;
        final float sh = srcy2 - srcy1;
        final float dw = dstx2 - dstx1;
        final float dh = dsty2 - dsty1;
        final float xs = texture.getWidth() / sw;
        final float ys = texture.getHeight() / sh;
        Paint paint = new Paint(4);
        paint.x1 = dstx1 - srcx1 * xs;
        paint.y1 = dsty1 - srcy1 * ys;
        paint.x2 = dw * xs;
        paint.y2 = dh * ys;
        paint.texture = texture;
        paint.transform = transform == null ? new Affine(identity) : new Affine(transform);
        return paint;
    }
    public static Paint image(float x, float y, float width, float height, Texture2D texture) {
        return image(x, y, width, height, texture, null);
    }

    public static Paint image(float x, float y, float width, float height, Texture2D texture, Affine transform) {
        Paint paint = new Paint(4);
        paint.x1 = x;
        paint.y1 = y;
        paint.x2 = x + width;
        paint.y2 = y + height;
        paint.texture = texture;
        paint.transform = transform == null ? identity : new Affine(transform);
        return paint;
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