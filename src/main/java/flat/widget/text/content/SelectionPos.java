package flat.widget.text.content;

import java.util.Objects;

public class SelectionPos {
    private final int line;
    private final int character;
    
    public SelectionPos(int line, int character) {
        this.line = line;
        this.character = character;
    }
    
    public int getLine() {
        return line;
    }
    
    public int getCharacter() {
        return character;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        SelectionPos that = (SelectionPos) object;
        return line == that.line && character == that.character;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(line, character);
    }
}
