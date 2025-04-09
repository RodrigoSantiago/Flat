package flat.graphics.context;

public class Glyph {
    private final float advance, x, y, width, height;

    public Glyph(float advance, float x, float y, float width, float height) {
        this.advance = advance;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public float getAdvance() {
        return advance;
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

    @Override
    public String toString() {
        return "Glyph{" +
                "advance=" + advance +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
