package flat.graphics.svg;

public class Arc {
    float x, y, radius, angleA, angleB;

    public Arc() {
    }

    public Arc(float x, float y, float radius, float angleA, float angleB) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.angleA = angleA;
        this.angleB = angleB;
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

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getAngleA() {
        return angleA;
    }

    public void setAngleA(float angleA) {
        this.angleA = angleA;
    }

    public float getAngleB() {
        return angleB;
    }

    public void setAngleB(float angleB) {
        this.angleB = angleB;
    }
}
