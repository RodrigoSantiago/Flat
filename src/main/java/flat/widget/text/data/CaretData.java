package flat.widget.text.data;

public class CaretData {
    public int lineChar;
    public int line;
    public int offset;
    public float width;

    public void set(CaretData other) {
        this.lineChar = other.lineChar;
        this.line = other.line;
        this.offset = other.offset;
        this.width = other.width;
    }
}
