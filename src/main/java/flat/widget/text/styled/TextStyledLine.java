package flat.widget.text.styled;

import flat.widget.text.content.CaretControl;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TextStyledLine {
    
    private final TextStyledContent parent;
    
    private ByteBuffer buffer;
    private byte[] textBytes;
    private int size;
    private int capacity;
    private int color;
    private boolean invalid = true;
    
    public TextStyledLine(TextStyledContent parent, int capacity) {
        this.parent = parent;
        this.capacity = capacity;
        textBytes = new byte[capacity * 2];
    }
    
    private boolean isChar(byte ch) {
        return ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_');
    }
    
    private boolean isHex(byte ch) {
        return ((ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F') || (ch >= '0' && ch <= '9'));
    }
    
    private void setStyleRange(int style, int start, int end) {
        int s = capacity + start;
        int e = capacity + end;
        for (int i = s; i < e; i++) {
            textBytes[i] = (byte) style;
        }
    }
    
    ByteBuffer getRenderByteBuffer() {
        if (invalid) {
            if (buffer == null || buffer.capacity() < capacity * 2) {
                buffer = ByteBuffer.allocateDirect(capacity * 2);
            }
            
            TextWord search = new TextWord("");
            int prev = 0;
            int state = 0;
            int style = 0;
            boolean identifier = false;
            byte lastCh = '\0';
            byte init = '\0';
            boolean rev = false;
            for (int i = 0; i < size; i ++) {
                byte ch = textBytes[i];
                if (state == 0) {
                    prev = i;
                    if (isChar(ch)) {
                        state = 1;
                        identifier = lastCh != '.';
                    } else if (ch >= '0' && ch <= '9') {
                        state = 2;
                    } else if (ch == '"' || ch == '\'') {
                        init = ch;
                        state = 3;
                    } else if (ch == '#') {
                        state = 4;
                    } else if (ch == '/' && i + 1 < size && textBytes[i + 1] == '/' && parent.getBundle().getCommentStyle() != 0) {
                        state = 5;
                        break;
                    } else if (ch != ' ') {
                        textBytes[capacity + prev] = (byte) parent.getBundle().findCharStyle((char) ch);
                    } else {
                        textBytes[capacity + prev] = 0;
                    }
                } else if (state == 1) {
                    if (!isChar(ch) && !(ch >= '0' && ch <= '9')) {
                        int s = parent.getBundle().findWordStyle(search.set(textBytes, prev, i));
                        if (s == 0 && identifier) s = parent.getBundle().findIdentifierStyle(search);
                        setStyleRange(s, prev, i);
                        state = 0;
                        i--;
                    }
                } else if (state == 2) {
                    if (ch!= '.' && !(ch >= '0' && ch <= '9')) {
                        setStyleRange(parent.getBundle().getNumberStyle(), prev, i);
                        state = 0;
                        i--;
                    }
                } else if (state == 3) {
                    if (ch == '\\') {
                        rev = !rev;
                    } else if (!rev && ch == init) {
                        setStyleRange(init == '\'' ? parent.getBundle().getCharsetStyle() : parent.getBundle().getStringStyle(), prev, i + 1);
                        state = 0;
                    } else {
                        rev = false;
                    }
                } else if (state == 4) {
                    if (!isHex(ch)) {
                        setStyleRange(parent.getBundle().getHexStyle(), prev, i);
                        state = 0;
                        i--;
                    }
                }
                if (ch != ' ') lastCh = ch;
            }
            
            if (state == 1) {
                int s = parent.getBundle().findWordStyle(search.set(textBytes, prev, size));
                if (s == 0 && identifier) s = parent.getBundle().findIdentifierStyle(search);
                setStyleRange(s, prev, size);
            } else if (state == 2) {
                setStyleRange(parent.getBundle().getNumberStyle(), prev, size);
            } else if (state == 3) {
                setStyleRange(init == '\'' ? parent.getBundle().getCharsetStyle() : parent.getBundle().getStringStyle(), prev, size);
            } else if (state == 4) {
                setStyleRange(parent.getBundle().getHexStyle(), prev, size);
            } else if (state == 5) {
                setStyleRange(parent.getBundle().getCommentStyle(), prev, size);
            }
            
            buffer.position(0);
            buffer.put(textBytes, 0, size);
            buffer.position(capacity);
            buffer.put(textBytes, capacity, size);
            invalid = false;
        }
        return buffer;
    }
    
    byte[] getTextBytes() {
        return textBytes;
    }
    
    public int getSize() {
        return size;
    }
    
    public int getCapacity() {
        return capacity;
    }
    
    public void setLength(int offset) {
        invalidate();
        size = offset;
    }
    
    private void ensureCapacity(int charCount) {
        int oldSize = size;
        int oldCapacity = capacity;
        size = charCount;
        
        if (capacity >= size) return;
        
        while (capacity < charCount) {
            capacity = (int) (capacity * 1.5) / 2 * 2;
            if (capacity <= 0) {
                throw new RuntimeException("Memory overflow");
            }
        }
        
        byte[] newBuf = new byte[capacity * 2];
        
        System.arraycopy(textBytes, 0, newBuf, 0, oldSize);
        System.arraycopy(textBytes, oldCapacity, newBuf, capacity, oldSize);
        
        textBytes = newBuf;
    }
    
    public void putChar(byte[] src, int start, int end) {
        int len = end - start;
        
        int localStart = size;
        invalidate();
        ensureCapacity(size + len);
        System.arraycopy(src, start, textBytes, localStart, len);
    }
    
    public void putLine(TextStyledLine line) {
        invalidate();
        int index = size;
        ensureCapacity(size + line.size);
        if (line.size >= 0) {
            System.arraycopy(line.textBytes, 0, textBytes, index, line.size);
        }
    }
    
    public TextStyledLine recycle(TextStyledLine copy, int offset) {
        setLength(0);
        setColor(0);
        if (copy != null) {
            color = copy.color;
            ensureCapacity(copy.size - offset);
            if (size >= 0) {
                System.arraycopy(copy.textBytes, offset, textBytes, 0, size);
            }
        }
        return this;
    }
    
    public void invalidate() {
        invalid = true;
    }
    
    public String getString() {
        return new String(textBytes, 0, size, StandardCharsets.UTF_8);
    }
    
    public String getSubString(int startChar, int endChar) {
        return new String(textBytes, startChar, endChar - startChar, StandardCharsets.UTF_8);
    }
    
    public int getCharsCount() {
        return CaretControl.countChars(textBytes, 0, size);
    }
    
    public void setColor(int color) {
        this.color = color;
    }
    
    public int getColor() {
        return color;
    }
}
