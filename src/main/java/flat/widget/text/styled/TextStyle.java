package flat.widget.text.styled;

import java.util.Objects;

public class TextStyle {
    private final int color;
    private final boolean bold;
    private final boolean italic;
    
    public TextStyle(int color, boolean bold, boolean italic) {
        this.bold = bold;
        this.color = color;
        this.italic = italic;
    }
    
    public int getColor() {
        return color;
    }
    
    public boolean isBold() {
        return bold;
    }
    
    public boolean isItalic() {
        return italic;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        TextStyle textStyle = (TextStyle) object;
        return color == textStyle.color && bold == textStyle.bold && italic == textStyle.italic;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(color, bold, italic);
    }
}
