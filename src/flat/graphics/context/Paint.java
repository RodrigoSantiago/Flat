package flat.graphics.context;

public final class Paint {
    protected final float[] stops = new float[16];
    protected final int[] colors = new int[16];
    protected int stopsCount;
    protected float radiusIn, radiusOut;
    protected float x1, y1, x2, y2;
    protected boolean radial;

    public Paint() {

    }

    public Paint(float x1, float y1, float x2, float y2, float[] stops, int[] colors) {
        setLinear(x1, y1, x2, y2, stops, colors);
    }

    public Paint(float x1, float y1, float radius, float[] stops, int[] colors) {
        setRadial(x1, y1, 0, radius, stops, colors);
    }

    public void set(Paint paint) {
        this.radial = paint.radial;
        this.radiusIn = paint.radiusIn;
        this.radiusOut = paint.radiusOut;
        this.x1 = paint.x1;
        this.x2 = paint.x2;
        this.y1 = paint.y1;
        this.y2 = paint.y2;
        this.stopsCount = paint.stopsCount;
        System.arraycopy(paint.stops, 0, this.stops, 0,16);
        System.arraycopy(paint.colors, 0, this.colors, 0, 16);
    }

    public void setLinear(float x1, float y1, float x2, float y2, float[] stops, int[] colors) {
        radial = false;
        setX1(x1);
        setY1(y1);
        setX2(x2);
        setY2(y2);
        setStopsCount(stops.length);
        System.arraycopy(stops, 0, this.stops, 0, Math.min(stops.length, this.stops.length));
        System.arraycopy(colors, 0, this.colors, 0, Math.min(colors.length, this.colors.length));
    }

    public void setRadial(float x1, float y1, float radiusIn, float radiusOut, float[] stops, int[] colors) {
        radial = true;
        setRadiusIn(radiusIn);
        setRadiusOut(radiusOut);
        setX1(x1);
        setY1(y1);
        setStopsCount(stops.length);
        System.arraycopy(stops, 0, this.stops, 0, Math.min(stops.length, this.stops.length));
        System.arraycopy(colors, 0, this.colors, 0, Math.min(colors.length, this.colors.length));
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

    public boolean isRadial() {
        return radial;
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
}
