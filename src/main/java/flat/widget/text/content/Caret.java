package flat.widget.text.content;

public class Caret {

    private int lineOffset;
    private int line;
    private int offset;
    private int chars;
    private float width;
    
    public Caret() {
    }
    
    public Caret(Caret copy) {
        set(copy);
    }
    
    public void set(Caret other) {
        this.lineOffset = other.lineOffset;
        this.line = other.line;
        this.offset = other.offset;
        this.width = other.width;
        this.chars = other.chars;
    }
    
    public int getChars() {
        return chars;
    }
    
    public void setChars(int chars) {
        this.chars = chars;
    }
    
    public void setLineOffset(int lineOffset) {
        this.lineOffset = lineOffset;
    }
    
    public void setLine(int line) {
        this.line = line;
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    public void setWidth(float width) {
        this.width = width;
    }
    
    public int getLineOffset() {
        return lineOffset;
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
