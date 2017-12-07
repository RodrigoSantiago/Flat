package flat.math;

public class RoundRect {
    private float width, height, radius;

    public RoundRect() {
        set(0, 0, 0);
    }

    public RoundRect(float width, float height) {
        set(width, height, 0);
    }

    public RoundRect(float width, float height, float radius) {
        set(width, height, radius);
    }

    public void set(float width, float height, float radius) {
        this.width = width;
        this.height = height;
        this.radius = radius;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public boolean contains(float px, float py) {
        if (width <= 0 && height <= 0) return false;

        if (px < 0 || py < 0 || px >= width || py >= height) {
            return false;
        } else {
            float x1, y1;
            float aw = Math.min(width, radius) / 2.0f;
            float ah = Math.min(height, radius) / 2.0f;

            if ((px >= (x1 = aw) && px < (x1 = width - aw)) || (py >= (y1 = ah) && py < (y1 = height - ah))) {
                return true;
            } else {
                px = (px - x1) / aw;
                py = (py - y1) / ah;
                return (px * px + py * py <= 1.0);
            }
        }
    }
}
