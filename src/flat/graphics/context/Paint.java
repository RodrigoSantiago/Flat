package flat.graphics.context;

public final class Paint {
    public enum CycleMethod {CLAMP, REPEATE, REFLECT}
    public enum Interpolation {LINEAR, CIRCLEIN, CIRCLEOUT, FADE}

    protected final float[] stops = new float[16];
    protected final int[] colors = new int[16];
    protected int stopsCount;
    protected CycleMethod cycleMethod = CycleMethod.CLAMP;
    protected Interpolation interpolation = Interpolation.LINEAR;
    protected float radiusIn, radiusOut;
    protected float x1, y1, x2, y2;
    protected float corners, alpha, blur;
    protected int type;

    public Paint() {

    }

    public Paint(Paint paint) {
        set(paint);
    }

    public void set(Paint paint) {
        this.type = paint.type;
        this.radiusIn = paint.radiusIn;
        this.radiusOut = paint.radiusOut;
        this.x1 = paint.x1;
        this.x2 = paint.x2;
        this.y1 = paint.y1;
        this.y2 = paint.y2;
        this.corners = paint.corners;
        this.alpha = paint.alpha;
        this.blur = paint.blur;
        this.stopsCount = paint.stopsCount;
        this.cycleMethod = paint.cycleMethod;
        this.interpolation = paint.interpolation;
        System.arraycopy(paint.stops, 0, this.stops, 0,16);
        System.arraycopy(paint.colors, 0, this.colors, 0, 16);
    }

    public void setLinear() {
        type = 0;
    }

    public void setLinear(float x1, float y1, float x2, float y2, float[] stops, int[] colors) {
        type = 0;
        setX1(x1);
        setY1(y1);
        setX2(x2);
        setY2(y2);
        setStopsCount(stops.length);
        System.arraycopy(stops, 0, this.stops, 0, Math.min(stops.length, this.stops.length));
        System.arraycopy(colors, 0, this.colors, 0, Math.min(colors.length, this.colors.length));
    }

    public void setRadial() {
        type = 1;
    }

    public void setRadial(float x1, float y1, float radiusIn, float radiusOut, float[] stops, int[] colors) {
        type = 1;
        setRadiusIn(radiusIn);
        setRadiusOut(radiusOut);
        setX1(x1);
        setY1(y1);
        setStopsCount(stops.length);
        System.arraycopy(stops, 0, this.stops, 0, Math.min(stops.length, this.stops.length));
        System.arraycopy(colors, 0, this.colors, 0, Math.min(colors.length, this.colors.length));
    }

    public void setBoxShadow() {
        type = 2;
    }

    public void setBoxShadow(float x1, float y1, float x2, float y2, float corners, float blur, float alpha) {
        type = 2;
        setX1(x1);
        setY1(y1);
        setX2(x2);
        setY2(y2);
        setCorners(corners);
        setBlur(blur);
        setAlpha(alpha);
    }

    public CycleMethod getCycleMethod() {
        return cycleMethod;
    }

    public void setCycleMethod(CycleMethod cycleMethod) {
        this.cycleMethod = cycleMethod == null ? CycleMethod.CLAMP : cycleMethod;
    }

    public Interpolation getInterpolation() {
        return interpolation;
    }

    public void setInterpolation(Interpolation interpolation) {
        this.interpolation = interpolation;
    }

    public int getStopsCount() {
        return stopsCount;
    }

    public void setStopsCount(int stopsCount) {
        this.stopsCount = stopsCount;
    }

    public float getStop(int id) {
        return stops[id];
    }

    public void setStop(int id, float value) {
        stops[id] = value;
    }

    public int getColor(int id) {
        return colors[id];
    }

    public void setColor(int id, int value) {
        colors[id] = value;
    }

    public boolean isLinear() {
        return type == 0;
    }

    public boolean isRadial() {
        return type == 1;
    }

    public boolean isShadow() {
        return type == 2;
    }

    public float getX1() {
        return x1;
    }

    public void setX1(float x1) {
        this.x1 = x1;
    }

    public float getY1() {
        return y1;
    }

    public void setY1(float y1) {
        this.y1 = y1;
    }

    public float getX2() {
        return x2;
    }

    public void setX2(float x2) {
        this.x2 = x2;
    }

    public float getY2() {
        return y2;
    }

    public void setY2(float y2) {
        this.y2 = y2;
    }

    public float getRadiusIn() {
        return radiusIn;
    }

    public void setRadiusIn(float radiusIn) {
        this.radiusIn = radiusIn;
    }

    public float getRadiusOut() {
        return radiusOut;
    }

    public void setRadiusOut(float radiusOut) {
        this.radiusOut = radiusOut;
    }

    public float getCorners() {
        return corners;
    }

    public void setCorners(float corners) {
        this.corners = corners;
    }

    public float getBlur() {
        return blur;
    }

    public void setBlur(float blur) {
        this.blur = blur;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
}
