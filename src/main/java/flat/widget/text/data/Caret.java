package flat.widget.text.data;

public class Caret {

    int lineChar;
    int line;
    int offset;
    float width;

    public void set(Caret other) {
        this.lineChar = other.lineChar;
        this.line = other.line;
        this.offset = other.offset;
        this.width = other.width;
    }

    public int getLineChar() {
        return lineChar;
    }

    public int getLine() {
        return line;
    }

    public int getOffset() {
        return offset;
    }

    public float getWidth() {
        return width;
    }
}
