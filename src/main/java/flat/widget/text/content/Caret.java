package flat.widget.text.content;

public class Caret {

    private int lineChar;
    private int line;
    private int offset;
    private float width;
    private int chars;

    public void set(Caret other) {
        this.setLineChar(other.getLineChar());
        this.setLine(other.getLine());
        this.setOffset(other.getOffset());
        this.setWidth(other.getWidth());
    }
    
    public void set(int line, int lineChar, int offset) {
        this.setLine(line);
        this.setLineChar(lineChar);
        this.setOffset(offset);
    }
    
    public int getChars() {
        return chars;
    }
    
    public void setChars(int chars) {
        this.chars = chars;
    }
    
    public void setLineChar(int lineChar) {
        this.lineChar = lineChar;
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
