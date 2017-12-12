package flat.math;

public class RoundRect {
    private float x, y, width, height, cTop, cRight, cBottom, cLeft;

    public RoundRect() {
        set(0, 0, 0, 0, 0, 0, 0, 0);
    }

    public RoundRect(float x, float y, float width, float height) {
        set(x, y, width, height, 0, 0, 0, 0);
    }

    public RoundRect(float x, float y, float width, float height, float radius) {
        set(x, y, width, height, radius, radius, radius, radius);
    }

    public RoundRect(float x, float y, float width, float height, float cTop, float cRight, float cBottom, float cLeft) {
        set(x, y, width, height, cTop, cRight, cBottom, cLeft);
    }

    public void set(float x, float y, float width, float height, float cTop, float cRight, float cBottom, float cLeft) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setCorners(cTop, cRight, cBottom, cLeft);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
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

    public float getCornerTop() {
        return cTop;
    }

    public float getCornerRight() {
        return cRight;
    }

    public float getCornerBottom() {
        return cBottom;
    }

    public float getCornerLeft() {
        return cLeft;
    }

    public void setCorners(float cTop, float cRight, float cBottom, float cLeft) {
        this.cTop = cTop;
        this.cRight = cRight;
        this.cBottom = cBottom;
        this.cLeft = cLeft;
    }

    public boolean contains(float px, float py) {
        float x1 = x, y1 = y;
        float x2 = x + width, y2 = y + height;
        if (width <= 0 || height <= 0 || px < x1 || py < y1 || px >= x2 || py >= y2) {
            return false;
        } else {
            float crn;
            if (px < width / 2f) {
                crn = py < height / 2f ? cTop : cLeft;
            } else {
                crn = py < height / 2f ? cRight : cBottom;
            }
            crn = Math.max(0, Math.min(height / 2f, Math.min(width / 2f, crn)));

            if ((x >= (x1 += crn) && x < (x1 = x2 - crn)) || (y >= (y1 += crn) && y < (y1 = y2 - crn))) {
                return true;
            } else {
                x = (x - x1) / crn;
                y = (y - y1) / crn;
                return (x * x + y * y <= 1.0);
            }
        }
    }
}