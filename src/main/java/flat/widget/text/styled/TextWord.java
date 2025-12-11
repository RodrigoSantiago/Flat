package flat.widget.text.styled;

import java.nio.charset.StandardCharsets;

public class TextWord {
    private byte[] chars;
    private int start;
    private int length;
    private int hash;
    private boolean hashIsZero;
    
    public TextWord(String src) {
        this.chars = src.getBytes(StandardCharsets.UTF_8);
        this.start = 0;
        this.length = chars.length;
    }
    
    public TextWord set(byte[] chars, int start, int end) {
        this.chars = chars;
        this.start = start;
        this.length = end - start;
        hash = 0;
        hashIsZero = false;
        return this;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TextWord textWord = (TextWord) object;
        if (textWord.length == length) {
            for (int i = 0; i < length; i++) {
                if (textWord.chars[i + textWord.start] != chars[i + start]) {
                    return false;
                }
            }
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0 && !hashIsZero) {
            for (int i = 0; i < length; i++) {
                h = 31 * h + chars[i + start];
            }
        }
        return h;
    }
}
